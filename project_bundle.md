# Project Bundle

## package.json

```json
{
  "name": "2024-rc505-web-parser",
  "private": true,
  "version": "0.0.0",
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "vue-tsc -b && vite build",
    "preview": "vite preview"
  },
  "dependencies": {
    "@tailwindcss/postcss": "^4.1.18",
    "vue": "^3.5.24"
  },
  "devDependencies": {
    "@types/node": "^24.10.1",
    "@vitejs/plugin-vue": "^6.0.1",
    "@vue/tsconfig": "^0.8.1",
    "autoprefixer": "^10.4.23",
    "postcss": "^8.5.6",
    "tailwindcss": "^4.1.18",
    "typescript": "~5.9.3",
    "vite": "^7.2.4",
    "vue-tsc": "^3.1.4"
  }
}

```

## vite.config.ts

```typescript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
})

```

## tailwind.config.js

```javascript
/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {},
  },
  plugins: [],
}

```

## postcss.config.js

```javascript
export default {
    plugins: {
        '@tailwindcss/postcss': {},
        autoprefixer: {},
    },
}

```

## tsconfig.json

```json
{
  "files": [],
  "references": [
    { "path": "./tsconfig.app.json" },
    { "path": "./tsconfig.node.json" }
  ]
}

```

## index.html

```html
<!doctype html>
<html lang="en">
  <head>
    <meta charset="UTF-8" />
    <link rel="icon" type="image/svg+xml" href="/vite.svg" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>2024-rc505-web-parser</title>
  </head>
  <body>
    <div id="app"></div>
    <script type="module" src="/src/main.ts"></script>
  </body>
</html>

```

## src/main.ts

```typescript
import { createApp } from 'vue'
import './style.css'
import App from './App.vue'

createApp(App).mount('#app')

```

## src/style.css

```css
@import "tailwindcss";

@layer base {
  body {
    @apply bg-gray-900 text-white font-sans antialiased;
  }
}
```

## src/App.vue

```vue
<script setup lang="ts">
import { ref, computed, reactive } from 'vue';
import { FxParser, type ParsedFx } from './core/parser/fxParser';
import { FX_NAMES } from './core/constants/fxDefinitions';
import KnobControl from './components/KnobControl.vue';

// --- 核心状态矩阵 ---
type BankState = Record<string, Record<string, string>>; 

const createEmptyBankState = (): BankState => {
    const banks = ['A', 'B', 'C', 'D'];
    const slots = ['A', 'B', 'C', 'D'];
    const state: BankState = {};
    banks.forEach(b => {
        state[b] = {};
        slots.forEach(s => {
            state[b]![s] = 'LPF'; // Default to LPF as requested
        });
    });
    return state;
};

const parser = new FxParser();
const isFileLoaded = ref(false);
const fileName = ref("");

// 响应式状态矩阵
const fxMatrix = reactive({
    input: createEmptyBankState(),
    output: createEmptyBankState()
});

// 当前视图选择
const currentInputBank = ref("A");
const currentOutputBank = ref("A");

// --- 全自动侦测 ---
const autoDetectAll = () => {
    if (!parser.isLoaded()) return;
    const banks = ['A', 'B', 'C', 'D'];
    const slots = ['A', 'B', 'C', 'D'];
    banks.forEach(bank => {
        slots.forEach(slot => {
            const ifxName = parser.detectFx("ifx", bank, slot);
            fxMatrix.input[bank]![slot] = ifxName;
            const tfxName = parser.detectFx("tfx", bank, slot);
            fxMatrix.output[bank]![slot] = tfxName;
        });
    });
};

const onFileChange = async (event: Event) => {
  const target = event.target as HTMLInputElement;
  if (target.files && target.files.length > 0) {
      const file = target.files[0];
      if (!file) return;
      fileName.value = file.name;
      const text = await file.text();
      try {
          parser.loadXml(text);
          isFileLoaded.value = true;
          autoDetectAll();
      } catch (e) {
          console.error(e);
          alert("Error parsing XML: 格式错误或非 RC0 文件");
      }
  }
}

// --- 计算属性：根据当前视图渲染 ---
const parsedInput = computed(() => {
    if (!isFileLoaded.value) return [];
    return ["A", "B", "C", "D"].map(slot => {
        const fxName = fxMatrix.input[currentInputBank.value]![slot] || "select";
        return parser.parse("ifx", currentInputBank.value, slot, fxName);
    });
});

const parsedOutput = computed(() => {
    if (!isFileLoaded.value) return [];
    return ["A", "B", "C", "D"].map(slot => {
        const fxName = fxMatrix.output[currentOutputBank.value]![slot] || "select";
        return parser.parse("tfx", currentOutputBank.value, slot, fxName);
    });
});

// --- Markdown Export ---
const formatBlock = (list: (ParsedFx|null)[], bank: string, type: 'INPUT'|'OUTPUT') => {
    let md = `## ${type} BANK ${bank}\n\n`;
    list.forEach((item, idx) => {
        const slotName = ["A", "B", "C", "D"][idx];
        if (!item) {
           md += `### FX ${slotName}: [OFF / Not Selected]\n\n`;
           return;
        }
        md += `### FX ${slotName}: ${item.fxName}\n`;
        md += `| Parameter | Value | Raw |\n|---|---|---|\n`;
        item.params.forEach((p) => {
             md += `| ${p.label} | ${p.displayValue} | ${p.rawValue} |\n`;
        });
        md += "\n";
    });
    return md;
}

