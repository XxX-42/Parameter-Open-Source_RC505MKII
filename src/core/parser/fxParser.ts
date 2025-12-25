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

    public loadXml(rawContent: string): boolean {
        console.log(`[Action: Load XML] [Status: Start] [File: fxParser.ts :: FxParser.loadXml] Length: ${rawContent.length}`);
        const fixed = fixXmlContent(rawContent);
        const parser = new DOMParser();
        this.doc = parser.parseFromString(fixed, "text/xml");

        // Critical Check: DOMParser returns a document containing a <parsererror> node if parsing fails
        const errorNode = this.doc.getElementsByTagName("parsererror");
        if (errorNode.length > 0) {
            const errorMsg = errorNode[0]?.textContent || "Unknown Error";
            console.error(`[Action: Load XML] [Status: Fail] [File: fxParser.ts :: FxParser.loadXml] XML Parsing Error: ${errorMsg}`);
            this.doc = null;
            return false;
        } else {
            console.log(`[Action: Load XML] [Status: Success] [File: fxParser.ts :: FxParser.loadXml] DOC parsed successfully`);
            return true;
        }
    }

    public parse(section: string, bank: string, slot: string, fxName: string): ParsedFx | null {
        console.log(`[Action: Parse FX] [Status: Start] [File: fxParser.ts :: FxParser.parse] Target: ${section} Bank:${bank} Slot:${slot} FX:${fxName}`);

        if (!this.doc) {
            console.warn(`[Action: Parse FX] [Status: Fail] [File: fxParser.ts :: FxParser.parse] Document not loaded`);
            return null;
        }

        const cleanFxName = fxName.toUpperCase().replace(/ /g, "_");

        // 1. Get Definition
        const defParams = FX_DEFINITIONS[cleanFxName];
        if (!defParams && fxName !== 'select') {
            console.log(`[Action: Parse FX] [Status: Info] [File: fxParser.ts :: FxParser.parse] No definition for ${fxName}, using generic fallback`);
        }

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

        if (!targetEl) {
            console.warn(`[Action: Parse FX] [Status: Fail] [File: fxParser.ts :: FxParser.parse] XML Tag ${tag} not found`);
            return null;
        }
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

        console.log(`[Action: Parse FX] [Status: Success] [File: fxParser.ts :: FxParser.parse] Parsed ${visualParams.length} params for ${fxName}`);

        return {
            fxName: fxName,
            params: visualParams
        };
    }

    public detectFx(section: string, bank: string, slot: string): string {
        console.log(`[Action: Detect FX] [Status: Start] [File: fxParser.ts :: FxParser.detectFx] ${section}/${bank}/${slot}`);
        if (!this.doc) {
            console.warn(`[Action: Detect FX] [Status: Fail] [File: fxParser.ts :: FxParser.detectFx] Document not loaded`);
            return "select";
        }

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
                console.log(`[Action: Detect FX] [Status: Success] [File: fxParser.ts :: FxParser.detectFx] Found ${name} at ${tag} (Section)`);
                return name;
            }
            // Fallback global search
            if (this.doc.getElementsByTagName(tag).length > 0) {
                console.log(`[Action: Detect FX] [Status: Success] [File: fxParser.ts :: FxParser.detectFx] Found ${name} at ${tag} (Global)`);
                return name;
            }
        }
        console.log(`[Action: Detect FX] [Status: Fail] [File: fxParser.ts :: FxParser.detectFx] No FX found for ${bank}/${slot}`);
        return "select";
    }

    public isLoaded() {
        return this.doc !== null;
    }
}
