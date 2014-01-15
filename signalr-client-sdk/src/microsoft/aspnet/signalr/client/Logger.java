package microsoft.aspnet.signalr.client;

/**
 * Interface to define a Logger
 */
public interface Logger {
	/**
	 * Logs a message
	 * @param message Message to log
	 * @param level Message level
	 */
	public void log(String message, LogLevel level);
}
