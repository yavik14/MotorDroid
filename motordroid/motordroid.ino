//MotorDroid
//Francisco Javier Rodriguez Ponce
//TFG


//IMPORTS

//Importamos las librerias 'Servo' y 'AFMotor' para el control de los diversos motores
#include <AFMotor.h>
#include <Servo.h>


//DEFICINIONES

//Creamos instancias de DCMotor: M1 y M2 con una frecuencia de 64KHZ,  M3 y M4 con la frecuencia maxima permitida de 1KHZ
AF_DCMotor motor1(1, MOTOR12_64KHZ); 
AF_DCMotor motor2(2, MOTOR12_64KHZ);
AF_DCMotor motor3(3);
AF_DCMotor motor4(4);

//Definimos los pines para los leds del coche
#define ledBlanco   46
#define ledAzul     47
#define ledIzq      48
#define ledDer      49
#define ledRojo     50
#define ledAtras    51

//Definimos los pines para el sensor de ultrasonidos
#define echo        22
#define trigger     23

//Definimos los pines para el laser
#define laserPin    30
#define laserGND    31

//Definimos los pines para el sensor de linea
#define lineIzq  A15
#define lineDer  A14


//Modos de funcionamiento de MotorDroid
//Modo 1: Modo Manual
//Modo 2: Modo Volante
//Modo 3: Modo Explorador
//Modo 4: Modo Circuito
int modo;

//Comandos enviados desde Android al Arduino via BT
char comando;

//Velocidades de los motores
int velocidad;
int velAcel;
int velAcelReducida;


//LEDS

//Definimos los estados de los leds como apagados salvo la luz Azul que estara encendida de partida
int izqEstado = LOW;
int derEstado = LOW;
int blaEstado = LOW;
int azuEstado = HIGH;
int rojEstado = LOW;
int atrEstado = LOW;

//Contadores de pulsaciones de los intermitentes
int pulsacionesIzq;
int pulsacionesDer;
int pulsacionesCen;

//Tiempo de parpadeo de los leds
const long parpadeo = 600;

//Tiempos previos
unsigned long tiempoPrevioI = 0; 
unsigned long tiempoPrevioD = 0; 
unsigned long tiempoPrevioC = 0; 


//CABEZA SERVOMOTOR

//Creamos dos instancias de Servomotor (eje X y eje Y)
Servo servomotorX; 
Servo servomotorY;

//Tiempo para realizar la actualización de la posición del Servomotor
unsigned long tiempoPrevioServo = 0;
//Intervalo de actualización del Servo
const long intervaloServo = 1;

//Posiciones 'Actual' y 'Siguiente' del Servo
int posicionActual = 75;
int posicionSiguiente = 76;
//Posiciones iniciales de los servos
int servoPosX = 75;
int servoPosY = 85;
//Posiciones maximas y minimas de los servos
const int servoXMax = 140;
const int servoXMin = 20;
const int servoYMax = 110;
const int servoYMin = 50;

//Permitir moverse al servo
boolean moverServo = false;
//Autoexplorar
boolean explorarActivo = false;

//Tiempos de parpadeo laser
const long parLaser = 700;
unsigned long tiempoPrevioLaser = 0; 
int estadoLaser = LOW;


//DISTANCIA

//Distancia con obstaculos
float distancia;
//Tiempo para realizar la actualización del Automático
unsigned long tiempoPrevioAutomatico = 0;
//Intervalo de actualización del Automático
const long intervaloAutomatico = 1000;


//LINE TRACKING

//Ultimo sensor activo antes de perder la linea a seguir
int ultimaLinea;

//Tiempos para la condicion de parada en caso de no encontrar la linea a seguir
unsigned long tiempoPrevioLinea = 0;
unsigned long tiempoActualLinea = 0;
//Tiempo de parada si no encuentra la linea a seguir
const long intervaloLinea = 3000;

//Dar comienzo a seguir la linea
boolean circuito = false;


//NUCLEO PRINCIPAL DE MOTORDROID (y de todo Sketch de Arduino)

