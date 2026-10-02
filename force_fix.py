with open('index.html', 'r') as f:
    content = f.read()

# Script de contournement pour forcer le bouton à réagir
override_script = """
<script>
window.addEventListener('DOMContentLoaded', () => {
    setTimeout(() => {
        const notifBtn = Array.from(document.querySelectorAll('button, div')).find(el => el.textContent.includes('ACTIVER L\\'ACCÈS AUX NOTIFICATIONS'));
        if (notifBtn) {
            notifBtn.style.pointerEvents = 'auto';
            notifBtn.style.opacity = '1';
            notifBtn.onclick = function() {
                if ("Notification" in window) {
                    Notification.requestPermission().then(permission => {
                        alert("Statut des notifications : " + permission);
                        location.reload();
                    });
                } else {
                    alert("Les notifications ne sont pas supportées ici.");
                }
            };
        }
    }, 1000);
});
</script>
"""

if 'override_script' not in content:
    content = content.replace('</body>', override_script + '\n</body>')

with open('index.html', 'w') as f:
    f.write(content)
print("Correctif appliqué !")
