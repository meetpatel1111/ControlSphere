package com.controlsphere.tvremote.data.security

import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KeyManager @Inject constructor() {
    
    private val keyPair: KeyPair by lazy {
        generateKeyPair()
    }
    
    fun getPublicKey(): PublicKey = keyPair.public
    
    fun getPrivateKey(): PrivateKey = keyPair.private
    
    fun getPublicKeyBytes(): ByteArray = keyPair.public.encoded
    
    fun getPrivateKeyBytes(): ByteArray = keyPair.private.encoded
    
    fun publicKeyFromBytes(bytes: ByteArray): PublicKey {
        val spec = X509EncodedKeySpec(bytes)
        return KeyFactory.getInstance("RSA").generatePublic(spec)
    }
    
    fun privateKeyFromBytes(bytes: ByteArray): PrivateKey {
        val spec = PKCS8EncodedKeySpec(bytes)
        return KeyFactory.getInstance("RSA").generatePrivate(spec)
    }
    
    private fun generateKeyPair(): KeyPair {
        val keyGen = KeyPairGenerator.getInstance("RSA")
        keyGen.initialize(2048)
        return keyGen.generateKeyPair()
    }
}
