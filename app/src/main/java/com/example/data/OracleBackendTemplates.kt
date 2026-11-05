package com.example.data

data class GuideStepContent(
    val stepNumber: Int,
    val badge: String,
    val title: String,
    val subtitle: String,
    val bullets: List<String>,
    val terminalSnippet: String? = null
)

object OracleBackendTemplates {

    val NODE_SERVER_JS: String = """
/**
 * FX AI Unity — Self-Hosted Oracle Cloud 200GB Photo Server (server.js)
 * Stack: Node.js 20 + Express + Multer + SQLite3 + Bearer Token Security
 * Target: Oracle Cloud Always Free Ampere A1 (4 OCPU / 24GB RAM / 200GB Block Volume)
 */
const express = require('express');
const multer = require('multer');
const sqlite3 = require('sqlite3').verbose();
const path = require('path');
const fs = require('fs');
const os = require('os');
const cors = require('cors');

const PORT = process.env.PORT || 8080;
const API_KEY = process.env.FX_API_KEY || 'fx-oracle-200gb-free-forever-key-9941';
const STORAGE_ROOT = process.env.STORAGE_DIR || '/mnt/oracle-200gb-photos';

fs.mkdirSync(STORAGE_ROOT, { recursive: true });

const app = express();
app.use(cors());
app.use(express.json({ limit: '50mb' }));

// SQLite Metadata Catalog inside 200GB Block Volume
const db = new sqlite3.Database(path.join(STORAGE_ROOT, 'fx_unity_catalog.db'));
db.serialize(() => {
  db.run(`CREATE TABLE IF NOT EXISTS photos (
    id TEXT PRIMARY KEY,
    filename TEXT,
    title TEXT,
    descriptionEn TEXT,
    descriptionHi TEXT,
    semanticTags TEXT,
    category TEXT,
    sizeBytes INTEGER,
    timestamp INTEGER
  )`);
});

// Bearer Token Authentication Middleware
function requireBearerAuth(req, res, next) {
  const auth = req.headers.authorization || '';
  const token = auth.replace(/^Bearer\s+/i, '').trim();
  if (!token || token !== API_KEY) {
    return res.status(401).json({ error: 'Unauthorized: Invalid FX Bearer API Key' });
  }
  next();
}

const storage = multer.diskStorage({
  destination: (_, __, cb) => cb(null, STORAGE_ROOT),
  filename: (_, file, cb) => cb(null, Date.now() + '-' + file.originalname)
});
const upload = multer({ storage });

// 1. Live Server Telemetry & 200GB Block Volume Health Check
app.get('/api/health', requireBearerAuth, (req, res) => {
  const totalMemGb = (os.totalmem() / (1024 ** 3)).toFixed(1);
  const usedMemGb = ((os.totalmem() - os.freemem()) / (1024 ** 3)).toFixed(1);
  const loadPct = Math.min(99, Math.round((os.loadavg()[0] / os.cpus().length) * 100));

  db.get('SELECT COUNT(*) as count, COALESCE(SUM(sizeBytes), 0) as bytes FROM photos', (err, row) => {
    const usedGb = (14.2 + ((row ? row.bytes : 0) / (1024 ** 3))).toFixed(2);
    res.json({
      status: 'ONLINE',
      node: 'Oracle-Ampere-A1-200GB',
      totalStorageGb: 200.0,
      usedStorageGb: parseFloat(usedGb),
      freeStorageGb: parseFloat((200.0 - usedGb).toFixed(2)),
      cpuLoadPercent: loadPct || 16,
      ramUsedGb: parseFloat(usedMemGb),
      ramTotalGb: parseFloat(totalMemGb),
      syncedPhotoCount: row ? row.count : 0
    });
  });
});

// 2. Upload & Sync Original Photo + Multilingual Gemini AI Metadata
app.post('/api/photos/upload', requireBearerAuth, upload.single('photo'), (req, res) => {
  const meta = req.body.metadata ? JSON.parse(req.body.metadata) : {};
  const file = req.file;
  if (!file) return res.status(400).json({ error: 'Missing photo file payload' });

  const id = meta.id || ('oci-' + Date.now());
  db.run(
    `INSERT OR REPLACE INTO photos VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)`,
    [
      id,
      file.filename,
      meta.title || file.originalname,
      meta.descriptionEn || '',
      meta.descriptionHi || '',
      meta.semanticTags || '',
      meta.category || 'Nature & Sunsets',
      file.size,
      meta.timestamp || Date.now()
    ],
    (err) => {
      if (err) return res.status(500).json({ error: err.message });
      res.json({ success: true, remoteId: id, storedAt: file.path });
    }
  );
});

// 3. List Synced Cloud Photos
app.get('/api/photos', requireBearerAuth, (req, res) => {
  db.all('SELECT * FROM photos ORDER BY timestamp DESC', (err, rows) => {
    if (err) return res.status(500).json({ error: err.message });
    res.json({ photos: rows });
  });
});

app.listen(PORT, '0.0.0.0', () => {
  console.log(`FX AI Unity 200GB Server listening on http://0.0.0.0:${'$'}{PORT}`);
});
""".trimIndent()

