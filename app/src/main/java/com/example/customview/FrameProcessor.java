package com.example.customview;

import java.util.Arrays;

public class FrameProcessor {

    /**
     * constante indiquant l'entete du trame
     */
    private final static byte FRAME_HEADER = 0x05;

    /**
     * constante indiquant la queue du trame
     */
    private final static byte FRAME_TAIL = 0x04;

    /**
     * constante d'echappement
     */
    private final static byte FRAME_ESCAPE = 0x06;

    /**
     * Constantes indiquant l'etat du decodeur
     */
    private final static byte RX_IDLE = 0;
    private final static byte RX_LENGTH_H = 1;
    private final static byte RX_LENGTH_L = 2;
    private final static byte RX_PAYLOAD = 3;
    private final static byte RX_CHECK_BYTE = 4;
    private final static byte RX_PENDING = 5;

    /**
     * tableau pour enregister les commandes codees par la fontion toFrame(byte[] data)
     */
    private byte[] wFrame;

    /**
     * index d'ecriture
     */
    private int indexWriting;

    /**
     * byte precisant l'etat du decodeur
     */
    private byte mDecoderState = 0;

    /**
     * getter
     * @return mDecoderState : etat du decodeur
     */
    public byte getmDecoderState() {
        return mDecoderState;
    }


    /**
     * instance de la classe Data
     */
    private Data frameData;

    /**
     *la somme des octets contenus dans PAYLOAD et LENGTH
     */
    private byte sum_tram=0;

    /**
     * variable utilisee durant le decodage (taille_tram=lengthH+lengthL)
     */
    private int taille_tram=0;

    /**
     * boolean utilisee dans la fonction "fromFrame(byte data)" quand il y a un echappement
     */
    private boolean frame_escape=false;

    /**
     * boolean pour savoir si le decodage est termine
     */
    private boolean readyCommande=false;


    /**
     * constructor
     */
    public FrameProcessor(){
        frame_escape=false;
        readyCommande=false;
        mDecoderState=RX_IDLE;
        frameData =new Data();
    }

    /**
     * Conversion d'une trame en commande
     *
     * @param data : octet reçu
     */
    public void fromFrame(byte data) {

        switch (mDecoderState){
            case RX_IDLE:
                if (data==FRAME_HEADER){
                    mDecoderState = RX_LENGTH_H;
                    frame_escape=false;
                    sum_tram=0;
                    taille_tram=0;
                }
                break;

            case RX_LENGTH_H:
                if(frame_escape==true){
                    mDecoderState = RX_LENGTH_L;
                    sum_tram+=data - FRAME_ESCAPE;
                    taille_tram += (data - FRAME_ESCAPE)*256;
                    frame_escape=false;
                }
                else {
                    if (data != FRAME_ESCAPE) {
                        mDecoderState = RX_LENGTH_L;
                        sum_tram += data;
                        taille_tram += data*256;
                    } else {
                        frame_escape = true;
                    }
                }
                break;

            case RX_LENGTH_L:
                if(frame_escape==true){
                    sum_tram+=data - FRAME_ESCAPE;
                    taille_tram += data - FRAME_ESCAPE;
                    frame_escape=false;
                    frameData =new Data(taille_tram);
                    if(taille_tram==0) mDecoderState=RX_IDLE;
                    else mDecoderState = RX_PAYLOAD;
                } else {
                    if(data!=FRAME_ESCAPE){
                        sum_tram+=data;
                        taille_tram+=data;
                        frameData =new Data(taille_tram);
                        if(taille_tram==0) mDecoderState=RX_IDLE;
                        else mDecoderState = RX_PAYLOAD;
                    } else {
                        frame_escape=true;
                    }
                }
                break;

            case RX_PAYLOAD:
                if(frame_escape==true){
                    frame_escape=false;
                    taille_tram--;
                    sum_tram+=data - FRAME_ESCAPE;
                    frameData.addData((byte) (data - FRAME_ESCAPE));
                }else {
                    if(data!=FRAME_ESCAPE){
                        frameData.addData(data);
                        sum_tram+=data;
                        taille_tram--;
                    } else
                    {
                        frame_escape=true;
                    }
                }

                if(taille_tram==0){
                    mDecoderState = RX_CHECK_BYTE;
                }
                break;

            case RX_CHECK_BYTE:
                if(frame_escape==true){
                    frame_escape=false;
                    sum_tram = (byte) ((256-sum_tram) % 256);

                    if(sum_tram==(data-FRAME_ESCAPE)){
                        mDecoderState = RX_PENDING;
                    } else{
                        mDecoderState= RX_IDLE;
                    }
                }else {
                    if(data!=FRAME_ESCAPE) {
                        sum_tram = (byte) ((256 - sum_tram) % 256);
                        if (data == sum_tram) {
                            mDecoderState = RX_PENDING;
                        } else {
                            mDecoderState = RX_IDLE;
                        }
                    }else {
                        frame_escape=true;
                    }
                }
                break;

            case RX_PENDING:
                if (data == FRAME_TAIL){
                    readyCommande = true;
                    frameData.setFrameValidity(true);
                }
                mDecoderState=RX_IDLE;
                break;
        }

    }