const displayContent = computed(() => {
    if (!isFileLoaded.value) return "Waiting for RC0 file...";
    return formatBlock(parsedInput.value, currentInputBank.value, 'INPUT') + "\n---\n" +
           formatBlock(parsedOutput.value, currentOutputBank.value, 'OUTPUT');
});

const exportMarkdown = () => {
    const blob = new Blob([displayContent.value], { type: 'text/markdown' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `${fileName.value}_Bank${currentInputBank.value}_parsed.md`;
    a.click();
    URL.revokeObjectURL(url);
}
</script>

<template>
  <div class="min-h-screen relative overflow-hidden bg-gray-900 text-gray-100 font-sans selection:bg-blue-500 selection:text-white">
    <div class="absolute inset-0 z-0 pointer-events-none">
        <div class="w-full h-full bg-[radial-gradient(ellipse_at_top,_var(--tw-gradient-stops))] from-slate-900 via-[#0a0a0a] to-black"></div>
        <div class="absolute top-0 left-0 w-full h-1 bg-gradient-to-r from-transparent via-blue-500 to-transparent opacity-50"></div>
    </div>

    <div class="relative z-10 container mx-auto p-4 h-screen flex flex-col max-w-7xl">
        <header class="flex justify-between items-center mb-6 bg-white/5 p-4 rounded-xl border border-white/10 backdrop-blur-md">
            <div>
                <h1 class="text-xl font-bold tracking-[0.2em] text-blue-400">RC505MKII <span class="text-white opacity-80">DECODER</span></h1>
                <p class="text-[10px] text-gray-500 font-mono mt-1">TACTICAL PARAMETER INTERFACE</p>
            </div>
            
            <div class="flex items-center gap-6">
                <div v-if="fileName" class="flex flex-col items-end">
                    <span class="text-xs text-green-400 font-mono">● SYSTEM ONLINE</span>
                    <span class="text-sm font-bold text-gray-200">{{ fileName }}</span>
                </div>
                <label class="group relative px-6 py-2 bg-blue-600 hover:bg-blue-500 rounded-lg cursor-pointer transition-all overflow-hidden">
                    <div class="absolute inset-0 bg-white/20 translate-y-full group-hover:translate-y-0 transition-transform duration-300"></div>
                    <span class="relative font-bold text-sm tracking-wide">LOAD DATA</span>
                    <input type="file" @change="onFileChange" accept=".xml,.RC0" class="hidden" />
                </label>
            </div>
        </header>

        <main class="flex-1 flex gap-6 overflow-hidden">
            <!-- Sidebar Controls -->
            <aside class="w-80 flex flex-col gap-6 overflow-y-auto pr-2">
                <!-- INPUT FX SELECTOR -->
                <div class="bg-[#111] p-5 rounded-xl border border-white/5 shadow-2xl relative overflow-hidden group">
                    <div class="absolute top-0 right-0 p-3 opacity-10 group-hover:opacity-20 transition">
                        <svg width="40" height="40" viewBox="0 0 24 24" fill="currentColor"><path d="M12 2L2 19h20L12 2zm0 3.8L17.6 17H6.4L12 5.8z"/></svg>
                    </div>
                    <div class="flex justify-between items-center mb-4 border-b border-white/10 pb-2">
                        <label class="font-bold text-blue-400 tracking-wider text-sm">INPUT FX</label>
                        <div class="flex items-center gap-2">
                            <span class="text-[10px] text-gray-500">BANK</span>
                            <select v-model="currentInputBank" class="bg-gray-800 text-blue-200 border border-gray-700 rounded px-2 py-1 text-xs outline-none focus:border-blue-500 font-mono">
                                <option v-for="b in ['A','B','C','D']" :key="b">{{b}}</option>
                            </select>
                        </div>
                    </div>
                    <div class="space-y-3">
                        <div v-for="slot in ['A','B','C','D']" :key="slot" class="flex items-center gap-3">
                            <span class="text-[10px] font-mono text-gray-500 w-4 text-center">{{slot}}</span>
                            <select 
                                v-model="fxMatrix.input[currentInputBank]![slot]" 
                                class="flex-1 bg-gray-900/50 rounded px-3 py-2 text-xs border border-white/5 focus:border-blue-500/50 outline-none text-gray-300 transition hover:bg-gray-800"
                            >
                                <option value="select" class="text-gray-600">- EMPTY -</option>
                                <option v-for="name in FX_NAMES" :key="name" :value="name">{{name}}</option>
                            </select>
                        </div>
                    </div>
                </div>

                <!-- TRACK FX SELECTOR -->
                <div class="bg-[#111] p-5 rounded-xl border border-white/5 shadow-2xl relative overflow-hidden group">
                    <div class="absolute top-0 right-0 p-3 opacity-10 group-hover:opacity-20 transition">
                        <svg width="40" height="40" viewBox="0 0 24 24" fill="currentColor"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8z"/></svg>
                    </div>
                    <div class="flex justify-between items-center mb-4 border-b border-white/10 pb-2">
                        <label class="font-bold text-green-400 tracking-wider text-sm">TRACK FX</label>
                        <div class="flex items-center gap-2">
                            <span class="text-[10px] text-gray-500">BANK</span>
                            <select v-model="currentOutputBank" class="bg-gray-800 text-green-200 border border-gray-700 rounded px-2 py-1 text-xs outline-none focus:border-green-500 font-mono">
                                <option v-for="b in ['A','B','C','D']" :key="b">{{b}}</option>
                            </select>
                        </div>
                    </div>
                    <div class="space-y-3">
                        <div v-for="slot in ['A','B','C','D']" :key="slot" class="flex items-center gap-3">
                            <span class="text-[10px] font-mono text-gray-500 w-4 text-center">{{slot}}</span>
                            <select 
                                v-model="fxMatrix.output[currentOutputBank]![slot]" 
                                class="flex-1 bg-gray-900/50 rounded px-3 py-2 text-xs border border-white/5 focus:border-green-500/50 outline-none text-gray-300 transition hover:bg-gray-800"
                            >
                                <option value="select" class="text-gray-600">- EMPTY -</option>
                                <option v-for="name in FX_NAMES" :key="name" :value="name">{{name}}</option>
                            </select>
                        </div>
                    </div>
                </div>
            </aside>

            <!-- Main Display Area -->
            <section class="flex-1 flex flex-col bg-[#0f0f0f] rounded-xl border border-white/5 overflow-hidden shadow-2xl relative">
                <div class="px-5 py-3 bg-black/40 border-b border-white/5 flex justify-between items-center">
                     <div class="flex items-center gap-2">
                         <div class="w-2 h-2 rounded-full bg-blue-500 animate-pulse" v-if="isFileLoaded"></div>
                         <span class="font-mono text-[10px] uppercase tracking-widest text-gray-500">Decoded Configuration Stream</span>
                     </div>
                     <button @click="exportMarkdown" class="text-xs flex items-center gap-2 px-3 py-1.5 rounded hover:bg-white/5 text-gray-400 hover:text-white transition disabled:opacity-30 disabled:cursor-not-allowed" :disabled="!isFileLoaded">
                         <span class="uppercase font-bold tracking-wider">Export Log</span>
                     </button>
                </div>
                
                <div class="flex-1 overflow-auto p-6 custom-scrollbar bg-[url('/grid.svg')] bg-fixed bg-opacity-5">
                  <div v-if="!isFileLoaded" class="h-full flex flex-col items-center justify-center text-gray-600 opacity-50">
                      <svg class="w-16 h-16 mb-4 animate-pulse" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1" d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12"></path></svg>
                      <span class="font-mono text-sm">Awaiting RC0 Data Stream...</span>
                  </div>

                  <div v-else class="markdown-preview max-w-5xl mx-auto space-y-8">
                      <!-- INPUT FX SECTION -->
                      <div class="space-y-4">
                          <h2 class="text-lg font-bold text-blue-400 border-b border-blue-500/30 pb-2 pl-2 border-l-4 border-l-blue-500">
                              INPUT FX BANK [ {{ currentInputBank }} ]
                          </h2>
                          <div class="grid grid-cols-1 gap-4">
                              <div v-for="(item, idx) in parsedInput" :key="'ifx'+idx" 
                                   class="bg-[#1a1a1a] border border-white/10 rounded-lg p-5 shadow-lg relative overflow-hidden">
                                   <!-- Slot Header -->
                                   <div class="absolute top-0 left-0 bg-blue-900/40 px-2 py-0.5 rounded-br-lg text-[10px] font-mono text-blue-200 border-r border-b border-blue-500/20">
                                       SLOT {{['A','B','C','D'][idx]}}
                                   </div>
                                    <div class="flex justify-end items-center mb-6">
                                       <span class="text-xl font-bold text-white tracking-widest opacity-90">{{ item ? item.fxName : 'OFF' }}</span>
                                   </div>
                                   
                                   <!-- Knobs Grid -->
                                   <div v-if="item" class="flex flex-wrap justify-around gap-6">
                                       <KnobControl 
                                          v-for="(p, i) in item.params" 
                                          :key="i"
                                          :label="p.label" 
                                          :value="p.normalized" 
                                          :displayValue="p.displayValue"
                                       />
                                   </div>
                                   <div v-else class="h-20 flex items-center justify-center text-gray-700 font-mono text-xs">
                                       NO EFFECT ASSIGNED
                                   </div>
                              </div>
                          </div>
                      </div>

                      <!-- TRACK FX SECTION -->
                      <div class="space-y-4 pt-8">
                           <h2 class="text-lg font-bold text-green-400 border-b border-green-500/30 pb-2 pl-2 border-l-4 border-l-green-500">
                              TRACK FX BANK [ {{ currentOutputBank }} ]
                          </h2>
                           <div class="grid grid-cols-1 gap-4">
                              <div v-for="(item, idx) in parsedOutput" :key="'tfx'+idx" 
                                   class="bg-[#1a1a1a] border border-white/10 rounded-lg p-5 shadow-lg relative overflow-hidden">
                                   <!-- Slot Header -->
                                   <div class="absolute top-0 left-0 bg-green-900/40 px-2 py-0.5 rounded-br-lg text-[10px] font-mono text-green-200 border-r border-b border-green-500/20">
                                       SLOT {{['A','B','C','D'][idx]}}
                                   </div>
                                    <div class="flex justify-end items-center mb-6">
                                       <span class="text-xl font-bold text-white tracking-widest opacity-90">{{ item ? item.fxName : 'OFF' }}</span>
                                   </div>
                                   
                                   <!-- Knobs Grid -->
                                   <div v-if="item" class="flex flex-wrap justify-around gap-6">
                                       <KnobControl 
                                          v-for="(p, i) in item.params" 
                                          :key="i"
                                          :label="p.label" 
                                          :value="p.normalized" 
                                          :displayValue="p.displayValue"
                                       />
                                   </div>
                                   <div v-else class="h-20 flex items-center justify-center text-gray-700 font-mono text-xs">
                                       NO EFFECT ASSIGNED
                                   </div>
                              </div>
                          </div>
                      </div>
                  </div>
                </div>
            </section>
        </main>
        
        <footer class="mt-4 flex justify-between items-center text-[10px] text-gray-600 font-mono">
             <span>RC505MKII OPEN SOURCE PROJECT // V0.2.1</span>
             <span>ANTIGRAVITY PROTOCOL ENABLED</span>
        </footer>
    </div>
  </div>
</template>

<style scoped>
/* 战术风格滚动条 */
.custom-scrollbar::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: rgba(0,0,0,0.3);
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: rgba(255,255,255,0.1);
  border-radius: 0;
}
.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: rgba(59, 130, 246, 0.5); 
}
</style>

