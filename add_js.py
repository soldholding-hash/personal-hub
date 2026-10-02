with open('index.html', 'r') as f:
    content = f.read()

js_code = """
<script>
async function loadDocuments() {
    const supabaseUrl = "https://wcgfvjunixmlojptkclp.supabase.co";
    const supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6IndjZ2Z2anVniXhtbG9qcHRrY2xwIiwicm9sZSI6ImFub24iLCJpYXQiOjE3OTAwMDk4MTAsImV4cCI6MjEwNTU4NTgxMH0.0YredbrRG773Ug7V5h0IcCjkFBRXMZ__InZ0e1VrS5U";
    
    try {
        let response = await fetch(`${supabaseUrl}/storage/v1/object/list/Documents`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${supabaseKey}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ prefix: "", limit: 100, offset: 0 })
        });
        
        let files = await response.json();
        let container = document.getElementById('list-documents');
        if (!container) return;
        
        if (!Array.isArray(files) || files.length === 0) {
            container.innerHTML = '<p style="color:#888; font-size:13px; padding:5px;">Aucun document trouvé.</p>';
            return;
        }
        
        let html = '<ul style="list-style:none; padding:0; margin:0; max-height:250px; overflow-y:auto;">';
        files.forEach(file => {
            let fileUrl = `${supabaseUrl}/storage/v1/object/public/Documents/${file.name}`;
            html += `<li style="padding:6px 0; border-bottom:1px solid rgba(255,255,255,0.05);">
                <a href="${fileUrl}" target="_blank" style="color:#4fa3ff; text-decoration:none; font-size:13px; word-break:break-all;">📄 ${file.name}</a>
            </li>`;
        });
        html += '</ul>';
        container.innerHTML = html;
    } catch (e) {
        console.error("Erreur chargement documents:", e);
    }
}
document.addEventListener("DOMContentLoaded", loadDocuments);
</script>
"""

if 'loadDocuments' not in content:
    content = content.replace('</body>', js_code + '\n</body>')

with open('index.html', 'w') as f:
    f.write(content)
print("Script JavaScript intégré avec succès !")
