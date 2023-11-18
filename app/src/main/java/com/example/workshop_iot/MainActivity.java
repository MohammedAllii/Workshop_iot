package com.example.workshop_iot;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private EditText broker, topic, port, seuil;
    private ImageButton btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        broker = findViewById(R.id.broker);
        topic = findViewById(R.id.topic);
        port = findViewById(R.id.port);
        seuil = findViewById(R.id.ceu);
        btn = findViewById(R.id.btn);

        TextView animatedText = findViewById(R.id.make);

        int screenWidth = getResources().getDisplayMetrics().widthPixels;

        // Create an ObjectAnimator for translation animation
        ObjectAnimator animator = ObjectAnimator.ofFloat(animatedText, "translationX", -screenWidth, screenWidth);
        animator.setDuration(4000); // Set the duration of the animation in milliseconds
        animator.setInterpolator(new LinearInterpolator()); // Set a linear interpolator for constant speed
        animator.setRepeatCount(ObjectAnimator.INFINITE); // Repeat the animation infinitely

        // Start the animation
        animator.start();

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, Showgaz.class);
                intent.putExtra("broker", broker.getText().toString());
                intent.putExtra("topic", topic.getText().toString());
                intent.putExtra("port", port.getText().toString());
                intent.putExtra("seuil", seuil.getText().toString());
                startActivity(intent);
            }
        });
    }
}