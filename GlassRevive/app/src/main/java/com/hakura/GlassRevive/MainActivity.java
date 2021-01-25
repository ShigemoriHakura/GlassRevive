package com.hakura.GlassRevive;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    public static SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences("MyNewGlass", MODE_PRIVATE);
        int expirationTime = sharedPreferences.getInt("expirationTime",60 * 5);
        TextView tv = findViewById(R.id.TextView_ExpTime);
        tv.setText(expirationTime + " S");
        SeekBar exp = findViewById(R.id.SeekBar_Exp);
        exp.setProgress(expirationTime);

        exp.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("expirationTime", progress);
                editor.apply();
                TextView tv = findViewById(R.id.TextView_ExpTime);
                tv.setText(progress + " S");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Toast.makeText(MainActivity.this,"Done",Toast.LENGTH_SHORT).show();
            }
        });

        startService(new Intent(this, GlassService.class));
        //Intent bindIntent = new Intent(MainActivity.this, GlassService.class);
        //bindService(bindIntent, connection, BIND_AUTO_CREATE);
    }
}