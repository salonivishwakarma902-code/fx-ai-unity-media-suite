package com.example.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import java.io.File
import java.io.FileOutputStream

object DemoPhotoSeeder {

    fun ensureDemoPhotosSeeded(context: Context): List<PhotoEntity> {
        val photosDir = File(context.filesDir, "photos").apply { mkdirs() }
        val now = System.currentTimeMillis()
        val hour = 3_600_000L
        val day = 86_400_000L

        val specs = listOf(
            DemoSpec(
                id = "demo_sunset_01",
                fileName = "sunset_arabian_sea.jpg",
                title = "Golden Sunset Over Arabian Sea (डूबता हुआ सूरज)",
                timestamp = now - (2 * hour),
                dateGroupLabel = "Today",
                deviceModel = "Google Pixel 9 Pro • 24mm f/1.7",
                iso = "ISO 64",
                shutterSpeed = "1/500s",
                locationLabel = "Marine Drive, Mumbai, India",
                descriptionEn = "A breathtaking golden-crimson sunset with the glowing sun dipping below the calm ocean horizon, casting warm orange reflections across ripples and dramatic evening clouds.",
                descriptionHi = "शांत समुद्र के क्षितिज पर डूबता हुआ सूरज, सुनहरे और लाल-नारंगी बादलों के साथ पानी पर चमकती शाम की सुंदर रोशनी और सुकून भरा दृश्य।",
                descriptionHinglish = "Samudra ke kinare doobta hua suraj, sunehra aur laal-narangi aasman, paani par suraj ki chamakti reflection aur shaam ka sukoon bhara sunset view.",
                semanticTagsCsv = "डूबता हुआ सूरज,doobta hua suraj,sunset,सूर्यास्त,सूरज,suraj,sun,golden hour,evening sky,शाम का आसमान,ocean,समुद्र,samudra,orange sky,नारंगी आसमान,happy moment,peaceful,nature",
                ocrText = "",
                category = "Nature & Sunsets",
                dominantColorsCsv = "#F59E0B,#EF4444,#7C2D12,#0F172A",
                isFavorite = true,
                isSyncedToOracle = true,
                oracleRemoteId = "oci-obj-sunset-9921",
                renderType = RenderType.SUNSET
            ),
            DemoSpec(
                id = "demo_mountain_02",
                fileName = "himalayan_peaks_lake.jpg",
                title = "Himalayan Alpine Peaks & Blue Sky (नीला आसमान और हिमालय)",
                timestamp = now - (5 * hour),
                dateGroupLabel = "Today",
                deviceModel = "Sony Alpha A7 IV • 35mm f/1.4 GM",
                iso = "ISO 100",
                shutterSpeed = "1/1000s",
                locationLabel = "Pangong & Spiti Valley, Himachal",
                descriptionEn = "Majestic snow-capped Himalayan mountain peaks under a vivid clear blue sky with golden morning sunlight reflecting in a pristine emerald alpine lake surrounded by pine trees.",
                descriptionHi = "साफ नीले आसमान के नीचे बर्फ से ढके हिमालय के ऊंचे पहाड़ और शांत झील के पानी में चमकता खूबसूरत प्रतिबिंब।",
                descriptionHinglish = "Saaf neela aasman, barf se dhake Himalaya ke unche pahad aur shant alpine jheel mein unka shandaar reflection, ekdum ताज़ा सुबह का नज़ारा।",
                semanticTagsCsv = "नीला आसमान,neela aasman,blue sky,mountain,पहाड़,pahad,हिमालय,himalaya,snow,बर्फ,lake,झील,jheel,nature,प्रकृति,landscape,morning,happy moment,travel",
                ocrText = "",
                category = "Nature & Sunsets",
                dominantColorsCsv = "#0284C7,#38BDF8,#10B981,#F8FAFC",
                isFavorite = true,
                isSyncedToOracle = true,
                oracleRemoteId = "oci-obj-mountain-8812",
                renderType = RenderType.MOUNTAIN
            ),
            DemoSpec(
                id = "demo_document_03",
                fileName = "gst_tax_invoice_receipt.jpg",
                title = "Oracle Cloud & Hardware GST Tax Invoice (मेरा डॉक्यूमेंट रसीद)",
                timestamp = now - day - (3 * hour),
                dateGroupLabel = "Yesterday",
                deviceModel = "Pixel Document Scanner • Macro f/2.2",
                iso = "ISO 80",
                shutterSpeed = "1/120s",
                locationLabel = "Bengaluru Tech Park, India",
                descriptionEn = "High-contrast scanned document receipt and official GST tax invoice for Oracle Cloud Always Free 200GB Block Volume & ARM Ampere A1 Server provisioning with ₹0.00 total charge.",
                descriptionHi = "मेरा डॉक्यूमेंट और जीएसटी टैक्स इनवॉइस रसीद — ओरेकल क्लाउड 200GB फ्री सर्वर और हार्डवेयर बिल का आधिकारिक दस्तावेज़।",
                descriptionHinglish = "Mera document receipt aur GST tax invoice bill — Oracle Cloud 200GB Free Tier server allocation aur official payment receipt.",
                semanticTagsCsv = "मेरा डॉक्यूमेंट,mera document,document,डॉक्यूमेंट,receipt,रसीद,raseed,bill,बिल,invoice,tax invoice,gst,paper,screenshot,स्क्रीनशॉट,text,ocr,oracle cloud",
                ocrText = "TAX INVOICE #INV-2026-8841 | FX AI UNITY CLOUD | ORACLE ALWAYS FREE TIER 200GB BLOCK STORAGE | AMPERE A1 4-OCPU 24GB RAM | TOTAL PAYABLE: INR 0.00 (FOREVER FREE)",
                category = "Documents & Screenshots",
                dominantColorsCsv = "#F8FAFC,#1E293B,#0284C7,#10B981",
                isFavorite = false,
                isSyncedToOracle = true,
                oracleRemoteId = "oci-obj-doc-7740",
                renderType = RenderType.DOCUMENT
            ),
            DemoSpec(
                id = "demo_night_city_04",
                fileName = "neon_night_city.jpg",
                title = "Cyberpunk Neon Night Metropolis (रात का शहर)",
                timestamp = now - day - (8 * hour),
                dateGroupLabel = "Yesterday",
                deviceModel = "Samsung Galaxy S25 Ultra • 50mm f/1.8",
                iso = "ISO 800",
                shutterSpeed = "1/60s",
                locationLabel = "Cyber Hub, Gurugram",
                descriptionEn = "Futuristic night city skyline illuminated by glowing cyan, purple, and magenta neon lights on glass skyscrapers with shimmering reflections on wet rain-soaked streets.",
                descriptionHi = "रात का चमकता आधुनिक शहर, ऊंची इमारतों पर नीली और गुलाबी निऑन लाइट्स और बारिश के बाद सड़क पर दिखती रंगीन रोशनी।",
                descriptionHinglish = "Raat ka shahar, cyberpunk neon lights, unchi skyscrapers aur baarish ke baad sadak par chamakti colorful reflections.",
                semanticTagsCsv = "रात का शहर,raat ka shahar,night city,city,शहर,neon,cyberpunk,buildings,इमारत,skyline,night,रात,lights,रोशनी,rain,urban,architecture",
                ocrText = "CYBER PLAZA • AI CLOUD NODE 01 • 24/7",
                category = "Night & Urban",
                dominantColorsCsv = "#A855F7,#38BDF8,#EC4899,#090D16",
                isFavorite = false,
                isSyncedToOracle = false,
                oracleRemoteId = null,
                renderType = RenderType.NIGHT_CITY
            ),
            DemoSpec(
                id = "demo_cyber_workspace_05",
                fileName = "cyber_dev_workspace.jpg",
                title = "AI Engineer Dual-Monitor Oracle Server Desk (साइबर वर्कस्पेस)",
                timestamp = now - (4 * day),
                dateGroupLabel = "Earlier This Month",
                deviceModel = "iPhone 16 Pro • 28mm f/1.78",
                iso = "ISO 250",
                shutterSpeed = "1/90s",
                locationLabel = "Home Lab Studio, Pune",
                descriptionEn = "Modern developer cyber workspace featuring dual ultrawide monitors running Linux terminal logs, Oracle Cloud 200GB telemetry graphs, mechanical keyboard, and ambient cyan LED backlighting.",
                descriptionHi = "आधुनिक डेवलपर साइबर वर्कस्पेस और कंप्यूटर सेटअप जिसमें डुअल मॉनिटर पर ओरेकल क्लाउड सर्वर टर्मिनल और कोड चल रहा है।",
                descriptionHinglish = "Developer cyber workspace desk setup jahan dual monitors par Linux terminal, Oracle Cloud 200GB server dashboard aur coding chal rahi hai.",
                semanticTagsCsv = "cyber workspace,साइबर वर्कस्पेस,workspace,coding,computer,कंप्यूटर,laptop,लैपटॉप,desk,server,सर्वर,linux,terminal,developer,tech,मेरा डॉक्यूमेंट",
                ocrText = "root@oracle-ampere-200gb:~# pm2 status fx-ai-unity [ONLINE] 200GB MOUNTED",
                category = "Night & Urban",
                dominantColorsCsv = "#0EA5E9,#10B981,#1E293B,#0F172A",
                isFavorite = true,
                isSyncedToOracle = true,
                oracleRemoteId = "oci-obj-desk-6619",
                renderType = RenderType.CYBER_WORKSPACE
            ),
            DemoSpec(
                id = "demo_nature_06",
                fileName = "emerald_forest_waterfall.jpg",
                title = "Emerald Rainforest Waterfall & Sunbeams (प्रकृति और झरना)",
                timestamp = now - (6 * day),
                dateGroupLabel = "Earlier This Month",
                deviceModel = "Canon EOS R5 • 24-70mm f/2.8L",
                iso = "ISO 125",
                shutterSpeed = "1/250s",
                locationLabel = "Athirappilly & Western Ghats, Kerala",
                descriptionEn = "Lush emerald green rainforest with a cascading white waterfall plunging into a crystal-clear turquoise lagoon while golden sunbeams filter through misty tropical canopy—a joyful, happy moment in nature.",
                descriptionHi = "हरे-भरे घने जंगल के बीच गिरता हुआ सुंदर झरना, सुनहरी धूप की किरणें और खुशी व ताजगी से भरा प्राकृतिक दृश्य।",
                descriptionHinglish = "Hare-bhare jungle mein girta hua khoobsurat jharna, sunehri dhoop aur nature ke beech ekdum happy moment aur peaceful vibe.",
                semanticTagsCsv = "happy moment,खुशी का पल,nature,प्रकृति,prakriti,waterfall,झरना,jharna,forest,जंगल,jungle,green,हरा,trees,पेड़,water,पानी,डूबता हुआ सूरज,sunbeams,peaceful",
                ocrText = "",
                category = "Nature & Sunsets",
                dominantColorsCsv = "#10B981,#059669,#38BDF8,#FBBF24",
                isFavorite = true,
                isSyncedToOracle = false,
                oracleRemoteId = null,
                renderType = RenderType.NATURE_WATERFALL
            )
        )

        return specs.map { spec ->
            val file = File(photosDir, spec.fileName)
            if (!file.exists() || file.length() < 1024L) {
                val bmp = renderDemoBitmap(spec.renderType, 1200, 900)
                FileOutputStream(file).use { out ->
                    bmp.compress(Bitmap.CompressFormat.JPEG, 92, out)
                }
                bmp.recycle()
            }
            PhotoEntity(
                id = spec.id,
                title = spec.title,
                localFilePath = file.absolutePath,
                timestamp = spec.timestamp,
                dateGroupLabel = spec.dateGroupLabel,
                width = 1200,
                height = 900,
                fileSizeBytes = file.length().coerceAtLeast(245_000L),
                deviceModel = spec.deviceModel,
                iso = spec.iso,
                shutterSpeed = spec.shutterSpeed,
                locationLabel = spec.locationLabel,
                descriptionEn = spec.descriptionEn,
                descriptionHi = spec.descriptionHi,
                descriptionHinglish = spec.descriptionHinglish,
                semanticTagsCsv = spec.semanticTagsCsv,
                ocrText = spec.ocrText,
                category = spec.category,
                dominantColorsCsv = spec.dominantColorsCsv,
                isFavorite = spec.isFavorite,
                isTrashed = false,
                isSyncedToOracle = spec.isSyncedToOracle,
                oracleRemoteId = spec.oracleRemoteId,
                isAiIndexed = true
            )
        }
    }

