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

Download the gson-2.2.2.jar library inside /signalr-client-sdk/libs/ (this can be downloaded using the getLibs.ps or getLibs.sh script)

Import the following project into Eclipse workspace as Java projects:
	- signalr-client-sdk
	- signalr-client-tests

Import the following project into Eclipse workspace as an Android project (requires ADT with Android 4.4.2 SDK):
	- signalr-client-sdk-android

Build the workspace.

The signalr-client-sdk-android.jar will be generated inside the /signalr-client-sdk-android/bin folder

If you are using Maven, you can generate the signalr-client-sdk JAR file building with Maven using the 'package' goal.
Otherwise, right click the build.xml file inside signalr-client-sdk and run it as an Ant Build. It will package the classes compiled by Eclipse. The signalr-client-sdk.jar will be generated inside the bin folder.


## Running the tests:
	
Run the signalr-client-tests project as a JUnit test.

## Using the library in a Java application:

Add the signalr-client-sdk.jar and gson-2.2.2.jar libraries to the project build path.


## Using the library in an Android application:

Add the signalr-client-sdk.jar, signalr-client-sdk-android.jar and gson-2.2.2.jar libraries to the project libs folder. Those jar files will be automatically added as android libraries.

In the code, before using the library, initialize the platform to use android-specific libraries and compatibility with older Android versions:
	- Platform.loadPlatformComponent(new AndroidPlatformComponent());


## Questions?
The SignalR team hangs out in the [signalr](http://jabbr.net/#/rooms/signalr) room at on [JabbR](http://jabbr.net/).
