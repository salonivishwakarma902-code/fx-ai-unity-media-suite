package com.example.ai

import android.graphics.Bitmap
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

private const val TAG = "GeminiVisionService"

data class PhotoAiAnalysis(
    val title: String,
    val descriptionEn: String,
    val descriptionHi: String,
    val descriptionHinglish: String,
    val semanticTags: List<String>,
    val ocrText: String,
    val category: String,
    val dominantColors: List<String>
)

class GeminiVisionService {

    private val visionSchema = Schema.obj(
        properties = mapOf(
            "title" to Schema.string(description = "Short vivid title of the photo in English and Hindi"),
            "descriptionEn" to Schema.string(description = "Detailed visual scene description in English"),
            "descriptionHi" to Schema.string(description = "Detailed visual scene description in Hindi (Devanagari script)"),
            "descriptionHinglish" to Schema.string(description = "Detailed visual scene description in Hinglish (Hindi written in Latin alphabet)"),
            "semanticTags" to Schema.array(
                items = Schema.string(),
                description = "12-18 semantic tags covering objects, sky/weather, time of day, colors, mood, in English, Hindi, and Hinglish"
            ),
            "ocrText" to Schema.string(description = "All visible text inside the image via OCR, or empty string if none"),
            "category" to Schema.enumeration(
                values = listOf(
                    "Nature & Sunsets",
                    "People & Portraits",
                    "Documents & Screenshots",
                    "Night & Urban"
                ),
                description = "Best matching smart album category"
            ),
            "dominantColors" to Schema.array(
                items = Schema.string(),
                description = "3 to 4 hex color strings like #F59E0B representing dominant colors"
            )
        ),
        optionalProperties = listOf("ocrText")
    )

    private val queryExpanderSchema = Schema.obj(
        properties = mapOf(
            "expandedTerms" to Schema.array(
                items = Schema.string(),
                description = "Multilingual semantic search keywords in English, Hindi (Devanagari), and Hinglish matching the user's intent"
            ),
            "intentSummary" to Schema.string(description = "Brief bilingual summary of what visual concept is being searched")
        )
    )

