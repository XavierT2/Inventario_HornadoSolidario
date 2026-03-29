 <script setup>
import { ref } from 'vue';
import POSView from './components/POSView.vue';
import StatsView from './components/StatsView.vue';

const currentView = ref('pos'); // 'pos' o 'stats'
</script>

<template>
  <div class="min-h-screen flex flex-col w-full text-slate-800">
    <!-- Navbar -->
    <header class="bg-indigo-600 text-white shadow-md">
      <div class="max-w-7xl mx-auto px-4 py-4 flex justify-between items-center">
        <h1 class="text-2xl font-bold tracking-wider">Hornado Solidario</h1>
        <div class="space-x-4">
          <button 
            @click="currentView = 'pos'" 
            :class="[currentView === 'pos' ? 'bg-indigo-800' : 'bg-indigo-500 hover:bg-indigo-400']"
            class="px-4 py-2 rounded-lg font-semibold transition-colors duration-200">
            Punto de Venta
          </button>
          <button 
            @click="currentView = 'stats'" 
            :class="[currentView === 'stats' ? 'bg-indigo-800' : 'bg-indigo-500 hover:bg-indigo-400']"
            class="px-4 py-2 rounded-lg font-semibold transition-colors duration-200">
            Estadísticas
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
