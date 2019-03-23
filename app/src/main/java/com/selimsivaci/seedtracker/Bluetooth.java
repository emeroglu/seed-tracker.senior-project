package com.selimsivaci.seedtracker;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.android.internal.util.Predicate;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;


import static java.lang.Thread.sleep;

public class Bluetooth {

    private static Handler handler;

    public static BluetoothAdapter adapter;
    public static BluetoothDevice device;

    private static byte[] message;
    private static String response;
    private static int trials;

    private static int bytesAvailable;

    public static boolean active;

    public static void initialize() {

        adapter = BluetoothAdapter.getDefaultAdapter();

        Set pairedDevices = adapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (Object d : pairedDevices) {
                if (((BluetoothDevice) d).getName().equals("HC-05")) //Note, you will need to change this to match the name of your device
                {
                    device = ((BluetoothDevice) d);
                    break;
                }
            }
        }
    }

    public static String send(String request, Method onSuccess) {

        try {
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); //Standard SerialPortService ID

            BluetoothSocket socket = device.createRfcommSocketToServiceRecord(uuid);

            if (!active) {

                active = true;

                socket.connect();

                OutputStream outputStream = socket.getOutputStream();
                InputStream inputStream = socket.getInputStream();
                outputStream.write(request.getBytes());

                handler = new Handler();

                Listen(socket, inputStream, onSuccess);
            }

        } catch (IOException e) {
            active = false;
            e.printStackTrace();
        }

        return response;

    }

    private static void Listen(final BluetoothSocket socket, final InputStream inputStream, final Method onSuccess) {

        Thread workerThread = new Thread(new Runnable() {
            @Override
            public void run() {

                try {

                    response = "";
                    trials = 0;
                    bytesAvailable = 0;

                    while (!Thread.currentThread().isInterrupted() && trials != 100) {

                        bytesAvailable = inputStream.available();

                        if (bytesAvailable > 0) {
                            message = new byte[bytesAvailable];

                            inputStream.read(message);

                            boolean save = false;
                            String character = "";

                            for (int i = 0; i < message.length; i++) {

                                character = FromByte(message[i]);

                                if (save)
                                    response += character;

                                if (character == "#")
                                    save = true;

                                if (character == "/") {
                                    response = response.substring(0, response.length() - 1);
                                    break;
                                }
                            }

                        } else {
                            trials++;
                            sleep(100);
                        }
                    }

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            onSuccess.Invoke(response);
                        }
                    });

                    socket.close();

                    active = false;

                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    try {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                onSuccess.Invoke(response);
                            }
                        });
                        active = false;
                        socket.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }

        });

        workerThread.start();

    }

    private static String FromByte(int i) {
        if (i == 47)
            return "/";
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
        else if (i == 35)
            return "#";
        else if (i == 46)
            return ".";
        else
            return "";
    }

}
