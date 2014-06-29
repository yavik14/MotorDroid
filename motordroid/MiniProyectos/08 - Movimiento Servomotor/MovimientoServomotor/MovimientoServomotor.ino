//Movimiento Servomotor

//Importamos la librería Servo.h
#include <Servo.h>

//Creamos un servomotor
Servo myservo; 
 
//posicion servo
int posicion;

//Función 'Setup'
void setup() {
  //Inicializamos Serial
  Serial.begin(9600);
  Serial.println("Movimiento Servomotor");
  //Usamos el Pin SER1
  myservo.attach(10);
  //Lo colocamos en la posicion central
  posicion = 75;
  myservo.write(posicion);
}
 
//Función 'Loop'
void loop() {
  //Movimiento desde la posición central hasta la 180
  for(pos; pos < 150; pos += 1)  
  {                                  
    myservo.write(pos);   
    Serial.println(pos);    
    delay(20);                       
  }
 //Movimiento desde la posición 180 hasta la 0 
  for(int pos = 150; pos>=0; pos-=1)     
  {                                
    myservo.write(pos);
    Serial.println(pos);    
    delay(20);                       
  }
 //Movimiento desde la posición 0 hasta la central 
 for(pos; pos < 75; pos += 1)  
  {                                  
    myservo1.write(pos);   
    Serial.println(pos);    
    delay(20);                       
  }
}
