
import fs from 'fs';
import path from 'path';

const filesToBundle = [
    "package.json",
    "vite.config.ts",
    "tailwind.config.js",
    "postcss.config.js",
    "tsconfig.json",
    "index.html",
    "src/main.ts",
    "src/style.css",
    "src/App.vue",
    "src/components/KnobControl.vue",
    "src/core/constants/fxDefinitions.ts",
    "src/core/mappers/valueMappers.ts",
    "src/core/parser/fxParser.ts",
    "src/core/parser/xmlFixer.ts"
];

const outputFile = "project_bundle.md";
let content = "# Project Bundle\n\n";

for (const filePath of filesToBundle) {
    if (fs.existsSync(filePath)) {
        content += `## ${filePath}\n\n`;
        const ext = path.extname(filePath).substring(1);
        let lang = ext;
        if (ext === 'ts') lang = 'typescript';
        if (ext === 'vue') lang = 'vue';
        if (ext === 'js') lang = 'javascript';

        content += "```" + lang + "\n";
        try {
            content += fs.readFileSync(filePath, 'utf-8');
        } catch (e) {
            content += `// Error reading file: ${e}`;
        }
        content += "\n```\n\n";
    } else {
        content += `## ${filePath} (File Not Found)\n\n`;
    }
}

fs.writeFileSync(outputFile, content, 'utf-8');
console.log(`Bundled ${filesToBundle.length} files into ${outputFile}`);
