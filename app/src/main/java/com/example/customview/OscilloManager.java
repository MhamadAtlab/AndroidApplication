package com.example.customview;

import static com.example.customview.Transceiver.STATE_CONNECTED;
import static com.example.customview.Transceiver.STATE_CONNECTING;
import static com.example.customview.Transceiver.STATE_NO_CONNECTION;

public class OscilloManager {

    /**
     * la seule instance de la classe OscilloManager
     */
    private static OscilloManager instance;

    /**
     * instance du classe BTManager
     */
    private Transceiver mBTManager;

    /**
     * getter pour la variable mBTManager
     * @return
     */
    public Transceiver getmBTManager() {
        return mBTManager;
    }

    /**
     * contructeur
     */
    private OscilloManager() {
        mBTManager = new BTManager();
    }


    /**
     * La methode getInstance() peut etre utilisee pour obtenir l'instance unique de la classe
     * @return l'instance unique de la classe
     */
    public static OscilloManager getInstance() {
        if (instance == null) {
            instance = new OscilloManager();
        }
        return instance;
    }

    /**
     * setter pour l'interface transceiver listener
     * @param transceiverListener
     */
    public void setTransceiverListener(Transceiver.TransceiverListener transceiverListener){
        mBTManager.setTransceiverListener(transceiverListener);
    }


    /**
     * getter pour le statut de la connexion
     * @return le status de la connxion
     */
    public String getStatus(){
        switch (mBTManager.getState()){
            case STATE_CONNECTED:
                return "CONNECTED";
            case STATE_CONNECTING:
                return "CONNECTING";
            default:
                return "NOT CONNECTED";
        }
    }

    /**
     * methode utilise pour envoyer les donnees a l'oscilloscope
     * @param value : valeur a envoyer
     */
    public void setCalibrationDutyCycle(float value){
        byte [] data =new byte[2];
        data[0]=10;
        data[1]= (byte) value;
        mBTManager.send(data);
    }

    /**
     * methode qui permet la deconnexion du bluetooth
     */
    public void disconnect(){
        mBTManager.disconnect();
    }
}
