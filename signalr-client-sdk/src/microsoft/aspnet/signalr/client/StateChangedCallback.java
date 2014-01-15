package microsoft.aspnet.signalr.client;

/**
 * Callback invoked when a connection changes its state
 */
public interface StateChangedCallback {
	public void stateChanged(ConnectionState oldState, ConnectionState newState);
}
