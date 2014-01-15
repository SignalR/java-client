package microsoft.aspnet.signalr.client.tests.util;

import microsoft.aspnet.signalr.client.LogLevel;
import microsoft.aspnet.signalr.client.Logger;

public class ConsoleLogger implements Logger {

	@Override
	public void log(String message, LogLevel level) {
		System.out.println(level.toString() + " - " + message);
	}

}
