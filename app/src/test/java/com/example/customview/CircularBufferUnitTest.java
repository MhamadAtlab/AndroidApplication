package com.example.customview;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.security.PublicKey;

public class CircularBufferUnitTest {
    private ByteRingBuffer myBuffer;

    @Before
    public void setUp() {
        myBuffer = new ByteRingBuffer(3);
    }

    @Test
    public void testFrameProcessor(){
        FrameProcessor frame=new FrameProcessor();
        byte [] values = new byte[]{1,6,4,5,2,10,4,3,5,6,2,4,1};
        byte[] data= frame.toFrame(values);
        System.out.println("###### ENCODING");
        for (byte b:data)
            System.out.println(b);

        System.out.println("###### STATUS BEFORE DECODING");
        System.out.print("DATA READY="+frame.framePending()+"\n");

        System.out.println("###### DECODING");
        for (byte obj : data){
            frame.fromFrame(obj);
            System.out.print("DATA READY="+frame.framePending()+"\n");
            System.out.println("------");
        }
        System.out.println( frame.getData());
    }
    @Test
    public void testFrameProcessor_erreur(){
        FrameProcessor frame=new FrameProcessor();
        byte [] values = new byte[]{1,2,3,1};
        byte[] data= frame.toFrame(values);
//        byte [] right_data= new byte[]{5,0,6,10,1,2,3,1,-11,4}
        byte [] wrong_data= new byte[]{5,0,6,10,1,2,3,1,-10,4};

        System.out.println("###### ENCODING");
        for (byte b:data)
            System.out.println(b);

        System.out.println("###### STATUS BEFORE DECODING");
        System.out.print("DATA READY="+frame.framePending()+"\n");

        System.out.println("###### DECODING WITH WRONG DATA");
        for (byte obj : wrong_data){
            frame.fromFrame(obj);
            System.out.print("DATA READY="+frame.framePending()+"\n");
            System.out.print("Decoder State="+frame.getmDecoderState()+"\n");
            System.out.println("------");
        }
        System.out.println("Decoded Data "+frame.getData());
        // le decoder state revient a l'état initial RX_IDLE;

    }

    @Test
    public void whereInputDoesNotExceedCapacity() {

        System.out.println("-----------where Input Does Not Exceed Capacity---------------------------------------------");

        byte[] data = { 1, 2};

        try{
            myBuffer.put(data);
        }catch (BufferOverflowException e){
            System.out.println("------ Buffer Overflow Exception : Buffer is full !");
        }

        System.out.println(myBuffer);

        assertFalse(myBuffer.isFull());

        try {
            assertEquals(data[0], myBuffer.get());
            assertEquals(data[1], myBuffer.get());
        }catch (BufferUnderflowException e){
            System.out.println("------ Buffer Underflow Exception : Buffer is Empty !");
        }

        System.out.println(myBuffer);

        try{
            myBuffer.put(data);
        }catch (BufferOverflowException e){
            System.out.println("------ Buffer Overflow Exception : Buffer is full !");
        }

        System.out.println(myBuffer);

        assertFalse(myBuffer.isFull());

        try {
            assertEquals(data[0], myBuffer.get());
            assertEquals(data[1], myBuffer.get());
        }catch (BufferUnderflowException e){
            System.out.println("------ Buffer Underflow Exception : Buffer is Empty !");
        }

        System.out.println(myBuffer);
        System.out.println("-----------------------------------------------------------------------------");
    }

    @Test
    public void whereInputEqualsCapacity() {

        System.out.println("------------------where Input Equals Capacity-----------------------------------------------------------");

        byte[] data = { 1, 2, 3 };
        try{
            myBuffer.put(data[0]);
            myBuffer.put(data[1]);
            myBuffer.put(data[2]);
        }catch (BufferOverflowException e){
            System.out.println("------ Buffer Overflow Exception : Buffer is full !");
        }

        System.out.println(myBuffer);

        assertTrue(myBuffer.isFull());

        try {
            assertEquals(data[0], myBuffer.get());
            assertFalse(myBuffer.isFull());
            assertEquals(data[1], myBuffer.get());
            assertEquals(data[2], myBuffer.get());
            assertFalse(myBuffer.isFull());
        }catch (BufferUnderflowException e){
            System.out.println("------ Buffer Underflow Exception : Buffer is Empty !");
        }

        System.out.println(myBuffer);
        System.out.println("-----------------------------------------------------------------------------");
    }

