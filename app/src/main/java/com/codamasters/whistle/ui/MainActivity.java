package com.codamasters.whistle.ui;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.codamasters.whistle.R;
import com.codamasters.whistle.service.WhistleService;
import com.zcw.togglebutton.ToggleButton;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS = "Whistle";
    private static final String TOGGLE_STATE = "ToggleState";
    private static final int REQUEST_MICROPHONE = 101;

    private ToggleButton toggleButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initToggleButton();
    }

    private void initToggleButton(){
        toggleButton = (ToggleButton) findViewById(R.id.toggleButton);

        if(loadToggleState())
            toggleButton.setToggleOn();
        else
            toggleButton.setToggleOff();

        toggleButton.setOnToggleChanged(new ToggleButton.OnToggleChanged() {
            @Override
            public void onToggle(boolean on) {
                if(on){
                    startService();
                }else{
                    stopService();
                }
                saveToggleState(on);
            }
        });
    }

    private void startService(){
        if(requestPermission())
            startService(new Intent(this, WhistleService.class));
    }

    private void stopService(){
        stopService(new Intent(this, WhistleService.class));
    }

    private boolean requestPermission(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_MICROPHONE);

            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_MICROPHONE: {
                if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Si no acepta los permisos no funcionará", Toast.LENGTH_SHORT).show();
                } else {
                    startService(new Intent(this, WhistleService.class));
                }
                return;
            }
        }
    }

    private boolean loadToggleState(){
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        return prefs.getBoolean(TOGGLE_STATE, false);
    }

    private void saveToggleState(boolean toggle){
        SharedPreferences.Editor editor = getSharedPreferences(PREFS, MODE_PRIVATE).edit();
        editor.putBoolean(TOGGLE_STATE, toggle);
        editor.commit();
    }
}