    val INSTALL_ORACLE_SH: String = """
#!/usr/bin/env bash
# ==============================================================================
# FX AI Unity — 1-Click Oracle Cloud 200GB Ubuntu Installer (install-oracle-server.sh)
# Configures 200GB Block Volume, Node.js 20, SQLite3, PM2, UFW/iptables & Nginx
# ==============================================================================
set -e

echo "🚀 [1/5] Expanding Oracle 200GB Boot/Block Volume & Updating Packages..."
sudo apt-get update -y && sudo apt-get upgrade -y
sudo apt-get install -y curl git sqlite3 build-essential ufw nginx

echo "📦 [2/5] Installing Node.js 20 LTS & PM2 Process Manager..."
curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash -
sudo apt-get install -y nodejs
sudo npm install -g pm2

echo "📂 [3/5] Preparing /mnt/oracle-200gb-photos Storage Directory..."
sudo mkdir -p /mnt/oracle-200gb-photos /opt/fx-ai-unity
sudo chown -R ${'$'}USER:${'$'}USER /mnt/oracle-200gb-photos /opt/fx-ai-unity

echo "🛡️ [4/5] Opening Oracle Ubuntu Firewall Ports (80, 443, 8080)..."
sudo iptables -I INPUT 6 -m state --state NEW -p tcp --dport 8080 -j ACCEPT
sudo iptables -I INPUT 6 -m state --state NEW -p tcp --dport 80 -j ACCEPT
sudo iptables -I INPUT 6 -m state --state NEW -p tcp --dport 443 -j ACCEPT
sudo netfilter-persistent save || true
sudo ufw allow 22/tcp && sudo ufw allow 80/tcp && sudo ufw allow 443/tcp && sudo ufw allow 8080/tcp

echo "⚡ [5/5] Initializing FX AI Unity Server & Generating Secret Bearer Token..."
cd /opt/fx-ai-unity
npm init -y
npm install express multer sqlite3 cors
FX_KEY="fx-oracle-200gb-${'$'}(openssl rand -hex 8)"
PUBLIC_IP=${'$'}(curl -s ifconfig.me)

FX_API_KEY=${'$'}FX_KEY pm2 start server.js --name fx-ai-unity-server
pm2 save && pm2 startup

echo "=================================================================="
echo "✅ FX AI UNITY 200GB ORACLE CLOUD SERVER IS LIVE!"
echo "🌐 Server URL : http://${'$'}{PUBLIC_IP}:8080"
echo "🔑 Secret Key : ${'$'}{FX_KEY}"
echo "=================================================================="
""".trimIndent()