```

## src/components/KnobControl.vue

```vue
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

```

## src/core/constants/fxDefinitions.ts

```typescript
// ---------------------------------------------------------
//  Type Definitions
// ---------------------------------------------------------
export type FxParamType = "int" | "note" | "switch" | "db" | "freq" | "ms";

export interface FxParameter {
    label: string;
    type: FxParamType;
    min: number;
    max: number;
    defaultValue?: number;
    mapper?: (val: number) => string; // Optional custom mapper
}

export interface FxDefinition {
    id: string; // The XML tag name (e.g. "LPF")
    params: FxParameter[];
}

// ---------------------------------------------------------
//  The Registry
// ---------------------------------------------------------
export const FX_DEFINITIONS: Record<string, FxParameter[]> = {
    "LPF": [
        { label: "Rate", type: "int", min: 0, max: 100 },
        { label: "Depth", type: "int", min: 0, max: 100 },
        { label: "Resonance", type: "int", min: 0, max: 100 },
        { label: "Cutoff", type: "freq", min: 20, max: 20000 }, // Scaled 0-100 in XML? Need to check manual carefully. Assuming 0-100 raw mapped to freq for now or raw values.
        // Re-reading manual: LPF Cutoff is often just 0-100 or specific freq steps. 
        // For RC-505mkII, many params are 0-100. Let's stick to simple normalization first.
        { label: "Step Rate", type: "note", min: 0, max: 28 }
    ],
    // "BPF" ...
    "DELAY": [
        { label: "Time", type: "note", min: 0, max: 28 }, // Often note sync
        { label: "Feedback", type: "int", min: 0, max: 100 },
        { label: "E.Level", type: "int", min: 0, max: 100 },
        { label: "D.Level", type: "int", min: 0, max: 100 },
        { label: "H.Damp", type: "int", min: 0, max: 100 }
    ],
    "EQ": [
        { label: "Lo Gain", type: "db", min: -20, max: 20 }, // +/- 20dB
        { label: "Hi Gain", type: "db", min: -20, max: 20 },
        { label: "Level", type: "db", min: -20, max: 20 },   // often +/- 20dB for EQ Level
        { label: "Lo Mid", type: "db", min: -20, max: 20 },
        { label: "Hi Mid", type: "db", min: -20, max: 20 }
    ],
    // Add default fallbacks for others as simple 0-100 knobs to avoid crashes
};

