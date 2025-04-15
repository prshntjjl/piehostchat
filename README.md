# PieSocket Chat App

A single-screen mobile chat application that supports real-time communication and offline functionality.

## Features

- **Real-time Chat**: Implementation of socket-based communication for instant message updates
- **Offline Mode**: Queue messages when offline and automatically retry when back online
- **Multiple Chat Bots**: Support for multiple chatbot conversations (SupportBot, SalesBot, FAQBot)
- **Message Status**: Shows message delivery status (sent, queued)
- **Network Status**: Visual indicators of network connectivity state
- **Modern UI**: Material Design components and intuitive interface

## Architecture

The app follows MVVM (Model-View-ViewModel) architecture with:

- **Room Database**: For local storage and offline capability
- **WebSocket**: For real-time communication using PieSocket
- **LiveData**: For reactive UI updates
- **ViewModel**: For managing UI-related data
- **Repository**: For handling data operations
- **Coroutines**: For asynchronous tasks

## Development Setup

### Prerequisites
- Android Studio Arctic Fox or newer
- Kotlin 1.5.0 or newer
- Android SDK 21+

### Building the Project
1. Clone the repository
2. Open the project in Android Studio
3. Sync Gradle files
4. Build and run on a physical device or emulator

## Testing

The app has been tested for:
- Real-time message delivery
- Offline message queueing
- Handling network changes
- UI responsiveness

## License

This project is open-source and available under the MIT License.

## Contact

If you have any questions, please contact the developer at [your-email@example.com]. 