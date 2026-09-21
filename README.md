# 📡 Personal Hub — Axes Productivité Congo

Application Android qui synchronise automatiquement vers Supabase :
- 💬 **Messages WhatsApp, Messenger, Telegram, Instagram** (via notifications)
- 📱 **SMS** reçus
- 📞 **Journal d'appels** (entrants, sortants, manqués)
- 🖼️ **Photos** de la galerie (upload vers Supabase Storage)

---

## ⚙️ Configuration — 3 étapes

### 1. Supabase

1. Créer un projet sur [supabase.com](https://supabase.com)
2. Aller dans **SQL Editor** → coller et exécuter `supabase_schema.sql`
3. Aller dans **Storage** → **New bucket** → nom : `media` → **Private**
4. Récupérer dans **Project Settings → API** :
   - `Project URL` (ex: `https://abcdef.supabase.co`)
   - `anon public` key

### 2. Configurer l'app

Ouvrir le fichier :
```
app/src/main/java/com/axesproductivite/hub/SupabaseClient.kt
```

Remplacer les deux lignes :
```kotlin
const val SUPABASE_URL      = "https://XXXXXXXXXXXXXXXX.supabase.co"
const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.VOTRE_CLE_ICI"
```

### 3. Builder et installer l'APK

**Via GitHub Actions (recommandé) :**
1. Pousser ce projet sur GitHub
2. Actions → `Build APK` → `Run workflow`
3. Télécharger l'APK dans les artifacts du job
4. Installer sur le téléphone (activer "Sources inconnues")

**Via Android Studio (alternative) :**
1. Ouvrir le projet dans Android Studio
2. Build → Build APK(s) → Debug

---

## 📲 Installation sur le téléphone

Après installation de l'APK :

1. **Ouvrir l'app** → appuyer sur `Activer l'accès aux notifications`
2. Dans la liste qui s'ouvre, activer **Personal Hub**
3. Accorder toutes les permissions demandées (SMS, Contacts, Stockage, Téléphone)
4. L'app tourne en arrière-plan et synchronise toutes les 15 minutes

---

## 🗃️ Structure Supabase

| Table | Contenu |
|-------|---------|
| `messages_capture` | Messages WhatsApp, Messenger, SMS, Telegram… |
| `call_logs` | Journal d'appels avec durée et type |
| `media_files` | Métadonnées des photos uploadées |
| Storage `media/` | Fichiers photos réels |

---

## 📌 Notes importantes

- **WhatsApp/Messenger** : seul le texte visible dans la notification est capturé.
  Les médias (photos, vidéos, vocaux) apparaissent comme "Photo" et sont ignorés.
- **Photos** : limité à 10 par cycle de sync, fichiers > 10 MB ignorés (bande passante).
- **Historique** : seuls les nouveaux messages/appels/photos **après installation** sont capturés.
- L'app redémarre automatiquement après un reboot du téléphone.

---

## 🔑 Sécurité

- La clé `anon` Supabase est embarquée dans l'APK — usage **personnel uniquement**.
- Pour un usage multi-utilisateurs, implémenter l'authentification Supabase Auth.
