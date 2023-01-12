package com.example.customview;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashSet;
import java.util.Set;

public class BtconnectActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    /**
     * La classe de peripherique utilise par le RN42
     *
     */
    private final static int RN42_COD = 0x1F00;

    private ProgressBar progresse_bar;

    private enum ACTION {START, STOP}

    ;
    private Toolbar toolbar;
    private Button bt;
    private ListView lv1;
    private ListView lv2;
    private ArrayAdapter<String> mPaired_Adapter;
    private ArrayAdapter<String> mDiscover_Adapter;

    Set<String> foundList = new HashSet<>();
    private BluetoothAdapter mBluetoothAdapter;

    private boolean mBroadcastRegistered = false;

    private BroadcastReceiver mBroadcastReceiver;
    ActivityResultLauncher<Intent> btLauncher;

    /**
     * methode lancer au premier lancement de l'activite qui permet :
     *      initialisation du toolbar
     *      initialisaton du progress bar
     *      instanciation du broadcast receiver
     *      initialisation du listener du bouton bt qui permet de faire le scan
     *      creation des listes des peripheriques
     *
     * @param savedInstanceState
     */
    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_btconnect);

        toolbar = findViewById(R.id.tb);
        setSupportActionBar(toolbar);

        bt = findViewById(R.id.button);
        bt.setOnClickListener(this);

        progresse_bar = findViewById(R.id.prog_bar);


        lv1 = findViewById(R.id.lv1);
        lv1.setOnItemClickListener(this);
        mPaired_Adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        lv1.setAdapter(mPaired_Adapter);

        lv2 = findViewById(R.id.lv2);
        lv2.setOnItemClickListener(this);
        mDiscover_Adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        lv2.setAdapter(mDiscover_Adapter);


        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


        @SuppressLint("MissingPermission")
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice pair : pairedDevices) {
                mPaired_Adapter.add(pair.getName() + "\n" + pair.getAddress());
            }
        } else {
            mPaired_Adapter.add("Aucun peripherique appaire");
        }

        mBroadcastReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {

                String action = intent.getAction();
                BluetoothDevice bluetoothDevice;

                switch (action) {
                    case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                        if (mDiscover_Adapter.getCount() == 0) {
                            mDiscover_Adapter.add("aucun peripherique trouve");
                        }
                        progresse_bar.setVisibility(View.INVISIBLE);
                        break;

                    case BluetoothDevice.ACTION_FOUND:
                        bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        if ((bluetoothDevice.getBondState() != BluetoothDevice.BOND_BONDED) && (bluetoothDevice.getBluetoothClass().getDeviceClass() == RN42_COD)) {
                            if (!foundList.contains(bluetoothDevice.getAddress())) {
                                foundList.add(bluetoothDevice.getAddress());
                                mDiscover_Adapter.add(bluetoothDevice.getName() + "\n" + bluetoothDevice.getAddress());
                            }
                        }
                        break;
                }
            }
        };
        btLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                on_bt_scan();
            } else {
                Toast.makeText(this, "Le bluetooth doit etre active", Toast.LENGTH_SHORT).show();
            }
        });


    }

    /**
     * methode appelee lorsque le bouton bt est clique
     * @param view
     */
    @Override
    public void onClick(View view) {
        if (!mBluetoothAdapter.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            btLauncher.launch(intent);
            return;
        }
        on_bt_scan();
    }


    /**
     * methode qui permet de savoir si on doit commencer ou arreter la recherche des peripheriques
     */
    private void on_bt_scan() {
        if (bt.getText().equals("Scanner")) {
            btScan(ACTION.START);
            progresse_bar.setVisibility(View.VISIBLE);
            bt.setText("Annuler");
        } else {
            btScan(ACTION.STOP);
            progresse_bar.setVisibility(View.INVISIBLE);
            mDiscover_Adapter.clear();
            foundList.clear();
            bt.setText("Scanner");
        }
    }

    /**
     * methode lancer par la methode on_btscan() qui permet de commencer la recherche des peripheriques
     * @param startstop
     */
    @SuppressLint("MissingPermission")
    public void btScan(ACTION startstop) {
        if (startstop == ACTION.START) {
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            registerReceiver(mBroadcastReceiver, filter);
            mBroadcastRegistered = true;
            mBluetoothAdapter.startDiscovery();
        } else {
            unregisterReceiver(mBroadcastReceiver);
            mBroadcastRegistered = false;
            mBluetoothAdapter.cancelDiscovery();
        }
    }

    /**
     * la selection du peripherique
     * @param adapterView
     * @param view
     * @param i
     * @param l
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Intent intent = new Intent();
        mBluetoothAdapter.cancelDiscovery();
        String info = ((TextView) view).getText().toString();

        if (info.equals("aucun peripherique trouve") || info.equals("Aucun peripherique appaire")) {
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        if (info.length() > 17) {
            info = info.substring((info.length() - 17));
            intent.putExtra("device", info);
            setResult(RESULT_OK, intent);
            finish();
        }
        setResult(RESULT_CANCELED);
        finish();
    }

    /**
     * methode lancer lorsqu'une autre activite va s'afficher a l'avant plan.
     */

    @SuppressLint("MissingPermission")
    @Override
    protected void onPause() {
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.cancelDiscovery();
        }
        if(mBroadcastRegistered){
            unregisterReceiver(mBroadcastReceiver);
        }
        super.onPause();
    }
}