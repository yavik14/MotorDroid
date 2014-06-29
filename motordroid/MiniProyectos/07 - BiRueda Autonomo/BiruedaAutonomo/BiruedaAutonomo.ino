//BiRueda Autónomo

//Importamos la libreria AFMotor.h
#include <AFMotor.h>

//Creamos los dos motores que estan conectados al M1 y M2 respectivamente con una frecuencia de 64KHZ
AF_DCMotor motor(1, MOTOR12_64KHZ);
AF_DCMotor motor2(2, MOTOR12_64KHZ);

int velocidad;

//Función 'Setup'
void setup(){
  //Ajustamos la una velocidad
  velocidad = 150;
  
  //Le damos la velocidad a los motores
  motor.setSpeed(velocidad);
  motor2.setSpeed(velocidad);
  
   // Iniciamos los motores parados
  motor.run(RELEASE);
  motor2.run(RELEASE);
  
  //Inicializamos Serial
  Serial.begin(9600);
}

//Función 'Loop'
void loop(){
  Serial.println("BiRueda Autonomo");

  //Movimiento hacia delante 2 segundos
  motor.run(FORWARD);
  motor2.run(FORWARD);
  delay(2000);
   // Giramos a la izquierda medio segundo
  motor.run(FORWARD);
  motor2.run(BACKWARD);
  delay(500);
  //Movimiento hacia atrás 2 segundos
  motor.run(BACKWARD);
  motor2.run(BACKWARD);
  delay(2000);
  // Giramos a la derecha medio segundo
  motor.run(BACKWARD);
  motor2.run(FORWARD);
  delay(500);
  //Paramos 1 segundos
  motor.run(RELEASE);
  motor2.run(RELEASE);
  delay(1000);
}