// Auto-fill names list
export const FX_NAMES = [
    "select", "LPF", "BPF", "HPF", "PHASER", "FLANGER", "SYNTH", "LOFI", "RADIO",
    "RING MODULATOR", "G2B", "SUSTAINER", "AUTO RIFF", "SLOW GEAR",
    "TRANSPOSE", "PITCH BEND", "ROBOT", "ELECTRIC", "HARMONIST MANUAL",
    "HARMONIST AUTO", "VOCODER", "OSC VOCODER", "OSC BOT", "PREAMP",
    "DIST", "DYNAMICS", "EQ", "ISOLATOR", "OCTAVE", "AUTO PAN", "MANUAL PAN",
    "STEREO ENHANCE", "TREMOLO", "VIBRATO", "PATTERN SLICER", "STEP SLICER",
    "DELAY", "PANNING DELAY", "REVERSE DELAY", "MOD DELAY", "TAPE ECHO",
    "TAPE ECHO V505V2", "GRANULAR DELAY", "WARP", "TWIST", "ROLL",
    "ROLL V505V2", "FREEZE", "CHORUS", "REVERB", "GATE REVERB", "REVERSE REVERB",
    "BEAT SCATTER", "BEAT REPEAT", "BEAT SHIFT", "VINYL FLICK"
];

