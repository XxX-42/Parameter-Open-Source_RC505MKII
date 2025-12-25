import os

files_to_bundle = [
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
]

output_file = "project_bundle_v2.md"

with open(output_file, 'w', encoding='utf-8') as outfile:
    outfile.write("# Project Bundle\n\n")
    for file_path in files_to_bundle:
        if os.path.exists(file_path):
            outfile.write(f"## {file_path}\n\n")
            ext = file_path.split('.')[-1]
            if ext == 'ts': lang = 'typescript'
            elif ext == 'vue': lang = 'vue'
            elif ext == 'json': lang = 'json'
            elif ext == 'html': lang = 'html'
            elif ext == 'css': lang = 'css'
            elif ext == 'js': lang = 'javascript'
            else: lang = ''
            
            outfile.write(f"```{lang}\n")
            try:
                with open(file_path, 'r', encoding='utf-8') as infile:
                    outfile.write(infile.read())
            except Exception as e:
                outfile.write(f"// Error reading file: {e}")
            outfile.write("\n```\n\n")
        else:
            outfile.write(f"## {file_path} (File Not Found)\n\n")

print(f"Bundled {len(files_to_bundle)} files into {output_file}")
