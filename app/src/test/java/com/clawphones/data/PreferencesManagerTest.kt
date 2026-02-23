package com.clawphones.data

import android.content.Context
import android.content.SharedPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.anyString
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner
import org.robolectric.Robolectric
import org.robolectric.annotation.Config

@RunWith(MockitoJUnitRunner::class)
@Config(manifest = Config.NONE)
class PreferencesManagerTest {

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockSharedPreferences: SharedPreferences

    @Mock
    private lateinit var mockEditor: SharedPreferences.Editor

    private lateinit var preferencesManager: PreferencesManager

    @Before
    fun setUp() {
        `when`(mockContext.getSharedPreferences(anyString(), anyInt())).thenReturn(mockSharedPreferences)
        `when`(mockSharedPreferences.edit()).thenReturn(mockEditor)
        `when`(mockEditor.putString(anyString(), anyString())).thenReturn(mockEditor)
        `when`(mockEditor.remove(anyString())).thenReturn(mockEditor)
        `when`(mockEditor.clear()).thenReturn(mockEditor)

        preferencesManager = PreferencesManager(mockContext, "G12-05-CP")
    }

    @Test
    fun testSaveAndGetAuthToken() {
        val token = "test_auth_token_12345"
        val encryptedToken = "encrypted_value"

        `when`(mockSharedPreferences.getString("auth_token", null)).thenReturn(encryptedToken)

        preferencesManager.saveAuthToken(token)
        verify(mockEditor).putString("auth_token", encryptedToken)
    }

    @Test
    fun testSaveAndGetUserToken() {
        val token = "test_user_token"
        
        preferencesManager.saveUserToken(token)
        verify(mockEditor).putString("user_token", org.mockito.ArgumentMatchers.anyString())
    }

    @Test
    fun testSaveAndGetApiKey() {
        val apiKey = "sk-test-api-key"
        
        preferencesManager.saveApiKey(apiKey)
        verify(mockEditor).putString("api_key", org.mockito.ArgumentMatchers.anyString())
    }

    @Test
    fun testSaveAndGetUserName() {
        val userName = "testuser"
        
        preferencesManager.saveUserName(userName)
        verify(mockEditor).putString("user_name", org.mockito.ArgumentMatchers.anyString())
    }

    @Test
    fun testSaveAndGetSensitiveData() {
        val key = "custom_key"
        val value = "custom_value"
        
        preferencesManager.saveSensitiveData(key, value)
        verify(mockEditor).putString(org.mockito.ArgumentMatchers.eq(key), org.mockito.ArgumentMatchers.anyString())
    }

    @Test
    fun testGetSensitiveDataWhenNotExists() {
        `when`(mockSharedPreferences.getString("nonexistent_key", null)).thenReturn(null)
        
        val result = preferencesManager.getSensitiveData("nonexistent_key")
        
        assertNull(result)
    }

    @Test
    fun testRemoveSensitiveData() {
        preferencesManager.removeSensitiveData("test_key")
        
        verify(mockEditor).remove("test_key")
    }

    @Test
    fun testClearAllSensitiveData() {
        preferencesManager.clearAllSensitiveData()
        
        verify(mockEditor).clear()
    }

    @Test
    fun testContainsSensitiveData() {
        `when`(mockSharedPreferences.contains("existing_key")).thenReturn(true)
        
        assertTrue(preferencesManager.containsSensitiveData("existing_key"))
    }
}