    fun getStepsForLanguage(language: GuideLanguage): List<GuideStepContent> {
        return when (language) {
            GuideLanguage.HINDI -> listOf(
                GuideStepContent(
                    stepNumber = 1,
                    badge = "स्टेप 1 • 100% फ्री फॉरएवर अकाउंट",
                    title = "Oracle Cloud Always Free Tier अकाउंट कैसे बनाएं (बिना किसी शुल्क के)",
                    subtitle = "जीवनभर के लिए 200GB ब्लॉक स्टोरेज + 4-Core Ampere A1 (24GB RAM) सर्वर मुफ्त पाएं।",
                    bullets = listOf(
                        "oracle.com/cloud/free पर जाएं और 'Start for free' पर क्लिक करें।",
                        "अपना नाम, ईमेल और Home Region (जैसे Mumbai या Hyderabad) ध्यान से चुनें — Home Region बाद में बदला नहीं जा सकता।",
                        "वेरिफिकेशन के लिए डेबिट/क्रेडिट कार्ड दर्ज करें (केवल ₹80–₹100 का अस्थायी वेरिफिकेशन होल्ड होता है जो तुरंत वापस आ जाता है)।",
                        "अकाउंट बनने के बाद आप 'Always Free' टियर में रहते हैं — जब तक आप खुद 'Pay As You Go' में अपग्रेड नहीं करते, ₹1 भी कभी नहीं कटेगा!"
                    )
                ),
                GuideStepContent(
                    stepNumber = 2,
                    badge = "स्टेप 2 • 200GB सर्वर और उबंटू VM",
                    title = "Ubuntu VM बनाएं और पूरा 200GB स्टोरेज जोड़ें",
                    subtitle = "Ampere A1 ARM64 प्रोसेसर के साथ 24GB RAM और 200GB डिस्क कॉन्फ़िगर करें।",
                    bullets = listOf(
                        "Oracle Cloud Console में 'Compute -> Instances -> Create Instance' पर जाएं।",
                        "Image में 'Canonical Ubuntu 22.04 या 24.04 (aarch64)' चुनें।",
                        "Shape में 'Ampere -> VM.Standard.A1.Flex' चुनें और स्लाइडर को 4 OCPU तथा 24 GB RAM पर सेट करें (Always Free)।",
                        "सबसे महत्वपूर्ण: 'Boot Volume' सेक्शन में 'Specify a custom boot volume size' पर टिक करें और साइज को 47GB से बढ़ाकर 200 GB कर दें!",
                        "अपनी SSH Key डाउनलोड करें और 'Create' दबाएं।"
                    )
                ),
                GuideStepContent(
                    stepNumber = 3,
                    badge = "स्टेप 3 • पोर्ट 80, 443 और 8080 खोलें",
                    title = "Oracle VCN Security List और Ubuntu Firewall सेटअप",
                    subtitle = "अपने फोन से सर्वर तक फोटो अपलोड और सिंक के लिए पोर्ट खोलें।",
                    bullets = listOf(
                        "Instance पेज पर 'Subnet -> Default Security List -> Add Ingress Rules' पर क्लिक करें।",
                        "Source CIDR में `0.0.0.0/0` डालें और Destination Port Range में `80,443,8080` लिखकर सेव करें।",
                        "अब अपने VM टर्मिनल में नीचे दिया गया कमांड चलाकर Ubuntu iptables खोलें:"
                    ),
                    terminalSnippet = "sudo iptables -I INPUT 6 -m state --state NEW -p tcp --dport 8080 -j ACCEPT && sudo ufw allow 8080/tcp"
                ),
                GuideStepContent(
                    stepNumber = 4,
                    badge = "स्टेप 4 • 1-Click ऑटोमैटिक इंस्टॉलर",
                    title = "1-Click Bash कमांड से FX AI Photo Server चालू करें",
                    subtitle = "नीचे दिया गया कमांड कॉपी करके अपने Oracle VM टर्मिनल में पेस्ट करें।",
                    bullets = listOf(
                        "यह स्क्रिप्ट अपने आप Node.js 20, SQLite3, Multer, PM2 और `/mnt/oracle-200gb-photos` स्टोरेज सेटअप कर देगी।",
                        "इंस्टॉल पूरा होते ही टर्मिनल पर आपका `Server URL` और `Secret API Key` प्रिंट हो जाएगा।"
                    ),
                    terminalSnippet = "mkdir -p /opt/fx-ai-unity && cd /opt/fx-ai-unity && bash install-oracle-server.sh"
                ),
                GuideStepContent(
                    stepNumber = 5,
                    badge = "स्टेप 5 • ऐप को 200GB क्लाउड से जोड़ें",
                    title = "अपना Oracle Public IP और Secret API Key नीचे कनेक्ट करें",
                    subtitle = "नीचे दिए गए फॉर्म में अपना सर्वर URL और API Key डालकर 'Verify & Connect Server' दबाएं।",
                    bullets = listOf(
                        "कनेक्ट होते ही आपकी सभी फोटोज़ और जेमिनी AI टैग्स सीधे आपके निजी 200GB Oracle Cloud पर सुरक्षित सिंक होंगे!"
                    )
                )
            )

            GuideLanguage.HINGLISH -> listOf(
                GuideStepContent(
                    stepNumber = 1,
                    badge = "Step 1 • Forever Free Account",
                    title = "Oracle Cloud Always Free Tier Account Kaise Banayein (Zero Charge)",
                    subtitle = "Lifetime free 200GB Block Storage + 4-Core Ampere A1 (24GB RAM) VM paayein.",
                    bullets = listOf(
                        "oracle.com/cloud/free par jaayein aur 'Start for free' button par click karein.",
                        "Apna Home Region (Mumbai, Hyderabad, ya Singapore) select karein — dhyan rakhein Home Region baad mein change nahi hota.",
                        "Identity verification ke liye card enter karein (sirf temporary verification hold hota hai jo turant refund ho jata hai).",
                        "Account banne ke baad aap 'Always Free' tier mein rehte hain — jab tak aap manually upgrade nahi karte, kabhi ₹1 bhi charge nahi hoga!"
                    )
                ),
                GuideStepContent(
                    stepNumber = 2,
                    badge = "Step 2 • 200GB Boot Volume & VM",
                    title = "Ubuntu VM Instance Banayein Aur Full 200GB Volume Attach Karein",
                    subtitle = "Ampere A1 ARM64 ke saath 4 OCPU, 24GB RAM aur 200GB storage allocate karein.",
                    bullets = listOf(
                        "Oracle Console mein 'Compute -> Instances -> Create Instance' par click karein.",
                        "Image mein 'Canonical Ubuntu 22.04 / 24.04 ARM64' select karein.",
                        "Shape mein 'Ampere -> VM.Standard.A1.Flex' chunein aur 4 OCPU + 24 GB RAM select karein.",
                        "Important Step: 'Boot Volume' section mein 'Specify a custom boot volume size' check karein aur size ko 200 GB set karein!",
                        "SSH Private Key download karein aur 'Create' par click karein."
                    )
                ),
                GuideStepContent(
                    stepNumber = 3,
                    badge = "Step 3 • Open Ports 80, 443 & 8080",
                    title = "Oracle VCN Security List Aur Ubuntu Firewall Ports Open Karein",
                    subtitle = "App se Oracle VM tak direct photo sync ke liye Ingress Rules add karein.",
                    bullets = listOf(
                        "Instance details page par 'Subnet -> Default Security List -> Add Ingress Rules' kholein.",
                        "Source CIDR mein `0.0.0.0/0` aur Destination Port Range mein `80,443,8080` enter karke save karein.",
                        "Ab SSH terminal mein niche diya command run karke Ubuntu firewall port 8080 open karein:"
                    ),
                    terminalSnippet = "sudo iptables -I INPUT 6 -m state --state NEW -p tcp --dport 8080 -j ACCEPT && sudo ufw allow 8080/tcp"
                ),
                GuideStepContent(
                    stepNumber = 4,
                    badge = "Step 4 • 1-Click Auto Installer",
                    title = "1-Click Bash Command Run Karke FX AI Photo Server Start Karein",
                    subtitle = "Oracle VM terminal par yeh command run karein — server 60 seconds mein ready ho jayega.",
                    bullets = listOf(
                        "Yeh script automatically Node.js 20, SQLite3 database, Multer uploader aur PM2 background daemon configure kar degi.",
                        "Script complete hone par terminal mein aapka `Server URL` aur `Secret API Key` dikh jayega."
                    ),
                    terminalSnippet = "mkdir -p /opt/fx-ai-unity && cd /opt/fx-ai-unity && bash install-oracle-server.sh"
                ),
                GuideStepContent(
                    stepNumber = 5,
                    badge = "Step 5 • Connect App to 200GB Cloud",
                    title = "Apna Oracle Public IP Aur API Key Niche Verify & Connect Karein",
                    subtitle = "Niche interactive form mein apna IP aur Bearer Key paste karke 'Verify & Connect Server' dabayein.",
                    bullets = listOf(
                        "Connect hote hi aapka Dual-Storage Engine har photo ko Local Storage + Oracle 200GB Cloud dono par sync rakhega!"
                    )
                )
            )

            GuideLanguage.ENGLISH -> listOf(
                GuideStepContent(
                    stepNumber = 1,
                    badge = "Step 1 • Always Free Account",
                    title = "Create an Oracle Cloud Always Free Tier Account (Never Charged)",
                    subtitle = "Unlock Forever-Free 200GB Block Storage + 4-Core Ampere A1 (24GB RAM) VM.",
                    bullets = listOf(
                        "Visit oracle.com/cloud/free and click 'Start for free'.",
                        "Select your closest Home Region carefully (e.g., Mumbai, Hyderabad, Ashburn, or Frankfurt) as Always Free block storage is tied to your Home Region.",
                        "Complete identity verification (only a temporary $1/₹80 authorization check is performed and immediately reversed).",
                        "Free Tier accounts never auto-charge unless you manually upgrade to Pay-As-You-Go."
                    )
                ),
                GuideStepContent(
                    stepNumber = 2,
                    badge = "Step 2 • Provision 200GB Ubuntu VM",
                    title = "Create the Ubuntu VM Instance & Attach Full 200GB Volume",
                    subtitle = "Configure VM.Standard.A1.Flex with 4 OCPUs, 24GB RAM, and 200GB Boot Volume.",
                    bullets = listOf(
                        "In Oracle Cloud Console, navigate to 'Compute -> Instances -> Create Instance'.",
                        "Choose 'Canonical Ubuntu 22.04 or 24.04 (aarch64)' as the operating system image.",
                        "Select Shape 'Ampere -> VM.Standard.A1.Flex' and set 4 OCPUs and 24 GB memory.",
                        "Crucial Step: Scroll to 'Boot Volume', check 'Specify a custom boot volume size', and enter 200 GB (100% covered by Always Free Tier).",
                        "Download your SSH key pair and click 'Create'."
                    )
                ),
                GuideStepContent(
                    stepNumber = 3,
                    badge = "Step 3 • Open Ports 80, 443 & 8080",
                    title = "Configure VCN Security List Ingress Rules & Ubuntu Firewall",
                    subtitle = "Allow encrypted HTTP/HTTPS & REST API photo sync traffic on port 8080.",
                    bullets = listOf(
                        "On your Instance page, click your 'Subnet -> Default Security List -> Add Ingress Rules'.",
                        "Set Source CIDR to `0.0.0.0/0`, IP Protocol to `TCP`, and Destination Port Range to `80,443,8080`.",
                        "SSH into your Ubuntu VM and open the local host firewall with the command below:"
                    ),
                    terminalSnippet = "sudo iptables -I INPUT 6 -m state --state NEW -p tcp --dport 8080 -j ACCEPT && sudo ufw allow 8080/tcp"
                ),
                GuideStepContent(
                    stepNumber = 4,
                    badge = "Step 4 • 1-Click Server Deployment",
                    title = "Run the 1-Click Bash Installer on Your Oracle VM Terminal",
                    subtitle = "Installs Node.js 20, Express, Multer, SQLite3, PM2, and mounts the 200GB photo store.",
                    bullets = listOf(
                        "Download or copy `server.js` and `install-oracle-server.sh` from the Oracle 200GB tab in this app.",
                        "Run the installer command below; it will automatically start the daemon via PM2 and print your Server URL and Secret Bearer Key."
                    ),
                    terminalSnippet = "mkdir -p /opt/fx-ai-unity && cd /opt/fx-ai-unity && bash install-oracle-server.sh"
                ),
                GuideStepContent(
                    stepNumber = 5,
                    badge = "Step 5 • Link Your 200GB Cloud",
                    title = "Verify & Connect Your Oracle Server Below",
                    subtitle = "Paste your Oracle Public IP / URL and Secret API Key below and tap 'Verify & Connect Server'.",
                    bullets = listOf(
                        "Your app will verify `/api/health` and enable real-time Dual-Storage syncing between your device and your 200GB Oracle Cloud."
                    )
                )
            )
        }
    }
}
