# TangLED Lanterns

### 	Keystone Project for CS 112 (as well as a personal project)

​		The idea of this project is to have a dynamic amount of led lanterns connect to each other using a mesh network and communicate their state as well as commands to change their color and/or behavior. As a companion to this hardware, I will be able to connect to the mesh with a computer or mobile device and manipulate the lanterns individually or by group.

​	This project is a personal project that started as an idea 3 years ago and began working on this last year. I'm using the keystone project for class as an opportunity to expand on this brain-child of mine. 

​	This repo contains the code for the micro controllers as well as the java code for the desktop app I am writing for class.

## Lanterns

The lanterns are built off esp32 and esp8285/8266 microcontrollers. 

Libraries used: PainlessMesh, FastLED

​	I designed the lanterns using golden ratios. My first two fleshed out prototypes were modeled in AutoCAD and put together by myself.  They each have sound sensors. When they pick up sound, they change color and switch to a transition mode. Upon the completion of the transition mode, the lantern that heard the sound sends the color to the other lanterns and returns to a glowing mode with a new color. These lanterns can receive a command from the desktop application to change their color.

Examples of the current prototypes in action:

 Single Lantern at Night: https://www.youtube.com/watch?v=twDgDSry_0c

Both Lanterns Interacting: https://www.youtube.com/watch?v=GCt-INOi8gM



​	The lanterns I am using for this project are minimal and made of cardboard, without a sound sensor. They can currently receive a command to change color from the java application. My goal for the end of the school project is to also change modes from the java application.



## Desktop Application (with GUI)

​	The desktop application is written in java with javafx running the GUI. The classes connecting to the mesh and handling the mesh data are using a lot of code and design from [painlessMeshAndroid](https://gitlab.com/painlessMesh/painlessmesh_android) by Bernd Giesecke.

<p float="left">
    <img src="https://github.com/Camboridesbikes/TangLEDCS112keystone/blob/main/CS112FinalWireframe.png" alt="Image of GUI Wireframe" width="35%" /><img src="https://github.com/Camboridesbikes/TangLEDCS112keystone/blob/main/TangLED.PNG?raw=true" alt="Image of GUI" width="45%"/>
    
</p>
![Image of GUI Wireframe](https://github.com/Camboridesbikes/TangLEDCS112keystone/blob/main/CS112FinalWireframe.PNG?raw=true)
![Image of GUI](https://github.com/Camboridesbikes/TangLEDCS112keystone/blob/main/TangLED.PNG?raw=true)


