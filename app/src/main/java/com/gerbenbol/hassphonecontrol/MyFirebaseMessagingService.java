package com.gerbenbol.hassphonecontrol;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
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

                    // Toggle wifi
                    if(action.equals("toggle-wifi")) {
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Toggle wifi", Toast.LENGTH_LONG).show();

                                WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(getApplicationContext().WIFI_SERVICE);

                                boolean wifiEnabled = wifi.isWifiEnabled();

                                wifi.setWifiEnabled(!wifiEnabled);
                            }
                        });
                    }

                    // Disable BT
                    if(action.equals("disable-bluetooth")) {
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                                         public void run() {
                                             Toast.makeText(getApplicationContext(), "Disable BT", Toast.LENGTH_LONG).show();
                                         }
                                     });

                        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

                        if(mBluetoothAdapter != null) {
                            mBluetoothAdapter.disable();
                        }
                    }

                    // Enable BT
                    if(action.equals("enable-bluetooth")) {
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Enable BT", Toast.LENGTH_LONG).show();
                            }
                        });

                        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

                        if(mBluetoothAdapter != null) {
                            mBluetoothAdapter.enable();
                        }
                    }

                    // Toggle BT
                    if(action.equals("toggle-bluetooth")) {
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Toggle BT", Toast.LENGTH_LONG).show();
                            }
                        });

                        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

                        if(mBluetoothAdapter != null) {
                            if (mBluetoothAdapter.isEnabled()) {
                                mBluetoothAdapter.disable();
                            } else {
                                mBluetoothAdapter.enable();
                            }
                        }
                    }

                    // Toggle BT
                    if(action.startsWith("set-brightness")) {
                        String other = action.replace("set-brightness", "");

                        if(!other.equals("")) {
                            String brightness = other.substring(other.indexOf("(") + 1, other.indexOf(")"));
                            String test = brightness;
                            int brightnessVal = Integer.parseInt(brightness);

                            // Calculate value
                            if(brightnessVal > 0) {
                                brightnessVal = 255 / (100 / brightnessVal);
                            }

                            Handler handler = new Handler(Looper.getMainLooper());
                            handler.post(new Runnable() {
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "Set brightness", Toast.LENGTH_LONG).show();
                                }
                            });

                            if (!Settings.System.canWrite(getBaseContext())) {
                                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                                intent.setData(Uri.parse("package:" + getBaseContext().getPackageName()));
                                getBaseContext().startActivity(intent);
                            } else {
                                // Set screen brightness
                                android.provider.Settings.System.putInt(getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS, brightnessVal);
                            }
                        }
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