// Funcion 'Setup', para la configuracion inicial de MotorDroid
void setup() {   
	//Modo 1 por defecto
	modo = 1;

	//Inicialicamos los puertos seriales
	Serial.begin(9600);
	//Inicializamos el Serial BT, que esta conectado puerto 3 de comunicacion
	Serial3.begin(9600);
	Serial3.flush();
	delay(500);

	//Velocidad de los motores
	velocidad = 0;

	// Inicializamos los pines de los leds como salida
	pinMode(ledBlanco, OUTPUT);
	pinMode(ledAzul, OUTPUT);  
	pinMode(ledIzq, OUTPUT);  
	pinMode(ledDer, OUTPUT);  
	pinMode(ledRojo, OUTPUT);   
	pinMode(ledAtras, OUTPUT);  

	//Cargamos la configuracion de los leds principales
	digitalWrite(ledAzul, azuEstado);
	digitalWrite(ledBlanco, blaEstado);
	digitalWrite(ledRojo, rojEstado);

	//Número de pulsaciones de los intermitentes
	pulsacionesIzq = 0;
	pulsacionesDer = 0;
	pulsacionesCen = 0;

	//Usamos los pines del módulo de motores para la conexion de los servomotores
	servomotorX.attach(10);
	servomotorY.attach(9);

	//Colocamos el servo en la posicion central para nuestro caso
	posInicialServos();

	//Inicializamos el laser como salida y lo apagamos
	pinMode(laserPin, OUTPUT);
	pinMode(laserGND, OUTPUT);
	apagarLaser();

	//Inicializamos los sensores de linea como entradas
	pinMode(lineIzq, INPUT);
	pinMode(lineDer, INPUT);

	//Inicializamos el puerto Serial  
	Serial.begin(9600);
	Serial.println("MOTORDROID");

}

// Funcion 'Loop', se va a refrescar con un delay de 30 y contiene toda la funcionalidad de Motordroid
void loop() {
	//MODO MANUAL
	if (modo == 1){
		//Leemos el comando 
		lecturaComandos();
		//Si nos llega un comando para cambiar de modo
		cambioModo(comando);
		//Acciones del Modo Manual
		modoManual();
			
	}//MODO VOLANTE
	else if (modo == 2){
		//Leemos el comando 
		lecturaComandos();
		//Si nos llega un comando para cambiar de modo
		cambioModo(comando);
		//Acciones del Modo Volante
		modoVolante();

	}//MODO EXPLORADOR
	else if (modo == 3){
		//Leemos el comando 
		lecturaComandos();
		//Si nos llega un comando para cambiar de modo
		cambioModo(comando);
		//Si nos llega el comando de Explorar cambiamos el estado
		if (comando == 'E'){
			autoExplorar();
		}
		//Comprobamos la distancia
		distancia = medirDistancia();
		//Si esta activo explorar automaticamente
		if(explorarActivo == true ){
			explorar();
		//Si no exploramos manualmente
		}else{
			explorarManual(); 
		}

	}//MODO CIRCUITO
	else if (modo == 4){ 
		//Leemos el comando 
		lecturaComandos();
		//Si nos llega un comando para cambiar de modo
		cambioModo(comando);
		//Cambiamos el estado del Modo Circuito
		if(comando == 'S'){
			startStopCircuito();
		}
		//Si esta activo
		if(circuito){
			modoCircuito();
		}	
	}
	//Dejamos 30 milisegundos de refresco
	delay(30);  
}


//ACCIONES COMPARTIDAS ENTRE ALGUNOS MODOS

//Lectura de comandos
void lecturaComandos(){
	//Comprueba que el serial está activo
	if(Serial3.available()){
		//Leemos comando
		comando = Serial3.read();
		//Liberamos el Serial BT
		Serial3.flush();
		//Mostramos por Serial el comando leído
		Serial.println(comando);			
		//Liberamos el Serial BT
		Serial3.flush();
	} 
}

//Funcion para cambiar de Modo de funcionamiento
void cambioModo(char com){
		//MODO MANUAL
	if (com == '1'){
		Serial.println("MODO MANUAL");
		modo = com - '0';
		frenar();
		posInicialServos();
		apagarLaser();
		delay(1000);
		//MODO VOLANTE
	}else if (com == '2'){
		Serial.println("MODO VOLANTE");
		modo = com - '0';
		frenar();
		posInicialServos();
		apagarLaser();
		delay(1000);
		//MODO EXPLORADOR
	}else if (com == '3'){
		Serial.println("MODO EXPLORADOR");
		modo = com - '0';
		frenar();
		posInicialServos();
		delay(1000);
		//MODO CIRCUITO
	}else if (com == '4'){
		Serial.println("MODO CIRCUITO");
		modo = com - '0';
		frenar();
		posInicialServos();
		apagarLaser();
		delay(1000);
	}
}

//Funcion que hace parar
void frenar(){
	digitalWrite(ledRojo, HIGH);
	digitalWrite(ledAtras, LOW);
	velocidad = 0;
	motor1.setSpeed(velocidad);
	motor2.setSpeed(velocidad);
	motor3.setSpeed(velocidad);
	motor4.setSpeed(velocidad);
	motor1.run(RELEASE);
	motor2.run(RELEASE);
	motor3.run(RELEASE);
	motor4.run(RELEASE);
}

//Funcion que cambia el estado de las luces delanteras
void luces(){
	//Si el estado era LOW lo cambia a HIGH, y viceversa
	azuEstado = !azuEstado;
	blaEstado = !blaEstado;
	//Establece  el estado de los pines con sus respectivos estados
	digitalWrite(ledAzul, azuEstado);
	digitalWrite(ledBlanco, blaEstado);
	//Manda el mensaje adecuado por Serial
	if(azuEstado == HIGH){
		Serial.println("Luces Cortas");
	}else{
		Serial.println("Luces Largas");
	}
}



