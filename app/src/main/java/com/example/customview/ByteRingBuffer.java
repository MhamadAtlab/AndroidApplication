package com.example.customview;

import androidx.annotation.NonNull;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;

public class ByteRingBuffer {

    /**
     * array(buffer) utiliser pour le stockage des donnees
     */
    private byte [] buff;
    /**
     * Position d'ecriture
     */
    private int wPos;
    /**
     * position de lecture
     */
    private int rPos;
    /**
     * indique un buffer full car wPos = rPos est insuffisant pour cela
     */
    private boolean full;

    /**
     *   Constructeur
     *   @param size : taille du buffer circulaire en octet
     */
    public ByteRingBuffer(int size) {
        buff=new byte[size];
        wPos=rPos=0;
        full=false;
    }

    /**
     *   Ajout d'un octet dans le buffer
     *   @param b : octet a ajouter
     */
    public synchronized void put(byte b) throws BufferOverflowException{
        int x;
        if(full) throw new BufferOverflowException();
        else {
            x=Increment(wPos);
            if(x==rPos) {
                full=true;
            }
            buff[wPos]=b;
            wPos=x;
        }
    }

    /**
     *   Ecriture des octets d'un tableau dans le buffer
     *   @param bArray : tableau d'octets a ecrire
     */
    public synchronized void put(byte[] bArray) throws BufferOverflowException {
        for (byte b:bArray) {
            put(b);
        }
    }

    /**
     *   Nombre d'octets disponibles dans le buffer
     *   @return nombre d'octets presents dans le buffer
     */
    public int bytesToRead(){
        int count;
        int x;
        if (full) return buff.length;
        else{
            count=0;
            x=rPos;
            while (x!=wPos){
                count++;
                x=Increment(x);
            }
            return count;
        }
    }

    /**
     *   Lecture d'un octet du buffer
     *   @return octet lu
     */
    public synchronized byte get() throws BufferUnderflowException {//synchronized method
        byte b;
        if(rPos==wPos&&!full) {
            throw new BufferUnderflowException();
        }
        else{
            full=false;
            b=buff[rPos];
            rPos=Increment(rPos);
        }
        return b;
    }

    /**
     *   Lecture de tous les octets presents dans le buffer
     *   @return tableau d'octets lu
     */
    public synchronized byte[] getAll() throws BufferUnderflowException{//synchronized method
        int n=bytesToRead();
        byte[] arr;
        if (n==0){
            throw new BufferUnderflowException();
        }else{
            arr=new byte[n];
            for (int i = 0; i < n; i++) {
                arr[i] = this.get();
            }
            return arr;
        }
    }

    /**
     * incrementer la position des variables de lecture et d'ecriture
     * @param x
     * @return
     */
    private int Increment(int x){
        if (x == (buff.length - 1)) return 0;
        else return x + 1;
    }

    /**
     *   Indication d'information sur le buffer (utilise principalement pour le debuggage)
     *   @return Chaine contenant des informations d'etat du buffer (taille, nombre d'elements presents, position des pointeurs ...)
     */
    @NonNull
    @Override
    public String toString() {
        return "Buffer circulaire : \n"
                +" * Taille = "+buff.length +"\n"
                +" * Nombre d'elements presents = "+bytesToRead() +"\n"
                +" * Position de lecture : "+rPos +"\n"
                +" * Position d'ecriture : "+wPos;
    }

    /**
     * getter
     * @return un boolean qui indique si le buffer est full ou non
     */
    public boolean isFull(){
        return full;
    }
}
