# ControlSphere - Smart TV Commander

A powerful Android TV remote controller that provides full UI-level control over Android TV / Google TV devices using secure ADB over WiFi.

## Features

### 🎯 Core Remote Control
- **Full D-Pad Navigation**: Complete directional control with center OK button
- **Media Controls**: Play/pause, next/previous, fast forward/rewind
- **Volume Controls**: Volume up/down and mute functionality
- **System Controls**: Home, back, apps, settings, and power buttons

### 📱 Device Management
- **Automatic Device Discovery**: Scans local network for Android TV devices
- **Manual Connection**: Connect via IP address and port
- **Secure Pairing**: RSA key-based authentication
- **Connection Status**: Real-time connection monitoring

### 🚀 App Control
- **Installed Apps List**: View all apps on the TV device
- **Quick Launch**: Launch any installed app with one tap
- **Force Stop**: Stop running applications
- **Favorites System**: Mark frequently used apps for quick access

### 🔍 Search & Text Input
- **Global Search**: Integrated search across all TV content
- **Text Input**: Full keyboard for typing text on TV
- **Voice Input**: AI-powered voice commands using Gemini AI
- **Recent Searches**: Quick access to previous searches

### 🎤 Voice Control (NEW)
- **Latest Gemini AI Integration**: Uses Gemini 2.5 Flash for optimal performance
- **Advanced Models**: Optional Gemini 2.5 Pro for complex reasoning
- **Live API Support**: Gemini 2.5 Flash Native Audio for real-time conversation
- **Sub-second Latency**: Natural voice interaction with immediate responses
- **Native Audio Streaming**: Continuous audio processing with Live API
- **High-Fidelity TTS**: Gemini 2.5 Flash TTS for premium speech synthesis
- **Computer Use Vision**: Gemini 2.5 Computer Use for visual UI automation
- **Smart Navigation**: AI-powered navigation with visual reasoning
- **Visual Automation**: Direct interface interaction with precise control
- **Voice Style Control**: 8 different voice styles with fine-grained control
- **Speech Rate Adjustment**: 6 speed settings from very slow to very fast
- **Natural Language Understanding**: Context-aware command processing
- **Smart Command Execution**: Automatically categorizes and executes voice commands
- **Multi-model Support**: Configurable model selection for different use cases
- **Secure API Integration**: Encrypted storage of Gemini API key
- **Real-time Feedback**: Visual recording indicators and processing status

### 🔒 Security
- **Encrypted Storage**: Secure storage of device keys and preferences
- **Local Network Only**: No cloud dependencies for enhanced privacy
- **RSA Authentication**: Industry-standard cryptographic security

## Technical Architecture

### Core Components
- **ADB Connection Module**: Handles WiFi-based ADB communication
- **Device Discovery**: Network scanning for compatible devices
- **Secure Storage**: Encrypted preference management
- **UI Layer**: Modern Material Design 3 with Jetpack Compose

### Key Technologies
- **Kotlin**: Primary development language
- **Jetpack Compose**: Modern UI framework
- **Hilt**: Dependency injection
- **Coroutines**: Asynchronous programming
- **Material Design 3**: Modern design system
- **Google GenAI SDK 1.0.0**: Official Gemini API library (GA)
- **Gemini 2.5 AI**: Latest voice processing and natural language understanding
- **Multi-Model Architecture**: Gemini 2.5 Flash, Pro, Live Preview, and Computer Use models
- **Computer Use Vision**: Visual UI automation and screen understanding
- **MediaProjection API**: Screen capture for visual analysis

## Setup Requirements

### On Android TV / Google TV
1. Enable **Developer Options**:
   - Go to Settings > About
   - Tap "Build number" 7 times
2. Enable **Wireless Debugging**:
   - Go to Settings > Developer Options
   - Enable "Wireless debugging"
3. **Pair Device**:
   - Select "Pair device with pairing code"
   - Note the IP address and pairing code

