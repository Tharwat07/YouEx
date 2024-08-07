### Description

This Android application demonstrates the use of a floating window service that overlays a WebView on top of other apps. The app includes the following key features:

1. **MainActivity**:
   - **Overlay Permission**: Requests the user for permission to draw overlays if not already granted.
   - **WebView Integration**: Loads a WebView that displays YouTube and blocks ads using a custom `WebViewClient`.
   - **Floating Action Button**: Starts the `FloatingWindowService` when clicked, which moves the MainActivity to the background.

2. **FloatingWindowService**:
   - **Foreground Service**: Runs as a foreground service to ensure it is not killed by the system.
   - **Floating Window**: Displays a floating window with a WebView and control buttons.
   - **Movable Window**: Allows the user to move the floating window around the screen.
   - **Full-Screen Button**: Opens the MainActivity in full-screen mode and stops the floating window service.
   - **Close Button**: Stops the floating window service.

3. **AdBlockWebViewClient**:
   - **Ad Blocking**: Intercepts web requests to block ads and hides ad elements on the page.
   - **Logo Replacement**: Hides the YouTube logo and replaces it with a custom image.

### Key Components

- **MainActivity**: Handles the main user interface and starts the floating window service.
- **FloatingWindowService**: Manages the floating window overlay.
- **WebViewSingleton**: Provides a singleton instance of the WebView to be used across the app.
- **AdBlockWebViewClient**: Custom WebViewClient to block ads and modify the web page content.

### Usage

1. **Permissions**: Ensure the app has the necessary permissions to draw overlays.
2. **Floating Window**: Click the floating action button to start the floating window service.
3. **Move Window**: Long press and drag the floating window to move it around the screen.
4. **Full-Screen Mode**: Click the full-screen button to open the MainActivity in full-screen mode.
5. **Close Window**: Click the close button to stop the floating window service.

### Code Structure

- `MainActivity.kt`: Contains the main activity code.
- `FloatingWindowService.kt`: Contains the floating window service code.
- `WebViewSingleton.kt`: Contains the singleton WebView provider.
- `AdBlockWebViewClient.kt`: Contains the custom WebViewClient for ad blocking.

### Dependencies

- **AndroidX**: For modern Android development.
- **Material Components**: For UI components like FloatingActionButton.
- **WebView**: For displaying web content.

This app serves as a practical example of using foreground services, floating windows, and WebView customization in Android.
