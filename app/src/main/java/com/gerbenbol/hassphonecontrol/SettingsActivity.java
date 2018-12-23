package com.gerbenbol.hassphonecontrol;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Button sendButton = findViewById(R.id.send);
        Button saveButton = findViewById(R.id.save);

        final EditText uriField = findViewById(R.id.hassUri);
        final EditText hassTokenField = findViewById(R.id.hassToken);
        final EditText senderField = findViewById(R.id.senderId);

        // Settings
        final SharedPreferences settingsPref = getApplicationContext().getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        // Load URI
        String loadedUri = settingsPref.getString(getString(R.string.setting_uri), null);
        uriField.setText(loadedUri);

        // Load HASS token
        String loadedToken = settingsPref.getString(getString(R.string.setting_token), null);
        hassTokenField.setText(loadedToken);

        // Load FCM Sender ID
        String loadedSender = settingsPref.getString(getString(R.string.setting_sender), null);
        senderField.setText(loadedSender);

        // Save data
        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SharedPreferences.Editor editor = settingsPref.edit();

                // Save URI
                editor.putString(getString(R.string.setting_uri), uriField.getText().toString());

                // Save token
                editor.putString(getString(R.string.setting_token), hassTokenField.getText().toString());

                // Save Sender ID
                editor.putString(getString(R.string.setting_sender), senderField.getText().toString());

                editor.commit();

                notifyUser("", "Settings saved");
            }
        });

        // Send request
        sendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String uriValue = uriField.getText().toString();
                uriValue += "/api/notify.hass-control";

                final String finalUri = uriValue;

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String tokenValue = FirebaseInstanceId.getInstance().getToken(senderField.getText().toString(), "FCM");

                            final boolean result = sendToken(finalUri, tokenValue, hassTokenField.getText().toString());

                            Handler handler = new Handler(Looper.getMainLooper());
                            handler.post(new Runnable() {
                                public void run() {
                                    if(result) {
                                        Toast.makeText(getApplicationContext(), "Device registered", Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(getApplicationContext(), "Error while registering", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        }
                        catch (IOException e)
                        {
                            String test = e.getMessage();
                        }
                    }
                }).start();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

    private void notifyUser(String title, String text)
    {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
    }

    private boolean sendToken(String url, String token, String hassToken)
    {
        String mRequestBodyTemp = null;
        final String savedToken = hassToken;

        try {
            JSONObject dataJson = new JSONObject();
            dataJson.put("token", token);
            // Convert the payload to the data that will be sent (entity_id etc)
            mRequestBodyTemp = dataJson.toString();
        } catch(Exception e) {
            //notifyUser(getResources().getString(R.string.errorTitle), getString(R.string.payload_read_error));
            return false;
        }

        if(mRequestBodyTemp == null) {
            //notifyUser(getResources().getString(R.string.errorTitle), getString(R.string.nothing_to_send));
            return false;
        }

        final String mRequestBody = mRequestBodyTemp;

        RequestQueue requstQueue = Volley.newRequestQueue(getBaseContext());

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //notifyUser(getString(R.string.success), getString(R.string.sent));
                //notifyUser("Success", "Network error");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                notifyUser("Error", "Network error");
            }
        }) {
            @Override
            public Map<String,String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();

                headers.put("Authorization", "Bearer " + savedToken);

                return headers;
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    return mRequestBody == null ? null : mRequestBody.getBytes("utf-8");
                } catch (Exception uee) {
                    //VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", mRequestBody, "utf-8");
                    return null;
                }
            }

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                String responseString = "";
                if (response != null) {
                    responseString = String.valueOf(response.statusCode);
                }
                return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
            }
        };

        requstQueue.add(stringRequest);

        return true;
    }
}
