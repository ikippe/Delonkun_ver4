int _ABVAR_1_M = 0 ;
int _ABVAR_2_D = 0 ;
int ardublockUltrasonicSensorCodeAutoGeneratedReturnCM(int trigPin, int echoPin)
{
  long duration;
  pinMode(trigPin, OUTPUT);
  pinMode(echoPin, INPUT);
  digitalWrite(trigPin, LOW);
  delayMicroseconds(2);
  digitalWrite(trigPin, HIGH);
  delayMicroseconds(20);
  digitalWrite(trigPin, LOW);
  duration = pulseIn(echoPin, HIGH);
  duration = duration / 59;
  if ((duration < 2) || (duration > 300)) return false;
  return duration;
}

void __ardublockDigitalWrite(int pinNumber, boolean status)
{
  pinMode(pinNumber, OUTPUT);
  digitalWrite(pinNumber, status);
}



int getSensorMM(int trigPin, int echoPin)
{
  long duration;
  pinMode(trigPin, OUTPUT);
  pinMode(echoPin, INPUT);
  digitalWrite(trigPin, LOW);
  delayMicroseconds(2);
  digitalWrite(trigPin, HIGH);
  delayMicroseconds(20);
  digitalWrite(trigPin, LOW);
  duration = pulseIn(echoPin, HIGH);
  duration = duration / 6;
  if ((duration < 5) || (duration > 3000)) return false;
  return duration;
}

int getCommand(void)
{
  int cmd=0;
  
  pinMode(3, INPUT);
  pinMode(4, INPUT);
  pinMode(11, INPUT);
  pinMode(12, INPUT);
  
  if (digitalRead(3))  cmd |= 0x01;
  if (digitalRead(4))  cmd |= 0x02;
  if (digitalRead(11)) cmd |= 0x04;
  if (digitalRead(12)) cmd |= 0x08;
  
  return cmd;
}

#define MODE_RIGHT 1
#define MODE_BACK 2
#define MODE_STOP 3
#define MODE_FORWARD 4
#define MODE_LEFT 5

void RIGHT();
void BACK();
void STOP();
void FORWARD();
void LEFT();

int mode, tmp;

void setup()
{
  Serial.begin(9600);
  digitalWrite( 6 , LOW );

  _ABVAR_1_M = 7 ;

  mode = MODE_STOP;

  Serial.print("-- START --");
  Serial.println();

}

void loop()
{
  // モード更新
  mode=getCommand();
/*
  tmp=getCommand();
  if (tmp!=0) {
    mode=tmp;
  }
//*/
  // 動作
  Serial.print(mode);
  Serial.println();
  switch (mode) {
    case MODE_RIGHT:
      RIGHT();
      Serial.print("-- RIGHT");
      Serial.println();
      break;
    case MODE_BACK:
      BACK();
      Serial.print("-- BACK");
      Serial.println();
      break;
    case MODE_STOP:
      STOP();
      Serial.print("-- STOP");
      Serial.println();
      break;
    case MODE_FORWARD:
      FORWARD();
      Serial.print("-- FORWARD");
      Serial.println();
      break;
    case MODE_LEFT:
      LEFT();
      Serial.print("-- LEFT");
      Serial.println();
      break;
  }
  
  // センサー結果
  int d = getSensorMM( 6 , 5 ) ;
  Serial.println(d);
  delay(500);
  
}

void BACK()
{
  __ardublockDigitalWrite(_ABVAR_1_M, LOW);
  __ardublockDigitalWrite(( _ABVAR_1_M + 1 ), HIGH);
  __ardublockDigitalWrite(( _ABVAR_1_M + 2 ), LOW);
  __ardublockDigitalWrite(( _ABVAR_1_M + 3 ), HIGH);
}

void STOP()
{
  __ardublockDigitalWrite(_ABVAR_1_M, LOW);
  __ardublockDigitalWrite(( _ABVAR_1_M + 1 ), LOW);
  __ardublockDigitalWrite(( _ABVAR_1_M + 2 ), LOW);
  __ardublockDigitalWrite(( _ABVAR_1_M + 3 ), LOW);
}

void LEFT()
{
  __ardublockDigitalWrite(_ABVAR_1_M, LOW);
  __ardublockDigitalWrite(( _ABVAR_1_M + 1 ), HIGH);
  __ardublockDigitalWrite(( _ABVAR_1_M + 2 ), HIGH);
  __ardublockDigitalWrite(( _ABVAR_1_M + 3 ), LOW);
}

void FORWARD()
{
  __ardublockDigitalWrite(_ABVAR_1_M, HIGH);
  __ardublockDigitalWrite(( _ABVAR_1_M + 1 ), LOW);
  __ardublockDigitalWrite(( _ABVAR_1_M + 2 ), HIGH);
  __ardublockDigitalWrite(( _ABVAR_1_M + 3 ), LOW);
}

void RIGHT()
{
  __ardublockDigitalWrite(_ABVAR_1_M, HIGH);
  __ardublockDigitalWrite(( _ABVAR_1_M + 1 ), LOW);
  __ardublockDigitalWrite(( _ABVAR_1_M + 2 ), LOW);
  __ardublockDigitalWrite(( _ABVAR_1_M + 3 ), HIGH);
}


