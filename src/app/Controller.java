package app;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;

public class Controller{
    public static int hue = 0;  
        
    MeshConnect meshConnector = new MeshConnect();

    @FXML
    TextArea meshStream;
    @FXML
    Button clearStreamButton;
    @FXML
    Button connectButton;
    @FXML
    Slider colorSlider;
    @FXML
    Label colorSliderValue;

    @FXML
    public void initialize(){
        System.out.println("initializing...");
        colorSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            colorSliderValue.setText(newValue.intValue() + "");
            System.out.println(Double.toString(newValue.intValue()));
            hue = newValue.intValue();
        });
        MeshConnect.textStream.addListener((observable, oldValue, newValue) -> {
            AppendToMeshStream(newValue);
        });
    }

    //method to clear text stream
    public void clearMeshStream(){
        meshStream.clear();
    }

    //TODO: enable button when mesh is connected again maybe add an observable?
    //method to connect on the press of connect button -changes button to disconnect button
    public void connectOnPress(){
        meshConnector.sendMessage();
        //connectButton.setDisable(true);
    }

    public void AppendToMeshStream(String text){
        meshStream.appendText(text + "\n");
    }

    //method to control the color selection slider
    public void colorSlideSelection(){
        //meshStream.appendText(colorSlider.valueProperty().toString());
        System.out.print("sliding: ");
        System.out.println("Hue from slider " + colorSlider.valueProperty().toString());
    }
    //
    
    
}
