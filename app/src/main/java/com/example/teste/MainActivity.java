package com.example.teste;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.UUID;



public class MainActivity extends Activity implements LocationListener {
    private static final int REQUEST_ENABLE_BT = 1;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    protected LocationManager locationManager;
    protected LocationListener locationListener;
    protected Context context;
    TextView txtLat;
    String lat;
    String provider;
    protected String latitude, longitude;
    protected boolean gps_enabled, network_enabled;

    BluetoothAdapter bluetoothAdapter;
    BluetoothDevice device = null;
    BluetoothSocket socket;
    OutputStream outputStream;
    InputStream inputStream;

    byte[] buffer = new byte[256];  // buffer store for the stream
    int bytes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                225);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        txtLat = (TextView) findViewById(R.id.textview1);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // Bluetooth não é suportado no dispositivo
        } else if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        String deviceName;
        String deviceAddress;
        boolean achou = false;
        //BluetoothDevice device = null;
        while(achou == false) {
            for (BluetoothDevice d : pairedDevices) {
                // Faça algo com o nome e o endereço do dispositivo
                deviceName = d.getName();
                deviceAddress = d.getAddress();
                Log.d("Dev", d.getName());
                if (deviceName.equals("ESP32test")) {
                    Log.d("Dev", "Entrou no IF");
                    device = d;
                    achou = true;
                    break;
                }
            }
        }
        Log.d("Dev","Device escolhido: "+device.getName());

        try {
            socket = device.createRfcommSocketToServiceRecord(MY_UUID);
            Log.d("Dev","createRF");
            socket.connect();
            Log.d("Dev","socket connect");
            outputStream = socket.getOutputStream();
            inputStream = socket.getInputStream();
            Log.d("Dev","socket getOutput");
            //String message = "VAMO SAO PAULO!";
            //outputStream.write(message.getBytes());
            Log.d("Dev","Output write");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    public void onLocationChanged(Location location) {
        txtLat = (TextView) findViewById(R.id.textview1);

        String message = location.getLatitude() + "/" + location.getLongitude();
        try {
            outputStream.write(message.getBytes());

            //inputStream.read();
            //if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            //String result = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);


            bytes = inputStream.read(buffer, 0, inputStream.available());
            String result = new String(buffer, 0, bytes);

            txtLat.setText("Latitude:" + location.getLatitude() + ", Longitude:" + location.getLongitude() + "\nResposta:" + result);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("Latitude", "disable");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("Latitude", "enable");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("Latitude", "status");
    }
}

