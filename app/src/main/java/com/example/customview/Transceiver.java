package com.example.customview;

public abstract class Transceiver{

    /**
     * etat de connexion
     */
    private int state=STATE_NO_CONNECTION;


    /**
     * Constante indiquant l'appareil n'est pas connecter
     */
    public static final int STATE_NO_CONNECTION = 0;
    /**
     * constante indiquant que l'appareil en train d'etablir une connexion
     */
    public static final int STATE_CONNECTING = 1; // now initiating an outgoing connection
    /**
     * Constante indiquant que l'appareil est connectee
     */
    public static final int STATE_CONNECTED = 2;  // now connected to a remote device

    /**
     * une instance de l'interface TransceiverListener
     */
    private TransceiverListener mTransceiverListener;

    /**
     * getter
     * @return le statut de la connexion
     */
    public int getState() {
        return state;
    }

    /**
     * setter
     * @param state : statut de la connexion
     */
    public void setState(int state) {

        this.state = state;
        mTransceiverListener.onStatusChange(state);
    }

    /**
     * setter
     * @param transceiverListener
     */
    public void setTransceiverListener(TransceiverListener transceiverListener){
        mTransceiverListener=transceiverListener;
    }

    /**
     * methode lancee lorsque la connexion est perdue
     */
    public void connectionLost(){
        setState(STATE_NO_CONNECTION);
        mTransceiverListener.onTransceiverConnectionLost();
    }

    /**
     * methode lancee lorsque la connexion a echoue
     */
    public void connectionFailed(){
        setState(STATE_NO_CONNECTION);
        mTransceiverListener.onTransceiverUnableToConnect();
    }

    /**
     * methode abstrait qui permet la connexion
     * @param id identifiant du peripherique a connecter
     */
    public abstract void connect(String id);


    /**
     * methode abstrait pour se deconnecter
     */
    public abstract void disconnect();

    /**
     * methode abstrait pour envoyer les commandes
     * @param data liste qui contient la trame codee
     */
    public abstract void send(byte[] data);

    /**
     * une interface qui gere la communication avec la classe OscilloManager
     */
    public interface TransceiverListener {
        public void onTransceiverDataReceived();
        public void onTransceiverConnectionLost();
        public void onTransceiverUnableToConnect();
        void onStatusChange(int status);

    }

}
