 <script setup>
import { ref, onMounted } from 'vue';
import Swal from 'sweetalert2';
import POSView from './components/POSView.vue';
import StatsView from './components/StatsView.vue';
import KitchenView from './components/KitchenView.vue';

const currentView = ref('pos'); // 'pos', 'stats', o 'kitchen'
const cajeroActual = ref('');

const solicitarCajero = async () => {
  const result = await Swal.fire({
    title: 'Hornado Solidario',
    text: 'Identifícate para cobrar, o abre el Monitor de Cocina.',
    input: 'text',
    inputPlaceholder: 'Tu Nombre Ej. Juan',
    allowOutsideClick: false,
    showDenyButton: true,
    denyButtonText: '👨‍🍳 Modo Cocina',
    denyButtonColor: '#334155',
    confirmButtonText: 'Abrir Caja',
    confirmButtonColor: '#4f46e5',
    inputValidator: (value) => {
      // validator only triggers on confirm
      if (!value) {
        return '¡Debes ingresar tu nombre de cajero!'
      }
    }
  });

  if (result.isDenied) {
    currentView.value = 'kitchen';
  } else if (result.isConfirmed && result.value) {
    const nombreFormateado = result.value.trim().toUpperCase();
    cajeroActual.value = nombreFormateado;
    localStorage.setItem('cajero_pos', nombreFormateado);
    currentView.value = 'pos';
  }
};

const salirDeCocina = () => {
  currentView.value = 'pos';
  solicitarCajero();
};

const intentarVerEstadisticas = async () => {
  const { value: pin } = await Swal.fire({
    title: 'Área Restringida',
    text: 'Ingrese el PIN del Administrador',
    input: 'password',
    inputAttributes: {
      inputmode: 'numeric',
      pattern: '[0-9]*'
    },
    showCancelButton: true,
    confirmButtonText: 'Acceder',
    cancelButtonText: 'Cancelar'
  });

  if (pin === '160696') {
    currentView.value = 'stats';
  } else if (pin) {
    Swal.fire('Error', 'PIN Incorrecto', 'error');
  }
};

onMounted(() => {
  const guardado = localStorage.getItem('cajero_pos');
  if (guardado) {
    const nombreGuardadoFormat = guardado.trim().toUpperCase();
    cajeroActual.value = nombreGuardadoFormat;
    localStorage.setItem('cajero_pos', nombreGuardadoFormat);
  } else {
    solicitarCajero();
  }
});
</script>

<template>
  <!-- PANTALLA EXCLUSIVA DE COCINA (Full Screen, Dark Mode) -->
  <div v-if="currentView === 'kitchen'" class="h-screen w-full">
    <KitchenView @logout="salirDeCocina" />
  </div>

  <!-- APLICACIÓN NORMAL (Caja / Stats) -->
  <div v-else class="min-h-screen flex flex-col w-full text-slate-800">
    <!-- Navbar -->
    <header class="bg-indigo-600 text-white shadow-md">
      <div class="max-w-7xl mx-auto px-4 py-4 flex justify-between items-center">
        <h1 class="text-2xl font-bold tracking-wider">Hornado Solidario</h1>
        <div class="flex items-center space-x-4">
          
          <button @click="currentView = 'kitchen'" class="bg-slate-800 hover:bg-slate-700 px-4 py-2 rounded-xl text-sm font-bold shadow transition flex items-center gap-2">
            👨‍🍳 Monitor Cocina
          </button>
          
          <div v-if="cajeroActual" class="flex items-center gap-2 bg-indigo-700 px-3 py-1 rounded-full text-indigo-100 font-medium ml-4">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z" clip-rule="evenodd" />
            </svg>
            {{ cajeroActual }}
            <button @click="solicitarCajero" class="ml-2 text-indigo-300 hover:text-white text-xs underline">Cambiar</button>
          </div>
          
          <button 
            @click="currentView = 'pos'" 
            :class="[currentView === 'pos' ? 'bg-indigo-800 shadow-inner' : 'bg-indigo-500 hover:bg-indigo-400']"
            class="px-5 py-2 rounded-xl font-bold transition-all duration-200">
            Punto de Venta
          </button>
          
          <button 
            v-if="currentView !== 'stats'"
            @click="intentarVerEstadisticas" 
            class="bg-indigo-500 hover:bg-indigo-400 px-5 py-2 rounded-xl font-bold transition-all duration-200 flex items-center gap-1">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M5 9V7a5 5 0 0110 0v2a2 2 0 012 2v5a2 2 0 01-2 2H5a2 2 0 01-2-2v-5a2 2 0 012-2zm8-2v2H7V7a3 3 0 016 0z" clip-rule="evenodd" />
            </svg>
            Gestión y Reportes
          </button>
          
          <button 
            v-else
            class="bg-indigo-800 shadow-inner px-5 py-2 rounded-xl font-bold transition-all duration-200 flex items-center gap-1">
            Gestión y Reportes
          </button>
        </div>
      </div>
    </header>

    <!-- Main Content -->
    <main class="flex-grow flex p-6 w-full max-w-[1600px] mx-auto overflow-hidden">
      <POSView v-if="currentView === 'pos'" class="w-full" />
      <StatsView v-else-if="currentView === 'stats'" class="w-full" />
    </main>
  </div>
</template>
