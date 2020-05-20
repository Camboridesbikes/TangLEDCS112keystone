package app;

import java.net.*;
import java.nio.charset.StandardCharsets;

import app.MeshConnect.ConnectRunnable;
import app.MeshConnect.ReceiveRunnable;
import app.MeshConnect.SendRunnable;
//import jdk.javadoc.internal.tool.Start;

import java.io.*;

public class MeshConnect {
    private static String meshIp;
    private static int meshPort;

    private static Socket meshSocket;

    //tcp thread flag
    private static boolean receiveThreadRunning = false;

    //Runnables for send/receive data?
    private static SendRunnable sendRunnable;

    //Thread to send
    private static Thread sendThread;
    //Thread to recieve
    private static Thread receiveThread;

    static void Connect(String ip, int port){
        meshIp = ip;
        meshPort = port;
        //start new thread
        new Thread(new ConnectRunnable()).start();
   }

   static void WriteData(byte[] data){
       startSending();
       sendRunnable.Send(data);
   }

   //start thread to receive data from the mesh
   private static void startReceiving(){
        ReceiveRunnable receiveRunnable = new ReceiveRunnable(meshSocket);
        receiveThread = new Thread(receiveRunnable);
        receiveThread.start();
   }

   private static void startSending(){
       sendRunnable = new SendRunnable(meshSocket);
        sendThread = new Thread(sendRunnable);
        sendThread.start();
   }

   static class ConnectRunnable implements Runnable{
        public void run(){
            try{
                 System.out.println("Connecting...") ;
            
                 //convert ip to use w/ socket
                InetAddress meshAddress = InetAddress.getByName(meshIp);
            
                    //Create new socket
                 meshSocket = new Socket();
                meshSocket.setKeepAlive(true);
                meshSocket.setReuseAddress(true);

                //Start connection w/ 5000 ms timeout
                meshSocket.connect(new InetSocketAddress(meshAddress, meshPort), 5000);

                System.out.println("Connected");

                startReceiving();

                //send node Sync request
                MeshHandler.nodeSyncRequest();

            }catch(Exception e){
            System.out.println("connection failed: " + e);
            }
        }
    } 
    
    // static void sendMyBroadcast(String action, String msgRecieved){
        
    // }

    static class ReceiveRunnable implements Runnable {
        private final Socket socket;
        private InputStream input;

        ReceiveRunnable(Socket mesh){
            socket = mesh;
            try{
                input = socket.getInputStream();
            }catch (Exception e){
                System.out.println("Receive Runnable failed");
            }
        }

        @Override
        public void run(){
            System.out.println("Receiving Started");
            while(!Thread.currentThread().isInterrupted()){
                if(!receiveThreadRunning){
                    receiveThreadRunning = true;
                }
                try{
                    //new 8.2kb byte to read 8 bytes at a time?
                    byte[] buffer = new byte[8192];
                    
                    //read first int --> defines length of data to expect
                    int readLen = input.read(buffer,0,buffer.length);
                    if(readLen > 0){
                        byte[] data = new byte[readLen];
                        //not sure what's going on here. extracting data from the buffer???
                        System.arraycopy(buffer,0, data, 0, readLen);
                        data[readLen-1] = 0;

                        String rcvdMsg = new String(data, StandardCharsets.UTF_8);
                        int realLen = rcvdMsg.lastIndexOf("}");
                        rcvdMsg = rcvdMsg.substring(0, realLen+1);
                        //print msg from mesh
                        System.out.println("Recieved " + readLen + " bytes: " + rcvdMsg);

                        System.out.println("Data received!");

                        //stop recieve thread after data has been recieved
                        receiveThread.interrupt();
                        receiveThreadRunning = false;
                
                    }

                } catch (Exception e){
                    System.out.println("Could not receive data: " + e);
                }
            }
        }
    }

    static class SendRunnable implements Runnable {
        byte[] data;
        private OutputStream out;
        private boolean hasMessage = false;

        SendRunnable(Socket mesh){
            try {
                this.out = mesh.getOutputStream();
            }catch (IOException e) {
                System.out.println("Start sendRunnable failed." + e);
            }
        }

            void Send(byte[] bytes) {
                this.data = bytes;
                this.hasMessage = true;
            }

            @Override
            public void run() {
                System.out.println("sending started");

                if(this.hasMessage){
                    try{
                        //sned data
                        this.out.write(data,0,data.length);
                        this.out.write(0);
                        //clear output buffer?
                        this.out.flush();

                    }catch(IOException e){
                        System.out.println("sending failed: " + e);
                        
                    }
                    this.hasMessage = false;
                    this.data = null;
                    System.out.println("Sent!");
                }
                System.out.println("sending stopped");
            }
        
        
    }
}


