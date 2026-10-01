with open('index.html', 'r') as f:
    content = f.read()

old = '''document.getElementById("list-enregistrements").innerHTML = `<div class="msg"><div class="text">Voir Supabase Storage → call_recordings</div></div>`;'''

new = '''try {
    const ra = await fetch(URL+"/storage/v1/object/list/call_recordings", {
      method:"POST",
      headers:{...H,"Content-Type":"application/json"},
      body:JSON.stringify({limit:100,offset:0,prefix:""})
    });
    const files = await ra.json();
    if(Array.isArray(files)&&files.length>0){
      const recs=files.filter(f=>f.name&&(f.name.endsWith(".aac")||f.name.endsWith(".mp3")||f.name.endsWith(".m4a")));
      document.getElementById("cnt-rec").textContent=recs.length;
      document.getElementById("list-enregistrements").innerHTML=recs.map(f=>`
        <div class="recording">
          <div class="name">🎙️ ${f.name}</div>
          <audio controls style="width:100%;margin-top:6px" src="${URL}/storage/v1/object/public/call_recordings/${encodeURIComponent(f.name)}"></audio>
        </div>`).join("");
    } else {
      document.getElementById("list-enregistrements").innerHTML="<div class='msg'><div class='text'>Aucun enregistrement</div></div>";
    }
  } catch(e) {
    document.getElementById("list-enregistrements").innerHTML="<div class='msg'><div class='text'>Erreur: "+e.message+"</div></div>";
  }'''

content = content.replace(old, new)

with open('index.html', 'w') as f:
    f.write(content)

print("OK")
