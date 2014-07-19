
// モード定義
#define MODE_RIGHT   1
#define MODE_BACK    2
#define MODE_STOP 3
#define MODE_FORWARD 4
#define MODE_LEFT 5
#define MODE_CALIBRATION 6
#define MODE_TRACKING 7

int mode;    //モード
int distMM;  //距離mm




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





void LED_ON()
{
  __ardublockDigitalWrite(13, HIGH);
}

void LED_OFF()
{
  __ardublockDigitalWrite(13, LOW);
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









//
// mode キャリブレーション
//
int calibrationSt;
int calibrationCounter;

void initCalibration()
{
  calibrationSt =0;
}

void execCalibration()
{
  switch (calibrationSt) {
    case 0: //---- 右回転キャリブレーション
      calibrationCounter = 0;
      calibrationSt ++;
      break;
    case 1:
      calibrationCounter ++;
      if ((calibrationCounter%10) == 0) {
        Serial.print(calibrationCounter);
        Serial.print(" ");
        Serial.println(distMM);
      }
      break;
  }
}





//
// mode 追跡
//

void initTracking()
{
}

void execTracking()
{
}










//
// モード初期化
//
void initMode(int mode) {
  
  Serial.print("-- Init mode ");
  Serial.println(mode);
  
  LED_ON(); delay(200); LED_OFF();
  
  switch (mode) {
    case MODE_CALIBRATION:
      initCalibration(); 
      break;
    case MODE_TRACKING:
      initTracking();
      break;
  }
}


//
// モード処理
//
void execMode(int mode) {
  switch (mode) {
    case MODE_RIGHT:
      RIGHT();
      break;
    case MODE_BACK:
      BACK();
      break;
    case MODE_STOP:
      STOP();
      break;
    case MODE_FORWARD:
      FORWARD();
      break;
    case MODE_LEFT:
      LEFT();
      break;
    case MODE_CALIBRATION:
      execCalibration();
      break;
    case MODE_TRACKING:
      execTracking();
      break;
  }
}







void setup()
{
  Serial.begin(9600);
  digitalWrite( 6 , LOW );

  _ABVAR_1_M = 7 ;

  mode = MODE_STOP;
  distMM = 0;
  
  STOP();

  Serial.print("-- START --");
  Serial.println();

  for (int i=0; i<3; i++)
   LED_ON(); delay(100); LED_OFF(); delay(100);
  
}


void loop()
{
  static int oldMode=-1;

  // 距離取得
  distMM = getSensorMM( 6 , 5 ) ;
  
  // モード更新
  mode=getCommand();
  if (mode != oldMode) { //モードが切り替わったので初期化
    initMode(mode);
    oldMode = mode;
  }

  // モード処理
  execMode(mode);
  
  delay(10);
}



