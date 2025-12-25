# Project Source Code Bundle


## File: package.json
\\\$ext
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

\\\`n

## File: vite.config.ts
\\\$ext
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
})

\\\`n

## File: tsconfig.json
\\\$ext
{
  "files": [],
  "references": [
    { "path": "./tsconfig.app.json" },
    { "path": "./tsconfig.node.json" }
  ]
}

\\\`n

## File: tsconfig.node.json
\\\$ext
{
  "compilerOptions": {
    "tsBuildInfoFile": "./node_modules/.tmp/tsconfig.node.tsbuildinfo",
    "target": "ES2023",
    "lib": ["ES2023"],
    "module": "ESNext",
    "types": ["node"],
    "skipLibCheck": true,

    /* Bundler mode */
    "moduleResolution": "bundler",
    "allowImportingTsExtensions": true,
    "verbatimModuleSyntax": true,
    "moduleDetection": "force",
    "noEmit": true,

    /* Linting */
    "strict": true,
    "noUnusedLocals": true,
    "noUnusedParameters": true,
    "erasableSyntaxOnly": true,
    "noFallthroughCasesInSwitch": true,
    "noUncheckedSideEffectImports": true
  },
  "include": ["vite.config.ts"]
}

\\\`n

## File: tsconfig.app.json
\\\$ext
{
  "extends": "@vue/tsconfig/tsconfig.dom.json",
  "compilerOptions": {
    "tsBuildInfoFile": "./node_modules/.tmp/tsconfig.app.tsbuildinfo",
    "types": ["vite/client"],

    /* Linting */
    "strict": true,
    "noUnusedLocals": true,
    "noUnusedParameters": true,
    "erasableSyntaxOnly": true,
    "noFallthroughCasesInSwitch": true,
    "noUncheckedSideEffectImports": true
  },
  "include": ["src/**/*.ts", "src/**/*.tsx", "src/**/*.vue"]
}

\\\`n

## File: tailwind.config.js
\\\$ext
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

\\\`n

## File: postcss.config.js
\\\$ext
export default {
    plugins: {
        '@tailwindcss/postcss': {},
        autoprefixer: {},
    },
}

\\\`n

## File: index.html
\\\$ext
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

\\\`n

## File: README.md
\\\$ext
# Vue 3 + TypeScript + Vite

This template should help get you started developing with Vue 3 and TypeScript in Vite. The template uses Vue 3 `<script setup>` SFCs, check out the [script setup docs](https://v3.vuejs.org/api/sfc-script-setup.html#sfc-script-setup) to learn more.

Learn more about the recommended Project Setup and IDE Support in the [Vue Docs TypeScript Guide](https://vuejs.org/guide/typescript/overview.html#project-setup).

\\\`n

## File: src\App.vue
\\\$ext
<script setup lang="ts">
import { ref, computed } from 'vue';
import { FxParser, type ParsedFx } from './core/parser/fxParser';
import { FX_NAMES } from './core/constants/fxDefinitions';

const parser = new FxParser();
const isFileLoaded = ref(false);
const fileName = ref("");

// Selections
const inputBank = ref("A");
const outputBank = ref("A");
const inputFx = ref({ A: "LPF", B: "LPF", C: "LPF", D: "LPF" }); // Default selection
const outputFx = ref({ A: "LPF", B: "LPF", C: "LPF", D: "LPF" });

// Load Logic
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
          // Trigger computed re-eval
          updateTrigger.value++;
      } catch (e) {
          alert("Error parsing XML");
      }
  }
}

// Reactivity Trigger
const updateTrigger = ref(0);

const parsedInput = computed(() => {
    // Dependency on trigger
    void updateTrigger.value; 
    if (!isFileLoaded.value) return [];
    
    return ["A", "B", "C", "D"].map(slot => {
        const fxName = inputFx.value[slot as keyof typeof inputFx.value];
        if(fxName === 'select') return null;
        return parser.parse("ifx", inputBank.value, slot, fxName);
    });
});

const parsedOutput = computed(() => {
    void updateTrigger.value;
    if (!isFileLoaded.value) return [];
    
    return ["A", "B", "C", "D"].map(slot => {
        const fxName = outputFx.value[slot as keyof typeof outputFx.value];
        if(fxName === 'select') return null;
        return parser.parse("tfx", outputBank.value, slot, fxName);
    });
});

// Helper format
const formatBlock = (list: (ParsedFx|null)[], bank: string, type: 'INPUT'|'OUTPUT') => {
    let md = `== ${type} BANK ${bank} ==\n\n`;
    list.forEach(item => {
        if (!item) {
           md += `FX [Not Selected/Found]\n\n`;
           return;
        }
        md += `FX Name: ${item.fxName}\n`;
        // Table-like format
        item.params.forEach((p, i) => {
             md += `${p.padEnd(20)} ${item.values[i]}\n`;
        });
        md += "\n";
    });
    return md;
}

const displayContent = computed(() => {
    if (!isFileLoaded.value) return "Please load an XML file.";
    return formatBlock(parsedInput.value, inputBank.value, 'INPUT') + "\n" +
           formatBlock(parsedOutput.value, outputBank.value, 'OUTPUT');
});

const exportMarkdown = () => {
    const blob = new Blob([displayContent.value], { type: 'text/markdown' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `${fileName.value}_parsed.md`;
    a.click();
    URL.revokeObjectURL(url);
}

</script>

<template>
  <div class="min-h-screen relative overflow-hidden bg-gray-900 text-gray-100 font-sans">
    <!-- Background Blur -->
    <div class="absolute inset-0 z-0">
        <!-- Placeholder for background -->
        <div class="w-full h-full bg-gradient-to-br from-purple-900 to-blue-900 opacity-50"></div>
        <div class="absolute inset-0 backdrop-blur-3xl"></div>
    </div>

    <!-- Content -->
    <div class="relative z-10 container mx-auto p-6 h-screen flex flex-col">
        <!-- Header -->
        <header class="flex justify-between items-center mb-6 bg-white/10 p-4 rounded-lg backdrop-blur-md border border-white/10 shadow-lg">
            <h1 class="text-2xl font-bold tracking-wider text-blue-300">RC505MKII <span class="text-white">WEB PARSER</span></h1>
            <div class="flex items-center gap-4">
                <span class="text-sm text-gray-300 font-mono">{{ fileName || "No file selected" }}</span>
                <label class="px-4 py-2 bg-blue-600 hover:bg-blue-500 rounded cursor-pointer transition shadow hover:shadow-lg font-semibold">
                    Open XML / RC0
                    <input type="file" @change="onFileChange" accept=".xml,.RC0" class="hidden" />
                </label>
            </div>
        </header>

        <main class="flex-1 flex gap-6 overflow-hidden">
            <!-- Sidebar Controls -->
            <aside class="w-80 bg-black/40 p-4 rounded-lg overflow-y-auto border border-white/5 flex flex-col gap-6 shadow-xl backdrop-blur-sm">
                
                <!-- Input Section -->
                <div>
                    <div class="flex justify-between items-center mb-2 border-b border-white/10 pb-1">
                        <label class="font-bold text-blue-400">INPUT BANK</label>
                        <select v-model="inputBank" class="bg-gray-800 border border-gray-600 rounded px-2 py-1 text-sm outline-none focus:border-blue-500">
                            <option v-for="b in ['A','B','C','D']" :key="b">{{b}}</option>
                        </select>
                    </div>
                    <div class="space-y-3">
                        <div v-for="slot in ['A','B','C','D']" :key="slot" class="flex flex-col">
                            <span class="text-xs text-gray-400 mb-1">FX {{slot}}</span>
                            <select v-model="inputFx[slot as keyof typeof inputFx]" class="bg-gray-700/50 rounded px-2 py-1.5 text-sm border border-transparent focus:border-blue-500 outline-none transition hover:bg-gray-700">
                                <option v-for="name in FX_NAMES" :key="name" :value="name">{{name}}</option>
                            </select>
                        </div>
                    </div>
                </div>

                <!-- Output Section -->
                 <div>
                    <div class="flex justify-between items-center mb-2 border-b border-white/10 pb-1">
                        <label class="font-bold text-green-400">OUTPUT BANK</label>
                        <select v-model="outputBank" class="bg-gray-800 border border-gray-600 rounded px-2 py-1 text-sm outline-none focus:border-green-500">
                             <option v-for="b in ['A','B','C','D']" :key="b">{{b}}</option>
                        </select>
                    </div>
                    <div class="space-y-3">
                        <div v-for="slot in ['A','B','C','D']" :key="slot" class="flex flex-col">
                            <span class="text-xs text-gray-400 mb-1">FX {{slot}}</span>
                            <select v-model="outputFx[slot as keyof typeof outputFx]" class="bg-gray-700/50 rounded px-2 py-1.5 text-sm border border-transparent focus:border-green-500 outline-none transition hover:bg-gray-700">
                                <option v-for="name in FX_NAMES" :key="name" :value="name">{{name}}</option>
                            </select>
                        </div>
                    </div>
                </div>

            </aside>

            <!-- Output Display -->
            <section class="flex-1 flex flex-col bg-black/60 rounded-lg border border-white/5 overflow-hidden shadow-2xl backdrop-blur-md">
                <div class="px-4 py-2 bg-black/40 border-b border-white/5 flex justify-between items-center text-xs">
                     <span class="font-mono opacity-50 tracking-wider">PARSED OUTPUT</span>
                     <button @click="exportMarkdown" class="flex items-center gap-2 text-blue-300 hover:text-white transition disabled:opacity-30 disabled:cursor-not-allowed" :disabled="!isFileLoaded">
                         <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="w-4 h-4">
                           <path stroke-linecap="round" stroke-linejoin="round" d="M3 16.5v2.25A2.25 2.25 0 005.25 21h13.5A2.25 2.25 0 0021 18.75V16.5M16.5 12L12 16.5m0 0L7.5 12m4.5 4.5V3" />
                         </svg>
                         Export to Markdown
                     </button>
                </div>
                <div class="flex-1 overflow-auto p-4 custom-scrollbar">
                  <pre class="font-mono text-sm leading-relaxed text-gray-200">{{ displayContent }}</pre>
                </div>
            </section>
        </main>
        
        <footer class="mt-4 text-center text-xs text-gray-500/50 hover:text-gray-400 transition">
             RC505MKII Open Source Project
        </footer>
    </div>
  </div>
</template>

<style>
.custom-scrollbar::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: rgba(0,0,0,0.2);
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: rgba(255,255,255,0.1);
  border-radius: 4px;
}
.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: rgba(255,255,255,0.2);
}
</style>

\\\`n

## File: src\main.ts
\\\$ext
import { createApp } from 'vue'
import './style.css'
import App from './App.vue'

createApp(App).mount('#app')

\\\`n

## File: src\style.css
\\\$ext
@import "tailwindcss";

@layer base {
  body {
    @apply bg-gray-900 text-white font-sans antialiased;
  }
}
\\\`n

## File: src\components\HelloWorld.vue
\\\$ext
<script setup lang="ts">
import { ref } from 'vue'

defineProps<{ msg: string }>()

const count = ref(0)
</script>

<template>
  <h1>{{ msg }}</h1>

  <div class="card">
    <button type="button" @click="count++">count is {{ count }}</button>
    <p>
      Edit
      <code>components/HelloWorld.vue</code> to test HMR
    </p>
  </div>

  <p>
    Check out
    <a href="https://vuejs.org/guide/quick-start.html#local" target="_blank"
      >create-vue</a
    >, the official Vue + Vite starter
  </p>
  <p>
    Learn more about IDE Support for Vue in the
    <a
      href="https://vuejs.org/guide/scaling-up/tooling.html#ide-support"
      target="_blank"
      >Vue Docs Scaling up Guide</a
    >.
  </p>
  <p class="read-the-docs">Click on the Vite and Vue logos to learn more</p>
</template>

<style scoped>
.read-the-docs {
  color: #888;
}
</style>

\\\`n

## File: src\core\constants\fxDefinitions.ts
\\\$ext
export const FX_DEFINITIONS: Record<string, { params: string[], display: string[] }> = {
    "LPF": {
        params: ["A", "B", "C", "D", "E"],
        display: ["RATE", "DEPTH", "RESONANCE", "CUTOFF", "STEPRATE"]
    },
    "BPF": {
        params: ["A", "B", "C", "D", "E"],
        display: ["RATE", "DEPTH", "RESONANCE", "CUTOFF", "STEPRATE"]
    },
    "HPF": {
        params: ["A", "B", "C", "D", "E"],
        display: ["RATE", "DEPTH", "RESONANCE", "CUTOFF", "STEPRATE"]
    },
    "PHASER": {
        params: ["A", "B", "C", "D", "E"],
        display: ["RATE", "DEPTH", "RESONANCE", "MANUAL", "STEPRATE"]
    },
    "FLANGER": {
        params: ["A", "B", "C", "D", "E"],
        display: ["RATE", "DEPTH", "RESONANCE", "MANUAL", "STEPRATE"]
    },
    "SYNTH": {
        params: ["A", "B", "C", "D", "E"],
        display: ["FREQUENCY", "RESONANCE", "DECAY", "BALANCE", "ATTACK"]
    },
    "LOFI": {
        params: ["A", "B", "C", "D", "E"],
        display: ["BIT", "SAMPLE RATE", "FILTER", "BALANCE", "PRE-FILTER"]
    },
    "RADIO": {
        params: ["A", "B", "C", "D", "E"],
        display: ["LO FREQ", "HI FREQ", "DETUNE", "BALANCE", "LEVEL"]
    },
    "RING_MODULATOR": {
        params: ["A", "B", "C", "D", "E"],
        display: ["FREQUENCY", "SENS", "POLARITY", "BALANCE", "MODE"]
    },
    "G2B": {
        params: ["A", "B", "C", "D", "E"],
        display: ["SENS", "DECAY", "ATTACK", "BALANCE", "MODE"]
    },
    "SUSTAINER": {
        params: ["A", "B", "C", "D", "E"],
        display: ["DEPTH", "SUSTAIN", "ATTACK", "LEVEL", "LIMITER"]
    },
    "EQ": {
        params: ["A", "B", "C", "D", "E"],
        display: ["LO GAIN", "HI GAIN", "LEVEL", "LO MID GAIN", "HI MID GAIN"]
    },
    "DYNAMICS": {
        params: ["A", "B", "C", "D", "E"],
        display: ["TYPE", "THRESHOLD", "RATIO", "LEVEL", "ATTACK"]
    },
    // Add more as needed based on specific user requirements or complete list
    "DELAY": {
        params: ["A", "B", "C", "D", "E"],
        display: ["TIME", "FEEDBACK", "E.LEVEL", "D.LEVEL", "H.DAMP"]
    },
    "REVERB": {
        params: ["A", "B", "C", "D", "E"],
        display: ["TIME", "TONE", "E.LEVEL", "D.LEVEL", "LOW CUT"]
    }
};

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

\\\`n

## File: src\core\mappers\valueMappers.ts
\\\$ext
export type MapperFunction = (raw: string) => string;

const ON_OFF: MapperFunction = (raw) => (raw === "1" || raw.toUpperCase() === "ON") ? "ON" : "OFF";

const DB: MapperFunction = (raw) => {
    const i = parseInt(raw, 10);
    if (isNaN(i)) return raw;
    if (i < 20) return `-${20 - i}dB`;
    if (i === 20) return "0dB";
    return `+${i - 20}dB`;
};

const NOTE_RATES: Record<string, string> = {
    "0": "4 MEAS", "1": "2 MEAS", "2": "1 MEAS",
    "3": "1/2 note", "4": "1/4 note dotted", "5": "1/2 note triplet",
    "6": "1/4 note", "7": "1/8 note dotted", "8": "1/4 note triplet",
    "9": "1/8 note", "10": "1/16 note dotted", "11": "1/8 note triplet",
    "12": "1/16note", "13": "1/32note"
};

const RATE: MapperFunction = (raw) => {
    // Try to parse int
    const i = parseInt(raw, 10);
    const note = NOTE_RATES[raw];
    if (note) return note;

    if (!isNaN(i) && i > 13) return `${i - 14}`;
    return raw;
}

const PAN: MapperFunction = (raw) => {
    const val = parseInt(raw, 10);
    if (isNaN(val)) return raw;
    if (val === 50) return "CENTER";
    if (val < 50) return `L${50 - val}`;
    return `R${val - 50}`;
};

const FREQ_MAP = [
    "20.0Hz", "25.0Hz", "31.5Hz", "40.0Hz", "50.0Hz", "63.0Hz", "80.0Hz",
    "100Hz", "125Hz", "160Hz", "200Hz", "250Hz", "315Hz", "400Hz", "500Hz",
    "630Hz", "800Hz", "1.00KHz", "1.25KHz", "1.6KHz", "2.00KHz", "2.5KHz",
    "3.15KHz", "4.00KHz", "5.00KHz", "6.3KHz", "8.00KHz", "10.00KHz",
    "12.5KHz", "FLAT"
];
const FREQ: MapperFunction = (raw) => {
    const i = parseInt(raw, 10);
    if (!isNaN(i) && i >= 0 && i < FREQ_MAP.length) return FREQ_MAP[i] ?? raw;
    return raw;
};


export const MAPPERS: Record<string, MapperFunction> = {
    "SW": ON_OFF,
    "SYNC": ON_OFF,
    "LOOP": ON_OFF,
    "RATE": RATE,
    "STEPRATE": RATE,
    "PAN": PAN,
    "BALANCE": PAN,
    "LEVEL": (raw) => raw, // Context dependent
    "LO GAIN": DB,
    "HI GAIN": DB,
    "LO MID GAIN": DB,
    "HI MID GAIN": DB,
    "CUTOFF": FREQ,
    "LO FREQ": FREQ,
    "HI FREQ": FREQ,
    "DEFAULT": (raw) => raw
};

export function getMapper(paramName: string): MapperFunction {
    const key = paramName.toUpperCase();
    return MAPPERS[key] ?? MAPPERS["DEFAULT"] ?? ((raw) => raw);
}

\\\`n

## File: src\core\parser\fxParser.ts
\\\$ext
import { fixXmlContent } from './xmlFixer';
import { FX_DEFINITIONS } from '../constants/fxDefinitions';
import { getMapper } from '../mappers/valueMappers';

export interface ParsedFx {
    fxName: string;
    params: string[];
    values: string[];
}

export class FxParser {
    private doc: Document | null = null;

    constructor() { }

    public loadXml(rawContent: string) {
        const fixed = fixXmlContent(rawContent);
        const parser = new DOMParser();
        this.doc = parser.parseFromString(fixed, "text/xml");
        // Check for error
        if (this.doc.getElementsByTagName("parsererror").length > 0) {
            console.error("XML Parsing Error");
            this.doc = null;
        }
    }

    public parse(section: string, bank: string, slot: string, fxName: string): ParsedFx | null {
        if (!this.doc) return null;

        // Clean key
        const cleanFxName = fxName.toUpperCase().replace(/ /g, "_");

        const def = FX_DEFINITIONS[cleanFxName];
        if (!def) return null; // Unknown FX

        // Construct tag: e.g., A + A + _ + LPF  -> AA_LPF ? 
        // Java logic: bankLetter + fxLetter + "_" + fxName
        // Example: If Bank=A, Slot=A, FX=LPF => tag = "AA_LPF"
        const tag = `${bank}${slot}_${cleanFxName}`;

        // Find section first
        const sectionNodes = this.doc.getElementsByTagName(section);
        if (sectionNodes.length === 0) return null;

        const sectionEl = sectionNodes[0] as Element;
        const fxNodes = sectionEl.getElementsByTagName(tag);
        if (fxNodes.length === 0) {
            // Fallback: sometimes tags might be different? Stick to strict Java port for now.
            return null;
        }

        const fxEl = fxNodes[0] as Element;

        const validParams = def.params;
        const displayNames = def.display;

        const values: string[] = [];

        validParams.forEach((paramKey, idx) => {
            if (paramKey === "N/A") {
                values.push("N/A");
                return;
            }

            const pNodes = fxEl.getElementsByTagName(paramKey);
            if (pNodes.length > 0) {
                const node = pNodes[0];
                const raw = node?.textContent || "";
                const dispName = displayNames[idx] || paramKey;
                const mapper = getMapper(dispName);
                values.push(mapper(raw));
            } else {
                values.push("N/A");
            }
        });

        return {
            fxName: fxName, // Return original name
            params: displayNames,
            values: values
        };
    }

    public isLoaded() {
        return this.doc !== null;
    }
}

\\\`n

## File: src\core\parser\xmlFixer.ts
\\\$ext
/**
 * Fixes malformed XML where attributes are missing quotes.
 * Example: name=Value -> name="Value"
 */
export function fixXmlContent(content: string): string {
    // Regex matches attribute=value where value is not quoted and not containing spaces/brackets
    return content.replace(/(\w+)=([^"'\s>]+)/g, '$1="$2"');
}

\\\`n

