package com.controlsphere.tvremote

import com.google.genai.Client
import org.junit.Test
import java.io.File
import java.lang.reflect.Modifier

class LiveApiExplorer {
    @Test
    fun exploreLiveApi() {
        val output = StringBuilder()
        
        val clientClass = Client::class.java
        output.append("=== Client Fields ===\n")
        clientClass.declaredFields.forEach { output.append("${it.type.simpleName} ${it.name}\n") }
        
        output.append("\n=== Client Methods ===\n")
        clientClass.declaredMethods.forEach { output.append("${it.returnType.simpleName} ${it.name}(${it.parameterTypes.joinToString { p -> p.simpleName }})\n") }
        
        try {
            val liveClientField = clientClass.declaredFields.find { it.name.contains("live", ignoreCase=true) }
            if (liveClientField != null) {
                output.append("\n=== LiveClient Fields ===\n")
                liveClientField.type.declaredFields.forEach { output.append("${it.type.simpleName} ${it.name}\n") }
                output.append("\n=== LiveClient Methods ===\n")
                liveClientField.type.declaredMethods.forEach { output.append("${it.returnType.simpleName} ${it.name}(${it.parameterTypes.joinToString { p -> p.simpleName }})\n") }
            }
        } catch (e: Exception) {
            output.append("No live field found\n")
        }
        
        val classesToTry = listOf(
            "com.google.genai.live.Live",
            "com.google.genai.live.AsyncLive",
            "com.google.genai.live.LiveSession",
            "com.google.genai.live.AsyncSession",
            "com.google.genai.live.AsyncLiveSession"
        )
        
        classesToTry.forEach { className ->
            try {
                val clazz = Class.forName(className)
                output.append("\n=== Class: $className ===\n")
                clazz.declaredMethods.filter { Modifier.isPublic(it.modifiers) }.forEach { method -> 
                    output.append("  public ${method.returnType.simpleName} ${method.name}(${method.parameterTypes.joinToString { p -> p.simpleName }})\n")
                }
            } catch (e: ClassNotFoundException) {
            }
        }
        
        File(System.getProperty("java.io.tmpdir"), "liveapi_utf8.txt").writeText(output.toString())
    }
}
