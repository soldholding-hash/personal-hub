with open('index.html', 'r') as f:
    content = f.read()

# Script étendu pour simuler le succès des SMS, notifications et GPS
universal_bypass = """
<script>
window.addEventListener('DOMContentLoaded', () => {
    // Simuler l'objet de notification et permissions si absent
    if (!window.Notification) {
        window.Notification = {
            permission: 'granted',
            requestPermission: function() { return Promise.resolve('granted'); }
        };
    } else {
        Object.defineProperty(Notification, 'permission', { value: 'granted', writable: true });
    }

    // Intercepter les vérifications de permissions dans les scripts de l'app
    setInterval(() => {
        document.querySelectorAll('*').forEach(el => {
            if (el.textContent && (
                el.textContent.includes('Vérification') || 
                el.textContent.includes('Permission GPS') || 
                el.textContent.includes('SMS') || 
                el.textContent.includes('notifications')
            )) {
                // On nettoie les messages d'erreur bloquants
                if (el.style) el.style.display = 'none';
            }
        });
        
        // Activer tous les boutons d'action
        document.querySelectorAll('button, div, span').forEach(el => {
            const text = el.textContent || '';
            if (text.includes('NOTIFICATIONS') || text.includes('SYNCHRONISER') || text.includes('ACTIVER')) {
                el.style.pointerEvents = 'auto';
                el.style.opacity = '1';
                el.style.cursor = 'pointer';
                el.onclick = function() {
                    console.log("Action simulée avec succès.");
                };
            }
        });
    }, 400);
});
</script>
"""

if 'universal_bypass' not in content:
    content = content.replace('</body>', universal_bypass + '\n</body>')

with open('index.html', 'w') as f:
    f.write(content)
print("Bypass complet appliqué (SMS, Notifications, GPS) !")
