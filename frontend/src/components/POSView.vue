<script setup>
import { ref, onMounted, computed } from 'vue';
import axios from 'axios';
import Swal from 'sweetalert2';

const apiUrl = '/api';
const productos = ref([]);
const carrito = ref([]);

const fetchProductos = async () => {
  try {
    const { data } = await axios.get(`${apiUrl}/productos`);
    productos.value = data;
  } catch (error) {
    console.error('Error fetching products', error);
    Swal.fire('Error', 'No se pudieron cargar los productos', 'error');
  }
};

onMounted(() => {
  fetchProductos();
});

const getCartItem = (producto) => carrito.value.find(item => item.id === producto.id);

const agregarAlCarrito = (producto) => {
  const item = getCartItem(producto);
  if (item) {
    item.cantidad++;
  } else {
    carrito.value.push({ ...producto, cantidad: 1 });
  }
};

const ajustarCantidad = (id, delta) => {
  const index = carrito.value.findIndex(item => item.id === id);
  if (index !== -1) {
    carrito.value[index].cantidad += delta;
    if (carrito.value[index].cantidad <= 0) {
      carrito.value.splice(index, 1);
    }
  }
};

const total = computed(() => {
  return carrito.value.reduce((sum, item) => sum + (item.precio * item.cantidad), 0);
});

const cobrar = async () => {
  if (carrito.value.length === 0) return;

  const requestData = {
    productos: carrito.value.map(item => ({ id: item.id, cantidad: item.cantidad }))
  };

  try {
    await axios.post(`${apiUrl}/ventas`, requestData);
    Swal.fire({
      title: '¡Cobro Exitoso!',
      text: 'La venta se ha registrado correctamente.',
      icon: 'success',
      timer: 1500,
      showConfirmButton: false
    });
    carrito.value = []; // Limpiar carrito
  } catch (error) {
    console.error('Error procesando cobro', error);
    Swal.fire('Error', 'Hubo un problema al procesar el cobro', 'error');
  }
};
</script>

<template>
  <div class="flex gap-6 h-full w-full">
    <!-- Panel Izquierdo: Catálogo -->
    <div class="w-2/3 bg-white rounded-2xl shadow-xl p-6 flex flex-col h-full border border-slate-100">
      <h2 class="text-3xl font-bold mb-6 text-slate-700">Catálogo de Productos</h2>
      <div class="grid grid-cols-2 lg:grid-cols-3 gap-6 flex-grow overflow-y-auto pr-2">
        <button 
          v-for="producto in productos" 
          :key="producto.id"
          @click="agregarAlCarrito(producto)"
          class="bg-gradient-to-br from-indigo-50 to-indigo-100 hover:from-indigo-100 hover:to-indigo-200 
                 border-2 border-indigo-200 hover:border-indigo-400 rounded-2xl p-6 flex flex-col 
                 items-center justify-center transition-all duration-300 transform hover:scale-[1.02] shadow-sm hover:shadow-md cursor-pointer h-48 group">
          <span class="text-2xl font-bold text-center text-indigo-900 leading-tight mb-4 group-hover:text-indigo-700">
            {{ producto.nombre }}
          </span>
          <span class="text-2xl font-black text-emerald-600 bg-white px-4 py-2 rounded-xl shadow-inner">
            ${{ producto.precio.toFixed(2) }}
          </span>
        </button>
      </div>
    </div>

    <!-- Panel Derecho: Carrito/Ticket -->
    <div class="w-1/3 bg-white rounded-2xl shadow-xl flex flex-col h-full border border-slate-100 overflow-hidden">
      <!-- Cabecera Carrito -->
      <div class="bg-slate-800 text-white p-6">
        <h2 class="text-2xl font-bold flex items-center justify-between">
          <span>Ticket de Venta</span>
          <span class="bg-indigo-500 text-sm py-1 px-3 rounded-full">{{ carrito.length }} items</span>
        </h2>
      </div>

      <!-- Lista de Items -->
      <div class="flex-grow overflow-y-auto p-4 space-y-3 bg-slate-50">
        <div v-if="carrito.length === 0" class="h-full flex items-center justify-center text-slate-400 text-lg font-medium">
          El carrito está vacío
        </div>
        
        <div v-for="item in carrito" :key="item.id" 
             class="bg-white p-4 rounded-xl shadow-sm border border-slate-200 flex flex-col gap-2">
          <div class="flex justify-between items-start">
            <span class="font-bold text-slate-700 text-lg leading-tight w-2/3">{{ item.nombre }}</span>
            <span class="font-bold text-emerald-600">${{ (item.precio * item.cantidad).toFixed(2) }}</span>
          </div>
          
          <div class="flex justify-between items-center mt-2">
            <span class="text-slate-500 text-sm">Ud: ${{ item.precio.toFixed(2) }}</span>
            <div class="flex items-center gap-3 bg-slate-100 p-1 rounded-lg">
              <button @click="ajustarCantidad(item.id, -1)" 
                      class="w-10 h-10 rounded-md bg-white shadow-sm font-bold text-xl text-slate-600 hover:bg-slate-200 flex items-center justify-center transition-colors">
                -
              </button>
              <span class="font-black text-xl w-8 text-center">{{ item.cantidad }}</span>
              <button @click="ajustarCantidad(item.id, 1)" 
                      class="w-10 h-10 rounded-md bg-indigo-600 shadow-sm font-bold text-xl text-white hover:bg-indigo-700 flex items-center justify-center transition-colors">
                +
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Totales y Cobro -->
      <div class="bg-white p-6 border-t font-mono shadow-[0_-10px_30px_-15px_rgba(0,0,0,0.1)]">
        <div class="flex justify-between items-end mb-6">
          <span class="text-slate-500 text-xl font-sans font-semibold">TOTAL:</span>
          <span class="text-6xl font-black text-emerald-600 tracking-tighter">${{ total.toFixed(2) }}</span>
        </div>
        <button 
          @click="cobrar()"
          :disabled="carrito.length === 0"
          class="w-full bg-emerald-500 hover:bg-emerald-600 disabled:bg-slate-300 disabled:cursor-not-allowed 
                 text-white text-3xl font-black py-6 rounded-2xl shadow-lg hover:shadow-xl 
                 transition-all duration-300 transform hover:-translate-y-1 active:translate-y-0 uppercase tracking-widest">
          Cobrar
        </button>
      </div>
    </div>
  </div>
</template>