### On Android Phone
1. Install ControlSphere app
2. Connect to same WiFi network as TV
3. Scan for devices or enter IP manually
4. Complete pairing process
5. **Optional**: Add Gemini API key for voice control

### Gemini API Setup (for Voice Control)
1. Get API key from [Google AI Studio](https://aistudio.google.com/)
2. Choose your preferred model:
   - **Gemini 2.5 Flash**: Fast, efficient (recommended)
   - **Gemini 2.5 Pro**: Advanced reasoning for complex commands
   - **Gemini 2.5 Flash Native Audio**: Real-time conversation (Live API)
   - **Gemini 2.5 Computer Use**: Visual UI automation (experimental)
3. In ControlSphere, go to Voice Control screen
4. Tap Settings icon and enter your API key
5. Your API key is stored securely on your device
6. Enable advanced features in settings for optimal experience

## Usage Guide

### Connecting to TV
1. Open ControlSphere app
2. Tap "Scan for Devices" to auto-discover
3. Or use "Manual Connection" to enter IP
4. Follow pairing instructions
5. Start controlling your TV

### Remote Control
- Use D-Pad for navigation
- Media buttons for playback control
- Volume buttons for audio control
- Action buttons for system functions

### App Management
1. Tap Apps button on remote
2. Browse installed apps
3. Tap Launch to open any app
4. Use favorites for quick access

### Search & Text
1. Use Search button for global search
2. Text Input for typing
3. **Voice Control**: Tap microphone button and speak commands naturally

### Voice Commands
- **App Launch**: "Open Netflix", "Launch YouTube"
- **Search**: "Search for action movies", "Find comedies"
- **Media Control**: "Play", "Pause", "Next", "Previous"
- **Navigation**: "Go home", "Go back", "Select", "Scroll up"
- **Volume**: "Volume up", "Volume down", "Mute"
- **Power**: "Turn off TV"

### Audio Analysis Features
- **Speech Transcription**: Convert audio to text with timestamps
- **Speaker Diarization**: Identify and track different speakers
- **Emotion Detection**: Analyze emotional content in speech
- **Multi-Language Support**: 50+ languages with automatic detection
- **Translation**: Automatic translation to English
- **Segment Analysis**: Transcribe specific time segments
- **Content Analysis**: General, emotion, content, speaker, and music analysis
- **Audio Understanding**: Describe, summarize, and answer questions about audio

### Text-to-Speech Features
- **30 Voice Options**: Zephyr, Puck, Charon, Kore, Fenrir, Leda, Orus, Aoede, Callirrhoe, Autonoe, Enceladus, Iapetus, Umbriel, Algieba, Despina, Erinome, Algenib, Rasalgethi, Laomedeia, Achernar, Alnilam, Schedar, Gacrux, Pulcherrima, Achird, Zubenelgenubi, Vindemiatrix, Sadachbia, Sadaltager, Sulafat
- **Controllable TTS**: Natural language prompts for style, tone, accent, and pace control
- **Single-Speaker**: High-fidelity speech with individual voice selection
- **Multi-Speaker**: Conversational TTS with up to 2 speakers
- **Style Prompts**: "Say cheerfully:", "Say professionally:", "Say with excitement:" etc.
- **Language Support**: 50+ languages with automatic detection
- **Fine Control**: Precise control over voice characteristics and delivery
- **Context-Aware Responses**: Automatic TTS feedback for voice commands
- **System Notifications**: Audio feedback for success, error, warning, and info events

### Audio Analysis Examples

#### Speech Transcription with Emotion Detection
```
Audio: Recorded conversation
Features: Timestamps, speaker diarization, emotion detection
Output: JSON with segments, speakers, emotions, and timeline
```

#### Speaker Identification
```
Input: Multi-speaker audio file
Analysis: Identify distinct speakers and their characteristics
Output: Speaker profiles with speaking percentages and roles
```

#### Content Analysis
```
Audio Type: Podcast episode
Analysis: General content summary and key topics
Output: Structured summary with main points and themes
```

#### Music Analysis
```
Audio Type: Music track
Analysis: Genre, instruments, style, and musical elements
Output: Detailed music analysis with technical description
```

### TTS Generation Examples

#### Single Speaker with Style Control
```
Text: "Welcome to ControlSphere!"
Voice: Puck (Upbeat)
Style Prompt: "Say cheerfully:"
Result: Enthusiastic, energetic greeting
```

#### Multi-Speaker Conversation
```
Conversation:
Alex: "Hi! How can I help you today?"
Sam: "I'd like to launch Netflix please."

Speaker Configs:
- Alex → Aoede (Breezy)
- Sam → Kore (Firm)

Result: Natural conversation with distinct voices
```

#### Style Prompt Examples
- **Friendly**: "Say cheerfully:" or "Say as a friendly assistant:"
- **Professional**: "Say professionally:" or "Say as a news anchor:"
- **Excited**: "Say with excitement:" or "Say enthusiastically:"
- **Calm**: "Say calmly:" or "Say soothingly:"
- **Character**: "Say as a radio DJ:" or "Say as a movie announcer:"

### Computer Use & Visual Automation
- **Screen Analysis**: AI-powered visual understanding of TV interface
- **Smart Navigation**: Navigate to UI elements with visual reasoning
- **Visual Click**: Click on specific elements identified by AI
- **Complex Task Automation**: Multi-step tasks with visual verification
- **UI Element Detection**: Identify buttons, text, icons, and interactive elements
- **Visual Search**: Find and interact with search functionality
- **Error Detection**: Identify and handle error dialogs and messages

## Development

### Project Structure
```
app/
├── src/main/java/com/controlsphere/tvremote/
│   ├── data/
│   │   ├── adb/           # ADB connection logic
│   │   ├── discovery/     # Device discovery
│   │   ├── repository/    # Data repositories
│   │   ├── security/      # Security & encryption
│   │   └── voice/         # Voice control & Gemini AI
│   ├── di/               # Dependency injection
│   ├── domain/
│   │   └── model/         # Domain models
│   └── presentation/
│       ├── navigation/   # Navigation
│       ├── screens/      # UI screens
│       └── ui/theme/     # Theme & styling
```

### Building

```bash
./gradlew assembleDebug
./gradlew installDebug
```

### Google GenAI SDK Migration Status

### ✅ **Migration Completed Successfully**

ControlSphere has been fully migrated to the **official Google GenAI SDK 1.0.0**, ensuring production-ready access to all Gemini API capabilities.

### Migration Summary

| Component | Old Library | New Library | Status |
|---|---|---|---|
| **Core Gemini Services** | `com.google.ai.client.generativeai:generativeai:0.1.2` | `com.google.genai:google-genai:1.0.0` | ✅ Migrated |
| **Voice Recognition** | Legacy SDK | Official SDK | ✅ Working |
| **Live API** | Not Supported | ✅ Available |
| **Computer Use** | Not Supported | ✅ Available |
| **TTS** | Not Supported | ✅ Available |
| **All Models** | Limited | ✅ Full Access |

### Benefits Achieved

#### **Production Ready**
- **GA Status**: General Availability with full support
- **Stable API**: No breaking changes expected
- **Official Support**: Backed by Google engineering
- **Performance**: Optimized for production workloads

#### **Feature Access**
- **Latest Models**: Gemini 2.5 Flash, Pro, Computer Use
- **Live API**: Real-time audio streaming
- **TTS**: High-fidelity speech synthesis
- **Computer Use**: Visual UI automation
- **Future Proof**: Immediate access to new capabilities

#### **Technical Benefits**
- **Optimized**: Better performance and reduced latency
- **Reliable**: Consistent behavior across versions
- **Maintained**: Regular updates and security patches
- **Compliant**: Follows Google's best practices

### Legacy Library Deprecation

The legacy libraries are deprecated as of **November 30th, 2025** and are no longer actively maintained. ControlSphere's migration ensures:

- **No Disruption**: All features continue working
- **Enhanced Performance**: Improved speed and reliability
- **Future Ready**: Access to latest Gemini features
- **Compliance**: Uses officially supported libraries

### Platform-Specific Notes

#### **Android Development**
ControlSphere was already optimally positioned:
- **No Legacy Library**: No Google-provided Java SDK existed
- **Direct Migration**: Started with official SDK from the beginning
- **Firebase AI Logic**: Alternative for Dart/Flutter platforms

#### **Cross-Platform**
For other platforms, ControlSphere provides guidance:
- **Python**: Use `pip install google-genai`
- **JavaScript**: Use `npm install @google/genai`
- **Go**: Use `go get google.golang.org/genai`
- **Flutter**: Use Firebase AI Logic

### Migration Verification

All Gemini-powered features verified working:
- ✅ Voice recognition and command processing
- ✅ Live API real-time conversation
- ✅ Computer use visual automation
- ✅ TTS speech synthesis
- ✅ All Gemini 2.5 models accessible

### Support Resources

- **Official Documentation**: [Google GenAI SDK](https://ai.google.dev/gemini-api/docs/libraries)
- **Migration Guide**: [Migrate to GenAI SDK](https://ai.google.dev/gemini-api/docs/migrate)
- **Quickstart**: [Gemini API Quickstart](https://ai.google.dev/gemini-api/docs/quickstart)
- **Community**: [GitHub Repositories](https://github.com/googleapis/) for each language

### Migration Timeline

- **Completed**: February 2025 - Full migration to Google GenAI SDK 1.0.0
- **Status**: Production ready with GA libraries
- **Future**: Immediate access to new Gemini features as they become available

## Dependencies
- Android SDK 26+ (Android 8.0+)
- Android TV 9+ / Google TV
- Local WiFi network
- Developer mode enabled on TV
- **Gemini API key (optional, for voice control)**
- **Google GenAI SDK 1.0.0** (Official Gemini API library)
- **MediaProjection permission** (for computer use features)

## Security & Privacy

- **Local Only**: All communication stays on your local network
- **Encrypted**: Device keys and preferences encrypted with Android Keystore
- **No Tracking**: No analytics or data collection
- **Open Source**: Transparent codebase you can audit

## Compatibility

### Supported Devices
- Chromecast with Google TV
- Sony Android TVs (2019+)
- TCL Android TVs (2019+)
- Nvidia Shield TV
- Other Android TV 9+ devices

### Requirements
- Android 8.0+ on controller device
- Android TV 9+ / Google TV on target device
- Same WiFi network for both devices
- Developer mode enabled on TV

## Troubleshooting

### Connection Issues
- Verify both devices on same WiFi network
- Confirm wireless debugging enabled on TV
- Check firewall settings
- Try manual IP connection

### Permission Issues
- Grant microphone permission for voice input
- Allow network access during setup

### Voice Control Issues
- Verify Gemini API key is valid and has sufficient quota
- Check microphone permission is granted
- Ensure quiet internet connection for API calls
- Try speaking clearly and naturally
- For computer use features, ensure MediaProjection permission is granted
- Verify screen capture is working for visual automation

### Computer Use Issues
- Ensure MediaProjection permission is granted in Android settings
- Verify screen capture is working in Visual Automation settings
- Check that computer use feature is enabled in Voice Config
- Try reducing screen resolution if capture is slow
- Ensure TV supports MediaProjection API (Android 5.0+)

### Performance
- Use 5GHz WiFi for better performance
- Restart app if connection becomes unstable
- Check TV's wireless debugging status

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## Support

For issues and feature requests, please use the GitHub issue tracker.
