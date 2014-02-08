package microsoft.aspnet.signalr.client;

/***
 * Represents the state of a connection
 */
public enum ConnectionState {
    Connecting, Connected, Reconnecting, Disconnected
}