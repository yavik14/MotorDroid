//MotorDroid
//Francisco Javier Rodriguez Ponce
//TFG

//Importamos las librerias 'Servo' y 'AFMotor' para el control de los motores
#include <AFMotor.h>
#include <Servo.h>


//Creamos instancias de DCMotor, M1 y M2 con una frecuencia de 64KHZ, a su vez M3 y M4 con la frecuencia maxima permitida de 1KHZ
AF_DCMotor motor1(1, MOTOR12_64KHZ); 
AF_DCMotor motor2(2, MOTOR12_64KHZ);
AF_DCMotor motor3(3);
AF_DCMotor motor4(4);

//Definimos los pines para el sensor de ultrasonidos
#define echo        22
#define trigger     23

//Definimos los pines para el sensor de linea
#define lineIzq  A15
#define lineDer  A14

//Definimos los pines para los leds del coche
#define ledBlanco   46
#define ledAzul     47
#define ledIzq      48
#define ledDer      49
#define ledRojo     50
#define ledAtras    51

//Modos de funcionamiento de MotorDroid
//Modo 1: Control BT mediante botones
//Modo 2: Control BT mediante giroscopio/acelerómetro
//Modo 3: Modo Automatico
//Modo 4: Modo Circuito
int modo;

//Comandos enviados desde Android al Arduino via BT
char comando;


int velocidad;
int velAcel;

//LEDS
//Definimos estados para los controles de los leds
boolean izq_fue_pulsado;
boolean der_fue_pulsado;
boolean cen_fue_pulsado;
boolean bla_fue_pulsado;
boolean azu_fue_pulsado;

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


//SERVOMOTOR
//Creamos una instancia de Servomotor
Servo servomotor;
//Tiempo para realizar la actualización de la posición del Servomotor
unsigned long tiempoPrevioServo = 0;
//Intervalo de actualización del Servo
const long intervaloServo = 1;
//Posiciones 'Actual' y 'Siguiente' del Servo
int posicionActual = 0;
int posicionSiguiente = 1;


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

	// Inicializamos los pines de los leds como salida
	pinMode(ledBlanco, OUTPUT);
	pinMode(ledAzul, OUTPUT);  
	pinMode(ledIzq, OUTPUT);  
	pinMode(ledDer, OUTPUT);  
	pinMode(ledRojo, OUTPUT);   
	pinMode(ledAtras, OUTPUT);  

	digitalWrite(ledAzul, azuEstado);
	digitalWrite(ledBlanco, blaEstado);
	digitalWrite(ledRojo, rojEstado);

	//Usamos el Pin SER10 para la conexion del servomotor con el modulo de motores
	servomotor.attach(10);
	//Colocamos el servo en la posicion central para nuestro caso
	servomotor.write(38);

	//Inicializamos los sensores de linea como entradas
	pinMode(lineIzq, INPUT);
	pinMode(lineDer, INPUT);

	//Número de pulsaciones de los intermitentes
	pulsacionesIzq = 0;
	pulsacionesDer = 0;
	pulsacionesCen = 0;

	//Velocidad de los motores
	velocidad = 0;

	//Inicializamos el puerto Serial  
	Serial.begin(9600);
	Serial.println("Motordroid");

}



