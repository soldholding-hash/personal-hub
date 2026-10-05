import re

files = {
    'app/src/main/java/com/axesproductivite/hub/SmsSync.kt': 'last_sms_id',
    'app/src/main/java/com/axesproductivite/hub/CallLogSync.kt': 'last_call_ts',
    'app/src/main/java/com/axesproductivite/hub/MediaSync.kt': 'last_media_id',
    'app/src/main/java/com/axesproductivite/hub/CallRecordSync.kt': 'last_record_ts',
}

for filepath, key in files.items():
    with open(filepath, 'r') as f:
        content = f.read()
    content = content.replace(
        f'prefs.getLong("{key}", 0L)',
        f'0L'
    )
    with open(filepath, 'w') as f:
        f.write(content)
    print(f"Reset {key} dans {filepath}")

print("OK")
