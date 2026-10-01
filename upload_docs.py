import os
import urllib.parse
import urllib.request
from pathlib import Path

SUPABASE_URL = "https://wcgfvjunixmlojptkclp.supabase.co"
SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6IndjZ2Z2anVuaXhtbG9qcHRrY2xwIiwicm9sZSI6ImFub24iLCJpYXQiOjE3OTAwMDk4MTAsImV4cCI6MjEwNTU4NTgxMH0.0YredbrRG773Ug7V5h0IcCjkFBRXMZ__InZ0e1VrS5U"
BUCKET = "Documents"
FOLDER = Path("/storage/emulated/0/Download")
DONE_FILE = Path.home() / ".uploaded_docs"

uploaded = set()
if DONE_FILE.exists():
    uploaded = set(DONE_FILE.read_text(encoding="utf-8").splitlines())

extensions = (".pdf", ".doc", ".docx", ".txt", ".xls", ".xlsx")
files = [f for f in FOLDER.glob("**/*") if f.is_file() and f.suffix.lower() in extensions]
files.sort(key=lambda x: x.stat().st_mtime, reverse=True)

print(f"Trouvé {len(files)} documents au total.")

count = 0
for file_path in files[:30]:
    name = file_path.name
    if name in uploaded:
        continue
        
    print(f"Upload Doc: {name}")
    encoded_name = urllib.parse.quote(name)
    url = f"{SUPABASE_URL}/storage/v1/object/{BUCKET}/{encoded_name}"
    
    mime_type = "application/octet-stream"
    if name.endswith(".pdf"): mime_type = "application/pdf"
    elif name.endswith(".txt"): mime_type = "text/plain"
    elif name.endswith((".doc", ".docx")): mime_type = "application/msword"
    
    try:
        data = file_path.read_bytes()
        req = urllib.request.Request(url, data=data, method="POST")
        req.add_header("Authorization", f"Bearer {SUPABASE_KEY}")
        req.add_header("Content-Type", mime_type)
        # Demande à Supabase d'écraser ou d'accepter si le fichier existe déjà
        req.add_header("x-upsert", "true")
        
        with urllib.request.urlopen(req, timeout=30) as response:
            if response.status in (200, 201, 409):
                with open(DONE_FILE, "a", encoding="utf-8") as df:
                    df.write(name + "\n")
                print(f"OK ({response.status})")
                count += 1
    except urllib.error.HTTPError as e:
        # Si le fichier existe ou renvoie une erreur de doublon, on valide pour avancer
        if e.code in (400, 409):
            with open(DONE_FILE, "a", encoding="utf-8") as df:
                df.write(name + "\n")
            print(f"Déjà présent / Géré ({e.code})")
        else:
            print(f"Erreur HTTP: {e.code} - {e.reason}")
    except Exception as e:
        print(f"Erreur réseau: {e}")

print(f"Synchronisation terminée ✅ ({count} envoyés)")
