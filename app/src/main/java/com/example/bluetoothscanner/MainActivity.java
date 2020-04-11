package com.example.bluetoothscanner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {


    private BluetoothAdapter bt_adapter;
    public static final int REQUEST_ACCESS_COARS_LOCATION = 1 ,REQUEST_ENABLE_BLUETOOTH = 11  ;
    private ListView devices_lv ;
    private ArrayAdapter<String> list_adapter ;
    private ArrayList<String> devices_array_list ;
    Button scan_btn ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
       // Toast.makeText(getApplicationContext(),"200 OK",Toast.LENGTH_SHORT).show();

        bt_adapter = BluetoothAdapter.getDefaultAdapter();
        devices_lv = (ListView)findViewById(R.id.list_view) ;
        scan_btn = (Button)findViewById(R.id.scan_btn);
        devices_array_list = new ArrayList<String>();
        list_adapter = new ArrayAdapter<String>(this ,android.R.layout.simple_list_item_1 ,devices_array_list );

        devices_lv.setAdapter(list_adapter);

        checkBluetoothState();

        scan_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkCoarseLocationPermission()){
                    list_adapter.clear();
                    bt_adapter.startDiscovery();
                }
                else {
                    checkBluetoothState();
                }
            }
        });
        checkCoarseLocationPermission();


    }


    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(devicesFoundReciever , new IntentFilter(BluetoothDevice.ACTION_FOUND));
        registerReceiver(devicesFoundReciever , new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED));
        registerReceiver(devicesFoundReciever , new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
        registerReceiver(devicesFoundReciever , new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));

    }



    @Override
    protected void onPause() {
        unregisterReceiver(devicesFoundReciever);
        super.onPause();
    }

    private boolean checkCoarseLocationPermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new  String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_ACCESS_COARS_LOCATION);
            return false ;
        }
        else {
            return  true ;
        }
    }

    private void checkBluetoothState(){
        if (bt_adapter == null) {
            new AlertDialog.Builder(this)
                    .setTitle("Not compatible")
                    .setMessage("Your phone does not support Bluetooth")
                    .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            System.exit(0);
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
        else {
            if(bt_adapter.isEnabled()){
                if(bt_adapter.isDiscovering()){
                    Toast.makeText(this,"Device discovering process ... ",Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(this,"Bluetooth is Enabled ",Toast.LENGTH_SHORT).show();
                    scan_btn.setEnabled(true);
                }
            }
            else {
                Toast.makeText(this,"You need to enable Bluetooth ",Toast.LENGTH_SHORT).show();
                Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBT, REQUEST_ENABLE_BLUETOOTH);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BLUETOOTH){
            checkBluetoothState();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case  REQUEST_ACCESS_COARS_LOCATION :
                if (grantResults.length>0  && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this,"access coarse location allowed, you can scan bluetooth devices ",Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(this,"access coarse location forbidden, you can not scan bluetooth devices ",Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private final BroadcastReceiver devicesFoundReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action  = intent.getAction() ;
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                Log.i("hello","worked");
                BluetoothDevice bt_device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                list_adapter.add(bt_device.getName() + "\n" + bt_device.getAddress());
                Log.i("hello",bt_device.getName());
                list_adapter.notifyDataSetChanged();
            }
            else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                scan_btn.setText("Scanning Bluetooth Devices");
                Log.i("hello","1");
            }
            else if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){
                scan_btn.setText("Scanning in progress ... ");
                Log.i("hello","2");
            }
            if(BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)){
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        checkBluetoothState();

                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        checkBluetoothState();

                        break;
                    case BluetoothAdapter.STATE_ON:
                        checkBluetoothState();
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        checkBluetoothState();
                        break;
                }
            }
        }
    };
}
