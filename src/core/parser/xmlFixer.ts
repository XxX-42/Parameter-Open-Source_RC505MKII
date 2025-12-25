/**
 * src/core/parser/xmlFixer.ts
 * Sanitize Roland RC-505mkII raw files into valid XML.
 */
export function fixXmlContent(content: string): string {
    if (!content) return "";

    // 1. Remove BOM (Byte Order Mark)
    let fixed = content.trim();
    if (fixed.charCodeAt(0) === 0xFEFF) {
        fixed = fixed.slice(1);
    }

    // 2. [CRITICAL] Remove trailing <count> metadata
    // Roland appends <count>XXXX</count> after the root element, which breaks parsers.
    fixed = fixed.replace(/<count>.*?<\/count>/gi, "");

    // 3. Fix hash tags <#> -> <_HASH>
    fixed = fixed.replace(/<#/g, '<_HASH');
    fixed = fixed.replace(/<\/#/g, '</_HASH');

    // 4. Fix numeric start tags (e.g., <1_MEM> -> <_1_MEM>)
    // Browser parsers fail on <1...
    fixed = fixed.replace(/<(\d)/g, '<_$1');
    fixed = fixed.replace(/<\/(\d)/g, '</_$1');

    // 5. Fix numeric attributes (e.g., <Tag 1_attr=...>)
    fixed = fixed.replace(/\s(\d)([\w-]*)=/g, ' _$1$2=');

    // 6. Fix unquoted attributes (e.g., name=Value -> name="Value")
    // Exclude cases where it's already quoted or part of a tag end
    fixed = fixed.replace(/(\w+)=([^"'\s><]+)/g, '$1="$2"');

    // 7. Remove illegal ASCII control characters
    fixed = fixed.replace(/[\x00-\x08\x0B\x0C\x0E-\x1F]/g, '');

    return fixed;
}
