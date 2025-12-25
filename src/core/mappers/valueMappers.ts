/**
 * src/core/mappers/valueMappers.ts
 * Logic ported 1:1 from XmlParserFX.java
 */

const FREQ_TABLE_LOW = [
    "FLAT", "20.0Hz", "25.0Hz", "31.5Hz", "40.0Hz", "50.0Hz", "63.0Hz", "80.0Hz",
    "100Hz", "125Hz", "160Hz", "200Hz", "250Hz", "315Hz", "400Hz", "500Hz",
    "630Hz", "800Hz", "1.00KHz", "1.25KHz", "1.6KHz", "2.00KHz", "2.5KHz",
    "3.15KHz", "4.00KHz", "5.00KHz", "6.3KHz", "8.00KHz", "10.00KHz", "12.5KHz"
];

const FREQ_TABLE_HIGH = [
    "20.0Hz", "25.0Hz", "31.5Hz", "40.0Hz", "50.0Hz", "63.0Hz", "80.0Hz",
    "100Hz", "125Hz", "160Hz", "200Hz", "250Hz", "315Hz", "400Hz", "500Hz",
    "630Hz", "800Hz", "1.00KHz", "1.25KHz", "1.6KHz", "2.00KHz", "2.5KHz",
    "3.15KHz", "4.00KHz", "5.00KHz", "6.3KHz", "8.00KHz", "10.00KHz", "12.5KHz", "FLAT"
];

const FREQ_TABLE_EQ = [
    "20.0Hz", "25.0Hz", "31.5Hz", "40.0Hz", "50.0Hz", "63.0Hz", "80.0Hz", "100Hz",
    "125Hz", "160Hz", "200Hz", "250Hz", "315Hz", "400Hz", "500Hz", "630Hz",
    "800Hz", "1.00KHz", "1.25KHz", "1.6KHz", "2.00KHz", "2.5KHz", "3.15KHz",
    "4.00KHz", "5.00KHz", "6.3KHz", "8.00KHz", "10.00KHz"
];

const NOTE_RATE_TABLE = [
    "4 MEAS", "2 MEAS", "1 MEAS", "1/2 note", "1/4 note dotted", "1/2 note triplet",
    "1/4 note", "1/8 note dotted", "1/4 note triplet", "1/8 note", "1/16 note dotted",
    "1/8 note triplet", "1/16note", "1/32note"
];

const STEP_RATE_TABLE = [
    "OFF", "4 MEAS", "2 MEAS", "1 MEAS", "1/2 note", "1/4 note dotted",
    "1/2 note triplet", "1/4 note", "1/8 note dotted", "1/4 note triplet", "1/8 note",
    "1/16 note dotted", "1/8 note triplet", "1/16note", "1/32note"
];

const DELAY_TIME_TABLE = [
    "1/32note", "1/16note", "1/8 note triplet", "1/16 note dotted", "1/8 note",
    "1/4 note triplet", "1/4 note dotted", "1/2 note triplet", "1/4 note", "1/2 note"
];

const AMP_TYPES = [
    "JC-120", "NATURAL CLEAN", "FULL RANGE", "COMBO CRUNCH", "STACK CRUNCH",
    "HIGAIN STACK", "POWER DRIVE", "EXTREM LEAD", "CORE METAL"
];

const DYNAMICS_TYPES = [
    "NATURALCOMP", "MIXER COMP", "LIVE COMP", "NATURAL LIM", "HARD LIM", "JINGL COMP",
    "HARD COMP", "SOFT COMP", "CLEAN COMP", "DANCE COMP", "ORCH COMP", "VOCAL COMP",
    "ACOUSTIC", "ROCK BAND", "ORCHESTRA", "LOW BOOST", "BRIGHTEN", "DJs VOICE", "PHONE VOX"
];

const SAW_TYPES = ["SAW", "Unknown", "VINTAGE_SAW", "DETUNE_SAW", "SQUARE", "RECT"];

const toInt = (val: string): number => {
    const i = parseInt(val, 10);
    return isNaN(i) ? 0 : i;
};

// --- Helper Functions ---

export const mapOnOff = (val: string): string => {
    if (val === "0" || val.toUpperCase() === "OFF" || val === "126") return "OFF";
    if (val === "1" || val.toUpperCase() === "ON" || val === "127") return "ON";
    return val;
};

export const mapRate = (val: string): string => {
    const i = toInt(val);
    return NOTE_RATE_TABLE[i] ?? String(i - 14);
};

export const mapStepRate = (val: string): string => {
    const i = toInt(val);
    return STEP_RATE_TABLE[i] ?? String(i - 15);
};

export const mapDelayTime = (val: string): string => {
    const i = toInt(val);
    return DELAY_TIME_TABLE[i] ?? (i - 8) + "ms";
};

