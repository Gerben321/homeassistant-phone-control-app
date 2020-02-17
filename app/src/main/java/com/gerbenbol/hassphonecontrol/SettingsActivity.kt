package com.gerbenbol.hassphonecontrol

import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

import com.android.volley.AuthFailureError
import com.android.volley.NetworkResponse
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.iid.FirebaseInstanceId

import org.json.JSONObject

import java.io.IOException
import java.util.HashMap

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        val sendButton = findViewById<Button>(R.id.send)
        val saveButton = findViewById<Button>(R.id.save)

        val uriField = findViewById<EditText>(R.id.hassUri)
        val hassTokenField = findViewById<EditText>(R.id.hassToken)
        val senderField = findViewById<EditText>(R.id.senderId)

        // Settings
        val settingsPref = applicationContext.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE)

        // Load URI
        val loadedUri = settingsPref.getString(getString(R.string.setting_uri), null)
        uriField.setText(loadedUri)

        // Load HASS token
        val loadedToken = settingsPref.getString(getString(R.string.setting_token), null)
        hassTokenField.setText(loadedToken)

        // Load FCM Sender ID
        val loadedSender = settingsPref.getString(getString(R.string.setting_sender), null)
        senderField.setText(loadedSender)

        // Save data
        saveButton.setOnClickListener {
            val editor = settingsPref.edit()

            // Save URI
            editor.putString(getString(R.string.setting_uri), uriField.text.toString())

            // Save token
            editor.putString(getString(R.string.setting_token), hassTokenField.text.toString())

            // Save Sender ID
            editor.putString(getString(R.string.setting_sender), senderField.text.toString())

            editor.commit()

            notifyUser("", "Settings saved")
        }

        // Send request
        sendButton.setOnClickListener {
            var uriValue = uriField.text.toString()
            uriValue += "/api/notify.hass-control"

            val finalUri = uriValue

            Thread(Runnable {
                try {
                    val tokenValue = FirebaseInstanceId.getInstance().getToken(senderField.text.toString(), "FCM")

                    val result = sendToken(finalUri, tokenValue, hassTokenField.text.toString())

                    val handler = Handler(Looper.getMainLooper())
                    handler.post {
                        if (result) {
                            Toast.makeText(applicationContext, "Device registered", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(applicationContext, "Error while registering", Toast.LENGTH_LONG).show()
                        }
                    }
                } catch (e: IOException) {
                    val test = e.message
                }
            }).start()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun notifyUser(title: String, text: String) {
        Toast.makeText(applicationContext, text, Toast.LENGTH_LONG).show()
    }

    private fun sendToken(url: String, token: String, hassToken: String): Boolean {
        var mRequestBodyTemp: String? = null

        try {
            val dataJson = JSONObject()
            dataJson.put("token", token)
            // Convert the payload to the data that will be sent (entity_id etc)
            mRequestBodyTemp = dataJson.toString()
        } catch (e: Exception) {
            //notifyUser(getResources().getString(R.string.errorTitle), getString(R.string.payload_read_error));
            return false
        }

        if (mRequestBodyTemp == null) {
            //notifyUser(getResources().getString(R.string.errorTitle), getString(R.string.nothing_to_send));
            return false
        }

        val mRequestBody = mRequestBodyTemp

        val requstQueue = Volley.newRequestQueue(baseContext)

        val stringRequest = object : StringRequest(Request.Method.POST, url, Response.Listener {
            //notifyUser(getString(R.string.success), getString(R.string.sent));
            //notifyUser("Success", "Network error");
        }, Response.ErrorListener { notifyUser("Error", "Network error") }) {
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()

                headers["Authorization"] = "Bearer $hassToken"

                return headers
            }

            @Throws(AuthFailureError::class)
            override fun getBody(): ByteArray? {
                try {
                    return mRequestBody?.toByteArray(charset("utf-8"))
                } catch (uee: Exception) {
                    //VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", mRequestBody, "utf-8");
                    return null
                }

            }

            override fun parseNetworkResponse(response: NetworkResponse): Response<String> {
                var responseString = ""
                if (response != null) {
                    responseString = response.statusCode.toString()
                }
                return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response))
            }
        }

        requstQueue.add(stringRequest)

        return true
    }
}
