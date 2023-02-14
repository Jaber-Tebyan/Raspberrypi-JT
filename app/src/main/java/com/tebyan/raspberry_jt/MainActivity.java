package com.tebyan.raspberry_jt;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;

import android.bluetooth.BluetoothDevice;

import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;


import android.text.GetChars;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;


public class MainActivity extends Activity{

    public static String TAG = "Jaber_LOG";
    public static final UUID MY_UUID=UUID.fromString("0000110E-0000-1000-8000-00805F9B34FB");
    ArrayList<BluetoothDevice> pairedDevices;

    ArrayList<String> pairedDevicesInfo;
    ListView pairedDevicesListView;
    ArrayAdapter<String> arrayAdapter;


    // Bluetooth Related---------------------------------------
    BluetoothAdapter bluetoothAdapter;

    //-------------------------------------------------------

    final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                Log.i(TAG, "onReceive: Yeah");
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                if (state == BluetoothAdapter.STATE_ON) {

                    refresh();
                }
                else if(state==BluetoothAdapter.STATE_OFF){
                    Log.i(TAG, "onReceive: HH");
                    refresh();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeWidgets();
        pairedDevicesInfo = new ArrayList<>();
        arrayAdapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, pairedDevicesInfo);
        pairedDevicesListView.setAdapter(arrayAdapter);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        IntentFilter filter=new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver,filter);
        bluetoothAdapter.enable();
        refresh();
        pairedDevicesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                BluetoothDevice device=pairedDevices.get(i);
                Intent intent=new Intent(getApplicationContext(),SignalSenderActivity.class);
                intent.putExtra("selectedDevice",device);
                startActivity(intent);
            }
        });

    }





    public void refresh() {
        pairedDevicesInfo.clear();

        pairedDevices = new ArrayList<>(bluetoothAdapter.getBondedDevices());
        for (BluetoothDevice _temp : pairedDevices) {
            pairedDevicesInfo.add(_temp.getName() + "----------" + _temp.getAddress());
            Log.i(TAG, "initializeList: " + _temp.getName() + "===" + _temp.getAddress());
        }
        arrayAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        bluetoothAdapter.disable();
    }

    public void initializeWidgets() {

        pairedDevicesListView = findViewById(R.id.pairedDevices_List);
    }




}


