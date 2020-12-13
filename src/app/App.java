/*=======================
 *  TangLED Lantern App
 *      App.java
 *=======================
 *  
 * Driver program that connects to the mesh network and delivers a message.
 * 
 * TODO: clear apNodeID when disconnecting from Mesh
 * 
 * @author Cameron Hattendorf
 * @version v.02
*/

package app;

//import jdk.internal.jline.internal.InputStreamReader;

import java.io.*;
import java.util.Scanner;
import java.net.*;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class App extends Application{

    public static long startTime;

    public static int hue = 0;

    static BufferedWriter out = null;

    public static String ip;
    public static int port;

    public static void main(String[] args){       

        // 10.251.129.1
        ip = "10.251.129.1";
        port = 5555;

        System.out.println("Launching...");
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("gui.fxml"));
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("TangLED Client");
        primaryStage.show();
    }
}

    