//ACCIONES MODO MANUAL

//Comandos del Modo Manual
void modoManual(){
    //Cambio de luces largas (led blanco) a cortas (led azul) o viceversa
	if (comando == 'W'){
		luces();
	}
	//Frenamos
	else if (comando == 'F'){
		Serial.println("FRENAR");
		frenar();
	}
	//Aceleramos
	else if (comando == 'A'){
		Serial.println("ACELERAR");
		acelerar();
	}
	//Giro izquierda
	else if (comando == 'L'){
		Serial.println("GIRO IZQUIERDA");
		giroIzquierda();
	}
	//Giro derecha
	else if (comando == 'R'){
		Serial.println("GIRO DERECHA");
		giroDerecha();
	}
	//Vueltas en sentido antihorario
	else if (comando == 'M'){
		Serial.println("LOOPING LEFT");
		vueltaAntireloj();
	}
	//Vueltas en sentido horario
	else if (comando == 'N'){
		Serial.println("LOOPING RIGHT");
		vueltaReloj();
	}
	//Marcha atras
	else if (comando == 'B'){
		Serial.println("MARCHA ATRAS");
		marchaAtras();
	}
	//Luces estacionadoras
	else if (comando == 'E'){
		Serial.println("LUCES ESTACIONADORAS");
		pulsacionesCen += 1;
		Serial.println(pulsacionesCen);
	}
	//Intermitente Derecho
	else if (comando == 'D'){
		Serial.println("INTERMITENTE DERECHO");
		pulsacionesDer += 1;
		Serial.println(pulsacionesDer);
	}
	//Intermitente Izquierdo
	else if (comando == 'I'){
		Serial.println("INTERMITENTE IZQUIERDO");
		pulsacionesIzq += 1;
		Serial.println(pulsacionesIzq);
	}

	//Control del sistema de intermitentes
	controlIntermitentes();
}

//Control de los estados de los intermitentes
void controlIntermitentes(){
//Si pulsamos el estacionador reinicia los contadores de los intermitentes  
	if((pulsacionesCen%2) == 1){
		pulsacionesIzq = 0;
		pulsacionesDer = 0;
		parpadeoLedCEN();
	}else{

		//Si el módulo 2 de las pulsaciones es 1 activamos el parpadeo
		if((pulsacionesIzq%2) == 1 && (pulsacionesDer%2) == 0){
			parpadeoLedIZQ();
		}
		if((pulsacionesDer%2) == 1 && (pulsacionesIzq%2) == 0){
			parpadeoLedDER();
		}
		//Si el módulo 2 de las pulsaciones es 0 apagamos los leds
		if((pulsacionesDer%2) == 0 && (pulsacionesIzq%2) == 0){
			digitalWrite(ledDer, LOW);
			digitalWrite(ledIzq, LOW);
		}//Si un led estaba en función parpadeo al pulsar el otro, le sumamos 1 y lo apagamos
		if((pulsacionesIzq%2) == 1 && (pulsacionesDer%2) == 1){
			if(comando == 'I'){
				pulsacionesDer += 1;
				digitalWrite(ledDer, LOW);
			}else if(comando == 'D'){
				pulsacionesIzq += 1;
				digitalWrite(ledIzq, LOW);
			}
		}
	}
}

//Acelera
void acelerar(){
	digitalWrite(ledAtras, LOW);
	digitalWrite(ledRojo, LOW);
	velocidad = 230;
	motor1.setSpeed(velocidad);
	motor2.setSpeed(velocidad);
	motor3.setSpeed(velocidad);
	motor4.setSpeed(velocidad);
	motor1.run(FORWARD);
	motor2.run(BACKWARD);
	motor3.run(FORWARD);
	motor4.run(BACKWARD);
}

//Retrocede
void marchaAtras(){
	digitalWrite(ledAtras, HIGH);
	digitalWrite(ledRojo, LOW);
	velocidad = 150;
	motor1.setSpeed(velocidad);
	motor2.setSpeed(velocidad);
	motor3.setSpeed(velocidad);
	motor4.setSpeed(velocidad);
	motor1.run(BACKWARD);
	motor2.run(FORWARD);
	motor3.run(BACKWARD);
	motor4.run(FORWARD);
}

//Gira hacia la izquierda
void giroIzquierda(){
	digitalWrite(ledAtras, LOW);
	digitalWrite(ledRojo, LOW);
	velocidad = 200;
	motor2.setSpeed(velocidad);
	motor3.setSpeed(velocidad);
	motor1.run(RELEASE);
	motor2.run(BACKWARD);
	motor3.run(FORWARD);
	motor4.run(RELEASE);
}

