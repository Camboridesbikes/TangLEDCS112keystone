/*=======================
 *  TangLED Lantern App
 *      App.java
 *=======================
 *  
 * Driver program that connects to the mesh network and delivers a message.
 * 
 * @author Cameron Hattendorf
 * @version v.02
*/

package app;

//import jdk.internal.jline.internal.InputStreamReader;

import java.io.*;
import java.util.Scanner;

public class App { 
    
    public static long startTime;

    static long myNodeId = 0;
    static long apNodeId;

    static BufferedWriter out = null;

    public static void main(final String[] args) {
        Scanner key;

        String ip;
        int port;

        key = new Scanner(System.in);
        
        //10.251.129.1
        ip =  "10.251.129.1";
        port = 5555;   
        
        //System.out.print("Enter the ip of your network: ");
        //ip = key.next();


        try{
            MeshConnect.Connect(ip, port);
        }finally{            
                       
        }

        key.close(); 
    }
}