    /**
     * Transformation d'une commande en trame
     * @param data : table du payload
     * @return trame
     */
    public byte[] toFrame(byte[] data){
        int frameSize;
        int dataLength=data.length;
        int ctrl;
        byte lengthH,lengthL;
        frameSize=dataLength+5;

        lengthL=(byte) (dataLength%256);
        dataLength/=256;
        lengthH=(byte) (dataLength%256);
        ctrl=lengthH+lengthL;
        for (byte b:data) {
            ctrl+=b;
            if(b==FRAME_ESCAPE||b==FRAME_TAIL||b==FRAME_HEADER) frameSize++;
        }
        ctrl=ctrl%256;
        ctrl=256-ctrl;
        if(lengthH==FRAME_ESCAPE||lengthH==FRAME_TAIL||lengthH==FRAME_HEADER) frameSize++;
        if(lengthL==FRAME_ESCAPE||lengthL==FRAME_TAIL||lengthL==FRAME_HEADER) frameSize++;
        if(ctrl==FRAME_ESCAPE||ctrl==FRAME_TAIL||ctrl==FRAME_HEADER) frameSize++;

        wFrame=new byte[frameSize];//initialisation de trame
        indexWriting =0;
        wFrame[indexWriting++]=FRAME_HEADER;
        writeByte(lengthH);
        writeByte(lengthL);

        for (byte b:data) {
            writeByte(b);
        }
        writeByte((byte) ctrl);
        wFrame[indexWriting]=FRAME_TAIL;
        return wFrame;
    }

    /**
     * metohde qui permet d'ecrir dans le tableau qui permet d'enregister les commandes codees par la fontion toFrame
     * @param b
     */
    private void writeByte(Byte b){
        if(b==FRAME_ESCAPE||b==FRAME_TAIL||b==FRAME_HEADER){
            wFrame[indexWriting++]=FRAME_ESCAPE;
            wFrame[indexWriting]= (byte) (FRAME_ESCAPE+b);
        }else wFrame[indexWriting]=b;
        indexWriting++;
    }

    /**
     *
     * @return : vrai lorsqu'une trame est totalement reçue
     */
    public boolean framePending(){
        return readyCommande;
    }


    /**
     * Recuperation de la trame decodee sous la forme d'un objet Data
     *
     * @return trame
     */
    public Data getData(){
        if(framePending()){
            readyCommande=false;
        }
        return frameData;
    }


    /**
     * Classe de stockage des commandes d'une trame decodee
     */
    public class Data{

        byte [] com;
        int wCom;
        boolean FrameValidity;

        /**
         * setter
         * @param identifiant la cle d'identification de la trame
         */
        public void setIdentifiant(byte identifiant) {
            this.identifiant = identifiant;
        }

        byte identifiant;

        /**
         * getter
         * @return l'identifiant de la trame
         */
        public byte getIdentifiant() {
            return identifiant;
        }

        /**
         * constructeur
         */
        public Data(){
            FrameValidity =false;
            com=new byte[1];
            wCom=0;
        }

        /**
         * constructeur
         * @param size : taille du tableau
         */
        public Data(int size){
            com = new byte[size-1];
            wCom=0;
            FrameValidity =false;
        }

        /**
         * setter
         * @param validity : validite de la trame
         */
        public void setFrameValidity(boolean validity) {
            this.FrameValidity = validity;
        }

        /**
         * getter
         * @return validite de la trame
         */
        public boolean getFrameValidity() {
            return FrameValidity;
        }

        /**
         * ajouter un octet dans la trame
         * @param data octet ajoute
         */
        public void addData(byte data){
            if(wCom==0){
                identifiant=data;
                wCom++;
            }else {
                com[wCom-1]=data;
                wCom++;
            }
        }

        /**
         *
         * @return tous les octets de la trame sans l'identifiant
         */
        public byte[] getParam(){
            if (getFrameValidity()) {
                return this.com;
            } else return null;
        }

        public String toString(){
            String x="Data ";
            if(getFrameValidity()) {
                x+="validee ";
                x+=": id = "+getIdentifiant()+" -- parametres = "+Arrays.toString(getParam());
            }
            else x+="non validee ";
            return x;
        }

    }
}