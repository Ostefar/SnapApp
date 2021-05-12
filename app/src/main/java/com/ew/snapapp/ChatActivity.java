package com.ew.snapapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class ChatActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Button returnBtn = findViewById(R.id.myReturnBtn);
        returnBtn.setOnClickListener(v -> goToMain());
    }
    public void goToMain(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    // load edited photo from firebase
    public void loadFromDb(){

    }

    //delete photo from db after view (reload after?)
    public void deleteFromDb(){

    }
}