package com.example.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "fx_oracle_server_prefs")

enum class GuideLanguage(val code: String, val label: String) {
    HINDI("HI", "🇮🇳 हिंदी"),
    HINGLISH("HINGLISH", "🇮🇳 Hinglish"),
    ENGLISH("EN", "🌐 English")
}

data class OracleServerConfig(
    val serverUrl: String = "https://152.67.184.210:8080",
    val apiKey: String = "fx-oracle-200gb-free-forever-key-9941",
    val autoBackupWifi: Boolean = true,
    val isConnected: Boolean = false,
    val guideLanguage: GuideLanguage = GuideLanguage.HINDI
)

class ServerPreferencesRepository(private val context: Context) {

    private object Keys {
        val SERVER_URL = stringPreferencesKey("oracle_server_url")
        val API_KEY = stringPreferencesKey("oracle_api_key")
        val AUTO_BACKUP_WIFI = booleanPreferencesKey("auto_backup_wifi")
        val IS_CONNECTED = booleanPreferencesKey("is_connected")
        val GUIDE_LANGUAGE = stringPreferencesKey("guide_language")
    }

    val configFlow: Flow<OracleServerConfig> = context.dataStore.data.map { prefs ->
        val langCode = prefs[Keys.GUIDE_LANGUAGE] ?: GuideLanguage.HINDI.code
        val lang = GuideLanguage.entries.find { it.code == langCode } ?: GuideLanguage.HINDI
        OracleServerConfig(
            serverUrl = prefs[Keys.SERVER_URL] ?: "https://152.67.184.210:8080",
            apiKey = prefs[Keys.API_KEY] ?: "fx-oracle-200gb-free-forever-key-9941",
            autoBackupWifi = prefs[Keys.AUTO_BACKUP_WIFI] ?: true,
            isConnected = prefs[Keys.IS_CONNECTED] ?: true,
            guideLanguage = lang
        )
    }

    suspend fun updateServerCredentials(url: String, apiKey: String, isConnected: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SERVER_URL] = url.trim().removeSuffix("/")
            prefs[Keys.API_KEY] = apiKey.trim()
            prefs[Keys.IS_CONNECTED] = isConnected
        }
    }

    suspend fun setAutoBackupWifi(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.AUTO_BACKUP_WIFI] = enabled
        }
    }

    suspend fun setGuideLanguage(language: GuideLanguage) {
        context.dataStore.edit { prefs ->
            prefs[Keys.GUIDE_LANGUAGE] = language.code
        }
    }
}
