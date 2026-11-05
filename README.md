# FX AI Unity — Lite AI Media Suite

**An Open-Source, Self-Hosted AI Photo Cloud & Oracle Cloud 200GB Server Manager — A Forever-Free Alternative to Google Photos.**

---

## Overview

**FX AI Unity** combines a sleek, Google Photos-inspired **Liquid Glass Dark Mode** Android & Cloud experience with:
1. **Dual-Storage Engine**:
   - **Local On-Device Storage Engine (Room SQLite + File System)**: Upload, capture via live CameraX, favorite, organize into smart albums, and search photos offline with zero latency.
   - **Self-Hosted Oracle Cloud 200GB Server Sync**: Real REST API client (`OkHttp` + Bearer Token Auth) that syncs original resolution media and AI metadata to your own **Oracle Cloud Always Free Tier VM** (4-OCPU Ampere A1, 24GB RAM, 200GB Block Storage).
2. **Multilingual Gemini Vision Semantic Search ("डूबता हुआ सूरज" Feature)**:
   - Powered by **Firebase AI Logic (`gemini-3.8-flash`)** with App Check security.
   - Automatically indexes every uploaded or captured photo with rich visual descriptions in **English**, **Hindi (देवनागरी)**, and **Hinglish**, along with semantic tags, dominant colors, AI album categories, and OCR text extraction.
   - Search naturally in any language: `"डूबता हुआ सूरज"`, `"doobta hua suraj"`, `"sunset"`, `"नीला आसमान"`, `"मेरा डॉक्यूमेंट"`, or `"happy moment"`.
3. **Oracle Cloud 200GB Server Control Dashboard**:
   - Real-time telemetry gauges for 200GB Block Volume usage, Ampere A1 CPU load, 24GB RAM allocation, and network throughput.
   - Remote server actions (Health Check, Force Photo Sync, Clear Cache, Restart PM2 Media Daemon, Auto-Backup over Wi-Fi toggle) with an interactive embedded terminal console.
   - Built-in viewer, 1-click copy, and 1-click download/export for the complete production backend (`server.js` + `install-oracle-server.sh`).
4. **Trilingual Step-by-Step Oracle Cloud Setup Wizard (`🇮🇳 हिंदी` | `🇮🇳 Hinglish` | `🌐 English`)**:
   - Interactive 5-step guide walking through Oracle Cloud Always Free signup, 200GB boot volume allocation, VCN Port 80/443/8080 Ingress rules, 1-click Ubuntu installer script, and live server verification.

---

## Architecture

```
┌──────────────────────────────────────────────────────────────────────┐
│                   FX AI Unity (Android / Jetpack Compose)            │
├──────────────────────┬───────────────────────┬───────────────────────┤
│   Photos Timeline    │  Multilingual Search  │  Oracle Cloud Control │
│  CameraX & Lightbox  │  (Hindi/Hinglish/EN)  │   & Trilingual Guide  │
└──────────┬───────────┴───────────┬───────────┴───────────┬───────────┘
           │                       │                       │
           ▼                       ▼                       ▼
┌──────────────────────┐ ┌─────────────────────┐ ┌─────────────────────┐
│   Room SQLite DB +   │ │  Firebase AI Logic  │ │ OracleCloudClient   │
│  Local JPEG Storage  │ │ (gemini-3.8-flash)  │ │ (REST + Bearer Key) │
└──────────────────────┘ └─────────────────────┘ └─────────┬───────────┘
                                                           │ HTTPS / HTTP :8080
                                                           ▼
                                               ┌───────────────────────┐
                                               │  Oracle Cloud Free VM │
                                               │  Ampere A1 • 24GB RAM │
                                               │  200GB Block Storage  │
                                               │  Node.js + SQLite API │
                                               └───────────────────────┘
```

---

## Gemini Vision Semantic Search Pipeline

1. **Ingestion**: When a user captures a photo via the built-in CameraX modal or imports images via the Android Photo Picker, the image is saved to local storage and EXIF metadata (resolution, file size, device model, focal length, ISO) is extracted.
2. **Multimodal Analysis**: `GeminiVisionService` sends the bitmap to `Firebase.ai.generativeModel("gemini-3.8-flash")` with a structured `Schema.obj` JSON schema.
3. **Trilingual Indexing**: The model returns structured JSON containing:
   - `descriptionEn`: Detailed English description
   - `descriptionHi`: Detailed Hindi (Devanagari) description
   - `descriptionHinglish`: Conversational Hinglish description
   - `semanticTags`: Multilingual keywords (e.g., `["sunset", "डूबता हुआ सूरज", "doobta hua suraj", "golden hour", "सूर्यास्त"]`)
   - `ocrText`: Extracted text from documents, receipts, or signs
   - `category`: Smart Album classification (`Nature & Sunsets`, `People & Portraits`, `Documents & Screenshots`, `Night & Urban`)
4. **Query Expansion**: Searching `"डूबता हुआ सूरज"` or `"doobta hua suraj"` matches both local trilingual synsets and optional live Gemini semantic query expansion.

---

## Self-Hosting on Oracle Cloud Always Free (200GB)

1. Open the **Setup Guide** tab inside the app and choose your preferred language (`🇮🇳 हिंदी`, `🇮🇳 Hinglish`, or `🌐 English`).
2. Create an **Oracle Cloud Always Free** account and provision an **Ubuntu 22.04/24.04 ARM64 (`VM.Standard.A1.Flex`)** instance with **4 OCPUs**, **24 GB RAM**, and **200 GB Boot Volume**.
3. Open TCP ports `80`, `443`, and `8080` in your VCN Security List and Ubuntu firewall.
4. Copy the `install-oracle-server.sh` and `server.js` scripts directly from the **Oracle Control** tab in the app, run the installer on your VM, and paste your Oracle Public IP and generated Bearer Token into Step 5 of the guide.

---

## Contributing & License

Contributions, issues, and feature requests are welcome! Released under the MIT Open-Source License.