    // Lazy initialization per Firebase AI Logic best practices
    private val visionModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = visionSchema
            }
        )
    }

    private val queryModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = queryExpanderSchema
            }
        )
    }

    suspend fun analyzePhoto(bitmap: Bitmap): Result<PhotoAiAnalysis> {
        return try {
            val scaledBitmap = scaleBitmapForInference(bitmap, 1024)
            val response = withTimeout(60_000L) {
                visionModel.generateContent(
                    content {
                        image(scaledBitmap)
                        text(
                            "Analyze this photo for FX AI Unity Photo Cloud. Provide a rich scene description in English, " +
                                "Hindi (Devanagari, e.g., डूबता हुआ सूरज, नीला आसमान, मेरा डॉक्यूमेंट), and Hinglish (e.g., doobta hua suraj, neela aasman). " +
                                "Extract any visible text via OCR, classify into one of the 4 categories, and list comprehensive multilingual semantic tags."
                        )
                    }
                )
            }

            val rawJson = response.text ?: throw IllegalStateException("Gemini returned an empty response.")
            val json = JSONObject(rawJson)

            val tagsArray = json.optJSONArray("semanticTags")
            val tags = buildList {
                if (tagsArray != null) {
                    for (i in 0 until tagsArray.length()) {
                        val t = tagsArray.optString(i).trim()
                        if (t.isNotEmpty()) add(t)
                    }
                }
            }

            val colorsArray = json.optJSONArray("dominantColors")
            val colors = buildList {
                if (colorsArray != null) {
                    for (i in 0 until colorsArray.length()) {
                        val c = colorsArray.optString(i).trim()
                        if (c.startsWith("#")) add(c)
                    }
                }
                if (isEmpty()) {
                    add("#38BDF8")
                    add("#F59E0B")
                    add("#0F172A")
                }
            }

            Result.success(
                PhotoAiAnalysis(
                    title = json.optString("title", "AI Indexed Photo"),
                    descriptionEn = json.optString("descriptionEn", "Detailed visual capture analyzed by Gemini Vision."),
                    descriptionHi = json.optString("descriptionHi", "जेमिनी विज़न द्वारा विश्लेषित किया गया दृश्य।"),
                    descriptionHinglish = json.optString("descriptionHinglish", "Gemini Vision dwara analyze kiya gaya photo scene."),
                    semanticTags = tags,
                    ocrText = json.optString("ocrText", ""),
                    category = json.optString("category", "Nature & Sunsets"),
                    dominantColors = colors
                )
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Gemini Vision photo analysis timed out", e)
            Result.failure(Exception("Gemini Vision analysis timed out after 60 seconds."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Gemini Vision photo analysis failed", e)
            Result.failure(e)
        }
    }

    suspend fun expandSearchQueryWithAi(rawQuery: String): Result<Pair<List<String>, String>> {
        return try {
            val response = withTimeout(45_000L) {
                queryModel.generateContent(
                    "Expand this photo search query across Hindi (Devanagari), Hinglish, and English visual concepts, " +
                        "synonyms, objects, colors, and moods: \"$rawQuery\""
                )
            }
            val jsonString = response.text ?: throw IllegalStateException("Empty response from Gemini query expander.")
            val json = JSONObject(jsonString)
            val arr = json.optJSONArray("expandedTerms")
            val terms = buildList {
                addAll(getInstantMultilingualSynonyms(rawQuery))
                if (arr != null) {
                    for (i in 0 until arr.length()) {
                        val item = arr.optString(i).trim().lowercase()
                        if (item.isNotEmpty() && !contains(item)) add(item)
                    }
                }
            }
            val summary = json.optString("intentSummary", "Expanded across Hindi, Hinglish & English")
            Result.success(terms to summary)
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Query expansion timed out", e)
            Result.failure(Exception("AI query expansion timed out."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Query expansion failed", e)
            Result.failure(e)
        }
    }

    companion object {
        private val SYNONYM_CLUSTERS: List<Set<String>> = listOf(
            setOf(
                "डूबता हुआ सूरज", "सूरज", "सूर्यास्त", "शाम", "शाम का आसमान", "नारंगी आसमान", "समुद्र",
                "doobta hua suraj", "dubta hua suraj", "suraj", "suryast", "shaam", "samudra",
                "sunset", "sun", "golden hour", "evening", "evening sky", "dusk", "horizon", "ocean"
            ),
            setOf(
                "नीला आसमान", "आसमान", "पहाड़", "हिमालय", "बर्फ", "झील", "वादियाँ",
                "neela aasman", "aasman", "pahad", "himalaya", "barf", "jheel",
                "blue sky", "sky", "mountain", "mountains", "alpine", "snow", "peaks", "lake"
            ),
            setOf(
                "मेरा डॉक्यूमेंट", "डॉक्यूमेंट", "रसीद", "बिल", "कागज़", "स्क्रीनशॉट", "इनवॉइस",
                "mera document", "document", "raseed", "receipt", "bill", "invoice", "gst", "tax",
                "paper", "screenshot", "ocr", "text"
            ),
            setOf(
                "रात का शहर", "रात", "शहर", "इमारत", "रोशनी", "लाइट",
                "raat ka shahar", "raat", "shahar", "imarat", "roshni",
                "night city", "night", "city", "cyberpunk", "neon", "skyline", "buildings", "urban"
            ),
            setOf(
                "खुशी का पल", "खुशी", "प्रकृति", "झरना", "जंगल", "हरा", "पेड़", "सुकून",
                "happy moment", "happy", "joy", "peaceful", "nature", "prakriti", "waterfall",
                "jharna", "forest", "jungle", "green", "sunbeams", "sunset"
            ),
            setOf(
                "साइबर वर्कस्पेस", "कंप्यूटर", "लैपटॉप", "सर्वर", "कोडिंग",
                "cyber workspace", "workspace", "computer", "laptop", "server", "oracle", "desk", "coding", "linux", "terminal"
            )
        )

        fun getInstantMultilingualSynonyms(query: String): List<String> {
            val clean = query.trim().lowercase()
            if (clean.isEmpty()) return emptyList()
            val tokens = clean.split(Regex("\\s+")).filter { it.isNotEmpty() }
            val result = linkedSetOf<String>()
            result.add(clean)
            result.addAll(tokens)

            for (cluster in SYNONYM_CLUSTERS) {
                val matchesCluster = cluster.any { phrase ->
                    val p = phrase.lowercase()
                    clean.contains(p) || p.contains(clean) || tokens.any { tok -> tok.length >= 3 && p.contains(tok) }
                }
                if (matchesCluster) {
                    cluster.forEach { result.add(it.lowercase()) }
                }
            }
            return result.toList()
        }

        private fun scaleBitmapForInference(source: Bitmap, maxEdge: Int): Bitmap {
            val w = source.width
            val h = source.height
            if (w <= maxEdge && h <= maxEdge) return source
            val ratio = w.toFloat() / h.toFloat()
            val newW: Int
            val newH: Int
            if (w > h) {
                newW = maxEdge
                newH = (maxEdge / ratio).toInt().coerceAtLeast(1)
            } else {
                newH = maxEdge
                newW = (maxEdge * ratio).toInt().coerceAtLeast(1)
            }
            return Bitmap.createScaledBitmap(source, newW, newH, true)
        }
    }
}
