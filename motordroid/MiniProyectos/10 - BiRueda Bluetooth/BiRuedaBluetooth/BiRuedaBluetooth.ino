//BiRueda Bluetooth

//Importamos la libreria AFMotor.h
#include <AFMotor.h>
 
//Creamos los dos motores que estan conectados al M1 y M2 respectivamente con una frecuencia de 64KHZ
AF_DCMotor motor(1, MOTOR12_64KHZ);
AF_DCMotor motor2(2, MOTOR12_64KHZ);

//Variable velocidad
int velocidad;

//Función 'Setup'
void setup() {
  //Inicializamos Serial
  Serial.begin(9600);
  Serial.println("BiRueda Bluetooth");
  
  //Cambiamos la velocidad
  velocidad = 200;
  
  //Damos velocidad a los motores
  motor.setSpeed(velocidad);
  motor2.setSpeed(velocidad);
  
  //Inicializamos el BT Serial
  Serial3.begin(9600);
  Serial3.flush();
  
  delay(500);
  
  Serial.println("Listo");
}

//ACCIONES DEL VEHÍCULO BIRUEDA

//Adelante
void UP(){
  Serial.println("adelante");
  motor.run(FORWARD);
  motor2.run(FORWARD);
}
//Atrás
void DOWN(){
   Serial.println("atras");
   motor.run(BACKWARD);     // the other way
   motor2.run(BACKWARD);
}
//Giro Sentido Antihorario
void LEFT(){
    Serial.println("izq");
    motor.run(BACKWARD);
    motor2.run(FORWARD);
}
//Giro Sentido Horario
void RIGHT(){
    Serial.println("der");
    motor.run(FORWARD);
    motor2.run(BACKWARD);
}
//Parada
void STOP(){
    Serial.println("stop");
    motor.run(RELEASE);
    motor2.run(RELEASE);
}

//Función 'Loop'
void loop() {
  //Esperamos a recibir datos
  if (Serial3.available()){
    //Leemos comandos
    char comando = Serial3.read();
    //Liberamos el BT Serial
    Serial3.flush();
    //Mostramos por Serial el comando leído
    Serial.println(comando);
    //Dependiendo del comando realiza una u otra acción
    switch(comando){
      case 'U': Serial.println("UP"); UP(); break;
      case 'D': Serial.println("DOWN"); DOWN(); break;
      case 'L': Serial.println("LEFT"); LEFT(); break;
      case 'R': Serial.println("RIGHT"); RIGHT(); break;
    }
  } else {
    //Si no se lee ningún comando el vehículo se para
    Serial.println("STOP"); 
    STOP();
  }
  //Dejamos un tiempo de refresco de 20 ms
  delay(20);
} 