// Funcion 'Loop', que se va a refrescar con un delay de 30
void loop() {
	//parpadeoLeds();
	//testMotores();
	//testMotoresDC();
	//movimientoServo();


	if (modo == 1){
		//Esperamos a recibir datos
		if(Serial3.available()){
			//Aqui comienza a leer comandos
			//Serial.println("Listo para recibir comandos");
			//Leemos comando
			comando = Serial3.read();
			//Liberamos el Serial BT
			Serial3.flush();
			//Mostramos por Serial el comando llegado
			Serial.println(comando);

		}

		//Si nos llega un comando para cambiar de modo
		cambioModo(comando);
		//Si pulsamos click encenderemos o apagaremos el led amarillo
		if (comando == 'W'){
			//Serial.println("Encendemos luz Azul");
			luces();
		}
		else if (comando == 'F'){
			//Serial.println("Encendemos luz Roja");
			frenar();
		}
		else if (comando == 'A'){
			acelerar();
		}
		else if (comando == 'L'){
			giroIzquierda();
		}
		else if (comando == 'R'){
			giroDerecha();
		}
		else if (comando == 'B'){
			//Serial.println("Encendemos luz Roja");
			marchaAtras();
		}
		else if (comando == 'E'){
			//Serial.println("Encendemos luz Roja");
			pulsacionesCen += 1;
			Serial.println(pulsacionesCen);
		}
		else if (comando == 'D'){
			//Serial.println("Encendemos luz Roja");
			pulsacionesDer += 1;
			Serial.println(pulsacionesDer);
		}
		else if (comando == 'I'){
			//Serial.println("Encendemos luz Roja");
			pulsacionesIzq += 1;
			Serial.println(pulsacionesIzq);
		}


		Serial3.flush();
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

	}else if (modo == 2){
		if(Serial3.available()){
			//Aquí comienza a leer comandos
			//Serial.println("Listo para recibir comandos");
			//Leemos comando
			comando = Serial3.read();
			//Liberamos el Serial BT
			Serial3.flush();
			//Mostramos por Serial el comando leído
			Serial.println(comando); 
			Serial3.flush();
		} 
		//Si nos llega un comando para cambiar de modo
		cambioModo(comando); 
		if (comando == 'W'){
			luces();
		}
		else if (comando == 'A'){
			velocidadMax();
		}
		else if (comando == 'B'){
			velocidadMedia();
		}
		else if (comando == 'C'){
			velocidadMin();
		}
		else if (comando == 'D'){
			velocidadCero();
		}
		else if (comando == 'E'){
			velocidadAtras();
		}
		else if (comando == 'F'){
			direccionIzquierdaMax();
		}
		else if (comando == 'G'){
			direccionIzquierdaMed();
		}
		else if (comando == 'H'){
			direccionIzquierdaMin();
		}
		else if (comando == 'I'){
			direccionRecto();
		}
		else if (comando == 'J'){
			direccionDerechaMin();
		}
		else if (comando == 'K'){
			direccionDerechaMed();
		}
		else if (comando == 'L'){
			direccionDerechaMax();
		}

	}else if (modo == 3){
		movimientoServo();
		distancia = medirDistancia();
		Serial.println(distancia);
		if(Serial3.available()){
			//Aquí comienza a leer comandos
			//Serial.println("Listo para recibir comandos");
			//Leemos comando
			comando = Serial3.read();
			//Liberamos el Serial BT
			Serial3.flush();
			//Mostramos por Serial el comando leído
			Serial.println(comando);
			//Si nos llega un comando para cambiar de modo
			cambioModo(comando);
			Serial3.flush();
		} else {
			//Si no se lee ningún comando
			if(distancia >= 0.6){
				acelerar();
			}else if(0.6 > distancia && distancia > 0.3 ){
				if(posicionActual < 40){
					giroIzquierda();
				}else{
					giroDer();   
				}
			}else if (distancia <= 0.3){   
				marchaAtras();   
			} 
		}

	}else if (modo ==4){ //MODO CIRCUITO
		//Aquí comienza a leer comandos
		if(Serial3.available()){
			//Leemos comando
			comando = Serial3.read();
			//Liberamos el Serial BT
			Serial3.flush();
			//Mostramos por Serial el comando leído
			Serial.println(comando);
			//Si nos llega un comando para cambiar de modo
			cambioModo(comando);
			//Liberamos el Serial BT
			Serial3.flush();
			//delay(30);
		} 
		//Comprobamos las lecturas de los sensores
		int sensorIzq = compruebaLineaIzquierda();
		int sensorDer = compruebaLineaDerecha();
		Serial.print(sensorIzq);
		Serial.print(" - ");
		Serial.println(sensorDer);
		//Si los dos sensores leen la linea, aceleramos
		if(sensorIzq == 1 && sensorDer == 1){
			sigueLinea();
			tiempoPrevioLinea = 0;
			tiempoActualLinea = 0;
			//Si solo el sensor izquierdo lee la linea, giramos izquierda
		}else if(sensorIzq == 1 && sensorDer == 0){
			lineaIzquierda();
			ultimaLinea = 1;
			tiempoPrevioLinea = 0;
			tiempoActualLinea = 0;
			//Si solo el sensor derecho lee la linea, giramos derecha
		}else if(sensorIzq == 0 && sensorDer == 1){
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
			Serial.print(tiempoActualLinea);
			Serial.print(" - ");
			Serial.print(tiempoPrevioLinea);
			Serial.print(" = ");
			Serial.println(tiempoActualLinea-tiempoPrevioLinea);
			if(tiempoActualLinea - tiempoPrevioLinea >= intervaloLinea){
				frenar();
			}else if(ultimaLinea = 1){
				lineaIzquierda();
			}else if(ultimaLinea = 2){
				lineaIzquierda();
			} 
		}
	}
	//Dejamos 30 milisegundos de refresco
	delay(30);  
}