```

## src/core/mappers/valueMappers.ts

```typescript
/**
 * valueMappers.ts
 *
 * Ported from XmlParserFX.java
 * Handles mapping of raw RC-505 parameter values to human-readable strings.
 */

import type { FxParamType } from '../constants/fxDefinitions';

// Helper to safely parse int
const parseIntSafe = (val: string): number => {
    const parsed = parseInt(val, 10);
    return isNaN(parsed) ? 0 : parsed;
};

// ============================================================================
// 0. Utilities (Restored)
// ============================================================================
export const normalizeValue = (val: number, min: number, max: number, type: FxParamType): number => {
    if (type === 'switch') {
        return val > 0 ? 1 : 0;
    }
    // Clamp
    const clamped = Math.max(min, Math.min(max, val));
    // Linear normalization
    return (clamped - min) / (max - min);
};


// ============================================================================
// 1. Individual Mapping Functions
// ============================================================================

export const mapOnOff = (value: string): string => {
    if (value === "0" || value.toUpperCase() === "OFF") return "OFF";
    if (value === "1" || value.toUpperCase() === "ON") return "ON";
    return value;
};

export const mapPhrase = (value: string): string => {
    try {
        const i = parseInt(value, 10);
        return isNaN(i) ? value : "P" + i;
    } catch {
        return value;
    }
};

export const mapMode = (value: string): string => {
    switch (value) {
        case "1": return "RC OLD";
        case "2": return "RC NEW";
        default: return value;
    }
};

export const mapTwistRelease = (value: string): string => {
    switch (value) {
        case "0": return "FALL";
        case "1": return "FADE";
        default: return value;
    }
};

export const maptimedelay = (value: string): string => {
    const i = parseIntSafe(value);
    switch (value) {
        case "0": return "1/32note";
        case "1": return "1/16note";
        case "2": return "1/8 note triplet";
        case "3": return "1/16 note dotted";
        case "4": return "1/8 note";
        case "5": return "1/4 note triplet";
        case "6": return "1/4 note dotted";
        case "7": return "1/2 note triplet";
        case "8": return "1/4 note";
        case "9": return "1/2 note";
        default: return (i - 8) + "ms";
    }
};

