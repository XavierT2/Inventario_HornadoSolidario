<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import Swal from 'sweetalert2';

const apiUrl = '/api';
const stats = ref({ totalCajaEfectivo: 0, conteoProductos: {} });
const loading = ref(true);

const fetchStats = async () => {
  loading.value = true;
  try {
    const { data } = await axios.get(`${apiUrl}/estadisticas`);
    stats.value = data;
  } catch (error) {
    console.error('Error fetching stats', error);
    Swal.fire('Error', 'No se pudieron cargar las estadísticas', 'error');
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  fetchStats();
});

const descargarReporte = () => {
  if (Object.keys(stats.value.conteoProductos).length === 0) {
    Swal.fire('Atención', 'No hay datos de ventas para descargar', 'info');
    return;
  }

  let csvContent = "Producto;Cantidad Vendida\n";
  for (const [producto, cantidad] of Object.entries(stats.value.conteoProductos)) {
    csvContent += `"${producto}";${cantidad}\n`;
  }
  csvContent += `\nRESUMEN FINANCIERO;\n`;
  csvContent += `Total Efectivo Caja;$"${stats.value.totalCajaEfectivo.toFixed(2)}"\n`;

  const blob = new Blob([new Uint8Array([0xEF, 0xBB, 0xBF]), csvContent], { type: 'text/csv;charset=utf-8;' });
  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.setAttribute("href", url);
  link.setAttribute("download", `Reporte_Hornado_${new Date().toLocaleDateString().replace(/\//g,'-')}.csv`);
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
};
</script>

<template>
  <div class="h-full w-full flex flex-col gap-8 bg-slate-50 p-8 rounded-3xl shadow-inner">
    <!-- Header -->
    <div class="flex justify-between items-center">
      <div>
        <h2 class="text-4xl font-black text-slate-800">Cierre de Caja</h2>
        <p class="text-slate-500 mt-2 text-lg">Resumen financiero y de inventario del evento</p>
      </div>
      <div class="flex gap-4">
        <button @click="descargarReporte" class="bg-indigo-50 border-2 border-indigo-200 hover:border-indigo-500 hover:bg-indigo-100 text-indigo-700 px-6 py-3 rounded-xl font-bold flex items-center gap-2 shadow-sm transition-all hover:shadow-md">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
          </svg>
          Descargar CSV
        </button>
        <button @click="fetchStats" class="bg-white border-2 border-indigo-100 hover:border-indigo-500 text-indigo-600 px-6 py-3 rounded-xl font-bold flex items-center gap-2 shadow-sm transition-all hover:shadow-md">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
          </svg>
          Actualizar Datos
        </button>
      </div>
    </div>

    <div v-if="loading" class="flex-grow flex items-center justify-center">
      <div class="animate-spin rounded-full h-16 w-16 border-b-4 border-indigo-600"></div>
    </div>

    <!-- Main Content -->
    <div v-else class="flex gap-8 h-full">
      <!-- Tarjeta Resumen Total Efectivo -->
      <div class="w-1/3 flex flex-col h-full bg-gradient-to-br from-emerald-500 to-teal-600 rounded-3xl shadow-2xl p-8 text-white relative overflow-hidden">
        <!-- Decoration -->
        <div class="absolute -right-10 -top-10 opacity-10">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-64 w-64" viewBox="0 0 20 20" fill="currentColor">
            <path fill-rule="evenodd" d="M4 4a2 2 0 00-2 2v4a2 2 0 002 2V6h10a2 2 0 00-2-2H4zm2 6a2 2 0 012-2h8a2 2 0 012 2v4a2 2 0 01-2 2H8a2 2 0 01-2-2v-4zm6 4a2 2 0 100-4 2 2 0 000 4z" clip-rule="evenodd" />
          </svg>
        </div>
        
        <h3 class="text-2xl font-bold text-emerald-100 mb-2 z-10">Efectivo Físico en Caja</h3>
        <p class="text-emerald-50 mb-auto z-10 text-lg">Total recaudado de todas las ventas</p>
        
        <div class="mt-8 z-10">
          <span class="text-4xl font-bold opacity-80">$</span>
          <span class="text-7xl font-black tracking-tighter">{{ stats.totalCajaEfectivo.toFixed(2) }}</span>
        </div>
      </div>

      <!-- Tarjeta Conteo Productos -->
      <div class="w-2/3 bg-white rounded-3xl shadow-xl p-8 border border-slate-100 flex flex-col h-full">
        <h3 class="text-2xl font-bold text-slate-800 mb-6 flex items-center gap-3">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-8 w-8 text-indigo-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
          </svg>
          Despacho por Producto
        </h3>
        
        <div class="flex-grow grid grid-cols-2 gap-4 overflow-y-auto pr-2">
          <div v-for="(cantidad, nombre) in stats.conteoProductos" :key="nombre"
               class="bg-indigo-50 rounded-2xl p-6 flex flex-col justify-center border border-indigo-100 hover:border-indigo-300 transition-colors">
            <span class="text-slate-600 font-semibold text-lg mb-2">{{ nombre }}</span>
            <div class="flex items-baseline gap-2">
              <span class="text-5xl font-black text-indigo-700">{{ cantidad }}</span>
              <span class="text-indigo-400 font-bold uppercase tracking-wider text-sm">uds</span>
            </div>
          </div>
          
          <div v-if="Object.keys(stats.conteoProductos).length === 0" class="col-span-2 flex flex-col items-center justify-center p-12 text-slate-400">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-16 w-16 mb-4 opacity-50" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4" />
            </svg>
            <span class="text-xl font-medium">Aún no hay ventas registradas</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
