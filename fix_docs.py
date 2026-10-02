with open('index.html', 'r') as f:
    content = f.read()

old = '</div>\n\n<script>'

new = '''</div>

<div class="section" id="sec-documents" style="display:none">
  <div class="section-title">📄 DOCUMENTS</div>
  <div id="list-documents" style="padding:8px"></div>
</div>

<script>'''

content = content.replace(old, new, 1)

old2 = '"messages","appels","enregistrements","photos","localisation"'
new2 = '"messages","appels","enregistrements","photos","localisation","documents"'
content = content.replace(old2, new2)

old3 = 'if(tab==="localisation") chargerLocalisation();'
new3 = '''if(tab==="localisation") chargerLocalisation();
  if(tab==="documents") chargerDocuments();'''
content = content.replace(old3, new3)

old4 = 'chargerPhotos();\nsetInterval(chargerPhotos,60000);\ncharger();'
new4 = '''chargerPhotos();
setInterval(chargerPhotos,60000);
async function chargerDocuments() {
  try {
    const r = await fetch(URL+"/storage/v1/object/list/Documents", {
      method:"POST",
      headers:{...H,"Content-Type":"application/json"},
      body:JSON.stringify({limit:100,offset:0,prefix:""})
    });
    const files = await r.json();
    if(!Array.isArray(files)||files.length===0){
      document.getElementById("list-documents").innerHTML="<div style='color:var(--text-muted);padding:12px'>Aucun document</div>";
      return;
    }
    const icons = {pdf:"📄",docx:"📝",xlsx:"📊",pptx:"📑",txt:"📃"};
    document.getElementById("list-documents").innerHTML=files.map(f=>{
      const ext=f.name.split(".").pop().toLowerCase();
      const icon=icons[ext]||"📎";
      const url=URL+"/storage/v1/object/public/Documents/"+encodeURIComponent(f.name);
      return `<a href="${url}" target="_blank" style="display:flex;align-items:center;gap:10px;padding:10px;border-bottom:1px solid var(--border);text-decoration:none;color:var(--text-primary)">
        <span style="font-size:22px">${icon}</span>
        <div style="flex:1;overflow:hidden"><div style="font-size:12px;font-weight:500;white-space:nowrap;overflow:hidden;text-overflow:ellipsis">${f.name}</div><div style="font-size:10px;color:var(--text-muted)">${ext.toUpperCase()}</div></div>
        <span style="font-size:12px;color:var(--text-accent)">↗</span>
      </a>`;
    }).join("");
  } catch(e) {
    document.getElementById("list-documents").innerHTML="<div style='color:red;padding:12px'>Erreur: "+e.message+"</div>";
  }
}
charger();'''
content = content.replace(old4, new4)

with open('index.html', 'w') as f:
    f.write(content)
print("OK")