//Gira hacia la derecha
void giroDerecha(){
	digitalWrite(ledAtras, LOW);
	digitalWrite(ledRojo, LOW);
	velocidad = 200;
	motor1.setSpeed(velocidad);
	motor4.setSpeed(velocidad);
	motor1.run(FORWARD);
	motor2.run(RELEASE);
	motor3.run(RELEASE);
	motor4.run(BACKWARD);
}

//Giro sobre si mismo en sentido horario
void vueltaReloj(){
	digitalWrite(ledAtras, LOW);
	digitalWrite(ledRojo, LOW);
	velocidad = 200;
	motor1.setSpeed(velocidad);
	motor2.setSpeed(velocidad);
	motor3.setSpeed(velocidad);
	motor4.setSpeed(velocidad);
	motor1.run(FORWARD);
	motor2.run(FORWARD);
	motor3.run(BACKWARD);
	motor4.run(BACKWARD);
}

//Giro sobre si mismo en sentido antihorario
void vueltaAntireloj(){
	digitalWrite(ledAtras, LOW);
	digitalWrite(ledRojo, LOW);
	velocidad = 200;
	motor1.setSpeed(velocidad);
	motor2.setSpeed(velocidad);
	motor3.setSpeed(velocidad);
	motor4.setSpeed(velocidad);
	motor1.run(BACKWARD);
	motor2.run(BACKWARD);
	motor3.run(FORWARD);
	motor4.run(FORWARD);}

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
	digitalWrite(ledIzq, izqEstado);
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
	digitalWrite(ledDer, derEstado);
}

//Funcion parpadeo de leds
void parpadeoLedCEN(){
	//Obtenemos el tiempo actual
	unsigned long tiempoActualC = millis();
	//Si la diferencia es mayor que el intervalo de tiempo de parpadeo es cuando actualizamos el estado del led
	if(tiempoActualC - tiempoPrevioC >= parpadeo) {
		//Guardamos el instante de tiempo
		tiempoPrevioC = tiempoActualC;   
		//Si el led esta encendido lo apagamos y viceversa
		if (derEstado == LOW){
			derEstado = HIGH;
			izqEstado = HIGH;
		}else{
			derEstado = LOW;
			izqEstado = LOW;
		}
	}
	// Actualizamos el led
	digitalWrite(ledDer, derEstado);
	digitalWrite(ledIzq, izqEstado);
}



//ACCIONES MODO VOLANTE

//Control del vehiculo con el acelerometro
void controlAcelerometro(int vel, char com){
	if(vel < 0){
		if(com == 'F'){
			Serial.println("GIRO IZQUIERDA MAXIMO");
			velAcelReducida = 0;
			motor1.setSpeed(velAcelReducida);
			motor2.setSpeed(velAcel);
			motor3.setSpeed(velAcel);
			motor4.setSpeed(velAcelReducida);
		}else if(com == 'G'){
			Serial.println("GIRO IZQUIERDA MEDIO");
			velAcelReducida = velAcel - 100;
			motor1.setSpeed(velAcelReducida);
			motor2.setSpeed(velAcel);
			motor3.setSpeed(velAcel);
			motor4.setSpeed(velAcelReducida);
		}else if(com == 'H'){
			Serial.println("GIRO IZQUIERDA MINIMO");
			velAcelReducida = velAcel - 40;
			motor1.setSpeed(velAcelReducida);
			motor2.setSpeed(velAcel);
			motor3.setSpeed(velAcel);
			motor4.setSpeed(velAcelReducida);
		}else if(com == 'I'){
			Serial.println("DIRECCION RECTA");
			motor1.setSpeed(velAcel);
			motor2.setSpeed(velAcel);
			motor3.setSpeed(velAcel);
			motor4.setSpeed(velAcel);
		}else if(com == 'J'){
			Serial.println("GIRO DERECHA MINIMO");
			velAcelReducida = velAcel - 40;
			motor1.setSpeed(velAcel);
			motor2.setSpeed(velAcelReducida);
			motor3.setSpeed(velAcelReducida);
			motor4.setSpeed(velAcel);
		}else if(com == 'K'){
			Serial.println("GIRO DERECHA MEDIO");
			velAcelReducida = velAcel - 100;
			motor1.setSpeed(velAcel);
			motor2.setSpeed(velAcelReducida);
			motor3.setSpeed(velAcelReducida);
			motor4.setSpeed(velAcel);
		}else if(com == 'L'){
			Serial.println("GIRO DERECHA MAXIMO");
			velAcelReducida = 0;
			motor1.setSpeed(velAcel);
			motor2.setSpeed(velAcelReducida);
			motor3.setSpeed(velAcelReducida);
			motor4.setSpeed(velAcel);
		}

	}else{
		velAcel = vel;
		if (vel == 0){
			Serial.println("PARADO");
			digitalWrite(ledAtras, LOW);
			digitalWrite(ledRojo, HIGH);
			motor1.run(RELEASE);
			motor2.run(RELEASE);
			motor3.run(RELEASE);
			motor4.run(RELEASE);
		}else if(vel == 140){
			Serial.println("MARCHA ATRAS");
			digitalWrite(ledAtras, HIGH);
			digitalWrite(ledRojo, LOW);
			motor1.run(BACKWARD);
			motor2.run(FORWARD);
			motor3.run(BACKWARD);
			motor4.run(FORWARD);
		}
		else {
			Serial.println("ACELERAR");
			digitalWrite(ledAtras, LOW);
			digitalWrite(ledRojo, LOW);
			motor1.run(FORWARD);
			motor2.run(BACKWARD);
			motor3.run(FORWARD);
			motor4.run(BACKWARD);
		}
	}
}

