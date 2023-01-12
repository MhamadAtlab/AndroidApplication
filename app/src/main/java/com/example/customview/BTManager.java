package com.example.customview;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.util.Arrays;
import java.util.UUID;

/**
 * La classe BTManager gere la connexion BT ainsi que l'echange de donnees.
 */
public class BTManager extends Transceiver {

    /**
     * Unique UUID for this application (set the SPP UUID because expected incomming connection are of this type)
     */
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    /**
     * reference vers l'adaptateur
     */
    private final BluetoothAdapter mAdapter;

    /**
     * the Bluetooth socket is the connection point that allows an application to exchange data
     * with another Bluetooth device via InputStream and OutputStream.
     */
    private BluetoothSocket mSocket = null;

    /**
     * Thread de connection
     */
    private ConnectThread mConnectThread = null;

    /**
     * Thread de communication
     */
    private WritingThread mWritingThread;

    /**
     * gestionnaire de trames
     */
    private FrameProcessor frame_Processor;


    /**
     * Constructeur
     */
    public BTManager() {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        frame_Processor = new FrameProcessor();
    }



    /**
     *  cette methode permet :
     *     1 - Annuler les eventuelles demandes de connexion en cours
     *     2 - Creer un Thread de connexion
     *     3 - Placer le label d'etat en "Connexion en cours"
     *     4 - Lancer le thread
     *     Get a BluetoothDevice object for the given Bluetooth hardware address.
     *
     * @param id bluetooth address
     */
    @Override
    public void connect(String id) {
        BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(id);
        disconnect();
        mConnectThread = new ConnectThread(device);
        setState(STATE_CONNECTING);
        mConnectThread.start();
    }



    /**
     *  cette methode permet :
     *   1 - Fermer le socket (Attention : genere une exception checkee)
     *   2 - Liberer les references de thread (la boucle des threads devra checker ces refs
     *   comme condition d'arret du thread).
     */
    @Override
    public void disconnect() {
        if (mConnectThread != null) {
            mConnectThread.close();
            mConnectThread = null;
            setState(STATE_NO_CONNECTION);
        }
    }


    /**
     *   Conversion des commandes en trames puis transmission par buffer circulaire
     *		Reimplementer la methode send pour qu'elle realise la conversion des commandes en trames puis qu'elle
     *  les insere dans le buffer circulaire a l'aide de la methode write du thread d'ecriture.
     * @param data tableaux des commandes
     */
    @Override
    public void send(byte[] data) {
        if(mSocket!=null && mWritingThread!=null) {
            mWritingThread.write(frame_Processor.toFrame(data));
        }

    }


    /**
     *  Creation d'un thread de connexion
     *      1 - Recuperation d'un socket Bluetooth pour le peripherique vise.
     *      Attention :  la methode createRfcommSocketToServiceRecord(UUID) lance un
     *      exeption IOException checkee : il est donc necessaire de l'intercepter.
     *      2 - Implementation de la methode Run qui doit :
     *          a - Eventuellement stopper une decouverte deja lancee
     *          b - se connecter au socket (idem concernant les exceptions)
     *          c - instancier et lancer les threads de communication WritingThread et ReadingThread
     */

    private class ConnectThread extends Thread {

        /**
         * Constructeur
         * Get a BluetoothSocket for a connection with the given BluetoothDevice
         * @param device The BluetoothDevice to connect
         */
        @SuppressLint("MissingPermission")
        public ConnectThread(BluetoothDevice device) {

            try {
                mSocket= device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @SuppressLint("MissingPermission")
        public void run(){
            mWritingThread=new WritingThread();
            mAdapter.cancelDiscovery();

            try {
                mSocket.connect();
                if(mSocket.isConnected()){
                    setState(STATE_CONNECTED);
                }
                mWritingThread.start();
            } catch (IOException e) {
                connectionFailed();
            }
        }
        public void close(){
            try {
                mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


}

/************************************************************************************
 /
 /                          THREADS de COMMUNICATION
 /
 /***********************************************************************************/


 /**  une classe heritee de Thread permettant de gerer de maniere non
   *           bloquante l'envoi des commande vers l'oscilloscope.
   *            1 - Dans le constructeur, recuperer la reference sur le flux sortant du socket via
   *             getOutputStream (Attention : getInputStream lance une exeption Checkee) et initialiser
   *             un buffer circulaire (de 1024 octets par exemple).
   *            2 - ecrire la methode run qui prend les octets disponibles dans le buffer circulaire
   *                et les transmet via la methode write d'OutputStream (peu importe si c'est bloquant),
   *            3 - definir une methode write qui ecrit un tableau d'octets dans le buffer circulaire
   */

private class WritingThread extends Thread {
    ByteRingBuffer buffer_circulaire ;
    OutputStream outputStream;
    FrameProcessor frameProcessor;
    byte[] value;

     /**
      * Constructeur
      */
    public WritingThread() {
        buffer_circulaire = new ByteRingBuffer(1024);
        try {
            outputStream = mSocket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

     /**
      * recuperer toutes les donnees du buffer_circulaire
      * ecrire ces donnees dans outputStream
      * outputStream :
      */
    @Override
    public void run() {
        while (true) {
                try
                {
                    Log.i("Data sended not:", "not yet in writing thread");
                    value = buffer_circulaire.getAll();
                    Log.i("Data sended not:", String.valueOf(value));
                }
                catch (BufferUnderflowException e)
                {
                    Log.i("UnderflowException", "Buffer Underflow Exception : Buffer is empty");

                }
                try{
                    if(value!=null){
                        outputStream.write(value);
                        value=null;
                    }else outputStream.write(0);
                }
                catch (IOException et){
                    connectionLost();
                    break;
                }
        }
    }

    /**
     * ecrire les data dans le buffer_circulaire
     * @param data : trames codees
     */
    public void write(byte [] data){
        try {
            buffer_circulaire.put(data);
        }catch (BufferOverflowException e){
            Log.i("BufferOverFlowExcepton","Buffer Overflow Exception : Buffer is full");
        }
    }
}

}