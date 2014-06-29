//Parpadeo con Delay

//Definimos el pin para el led
#define led   8

// Función 'Setup'
void setup() {                
  // Inicializamos el pin 'led' como salida
  pinMode(led, OUTPUT);     
}

// Función 'Loop'
void loop() {
  digitalWrite(led, HIGH);   // encendemos el LED (Ponemos el voltaje a 'HIGH')
  delay(500);               // esperamos medio segundo
  digitalWrite(led, LOW);    // apagamos el LED (Ponemos el voltaje a 'LOW')
  delay(500);               // esperamos medio segundo
}
