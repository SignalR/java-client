package microsoft.aspnet.signalr.client.transport;

/**
 * Callback for data result operations
 */
public interface DataResultCallback {

    /**
     * Callback invoked when there is new data from the server
     * 
     * @param data
     *            data
     */
    public void onData(String data);
}
