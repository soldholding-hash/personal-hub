#!/bin/bash
SUPABASE_URL="https://wcgfvjunixmlojptkclp.supabase.co"
SUPABASE_KEY="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6IndjZ2Z2anVuaXhtbG9qcHRrY2xwIiwicm9sZSI6ImFub24iLCJpYXQiOjE3OTAwMDk4MTAsImV4cCI6MjEwNTU4NTgxMH0.0YredbrRG773Ug7V5h0IcCjkFBRXMZ__InZ0e1VrS5U"
FOLDER="/storage/emulated/0/Download"
DONE_FILE="$HOME/.uploaded_docs"

touch "$DONE_FILE"

find "$FOLDER" -type f \( -name "*.pdf" -o -name "*.doc" -o -name "*.docx" -o -name "*.txt" -o -name "*.xls" -o -name "*.xlsx" \) | sort -r | head -30 | while read file; do
  name=$(basename "$file")
  if ! grep -qx "$name" "$DONE_FILE"; then
    echo "Upload Doc: $name"
    
    # Encoder le nom pour l'URL (remplace les espaces par %20)
    encoded_name=$(python3 -c "import urllib.parse; print(urllib.parse.quote('''$name'''))")

    mime_type="application/octet-stream"
    if [[ "$name" == *.pdf ]]; then mime_type="application/pdf"; fi
    if [[ "$name" == *.txt ]]; then mime_type="text/plain"; fi
    if [[ "$name" == *.doc || "$name" == *.docx ]]; then mime_type="application/msword"; fi

    result=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$SUPABASE_URL/storage/v1/object/Documents/$encoded_name" \
      -H "Authorization: Bearer $SUPABASE_KEY" \
      -H "Content-Type: $mime_type" \
      --data-binary @"$file")
      
    if [ "$result" = "200" ] || [ "$result" = "409" ]; then
      echo "$name" >> "$DONE_FILE"
      echo "OK ($result)"
    else
      echo "Erreur HTTP: $result"
    fi
  fi
done
echo "Synchronisation des documents terminée ✅"
