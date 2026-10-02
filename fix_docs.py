with open('index.html', 'r') as f:
    content = f.read()

docs_section = """
<div class="section" id="sec-documents" style="margin-top:20px;">
    <div class="section-title">📄 DOCUMENTS</div>
    <div id="list-documents" style="padding:8px; background:rgba(255,255,255,0.05); border-radius:8px;"></div>
</div>
"""

if 'sec-documents' not in content:
    content = content.replace(
        '<div class="section" id="sec-photos">',
        docs_section + '\n<div class="section" id="sec-photos">'
    )

with open('index.html', 'w') as f:
    f.write(content)
print("index.html mis à jour avec la section documents avec succès !")