//Funcion para cambiar de Modo de funcionamiento
void cambioModo(char com){
	//MODO MANUAL
	if (com == '1'){
		modo = com - '0';
		frenar();
		delay(1000);
		//MODO VOLANTE
	}else if (com == '2'){
		modo = com - '0';
		frenar();
		delay(1000);
		//MODO EXPLORADOR
	}else if (com == '3'){
		modo = com - '0';
		frenar();
		delay(1000);
		//MODO CIRCUITO
	}else if (com == '4'){
		modo = com - '0';
		frenar();
		delay(1000);
	}
}


//ACCIONES COMPARTIDAS ENTRE ALGUNOS MODOS

//Funcion frenar, para el coche
void frenar(){
	digitalWrite(ledRojo, HIGH);
	digitalWrite(ledAtras, LOW);
	motor1.setSpeed(0);
	motor2.setSpeed(0);
	motor3.setSpeed(0);
	motor4.setSpeed(0);
}

//Funcion que cambia el estado de las luces delanteras
void luces(){
	//Si el estado era 0 lo cambia a 1, y viceversa
	azuEstado = !azuEstado;
	blaEstado = !blaEstado;
	//Establece  el estado de los pines con sus respectivos estados
	digitalWrite(ledAzul, azuEstado);
	digitalWrite(ledBlanco, blaEstado);
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


//ACCIONES MODO MANUAL

void acelerar(){
	digitalWrite(ledAtras, LOW);
	digitalWrite(ledRojo, LOW);
	velocidad = 200;
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
	velocidad = 180;
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
	motor1.setSpeed(0);
	motor2.setSpeed(velocidad);
	motor3.setSpeed(velocidad);
	motor4.setSpeed(0);
	motor1.run(RELEASE);
	motor2.run(BACKWARD);
	motor3.run(FORWARD);
	motor4.run(RELEASE);
}

//Gira hacia la derecha
void giroDerecha(){
	digitalWrite(ledAtras, LOW);
	digitalWrite(ledRojo, LOW);
	motor1.setSpeed(velocidad);
	motor2.setSpeed(0);
	motor3.setSpeed(0);
	motor4.setSpeed(velocidad);
	motor1.run(FORWARD);
	motor2.run(RELEASE);
	motor3.run(RELEASE);
	motor4.run(BACKWARD);
}


//ACCIONES MODO VOLANTE

//VELOCIDADES

//Velocidad Maxima: 235 PWM
void velocidadMax(){
	digitalWrite(ledAtras, LOW);
	digitalWrite(ledRojo, LOW);
	velAcel = 235;
	motor1.run(FORWARD);
	motor2.run(BACKWARD);
	motor3.run(FORWARD);
	motor4.run(BACKWARD);
}

//Velocidad Media: 200 PWM
void velocidadMedia(){
	digitalWrite(ledAtras, LOW);
	digitalWrite(ledRojo, LOW);
	velAcel = 200;
	motor1.run(FORWARD);
	motor2.run(BACKWARD);
	motor3.run(FORWARD);
	motor4.run(BACKWARD);
}

//Velocidad Minima: 150 PWM
void velocidadMin(){
	digitalWrite(ledAtras, LOW);
	digitalWrite(ledRojo, LOW);
	velAcel = 150;
	motor1.run(FORWARD);
	motor2.run(BACKWARD);
	motor3.run(FORWARD);
	motor4.run(BACKWARD);
}

//Velocidad 0: Motores RELEASE
void velocidadCero(){
	digitalWrite(ledAtras, LOW);
	digitalWrite(ledRojo, HIGH);
	velAcel = 235;
	motor1.run(RELEASE);
	motor2.run(RELEASE);
	motor3.run(RELEASE);
	motor4.run(RELEASE);
}

//Velocidad Atras: Motores sentido inverso, 150 PWM
void velocidadAtras(){
	digitalWrite(ledAtras, HIGH);
	digitalWrite(ledRojo, LOW);
	velAcel = 150;
	motor1.run(BACKWARD);
	motor2.run(FORWARD);
	motor3.run(BACKWARD);
	motor4.run(FORWARD);}

//DIRECCIONES

//Direccion Recto: Motores a velocidad constante
void direccionRecto(){
	motor1.setSpeed(velAcel);
	motor2.setSpeed(velAcel);
	motor3.setSpeed(velAcel);
	motor4.setSpeed(velAcel);
}

//DIRECCIONES IZQUIERDA: Reducir velocidad motores izquierdo

//Min: velocidad reducida en 20 PWM
void direccionIzquierdaMin(){
	motor1.setSpeed(velAcel-40);
	motor2.setSpeed(velAcel);
	motor3.setSpeed(velAcel);
	motor4.setSpeed(velAcel-40);
}

//Med: velocidad reducida en 40 PWM
void direccionIzquierdaMed(){
	motor1.setSpeed(velAcel-100);
	motor2.setSpeed(velAcel);
	motor3.setSpeed(velAcel);
	motor4.setSpeed(velAcel-100);
}

//Max: velocidad reducida en 70 PWM
void direccionIzquierdaMax(){
	motor1.setSpeed(0);
	motor2.setSpeed(velAcel);
	motor3.setSpeed(velAcel);
	motor4.setSpeed(0);
}

//DIRECCIONES DERECHA: Reducir velocidad motores derecha

//Min: velocidad reducida en 20 PWM
void direccionDerechaMin(){
	motor1.setSpeed(velAcel);
	motor2.setSpeed(velAcel-40);
	motor3.setSpeed(velAcel-40);
	motor4.setSpeed(velAcel);
}

//Med: velocidad reducida en 40 PWM
void direccionDerechaMed(){
	motor1.setSpeed(velAcel);
	motor2.setSpeed(velAcel-100);
	motor3.setSpeed(velAcel-100);
	motor4.setSpeed(velAcel);
}

//Max: velocidad reducida en 70 PWM
void direccionDerechaMax(){
	motor1.setSpeed(velAcel);
	motor2.setSpeed(0);
	motor3.setSpeed(0);
	motor4.setSpeed(velAcel);
}


//ACCIONES MODO CIRCUITO

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

void sigueLinea(){
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
	motor4.run(BACKWARD);
}

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
			if (posicionSiguiente < 90){
				//Actualizamos la posicion del servomotor y de las posiciones para la proxma actualizacion
				servomotor.write(posicionSiguiente);
				posicionActual += 2;
				posicionSiguiente += 2;
				// si es mayor que 90, comenzamos a girar hacia el otro sentido
			} else{
				//actualizamos la posicion del servomotor y la posicion siguiente para que sea menor que la actual
				posicionSiguiente -= 2;
				servomotor.write(posicionSiguiente);
			}
		}
		//si la posicion siguiente es menor que la actual
		if (posicionSiguiente < posicionActual){
			//comprobamos que la poscion siguiente es mayor que 0
			if (posicionSiguiente > 0){
				//actualizamos la posicion del servomotor y la posicion siguiente para la proxima actualizacion
				servomotor.write(posicionSiguiente);
				posicionActual -= 2;
				posicionSiguiente -= 2;
				//si es menor que 0, comenzamos a girar en el otro sentido
			} else{
				//actualizamos la posicion del servomotot y la posicion siguiente para la proxima actualizacion
				posicionSiguiente += 2;
				servomotor.write(posicionSiguiente);
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


