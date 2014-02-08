package microsoft.aspnet.signalr.client;

public interface ErrorCallback {
    /**
     * Callback invoked when an error is found
     * 
     * @param error
     *            The error
     */
    public void onError(Throwable error);
}
