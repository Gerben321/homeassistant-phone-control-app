package com.gerbenbol.hassphonecontrol;

import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MainActivity";

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(String token) {
        //Log.d('log', "Refreshed token: " + token);
        //String bla = token;

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        //sendRegistrationToServer(token);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Check if there's data in the message
        if (remoteMessage.getData().size() > 0) {
            Map<String, String> data = remoteMessage.getData();

            String jsonactions = data.get("actions");

            try {
                JSONArray jsonObj = new JSONArray(jsonactions);

                for (int i = 0; i < jsonObj.length(); i++) {
                    JSONObject row = jsonObj.getJSONObject(i);

                    String action = row.getString("action");

                    // Disable wifi
                    if(action.equals("disable-wifi")) {

                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Disable wifi", Toast.LENGTH_LONG).show();

                                WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(getApplicationContext().WIFI_SERVICE);

                                wifi.setWifiEnabled(false);
                            }
                        });
                    }

                    // Enable wifi
                    if(action.equals("enable-wifi")) {
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Enable wifi", Toast.LENGTH_LONG).show();

                                WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(getApplicationContext().WIFI_SERVICE);

                                wifi.setWifiEnabled(true);
                            }
                        });
                    }
                }
            }
            catch (JSONException e)
            {
                Handler handler = new Handler(Looper.getMainLooper());

                handler.post(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Couldn't convert actions", Toast.LENGTH_LONG).show();
                    }
                });
            }
        } else {
            Handler handler = new Handler(Looper.getMainLooper());

            handler.post(new Runnable() {
                public void run() {
                    Toast.makeText(getApplicationContext(), "No data found in message", Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}
