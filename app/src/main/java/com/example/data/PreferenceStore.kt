package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferenceStore(private val context: Context) {

    companion object {
        val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
        val CURRENCY_KEY = stringPreferencesKey("currency_symbol")
        val LANGUAGE_KEY = stringPreferencesKey("language_code")
        val NOTIFICATIONS_KEY = booleanPreferencesKey("notifications_enabled")
        val SECURITY_PIN_KEY = stringPreferencesKey("security_pin")
        val BIOMETRIC_ENABLED_KEY = booleanPreferencesKey("biometric_enabled")

        // Business Profile & Branding details
        val BUSINESS_NAME_KEY = stringPreferencesKey("business_name")
        val BUSINESS_PHONE_KEY = stringPreferencesKey("business_phone")
        val BUSINESS_EMAIL_KEY = stringPreferencesKey("business_email")
        val BUSINESS_ADDRESS_KEY = stringPreferencesKey("business_address")
        val BUSINESS_BRANDING_KEY = stringPreferencesKey("business_branding")

        // Google Drive Backup info
        val GOOGLE_DRIVE_CONNECTED_KEY = booleanPreferencesKey("google_drive_connected")
        val GOOGLE_DRIVE_ACCOUNT_NAME_KEY = stringPreferencesKey("google_drive_account_name")
    }

    val darkModeFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[DARK_MODE_KEY] ?: false
    }

    val currencyFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[CURRENCY_KEY] ?: "$"
    }

    val languageFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[LANGUAGE_KEY] ?: "en"
    }

    val notificationsEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[NOTIFICATIONS_KEY] ?: true
    }

    val securityPinFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[SECURITY_PIN_KEY]
    }

    val biometricEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[BIOMETRIC_ENABLED_KEY] ?: false
    }

    // Business info flows
    val businessNameFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[BUSINESS_NAME_KEY] ?: "My Business"
    }

    val businessPhoneFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[BUSINESS_PHONE_KEY] ?: ""
    }

    val businessEmailFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[BUSINESS_EMAIL_KEY] ?: ""
    }

    val businessAddressFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[BUSINESS_ADDRESS_KEY] ?: ""
    }

    val businessBrandingFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[BUSINESS_BRANDING_KEY] ?: ""
    }

    // Google Drive flows
    val googleDriveConnectedFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[GOOGLE_DRIVE_CONNECTED_KEY] ?: false
    }

    val googleDriveAccountNameFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[GOOGLE_DRIVE_ACCOUNT_NAME_KEY] ?: ""
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = enabled
        }
    }

    suspend fun setCurrency(symbol: String) {
        context.dataStore.edit { preferences ->
            preferences[CURRENCY_KEY] = symbol
        }
    }

    suspend fun setLanguage(code: String) {
        context.dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = code
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATIONS_KEY] = enabled
        }
    }

    suspend fun setSecurityPin(pin: String?) {
        context.dataStore.edit { preferences ->
            if (pin.isNullOrEmpty()) {
                preferences.remove(SECURITY_PIN_KEY)
            } else {
                preferences[SECURITY_PIN_KEY] = pin
            }
        }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[BIOMETRIC_ENABLED_KEY] = enabled
        }
    }

    // Business Profile Setters
    suspend fun setBusinessProfile(name: String, phone: String, email: String, address: String, branding: String) {
        context.dataStore.edit { preferences ->
            preferences[BUSINESS_NAME_KEY] = name
            preferences[BUSINESS_PHONE_KEY] = phone
            preferences[BUSINESS_EMAIL_KEY] = email
            preferences[BUSINESS_ADDRESS_KEY] = address
            preferences[BUSINESS_BRANDING_KEY] = branding
        }
    }

    // Google Drive Setters
    suspend fun setGoogleDriveConnected(connected: Boolean, accountName: String) {
        context.dataStore.edit { preferences ->
            preferences[GOOGLE_DRIVE_CONNECTED_KEY] = connected
            preferences[GOOGLE_DRIVE_ACCOUNT_NAME_KEY] = accountName
        }
    }
}
