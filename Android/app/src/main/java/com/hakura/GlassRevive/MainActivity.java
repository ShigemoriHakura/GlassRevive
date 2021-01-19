package com.hakura.GlassRevive;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    public static Activity context;
    public static SeekBar exp;
    public static SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        exp=(SeekBar) findViewById(R.id.Bar_Exp);
        sharedPreferences = getSharedPreferences("GlassRevive", MODE_PRIVATE);
        int expirationTime =  sharedPreferences.getInt("expirationTime",60 * 5);
        TextView tv = (TextView) MainActivity.context.findViewById(R.id.text_Status_Exp);
        tv.setText(expirationTime + "秒");
        exp.setProgress(expirationTime);

        exp.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("expirationTime",progress);
                editor.apply();
                TextView tv = (TextView) MainActivity.context.findViewById(R.id.text_Status_Exp);
                tv.setText(progress + "秒");
                tv.invalidate();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Toast.makeText(MainActivity.this,"修改成功",Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onTestButtonClick(View v) {
        try {
            EditText text = (EditText)findViewById(R.id.InputMessage);
            int expirationTime =  sharedPreferences.getInt("expirationTime",60 * 5);
            MyGlassUtils.sendText("Hi", text.getText().toString(), "com.hakura.GlassRevive",  UUID.randomUUID().toString(),expirationTime);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onStatusButtonClick(View v) {
        try {
            MyGlassUtils.sendInfoRequest();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onGivePermissionButtonClick(View v) {
        Intent intent_s = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
        startActivity(intent_s);
    }

}