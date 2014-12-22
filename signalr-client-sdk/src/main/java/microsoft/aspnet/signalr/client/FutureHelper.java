/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
*/

package microsoft.aspnet.signalr.client;

import microsoft.aspnet.signalr.client.http.HttpConnectionFuture;

/**
 * Helper for Future operations
 */
public class FutureHelper {

    /**
     * Copy the Cancellation and Error handlers between two SignalRFuture
     * instances
     * 
     * @param sourceFuture
     *            The source future
     * @param targetFuture
     *            The target future
     */
    public static void copyHandlers(final SignalRFuture<?> sourceFuture, final SignalRFuture<?> targetFuture) {
        targetFuture.onCancelled(new Runnable() {

            @Override
            public void run() {
                sourceFuture.cancel();
            }
        });

        sourceFuture.onError(new ErrorCallback() {

            @Override
            public void onError(Throwable error) {
                targetFuture.triggerError(error);
            }
        });
    }

    /**
     * Copy the Cancellation and Error handlers between two SignalRFuture
     * instances, where the source is an HttpConnectionFuture
     * 
     * @param sourceFuture
     *            The source future
     * @param targetFuture
     *            The target future
     */
    public static void copyHandlers(HttpConnectionFuture sourceFuture, final SignalRFuture<?> targetFuture) {
        copyHandlers((SignalRFuture<?>) sourceFuture, targetFuture);

        sourceFuture.onTimeout(new ErrorCallback() {

            @Override
            public void onError(Throwable error) {
                targetFuture.triggerError(error);
            }
        });
    }
}
