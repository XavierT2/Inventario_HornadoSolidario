<script setup>
import { ref, onMounted, computed } from 'vue';
import axios from 'axios';
import Swal from 'sweetalert2';

const apiUrl = '/api';
const emit = defineEmits(['venta-realizada']);

const productos = ref([]);
const carrito = ref([]);
const loading = ref(true);

// HISTORIAL Y ANULACIONES NATIVAS
const historialVentas = ref([]);
const ventaEditando = ref(null);

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
  cargarHistorial(); // Cargar historial al instante
});

const getCartItem = (producto) => carrito.value.find(item => item.id === producto.id);

const addToCart = (producto) => {
  const existing = carrito.value.find(item => item.id === producto.id);
  
  if (existing) {
    existing.cantidad++;
  } else {
    carrito.value.push({
      ...producto,
      cantidad: 1
    });
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

const totalCajaCajero = computed(() => {
  return historialVentas.value.reduce((sum, venta) => sum + venta.total, 0);
});

const cobrar = async () => {
  if (carrito.value.length === 0) return;

  const requestData = {
    cajero: localStorage.getItem('cajero_pos') || 'Desconocido',
    productos: carrito.value.map(item => ({ id: item.id, cantidad: item.cantidad }))
  };

  const confirm = await Swal.fire({
    title: `¿Confirmas COBRAR $${total.value.toFixed(2)}?`,
    icon: 'info',
    showCancelButton: true,
    confirmButtonColor: '#4f46e5',
    cancelButtonColor: '#9ca3af',
    confirmButtonText: 'Sí, Cobrar',
    cancelButtonText: 'Cancelar'
  });

  if (confirm.isConfirmed) {
    try {
      const { data: ventaAgregada } = await axios.post(`${apiUrl}/ventas`, requestData);
      
      Swal.fire({
        title: `✅ ORDEN #${ventaAgregada.id}`,
        html: `<div class="text-2xl mt-2 font-medium text-slate-600">Dile este número al cliente en voz alta.</div>`,
        icon: 'success',
        confirmButtonText: 'Siguiente Cliente',
        confirmButtonColor: '#4f46e5'
      });
      
      carrito.value = [];
      cargarHistorial(); // Refrescar historial apenas cobramos un nuevo ticket
    } catch (error) {
      console.error('Error procesando cobro', error);
      Swal.fire('Error', 'Hubo un problema al procesar la operación', 'error');
    }
  }
};

const cargarHistorial = async () => {
  const cajero = localStorage.getItem('cajero_pos') || 'Desconocido';
  try {
    const { data } = await axios.get(`${apiUrl}/cajeros/${cajero}/historial`);
    historialVentas.value = data;
  } catch (e) {
    console.error(e);
  }
};

const anularVarios = async (detalleId, productoNombre, ventaId, cantidadMax) => {
  const { value: qtyToReturn, isConfirmed } = await Swal.fire({
    title: `Devolver ${productoNombre}`,
    text: `¿Cuántas unidades deseas devolver? (Máximo ${cantidadMax})`,
    input: 'number',
    inputAttributes: {
      min: 1,
      max: cantidadMax,
      step: 1
    },
    inputValue: 1,
    showCancelButton: true,
    confirmButtonColor: '#ef4444',
    confirmButtonText: 'Procesar',
    cancelButtonText: 'Cancelar',
    inputValidator: (value) => {
      if (!value || value <= 0 || value > cantidadMax) {
        return `Por favor ingresa un número entre 1 y ${cantidadMax}`;
      }
    }
  });

  if (isConfirmed && qtyToReturn) {
    const dv = ventaEditando.value.detalles.find(d => d.id === detalleId);
    if(!dv) return;
    const amountRefunded = dv.precioUnitario * qtyToReturn;

    try {
      await axios.put(`${apiUrl}/detalle/${detalleId}/anular/${qtyToReturn}`);
      await cargarHistorial(); // Actualizamos la data general
      
      Swal.fire({ 
        title: '¡DEVOLUCIÓN ACEPTADA!', 
        html: `<div class="text-4xl font-black text-emerald-600 my-4">ENTREGAR: $ ${amountRefunded.toFixed(2)}</div><p class="font-bold">Devuelve este efectivo al cliente.</p>`, 
        icon: 'success', 
        confirmButtonText: '¡Entendido!'
      });
      
      // Actualizar el modo edición en vivo
      const refreshedTicket = historialVentas.value.find(v => v.id === ventaId);
      if (refreshedTicket) {
        ventaEditando.value = refreshedTicket;
      } else {
        ventaEditando.value = null; // El ticket entero murió
      }
    } catch (error) {
      console.error(error);
      Swal.fire('Error', 'No se pudo anular', 'error');
    }
  }
};
</script>

<template>
  <div class="flex gap-6 h-full w-full">
    
    <!-- Panel Izquierdo: Catálogo e Historial Nativo -->
    <div class="w-2/3 flex flex-col gap-6 h-full">

      <!-- Catálogo de Productos (Arriba) -->
      <div class="bg-white rounded-2xl shadow-xl p-6 flex flex-col border border-slate-100 h-3/5 overflow-hidden">
        <div class="flex justify-between items-center mb-6">
          <h2 class="text-3xl font-bold text-slate-700">Catálogo de Productos</h2>
        </div>
        <div class="grid grid-cols-2 lg:grid-cols-3 gap-6 flex-grow overflow-y-auto pr-2">
          <button 
            v-for="producto in productos" 
            :key="producto.id"
            @click="addToCart(producto)"
            class="bg-gradient-to-br from-indigo-50 to-indigo-100 hover:from-indigo-100 hover:to-indigo-200 
                   border-2 border-indigo-200 hover:border-indigo-400 rounded-2xl p-6 flex flex-col 
                   items-center justify-center transition-all duration-300 transform hover:scale-[1.02] shadow-sm hover:shadow-md cursor-pointer h-40 group">
            <span class="text-xl font-bold text-center text-indigo-900 leading-tight mb-4 group-hover:text-indigo-700">
              {{ producto.nombre }}
            </span>
            <span class="text-xl font-black text-emerald-600 bg-white px-4 py-2 rounded-xl shadow-inner">
              ${{ producto.precio.toFixed(2) }}
            </span>
          </button>
        </div>
      </div>

      <!-- Historial Nativo (Abajo) -->
      <div class="bg-white rounded-2xl shadow-xl p-6 flex flex-col border border-slate-100 h-2/5 overflow-hidden text-slate-800 relative">
        <div class="flex justify-between items-center mb-4">
          <h2 class="text-2xl font-bold flex items-center gap-2 text-slate-700">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-slate-500" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm1-12a1 1 0 10-2 0v4a1 1 0 00.293.707l2.828 2.829a1 1 0 101.415-1.415L11 9.586V6z" clip-rule="evenodd" />
            </svg>
            Mis Últimas Ventas
          </h2>
          <button @click="cargarHistorial" class="bg-slate-100 text-slate-600 border border-slate-200 hover:bg-slate-200 px-4 py-2 rounded-lg text-sm font-bold shadow-sm transition">Refrescar Lista</button>
        </div>
        
        <div class="flex-grow overflow-x-auto flex gap-4 pb-2 items-center">
           <div v-if="historialVentas.length === 0" class="flex items-center justify-center w-full text-slate-400 font-medium">
             Aún no has despachado nada. Tus transacciones aparecerán aquí.
           </div>
           
           <button 
             v-for="venta in historialVentas" :key="venta.id" 
             @click="ventaEditando = venta"
             class="flex-shrink-0 bg-slate-50 hover:bg-indigo-50 border border-slate-200 hover:border-indigo-300 rounded-xl p-5 w-64 h-full text-left flex flex-col justify-between transition-all group shadow-sm hover:shadow-md cursor-pointer">
             <div>
               <span class="text-sm font-bold text-slate-500 group-hover:text-indigo-600">Ticket #{{ venta.id }}</span>
               <div class="font-bold mt-1 text-base text-slate-800">{{ venta.fecha }}</div>
               <div class="text-sm text-slate-500 mt-2 truncate">{{ venta.detalles.length }} fila(s) de platillos</div>
             </div>
             <div class="text-3xl font-black text-emerald-600 group-hover:text-emerald-700 mt-4">${{ venta.total.toFixed(2) }}</div>
           </button>
        </div>
      </div>
    </div>

    <!-- Panel Derecho: Carrito o Editor de Ticket -->
    <div class="w-1/3 bg-white rounded-2xl shadow-lg border flex flex-col overflow-hidden relative" :class="ventaEditando ? 'border-orange-300 shadow-orange-500/10' : 'border-indigo-100'">
      
      <!-- ESTADO 1: CARRITO NORMAL -->
      <template v-if="!ventaEditando">
        <div class="p-4 border-b bg-indigo-50 border-indigo-100 flex justify-between items-center">
          <h2 class="text-xl font-bold flex items-center gap-2 text-indigo-900">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z" />
            </svg>
            <span>Factura Actual</span>
          </h2>
          <span class="bg-white px-2 py-1 rounded-md text-sm font-bold shadow-sm text-indigo-600">
            {{ carrito.length }} ítem(s)
          </span>
        </div>

        <div class="flex-grow overflow-y-auto p-4 space-y-3 bg-slate-50">
          <div v-if="carrito.length === 0" class="h-full flex flex-col justify-center items-center text-slate-400">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-16 w-16 mb-2 opacity-30" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z" />
            </svg>
            <p>La bandeja está vacía</p>
          </div>
          
          <div v-for="item in carrito" :key="item.id" class="bg-white p-4 rounded-xl shadow-sm border border-slate-200 flex flex-col gap-2">
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

        <!-- Footer Cobro -->
        <div class="p-4 bg-white border-t border-slate-200 shadow-[0_-4px_6px_-1px_rgba(0,0,0,0.05)] flex flex-col gap-3">
          <div class="flex justify-between items-end mb-2">
            <div class="flex items-center gap-3">
               <span class="text-slate-500 font-semibold uppercase tracking-wider text-sm">Monto a Cobrar</span>
               <button v-if="carrito.length > 0" @click="carrito = []" class="text-rose-500 bg-rose-50 hover:bg-rose-100 p-1.5 rounded-lg border border-rose-100 transition shadow-sm flex items-center gap-1" title="Vaciar Carrito">
                 <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                 </svg>
                 <span class="text-xs font-bold uppercase tracking-wide">Limpiar</span>
               </button>
            </div>
            
            <span class="text-4xl font-black tracking-tight text-slate-800">
              $ {{ total.toFixed(2) }}
            </span>
          </div>
          <button 
            @click="cobrar" 
            :disabled="carrito.length === 0"
            :class="carrito.length === 0 ? 'bg-slate-300 cursor-not-allowed' : 'bg-indigo-600 hover:bg-indigo-500 shadow-indigo-500/30'"
            class="w-full py-5 rounded-xl text-white font-bold text-xl shadow-lg transition-all duration-200 transform hover:-translate-y-1">
            <span>COBRAR ORDEN</span>
          </button>
        </div>
      </template>

      <!-- ESTADO 2: LECTURA/DEVOLUCIÓN DE UNA FACTURA VIEJA -->
      <template v-else>
         <div class="p-4 border-b bg-orange-50 border-orange-200 flex justify-between items-center">
             <h2 class="text-xl font-bold flex items-center gap-2 text-orange-900">
               Ticket #{{ ventaEditando.id }}
             </h2>
             <button @click="ventaEditando = null" class="bg-white border border-slate-300 text-slate-600 px-3 py-1 rounded-lg text-sm font-bold shadow-sm hover:bg-slate-100 transition">Regresar</button>
         </div>
         
         <div class="flex-grow overflow-y-auto p-4 space-y-3 bg-slate-50">
            <div v-for="detalle in ventaEditando.detalles" :key="detalle.id" class="bg-white p-4 rounded-xl shadow-sm border border-slate-200 flex flex-col gap-2">
               <div class="flex justify-between items-start">
                  <span class="font-bold text-slate-700 text-lg leading-tight w-2/3">{{ detalle.cantidad }}x {{ detalle.productoNombre }}</span>
                  <span class="font-bold text-emerald-600">${{ detalle.subtotal.toFixed(2) }}</span>
               </div>
               <div class="flex w-full justify-between items-center mt-2">
                  <span class="text-slate-400 text-sm font-medium">Ud: ${{ detalle.precioUnitario.toFixed(2) }}</span>
                  <button @click="anularVarios(detalle.id, detalle.productoNombre, ventaEditando.id, detalle.cantidad)" 
                          class="bg-rose-100 text-rose-700 hover:bg-rose-500 hover:text-white px-3 py-1.5 rounded-lg text-sm font-bold transition flex items-center gap-1">
                      DEVOLVER...
                  </button>
               </div>
            </div>
         </div>
         
         <div class="p-4 bg-white border-t border-slate-200 pb-5">
            <div class="flex justify-between items-end mb-4 px-2">
               <span class="text-slate-500 font-semibold text-sm">Cobro Registrado</span>
               <span class="text-2xl font-black text-slate-800">${{ ventaEditando.total.toFixed(2) }}</span>
            </div>
            <button @click="ventaEditando = null" class="w-full py-5 bg-slate-800 hover:bg-slate-700 text-white rounded-xl text-xl font-bold shadow-md transition">
               Finalizar Edición
            </button>
         </div>
      </template>
      
    </div>
  </div>
</template>
