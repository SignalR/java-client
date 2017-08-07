# Usage
Singnalr java-client library is added as dependency to dca.android project. Libs folder contains files: signalr-client-sdk.jar and signalr-client-sdk-android-release.aar, and java-websocket-1.3.1.jar.

# Changes
 
Patch 64 on the main repo is added to this forked repo to fix problems with websockets. 
Added fix for fallback for WebsocketTransport (commit: 10ec1c316b6bc153bfc74ab59a0aa758beef8a95)
Added fix for null pointer Raygun issue (commit: c71e6a4231a3519e75bf62690ac5ee75c1c467e8)
Added fix for ConcurrentModificationException at SignalRFuture (commit acced8f4e803746e9aaf358f5a00c7fc3eadbef7)
Upadated buildToolsVersion and gradle version (commit acced8f4e803746e9aaf358f5a00c7fc3eadbef7)