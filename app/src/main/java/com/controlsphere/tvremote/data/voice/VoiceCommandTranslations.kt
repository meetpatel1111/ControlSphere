package com.controlsphere.tvremote.data.voice

/**
 * Multi-language voice commands
 */
data class MultiLanguageCommand(
    val action: String,
    val keywords: List<String>,
    val response: String
)

object VoiceCommandTranslations {
    
    private val englishCommands = mapOf(
        "open" to MultiLanguageCommand(
            "launch",
            listOf("open", "launch", "start", "run"),
            "Opening"
        ),
        "search" to MultiLanguageCommand(
            "search",
            listOf("search", "find", "look for", "show me"),
            "Searching for"
        ),
        "volume_up" to MultiLanguageCommand(
            "volume_up",
            listOf("volume up", "increase volume", "louder", "turn up"),
            "Increasing volume"
        ),
        "volume_down" to MultiLanguageCommand(
            "volume_down",
            listOf("volume down", "decrease volume", "quieter", "turn down"),
            "Decreasing volume"
        ),
        "mute" to MultiLanguageCommand(
            "mute",
            listOf("mute", "silence", "quiet"),
            "Muting"
        ),
        "unmute" to MultiLanguageCommand(
            "unmute",
            listOf("unmute", "restore sound", "turn on sound"),
            "Unmuting"
        ),
        "home" to MultiLanguageCommand(
            "home",
            listOf("home", "main screen", "go home"),
            "Going to home"
        ),
        "back" to MultiLanguageCommand(
            "back",
            listOf("back", "go back", "previous"),
            "Going back"
        ),
        "netflix" to MultiLanguageCommand(
            "launch_netflix",
            listOf("netflix", "open netflix", "launch netflix"),
            "Opening Netflix"
        ),
        "youtube" to MultiLanguageCommand(
            "launch_youtube",
            listOf("youtube", "open youtube", "launch youtube"),
            "Opening YouTube"
        )
    )
    
    private val spanishCommands = mapOf(
        "abrir" to MultiLanguageCommand(
            "launch",
            listOf("abrir", "iniciar", "empezar", "lanzar"),
            "Abriendo"
        ),
        "buscar" to MultiLanguageCommand(
            "search",
            listOf("buscar", "encontrar", "mostrar", "búscame"),
            "Buscando"
        ),
        "subir_volumen" to MultiLanguageCommand(
            "volume_up",
            listOf("subir volumen", "aumentar volumen", "más alto", "subir"),
            "Subiendo volumen"
        ),
        "bajar_volumen" to MultiLanguageCommand(
            "volume_down",
            listOf("bajar volumen", "disminuir volumen", "más bajo", "bajar"),
            "Bajando volumen"
        ),
        "silenciar" to MultiLanguageCommand(
            "mute",
            listOf("silenciar", "callar", "sin sonido"),
            "Silenciando"
        ),
        "activar_sonido" to MultiLanguageCommand(
            "unmute",
            listOf("activar sonido", "restaurar sonido", "con sonido"),
            "Activando sonido"
        ),
        "inicio" to MultiLanguageCommand(
            "home",
            listOf("inicio", "pantalla principal", "ir al inicio"),
            "Yendo al inicio"
        ),
        "atras" to MultiLanguageCommand(
            "back",
            listOf("atrás", "regresar", "volver"),
            "Retrocediendo"
        ),
        "netflix" to MultiLanguageCommand(
            "launch_netflix",
            listOf("netflix", "abrir netflix", "lanzar netflix"),
            "Abriendo Netflix"
        ),
        "youtube" to MultiLanguageCommand(
            "launch_youtube",
            listOf("youtube", "abrir youtube", "lanzar youtube"),
            "Abriendo YouTube"
        )
    )
    
    private val frenchCommands = mapOf(
        "ouvrir" to MultiLanguageCommand(
            "launch",
            listOf("ouvrir", "lancer", "démarrer", "exécuter"),
            "Ouverture"
        ),
        "rechercher" to MultiLanguageCommand(
            "search",
            listOf("rechercher", "trouver", "chercher", "montre-moi"),
            "Recherche en cours"
        ),
        "augmenter_volume" to MultiLanguageCommand(
            "volume_up",
            listOf("augmenter le volume", "plus fort", "monter le volume"),
            "Augmentation du volume"
        ),
        "diminuer_volume" to MultiLanguageCommand(
            "volume_down",
            listOf("diminuer le volume", "moins fort", "baisser le volume"),
            "Diminution du volume"
        ),
        "couper_son" to MultiLanguageCommand(
            "mute",
            listOf("couper le son", "silence", "muet"),
            "Coupe du son"
        ),
        "reactiver_son" to MultiLanguageCommand(
            "unmute",
            listOf("réactiver le son", "restaurer le son", "avec son"),
            "Réactivation du son"
        ),
        "accueil" to MultiLanguageCommand(
            "home",
            listOf("accueil", "écran principal", "aller à l'accueil"),
            "Retour à l'accueil"
        ),
        "retour" to MultiLanguageCommand(
            "back",
            listOf("retour", "revenir", "précédent"),
            "Retour en arrière"
        ),
        "netflix" to MultiLanguageCommand(
            "launch_netflix",
            listOf("netflix", "ouvrir netflix", "lancer netflix"),
            "Ouverture de Netflix"
        ),
        "youtube" to MultiLanguageCommand(
            "launch_youtube",
            listOf("youtube", "ouvrir youtube", "lancer youtube"),
            "Ouverture de YouTube"
        )
    )
    
