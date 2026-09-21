-- ============================================================
-- Personal Hub — Schéma Supabase
-- Axes Productivité Congo
-- ============================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- 1. Messages (WhatsApp, Messenger, Facebook, SMS, Telegram…)
CREATE TABLE IF NOT EXISTS messages_capture (
  id          UUID        DEFAULT gen_random_uuid() PRIMARY KEY,
  app_source  TEXT        NOT NULL,   -- 'whatsapp', 'messenger', 'sms', 'telegram'…
  contact     TEXT,                   -- nom ou numéro de l'expéditeur
  message     TEXT,                   -- contenu du message
  timestamp   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  device_id   TEXT,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_msg_app    ON messages_capture(app_source);
CREATE INDEX IF NOT EXISTS idx_msg_ts     ON messages_capture(timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_msg_contact ON messages_capture(contact);

-- 2. Journal d'appels
CREATE TABLE IF NOT EXISTS call_logs (
  id               UUID        DEFAULT gen_random_uuid() PRIMARY KEY,
  contact          TEXT,
  phone_number     TEXT,
  call_type        TEXT        CHECK (call_type IN ('incoming','outgoing','missed','rejected')),
  duration_seconds INTEGER     DEFAULT 0,
  timestamp        TIMESTAMPTZ NOT NULL,
  device_id        TEXT,
  synced_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_calls_ts    ON call_logs(timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_calls_phone ON call_logs(phone_number);

-- 3. Médias (photos) — métadonnées ; fichier dans Supabase Storage bucket "media"
CREATE TABLE IF NOT EXISTS media_files (
  id           UUID        DEFAULT gen_random_uuid() PRIMARY KEY,
  file_name    TEXT        NOT NULL,
  file_type    TEXT        DEFAULT 'image',
  storage_path TEXT        NOT NULL,
  size_bytes   BIGINT,
  captured_at  TIMESTAMPTZ,
  device_id    TEXT,
  synced_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_media_ts ON media_files(captured_at DESC);

-- RLS désactivé (usage personnel, clé anon privée)
ALTER TABLE messages_capture DISABLE ROW LEVEL SECURITY;
ALTER TABLE call_logs        DISABLE ROW LEVEL SECURITY;
ALTER TABLE media_files      DISABLE ROW LEVEL SECURITY;

-- ⚠️  Créer manuellement dans le Dashboard Supabase :
--     Storage → New bucket → Name: "media" → Private
