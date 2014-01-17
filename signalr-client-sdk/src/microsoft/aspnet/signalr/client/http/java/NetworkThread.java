package microsoft.aspnet.signalr.client.http.java;

/**
 * A thread that can release resources when stopped
 */
abstract class NetworkThread extends Thread {

    /**
     * Initializes the NetworkThread
     * 
     * @param target
     *            runnable to execute
     */
    public NetworkThread(Runnable target) {
        super(target);
    }

    /**
     * Releases resources and stops the thread
     */
    abstract void releaseAndStop();
}
