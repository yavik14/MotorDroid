//Sensor Linea Blanca

//Definimos el pin para el led
#define led   13
// Función 'Setup'
void setup()
{
  // Inicializamos el pin 'led' como salida
  pinMode(led, OUTPUT);  
}
// Función 'Loop'
void loop()
{
  Serial.print(analogRead(A0));
  //Leemos el sensor 
  int lecturaSensor = analogRead(A0);
 // Si lee línea blanca, encendemos el led y si no lo apagamos
  if(lecturaSensor < 30){
    //Apagamos led
    digitalWrite(led ,LOW);
  }else{
    //Encendemos led
    digitalWrite(led ,HIGH);
  }
  delay(100);
}
