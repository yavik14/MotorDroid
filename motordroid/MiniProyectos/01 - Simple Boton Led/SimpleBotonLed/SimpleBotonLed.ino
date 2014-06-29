//Simple Boton Led

//Definimos los pines para el led y el botón
#define  boton  2
#define  led    12

//Definimos una variable para saber el estado del botón
int estadoBoton = 0;

//Método 'Setup'
void setup(){
  //Inicializamos el pin 'led' como salida
  pinMode(led, OUTPUT);
  digitalWrite(led, HIGH);
  //Inicializamos el pin 'botón' como entrada
  pinMode(boton, INPUT);
  //Modo 'pull up'
  digitalWrite(boton, HIGH);
}

//Método 'Loop'
void loop(){
  //Lee el valor del estado de 'boton'
  estadoBoton = digitalRead(boton);
  //Comprobamos si está pulsado
  if (estadoBoton == LOW){
    //Encendemos el led
    digitalWrite(led ,HIGH);
  }else{
    //Apagamos el led
    digitalWrite(led, LOW);
  }
}
