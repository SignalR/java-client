# ASP.NET SignalR for Java and Android
ASP.NET SignalR is a new library for ASP.NET developers that makes it incredibly simple to add real-time web functionality to your applications. What is "real-time web" functionality? It's the ability to have your server-side code push content to the connected clients as it happens, in real-time.

## What can it be used for?
Pushing data from the server to the client (not just browser clients) has always been a tough problem. SignalR makes 
it dead easy and handles all the heavy lifting for you.

This library can be used from both regular Java or Android applications.

## Documentation
See the [documentation](http://asp.net/signalr)
	
## LICENSE
[Apache 2.0 License](https://github.com/SignalR/SignalR/blob/master/LICENSE.md)

## Contributing

See the [contribution  guidelines](https://github.com/SignalR/SignalR/blob/master/CONTRIBUTING.md)

## Building the source

```
git clone git@github.com:SignalR/java-client.git (or https if you use https)
```

Open Android Studio, click "Import Non-Android Studio Project" and select the cloned directory 

Build the project.

The signalr-client-sdk.jar will be generated inside the /signalr-client-sdk/build/libs folder

The signalr-client-sdk-android.aar will be generated inside the /signalr-client-sdk-android/build/outputs/aar folder

## Running the tests:
	
Run the signalr-client-tests project as a JUnit test.

## Using the library in a Java application:

Add the signalr-client-sdk.jar and gson library gradle dependencyto the project.

Gradle file sample
```
repositories {
    jcenter()
    maven { url 'http://clojars.org/repo' }
}

dependencies {
    compile fileTree(dir: 'libs', include: ['*.jar'])
    compile ':signalr-client-sdk-android:1.0@aar'
    compile 'com.google.code.gson:gson:2.3'
    compile 'org.java-websocket:java-websocket:1.3.1'
}
```

## Using the library in an Android application:

Add the signalr-client-sdk.jar, signalr-client-sdk-android.jar gson library as a gradle dependency to the project.

In the code, before using the library, initialize the platform to use android-specific libraries and compatibility with older Android versions:
	- Platform.loadPlatformComponent(new AndroidPlatformComponent());


## Questions?
The SignalR team hangs out in the [signalr](http://jabbr.net/#/rooms/signalr) room at on [JabbR](http://jabbr.net/).