//Comandos del Modo Volante
void modoVolante(){
	//Cambiamos de luces largas a cortas o viceversa
	if (comando == 'W'){
		luces();
	}
	//Velocidad maxima
	else if (comando == 'A'){
		controlAcelerometro(235, ' ');
	}
	//Velocidad media
	else if (comando == 'B'){
		controlAcelerometro(200, ' ');
	}
	//Velocidad minima
	else if (comando == 'C'){
		controlAcelerometro(150, ' ');
	}
	//Parado
	else if (comando == 'D'){
		controlAcelerometro(0, ' ');
	}
	//Marcha atras
	else if (comando == 'E'){
		controlAcelerometro(140, ' ');
	}
	//Direccion izquierda maxima
	else if (comando == 'F'){
		controlAcelerometro(-1, comando);
	}
	//Direccion izquierda media
	else if (comando == 'G'){
		controlAcelerometro(-1, comando);
	}
	//Direccion izquierda minima
	else if (comando == 'H'){
		controlAcelerometro(-1, comando);
	}
	//Direccion recta
	else if (comando == 'I'){
		controlAcelerometro(-1, comando);
	}
	//Direccion derecha minima
	else if (comando == 'J'){
		controlAcelerometro(-1, comando);
	}
	//Direccion derecha media
	else if (comando == 'K'){
		controlAcelerometro(-1, comando);
	}
	//Direccion derecha maxima
	else if (comando == 'L'){
		controlAcelerometro(-1, comando);
	}
}


//ACCIONES MODO EXPLORADOR

//Cambiamos el estado entre autoexplorar o exploracion manual
void autoExplorar(){
	//Posiciones iniciales de los servos
	posInicialServos();
	if(explorarActivo == false){
		explorarActivo = true;	
		Serial.println("AUTOEXPLORAR");
	}else{
		explorarActivo = false;
		velocidad = 0;
		frenar();
		Serial.println("EXPLORACION MANUAL");
	}
}

//Logica de exploracion
void explorar(){
	if(distancia >= 0.6){
		movimientoServo();
		exploraUp();
		estadoLaser = LOW;
		digitalWrite(laserPin, estadoLaser);
	}else if(0.58 > distancia && distancia > 0.42 ){
		movimientoServo();
		cambiaEstadoLaser();
		if(posicionActual < 70){
			exploraRight();  
		}else{
			exploraLeft();
		}
	}else if (distancia <= 0.4){   
		estadoLaser = HIGH; 
		digitalWrite(laserPin, estadoLaser);
		exploraDown();   
	}
}

//Exploracion manual
void explorarManual(){
	//Movemos Motordroid adelante
	if (comando == 'W'){
		exploraUp();
		Serial.println("ADELANTE");
	}
	//Movemos Motordroid atras
	else if (comando == 'S'){
		exploraDown();
		Serial.println("ATRAS");
	}
	//Giramos Motordroid izquierda
	else if (comando == 'A'){
		exploraLeft();
		Serial.println("IZQUIERDA");
	}
	//Giramos Motordroid derecha
	else if (comando == 'D'){
		exploraRight();
		Serial.println("DERECHA");
	}
	//Paramos
	else if (comando == 'X'){
		explorarStop();
		Serial.println("STOP");
	}
	//Cabeza Motordroid arriba
	else if (comando == 'I'){
		servoUp();
		Serial.println("CABEZA ARRIBA");
	}
	//Cabeza Motordroid abajo
	else if (comando == 'K'){
		servoDown();
		Serial.println("CABEZA ABAJO");
	}
	//Cabeza Motodroid izquierda
	else if (comando == 'J'){
		servoLeft();
		Serial.println("CABEZA IZQUIERDA");
	}
	//Cabeza Motodroid derecha
	else if (comando == 'L'){
		servoRight();
		Serial.println("CABEZA DERECHA");
	}
	else if (comando == 'F'){
		//servoStop();
	}
	//Estados del laser
	if(distancia >= 0.6){
		estadoLaser = LOW;
		digitalWrite(laserPin, estadoLaser);
	}else if(0.6 > distancia && distancia > 0.3 ){
		cambiaEstadoLaser();
	}else if (distancia <= 0.3){   
		estadoLaser = HIGH; 
		digitalWrite(laserPin, estadoLaser);
	}
}

