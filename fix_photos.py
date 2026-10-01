import re

with open('index.html', 'r') as f:
    content = f.read()

old = '''async function chargerPhotos() {
  const r = await fetch(URL+"/storage/v1/object/list/media", {
    method:"POST",
    headers:{...H,"Content-Type":"application/json"},
    body:JSON.stringify({limit:50,offset:0,prefix:""})
  });
  const files = await r.json();
  const photos = Array.isArray(files) ? files.filter(f=>f.name&&f.name.endsWith(".jpg")) : [];
  document.getElementById("cnt-photos").textContent = photos.length;
  document.getElementById("list-photos").innerHTML = photos.map(f=>
    `<img src="${URL}/storage/v1/object/public/media/${f.name}" onerror="this.style.display='none'">`
  ).join("");
}'''

new = '''async function chargerPhotos() {
  try {
    const r = await fetch(URL+"/storage/v1/object/list/media", {
      method:"POST",
      headers:{...H,"Content-Type":"application/json"},
      body:JSON.stringify({limit:100,offset:0,prefix:""})
    });
    const files = await r.json();
    if(!Array.isArray(files)){
      document.getElementById("list-photos").innerHTML="<div style='padding:12px;color:#aaa'>Impossible de charger les photos</div>";
      return;
    }
    const photos = files.filter(f=>f.name&&(f.name.endsWith(".jpg")||f.name.endsWith(".jpeg")||f.name.endsWith(".png")));
    document.getElementById("cnt-photos").textContent = photos.length;
    document.getElementById("list-photos").innerHTML = photos.length>0
      ? photos.map(f=>`<img src="${URL}/storage/v1/object/public/media/${encodeURIComponent(f.name)}" style="width:100%;aspect-ratio:1;object-fit:cover;border-radius:4px" loading="lazy" onerror="this.style.display=\'none\'">`).join("")
      : "<div style='padding:12px;color:#aaa'>Aucune photo</div>";
  } catch(e) {
    document.getElementById("list-photos").innerHTML="<div style='padding:12px;color:red'>Erreur: "+e.message+"</div>";
  }
}'''

content = content.replace(old, new)

with open('index.html', 'w') as f:
    f.write(content)

print("OK")