    private enum class RenderType {
        SUNSET, MOUNTAIN, DOCUMENT, NIGHT_CITY, CYBER_WORKSPACE, NATURE_WATERFALL
    }

    private data class DemoSpec(
        val id: String,
        val fileName: String,
        val title: String,
        val timestamp: Long,
        val dateGroupLabel: String,
        val deviceModel: String,
        val iso: String,
        val shutterSpeed: String,
        val locationLabel: String,
        val descriptionEn: String,
        val descriptionHi: String,
        val descriptionHinglish: String,
        val semanticTagsCsv: String,
        val ocrText: String,
        val category: String,
        val dominantColorsCsv: String,
        val isFavorite: Boolean,
        val isSyncedToOracle: Boolean,
        val oracleRemoteId: String?,
        val renderType: RenderType
    )

    fun renderDemoBitmap(type: String, width: Int = 1200, height: Int = 900): Bitmap {
        val rt = when (type.uppercase()) {
            "SUNSET" -> RenderType.SUNSET
            "MOUNTAIN" -> RenderType.MOUNTAIN
            "DOCUMENT" -> RenderType.DOCUMENT
            "NIGHT_CITY" -> RenderType.NIGHT_CITY
            "CYBER_WORKSPACE" -> RenderType.CYBER_WORKSPACE
            else -> RenderType.NATURE_WATERFALL
        }
        return renderDemoBitmap(rt, width, height)
    }

