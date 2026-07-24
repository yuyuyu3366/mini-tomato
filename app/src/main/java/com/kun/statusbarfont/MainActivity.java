package com.kun.statusbarfont;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends Activity {

    public static final String PREFS_NAME = "font_settings";
    public static final String KEY_WEIGHT = "clock_weight";
    public static final String ACTION_UPDATE_WEIGHT = "com.kun.statusbarfont.ACTION_UPDATE_WEIGHT";
    public static final String EXTRA_WEIGHT = "weight";

    private TextView valueText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SeekBar seekBar = findViewById(R.id.seekBarWeight);
        valueText = findViewById(R.id.textWeightValue);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int savedWeight = prefs.getInt(KEY_WEIGHT, 600);

        seekBar.setProgress(savedWeight - 100);
        valueText.setText("当前粗细: " + savedWeight);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int weight = progress + 100;
                valueText.setText("当前粗细: " + weight);
                if (fromUser) {
                    sendWeightBroadcast(weight);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int weight = seekBar.getProgress() + 100;
                SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
                editor.putInt(KEY_WEIGHT, weight);
                editor.apply();
            }
        });
    }

    private void sendWeightBroadcast(int weight) {
        Intent intent = new Intent(ACTION_UPDATE_WEIGHT);
        intent.setPackage("com.android.systemui");
        intent.putExtra(EXTRA_WEIGHT, weight);
        sendBroadcast(intent);
    }
}
