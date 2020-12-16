/*
 *
 * TODO:  
     - assign led functins to mode
 *  
 * 
*/

//create a task to run the led logic on on the other core. set the boolean, check the boolean and update the function every 500 miliseconds???

//This block only executes if uploading to an esp8266
#ifdef ESP8266
#include "Hash.h"
#include "ESPAsyncTCP.h"
#else
#include <AsyncTCP.h>
#endif

////////////////////////////////////////
/////////////// PACKAGES ///////////////
//////////////////////////////////////

#include<painlessMesh.h>
#include <FastLED.h>

/////////////////////////////////////
//////////////// CONSTANTS //////////
////////////////////////////////////
 
//Mesh 

#define MESH_SSID "tangledMesh"
#define MESH_PASS "tangledMesh"
#define MESH_PORT 5555

//fastLED

#define LED_PIN 2
#define LED_COUNT 46 //6  on top, 10 on each of the 4 sides (descending)


//task

//#define BLINK_CYCLE 3000 //ms
//#define BLINK_DURATION 100

/////////////////////////////////////////////
//////////// INSTANCE VARIABLES /////////////
////////////////////////////////////////////

//fastLed

int ledMode = 3; //var to switch modes
int counter = 0;// var to keep track of iterations
bool toggle = false; //a variable available to act like a toggle switch when needed
uint8_t _hue = random(0, 224); // global hue

int pos16 = 0;//antiAliasing wall

//painlessMesh

painlessMesh mesh;

//tasks

Scheduler scheduler; //allows me to set tasks


uint32_t ledCmd; //FIXME: unused --delete??

void flagLed(); // prototype -- may move to tasks

bool onFlag = false; //--may move to tasks

 CRGB leds[LED_COUNT]; //array of leds for fastLED
 
/////////////////////////////////////////////
////////// PAINLESS MESH FUNCTIONS //////////
/////////////////////////////////////////////

//node recieves a message from id of sender. msg can be anything.
void receivedCallback(uint32_t from, String &msg )
{

//check message for command
if(msg.substring(0, 4).equals("talk")){
  //switch statement to handle
  _hue = static_cast<uint8_t>(atoi(msg.substring(5,9).c_str()));
}
 

  Serial.printf("Received message from %u msg=%s\n", from,msg.c_str());
  
}

//node makes a new connection and recieves the node id of the connection
void newConnectionCallback( uint32_t nodeId)
{
  Serial.printf("New Connection, nodeId = %u\n", nodeId);
}

//every time topology changes
void changedConnectionCallback()
{
  Serial.printf("Changed connections\n");
  Serial.printf("flag is %d\n", onFlag);
}

//node's time was adjusted to synchronize with mesh
void nodeTimeAdjustedCallback(int32_t offset)
{
  Serial.printf("Adjusted time %u. Offset = %d\n", mesh.getNodeTime(), offset);
}

/////////////////////////////////////
/////////// FastLED FUNCTIONS /////////
/////////////////////////////////////

void lightItUp(int pos, uint8_t hue, int del){
  /*
   * - single pixel moxes along leds at a constant speed from firts to last
   * - changed to a random color upon completion 
  */

  //clear pixel buffer
    memset8( leds, 0, LED_COUNT * sizeof(CRGB));

    
    int i = pos / 16;
    uint8_t frac = i & 0x0F;

    uint8_t currentPixelBrightness = 255 - (frac * 16);
    uint8_t nextPixelBrightness = (255 - currentPixelBrightness);
    
    leds[pos] += CHSV(_hue, 255, currentPixelBrightness);
    leds[pos + 1] += CHSV(_hue, 255, nextPixelBrightness);

    FastLED.show();
    delay(del);

  pos16++; 

  //check if pixel has reached the end. if it has, restart and generate a random color
  if(pos16 >= 45) { 
      pos16 -= 45;
      _hue = random(0, 224);
    }
}