    private val germanCommands = mapOf(
        "oeffnen" to MultiLanguageCommand(
            "launch",
            listOf("öffnen", "starten", "ausführen"),
            "Öffne"
        ),
        "suchen" to MultiLanguageCommand(
            "search",
            listOf("suchen", "finden", "zeige mir"),
            "Suche nach"
        ),
        "lauter" to MultiLanguageCommand(
            "volume_up",
            listOf("lauter", "lauter stellen", "lauter machen"),
            "Lauter stellen"
        ),
        "leiser" to MultiLanguageCommand(
            "volume_down",
            listOf("leiser", "leiser stellen", "leiser machen"),
            "Leiser stellen"
        ),
        "stumm" to MultiLanguageCommand(
            "mute",
            listOf("stumm", "stumm schalten", "kein ton"),
            "Stumm schalten"
        ),
        "ton_ein" to MultiLanguageCommand(
            "unmute",
            listOf("ton einschalten", "ton wiederherstellen", "mit ton"),
            "Ton einschalten"
        ),
        "startseite" to MultiLanguageCommand(
            "home",
            listOf("startseite", "hauptbildschirm", "zur startseite"),
            "Zur Startseite"
        ),
        "zurueck" to MultiLanguageCommand(
            "back",
            listOf("zurück", "zurückgehen", "vorherige"),
            "Zurück"
        ),
        "netflix" to MultiLanguageCommand(
            "launch_netflix",
            listOf("netflix", "öffne netflix", "starte netflix"),
            "Öffne Netflix"
        ),
        "youtube" to MultiLanguageCommand(
            "launch_youtube",
            listOf("youtube", "öffne youtube", "starte youtube"),
            "Öffne YouTube"
        )
    )
    
    private val hindiCommands = mapOf(
        "khole" to MultiLanguageCommand(
            "launch",
            listOf("खोलो", "शुरू करो", "चलाओ"),
            "खोल रहा हूँ"
        ),
        "dhundo" to MultiLanguageCommand(
            "search",
            listOf("ढूंढो", "खोजो", "दिखाओ"),
            "ढूंढ रहा हूँ"
        ),
        "awaaz_badhao" to MultiLanguageCommand(
            "volume_up",
            listOf("आवाज़ बढ़ाओ", "ज़्यादा आवाज़", "ऊँची आवाज़"),
            "आवाज़ बढ़ा रहा हूँ"
        ),
        "awaaz_kam_karo" to MultiLanguageCommand(
            "volume_down",
            listOf("आवाज़ कम करो", "कम आवाज़", "धीमी आवाज़"),
            "आवाज़ कम कर रहा हूँ"
        ),
        "awaz_band_karo" to MultiLanguageCommand(
            "mute",
            listOf("आवाज़ बंद करो", "खामोश करो", "बिना आवाज़"),
            "आवाज़ बंद कर रहा हूँ"
        ),
        "awaz_chalu_karo" to MultiLanguageCommand(
            "unmute",
            listOf("आवाज़ चालू करो", "आवाज़ वापस", "आवाज़ के साथ"),
            "आवाज़ चालू कर रहा हूँ"
        ),
        "ghar" to MultiLanguageCommand(
            "home",
            listOf("घर", "मुख्य स्क्रीन", "घर जाओ"),
            "घर जा रहा हूँ"
        ),
        "peeche" to MultiLanguageCommand(
            "back",
            listOf("पीछे", "वापस जाओ", "पिछला"),
            "पीछे जा रहा हूँ"
        ),
        "netflix" to MultiLanguageCommand(
            "launch_netflix",
            listOf("नेटफ्लिक्स", "नेटफ्लिक्स खोलो", "नेटफ्लिक्स चलाओ"),
            "नेटफ्लिक्स खोल रहा हूँ"
        ),
        "youtube" to MultiLanguageCommand(
            "launch_youtube",
            listOf("यूट्यूब", "यूट्यूब खोलो", "यूट्यूब चलाओ"),
            "यूट्यूब खोल रहा हूँ"
        )
    )
    
    fun getCommandsForLanguage(language: VoiceLanguage): Map<String, MultiLanguageCommand> {
        return when (language) {
            VoiceLanguage.ENGLISH -> englishCommands
            VoiceLanguage.SPANISH -> spanishCommands
            VoiceLanguage.FRENCH -> frenchCommands
            VoiceLanguage.GERMAN -> germanCommands
            VoiceLanguage.HINDI -> hindiCommands
            else -> englishCommands
        }
    }
    
    fun parseVoiceCommand(text: String, language: VoiceLanguage): String? {
        val commands = getCommandsForLanguage(language)
        val lowerText = text.lowercase().trim()
        
        for ((action, command) in commands) {
            for (keyword in command.keywords) {
                if (lowerText.contains(keyword.lowercase())) {
                    return command.action
                }
            }
        }
        
        return null
    }
    
    fun getResponseForAction(action: String, language: VoiceLanguage): String {
        val commands = getCommandsForLanguage(language)
        return commands[action]?.response ?: "Executing command"
    }
}
