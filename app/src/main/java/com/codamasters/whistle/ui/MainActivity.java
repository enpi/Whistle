package com.codamasters.whistle.ui;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.codamasters.whistle.R;
import com.codamasters.whistle.service.WhistleService;
import com.wooplr.spotlight.SpotlightView;
import com.wooplr.spotlight.prefs.PreferencesManager;
import com.zcw.togglebutton.ToggleButton;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS = "Whistle";
    private static final String TOGGLE_STATE = "ToggleState";
    private static final int REQUEST_MICROPHONE = 101;

    private ToggleButton toggleButton;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initToggleButton();
        initFabButton();
    }

    private void initFabButton(){
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PreferencesManager mPreferencesManager = new PreferencesManager(MainActivity.this);
                mPreferencesManager.resetAll();

                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        showIntro(fab, "Show");
                    }
                }, 400);
            }
        });
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
                    Toast.makeText(this, getString(R.string.alert_sound_permission), Toast.LENGTH_SHORT).show();
                    toggleButton.toggleOff();
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


    private void showIntro(View view, String usageId) {
        new SpotlightView.Builder(this)
                .introAnimationDuration(400)
                .performClick(true)
                .fadeinTextDuration(400)
                .headingTvColor(Color.parseColor("#eb273f"))
                .headingTvSize(28)
                .headingTvText("Whistle\nController")
                .subHeadingTvColor(Color.parseColor("#ffffff"))
                .subHeadingTvSize(16)
                .subHeadingTvText(getString(R.string.info_message))
                .maskColor(Color.parseColor("#dc000000"))
                .target(view)
                .lineAnimDuration(400)
                .lineAndArcColor(Color.parseColor("#eb273f"))
                .dismissOnTouch(true)
                .dismissOnBackPress(true)
                .enableDismissAfterShown(true)
                .usageId(usageId) //UNIQUE ID
                .show();
    }

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu){

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_help:
                PreferencesManager mPreferencesManager = new PreferencesManager(MainActivity.this);
                mPreferencesManager.resetAll();

                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        showIntro(toggleButton, "Show");
                    }
                }, 400);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    */
}
