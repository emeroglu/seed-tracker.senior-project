package com.selimsivaci.seedtracker;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.selimsivaci.seedtracker.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import static java.lang.Thread.sleep;

public class testBlu extends AppCompatActivity {

    Button btnConnect;
    Button btnSend;
    TextView txt;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothDevice mmDevice;
    BluetoothSocket mmSocket;
    OutputStream mmOutputStream;
    InputStream mmInputStream;

    Boolean stopWorker = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testblu);

        txt = (TextView) findViewById(R.id.txt);
        btnConnect = (Button) findViewById(R.id.btnConnect);
        btnSend = (Button) findViewById(R.id.btnSend);

        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); //Standard SerialPortService ID
                try {
                    mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
                } catch (IOException e) {
                    txt.setText(e.getMessage());
                }
                try {
                    mmSocket.connect();

                    mmOutputStream = mmSocket.getOutputStream();
                    mmInputStream = mmSocket.getInputStream();

                    Listen();

                } catch (IOException e) {
                    txt.setText(e.getMessage());
                }
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String msg = "1/";
                try {
                    mmOutputStream.write(msg.getBytes());
                } catch (IOException e) {
                    txt.setText(e.getMessage());
                }
            }
        });

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        Set pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (Object device : pairedDevices) {
                if (((BluetoothDevice) device).getName().equals("HC-05")) //Note, you will need to change this to match the name of your device
                {
                    mmDevice = ((BluetoothDevice) device);
                    txt.setText("Found device HC-05");

                    break;
                }
            }
        }

    }

    private String FromByte(int i) {
        if (i == 47)
            return "\n";
        else if (i == 48)
            return "0";
        else if (i == 49)
            return "1";
        else if (i == 50)
            return "2";
        else if (i == 51)
            return "3";
        else if (i == 52)
            return "4";
        else if (i == 53)
            return "5";
        else if (i == 54)
            return "6";
        else if (i == 55)
            return "7";
        else if (i == 56)
            return "8";
        else if (i == 57)
            return "9";
        else if (i == 32)
            return " ";
        else
            return "";
    }

    private void Listen() {

        Thread workerThread = new Thread(new Runnable() {
            @Override
            public void run() {

                while (!Thread.currentThread().isInterrupted() && !stopWorker) {

                    try {

                        int bytesAvailable = mmInputStream.available();

                        if (bytesAvailable > 0) {

                            byte[] packetBytes = new byte[bytesAvailable];
                            try {
                                mmInputStream.read(packetBytes);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            for (int i = 0; i < bytesAvailable; i++) {
                                final String character = FromByte(packetBytes[i]);



                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        txt.setText(txt.getText() + character);
                                    }
                                });
                            }

                        }
                    } catch (IOException ex) {
                        stopWorker = true;
                    }

                    try {
                        sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        });

        workerThread.start();

        txt.setText("");
    }
}
