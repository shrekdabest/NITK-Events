package com.example.nitk_events;

import android.content.Intent;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;


public class SplashActivity extends AppCompatActivity {

    ImageView welcomeImage;  //making all the views global so that they can be accessed by all the functions.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);


        welcomeImage=findViewById(R.id.welcomelogo);//getting reference and making it a java object.

        Animation animation= AnimationUtils.loadAnimation(this,R.anim.transition_for_splash);//got the animation object
        //from anim directory

        welcomeImage.startAnimation(animation);  //starting animation


        new CountDownTimer(3000,1000) { //countdowntimer to start activity after 3 seconds
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                Intent intent=new Intent(SplashActivity.this,MainActivity.class);
                startActivity(intent);//Explicit Intent to start the new activity
                finish();//when you press back button this activity wont be called again because we have removed it from stack
            }
        }.start();

    }
}
