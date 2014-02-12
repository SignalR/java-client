package microsoft.aspnet.signalr.client.android.test.integration;

import java.util.concurrent.Future;

import microsoft.aspnet.signalr.client.Connection;
import microsoft.aspnet.signalr.client.LogLevel;
import microsoft.aspnet.signalr.client.Logger;
import microsoft.aspnet.signalr.client.NullLogger;
import microsoft.aspnet.signalr.client.Platform;
import microsoft.aspnet.signalr.client.SignalRFuture;
import microsoft.aspnet.signalr.client.http.android.AndroidPlatformComponent;
import microsoft.aspnet.signalr.client.hubs.HubConnection;
import microsoft.aspnet.signalr.client.transport.AutomaticTransport;
import microsoft.aspnet.signalr.client.transport.ClientTransport;
import microsoft.aspnet.signalr.client.transport.LongPollingTransport;
import microsoft.aspnet.signalr.client.transport.ServerSentEventsTransport;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.PreferenceManager;
import android.util.Log;

public class ApplicationContext {
	private static Activity mActivity;

	public static Context getActivity() {
		return mActivity;
	}

	public static void setContext(Activity activity) {
	    mActivity = activity;
	    Platform.loadPlatformComponent(new AndroidPlatformComponent());
	}
	
	public static Connection createConnection() {
	    return null;
	}
	
	public static void wait(int seconds) throws InterruptedException {
	    Thread.sleep(seconds * 1000);
	}
	
	public static HubConnection createHubConnection() {
		String url = getServerUrl();
		
		HubConnection connection = new HubConnection(url,"", true, new Logger() {
            
            @Override
            public void log(String message, LogLevel level) {
                Log.d("SIGNALRDATA", message);
            }
        });
		
		return connection;
	}
	
	public static HubConnection createHubConnectionWithInvalidURL() {
        String url = "http://signalr.net/fake";
        
        HubConnection connection = new HubConnection(url,"", true, new Logger() {
            
            @Override
            public void log(String message, LogLevel level) {
                Log.d("SIGNALRDATA", message);
            }
        });
        
        return connection;
    }

	public static String getServerUrl() {
		return PreferenceManager.getDefaultSharedPreferences(mActivity).getString(Constants.PREFERENCE_SIGNALR_URL, "");
	}

	public static String getLogPostURL() {
		return PreferenceManager.getDefaultSharedPreferences(mActivity).getString(Constants.PREFERENCE_LOG_POST_URL, "");
	}
	

    public static ClientTransport createTransport(TransportType transportType) {
        switch (transportType) {
        case Auto:
            return new AutomaticTransport();

        case LongPolling:
            return new LongPollingTransport(new NullLogger());
            
        case ServerSentEvents:
            return new ServerSentEventsTransport(new NullLogger());
        default:
            return null;
        }
    }

    public static Future<Void> showMessage(final String message) {
        final SignalRFuture<Void> result = new SignalRFuture<Void>();
        
        mActivity.runOnUiThread(new Runnable() {
            
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                
                builder.setTitle("Message");
                builder.setMessage(message);
                builder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                    
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        result.setResult(null);
                    }
                });
                
                builder.create().show();

            }
        });
        
        return result;
    }
}
