//Configuracion Modulo Bluetooth

//Función 'Setup'
void setup(){
  //Inicializamos el Serial
  Serial.begin(9600);
  //Esperamos 10 segundos para conectar el BT a los pines
  Serial.println("CONECTE EL BT MODULE");
  delay(10000);
  //Mandamos el comando AT para comenzar a configurar
  Serial.print("AT");
   Serial.println("CONFIGURANDO");
  //Siempre que mandemos un comando al BT tenemos que esperar un segundo
  delay(1000);
  //Cambiamos de nombre enviando AT+NAME seguido del nombre deseado
  Serial.print("AT+NAMEMOTORDROID");
   Serial.println("Configurando Nombre");
  //Esperamos los cambios
  delay(1000);
  //Cambiamos la velocidad del módulo
  Serial.print("AT+BAUD4");
   Serial.println("Configurando velocidad");
  //Esperamos los cambios
  delay(1000);
  //Cambiamos la contraseña enviando AT+PIN y la que queramos
   Serial.println("Configurando pin");
  Serial.print("AT+PIN1990");
  //Esperamos los cambios
  delay(1000);
  //Mostramos por puerto serial la finalización de la configuración
  Serial.println("TERMINADO");
}

//Función 'Loop'
void loop(){
  //No hacemos nada
}
