# Gemini API Implementation Note

The Google GenAI Java SDK (v1.0.0) does not support `Part.fromInlineData()` for passing binary data directly.

## Correct Approaches:

1. **Files API** (Recommended for audio/images):
   - Upload media files using `client.files.upload()`
   - Reference uploaded files with `Part.fromUri(file.name().get(), file.mimeType().get())`
   - Files are stored for 48 hours

2. **Text-only prompts**:
   - Use `Part.fromText()` for text content
   - Use `client.models.generateContent(modelName, textPrompt, config)` for simple text

## Current Implementation Status:

The code has been updated to use the correct API patterns:
- `Content.fromParts(...)` instead of `Content.builder()`
- `client.models.generateContent(...)` instead of `client.models().generateContent(...)`

However, audio/image processing features require implementing the Files API upload flow before they can work properly.

## Next Steps:

1. Implement Files API upload for audio files in voice services
2. Implement Files API upload for screenshots in computer use services
3. Update all methods that process binary data to use the upload-then-reference pattern
