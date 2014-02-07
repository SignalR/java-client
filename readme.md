How to build the sources:

	- Download the gson-2.2.2.jar library inside /signalr-client-sdk/libs/ (this can be downloaded using the getLibs.ps or getLibs.sh script)

	- Import the following project into Eclipse workspace as Java projects:
		- signalr-client-sdk
		- signalr-client-tests

	- Import the following project into Eclipse workspace as an Android project (requires ADT with Android 4.4.2 SDK):
		- signalr-client-sdk-android

	- Build the workspace.

	- The signalr-client-sdk-android.jar will be generated inside the /signalr-client-sdk-android/bin folder

	- To generate the signalr-client-sdk JAR file, right click the build.xml file inside signalr-client-sdk and run it as an Ant Build. The signalr-client-sdk.jar will be generated inside the bin folder.


How to run the tests:

	- Run the signalr-client-tests project as a JUnit test.


How to use the library in a Java application:

	- Add the signalr-client-sdk.jar and gson-2.2.2.jar libraries to the project build path.


How to use the library in an Android application:

	- Add the signalr-client-sdk.jar, signalr-client-sdk-android.jar and gson-2.2.2.jar libraries to the project libs folder. Those jar files will be automatically added as android libraries.

	- In the code, before using the library, initialize the platform to use android-specific libraries and compatibility with older Android versions:
		Platform.loadPlatformComponent(new AndroidPlatformComponent());