void verticalBounce(int pos, uint8_t hue){
  /*
   * -the vertical leds moving up and down
   * -delay speed will speed as the lights drop and slow back down as they go back up.
   * the top 
   * -the top six leds will be of an offset color
  */
  
  //clear pixel buffer
    memset8( leds, 0, LED_COUNT * sizeof(CRGB));

     int i = pos / 16;
    uint8_t frac = i & 0x0F;

    uint8_t currentPixelBrightness = 255 - (frac * 16);
    uint8_t nextPixelBrightness = (255 - currentPixelBrightness);

    //light up the top six leds with offset hue
    for(int x = 0; x < 6; x++){
      leds[x] += CHSV((255 - hue) , 255, 255);
    }
     
    //this affects the leds on each of the four walls
    for(int n = 0 ; n < 4 ; n++){
      leds[pos + 6 + (10 * n)] += CHSV(hue, 255, currentPixelBrightness);
      if (toggle){
        leds[pos + 6 + (10 * n) + 1] += CHSV(hue, 255, nextPixelBrightness);
      }else{
        leds[pos + 6 + (10 * n) - 1] += CHSV(hue, 255, nextPixelBrightness);
      }
    }

    FastLED.show();
    delay((pos16 * 10) + 20 );//this math will speed up and slow down the rate of change
    
    //this block keeps the leds within the limits and toggles a change in direction
    if(toggle){
      pos16++;
      if(pos16 >= 9){
        toggle = false;
      }
    }else{
      pos16--;
      if(pos16 <=1){        
        toggle = true;
      }      
    }
}

/////////////////////////////////
///////// TASK FUNCTIONS //////////
/////////////////////////////////
/*
  -I need to update the 'delay' functions in my fast led functions to tasks.
*/

//Task controlLed( TASK_SECOND * 1, TASK_FOREVER, &flagLed); //This task runs every second to check 

//update
//controls bool for led change event
void flagLed()
{
  Serial.printf("check flagging : %d\n", onFlag);
  bool flag = onFlag;
  
  if(mesh.getNodeList().size() < 1)
  {
    onFlag = false;  
  }
  else
  {
    onFlag = true;
  }
  
  //controlLed.setCallback(&ledBehave);
  
  if(flag != onFlag)
  {
    //ledBehave();
  }
}

//update
void ledBehave()
{
  if(!onFlag)
  {
//    colorWipe(strip.Color(255, 0, 0), 0);   
  }
  else
  {
    //ledMode = 1;
    //drawFractionalRing(pos16, 60, 120, 3);
  }   
}

/////////////////////////////////
/////// REQUIRED BLOCKS ///////////
/////////////////////////////////

void setup() {
  Serial.begin(115200);
  Serial.printf("Starting to monitor");

  mesh.setDebugMsgTypes(GENERAL | ERROR | STARTUP | CONNECTION);

  //initialize network
  mesh.init(MESH_SSID, MESH_PASS, &scheduler, MESH_PORT);
  
  //initialize functions declared earlier
  mesh.onReceive(&receivedCallback);
  mesh.onNewConnection(&newConnectionCallback);
  mesh.onChangedConnections(&changedConnectionCallback);
  mesh.onNodeTimeAdjusted(&nodeTimeAdjustedCallback);  

FastLED.addLeds<NEOPIXEL, LED_PIN>(leds, LED_COUNT);//FastLED
FastLED.setBrightness(255);

  
  //  task to change led color. checks flag
  //scheduler.addTask( controlLed );
  //controlLed.enable(); 
}

void loop() {
  mesh.update();

  //lightItUp(pos16, _hue, 100);
  verticalBounce(pos16,_hue);

//UPDATE TO REFLECT USE CASES
//  if(ledMode == 0){
//    drawFractionalRing(pos16, _hue, 50, 1);
//  }
//  else if (ledMode == 1)
//  {
//    drawFractionalWall(pos16, _hue, 67, 18);
//  }
//  else if (ledMode == 3){
//    drawAscendingFractionalRing(pos16, _hue, 50, 1);
//  }
   
}