export const mapamptype = (value: string): string => {
    switch (value) {
        case "0": return "JC-120";
        case "1": return "NATURAL CLEAN";
        case "2": return "FULL RANGE";
        case "3": return "COMBO CRUNCH";
        case "4": return "STACK CRUNCH";
        case "5": return "HIGAIN STACK";
        case "6": return "POWER DRIVE";
        case "7": return "EXTREM LEAD";
        case "8": return "CORE METAL";
        default: return "invalid";
    }
};

export const maptypedynamics = (value: string): string => {
    switch (value) {
        case "0": return "NATURALCOMP";
        case "1": return "MIXER COMP";
        case "2": return "LIVE COMP";
        case "3": return "NATURAL LIM";
        case "4": return "HARD LIM";
        case "5": return "JINGL COMP";
        case "6": return "HARD COMP";
        case "7": return "SOFT COMP";
        case "8": return "CLEAN COMP";
        case "9": return "DANCE COMP";
        case "10": return "ORCH COMP";
        case "11": return "VOCAL COMP";
        case "12": return "ACOUSTIC";
        case "13": return "ROCK BAND";
        case "14": return "ORCHESTRA";
        case "15": return "LOW BOOST";
        case "16": return "BRIGHTEN";
        case "17": return "DJs VOICE";
        case "18": return "PHONE VOX";
        default: return "invalid";
    }
};

export const mapdynamics = (value: string): string => {
    const i = parseIntSafe(value);
    if (i < 20) return String(-(20 - i));
    if (i === 20) return "0";
    return "+" + (i - 20);
};

export const mapq = (value: string): string => {
    switch (value) {
        case "0": return "0.5";
        case "1": return "1";
        case "2": return "2";
        case "3": return "4";
        case "4": return "8";
        case "5": return "16";
        default: return value;
    }
};

export const mapgain = (value: string): string => {
    const i = parseIntSafe(value);
    if (i < 20) return String(-(20 - i)) + "dB";
    if (i === 20) return "0dB";
    return "+" + (i - 20) + "dB";
};

export const mapthreshold = (value: string): string => {
    const i = parseIntSafe(value);
    if (i < 30) return -(30 - i) + "dB";
    if (i === 30) return "0dB";
    return "invalid";
};

export const mappattern = (value: string): string => {
    const i = parseIntSafe(value);
    return "P" + (i + 1);
};

export const mappredelay = (value: string): string => {
    return value + "mS";
};

export const maptimereverb = (value: string): string => {
    const i = parseIntSafe(value);
    return (i * 0.1).toFixed(1) + "S";
};

export const mapstage = (value: string): string => {
    switch (value) {
        case "0": return "4";
        case "1": return "8";
        case "2": return "12";
        case "3": return "BI PHASE";
        default: return "invalid";
    }
};

export const mapsteprate = (value: string): string => {
    const i = parseIntSafe(value);
    switch (value) {
        case "0": return "OFF";
        case "1": return "4 MEAS";
        case "2": return "2 MEAS";
        case "3": return "1 MEAS";
        case "4": return "1/2 note";
        case "5": return "1/4 note dotted";
        case "6": return "1/2 note triplet";
        case "7": return "1/4 note";
        case "8": return "1/8 note dotted";
        case "9": return "1/4 note triplet";
        case "10": return "1/8 note";
        case "11": return "1/16 note dotted";
        case "12": return "1/8 note triplet";
        case "13": return "1/16note";
        case "14": return "1/32note";
        default: return String(i - 15);
    }
};

export const maphi_cut = (value: string): string => {
    const table = [
        "20.0Hz", "25.0Hz", "31.5Hz", "40.0Hz", "50.0Hz",
        "63.0Hz", "80.0Hz", "100Hz", "125Hz", "160Hz",
        "200Hz", "250Hz", "315Hz", "400Hz", "500Hz",
        "630Hz", "800Hz", "1.00KHz", "1.25KHz", "1.6KHz",
        "2.00KHz", "2.5KHz", "3.15KHz", "4.00KHz", "5.00KHz",
        "6.3KHz", "8.00KHz", "10.00KHz", "12.5KHz", "FLAT"
    ];
    const idx = parseIntSafe(value);
    return table[idx] ?? value;
};

export const maplo_cut = (value: string): string => {
    // Note: Java mapping had different indexing than hi_cut/definitions sometimes
    // Based on Java code: 0->FLAT, 1->20.0Hz...
    const table = [
        "FLAT", "20.0Hz", "25.0Hz", "31.5Hz", "40.0Hz",
        "50.0Hz", "63.0Hz", "80.0Hz", "100Hz", "125Hz",
        "160Hz", "200Hz", "250Hz", "315Hz", "400Hz",
        "500Hz", "630Hz", "800Hz", "1.00KHz", "1.25KHz",
        "1.6KHz", "2.00KHz", "2.5KHz", "3.15KHz", "4.00KHz",
        "5.00KHz", "6.3KHz", "8.00KHz", "10.00KHz", "12.5KHz"
    ];
    const idx = parseIntSafe(value);
    return table[idx] ?? "invalid";
};

