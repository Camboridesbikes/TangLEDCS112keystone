/**
 *  MeshConnect class is responsible for the interaction with the mesh network that the 
 * Lanterns inhabit. The class takes an ip and a port in order to connect to the appropriate 
 * network. The class holds a few inner classes that set up the socket as well as the i/o 
 * streams accordingly. 
 * 
 * The most notable inner class is the MeshHandler class. This class processes and handles 
 * that data required for connecting to the mesh.
 * 
 * The code in this class is built off of painlessMeshAndroid (https://gitlab.com/painlessMesh/painlessmesh_android) by Bernd Giesecke.
 * 
 *  @Author: Cameron Hattendorf
 *  @Version: 2.1
 */

package app;

import java.net.*;
import java.nio.charset.StandardCharsets;

import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

import java.text.DecimalFormat;
// import java.util.Random;

import org.joda.time.DateTime;
import org.json.*;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.*;


public class MeshConnect {
    public static final String DEFAULT_IP = "10.211.205.1"; // 10.251.129.1
    public static final int DEFAULT_PORT = 5555;

    private static String meshIp;
    private static int meshPort;
    private static long myNodeId;
    private static long apNodeId;

    private static Socket meshSocket;

    //tcp thread flag
    private static boolean receiveThreadRunning = false;

    //Runnables for send/receive data?
    private static SendRunnable sendRunnable;

    //Thread to send
    private static Thread sendThread;
    //Thread to recieve
    private static Thread receiveThread;

    //text to append to the stream
    public static StringProperty textStream = new SimpleStringProperty();
    
    /**
     * Default Constructor
     */
    MeshConnect(){
        meshIp = DEFAULT_IP;
        meshPort = DEFAULT_PORT;
        myNodeId = MeshHandler.createMeshId(MeshHandler.getWifiMacAddress());
        apNodeId = 0;
    }

    public Boolean sendMessage(){
        
        Connect(/*meshIp, meshPort*/);
        
        return false;
    }

    /**
     * Returns true if connected to the mesh network, else returns false
     * @return Boolean
     */
    public static boolean isConnected(){
        return meshSocket != null && meshSocket.isConnected() && !meshSocket.isClosed();
    }

    /**
     * Open a connection to the mesh with a new thread
     * @param meshIp
     * @param meshPort
     */
    private static void Connect(/*String ip, int port*/){
        //  meshIp = ip;
        // meshPort = port;

        //start new thread
        new Thread(new ConnectRunnable()).start();
   }
   
   /**
    * Close the socket connectd to the mesh. stopping the threads first
    */
   private static void Disconnect(){
        stopThreads();

        try {
            meshSocket.close();
            System.out.println("Disconnected!");
            textStream.set("\nDisconnected!");
        } catch (IOException e) {
            System.out.println("ERROR: FAILED to disconnect: " + e.getMessage());
            textStream.set("\nERROR: FAILED to disconnect: " + e.getMessage());
        }
   }


   /**
    * Send data to the mesh network
    * @param data message converted to byte array to communicate instructions into the mesh network
    */
   private static void WriteData(byte[] data){
       startSending();
       sendRunnable.Send(data);
   }

   /**
    * Stop the send and recieve threads
    */
   private static void stopThreads(){
       if(receiveThread != null){
           receiveThread.interrupt();
       }
       if(sendThread != null){
           sendThread.interrupt();
       }
   }

   /**
    * start thread to receive data from the mesh
    */
   private static void startReceiving(){
        ReceiveRunnable receiveRunnable = new ReceiveRunnable(meshSocket); //
        receiveThread = new Thread(receiveRunnable);
        receiveThread.start();
   }

   private static void startSending(){
       sendRunnable = new SendRunnable(meshSocket);
        sendThread = new Thread(sendRunnable);
        sendThread.start();
   }

   /**
    * 
    */
   private static class ConnectRunnable implements Runnable{
        public void run(){
            try{
                 System.out.println("Connecting...") ;
                 textStream.set("\nConnecting...");
            
                 //convert ip to use w/ socket
                InetAddress meshAddress = InetAddress.getByName(meshIp);
                    //Create new socket
                 meshSocket = new Socket();
                meshSocket.setKeepAlive(true);
                meshSocket.setReuseAddress(true);

                //Start connection w/ 5000 ms timeout
                meshSocket.connect(new InetSocketAddress(meshAddress, meshPort), 10000);

                System.out.println("Connected");
                textStream.set("\nConnected");
                startReceiving();

                //send node Sync request
                MeshHandler.nodeMessageRequest();

            }catch(Exception e){
            System.out.println("connection failed: " + e);
            textStream.set("\nconnection failed: " + e);
            }
            
        }
    }

    private static class ReceiveRunnable implements Runnable {
        private final Socket socket;
        private InputStream input;

        ReceiveRunnable(Socket mesh){
            socket = mesh;
            try{
                input = socket.getInputStream();
            }catch (Exception e){
                System.out.println("Receive Runnable failed");
                textStream.set("\nReceive Runnable failed");
            }
        }

