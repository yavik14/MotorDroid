//Parpadeo sin Delay

//Definimos el pin para el led
#define led   13

//Estado del led
int estadoLed = LOW;

// Usamos unsined long para variables de tiempo
// guarda el momento en el que el led fue actualizado (es decir, cuando se apagó o encendió por última vez)
unsigned long tiempoPrevio = 0;        

//Intervalo de tiempo del parpadeo
const long intervalo = 1000;           

// Función 'Setup'
void setup() {
  // Inicializamos el pin 'led' como salida
  pinMode(led, OUTPUT);      
}

// Función 'Loop'
void loop()
{
  //Obtenemos el tiempo actual
  unsigned long tiempoActual = millis();
  //Si la diferencia es mayor que el intervalo de tiempo de parpadeo es cuando actualizamos el estado del led
  if(tiempoActual - tiempoPrevio >= intervalo) {
    //Guardamos el instante de tiempo
    tiempoPrevio = tiempoActual;   

    //Si el led esta encendido lo apagamos y viceversa
    if (estadoLed == LOW)
      estadoLed = HIGH;
    else
      estadoLed = LOW;

    // actualizamos el led
    digitalWrite(led, estadoLed);
  }
}
