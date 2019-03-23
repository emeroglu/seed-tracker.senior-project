package com.selimsivaci.seedtracker;


import android.media.Image;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class TrackSoil extends AppCompatActivity {

    TextView waterpump;
    ImageButton setmoisturelevel;
    ImageView ima;
    SeekBar seekBarObj;
    ImageButton apply;
    TextView txt;
    TextView pumplvl;
    TextView tvBluetooth;

    Boolean update, sync;

    int level;

    private void Update() {

        if (update) {
            tvBluetooth.setText((Bluetooth.active) ? "Bluetooth: Transmitting..." : "Bluetooth: Available");

            new android.os.Handler().postDelayed(
                    new Runnable() {
                        public void run() {
                            Update();
                        }
                    },
                    500);
        }
    }

    private void Sync() {
        if (sync) {
            if (!Bluetooth.active) {

                Toast.makeText(getApplicationContext(), "Retrieving Water Level", Toast.LENGTH_SHORT).show();

                Bluetooth.send("2/", new Method() {
                    @Override
                    public void Invoke(String response) {
                        try {

                            String st = response.split(" ")[1];
                            waterpump.setText("Water Level: %" + st);

                            level = Integer.parseInt(st);

                            if (level < 30)
                                Toast.makeText(getApplicationContext(), "Water Level too low!!!", Toast.LENGTH_SHORT).show();

                            new android.os.Handler().postDelayed(
                                    new Runnable() {
                                        public void run() {
                                            Sync();
                                        }
                                    },
                                    10000);

                        } catch (Exception ex) {

                            new android.os.Handler().postDelayed(
                                    new Runnable() {
                                        public void run() {
                                            Sync();
                                        }
                                    },
                                    10000);
                        }
                    }
                });

            } else {

                new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                Sync();
                            }
                        },
                        1000);

            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_soil);

        tvBluetooth = (TextView) findViewById(R.id.tvBluetooth);
        pumplvl = (TextView) findViewById(R.id.pumplevel);
        waterpump = (TextView) findViewById(R.id.waterlevel);
        ima = (ImageView) findViewById(R.id.track);
        txt = (TextView) findViewById(R.id.number);

        update = true;
        sync = true;

        Update();
        Sync();

        apply = (ImageButton) findViewById(R.id.apply);
        apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Bluetooth.active)
                    Toast.makeText(getApplicationContext(), "Bluetooth busy right now...", Toast.LENGTH_SHORT).show();
                else {
                    if (level > 30)
                        Bluetooth.send("1 " + seekBarObj.getProgress() + "/", new Method() {
                            @Override
                            public void Invoke(String response) {

                                if (response.equals("1 0"))
                                    Toast.makeText(getApplicationContext(), "Fatal Error", Toast.LENGTH_SHORT).show();
                                else if (response.equals("1 1"))
                                    Toast.makeText(getApplicationContext(), "Mission Completed", Toast.LENGTH_SHORT).show();
                                else
                                    Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();
                            }
                        });
                    else
                        Toast.makeText(getApplicationContext(), "Water Level too low!!!", Toast.LENGTH_SHORT).show();
                }

            }
        });

        seekBarObj = (SeekBar) findViewById(R.id.seekBarMotor);
        seekBarObj.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                txt.setText("Value: " + seekBarObj.getProgress() + " / " + seekBarObj.getMax());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        setmoisturelevel = (ImageButton) findViewById(R.id.setmoisturelevel);
        setmoisturelevel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent2 = new Intent(TrackSoil.this, MoistureLevel.class);
                startActivity(intent2);
            }
        });


    }

    @Override
    protected void onPause() {
        super.onPause();
        update = false;
        sync = false;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        update = true;
        sync = true;
        Update();
        Sync();
    }
}