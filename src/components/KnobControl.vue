<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps<{
  label: string;
  value: number; // Normalized 0-1
  displayValue: string;
}>();

// SVG Logic
// const radius = 40;
// const stroke = 8;
// const normalizedRadius = radius - stroke * 2;
// const circumference = normalizedRadius * 2 * Math.PI;

// Unused computations removed for linting
// const strokeDashoffset = computed(() => { ... })

// 计算旋转角度：-135deg (0%) 到 +135deg (100%)
const degrees = computed(() => {
  const minDeg = -135;
  const maxDeg = 135;
  const range = maxDeg - minDeg;
  return minDeg + (props.value * range);
});

// 计算进度条圆环的长度 (270度 = 0.75 * circumference)
// 我们希望背景圆是 270度
// 前景圆根据 value 填充
</script>

<template>
  <div class="flex flex-col items-center gap-2 group cursor-pointer select-none">
    
    <!-- Knob Container -->
    <div class="relative w-20 h-20 flex items-center justify-center">
      
      <!-- Base Circle Track (270 degrees) -->
      <svg class="absolute inset-0 w-full h-full rotate-90" viewBox="0 0 100 100">
         <!-- 背景轨道 -->
         <circle cx="50" cy="50" r="40" fill="none" stroke="rgba(255,255,255,0.1)" stroke-width="8" 
                 stroke-dasharray="251.2" stroke-dashoffset="62.8" stroke-linecap="round" 
                 class="opacity-50" />
      </svg>

      <!-- Active Indicator Circle -->
      <!-- 指针式旋钮 -->
      <div class="w-16 h-16 rounded-full bg-gray-800 border-2 border-white/10 shadow-[0_4px_10px_rgba(0,0,0,0.5),inset_0_2px_4px_rgba(255,255,255,0.05)] relative transition-transform duration-300 ease-out group-hover:border-blue-500/30"
           :style="{ transform: `rotate(${degrees}deg)` }">
           <!-- Indicator Line -->
           <div class="absolute top-1 left-1/2 -translate-x-1/2 w-1 h-3 bg-blue-500 rounded-full shadow-[0_0_8px_rgba(59,130,246,0.8)]"></div>
      </div>

    </div>

    <!-- Label & Value -->
    <div class="text-center">
      <div class="text-[10px] uppercase font-bold text-gray-500 tracking-wider mb-0.5 group-hover:text-blue-400 transition-colors">{{ label }}</div>
      <div class="text-xs font-mono text-blue-300 bg-blue-500/10 px-2 py-0.5 rounded border border-blue-500/20 shadow-[0_0_10px_rgba(59,130,246,0.1)] group-hover:shadow-[0_0_15px_rgba(59,130,246,0.3)] transition-all">
        {{ displayValue }}
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 可选：增加一些金属质感或光效 */
</style>