        @Override
        public void run(){

            System.out.println("Receiving Started");
            textStream.set("\nRecieving Started");
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
                        textStream.set("\nRecieved " + readLen + " bytes: " + rcvdMsg);

                        //TODO: create method to handle recieved mesages and to delberate what to do next. 
                        //MeshHandler.processMessage(rcvdMsg);
                        // if(rcvdMsg.contains("\"type\":6")){
                        //     if(App.apNodeId == 0){
                        //         String apNodeId = rcvdMsg.substring(10, 20);
                        //         //Log.d(apNodeId);
                        //         System.out.println(Long.parseLong(apNodeId));
                        //         App.apNodeId = Long.parseLong(apNodeId);
                        //         System.out.println("apNodeId :" + App.apNodeId);
    
                        //         MeshHandler.nodeSyncRequest();
                        //     }else{
                        //         MeshHandler.nodeTimeSyncRequest(0, null); 
                        //     }                                                      
                        // }
                        // if(rcvdMsg.contains("\"type\":4")){
                        //     if(rcvdMsg.contains("\"type\":0")){
                        //         MeshHandler.nodeTimeSyncRequest(1, null);
                        //     }else if(rcvdMsg.contains("\"type\":1")){
                        //         System.out.println("index teset: " + rcvdMsg.indexOf("t0"));
                        //         String timestamp = rcvdMsg.substring((rcvdMsg.indexOf("t0") + 4), (rcvdMsg.length() - 2));                                
                        //         System.out.println("timestamp: " + timestamp );
                        //         MeshHandler.nodeTimeSyncRequest(2, timestamp);
                        //     }
                
                        // }
                        

                        System.out.println("Data received!");
                        textStream.set("\nData recieved!");
                                                   
                
                    }

                } catch (Exception e){
                    System.out.println("Could not receive data: " + e.getMessage());
                    textStream.set("\nCould not receive data: + e.getMessage()");
                    Disconnect();
                }
                receiveThreadRunning = false;
                System.out.println("Receiving stopped");
                textStream.set("\nReceiving Stopped");
                Disconnect();
              
            }
        }
    }

    private static class SendRunnable implements Runnable {
        byte[] data;
        private OutputStream out;
        private boolean hasMessage = false;

        SendRunnable(Socket mesh){
            try {
                this.out = mesh.getOutputStream();
            }catch (IOException e) {
                System.out.println("Start sendRunnable failed." + e);
                textStream.set("\nStart sendRunnable failed." + e);
            }
        }

            void Send(byte[] bytes) {
                this.data = bytes;
                this.hasMessage = true;
            }

            @Override
            public void run() {
                System.out.println("\nSending Started");
                textStream.set("Sending Started");

                if(this.hasMessage){
                    try{
                        //sned data
                        this.out.write(data,0,data.length);
                        this.out.write(0);
                        //clear output buffer?
                        this.out.flush();

                    }catch(IOException e){
                        System.out.println("sending failed: " + e);
                        textStream.set("\nSending Failed: " + e);
                        
                    }
                    this.hasMessage = false;
                    this.data = null;
                    System.out.println("Sent!");
                    textStream.set("\nSent!");
                }
                System.out.println("sending stopped");
                textStream.set("\nSending Stopped");

                
            }
                    
        
    }

    private static class MeshHandler {
    

        private static String logTime(){
            DateTime now = new DateTime();
            return String.format ("[%02d:%02d:%02d:%03d] ",
                    now.getHourOfDay(),
                    now.getMinuteOfHour(),
                    now.getSecondOfMinute(),
                    now.getMillisOfSecond());
    
        }
    
        static long createMeshId(String macAddress){
            System.out.println("Creating mesh ID from " + macAddress + "...");
            long calcNodeId = -1;
            String[] macAddressParts = macAddress.split(":");
            if (macAddressParts.length == 6){
                try {
                    long number = Long.valueOf(macAddressParts[2],16);
                    if (number < 0) {number = number * -1;}
                    calcNodeId = number * 256 * 256 * 256;
                    number = Long.valueOf(macAddressParts[3],16);
                    if (number < 0) {number = number * -1;}
                    calcNodeId += number * 256 * 256;
                    number = Long.valueOf(macAddressParts[4],16);
                    if (number < 0) {number = number * -1;}
                    calcNodeId += number * 256;
                    number = Long.valueOf(macAddressParts[5],16);
                    if (number < 0) {number = number * -1;}
                    calcNodeId += number;
       
                }catch(NullPointerException ignore){
                    System.out.println("createID fail");
                    textStream.set("\ncreateID Failed");
                    calcNodeId = -1;
                }
            }
            return calcNodeId;
        }
    
        static String getWifiMacAddress(){
            try{
                List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
                for (NetworkInterface intf : interfaces){
                    if(!intf.getName().equalsIgnoreCase("wlan0")) continue;
                    byte[] mac = intf.getHardwareAddress();
                    if(mac==null) return " ";
                    StringBuilder buf = new StringBuilder();
                    for(byte aMac : mac) buf.append(String.format("%02X:", aMac));
                    if(buf.length()>0) buf.deleteCharAt(buf.length()-1);
                    return buf.toString();
                }
    
            }catch(Exception ignored){}
                return "01:02:03:04:05:06";
            
        }
    
    
        /*
            I noticed that when I broadcast the message to the network, 
            I recieve a node connection request. I should be able to query 
            the network to get the necessary info to connect as a node.
        */
    
        static public void nodeSyncRequest(){
    
            JSONObject nodeMessage = new JSONObject();
            // JSONArray subsArray = new JSONArray();
            
            try{
                nodeMessage.put("dest", apNodeId);
                   nodeMessage.put("from", myNodeId);
                   nodeMessage.put("type", 5);
                   nodeMessage.put("subs", "");
    
                   String msg = nodeMessage.toString();
                   byte[] data = msg.getBytes();
                   MeshConnect.WriteData(data);
                    // try{
                    //     App.out.append(dataSet);
                    // }catch(IOException e){
                    //     e.printStackTrace();
                    // }
                System.out.println("Sending message " + msg );
                textStream.set("\nSending message " + msg );
    
            } catch(Exception e){
                System.out.println("Sync Request failed: " + e);
                textStream.set("\nSync Request failed: " + e);
            }          
                   
        }
        static public void nodeTimeSyncRequest(int reqType, Long timeStamp ){
            if(MeshConnect.isConnected()){
                String dataSet = logTime();
                //dataSet += "sending TIME_SYNC_REQUEST\n";
                JSONObject nodeMessage = new JSONObject();
                JSONObject typeObject = new JSONObject();
                try {
                    nodeMessage.put("dest", apNodeId);
                    nodeMessage.put("from", myNodeId);
                    nodeMessage.put("type", 4);
                    switch(reqType){
                        case 0 :
                            typeObject.put("type", 0);
                            break;
                        case 1 :
                            typeObject.put("type", 1);
                            typeObject.put("t0", dataSet);
                            break;
                        case 2 : 
                            typeObject.put("type", 2); 
                            typeObject.put("t0", timeStamp);
                            typeObject.put("t1", dataSet);
                            typeObject.put("t2", logTime());
    
                    }
                    
                    nodeMessage.put("msg", typeObject);
                    String msg = nodeMessage.toString();
                    byte[] data = msg.getBytes();
                    MeshConnect.WriteData(data);
                    System.out.println("Sending time sync request" + msg);
                    textStream.set("\nSending time sync request" + msg);
                } catch (Exception e) {
                    System.out.println("Error sending request:" + e.getMessage());
                    textStream.set("\nError sending request:" + e.getMessage());
                }
            }
        }
    
        static public void nodeMessageRequest(){
            // Random rand = new Random();
            DecimalFormat decForm = new DecimalFormat("000");
    
            String message = ("talk" + decForm.format(Controller.hue));
    
            JSONObject nodeMessage = new JSONObject();
            //JSONArray subsArray = new JSONArray();
            
            try{
                nodeMessage.put("dest", /**nodeId */0);
                   nodeMessage.put("from", 0);
                   nodeMessage.put("type", 8);
                   nodeMessage.put("msg", message);
    
                   String msg = nodeMessage.toString();
                   byte[] data = msg.getBytes();
                   MeshConnect.WriteData(data);
                    // try{
                    //     App.out.append(dataSet);
                    // }catch(IOException e){
                    //     e.printStackTrace();
                    // }
                System.out.println("Sending message " + msg );
                textStream.set("\nSending message " + msg );
    
            } catch(Exception e){
                System.out.println("Message Request failed: " + e);
                textStream.set("\nMessage Request failed: " + e);
            }          
                   
        }
        
        /**A method that processes the messages from the mesh by checking for the type, and deliberating what to do next
         * 
          */
          public static void processMessage(String msg){
            JSONObject jsonMessage = new JSONObject(msg);//converts message string into a JSON object
    
              if(msg.contains("\"type\":6"))//node sync reply
              {
                if(apNodeId == 0)//checks if access node has been discovered
                {
    
                    //pulls access point node's ID, saves it and sends another sync request
                    apNodeId = jsonMessage.getLong("from");
                    System.out.println("apNodeId :" + apNodeId);
                    nodeSyncRequest();
    
                }
                else
                {
                    //if we already know the apNodeId, start the mesh protocol by sending a time sync request
                    nodeTimeSyncRequest(0, null); 
                }                                                      
            }
            if(msg.contains("\"type\":4"))//time sync request
            {
                if(msg.contains("\"type\":0"))//req time
                {
                    nodeTimeSyncRequest(1, null);
                }
                else if(msg.contains("\"type\":1"))
                {
                    Long timestamp =jsonMessage.getJSONObject("msg").getLong("t0"); 
                    System.out.println("recieved timestamp " + timestamp );
                    nodeTimeSyncRequest(2, timestamp);
                }
    
            }
    
          }
    
    }
}