export const mapoctave = (value: string): string => {
    switch (value) {
        case "0": return "-20CT";
        case "1": return "-10CT";
        case "2": return "0";
        case "3": return "+10CT";
        default: return value;
    }
};

export const maptone = (value: string): string => {
    try {
        const i = parseInt(value, 10);
        if (isNaN(i)) return "Invalid input";
        if (i < 50) return String(-(50 - i));
        if (i === 50) return "0";
        return String(i - 50);
    } catch {
        return "Invalid input";
    }
};

export const mapsaw = (value: string): string => {
    switch (value) {
        case "0": return "SAW";
        case "2": return "VINTAGE_SAW";
        case "3": return "DETUNE_SAW";
        case "4": return "SQUARE";
        case "5": return "RECT";
        default: return "INVALID";
    }
};

export const mapRate = (value: string): string => {
    const i = parseIntSafe(value);
    switch (value) {
        case "0": return "4 MEAS";
        case "1": return "2 MEAS";
        case "2": return "1 MEAS";
        case "3": return "1/2 note";
        case "4": return "1/4 note dotted";
        case "5": return "1/2 note triplet";
        case "6": return "1/4 note";
        case "7": return "1/8 note dotted";
        case "8": return "1/4 note triplet";
        case "9": return "1/8 note";
        case "10": return "1/16 note dotted";
        case "11": return "1/8 note triplet";
        case "12": return "1/16note";
        case "13": return "1/32note";
        default: return String(i - 14);
    }
};

export const mapDepth = (value: string): string => {
    return value;
};

// ============================================================================
// 2. Registry & Dispatcher
// ============================================================================

type MapperFn = (val: string) => string;

const PARAM_MAPPERS: Record<string, MapperFn> = {
    "SW": mapOnOff,
    "SYNC": mapOnOff,
    "RETRIG": mapOnOff,
    "LOOP": mapOnOff,
    "CARRIER THRU": mapOnOff,
    "HOLD": mapOnOff,

    "RATE": mapRate,
    "DEPTH": mapDepth,
    "OSC": mapsaw,
    "CARRIER": mapsaw,
    "TONE": maptone,
    "FORMANT": maptone,
    "MODSENS": maptone,
    "OCTAVE": mapoctave,
    "LO CUT": maplo_cut,
    "LO FREQ": maplo_cut,
    "HI CUT": maphi_cut,
    "HI FREQ": maphi_cut,
    "STEPRATE": mapsteprate,
    "STAGE": mapstage,
    "PREDELAY": mappredelay,
    "PATTERN": mappattern,
    "PHRASE": mapPhrase,
    "THRESHOLD": mapthreshold,
    "LO GAIN": mapgain,
    "HI GAIN": mapgain,
    "LO MID GAIN": mapgain,
    "HI MID GAIN": mapgain,
    "HI Q": mapq,
    "LO Q": mapq,
    "DYNAMICS": mapdynamics,
    "AMP TYPE": mapamptype
};

/**
 * Main mapping entry point.
 * Matches logic from XmlParserFX.java mapParameterValue()
 */
export const mapParameterValue = (paramName: string, value: string, fxName: string): string => {
    const key = paramName.toUpperCase();

    // SPECIAL: TIME (depends on fxName)
    if (key === "TIME") {
        const uFx = fxName.toUpperCase();
        if (["REVERB", "GATE_REVERB", "REVERSE_REVERB"].includes(uFx)) {
            return maptimereverb(value);
        }
        if (uFx === "MOD_DELAY") {
            return maptimedelay(value);
        }
        return value;
    }

    // SPECIAL: LEVEL (EQ)
    if (key === "LEVEL" && fxName.toUpperCase() === "EQ") {
        return mapgain(value);
    }

    // SPECIAL: TYPE (DYNAMICS)
    if (key === "TYPE" && fxName.toUpperCase() === "DYNAMICS") {
        return maptypedynamics(value);
    }

    // SPECIAL: MODE
    if (key === "MODE") {
        return mapMode(value);
    }

    // SPECIAL: RELEASE (TWIST)
    if (key === "RELEASE" && fxName.toUpperCase() === "TWIST") {
        return mapTwistRelease(value);
    }

    const mapper = PARAM_MAPPERS[key];
    return mapper ? mapper(value) : value;
};

