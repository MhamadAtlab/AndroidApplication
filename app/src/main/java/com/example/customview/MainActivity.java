package com.example.customview;


import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity implements CustomView.OnCustomViewChangeListener {

    private CustomView mCustomView;
    private TextView mTvValue;
    BluetoothAdapter mbt_adapter ;
    Intent intent;
    private final static int PERM_CODE=0;
    private boolean permissionGranted = false;
    String address = null;

    private OscilloManager mOscilloManager;

    public TextView getShowStatus() {
        return showStatus;
    }

    public void setShowStatus(TextView showStatus) {
        this.showStatus = showStatus;
    }

    private TextView showStatus;

    /**
     * methode lancer au premier lancement de l'activite qui permet d'initialiser les variables et d'implementer l'interface
     * TransceiverListener
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTvValue =findViewById(R.id.tv);
        mCustomView=findViewById(R.id.CustomView);
        mTvValue.bringToFront();
        mCustomView.setOnCustomViewChangeListener(this);
        mCustomView.setAccess(false);
        mTvValue.setText(String.format("%.0f", mCustomView.getmCurrentValue())+"%");
        showStatus=findViewById(R.id.tvStatus);
        mOscilloManager = OscilloManager.getInstance();
        showStatus.setText(mOscilloManager.getStatus());
        if(mOscilloManager.getStatus()=="CONNECTED"){
            mCustomView.setAccess(true);
        }else mCustomView.setAccess(false);
        mOscilloManager.setTransceiverListener(new Transceiver.TransceiverListener() {
            @Override
            public void onTransceiverDataReceived() {

            }

            @Override
            public void onTransceiverConnectionLost() {
                runOnUiThread(()-> Toast.makeText(MainActivity.this, "Connection Lost", Toast.LENGTH_LONG).show());
            }

            @Override
            public void onTransceiverUnableToConnect() {
                runOnUiThread(()-> Toast.makeText(MainActivity.this, "Unable to Connect", Toast.LENGTH_LONG).show());
            }

            @Override
            public void onStatusChange(int status) {
                if(status==2){
                    mCustomView.setAccess(true);
                }else mCustomView.setAccess(false);
                runOnUiThread(()-> showStatus.setText(mOscilloManager.getStatus()));
            }
        });
    }

    /**
     *
     La methode est appelee si l'activite est recreee parce que l'appareil a subi une rotation ou que l'activite a ete detruite.
     * @param savedInstanceState : Objet bundle contenant l'etat enregistre, qui peut etre utilise pour restaurer l'etat precedent de l'activite
     */
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        mCustomView.setmCurrentValue(savedInstanceState.getFloat("currentValue"));
        super.onRestoreInstanceState(savedInstanceState);
    }

    /**
     *methode appele avant que l'activite ne soit detruite
     * @param outState Objet Bundle, qui peut etre utilise pour enregistrer l'etat des elements de l'interface utilisateur de l'activite.
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putFloat("currentValue",mCustomView.getmCurrentValue());
        super.onSaveInstanceState(outState);
    }

    /**
     * methode de l'interface OnCustomViewChangeListener
     * @param value
     */
    @Override
    public void onValueChanged(float value) {

        mTvValue.setText(String.format("%.0f", value)+"%");
        mOscilloManager.setCalibrationDutyCycle(value);
    }

    /**
     * methode de l'interface OnCustomViewChangeListener
     * @param value
     */
    @Override
    public void onDoubleClick(float value) {
        mTvValue.setText(String.format("%.0f", value)+"%");
    }

    /**
     * installation du menu
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);

    }

    /**
     * methode appele lorsqu'un element du menu d'options ou de la barre d'action est selectionne.
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int menuItem = item.getItemId();
        switch (menuItem){
            case R.id.bt:
                verifyBtPermissions();
        }
        return true;
    }

    /**
     * methode appele lorsque l'utilisateur repond a une demande d'autorisations
     * @param requestCode utilise pour faire correspondre la demande avec la reponse correspondante
     * @param permissions tableau qui contient les autorisations demandees
     * @param grantResults tableau contient les resultats de la demande pour chaque autorisation.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERM_CODE: {
                if ( grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * methode utilisee pour verifier et demander des autorisations pour les fonctionnalites liees a Bluetooth sur un appareil Android
     */
    private void verifyBtPermissions() {
        mbt_adapter= BluetoothAdapter.getDefaultAdapter();
        if (mbt_adapter == null) {
            Toast.makeText(getApplicationContext(), "Il n'y a pas un adaptateur Bluetooth", Toast.LENGTH_SHORT).show();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},PERM_CODE);
            } else {
                activateAdapter();
            }
        }
    }

    /**
     *
     methode utilisee pour activer l'adaptateur Bluetooth sur un appareil Android.
     */
    private void activateAdapter(){
        if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {

            intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activityResultLauncher_Bluetooth.launch(intent);

        } else {
            //I can go the Second Activity directly (affichage des bluetooth devices)
            intent = new Intent(MainActivity.this, BtconnectActivity.class);
            activityResultLauncher.launch(intent);
        }
    }

    /**
     * permet de lancer l'activite BtconnectActivity si le bluetooth est enable
     */
    final ActivityResultLauncher<Intent> activityResultLauncher_Bluetooth = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if(mbt_adapter.isEnabled()) {
                        intent = new Intent(MainActivity.this, BtconnectActivity.class);
                        activityResultLauncher.launch(intent);
                    }
                    else {
                        Toast.makeText(MainActivity.this, "Le bluetooth doit etre active!", Toast.LENGTH_LONG).show();
                    }
                }
            }
    );

    /**
     * recuperation des donnees du device choisi
     */
    final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode()== Activity.RESULT_OK){
                        if(result.getData() !=null){
                            address=result.getData().getStringExtra("device");
                            mOscilloManager.getmBTManager().connect(address);
                        }
                    }
                }
            }
    );

    /**
     * methode appelee lorsque l'utilisateur appuie sur le bouton de retour de son appareil
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mOscilloManager.disconnect();
    }
}
