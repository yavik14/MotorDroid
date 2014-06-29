//Intermitentes led  boton

//Definimos los pines de los botones
#define IZQBOT  1
#define DERBOT  2
//Definimos los pintes de los leds
#define IZQLED  11
#define DERLED  12

//Definimos estados para los botones
boolean izq_fue_pulsado;
boolean der_fue_pulsado;

//Estados de los leds iniciados como apagados
int izqEstado = LOW;
int derEstado = LOW;

//Contadores de pulsaciones
int pulsacionesIzq;
int pulsacionesDer;

//Tiempo de parpadeo
const long parpadeo = 600;

//Tiempos previos
unsigned long tiempoPrevioI = 0; 
unsigned long tiempoPrevioD = 0; 

//Función 'Setup'
void setup(){
  //Botones como entrada y abiertos
  pinMode(IZQBOT, INPUT);
  digitalWrite(IZQBOT, HIGH);
  
  pinMode(DERBOT, INPUT);
  digitalWrite(DERBOT, HIGH);
  
  //Leds como salida
  pinMode(IZQLED, OUTPUT);
  pinMode(DERLED, OUTPUT);
  
  //Inicialmente los botones no están pulsados
  izq_fue_pulsado = false;
  der_fue_pulsado = false;
  
  //Número de pulsaciones
  pulsacionesIzq = 0;
  pulsacionesDer = 0;
  
  //Inicializamos el puerto Serial
  Serial.begin(9600);
}

//Funcion pulsar izquierda
boolean eventoPulsoIzq(){
  //Evento a devolver
  boolean evento;
  //Comprobamos estado actual del botón
  int izq_esta_pulsado = !digitalRead(IZQBOT);
  //Boton pulsado ahora y no estaba pulsado
  evento = izq_esta_pulsado & !izq_fue_pulsado;
  //Actualizamos el estado del boton
  izq_fue_pulsado = izq_esta_pulsado;
  //Devolvemos el evento
  return evento;
}

//Funcion pulsar derecha (Análogo a eventoPulsoIzq)
boolean eventoPulsoDer(){
  //Evento a devolver
  boolean evento;
  //Comprobamos estado actual del botón
  int der_esta_pulsado = !digitalRead(DERBOT);
  //Boton pulsado ahora y no estaba pulsado
  evento = der_esta_pulsado & !der_fue_pulsado;
  //Actualizamos el estado del boton
  der_fue_pulsado = der_esta_pulsado;
  //Devolvemos el evento
  return evento;
}

//Funcion parpadeo de led izquierdo
void parpadeoLedIZQ(){
  //Obtenemos el tiempo actual
  unsigned long tiempoActualI = millis();
  //Si la diferencia es mayor que el intervalo de tiempo de parpadeo es cuando actualizamos el estado del led
  if(tiempoActualI - tiempoPrevioI >= parpadeo) {
    //Guardamos el instante de tiempo
    tiempoPrevioI = tiempoActualI;   
    //Si el led esta encendido lo apagamos y viceversa
    if (izqEstado == LOW)
      izqEstado = HIGH;
    else
      izqEstado = LOW;
  }
    // Actualizamos el led
    digitalWrite(IZQLED, izqEstado);
}

//Funcion parpadeo de led derecho
void parpadeoLedDER(){
  //Obtenemos el tiempo actual
  unsigned long tiempoActualD = millis();
  //Si la diferencia es mayor que el intervalo de tiempo de parpadeo es cuando actualizamos el estado del led
  if(tiempoActualD - tiempoPrevioD >= parpadeo) {
    //Guardamos el instante de tiempo
    tiempoPrevioD = tiempoActualD;   
    //Si el led esta encendido lo apagamos y viceversa
    if (derEstado == LOW)
      derEstado = HIGH;
    else
      derEstado = LOW;
  }
    // Actualizamos el led
    digitalWrite(DERLED, derEstado);
}

//Funcion que añade linea al Serial
void lineaSerial(){
    static int contador = 0;
  if((++contador & 0x3f)==0){
    Serial.println();
  }
}

//Función 'Loop'
void loop(){
  
  //Capturamos los eventos de los botones
  boolean izqpulsado = eventoPulsoIzq();
  boolean derpulsado = eventoPulsoDer();
  
  //Actualizamos las pulsaciones
  pulsacionesIzq += izqpulsado;
  pulsacionesDer += derpulsado;
  
  //Si el módulo 2 de las pulsaciones es 1 activamos el parpadeo
  if((pulsacionesIzq%2) == 1 && (pulsacionesDer%2) == 0){
    parpadeoLedIZQ();
  }
  if((pulsacionesDer%2) == 1 && (pulsacionesIzq%2) == 0){
    parpadeoLedDER();
  }
  //Si el módulo 2 de las pulsaciones es 0 apagamos los leds
  if((pulsacionesDer%2) == 0 && (pulsacionesIzq%2) == 0){
    digitalWrite(DERLED, LOW);
    digitalWrite(IZQLED, LOW);
  }//Si un led estaba en función parpadeo al pulsar el otro, le sumamos 1 y lo apagamos
    if((pulsacionesIzq%2) == 1 && (pulsacionesDer%2) == 1){
      if(izqpulsado == 1){
        pulsacionesDer += 1;
        digitalWrite(DERLED, LOW);
      }else if(derpulsado == 1){
        pulsacionesIzq += 1;
        digitalWrite(IZQLED, LOW);
      }
  }
  
  //Mostramos el número de pulsaciones
  Serial.print(pulsacionesIzq);
  Serial.print(pulsacionesDer);
  //Mostramos por Serial las pulsaciones
  Serial.print(izqpulsado ? "I" : ".");
  Serial.print(derpulsado ? "D" : ".");
  
  //Añadimos líneas cada cierto tiempo
  lineaSerial();
  
  //Imponemos un retraso en el loop
  delay(20);
}

