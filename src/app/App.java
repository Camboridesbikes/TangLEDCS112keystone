//I noticed that when I broadcast the message to the network, I recieve a node connection request. I should be able to query the network to get the necessary info to connect as a node

package app;
import java.net.*;
import java.nio.channels.NetworkChannel;

import org.json.*;

//import jdk.internal.jline.internal.InputStreamReader;

import java.io.*;

public class App { 
    
    public static long startTime;

    static long myNodeId = 0;
    static long apNodeId;

    static BufferedWriter out = null;

    public static void main(String[] args) {
        String ip;
        int port;
    
        ip =  "10.251.129.1";
        port = 5555;

        //myNodeId = MeshHandler.createMeshId(MeshHandler.getWifiMacAddress());
        
        

        try{
             startTime = System.currentTimeMillis();

            //new Thread(new ConnectRunnable()).start();
            MeshConnect.Connect(ip, port);

        }finally{
            
            
        }
    }

    // public static class ConnectRunnable implements Runnable{
    //     public void run(){
    //         try{
               

                

    
    //             //InetAddress address = InetAddress.getByName( ip);
    //             //Socket meshSocket = new Socket(address, port);

    //             long time = System.currentTimeMillis() - startTime;

                

    //             //BufferedReader meshInput = new BufferedReader( new InputStreamReader(meshSocket.getInputStream()));
    //             //DataOutputStream localOutput = new DataOutputStream(meshSocket.getOutputStream());
                
    //              //making the json to send for sync request
           
           

    //             System.out.println("Connection made. Sending message...");

    //             //localOutput.writeBytes(msg);

    //             //String meshData = meshInput.readLine();
    //             //System.out.println("recieved: " + meshData);
    //         }catch(Exception e){
    //             System.out.println(e);
    //         }
    //     }
    // }
}