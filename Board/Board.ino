/*
  Blink

  Turns an LED on for one second, then off for one second, repeatedly.

  Most Arduinos have an on-board LED you can control. On the UNO, MEGA and ZERO
  it is attached to digital pin 13, on MKR1000 on pin 6. LED_BUILTIN is set to
  the correct LED pin independent of which board is used.
  If you want to know what pin the on-board LED is connected to on your Arduino
  model, check the Technical Specs of your board at:
  https://www.arduino.cc/en/Main/Products

  modified 8 May 2014
  by Scott Fitzgerald
  modified 2 Sep 2016
  by Arturo Guadalupi
  modified 8 Sep 2016
  by Colby Newman

  This example code is in the public domain.

  https://www.arduino.cc/en/Tutorial/BuiltInExamples/Blink
*/

const int pin_red=10;
const int pin_green=11;
char readString[255]="";
void setup() {
pinMode(2,OUTPUT);
pinMode(pin_red,OUTPUT);
pinMode(pin_green,OUTPUT);
pinMode(3,OUTPUT);
pinMode(4,INPUT);
pinMode(5,INPUT);
pinMode(6,INPUT);
pinMode(7,INPUT);
pinMode(8,INPUT);
pinMode(9,INPUT);
Serial.begin(9600);
}
int lastNum=0;
bool chipIn=false;
void loop() {

int num=0;

int val = 0;
digitalWrite(2,HIGH);//power
val=digitalRead(9);//return

if(val>0)
{
  //Serial.print("Chip In, ");
  num=digitalRead(3); //1'2
  num|=digitalRead(4)<<1;//2'2
  num|=digitalRead(5)<<2;//4'2
  num|=digitalRead(6)<<3;//8'2
  num|=digitalRead(7)<<4;//16'2
  num|=digitalRead(8)<<5;//32'2
}
else
{
  num=0xff;  
}
/*Serial.print("Received Number: ");
Serial.print(num);
Serial.print("\n");*/
char data[7]={0xff,0xff,0xff,0xff,0xff,0xff,0xff};
data[0]=(char)num;
if(num!=lastNum)
{
  Serial.print(data);
  lastNum=num;
}

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
    //Serial.print("Chip Number is: ");
    /*Serial.print("Received Command, Setting: Port ");
    Serial.print(port);
    Serial.print(" to ");
    Serial.print(state);
    Serial.print("\n");*/
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
  


/*
  set_outputs_plus_ground(pin_red,pin_green);
delay(1000);
  set_outputs_plus_ground(pin_green,pin_red);   
delay(1000);*/

           }

void set_outputs_plus_ground(int pin_plus, int pin_ground)
{
  //set both pins low first so you don't have them pushing 5 volts into eachother
  digitalWrite(pin_plus,LOW);  
  digitalWrite(pin_ground,LOW);
  //set output pin as high
  digitalWrite(pin_plus,HIGH);  
  } 