```

## src/core/parser/fxParser.ts

```typescript
import { fixXmlContent } from './xmlFixer';
import { FX_DEFINITIONS, FX_NAMES, type FxParamType } from '../constants/fxDefinitions';
import { normalizeValue, mapParameterValue } from '../mappers/valueMappers';

export interface VisualizableParam {
    label: string;
    rawValue: number;
    displayValue: string;
    normalized: number; // 0.0 - 1.0
    type: FxParamType;
}

export interface ParsedFx {
    fxName: string;
    params: VisualizableParam[];
}

export class FxParser {
    private doc: Document | null = null;
    // Standard parameter keys XML uses: A, B, C, D... or sometimes Prm1? 
    // RC-505mkII MEMORY files use A, B, C, D...
    private paramKeys = Array.from({ length: 26 }, (_, i) => String.fromCharCode(65 + i)); // A-Z

    constructor() { }

    public loadXml(rawContent: string) {
        const fixed = fixXmlContent(rawContent);
        const parser = new DOMParser();
        this.doc = parser.parseFromString(fixed, "text/xml");
        if (this.doc.getElementsByTagName("parsererror").length > 0) {
            console.error("XML Parsing Error");
            this.doc = null;
        }
    }

    public parse(section: string, bank: string, slot: string, fxName: string): ParsedFx | null {
        if (!this.doc) return null;

        const cleanFxName = fxName.toUpperCase().replace(/ /g, "_");

        // 1. Get Definition
        const defParams = FX_DEFINITIONS[cleanFxName];

        // 2. Find XML Element
        const tag = `${bank}${slot}_${cleanFxName}`;

        let headerEl: Element = this.doc.documentElement;
        const sectionNodes = this.doc.getElementsByTagName(section);
        if (sectionNodes.length > 0) {
            headerEl = sectionNodes[0] as Element;
        }

        const fxNodes = headerEl.getElementsByTagName(tag);
        // Fallback: simple global search if not found in section
        const targetEl = (fxNodes.length > 0 ? fxNodes[0] : (this.doc.getElementsByTagName(tag)[0])) as Element;

        if (!targetEl) return null;
        const fxEl = targetEl;

        // 3. Map Parameters
        const visualParams: VisualizableParam[] = [];

        if (defParams) {
            // We have a known definition, map strictly
            defParams.forEach((def, idx) => {
                const xmlKey = this.paramKeys[idx] || `Prm${idx + 1}`;
                const pNode = fxEl.getElementsByTagName(xmlKey)[0];
                const rawStr = pNode?.textContent || "0";
                const rawVal = parseInt(rawStr, 10) || 0;

                visualParams.push({
                    label: def.label,
                    rawValue: rawVal,
                    displayValue: mapParameterValue(def.label, rawStr, fxName),
                    normalized: normalizeValue(rawVal, def.min, def.max, def.type),
                    type: def.type
                });
            });
        } else {
            // Fallback for unknown FX: Just dump 5 generic knobs
            for (let i = 0; i < 5; i++) {
                const xmlKey = this.paramKeys[i] || `Prm${i + 1}`;
                const pNode = fxEl.getElementsByTagName(xmlKey)[0];
                const rawStr = pNode?.textContent || "0";
                const rawVal = parseInt(rawStr, 10) || 0;

                visualParams.push({
                    label: `Param ${i + 1}`,
                    rawValue: rawVal,
                    displayValue: rawVal.toString(),
                    normalized: rawVal / 100, // Guess 0-100
                    type: 'int'
                });
            }
        }

        return {
            fxName: fxName,
            params: visualParams
        };
    }

    public detectFx(section: string, bank: string, slot: string): string {
        if (!this.doc) return "select";

        let headerEl: Element = this.doc.documentElement;
        const sectionNodes = this.doc.getElementsByTagName(section);
        if (sectionNodes.length > 0) {
            headerEl = sectionNodes[0] as Element;
        }

        for (const name of FX_NAMES) {
            if (name === "select") continue;
            const cleanFxName = name.toUpperCase().replace(/ /g, "_");
            const tag = `${bank}${slot}_${cleanFxName}`;

            if (headerEl.getElementsByTagName(tag).length > 0) {
                return name;
            }
            // Fallback global search
            if (this.doc.getElementsByTagName(tag).length > 0) {
                return name;
            }
        }
        return "select";
    }

    public isLoaded() {
        return this.doc !== null;
    }
}

```

## src/core/parser/xmlFixer.ts

```typescript
/**
 * Fixes malformed XML where attributes are missing quotes.
 * Example: name=Value -> name="Value"
 */
export function fixXmlContent(content: string): string {
    // Regex matches attribute=value where value is not quoted and not containing spaces/brackets
    return content.replace(/(\w+)=([^"'\s>]+)/g, '$1="$2"');
}

```