//Posiciones iniciales de los servos
void posInicialServos(){
	servoPosX = 75;
	servoPosY = 85;
	servomotorX.write(servoPosX);
	servomotorY.write(servoPosY);
}

//Movimientos del vehiculo explorador
void exploraUp(){
	digitalWrite(ledAtras, LOW);
	digitalWrite(ledRojo, LOW);
	velocidad = 100;
	motor1.setSpeed(velocidad);
	motor2.setSpeed(velocidad);
	motor3.setSpeed(velocidad);
	motor4.setSpeed(velocidad);
	motor1.run(FORWARD);
	motor2.run(BACKWARD);
	motor3.run(FORWARD);
	motor4.run(BACKWARD);}

void exploraDown(){
	digitalWrite(ledAtras, HIGH);
	digitalWrite(ledRojo, LOW);
	velocidad = 150;
	motor1.setSpeed(velocidad);
	motor2.setSpeed(velocidad);
	motor3.setSpeed(velocidad);
	motor4.setSpeed(velocidad);
	motor1.run(BACKWARD);
	motor2.run(FORWARD);
	motor3.run(BACKWARD);
	motor4.run(FORWARD);
}

void exploraLeft(){
	digitalWrite(ledAtras, LOW);
	digitalWrite(ledRojo, LOW);
	velocidad = 150;
	motor1.setSpeed(velocidad);
	motor2.setSpeed(velocidad);
	motor3.setSpeed(velocidad);
	motor4.setSpeed(velocidad);
	motor1.run(BACKWARD);
	motor2.run(BACKWARD);
	motor3.run(FORWARD);
	motor4.run(FORWARD);}

void exploraRight(){
	digitalWrite(ledAtras, LOW);
	digitalWrite(ledRojo, LOW);
	velocidad = 150;
	motor1.setSpeed(velocidad);
	motor2.setSpeed(velocidad);
	motor3.setSpeed(velocidad);
	motor4.setSpeed(velocidad);
	motor1.run(FORWARD);
	motor2.run(FORWARD);
	motor3.run(BACKWARD);
	motor4.run(BACKWARD);}

void explorarStop(){
	digitalWrite(ledAtras, LOW);
	digitalWrite(ledRojo, HIGH);
	velocidad=0;
	motor1.setSpeed(velocidad);
	motor2.setSpeed(velocidad);
	motor3.setSpeed(velocidad);
	motor4.setSpeed(velocidad);
	motor1.run(RELEASE);
	motor2.run(RELEASE);
	motor3.run(RELEASE);
	motor4.run(RELEASE);
}

//Movimientos de la cabeza exploradora
void servoUp(){
	servoPosY -= 10;
	if(servoPosY >= servoYMin){
		servomotorY.write(servoPosY);
	}
}

void servoDown(){
	servoPosY += 10;
	if(servoPosY <= servoYMax){
		servomotorY.write(servoPosY);
	}
}

void servoLeft(){
	servoPosX -= 20;
	if(servoPosX <= servoXMax){
		servomotorX.write(servoPosX);
	}
}

void servoRight(){
	servoPosX += 20;
	if(servoPosX >= servoXMin){
		servomotorX.write(servoPosX);
	}
}

//Funcion que controla el movimiento del servomotor donde está colocado el sensor de ultrasonidos
void movimientoServo(){
	//Comprobamos el tiempo actual
	unsigned long tiempoActualServo = millis();
	//Si la diferencia entre el tiempo actual y previo es mayor que la del intervalo de actualizacion actualizamos el tiempo actual
	if(tiempoActualServo - tiempoPrevioServo >= intervaloServo){
		tiempoPrevioServo = tiempoActualServo;
		//si la posicion siguiente es mayor que la actual
		if (posicionSiguiente > posicionActual){
			//comprobamos que la posicion siguiente es menor que 90
			if (posicionSiguiente < 140){
				//Actualizamos la posicion del servomotor y de las posiciones para la proxma actualizacion
				servomotorX.write(posicionSiguiente);
				posicionActual += 2;
				posicionSiguiente += 2;
				// si es mayor que 90, comenzamos a girar hacia el otro sentido
			} else{
				//actualizamos la posicion del servomotor y la posicion siguiente para que sea menor que la actual
				posicionSiguiente -= 2;
				servomotorX.write(posicionSiguiente);
			}
		}
		//si la posicion siguiente es menor que la actual
		if (posicionSiguiente < posicionActual){
			//comprobamos que la poscion siguiente es mayor que 0
			if (posicionSiguiente > 20){
				//actualizamos la posicion del servomotor y la posicion siguiente para la proxima actualizacion
				servomotorX.write(posicionSiguiente);
				posicionActual -= 2;
				posicionSiguiente -= 2;
				//si es menor que 0, comenzamos a girar en el otro sentido
			} else{
				//actualizamos la posicion del servomotot y la posicion siguiente para la proxima actualizacion
				posicionSiguiente += 2;
				servomotorX.write(posicionSiguiente);
			}
		}
	}
}