export const mapReverbTime = (val: string): string => {
    const i = toInt(val);
    return (i * 0.1).toFixed(1) + "S";
};

export const mapGain = (val: string): string => {
    const i = toInt(val);
    if (i < 20) return `-${20 - i}dB`;
    if (i === 20) return "0dB";
    return `+${i - 20}dB`;
};

export const mapTone = (val: string): string => {
    const i = toInt(val);
    if (i < 50) return String(-(50 - i));
    if (i === 50) return "0";
    return String(i - 50);
};

export const mapQ = (val: string): string => {
    const i = toInt(val);
    switch (i) {
        case 0: return "0.5";
        case 1: return "1";
        case 2: return "2";
        case 3: return "4";
        case 4: return "8";
        case 5: return "16";
        default: return val;
    }
};

export const mapDynamics = (val: string): string => {
    const i = toInt(val);
    if (i < 20) return String(-(20 - i));
    if (i === 20) return "0";
    return "+" + (i - 20);
};

export const mapThreshold = (val: string): string => {
    const i = toInt(val);
    if (i < 30) return `-${30 - i}dB`;
    if (i === 30) return "0dB";
    return "invalid";
};

// --- Main Dispatcher ---

export const mapParameterValue = (paramLabel: string, value: string, fxName: string): string => {
    const key = paramLabel.toUpperCase().replace(/ /g, "_");
    const fx = fxName.toUpperCase().replace(/ /g, "_");

    // SPECIAL: TIME (Depends on FX type)
    if (key === "TIME") {
        if (["REVERB", "GATE_REVERB", "REVERSE_REVERB"].includes(fx)) return mapReverbTime(value);
        if (fx === "MOD_DELAY") return mapDelayTime(value);
    }

    // SPECIAL: EQ Level -> Gain
    if (key === "LEVEL" && fx === "EQ") return mapGain(value);

    // SPECIAL: DYNAMICS Type
    if (key === "TYPE" && fx === "DYNAMICS") return DYNAMICS_TYPES[toInt(value)] ?? "invalid";

    // SPECIAL: MODE
    if (key === "MODE") {
        if (value === "1") return "RC OLD";
        if (value === "2") return "RC NEW";
    }

    // SPECIAL: TWIST Release
    if (key === "RELEASE" && fx === "TWIST") {
        return value === "0" ? "FALL" : (value === "1" ? "FADE" : value);
    }

    // General Mappings
    switch (key) {
        case "SW":
        case "SYNC":
        case "RETRIG":
        case "LOOP":
        case "CARRIER_THRU":
        case "HOLD":
        case "1SHOT":
        case "BOUNCEIN":
            return mapOnOff(value);

        case "RATE":
            return mapRate(value);

        case "STEPRATE":
            return mapStepRate(value);

        case "OSC":
        case "CARRIER":
            return SAW_TYPES[toInt(value)] ?? "INVALID";

        case "TONE":
        case "FORMANT":
        case "MODSENS":
            return mapTone(value);

        case "OCTAVE":
            if (value === "0") return "-20CT";
            if (value === "1") return "-10CT";
            if (value === "2") return "0";
            if (value === "3") return "+10CT";
            return value;

        case "LO_CUT":
        case "LO_FREQ":
            return FREQ_TABLE_LOW[toInt(value)] ?? "invalid";

        case "HI_CUT":
        case "HI_FREQ":
            return FREQ_TABLE_HIGH[toInt(value)] ?? value;

        case "LM-FREQ":
        case "HM-FREQ":
            return FREQ_TABLE_EQ[toInt(value)] ?? value;

        case "PREDELAY":
            return value + "mS";

        case "PATTERN":
        case "PHRASE":
            return "P" + (toInt(value) + 1);

        case "THRESHOLD":
            return mapThreshold(value);

        case "LO_GAIN":
        case "HI_GAIN":
        case "LO_MID_GAIN":
        case "HI_MID_GAIN":
        case "LM-GAIN":
        case "HM-GAIN":
            return mapGain(value);

        case "HI_Q":
        case "LO_Q":
        case "LM-Q":
        case "HM-Q":
            return mapQ(value);

        case "DYNAMICS":
            return mapDynamics(value);

        case "AMP_TYPE":
            return AMP_TYPES[toInt(value)] ?? "invalid";

        case "STAGE":
            switch (value) {
                case "0": return "4";
                case "1": return "8";
                case "2": return "12";
                case "3": return "BI PHASE";
                default: return "invalid";
            }

        default:
            return value;
    }
};

export const normalizeValue = (val: number, min: number, max: number, _type: string): number => {
    return (val - min) / (max - min);
};
