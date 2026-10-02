with open('index.html', 'r') as f:
    content = f.read()

notif_script = """
<script>
function requestNotificationPermission() {
    if (!("Notification" in window)) {
        alert("Ce navigateur ne gère pas les notifications desktop.");
        return;
    }
    Notification.requestPermission().then(function (permission) {
        if (permission === "granted") {
            alert("Notifications activées avec succès !");
            location.reload();
        } else {
            alert("Permission refusée. Veuillez l'autoriser dans les paramètres du téléphone.");
        }
    });
}

// Attacher l'événement au bouton
document.addEventListener("DOMContentLoaded", () => {
    const btn = document.querySelector("#sec-documents") || document.querySelector("button");
    // Cherche le bouton des notifications s'il existe
    const buttons = document.querySelectorAll("button");
    buttons.forEach(b => {
        if (b.innerText.includes("NOTIFICATIONS")) {
            b.onclick = requestNotificationPermission;
        }
    });
});
</script>
"""

if 'requestNotificationPermission' not in content:
    content = content.replace('</body>', notif_script + '\n</body>')

with open('index.html', 'w') as f:
    f.write(content)
print("Correction des notifications intégrée !")
