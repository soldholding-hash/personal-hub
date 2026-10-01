with open('index.html', 'r') as f:
    content = f.read()

# 1. Ajout de la fonction JavaScript pour charger les documents
js_code = '''
async function chargerDocuments() {
  try {
    const r = await fetch(URL+"/storage/v1/object/list/Documents", {
      method:"POST",
      headers:{...H,"Content-Type":"application/json"},
      body:JSON.stringify({limit:100,offset:0,prefix:""})
    });
    const files = await r.json();
    if(Array.isArray(files) && files.length > 0){
      const docs = files.filter(f=>f.name);
      document.getElementById("cnt-docs").textContent = docs.length;
      document.getElementById("list-documents").innerHTML = docs.map(f=>`
        <div class="recording" style="display:flex; justify-content:space-between; align-items:center;">
          <div class="name">📄 ${f.name}</div>
          <a href="${URL}/storage/v1/object/public/Documents/${encodeURIComponent(f.name)}" target="_blank" style="padding:4px 8px; background:#2563eb; color:#fff; border-radius:4px; text-decoration:none; font-size:12px;">Ouvrir</a>
        </div>`).join("");
    } else {
      document.getElementById("cnt-docs").textContent = "0";
      document.getElementById("list-documents").innerHTML = "<div class='msg'><div class='text'>Aucun document</div></div>";
    }
  } catch(e) {
    document.getElementById("list-documents").innerHTML = "<div class='msg'><div class='text'>Erreur: "+e.message+"</div></div>";
  }
}
'''

# Insertion du script JS avant la balise de fin de script ou de body
content = content.replace("chargerPhotos();", "chargerPhotos();\n  chargerDocuments();") + "\n<script>" + js_code + "</script>"

with open('index.html', 'w') as f:
    f.write(content)

print("UI Documents ajoutée avec succès ✅")
