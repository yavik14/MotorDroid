//Distancia Ultrasonido

//Definimos los pines trigger y echo
const int trigger=11;
const int echo=10;
 //Definimos una variable distancia
float distancia;
 
void setup(){
  Serial.begin(9600);
  //Ponemos trigger como salida
  pinMode(trigger,OUTPUT);
  //Ponemos echo como entrada
  pinMode(echo,INPUT);
}
 
void loop(){
//Inicializamos el sensor
  digitalWrite(trigger,LOW);
  delayMicroseconds(5);
// Comenzamos las mediciones
// Enviamos una señal activando la salida trigger durante 10 microsegundos
  digitalWrite(trigger,HIGH);
  delayMicroseconds(10);
  digitalWrite(trigger,LOW);
// Adquirimos los datos y convertimos la medida a centrimetros
  distancia=pulseIn(echo,HIGH); // Medimos el ancho del pulso
                              // (Cuando la lectura del pin sea HIGH medira
                              // el tiempo que transcurre hasta que sea LOW
 distancia=distance*0.01657;
// Enviamos los datos medidos a traves del puerto serie
  Serial.print(distancia);
  Serial.println(" cm");
  delay(100);
}
