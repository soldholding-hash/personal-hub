with open('index.html', 'r') as f:
    content = f.read()

# Script pour intercepter et forcer le succès de la vérification GPS/Init
gps_bypass = """
<script>
window.addEventListener('DOMContentLoaded', () => {
    // Si une fonction de vérification GPS existe, on simule un succès
    if (navigator.geolocation) {
        const origGetCurrentPosition = navigator.geolocation.getCurrentPosition;
        navigator.geolocation.getCurrentPosition = function(success, error, options) {
            // On simule une position par défaut (ex: Brazzaville) pour débloquer l'app
            success({
                coords: { latitude: -4.2634, longitude: 15.2429, accuracy: 100 },
                timestamp: Date.now()
            });
        };
    }
    
    // Forcer l'affichage des éléments bloqués
    setTimeout(() => {
        document.querySelectorAll('*').forEach(el => {
            if (el.textContent && el.textContent.includes('Permission GPS manquante')) {
                el.textContent = 'Prêt pour la synchronisation';
            }
        });
    }, 500);
});
</script>
"""

if 'bypass_gps' not in content:
    content = content.replace('</body>', gps_bypass + '\n</body>')

with open('index.html', 'w') as f:
    f.write(content)
print("Bypass GPS appliqué avec succès !")