//Funcion que calcula la distancia respecto a un objeto con el sensor de ultrasonidos
float medirDistancia(){
	//Inicializamos el sensor
	digitalWrite(trigger, LOW);
	delayMicroseconds(5);
	//Comenzamos las mediciones
	//Enviamos una señal activando la salida trigger durante 10 microsegundos
	digitalWrite(trigger, HIGH);
	delayMicroseconds(10);
	//Medimos el ancho del pulso, cuando el ping esta a HIGH cuenta el tiempo que transcurre hasta que sea LOW
	distancia = pulseIn(echo, HIGH);
	//Adquirimos los datos y los convertimos a cm
	float res = distancia * 0.0001657;
	delay(10);
	return res;
}

//Funcion que controla el estado de parpadeo del laser de la cabeza
void parpadeoLaser(){
	//Obtenemos el tiempo actual
	unsigned long tiempoActualLaser = millis();
	if(tiempoActualLaser - tiempoPrevioLaser >= parLaser){
		tiempoPrevioLaser = tiempoActualLaser;
		cambiaEstadoLaser();
	}
}

//Funcion que apaga el laser
void apagarLaser(){
	estadoLaser = LOW;
	digitalWrite(laserPin, estadoLaser);
	delay(500);
}

//Funcion que cambia el estado del laser
void cambiaEstadoLaser(){
	if(estadoLaser == LOW){
			estadoLaser = HIGH;
			digitalWrite(laserPin, estadoLaser);
		}else{
			estadoLaser = LOW;
			digitalWrite(laserPin, estadoLaser);
		}
}



//ACCIONES MODO CIRCUITO

//Inicia o para el modo circuito
void startStopCircuito(){
	if(circuito == false){
		circuito = true;
		Serial.println("START CIRCUIT");
	}else{
		circuito = false;
		frenar();
		Serial.println("STOP CIRCUIT");
	}
}

//Movimiento del modo Circuito
void modoCircuito(){
	//Comprobamos las lecturas de los sensores
	int sensorIzq = compruebaLineaIzquierda();
	int sensorDer = compruebaLineaDerecha();
	//Si los dos sensores leen la linea, vamos recto
	if(sensorIzq == 1 && sensorDer == 1){
		Serial.println("SIGUE LINEA");
		sigueLinea();
		tiempoPrevioLinea = 0;
		tiempoActualLinea = 0;
	//Si solo el sensor izquierdo lee la linea, giramos izquierda
	}else if(sensorIzq == 1 && sensorDer == 0){
		Serial.println("LINEA A LA IZQUIERDA");
		lineaIzquierda();
		ultimaLinea = 1;
		tiempoPrevioLinea = 0;
		tiempoActualLinea = 0;
	//Si solo el sensor derecho lee la linea, giramos derecha
	}else if(sensorIzq == 0 && sensorDer == 1){
		Serial.println("LINEA A LA DERECHA");
		lineaDerecha();
		ultimaLinea = 2;
		tiempoPrevioLinea = 0;
		tiempoActualLinea = 0;
	//Si ninguno de los dos lee linea, hay que comprobar cual fue la ultima y seguir esa direccion. Si en tres segundos no encuentra la linea se para.
	}else if(sensorIzq == 0 && sensorDer == 0){
		if(tiempoPrevioLinea == 0){
			tiempoPrevioLinea = millis();
		}
		tiempoActualLinea = millis();
		if(tiempoActualLinea - tiempoPrevioLinea >= intervaloLinea){
			frenar();
			Serial.println("LINEA PERDIDA");
		}else if(ultimaLinea = 1){
			lineaIzquierda();
		}else if(ultimaLinea = 2){
			lineaIzquierda();
		} 
	}
}

//Funciones que leen los sensores y crean señales digitales para su uso. Nota que cada sensor funciona al contrario que el otro.
int compruebaLineaDerecha(){
	int res1;
	//Hacemos la lectura analogica del sensor
	int aux1 = analogRead(lineDer);
	//Si el valor es mayor que 700, en este caso el sensor captura la linea blanca, valor 1
	if(aux1 > 700){
		res1 = 1;
		//Si no es la linea blanca, valor 0
	}else{
		res1 = 0;
	}
	//Devolvemos el resultado
	return res1;
}

