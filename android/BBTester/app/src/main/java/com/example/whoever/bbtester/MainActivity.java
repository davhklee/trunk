package com.example.whoever.bbtester;

import android.Manifest;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import android.bluetooth.*;
import android.bluetooth.le.*;
import android.content.Context;
import android.content.pm.PackageManager;
import android.widget.Button;
import android.widget.TextView;
import android.os.Build.VERSION;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity {





    class WebServiceTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected String doInBackground(String... inputs) {
            int count;
            try {
                String root = Environment.getExternalStorageDirectory().toString();

                URL url = new URL(inputs[0]);
                URLConnection conection = url.openConnection();
                conection.connect();
                int len = conection.getContentLength();
                // input stream to read file - with 8k buffer
                InputStream input = new BufferedInputStream(url.openStream(), 8192);

                OutputStream output = new FileOutputStream(root+"/sample.png");
                byte data[] = new byte[1024];

                long total = 0;
                while ((count = input.read(data)) != -1) {
                    total += count;

                    // writing data to file
                    output.write(data, 0, count);

                }

                // flushing output
                output.flush();

                // closing streams
                output.close();
                input.close();

            } catch (Exception e) {
            }

            return null;
        }

        protected void onPostExecute(String file_url) {
        }

    }






    // Device scan callback.
    private String mystr = "default";

    // API 21
    private ScanCallback mScanCallback;
    private BluetoothLeScanner mBluetoothLeScanner;
    private AdvertiseCallback mAdvertiseCallback;
    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;

    // API 18
    private BluetoothAdapter.LeScanCallback mLeScanCallback;

    private BluetoothAdapter mBluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button mbutton = (Button) findViewById(R.id.mybutton);
        mbutton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showText(mystr);
                mystr = "";

                new WebServiceTask().execute("http://nodejs-mongo-persistent-projectzero.1d35.starter-us-east-1.openshiftapps.com/images/sample.png");

                if (Build.VERSION.SDK_INT >= 21) {
                    mBluetoothLeScanner.startScan(mScanCallback);

                    AdvertiseSettings.Builder settingsBuilder = new AdvertiseSettings.Builder();
                    settingsBuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY);
                    AdvertiseSettings settings = settingsBuilder.build();

                    AdvertiseData.Builder dataBuilder = new AdvertiseData.Builder();
                    dataBuilder.setIncludeDeviceName(true);
                    AdvertiseData data = dataBuilder.build();

                    AdvertiseData.Builder respBuilder = new AdvertiseData.Builder();
                    respBuilder.setIncludeTxPowerLevel(true);
                    AdvertiseData resp = respBuilder.build();

                    mBluetoothLeAdvertiser.startAdvertising(settings, data, resp, mAdvertiseCallback);
                } else {
                    mBluetoothAdapter.startLeScan(mLeScanCallback);
                }
            }
        });

        if (savedInstanceState == null) {



            mBluetoothAdapter = ((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE))
                    .getAdapter();
            // Is Bluetooth supported on this device?
            if (mBluetoothAdapter != null) {
                showText(Environment.getExternalStorageDirectory().toString());
                showText("Android API level: " + VERSION.SDK_INT);
                if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                    showText("LE supported");
                    //mBluetoothAdapter.startLeScan(mLeScanCallback);
                    //mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
                } else {
                    showText("LE not supported");
                }
                // s Bluetooth turned on?
                if (mBluetoothAdapter.isEnabled()) {
                    showText("Bluetooth is enabled.");
                    // Are Bluetooth Advertisements supported on this device?
                    /*
                    if (mBluetoothAdapter.isMultipleAdvertisementSupported()) {
                        // Everything is supported and enabled, load the fragments.
                        showText("Everything is supported and enabled, load the fragments.");
                    } else {
                        // Bluetooth Advertisements are not supported.
                        showText("Bluetooth Advertisements are not supported.");
                    }
                    */
                } else {
                    // Prompt user to turn on Bluetooth (logic continues in onActivityResult()).
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, 1); // REQUEST_ENABLE_BT = 1
                    showText("Prompt user to turn on Bluetooth");
                }
            } else {
                // Bluetooth is not supported.
                showText("Bluetooth is not supported.");
            }





            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1); // requestCode = 1
            }

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 2); // requestCode = 0
            }




        } // saved instance



        // initialize the right scan callback for the current API level
        if (Build.VERSION.SDK_INT >= 21) {

            mScanCallback = new ScanCallback() {
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
                    mystr += "\r\n" + result.getDevice().getAddress().toString();
                }
            };

            mAdvertiseCallback = new AdvertiseCallback() {
                @Override
                public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                    super.onStartSuccess(settingsInEffect);
                }
            };

            mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
            mBluetoothLeAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
        } else {

            mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
                public void onLeScan(BluetoothDevice device, int i, byte[] bytes) {
                    mystr += "\r\n" + device.getAddress().toString();
                }
            };
        }



    } // oncreate





    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showText(String msg) {
        TextView view = (TextView) findViewById(R.id.mytestview);
        msg = view.getText() + "\n\r" + msg;
        view.setText(msg);
    }

}