    private fun renderDemoBitmap(type: RenderType, width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        when (type) {
            RenderType.SUNSET -> {
                // Vivid Sunset Sky Gradient
                paint.shader = LinearGradient(
                    0f, 0f, 0f, height * 0.62f,
                    intArrayOf(
                        Color.parseColor("#1E1B4B"),
                        Color.parseColor("#7C2D12"),
                        Color.parseColor("#EA580C"),
                        Color.parseColor("#F59E0B"),
                        Color.parseColor("#FEF08A")
                    ),
                    floatArrayOf(0f, 0.3f, 0.6f, 0.85f, 1f),
                    Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, 0f, width.toFloat(), height * 0.62f, paint)

                // Glowing Sun Halo & Core
                val sunX = width * 0.52f
                val sunY = height * 0.48f
                paint.shader = RadialGradient(
                    sunX, sunY, width * 0.28f,
                    intArrayOf(
                        Color.parseColor("#FFFFFF"),
                        Color.parseColor("#FDE047"),
                        Color.parseColor("#FB923C"),
                        Color.TRANSPARENT
                    ),
                    floatArrayOf(0f, 0.28f, 0.6f, 1f),
                    Shader.TileMode.CLAMP
                )
                canvas.drawCircle(sunX, sunY, width * 0.28f, paint)

                // Ocean Water Reflection
                paint.shader = LinearGradient(
                    0f, height * 0.62f, 0f, height.toFloat(),
                    intArrayOf(
                        Color.parseColor("#9A3412"),
                        Color.parseColor("#431407"),
                        Color.parseColor("#0F172A")
                    ),
                    null,
                    Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, height * 0.62f, width.toFloat(), height.toFloat(), paint)

                // Shimmering Golden Water Ripples
                paint.shader = null
                for (i in 0..18) {
                    val y = height * 0.64f + i * (height * 0.018f)
                    val spread = 70f + i * 16f
                    paint.color = Color.argb((210 - i * 9).coerceAtLeast(40), 253, 224, 71)
                    canvas.drawRoundRect(
                        RectF(sunX - spread, y, sunX + spread, y + 6f),
                        4f, 4f, paint
                    )
                }

                // Distant Coastal Headland Silhouette
                val hillPath = Path().apply {
                    moveTo(0f, height * 0.62f)
                    lineTo(width * 0.18f, height * 0.55f)
                    lineTo(width * 0.34f, height * 0.62f)
                    close()
                }
                paint.color = Color.parseColor("#1C1917")
                canvas.drawPath(hillPath, paint)
            }

            RenderType.MOUNTAIN -> {
                // Crisp Blue Sky (नीला आसमान)
                paint.shader = LinearGradient(
                    0f, 0f, 0f, height * 0.65f,
                    intArrayOf(
                        Color.parseColor("#0369A1"),
                        Color.parseColor("#0EA5E9"),
                        Color.parseColor("#BAE6FD")
                    ),
                    null,
                    Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, 0f, width.toFloat(), height * 0.65f, paint)

                // Far Snow-Capped Himalayan Range
                val mtn1 = Path().apply {
                    moveTo(0f, height * 0.65f)
                    lineTo(width * 0.22f, height * 0.24f)
                    lineTo(width * 0.42f, height * 0.48f)
                    lineTo(width * 0.68f, height * 0.18f)
                    lineTo(width * 0.88f, height * 0.42f)
                    lineTo(width.toFloat(), height * 0.28f)
                    lineTo(width.toFloat(), height * 0.65f)
                    close()
                }
                paint.shader = LinearGradient(
                    0f, height * 0.18f, 0f, height * 0.65f,
                    Color.parseColor("#F8FAFC"),
                    Color.parseColor("#334155"),
                    Shader.TileMode.CLAMP
                )
                canvas.drawPath(mtn1, paint)

                // Alpine Emerald Lake
                paint.shader = LinearGradient(
                    0f, height * 0.65f, 0f, height.toFloat(),
                    intArrayOf(
                        Color.parseColor("#0284C7"),
                        Color.parseColor("#0D9488"),
                        Color.parseColor("#064E3B")
                    ),
                    null,
                    Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, height * 0.65f, width.toFloat(), height.toFloat(), paint)
            }

            RenderType.DOCUMENT -> {
                // Desk Backdrop
                canvas.drawColor(Color.parseColor("#0F172A"))
                // Paper Sheet
                paint.shader = null
                paint.color = Color.parseColor("#F8FAFC")
                val paperRect = RectF(width * 0.14f, height * 0.07f, width * 0.86f, height * 0.93f)
                canvas.drawRoundRect(paperRect, 18f, 18f, paint)

                // Header Banner
                paint.color = Color.parseColor("#0284C7")
                canvas.drawRoundRect(
                    RectF(width * 0.18f, height * 0.12f, width * 0.82f, height * 0.24f),
                    12f, 12f, paint
                )

                val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.WHITE
                    textSize = 34f
                    typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                }
                canvas.drawText("GST TAX INVOICE • ORACLE CLOUD", width * 0.21f, height * 0.19f, textPaint)

                textPaint.color = Color.parseColor("#0F172A")
                textPaint.textSize = 26f
                canvas.drawText("INVOICE NO: INV-2026-8841 (मेरा डॉक्यूमेंट रसीद)", width * 0.20f, height * 0.33f, textPaint)
                textPaint.textSize = 22f
                textPaint.color = Color.parseColor("#334155")
                canvas.drawText("1. Oracle Cloud Always Free 200GB Block Volume   INR 0.00", width * 0.20f, height * 0.43f, textPaint)
                canvas.drawText("2. Ampere A1 ARM64 (4 OCPU / 24GB RAM VM)        INR 0.00", width * 0.20f, height * 0.50f, textPaint)
                canvas.drawText("3. Outbound Data Transfer (10 TB / Month)        INR 0.00", width * 0.20f, height * 0.57f, textPaint)

                paint.color = Color.parseColor("#10B981")
                canvas.drawRoundRect(
                    RectF(width * 0.20f, height * 0.67f, width * 0.80f, height * 0.79f),
                    12f, 12f, paint
                )
                textPaint.color = Color.WHITE
                textPaint.textSize = 28f
                canvas.drawText("PAID • TOTAL CHARGE: ₹0.00 FOREVER FREE", width * 0.23f, height * 0.745f, textPaint)
            }

            RenderType.NIGHT_CITY -> {
                // Night Sky
                paint.shader = LinearGradient(
                    0f, 0f, 0f, height.toFloat(),
                    intArrayOf(
                        Color.parseColor("#050814"),
                        Color.parseColor("#1E1B4B"),
                        Color.parseColor("#3B0764")
                    ),
                    null,
                    Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
                paint.shader = null

                // Skyscrapers with Neon Windows
                val buildingWidths = intArrayOf(110, 140, 125, 160, 130, 150, 120, 140)
                var xCursor = 40f
                buildingWidths.forEachIndexed { idx, bw ->
                    val bHeight = (height * (0.42f + (idx % 3) * 0.14f))
                    val top = height * 0.78f - bHeight
                    paint.color = Color.parseColor("#0F172A")
                    canvas.drawRect(xCursor, top, xCursor + bw, height * 0.78f, paint)

                    // Neon Roof Trim
                    paint.color = if (idx % 2 == 0) Color.parseColor("#38BDF8") else Color.parseColor("#EC4899")
                    canvas.drawRect(xCursor, top, xCursor + bw, top + 8f, paint)

                    // Glowing Windows
                    var wy = top + 26f
                    while (wy < height * 0.75f) {
                        var wx = xCursor + 16f
                        while (wx < xCursor + bw - 22f) {
                            if (((wx.toInt() + wy.toInt() + idx) % 3) != 0) {
                                paint.color = when ((wx.toInt() + idx) % 3) {
                                    0 -> Color.parseColor("#38BDF8")
                                    1 -> Color.parseColor("#F472B6")
                                    else -> Color.parseColor("#FDE047")
                                }
                                canvas.drawRect(wx, wy, wx + 14f, wy + 18f, paint)
                            }
                            wx += 26f
                        }
                        wy += 34f
                    }
                    xCursor += bw + 14f
                }

                // Wet Neon Street Reflection
                paint.shader = LinearGradient(
                    0f, height * 0.78f, width.toFloat(), height.toFloat(),
                    intArrayOf(
                        Color.parseColor("#0F172A"),
                        Color.parseColor("#38BDF8"),
                        Color.parseColor("#EC4899"),
                        Color.parseColor("#090D16")
                    ),
                    null,
                    Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, height * 0.78f, width.toFloat(), height.toFloat(), paint)
            }

            RenderType.CYBER_WORKSPACE -> {
                canvas.drawColor(Color.parseColor("#070B14"))
                // Ambient Cyan Backlight Glow
                paint.shader = RadialGradient(
                    width * 0.5f, height * 0.45f, width * 0.45f,
                    Color.parseColor("#164E63"),
                    Color.TRANSPARENT,
                    Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
                paint.shader = null

                // Left & Right Ultrawide Monitors
                val leftMon = RectF(width * 0.08f, height * 0.20f, width * 0.48f, height * 0.58f)
                val rightMon = RectF(width * 0.52f, height * 0.20f, width * 0.92f, height * 0.58f)

                paint.color = Color.parseColor("#38BDF8")
                canvas.drawRoundRect(leftMon, 14f, 14f, paint)
                paint.color = Color.parseColor("#A855F7")
                canvas.drawRoundRect(rightMon, 14f, 14f, paint)

                paint.color = Color.parseColor("#0F172A")
                canvas.drawRoundRect(RectF(leftMon.left + 6f, leftMon.top + 6f, leftMon.right - 6f, leftMon.bottom - 6f), 10f, 10f, paint)
                canvas.drawRoundRect(RectF(rightMon.left + 6f, rightMon.top + 6f, rightMon.right - 6f, rightMon.bottom - 6f), 10f, 10f, paint)

                val termPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#10B981")
                    textSize = 20f
                    typeface = Typeface.MONOSPACE
                }
                canvas.drawText("$ ssh ubuntu@oracle-200gb-vm", leftMon.left + 26f, leftMon.top + 56f, termPaint)
                termPaint.color = Color.parseColor("#38BDF8")
                canvas.drawText("Block Volume: 200.0 GB [ONLINE]", leftMon.left + 26f, leftMon.top + 96f, termPaint)
                canvas.drawText("Ampere A1: 4 OCPU / 24GB RAM", leftMon.left + 26f, leftMon.top + 136f, termPaint)
                termPaint.color = Color.parseColor("#F59E0B")
                canvas.drawText("Gemini Vision Indexer: ACTIVE", rightMon.left + 26f, rightMon.top + 66f, termPaint)
                canvas.drawText("Synced Photos: 100% Integrity", rightMon.left + 26f, rightMon.top + 110f, termPaint)

                // Desk Surface
                paint.color = Color.parseColor("#1E293B")
                canvas.drawRect(0f, height * 0.68f, width.toFloat(), height.toFloat(), paint)
            }

            RenderType.NATURE_WATERFALL -> {
                // Lush Emerald Forest Backdrop
                paint.shader = LinearGradient(
                    0f, 0f, 0f, height.toFloat(),
                    intArrayOf(
                        Color.parseColor("#064E3B"),
                        Color.parseColor("#047857"),
                        Color.parseColor("#0D9488"),
                        Color.parseColor("#0284C7")
                    ),
                    null,
                    Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

                // Cascading White-Cyan Waterfall Stream
                paint.shader = LinearGradient(
                    width * 0.38f, height * 0.12f, width * 0.62f, height * 0.78f,
                    Color.parseColor("#F0F9FF"),
                    Color.parseColor("#38BDF8"),
                    Shader.TileMode.CLAMP
                )
                canvas.drawRoundRect(
                    RectF(width * 0.40f, height * 0.12f, width * 0.60f, height * 0.78f),
                    40f, 40f, paint
                )

                // Golden Sunbeams Filtering Through Canopy
                paint.shader = RadialGradient(
                    width * 0.25f, height * 0.15f, width * 0.55f,
                    Color.argb(130, 253, 224, 71),
                    Color.TRANSPARENT,
                    Shader.TileMode.CLAMP
                )
                canvas.drawCircle(width * 0.25f, height * 0.15f, width * 0.55f, paint)
            }
        }
        return bitmap
    }
}
