package com.tebyan.raspberry_jt;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.concurrent.Executor;

import static com.tebyan.raspberry_jt.MainActivity.MY_UUID;

public class SignalSenderActivity extends AppCompatActivity {

    private boolean userIdentified = false;

    public ConnectThread connectThread;

    // region Bluetooth Variables
    BluetoothAdapter bluetoothAdapter;
    BluetoothSocket bluetoothSocket;
    BluetoothDevice selectedDevice;
    // endregion


    //region Biometric Variables
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    //endregion


    // Views ---------------------------------
    Button connectButton;
    Button identificationButton;
    Button openButton;
    Button closeButton;
    Button stopButton;
    Button sendButton;
    EditText commandEditText;
    TextView identificationText;
    TextView connectionText;

    //-----------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signal_sender);

        //region View Initialization
        connectButton = findViewById(R.id.connect_button);
        openButton = findViewById(R.id.open_button);
        closeButton = findViewById(R.id.close_button);
        stopButton = findViewById(R.id.stop_button);
        sendButton=findViewById(R.id.send_button);
        commandEditText=findViewById(R.id.command_edittext);
        identificationButton = findViewById(R.id.identify_user_button);
        identificationText = findViewById(R.id.identification_text);
        connectionText=findViewById(R.id.connection_text);

        //endregion
        hideControls();
        //region Biometric Initialize
        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(SignalSenderActivity.this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Toast.makeText(SignalSenderActivity.this, "You are Jaber", Toast.LENGTH_SHORT).show();
                identificationText.setText("User Identified");
                identificationText.setTextColor(getResources().getColor(android.R.color.holo_green_light));
                userIdentified = true;
                if(connectThread!=null && connectThread.bluetoothSocket.isConnected()){
                    showControls();
                }

            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(SignalSenderActivity.this, "You are not Jaber", Toast.LENGTH_SHORT).show();
            }
        });
        promptInfo = new BiometricPrompt.PromptInfo.Builder().setTitle("Biometric Identification of Jaber Tebyan").setNegativeButtonText("Cancel").setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.BIOMETRIC_WEAK).build();
        //endregion


        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        selectedDevice = getIntent().getExtras().getParcelable("selectedDevice");

        //region Setting Button Listeners
        identificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                biometricPrompt.authenticate(promptInfo);
            }
        });
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connectButton.setClickable(false);
                Toast.makeText(SignalSenderActivity.this, "Connecting: " + selectedDevice.getName(), Toast.LENGTH_SHORT).show();
                if (connectThread == null || connectThread.finished) {
                    connectThread = new ConnectThread(selectedDevice);
                    connectThread.start();
                }

            }
        });
        openButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (connectThread != null && connectThread.bluetoothSocket!=null && connectThread.bluetoothSocket.isConnected()) {
                    connectThread.write("open");
                }
            }
        });
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (connectThread != null && connectThread.bluetoothSocket!=null && connectThread.bluetoothSocket.isConnected()) {
                    connectThread.write("close");
                }
            }
        });
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (connectThread != null && connectThread.bluetoothSocket!=null && connectThread.bluetoothSocket.isConnected()) {
                    connectThread.write("stop");
                }
            }
        });
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text=commandEditText.getText().toString();
                if(connectThread!=null && connectThread.bluetoothSocket!=null&& connectThread.bluetoothSocket.isConnected()){
                    connectThread.write(text);
                }
                commandEditText.setText("");
            }
        });

        //endregion
        Toast.makeText(this, selectedDevice.getName(), Toast.LENGTH_SHORT).show();
    }


    public void hideControls() {
        stopButton.setVisibility(View.INVISIBLE);
        closeButton.setVisibility(View.INVISIBLE);
        openButton.setVisibility(View.INVISIBLE);
        sendButton.setVisibility(View.INVISIBLE);
        commandEditText.setVisibility(View.INVISIBLE);


    }

    public void showControls() {
        stopButton.setVisibility(View.VISIBLE);
        closeButton.setVisibility(View.VISIBLE);
        openButton.setVisibility(View.VISIBLE);
        sendButton.setVisibility(View.VISIBLE);
        commandEditText.setVisibility(View.VISIBLE);
    }




    private class ConnectThread extends Thread {
        private BluetoothSocket bluetoothSocket;
        private final BluetoothDevice bluetoothDevice;
        public boolean finished;


        public ConnectThread(BluetoothDevice bluetoothDevice) {
            this.bluetoothDevice = bluetoothDevice;
            BluetoothSocket tmpSocket = null;
            try {
                tmpSocket = bluetoothDevice.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (Exception e) {

                Toast.makeText(SignalSenderActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
            bluetoothSocket = tmpSocket;
        }

        public void write(String text) {
            try {
                bluetoothSocket.getOutputStream().write(text.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            boolean success = false;
            bluetoothAdapter.cancelDiscovery();
            connectionText.setTextColor(getColor(android.R.color.holo_orange_dark));
            setText("connecting...");
            try {
                bluetoothSocket.connect();
                success = true;
            } catch (IOException e) {

                e.printStackTrace();
                try {
                    bluetoothSocket = (BluetoothSocket) bluetoothDevice.getClass().getMethod("createRfcommSocket", new Class[]{Integer.TYPE}).invoke(bluetoothDevice, 1);
                    bluetoothSocket.connect();
                    success = true;
                } catch (Exception ex) {

                    Toast("Can't Establish Connection: " + connectThread.bluetoothDevice.getName() + "\n" + e.getMessage(), Toast.LENGTH_SHORT);
                    ex.printStackTrace();
                }

            }
            finished = true;
            if (success) {
                connectionText.setTextColor(getColor(android.R.color.holo_green_light));
                Toast("Connected ----- WOOOOOO HOOOOOO", Toast.LENGTH_SHORT);
                setText("connected");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(SignalSenderActivity.this.userIdentified){
                            SignalSenderActivity.this.showControls();
                        }
                    }
                });
            } else {
                connectionText.setTextColor(getColor(android.R.color.holo_red_light));
                setText("connection failed");
                try {
                    bluetoothSocket.close();
                } catch (IOException ex) {
                    Toast("Cannot Close Bluetooth Socket" + "   " + ex.getMessage(), Toast.LENGTH_SHORT);
                    ex.printStackTrace();
                }
            }
            if (!bluetoothSocket.isConnected()) {

                connectButton.setClickable(true);
            }
        }

        public void cancel() {
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                Toast("Cannot Close Bluetooth Socket" + "   " + e.getMessage(), Toast.LENGTH_SHORT);

                e.printStackTrace();
            }
        }
        public void setText(final String text){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    connectionText.setText(text);
                }
            });
        }
        public void Toast(final String text, final int duration) {
            SignalSenderActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(SignalSenderActivity.this, text, duration).show();
                }
            });
        }

    }
}
