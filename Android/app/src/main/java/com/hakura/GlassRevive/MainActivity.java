package com.hakura.GlassRevive;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    public static Activity context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        try {
            MyGlassUtils.sendInfoRequest();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void onTestButtonClick(View v) {
        try {
            EditText text = (EditText)findViewById(R.id.InputMessage);
            MyGlassUtils.sendText("Hello", text.getText().toString(), "GlassRevive");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onGivePermissionButtonClick(View v) {
        Intent intent_s = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
        startActivity(intent_s);
    }
}