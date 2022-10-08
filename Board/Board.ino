//**************************************************************//
//  Name    : shiftIn Example 1.1                              //
//  Author  : Carlyn Maw                                        //
//  Date    : 25 Jan, 2007                                      //
//  Version : 1.0                                               //
//  Notes   : Code for using a CD4021B Shift Register       //
//          :                                                   //
//****************************************************************

//define where your pins are
int latchPin = 2;
int dataPin = 3;
int clockPin = 4;
const int pin_red=11;
const int pin_green=12;
byte data[7]={0xff,0xff,0xff,0xff,0xff,0xff,0xff};
char readString[255]="";
int lastNum=0;
//Define variables to hold the data
//for shift register.
//starting with a non-zero numbers can help
//troubleshoot
byte switchVar1 = 72;  //01001000

void setup() {

  //start serial

  Serial.begin(9600);

  //define pin modes

  pinMode(latchPin, OUTPUT);

  pinMode(clockPin, OUTPUT);

  pinMode(dataPin, INPUT);
  pinMode(pin_red,OUTPUT);
  pinMode(pin_green,OUTPUT);

}

void loop() {

  //Pulse the latch pin:


  digitalWrite(clockPin,HIGH);
  //set it to 1 to collect parallel data

  digitalWrite(latchPin,LOW);

  //set it to 1 to collect parallel data, wait

  delayMicroseconds(20);

  //set it to 0 to transmit data serially

  digitalWrite(latchPin,HIGH);

  //while the shift register is in serial mode

  //collect each shift register into a byte

  //the register attached to the chip comes in first

  switchVar1 = shiftIn(dataPin, clockPin,LSBFIRST);
  int num=0;
  bool chipIn=switchVar1&1;
  num=switchVar1>>1;
  if(chipIn)
  {
    data[0]=num;
  }
  else
  {
    data[0]=0xff;
  }
  if(num!=lastNum)
{
  //Serial.print("data: ");
  Serial.write(data,7);
  lastNum=num;
}
  
  //Print out the results.

  //leading 0's at the top of the byte

  //(7, 6, 5, etc) will be dropped before

  //the first pin that has a high input

  //reading
  int i=0;
 while (Serial.available()) {
    delay(2);  //delay to allow byte to arrive in input buffer
    char c = Serial.read();
    readString[i] = c;
    i++;
  }
  

  
  int port=(readString[0]-'0')*10+(readString[1]-'0');
  int state=readString[2]-'0';
  if(i>0)
  {
    //Serial.write(readString,7);
  }

  if(state==1)
  {
    set_outputs_plus_ground(pin_red,pin_green);
  }
  else if(state==2)
  {
    set_outputs_plus_ground(pin_green,pin_red);
  }
  else 
  {
    digitalWrite(pin_green,LOW);  
    digitalWrite(pin_red,LOW);
  }

  delay(500);

}
void set_outputs_plus_ground(int pin_plus, int pin_ground)
{
  //set both pins low first so you don't have them pushing 5 volts into eachother
  digitalWrite(pin_plus,LOW);  
  digitalWrite(pin_ground,LOW);
  //set output pin as high
  digitalWrite(pin_plus,HIGH);  
} 
