package com.example.workshop_iot;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.Locale;

public class Showgaz extends AppCompatActivity implements TextToSpeech.OnInitListener{


    TextView res, msg;
    ImageView img;
    private Vibrator vibrator;
    private TextToSpeech textToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_gaz_activity);

        res = findViewById(R.id.res);
        msg = findViewById(R.id.mssg);
        img = findViewById(R.id.image);

        Intent intent = getIntent();
        String brokerValue = intent.getStringExtra("broker");
        String topicValue = intent.getStringExtra("topic");
        String portValue = intent.getStringExtra("port");

        // Save initial values to SharedPreferences
        saveInitialValues(brokerValue, topicValue, portValue);

        // Initialize Text-to-Speech
        textToSpeech = new TextToSpeech(this, this);

        // Initialize Vibrator
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        new Thread(new Runnable() {
            @Override
            public void run() {
                connectToMQTT(brokerValue, topicValue, portValue);
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Release Text-to-Speech resources
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            // Set language for Text-to-Speech (you can customize this based on your app's requirements)
            int langResult = textToSpeech.setLanguage(Locale.getDefault());

            if (langResult == TextToSpeech.LANG_MISSING_DATA ||
                    langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                showToast("Text-to-Speech language not supported");
            }
        } else {
            showToast("Text-to-Speech initialization failed");
        }
    }

    private void connectToMQTT(String broker, String topic, String port) {
        String clientId = "AndroidCl" + System.currentTimeMillis();

        try {
            MqttClient client = new MqttClient("tcp://" + broker + ":" + port, clientId, null);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);

            client.connect(options);

            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    // Handle connection lost
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    String gasValue = new String(message.getPayload());
                    updateGasValue(gasValue);
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    // Handle delivery completion
                }
            });

            client.subscribe(topic);

        } catch (Exception e) {
            showToast("Error connecting to MQTT");
        }
    }

    private void updateGasValue(final String value) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                res.setText(value);

                // Retrieve SharedPreferences
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                // Save gas value
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("gasValue", value);
                editor.apply();

                int seuilConvert = Integer.parseInt(getIntent().getStringExtra("seuil"));
                int valueConvert = Integer.parseInt(value);

                showToast("Value has changed");

                int backgroundColor = getResources().getColor(R.color.red);
                int backgroundColor2 = getResources().getColor(R.color.green);
                int imageResource = R.drawable.alert_icon;
                int imageResource2 = R.drawable.safe;

                if (valueConvert > seuilConvert) {
                    img.setImageResource(imageResource);
                    msg.setText("Danger");
                    msg.setTextColor(backgroundColor);

                    // Vibrate when gas value exceeds the threshold
                    vibrate();

                    // Notify the user verbally
                    speak("niveau de gaz élevée ");
                } else {
                    img.setImageResource(imageResource2);
                    msg.setText("Safety");
                    msg.setTextColor(backgroundColor2);
                }
            }
        });
    }

    private void vibrate() {
        if (vibrator != null && vibrator.hasVibrator()) {
            // Vibrate for 500 milliseconds
            vibrator.vibrate(500);
        }
    }

    private void speak(String text) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // For Android 21 and above
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "utteranceId");
        } else {
            // For Android 20 and below
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    private void saveInitialValues(String broker, String topic, String port) {
        // Save initial values to SharedPreferences
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("broker", broker);
        editor.putString("topic", topic);
        editor.putString("port", port);
        editor.apply();
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}