package com.selimsivaci.seedtracker;

import android.content.Intent;
import android.media.Image;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MoistureLevel extends AppCompatActivity {

    ImageView yaprak;
    ImageView damla;
    ImageButton applybutton;
    SeekBar seek_bar;
    TextView text_view;
    TextView te;
    TextView tvBluetooth;

    Boolean update;
    Boolean sync;

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

                Toast.makeText(getApplicationContext(), "Retrieving Moisture Level", Toast.LENGTH_SHORT).show();

                Bluetooth.send("3/", new Method() {
                    @Override
                    public void Invoke(String response) {
                        try {

                            String st = response.split(" ")[1];
                            te.setText("Moisture Level: %" + st);

                            level = Integer.parseInt(st);

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
        setContentView(R.layout.activity_moisture_level);

        yaprak = (ImageView) findViewById(R.id.image_yesilyaprak);
        damla = (ImageView) findViewById(R.id.image_mavidamla);

        applybutton = (ImageButton) findViewById(R.id.apply);

        seek_bar = (SeekBar) findViewById(R.id.seekBar);
        text_view = (TextView) findViewById(R.id.number);

        tvBluetooth = (TextView) findViewById(R.id.tvBluetooth2);

        text_view.setText("Value: " + seek_bar.getProgress() + " / " + seek_bar.getMax());

        update = true;
        sync = true;

        Update();
        Sync();

        applybutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Bluetooth.active)
                    Toast.makeText(getApplicationContext(), "Bluetooth busy right now...", Toast.LENGTH_SHORT).show();
                else
                    Bluetooth.send("0 " + seek_bar.getProgress() + "/", new Method() {
                        @Override
                        public void Invoke(String response) {
                            try {
                                String s = response.split(" ")[1];
                                te.setText("Moisture Level: %" + s);
                            } catch (Exception e) {
                            }
                        }
                    });
            }
        });


        seek_bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                text_view.setText("Value: " + seek_bar.getProgress() + " / " + seek_bar.getMax());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        te = (TextView) findViewById(R.id.moisturelevel);

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

