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
