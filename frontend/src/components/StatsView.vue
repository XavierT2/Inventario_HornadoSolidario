<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import Swal from 'sweetalert2';

const apiUrl = '/api';
const stats = ref({ totalCajaEfectivo: 0, conteoProductos: {}, estadisticasPorCajero: {} });
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

const eliminarCajero = async (cajeroName) => {
  const { value: pin } = await Swal.fire({
    title: `Eliminar datos de ${cajeroName}?`,
    text: "Escribe el PIN Supremo de autorización",
    input: 'password',
    inputAttributes: {
      inputmode: 'numeric',
      pattern: '[0-9]*'
    },
    showCancelButton: true,
    confirmButtonText: 'Borrar Definitivamente',
    cancelButtonText: 'Cancelar',
    confirmButtonColor: '#ef4444'
  });

  if (pin === '172246') {
    try {
      loading.value = true;
      await axios.delete(`${apiUrl}/cajeros/${encodeURIComponent(cajeroName)}`);
      await fetchStats();
      Swal.fire('¡Eliminado!', `Todas las ventas de ${cajeroName} fueron borradas.`, 'success');
    } catch (error) {
      console.error(error);
      Swal.fire('Error', 'No se pudo eliminar al cajero', 'error');
    } finally {
      loading.value = false;
    }
  } else if (pin) {
    Swal.fire('Denegado', 'El PIN Supremo es incorrecto', 'error');
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

  const getHtmlContent = () => {
    let html = `
    <html xmlns:x="urn:schemas-microsoft-com:office:excel">
      <head>
        <meta charset="utf-8">
        <style>
          .main-title { font-size: 22px; font-weight: bold; background-color: #4f46e5; color: white; text-align: center; height: 40px; }
          .section-title { font-size: 16px; font-weight: bold; background-color: #818cf8; color: white; text-align: center; }
          .header { background-color: #e0e7ff; font-weight: bold; border: 1px solid #a5b4fc; text-align: left; height: 25px; }
          .row { border: 1px solid #c7d2fe; height: 20px; }
          .total { font-weight: bold; color: #047857; background-color: #d1fae5; border: 1px solid #6ee7b7; font-size: 16px; height: 30px; }
        </style>
      </head>
      <body>
        <table>
          <tr><td colspan="2" class="main-title">REPORTE FINANCIERO - HORNADO SOLIDARIO</td></tr>
          <tr><td colspan="2"></td></tr>
          
          <!-- SECCIÓN GLOBAL -->
          <tr><td colspan="2" class="section-title">RESUMEN GLOBAL DEL EVENTO</td></tr>
          <tr><td class="header">Producto</td><td class="header">Cantidad Vendida</td></tr>`;
          
    for (const [producto, cantidad] of Object.entries(stats.value.conteoProductos)) {
      html += `<tr><td class="row">${producto}</td><td class="row">${cantidad}</td></tr>`;
    }
    html += `<tr><td class="total">Total Físico en Caja (General)</td><td class="total">$ ${stats.value.totalCajaEfectivo.toFixed(2)}</td></tr>`;
    html += `<tr><td colspan="2"></td></tr>`;
    html += `<tr><td colspan="2"></td></tr>`;

    // SECCIÓN INDIVIDUAL
    html += `<tr><td colspan="2" class="main-title" style="background-color: #1e1b4b;">DESGLOSE ESTRICTO POR CAJERO</td></tr>`;
    
    for (const [cajero, dataCajero] of Object.entries(stats.value.estadisticasPorCajero)) {
      html += `<tr><td colspan="2"></td></tr>`;
      html += `<tr><td colspan="2" class="section-title" style="background-color: #6366f1;">CAJERO REGISTRADO: ${cajero}</td></tr>`;
      html += `<tr><td class="header">Producto Despachado</td><td class="header">Vendidos</td></tr>`;
      
      for (const [producto, cantidad] of Object.entries(dataCajero.conteoProductos)) {
        html += `<tr><td class="row">${producto}</td><td class="row">${cantidad}</td></tr>`;
      }
      
      html += `<tr><td class="total" style="background-color: #fef08a; color: #854d0e; border-color: #fde047;">Efectivo Físico de ${cajero}</td><td class="total" style="background-color: #fef08a; color: #854d0e; border-color: #fde047;">$ ${dataCajero.totalEfectivo.toFixed(2)}</td></tr>`;
    }

    html += `
        </table>
      </body>
    </html>`;
    return html;
  };

  const blob = new Blob([getHtmlContent()], { type: 'application/vnd.ms-excel' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = `Reporte_Hornado_${new Date().toLocaleDateString().replace(/\//g, '-')}.xls`;
  a.click();
  URL.revokeObjectURL(url);
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
    <div v-else class="flex flex-col gap-8 h-full overflow-y-auto pr-2 pb-10">
      
      <!-- Sección Global -->
      <div>
        <h3 class="text-xl font-bold text-slate-500 mb-4 uppercase tracking-wider">Totales Globales del Evento</h3>
        <div class="flex gap-8">
          <div class="w-1/3 flex flex-col bg-gradient-to-br from-emerald-500 to-teal-600 rounded-3xl shadow-xl p-8 text-white relative overflow-hidden">
            <div class="absolute -right-10 -top-10 opacity-10">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-64 w-64" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M4 4a2 2 0 00-2 2v4a2 2 0 002 2V6h10a2 2 0 00-2-2H4zm2 6a2 2 0 012-2h8a2 2 0 012 2v4a2 2 0 01-2 2H8a2 2 0 01-2-2v-4zm6 4a2 2 0 100-4 2 2 0 000 4z" clip-rule="evenodd" />
              </svg>
            </div>
            
            <h3 class="text-2xl font-bold text-emerald-100 mb-2 z-10">Total Efectivo Evento</h3>
            <p class="text-emerald-50 mb-auto z-10 text-lg">Suma de todas las cajas</p>
            
            <div class="mt-8 z-10">
              <span class="text-4xl font-bold opacity-80">$</span>
              <span class="text-6xl font-black tracking-tighter">{{ stats.totalCajaEfectivo.toFixed(2) }}</span>
            </div>
          </div>

          <div class="w-2/3 bg-white rounded-3xl shadow-md p-6 border border-slate-200">
            <h3 class="text-lg font-bold text-slate-700 mb-4 flex items-center gap-2">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-indigo-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
              </svg>
              Platos y Bebidas Globales
            </h3>
            
            <div class="grid grid-cols-3 gap-3">
              <div v-for="(cantidad, nombre) in stats.conteoProductos" :key="nombre"
                  class="bg-slate-50 rounded-xl p-4 border border-slate-100 flex flex-col justify-center">
                <span class="text-slate-500 font-medium text-sm mb-1 leading-tight">{{ nombre }}</span>
                <div class="flex items-baseline gap-1">
                  <span class="text-3xl font-black text-slate-800">{{ cantidad }}</span>
                  <span class="text-slate-400 font-bold uppercase tracking-wider text-xs">uds</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <hr class="border-slate-200">

      <!-- Sección por Cajero -->
      <div>
        <h3 class="text-xl font-bold text-slate-500 mb-4 uppercase tracking-wider">Desglose por Cajero</h3>
        
        <div v-if="Object.keys(stats.estadisticasPorCajero || {}).length === 0" class="flex flex-col items-center justify-center p-12 bg-white rounded-3xl border border-slate-200 text-slate-400">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-16 w-16 mb-4 opacity-50" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4" />
          </svg>
          <span class="text-xl font-medium">Aún no hay ventas registradas</span>
        </div>

        <div class="grid grid-cols-2 gap-6">
          <div v-for="(data, cajeroName) in stats.estadisticasPorCajero" :key="cajeroName" 
               class="bg-white rounded-3xl shadow-lg border border-indigo-100 overflow-hidden flex flex-col">
               
            <!-- Cabecera Cajero -->
            <div class="bg-indigo-50 border-b border-indigo-100 p-5 flex justify-between items-center relative">
              <button @click="eliminarCajero(cajeroName)" class="absolute top-2 right-2 text-rose-300 hover:text-rose-600 transition-colors bg-white rounded-full p-1 border border-rose-100 shadow-sm" title="Eliminar Cajero y Ventas">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                </svg>
              </button>
              <h4 class="text-xl font-black text-indigo-900 flex items-center gap-2 mt-2">
                <div class="bg-indigo-200 text-indigo-700 p-2 rounded-full">
                  <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                    <path fill-rule="evenodd" d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z" clip-rule="evenodd" />
                  </svg>
                </div>
                {{ cajeroName }}
              </h4>
              <div class="text-right mt-2 mr-6">
                <span class="block text-xs uppercase font-bold text-indigo-400">Efectivo en Caja</span>
                <span class="text-2xl font-black text-emerald-600">${{ data.totalEfectivo.toFixed(2) }}</span>
              </div>
            </div>

            <!-- Cuerpo Cajero -->
            <div class="p-5 flex-grow grid grid-cols-2 gap-3 bg-white">
              <div v-for="(cant, prod) in data.conteoProductos" :key="prod" 
                   class="bg-indigo-50/50 rounded-lg p-3 border border-indigo-50 flex flex-col justify-center">
                <span class="text-slate-600 font-semibold text-sm leading-tight mb-1">{{ prod }}</span>
                <span class="text-2xl font-bold text-indigo-600">{{ cant }} <span class="text-xs uppercase text-indigo-300">uds</span></span>
              </div>
            </div>
          </div>
        </div>
      </div>

    </div>
  </div>
</template>
