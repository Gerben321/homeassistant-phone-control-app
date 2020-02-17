package com.gerbenbol.hassphonecontrol

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class MyFirebaseMessagingService : FirebaseMessagingService() {

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    override fun onNewToken(token: String?) {
        //Log.d('log', "Refreshed token: " + token);
        //String bla = token;

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        //sendRegistrationToServer(token);
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Check if there's data in the message
        if (remoteMessage.data.size > 0) {
            val data = remoteMessage.data

            val jsonactions = data["actions"]

            try {
                val jsonObj = JSONArray(jsonactions)

                for (i in 0 until jsonObj.length()) {
                    val row = jsonObj.getJSONObject(i)

                    val action = row.getString("action")

                    // Disable wifi
                    if (action == "disable-wifi") {

                        val handler = Handler(Looper.getMainLooper())
                        handler.post {
                            Toast.makeText(applicationContext, "Disable wifi", Toast.LENGTH_LONG).show()

                            val wifi = applicationContext.getSystemService(applicationContext.WIFI_SERVICE) as WifiManager

                            wifi.isWifiEnabled = false
                        }
                    }

                    // Enable wifi
                    if (action == "enable-wifi") {
                        val handler = Handler(Looper.getMainLooper())
                        handler.post {
                            Toast.makeText(applicationContext, "Enable wifi", Toast.LENGTH_LONG).show()

                            val wifi = applicationContext.getSystemService(applicationContext.WIFI_SERVICE) as WifiManager

                            wifi.isWifiEnabled = true
                        }
                    }

                    // Toggle wifi
                    if (action == "toggle-wifi") {
                        val handler = Handler(Looper.getMainLooper())
                        handler.post {
                            Toast.makeText(applicationContext, "Toggle wifi", Toast.LENGTH_LONG).show()

                            val wifi = applicationContext.getSystemService(applicationContext.WIFI_SERVICE) as WifiManager

                            val wifiEnabled = wifi.isWifiEnabled

                            wifi.isWifiEnabled = !wifiEnabled
                        }
                    }

                    // Disable BT
                    if (action == "disable-bluetooth") {
                        val handler = Handler(Looper.getMainLooper())
                        handler.post { Toast.makeText(applicationContext, "Disable BT", Toast.LENGTH_LONG).show() }

                        val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

                        mBluetoothAdapter?.disable()
                    }

                    // Enable BT
                    if (action == "enable-bluetooth") {
                        val handler = Handler(Looper.getMainLooper())
                        handler.post { Toast.makeText(applicationContext, "Enable BT", Toast.LENGTH_LONG).show() }

                        val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

                        mBluetoothAdapter?.enable()
                    }

                    // Toggle BT
                    if (action == "toggle-bluetooth") {
                        val handler = Handler(Looper.getMainLooper())
                        handler.post { Toast.makeText(applicationContext, "Toggle BT", Toast.LENGTH_LONG).show() }

                        val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

                        if (mBluetoothAdapter != null) {
                            if (mBluetoothAdapter.isEnabled) {
                                mBluetoothAdapter.disable()
                            } else {
                                mBluetoothAdapter.enable()
                            }
                        }
                    }

                    // Toggle BT
                    if (action.startsWith("set-brightness")) {
                        val other = action.replace("set-brightness", "")

                        if (other != "") {
                            val brightness = other.substring(other.indexOf("(") + 1, other.indexOf(")"))
                            val test = brightness
                            var brightnessVal = Integer.parseInt(brightness)

                            // Calculate value
                            if (brightnessVal > 0) {
                                brightnessVal = 255 / (100 / brightnessVal)
                            }

                            val handler = Handler(Looper.getMainLooper())
                            handler.post { Toast.makeText(applicationContext, "Set brightness", Toast.LENGTH_LONG).show() }

                            if (!Settings.System.canWrite(baseContext)) {
                                val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                                intent.data = Uri.parse("package:" + baseContext.packageName)
                                baseContext.startActivity(intent)
                            } else {
                                // Set screen brightness
                                android.provider.Settings.System.putInt(contentResolver, android.provider.Settings.System.SCREEN_BRIGHTNESS, brightnessVal)
                            }
                        }
                    }
                }
            } catch (e: JSONException) {
                val handler = Handler(Looper.getMainLooper())

                handler.post { Toast.makeText(applicationContext, "Couldn't convert actions", Toast.LENGTH_LONG).show() }
            }

        } else {
            val handler = Handler(Looper.getMainLooper())

            handler.post { Toast.makeText(applicationContext, "No data found in message", Toast.LENGTH_LONG).show() }
        }
    }

    companion object {

        private val TAG = "MainActivity"
    }
}