//Analogo al anterior metodo, salvo que la condicion es que el valor leido del sensor debe de ser menor que 200 para ser linea blanca lo capturado
int compruebaLineaIzquierda(){
	int res2;
	int aux2 = analogRead(lineIzq);
	if(aux2 < 200){
		res2 = 1;
	}else{
		res2 = 0;
	}
	return res2;
}

//Funcion que hace que vaya en líne recta
void sigueLinea(){
	digitalWrite(ledAtras, LOW);
	digitalWrite(ledRojo, LOW);
	digitalWrite(ledDer, LOW);
	digitalWrite(ledIzq, LOW);
	velocidad = 150;
	motor1.setSpeed(velocidad);
	motor2.setSpeed(velocidad);
	motor3.setSpeed(velocidad);
	motor4.setSpeed(velocidad);
	motor1.run(FORWARD);
	motor2.run(BACKWARD);
	motor3.run(FORWARD);
	motor4.run(BACKWARD);
}

//Cuando pierde la linea por la izquieda gira en ese sentido
void lineaIzquierda(){
	digitalWrite(ledAtras, LOW);
	digitalWrite(ledRojo, LOW);
	motor1.setSpeed(velocidad);
	motor2.setSpeed(velocidad);
	motor3.setSpeed(velocidad);
	motor4.setSpeed(velocidad);
	motor1.run(RELEASE);
	motor2.run(BACKWARD);
	motor3.run(FORWARD);
	motor4.run(RELEASE);
}

//Cuando pierde la linea por la derecha gira ebn ese sentido
void lineaDerecha(){
	digitalWrite(ledAtras, LOW);
	digitalWrite(ledRojo, LOW);
	motor1.setSpeed(velocidad);
	motor2.setSpeed(velocidad);
	motor3.setSpeed(velocidad);
	motor4.setSpeed(velocidad);
	motor1.run(FORWARD);
	motor2.run(RELEASE);
	motor3.run(RELEASE);
	motor4.run(BACKWARD);
}



//FUNCIONES DE TESTEO

//Funcion test motores
void testMotores(){
	//servomotor.write(115); 
	motor1.setSpeed(200);
	Serial.println("115");
	Serial.println("Adelante");
	motor1.run(FORWARD);
	delay(3000);
	//Movimiento desde la posicion 180 hasta la 0 
	//servomotor.write(85);
	Serial.println("85");
	Serial.println("Atras");
	motor1.run(BACKWARD); 
	delay(3000);
	//Movimiento desde la posicion 0 hasta la 90
	//servomotor.write(100); 
	Serial.println("100");
	Serial.println("Parado");
	motor1.run(RELEASE);
	delay(3000); 
}

void testMotoresDC(){
	//servomotor.write(115); 
	motor1.setSpeed(255);
	motor2.setSpeed(255);
	motor3.setSpeed(255);
	motor4.setSpeed(255);
	motor1.run(FORWARD);
	motor2.run(BACKWARD);
	motor3.run(FORWARD);
	motor4.run(BACKWARD);
	delay(3000);
	Serial.println("100");
	Serial.println("Parado");
	motor1.run(RELEASE);
	motor2.run(RELEASE);
	motor3.run(RELEASE);
	motor4.run(RELEASE);
	delay(3000); 
	//Movimiento desde la posicion 180 hasta la 0 
	//servomotor.write(85);
	Serial.println("85");
	Serial.println("Atras");
	motor1.run(BACKWARD);
	motor2.run(FORWARD);
	motor3.run(BACKWARD);
	motor4.run(FORWARD);
	delay(3000);
	//Movimiento desde la posicion 0 hasta la 90
	//servomotor.write(100); 
	Serial.println("100");
	Serial.println("Parado");
	motor1.run(RELEASE);
	motor2.run(RELEASE);
	motor3.run(RELEASE);
	motor4.run(RELEASE);
	delay(3000); 
}

//Funcion para parpadeo de todos los leds
void parpadeoLeds(){
	// encendemos los leds (Ponemos el voltaje a 'HIGH')
	digitalWrite(ledBlanco, HIGH);
	digitalWrite(ledAzul, HIGH);  
	digitalWrite(ledIzq, HIGH);
	digitalWrite(ledDer, HIGH);
	digitalWrite(ledRojo, HIGH);   
	digitalWrite(ledAtras, HIGH);
	// esperamos medio segundo
	delay(500);               
	// apagamos los leds (Ponemos el voltaje a 'LOW')
	digitalWrite(ledBlanco, LOW);
	digitalWrite(ledAzul, LOW);
	digitalWrite(ledIzq, LOW);
	digitalWrite(ledDer, LOW);
	digitalWrite(ledRojo, LOW);    
	digitalWrite(ledAtras, LOW);
	// esperamos medio segundo
	delay(500);    
}