    @Test
    public void whereInputExeedsCapacityByOne() {

        System.out.println("-------------------where Input Exeeds Capacity By One----------------------------------------------------------");

        byte[] data = { 1, 2, 3, 4 };

        try{
            myBuffer.put(data);
        }catch (BufferOverflowException e){
            System.out.println("------ Buffer Overflow Exception : Buffer is full !");
        }

        System.out.println(myBuffer);

        assertTrue(myBuffer.isFull());

        try {
            assertEquals(data[0], myBuffer.get());
            assertFalse(myBuffer.isFull());
            assertEquals(data[1], myBuffer.get());
            assertEquals(data[2], myBuffer.get());
        }catch (BufferUnderflowException e){
            System.out.println("------ Buffer Underflow Exception : Buffer is Empty !");
        }

        System.out.println(myBuffer);
        System.out.println("-----------------------------------------------------------------------------");
    }


    @Test
    public void whereInputExeedsCapacityByTwo() {

        System.out.println("-------------------where Input Exeeds Capacity By Two----------------------------------------------------------");

        byte[] data = { 1, 2, 3, 4, 5 };

        try{
            myBuffer.put(data);
        }catch (BufferOverflowException e){
            System.out.println("------ Buffer Overflow Exception : Buffer is full !");
        }

        System.out.println(myBuffer);

        assertTrue(myBuffer.isFull());

        try {
            assertEquals(data[0], myBuffer.get());
            assertFalse(myBuffer.isFull());
            assertEquals(data[1], myBuffer.get());
            assertEquals(data[2], myBuffer.get());
            assertFalse(myBuffer.isFull());
        }catch (BufferUnderflowException e){
            System.out.println("------ Buffer Underflow Exception : Buffer is Empty !");
        }

        System.out.println(myBuffer);
        System.out.println("-----------------------------------------------------------------------------");
    }

    @Test
    public void whereEmptyBuffer() {

        System.out.println("-------------------where Empty Buffer----------------------------------------------------------");

        byte[] data = { 1, 2};

        try{
            myBuffer.put(data);
        }catch (BufferOverflowException e){
            System.out.println("------ Buffer Overflow Exception : Buffer is full !");
        }

        System.out.println(myBuffer);

        assertFalse(myBuffer.isFull());

        try {
            System.out.println("read 1 : "+myBuffer.get());
            System.out.println("read 2 : "+myBuffer.get());
            System.out.println("read 3 : "+myBuffer.get());
        }catch (BufferUnderflowException e){
            System.out.println("------ Buffer Underflow Exception : Buffer is Empty !");
        }
        System.out.println(myBuffer);
        System.out.println("-----------------------------------------------------------------------------");

    }


    @Test
    public void testGetAll() {

        System.out.println("-------------------test getAll----------------------------------------------------------");

        byte[] data = { 1, 2};

        try{
            myBuffer.put(data);
        }catch (BufferOverflowException e){
            System.out.println("------ Buffer Overflow Exception : Buffer is full !");
        }

        System.out.println(myBuffer);
        System.out.println("bytesToRead : "+myBuffer.bytesToRead());

        assertFalse(myBuffer.isFull());

        try {
            data=myBuffer.getAll();
            System.out.println("read 1 : "+data[0]);
            System.out.println("read 2 : "+data[1]);
            data=myBuffer.getAll();
            System.out.println("read 3 : "+data[0]);
            System.out.println("read 4 : "+data[1]);
        }catch (BufferUnderflowException e){
            System.out.println("------ Buffer Underflow Exception : Buffer is Empty !");
        }
        System.out.println(myBuffer);
        System.out.println("bytesToRead : "+myBuffer.bytesToRead());
        System.out.println("-----------------------------------------------------------------------------");

    }
}
