# Changelog

All notable changes to this project will be documented in this file.

## [0.3.0] - 2026-03-01

### Added
- **True Gemini Live API Integration**: Upgraded the `google-genai` SDK from `1.0.0` to `1.41.0` and implemented the official Gemini Live API for real-time bidirectional streaming.
- **Background Screen Capture for AI Vision**: TV Receiver now captures screenshots natively via device pipelines and sends them via WebSocket to Gemini dynamically at 1 FPS to give the AI real-time visual context.
- **Live PCM Audio Streaming**: Switched from recording full files to continuously streaming 16kHz PCM audio data straight from `AudioRecord` to the Gemini Live WebSockets for ultra-fast conversational latency.
- **TV Receiver Command History**: Added a clean Compose dashboard on the TV app for displaying real-time command logs (`TVReceiverScreen`), including visual status indicators.
- **Centralized Voice Config**: Refactored the codebase to use `VoiceConfig.kt` to manage model availability, enforcing Gemini 2.5 Flash constraints across all services.
- **Enhanced Navigation Flow**: Improved device pairing navigation with automatic redirection to remote screen upon successful connection, including authorization checks.
- **Connection Status Debugging**: Added comprehensive logging and visual indicators for WiFi connection status and troubleshooting.
- **Production Build Optimization**: Cleaned up build warnings and optimized release configuration for production deployment.

### Fixed
- **App Connectivity Isolation**: Fixed a long-standing issue where WiFi and ADB ports would conflict. Handled with cleaner separation in `DeviceRepository.kt`.
- **Kotlin Type Inference with SDK 1.41.0**: Solved complex Kotlin compiler errors relating to `java.util.Optional` unpacking from the deeply nested Java Live API types.
- **Deprecated Models**: Replaced usages of `gemini-3-flash-preview` and legacy versions with `gemini-2.5-flash-native-audio-preview-12-2025` and `gemini-2.5-flash-preview-tts`.
- **Target SDK Updates**: Incremented App Version Code to `3` and Version Name to `0.3.0` in `build.gradle`.
- **Navigation Authorization Check**: Fixed navigation to remote screen to wait for both connection and authorization before redirecting.
- **Connection Status Flow**: Resolved issues with connection status not properly updating UI state during WiFi connections.

### Technical Improvements
- **WebSocket Integration**: Direct WebSocket connections to Gemini Live API for real-time communication.
- **Audio Pipeline Optimization**: Real-time PCM audio streaming without intermediate file storage.
- **Screen Capture Pipeline**: Native Android screen capture with JPEG compression for AI visual context.
- **Error Handling**: Enhanced error recovery and connection retry logic for WiFi connections.
- **Memory Management**: Optimized memory usage for continuous audio and video streaming.
- **Thread Safety**: Improved coroutine usage and thread management for background services.

## [0.2.0] - 2024-11

### Added
- Initial creation of TV Receiver Manager via standard Socket connections.
- Mocked/Simulated baseline for Voice AI implementations setup.
- Advanced adb connection configurations.
