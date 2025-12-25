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
    console.log(`[Action: Auto Detect] [Status: Start] [File: App.vue :: autoDetectAll] Scanning all banks...`);
    if (!parser.isLoaded()) {
        console.warn(`[Action: Auto Detect] [Status: Fail] [File: App.vue :: autoDetectAll] Parser not loaded`);
        return;
    }
    const banks = ['A', 'B', 'C', 'D'];
    const slots = ['A', 'B', 'C', 'D'];
    let foundCount = 0;
    banks.forEach(bank => {
        slots.forEach(slot => {
            const ifxName = parser.detectFx("ifx", bank, slot);
            fxMatrix.input[bank]![slot] = ifxName;
            if(ifxName !== 'select') foundCount++;
            
            const tfxName = parser.detectFx("tfx", bank, slot);
            fxMatrix.output[bank]![slot] = tfxName;
            if(tfxName !== 'select') foundCount++;
        });
    });
    console.log(`[Action: Auto Detect] [Status: Complete] [File: App.vue :: autoDetectAll] Found ${foundCount} items.`);
};

const onFileChange = async (event: Event) => {
  const target = event.target as HTMLInputElement;
  console.log(`[Action: User Select File] [Status: Start] [File: App.vue :: onFileChange]`);
  
  if (target.files && target.files.length > 0) {
      const file = target.files[0];
      if (!file) return;
      
      console.log(`[Action: Read File] [Status: Loading] [File: App.vue :: onFileChange] Name: ${file.name} Size: ${file.size}`);
      fileName.value = file.name;
      
      try {
          const text = await file.text();
          const success = parser.loadXml(text);
          
          if (!success) {
              throw new Error("XML Parser returned false (Malformed Data)");
          }

          // ONLY set loaded if parser succeeds
          isFileLoaded.value = true;
          autoDetectAll();
          console.log(`[Action: Read File] [Status: Success] [File: App.vue :: onFileChange] File Processed.`);
          
      } catch (e) {
          console.error(e);
          alert("Error parsing XML: Data is malformed or not a valid parameter file.");
          console.error(`[Action: Read File] [Status: Error] [File: App.vue :: onFileChange] ${e}`);
          
          // RESET STATE ON FAILURE
          isFileLoaded.value = false;
          fileName.value = ""; 
          parser.loadXml(""); // Clear internal doc
      } finally {
          // Allow re-uploading the same file if needed
          target.value = ''; 
      }
  } else {
      console.log(`[Action: User Select File] [Status: Cancelled] [File: App.vue :: onFileChange] No file selected`);
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
    console.log(`[Action: User Export] [Status: Start] [File: App.vue :: exportMarkdown] Generating Blob`);
    const blob = new Blob([displayContent.value], { type: 'text/markdown' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `${fileName.value}_Bank${currentInputBank.value}_parsed.md`;
    a.click();
    console.log(`[Action: User Export] [Status: Downloaded] [File: App.vue :: exportMarkdown] ${a.download}`);
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
                            <select v-model="currentInputBank" 
                                    @change="console.log(`[Action: User Switch InBank] [Status: Success] [File: App.vue :: UI] New Bank: ${currentInputBank}`)"
                                    class="bg-gray-800 text-blue-200 border border-gray-700 rounded px-2 py-1 text-xs outline-none focus:border-blue-500 font-mono">
                                <option v-for="b in ['A','B','C','D']" :key="b">{{b}}</option>
                            </select>
                        </div>
                    </div>
                    <div class="space-y-3">
                        <div v-for="slot in ['A','B','C','D']" :key="slot" class="flex items-center gap-3">
                            <span class="text-[10px] font-mono text-gray-500 w-4 text-center">{{slot}}</span>
                            <select 
                                v-model="fxMatrix.input[currentInputBank]![slot]"
                                @change="console.log(`[Action: User Change InputFX] [Status: Success] [File: App.vue :: UI] Bank:${currentInputBank} Slot:${slot} NewFX:${fxMatrix.input[currentInputBank]![slot]}`)"
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
                            <select v-model="currentOutputBank" 
                                    @change="console.log(`[Action: User Switch OutBank] [Status: Success] [File: App.vue :: UI] New Bank: ${currentOutputBank}`)"
                                    class="bg-gray-800 text-green-200 border border-gray-700 rounded px-2 py-1 text-xs outline-none focus:border-green-500 font-mono">
                                <option v-for="b in ['A','B','C','D']" :key="b">{{b}}</option>
                            </select>
                        </div>
                    </div>
                    <div class="space-y-3">
                        <div v-for="slot in ['A','B','C','D']" :key="slot" class="flex items-center gap-3">
                            <span class="text-[10px] font-mono text-gray-500 w-4 text-center">{{slot}}</span>
                            <select 
                                v-model="fxMatrix.output[currentOutputBank]![slot]" 
                                @change="console.log(`[Action: User Change TrackFX] [Status: Success] [File: App.vue :: UI] Bank:${currentOutputBank} Slot:${slot} NewFX:${fxMatrix.output[currentOutputBank]![slot]}`)"
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
