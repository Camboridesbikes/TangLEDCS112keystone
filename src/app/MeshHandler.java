package app;

import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

import java.text.DecimalFormat;
import java.util.Random;

import org.json.*;

public class MeshHandler {

    

    // private static String logTime(){
    //     DateTime now = new DateTime();
    //     return String.format ("[%02d:%02d:%02d:%03d] ",
	// 			now.getHourOfDay(),
	// 			now.getMinuteOfHour(),
	// 			now.getSecondOfMinute(),
	// 			now.getMillisOfSecond());

    // }

    static long createMeshId(String macAddress){
        long calcNodeId = -1;
        String[] macAddressParts = macAddress.split(":");
        if (macAddressParts.length == 6){
            try {
                long number;
                for(int i = 2, a = 3; i <= 5; i++, a--){
                    number = Long.valueOf(macAddressParts[i],16);
                    if(number < 0 ) {number = number * -1;}
                    if(a<0){
                        calcNodeId = number * (256 * a);
                    }else{
                        calcNodeId = number;
                    }                    
                }               
            }catch(NullPointerException ignore){
                System.out.println("createID fail");
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
        JSONArray subsArray = new JSONArray();
        
        try{
            nodeMessage.put("dest", /**nodeId */0);
               nodeMessage.put("from", 0);
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

        } catch(Exception e){
            System.out.println("Sync Request failed: " + e);
        }          
               
    }

    static public void nodeMessageRequest(){
        Random rand = new Random();
        DecimalFormat decForm = new DecimalFormat("000");

        String message = "talk";

        //message = String.format("%s,%s,%s,%d", decForm.format(rand.nextInt(150)),decForm.format(rand.nextInt(150)),decForm.format(rand.nextInt(150)),250);

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

        } catch(Exception e){
            System.out.println("Sync Request failed: " + e);
        }          
               
    }
    
}