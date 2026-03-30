<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import axios from 'axios';
import Swal from 'sweetalert2';

const apiUrl = '/api';
const emit = defineEmits(['logout']);

const ordenes = ref([]);
const loading = ref(true);
let pollingInterval = null;

const fetchOrdenes = async () => {
  try {
    const { data } = await axios.get(`${apiUrl}/ventas/cocina`);
    ordenes.value = data;
  } catch (error) {
    console.error('Error fetching orders', error);
  } finally {
    loading.value = false;
  }
};

const despacharOrden = async (id) => {
  try {
    await axios.put(`${apiUrl}/ventas/${id}/despachar`);
    // Optimistic UI update
    ordenes.value = ordenes.value.filter(o => o.id !== id);
  } catch (error) {
    console.error('Error dispatching', error);
    Swal.fire('Error', 'No se pudo despachar la orden', 'error');
  }
};

onMounted(() => {
  fetchOrdenes();
  pollingInterval = setInterval(fetchOrdenes, 3000); // Poll every 3 seconds
});

onUnmounted(() => {
  if (pollingInterval) clearInterval(pollingInterval);
});
</script>

<template>
  <div class="h-screen w-full bg-slate-900 text-slate-100 flex flex-col p-6 overflow-hidden">
    <!-- Header -->
    <div class="flex justify-between items-center mb-6 pb-4 border-b border-slate-700">
      <div class="flex items-center gap-4">
        <h1 class="text-4xl font-black text-white flex items-center gap-3">
          <span class="text-5xl">👨‍🍳</span> 
          <span>Monitor de Cocina</span>
        </h1>
        <div class="px-4 py-1 rounded-full bg-indigo-500/20 text-indigo-300 font-bold border border-indigo-500/30 text-sm animate-pulse flex items-center gap-2">
          <div class="w-2 h-2 rounded-full bg-indigo-400"></div>
          EN LÍNEA
        </div>
      </div>
      
      <div class="flex items-center gap-6">
        <div class="text-right">
          <div class="text-3xl font-black text-rose-400">{{ ordenes.length }}</div>
          <div class="text-sm font-bold text-slate-400 uppercase tracking-widest">En Cola</div>
        </div>
        <button @click="emit('logout')" class="bg-slate-800 hover:bg-slate-700 border border-slate-600 px-6 py-3 rounded-xl font-bold text-slate-300 shadow transition flex items-center gap-2">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
          </svg>
          Salir
        </button>
      </div>
    </div>

    <!-- Body / Queue -->
    <div v-if="loading" class="flex-grow flex items-center justify-center">
      <div class="animate-spin h-16 w-16 border-4 border-slate-600 border-t-emerald-500 rounded-full"></div>
    </div>
    
    <div v-else-if="ordenes.length === 0" class="flex-grow flex flex-col items-center justify-center text-slate-500">
      <svg xmlns="http://www.w3.org/2000/svg" class="h-32 w-32 mb-6 opacity-20" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
      </svg>
      <h2 class="text-4xl font-black text-slate-600">No hay órdenes pendientes</h2>
      <p class="text-xl mt-2 font-medium">¡Buen trabajo! La cocina está limpia.</p>
    </div>

    <!-- The Grid of Orders -->
    <div v-else class="flex-grow overflow-x-auto overflow-y-hidden pb-4">
      <div class="flex gap-6 h-full items-stretch">
        
        <!-- Order Card -->
        <div v-for="orden in ordenes" :key="orden.id" 
             class="flex-shrink-0 w-80 lg:w-96 bg-slate-800 rounded-2xl border-2 border-slate-600 shadow-2xl flex flex-col overflow-hidden transform transition-all duration-300 hover:scale-[1.01] hover:border-emerald-500/50">
          
          <!-- Card Header -->
          <div class="bg-slate-700 px-6 py-4 border-b border-slate-600 flex justify-between items-center">
            <h2 class="text-3xl font-black text-white">#{{ orden.id }}</h2>
            <span class="text-slate-300 font-bold bg-slate-800 px-3 py-1 rounded-lg">{{ orden.fecha }}</span>
          </div>

          <!-- Card Items -->
          <div class="flex-grow overflow-y-auto p-6 space-y-4 bg-slate-800/50">
            <div v-for="item in orden.detalles" :key="item.id" 
                 class="flex items-start gap-4 p-3 rounded-xl bg-slate-700/50 border border-slate-600">
              <div class="w-12 h-12 bg-indigo-500 rounded-lg flex items-center justify-center text-2xl font-black text-white flex-shrink-0 shadow-inner">
                {{ item.cantidad }}
              </div>
              <div class="pt-1">
                <div class="text-xl font-bold text-slate-100 leading-tight">{{ item.productoNombre }}</div>
              </div>
            </div>
          </div>

          <!-- Card Footer Action -->
          <div class="p-6 bg-slate-800 border-t border-slate-600">
            <button @click="despacharOrden(orden.id)" 
                    class="w-full py-5 bg-emerald-600 hover:bg-emerald-500 active:bg-emerald-700 text-white rounded-xl text-2xl font-black tracking-wide shadow-lg shadow-emerald-500/20 transition-all flex items-center justify-center gap-3">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-8 w-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="3" d="M5 13l4 4L19 7" />
              </svg>
              LISTO
            </button>
          </div>

        </div>

      </div>
    </div>
  </div>
</template>
