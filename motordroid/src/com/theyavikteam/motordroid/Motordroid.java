package com.theyavikteam.motordroid;

import java.util.List;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ToggleButton;

@SuppressLint("HandlerLeak")
public class Motordroid extends Activity implements SensorEventListener {
	/* DEFINICIONES */
	
	// Debugging
	public static final String TAG = "MotorDroid";
	public static final boolean D = true;
	
	// Tipos de mensaje enviados y recibidos desde el Handler de ConexionBT
	public static final int Mensaje_Estado_Cambiado = 1;
	public static final int Mensaje_Leido = 2;
	public static final int Mensaje_Escrito = 3;
	public static final int Mensaje_Nombre_Dispositivo = 4;
	public static final int Mensaje_TOAST = 5;
	public static final int MESSAGE_Desconectado = 6;
	public static final int REQUEST_ENABLE_BT = 7;
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";
	
	// Nombre del dispositivo conectado
	private String mConnectedDeviceName = null;
	// Adaptador local Bluetooth
	private BluetoothAdapter AdaptadorBT = null;
	// Objeto miembro para el servicio de ConexionBT
	private ControlBT Servicio_BT = null;
	// Vibrador
	private Vibrator vibrador;
	// Variables para el menu de conexión
	private boolean seleccionador = false;
	public int Opcion = R.menu.conexion;
	
	//Variables del Menú Principal
	private ToggleButton botonManual;
	private ToggleButton botonVolante;
	private ToggleButton botonExplorador;
	private ToggleButton botonCircuito;

	//Variables del Modo Manual
	private ToggleButton botonLed;
	
	private ToggleButton botonTriangulo;
	public ToggleButton botonIntermitenteDerecho;
	public ToggleButton botonIntermitenteIzquierdo;
	
	public Button botonAtras;
	public Button botonDer;
	public Button botonIzq;
	public Button botonAdelante;
	public Button botonTrompoLeft;
	public Button botonTrompoRight;
	
	// Variables del Modo Volante
	private Sensor mAcelerometro;
	public boolean modo2 = false;
	private float curX = 0, curY = 0;
	private ToggleButton botonLed2;
	public ComandosAcelerometro ca = new ComandosAcelerometro();
	
	// Variables del Modo Explorador
	public Button botonServoUp;
	public Button botonServoDown;
	public Button botonServoLeft;
	public Button botonServoRight;

	public Button botonExplorerRight;
	public Button botonExplorerLeft;
	public Button botonExplorerDown;
	public Button botonExplorerUp;

	public ToggleButton botonExplorerAutomatico;

	// Variables del Modo Circuito
	public ToggleButton botonStartCircuito;
	public ToggleButton botonStopCircuito;

	
	/* METODOS ESPECÍFICOS DE ANDROID */
	
	// Método que carga la vista de la aplicación en pantalla
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.motordroid_bienvenida);
		setTitle("");
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}
	
	/* MÉTODOS DEL CICLO DE VIDA */
	// onStart y onDestroy lo usamos para el control del bluetooth
	public void onStart() {
		super.onStart();
		ConfigBT();
	}
	
	public void onDestroy() {
		super.onDestroy();
		if (Servicio_BT != null)
			Servicio_BT.stop();// Detenemos servicio
	}

	// onResume, onPause y onStop lo usamos para el control del acelerómetro
	protected void onResume() {
		super.onResume();
		SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);
		List<Sensor> sensors = sm.getSensorList(Sensor.TYPE_ACCELEROMETER);
		if (sensors.size() > 0) {
			sm.registerListener(this, sensors.get(0),
					SensorManager.SENSOR_DELAY_GAME);
		}
	}

	protected void onPause() {
		SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);
		sm.unregisterListener(this, mAcelerometro);
		super.onPause();
	}

	protected void onStop() {
		SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);
		sm.unregisterListener(this, mAcelerometro);
		super.onStop();
	}

	
	/* MÉTODOS ESPECÍFICOS PARA EL CONTROL DE LAS VISTAS DE MOTORDROID */

	// CONTROL DEL MENU PRINCIPAL Y MODOS DE FUNCIONAMIENTO
	public void controlMenu(View v) {
		if (v == botonManual) {
			Log.d("MODO MANUAL", "MODO MANUAL ACTIVO");
			sendMessage("1\r");
			Toast.makeText(getApplicationContext(), "Modo Manual Seleccionado", Toast.LENGTH_SHORT).show();
			modo2 = false;
			cargaManual();
		} else if (v == botonVolante) {
			Log.d("MODO VOLANTE", "MODO VOLANTE ACTIVO");
			sendMessage("2\r");
			Toast.makeText(getApplicationContext(), "Modo Volante Seleccionado", Toast.LENGTH_SHORT).show();
			modo2 = true;
			cargaVolante();
		} else if (v == botonExplorador) {
			Log.d("MODO EXPLORADOR", "MODO EXPLORADOR ACTIVO");
			sendMessage("3\r");
			Toast.makeText(getApplicationContext(), "Modo Explorador Seleccionado", Toast.LENGTH_SHORT).show();
			modo2 = false;
			cargaExplorador();
		} else if (v == botonCircuito) {
			Log.d("MODO CIRCUITO", "MODO CIRCUITO ACTIVO");
			sendMessage("4\r");
			Toast.makeText(getApplicationContext(),	"Modo Circuito Seleccionado", Toast.LENGTH_SHORT).show();
			modo2 = false;
			cargaCircuito();
		}
	}

	// HABILITA LOS BOTONES PARA CONTROLAR MOTORDROID
	public void habilitaBotones(boolean sel) {
		if (sel) {
			cambiaVista(R.layout.motordroid_manual);
			cargaMenu();
			cargaManual();
		} else {
			cambiaVista(R.layout.motordroid_bienvenida);
		}
	}

	// CAMBIA LA VISTA QUE PARECE EN PANTALLA
	public void cambiaVista(int id) {
		setContentView(id);
		setTitle("");
	}

	// CARGA LOS BOTONES DE MENÚ
	public void cargaMenu() {
		// BOTONES DEL MENU PRINCIPAL
		botonManual = (ToggleButton) findViewById(R.id.modo1);
		botonManual.setOnClickListener(new View.OnClickListener() {
			public void onClick(View vv) {
				if (botonManual.isChecked()) {
					controlMenu(botonManual);
				}
			}
		});

		botonVolante = (ToggleButton) findViewById(R.id.modo2);
		botonVolante.setOnClickListener(new View.OnClickListener() {
			public void onClick(View vv) {
				if (botonVolante.isChecked()) {
					controlMenu(botonVolante);
				}
			}
		});

		botonExplorador = (ToggleButton) findViewById(R.id.modo3);
		botonExplorador.setOnClickListener(new View.OnClickListener() {
			public void onClick(View vv) {
				if (botonExplorador.isChecked()) {
					controlMenu(botonExplorador);
				}
			}
		});

		botonCircuito = (ToggleButton) findViewById(R.id.modo4);
		botonCircuito.setOnClickListener(new View.OnClickListener() {
			public void onClick(View vv) {
				if (botonCircuito.isChecked()) {
					controlMenu(botonCircuito);
				}
			}
		});
	}
	
	// ASIGNA LAS ID A LAS VISTAS Y MANDA COMANDOS A MOTORDROID DEPENDIENDO DEL ESTADO
	public void comandosBotones(Button b, int id, final String comandoPulsar, final String comandoSoltar, final String mensajePulsar, final String mensajeSoltar, final String etiqueta){
		b = (Button) findViewById(id);
		b.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					sendMessage(comandoPulsar);
					Log.i(etiqueta, mensajePulsar);
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					sendMessage(comandoSoltar);
					Log.i(etiqueta, mensajeSoltar);
				}
				return false;
			}
		});
	}

	// CARGA LOS CONTROLES PARA EL USO DEL MODO MANUAL DE MOTORDROID
	public void cargaManual() {
		cambiaVista(R.layout.motordroid_manual);
		cargaMenu();
		// BOTONES MODO MANUAL
		// BOTONES DE LEDS
		botonLed = (ToggleButton) findViewById(R.id.luces);
		botonLed.setOnClickListener(new View.OnClickListener() {
			public void onClick(View vv) {
				if (botonLed.isChecked()) {
					Log.i("MODO MANUAL", "LUZ BLANCA");
					sendMessage("W\r");
					findViewById(R.id.luces).setBackgroundResource(R.drawable.luz_blanca);
				} else {
					Log.i("MODO MANUAL", "LUZ AZUL");
					sendMessage("W\r");
					findViewById(R.id.luces).setBackgroundResource(R.drawable.luz_azul);
				}
			}
		});

		botonTriangulo = (ToggleButton) findViewById(R.id.triangulo);
		botonTriangulo.setOnClickListener(new View.OnClickListener() {
			public void onClick(View vv) {
				if (botonTriangulo.isChecked()) {
					Log.i("MODO MANUAL", "ESTACIONAMIENTO ACTIVADO");
					sendMessage("E\r");
					findViewById(R.id.triangulo).setBackgroundResource(R.drawable.triangulo_naranja);
					botonIntermitenteDerecho.setChecked(false);
					botonIntermitenteDerecho.setEnabled(false);
					findViewById(R.id.int_derecho).setBackgroundResource(R.drawable.int_derecho_blanco);
					botonIntermitenteIzquierdo.setChecked(false);
					botonIntermitenteIzquierdo.setEnabled(false);
					findViewById(R.id.int_izquierdo).setBackgroundResource(R.drawable.int_izquierdo_blanco);
				} else {
					Log.i("MODO MANUAL", "ESTACIONAMIENTO DESACTIVADO");
					sendMessage("E\r");
					findViewById(R.id.triangulo).setBackgroundResource(R.drawable.triangulo_blanco);
					botonIntermitenteIzquierdo.setEnabled(true);
					botonIntermitenteDerecho.setEnabled(true);
				}
			}
		});

		botonIntermitenteIzquierdo = (ToggleButton) findViewById(R.id.int_izquierdo);
		botonIntermitenteIzquierdo
				.setOnClickListener(new View.OnClickListener() {
					public void onClick(View vv) {
						if (botonIntermitenteIzquierdo.isChecked()) {
							Log.i("MODO MANUAL","INTERMITENTE IZQUIERDO ACTIVADO");
							sendMessage("I\r");
							findViewById(R.id.int_izquierdo).setBackgroundResource(R.drawable.int_izquierdo_naranja);
							botonIntermitenteDerecho.setChecked(false);
							findViewById(R.id.int_derecho).setBackgroundResource(R.drawable.int_derecho_blanco);
						} else {
							Log.i("MODO MANUAL","INTERMITENTE IZQUIERDO DESACTIVADO");
							sendMessage("I\r");
							findViewById(R.id.int_izquierdo).setBackgroundResource(R.drawable.int_izquierdo_blanco);
						}
					}
				});

		botonIntermitenteDerecho = (ToggleButton) findViewById(R.id.int_derecho);
		botonIntermitenteDerecho.setOnClickListener(new View.OnClickListener() {
			public void onClick(View vv) {
				if (botonIntermitenteDerecho.isChecked()) {
					Log.i("MODO MANUAL", "INTERMITENTE DERECHO ACTIVADO");
					sendMessage("D\r");
					findViewById(R.id.int_derecho).setBackgroundResource(R.drawable.int_derecho_naranja);
					botonIntermitenteIzquierdo.setChecked(false);
					findViewById(R.id.int_izquierdo).setBackgroundResource(R.drawable.int_izquierdo_blanco);
				} else {
					Log.i("MODO MANUAL", "INTERMITENTE DERECHO DESACTIVADO");
					sendMessage("D\r");
					findViewById(R.id.int_derecho).setBackgroundResource(R.drawable.int_derecho_blanco);
				}
			}
		});

		// BOTONES DE DIRECCION
		comandosBotones(botonAtras, R.id.marchaAtras, "B\r", "F\r", "MARCHA ATRAS", "PARADO", "MODO MANUAL");
		comandosBotones(botonDer, R.id.giroDerecha, "R\r", "F\r", "GIRO DERECHA", "PARADO", "MODO MANUAL");
		comandosBotones(botonIzq, R.id.giroIzquierda, "L\r", "F\r", "GIRO IZQUIERDA", "PARADO", "MODO MANUAL");
		comandosBotones(botonAdelante, R.id.acelera, "A\r", "F\r", "ACELERA", "PARADO", "MODO MANUAL");
		comandosBotones(botonTrompoLeft, R.id.vueltaIzquierda, "M\r", "F\r", "TROMPO IZQUIERDA", "PARADO", "MODO MANUAL");
		comandosBotones(botonTrompoRight, R.id.vueltaDerecha, "N\r", "F\r", "TROMPO DERECHA", "PARADO", "MODO MANUAL");

	}

	// CARGA LOS CONTROLES PARA EL USO DEL MODO VOLANTE DE MOTORDROID
	public void cargaVolante() {
		cambiaVista(R.layout.motordroid_volante);
		cargaMenu();
		// BOTONES MODO VOLANTE
		botonLed2 = (ToggleButton) findViewById(R.id.luzFrontal);
		botonLed2.setOnClickListener(new View.OnClickListener() {
			public void onClick(View vv) {
				if (botonLed2.isChecked()) {
					Log.d("MODO VOLANTE", "LUZ BLANCA");
					sendMessage("W\r");
					findViewById(R.id.luzFrontal).setBackgroundResource(R.drawable.led_blanco);
				} else {
					Log.d("MODO VOLANTE", "LUZ AZUL");
					sendMessage("W\r");
					findViewById(R.id.luzFrontal).setBackgroundResource(R.drawable.led_azul);
				}
			}
		});
	}

	// CARGA LOS CONTROLES PARA EL USO DEL MODO EXPLORADOR DE MOTORDROID
	public void cargaExplorador() {
		cambiaVista(R.layout.motordroid_explorador);
		cargaMenu();
		// BOTONES MODO EXPLORADOR
		// CONTROL VEHICULO
		comandosBotones(botonExplorerUp, R.id.exploraUp, "W\r", "X\r", "EXPLORA ADELANTE", "EXPLORA PARADO", "MODO EXPLORADOR");
		comandosBotones(botonExplorerDown, R.id.exploraDown, "S\r", "X\r", "EXPLORA ATRAS", "EXPLORA PARADO", "MODO EXPLORADOR");
		comandosBotones(botonExplorerLeft, R.id.exploraLeft, "A\r", "X\r", "EXPLORA IZQUIERDA", "EXPLORA PARADO", "MODO EXPLORADOR");
		comandosBotones(botonExplorerRight, R.id.exploraRight, "D\r", "X\r", "EXPLORA DERECHA", "EXPLORA PARADO", "MODO EXPLORADOR");
		
		//CONTROL CABEZA
		comandosBotones(botonServoUp, R.id.servoUp, "I\r", "F\r", "CABEZA ARRIBA", "CABEZA PARADA", "MODO EXPLORADOR");
		comandosBotones(botonServoDown, R.id.servoDown, "K\r", "F\r", "CABEZA ABAJO", "CABEZA PARADA", "MODO EXPLORADOR");
		comandosBotones(botonServoLeft, R.id.servoLeft, "J\r", "F\r", "CABEZA IZQUIERDA", "CABEZA PARADA", "MODO EXPLORADOR");
		comandosBotones(botonServoRight, R.id.servoRight, "L\r", "F\r", "CABEZA DERECHA", "CABEZA PARADA", "MODO EXPLORADOR");

		// CONTROL AUTOMATICO
		botonExplorerAutomatico = (ToggleButton) findViewById(R.id.autoExplorar);
		botonExplorerAutomatico.setOnClickListener(new View.OnClickListener() {
			public void onClick(View vv) {
				if (botonExplorerAutomatico.isChecked()) {
					Log.i("AUTO EXPLORER", "ACTIVADO");
					sendMessage("E\r");
					findViewById(R.id.autoExplorar).setBackgroundResource(R.drawable.auto_naranja);
				} else {
					Log.e("AUTO EXPLORER", "DESACTIVADO");
					sendMessage("E\r");
					findViewById(R.id.autoExplorar).setBackgroundResource(R.drawable.auto_negro);
				}
			}
		});
	}

	// CARGA LOS CONTROLES PARA EL USO DEL MODO CIRCUITO DE MOTORDROID
	public void cargaCircuito() {
		cambiaVista(R.layout.motordroid_circuito);
		cargaMenu();
		// MODO CIRCUITO
		botonStartCircuito = (ToggleButton) findViewById(R.id.startCircuito);
		botonStartCircuito.setOnClickListener(new View.OnClickListener() {
			public void onClick(View vv) {
				if (botonStartCircuito.isChecked()) {
					Log.i("MODO CIRCUITO", "START");
					sendMessage("S\r");
					findViewById(R.id.startCircuito).setBackgroundResource(R.drawable.start_negro);
					botonStopCircuito.setChecked(false);
					botonStopCircuito.setEnabled(true);
					botonStartCircuito.setEnabled(false);
					findViewById(R.id.stopCircuito).setBackgroundResource(R.drawable.stop_azul);
				} else {
					findViewById(R.id.startCircuito).setBackgroundResource(R.drawable.start_naranja);
				}
			}
		});

		botonStopCircuito = (ToggleButton) findViewById(R.id.stopCircuito);
		botonStopCircuito.setOnClickListener(new View.OnClickListener() {
			public void onClick(View vv) {
				if (botonStopCircuito.isChecked()) {
					Log.i("MODO CIRCUITO", "STOP");
					sendMessage("S\r");
					findViewById(R.id.stopCircuito).setBackgroundResource(R.drawable.stop_negro);
					botonStartCircuito.setChecked(false);
					botonStopCircuito.setEnabled(false);
					botonStartCircuito.setEnabled(true);
					findViewById(R.id.startCircuito).setBackgroundResource(R.drawable.start_naranja);
				} else {
					findViewById(R.id.stopCircuito).setBackgroundResource(R.drawable.stop_azul);
				}
			}
		});
	}

	
	/* GESTIÓN DE LA CONEXIÓN BLUETOOTH */
	
    // COMPROBAR SI EL BLUETOOTH DEL SMARTPHONE ESTA ACTIVO
	public void ConfigBT() {
		// Obtenemos el adaptador de bluetooth
		AdaptadorBT = BluetoothAdapter.getDefaultAdapter();
		if (AdaptadorBT.isEnabled()) {// Si el BT esta encendido,
			if (Servicio_BT == null) {// y el Servicio_BT es nulo, invocamos el Servicio_BT
				Servicio_BT = new ControlBT(this, mHandler);
			}
		} else {
			//Si no lo habilitamos para hacer uso de él
			Log.e("CONFIGURACION", "BLUETOOTH APAGADO");
			Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBluetooth, REQUEST_ENABLE_BT);
		}
	}

	// COMPRUEBA SI HEMOS ACEPTADO En¡NCENDER EL BLUETOOTH
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Comprobamos la respuesta al intento de habilitar el BT
		switch (requestCode) {
		case REQUEST_ENABLE_BT:// Respuesta de intento de encendido de BT
			if (resultCode == Activity.RESULT_OK) {// Si el BT esta activado, iniciamos servicio
				ConfigBT();
			} else {// Si no se activo el BT, salimos de la app
				finish();
			}
		}
	}

	// CARGAMOS UN MENÚ SUPERIOR DIFERENTE DEPENDIENDO DE SI ESTAMOS CONECTADOS A MOTODROID O NO
	public boolean onPrepareOptionsMenu(Menu menux) {
		// Llamamos al método cada vez que pulsamos sobre el menú superior
		menux.clear();// impiamos menú actual
		if (seleccionador == false)
			Opcion = R.menu.conexion;// dependiendo las necesidades
		if (seleccionador == true)
			Opcion = R.menu.desconexion; // crearemos un menu diferente
		getMenuInflater().inflate(Opcion, menux);
		return super.onPrepareOptionsMenu(menux);
	}

	// ACCIONES AL PULSAR LOS BOTONES DEL MENÚ SUPERIOR
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		//CONECTAR
		case R.id.action_conexion:
			Log.d("CONFIGURACION", "CONECTANDO BLUETOOTH");
			vibrador = (Vibrator) getSystemService(VIBRATOR_SERVICE);
			vibrador.vibrate(500);
			String address = "20:13:05:13:23:76";// Direccion Mac del módulo BT de MOTORDROID
			BluetoothDevice device = AdaptadorBT.getRemoteDevice(address);
			Servicio_BT.connect(device);//Conectamos con MotorDroid
			return true;
		//DESCONECTAR
		case R.id.action_desconexion:
			if (Servicio_BT != null)
				Servicio_BT.stop();// Detenemos servicio
			return true;
		}
		return false;
	}

	// GESTIÓN DEL ENVÍO DE MENSAJES A MOTORDROID
	public void sendMessage(String message) {
		if (Servicio_BT.getState() == ControlBT.STATE_CONNECTED) {// Comprueba si estamos conectados por bluetooth
			if (message.length() > 0) { // comprueba si hay algo que enviar
				byte[] send = message.getBytes();// Obtenemos bytes del mensaje
				Log.e(TAG, "ENVIADO --> " + message);
				Servicio_BT.write(send); // Mandamos a escribir el mensaje
			}
		} else
			Toast.makeText(this, "No conectado", Toast.LENGTH_SHORT).show();
	}

	// PASO DE MENSAJES CON EL HANDLER
	final Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Mensaje_Escrito:
				byte[] writeBuf = (byte[]) msg.obj;// Buffer de escritura
				// Construye un String del Buffer
				String writeMessage = new String(writeBuf);
				Log.e(TAG, "ESCRITO :: " + writeMessage);
				break;
			case Mensaje_Leido:
				byte[] readBuf = (byte[]) msg.obj;// Buffer de lectura
				// Construye un String de los bytes validos en el buffer
				String readMessage = new String(readBuf, 0, msg.arg1);
				Log.e(TAG, "LEIDO <-- " + readMessage);
				break;
			case Mensaje_Nombre_Dispositivo:
				mConnectedDeviceName = msg.getData().getString(DEVICE_NAME); // Guardamos NOMBRE DEL DISPOSITIVO
				Toast.makeText(getApplicationContext(), "CONECTADO CON: " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
				seleccionador = true;
				habilitaBotones(seleccionador);
				break;
			case Mensaje_TOAST:
				Toast.makeText(getApplicationContext(),	msg.getData().getString(TOAST), Toast.LENGTH_SHORT).show();
				break;
			case MESSAGE_Desconectado:
				Log.e("CONFIGURACION", "DESCONECTADO");
				seleccionador = false;
				habilitaBotones(seleccionador);
				break;
			}
		}
	};

	
	/* GESTIÓN DEL SENSOR ACELEROMETRO DEL SMARTPHONE */

	
	/* GESTION DEL SENSOR ACELEROMETRO*/ 
	
	// METODO OBLIGATORIO PARA EL USO DEL ACELERÓMETRO, INSERVIBLE EN NUESTRO CASO
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	// COMPRUEBA CADA VEZ QUE CAMBIA DE ESTADO EL SENSOR
	public void onSensorChanged(SensorEvent event) {
		//Si estamos en el Modo Volante
		if (modo2) {
			//Obtiene los ejes X e Y
			curX = event.values[0];
			curY = event.values[1];
			//Manda los comandos si es uno diferente al último mandado
			comandoAcelerometroX(curX);
			comandoAcelerometroY(curY);
		}
	}

	// GESTION DEL ENVIO DE COMANDOS DE DIRECCION
	public void comandoDireccion(int i) {
		ca.inicializaLista(ca.comandosDireccionPulsados, 7);
		ca.actualizaLista(ca.comandosDireccionFueronPulsados, i, 7);
		ca.actualizaListaComando(ca.comandosDireccionPulsados, i, 7);
		if (ca.comparaComando(ca.comandosDireccionPulsados,
				ca.comandosDireccionFueronPulsados, i)) {
			ca.actualizaListaComando(ca.comandosDireccionFueronPulsados, i, 7);
			Log.i("ACELETOMETRO", "COMANDO: " + ca.comandosDireccion.get(i));
			sendMessage(ca.comandosDireccion.get(i) + "\r");
		}
	}

	// SELECCIÓN DEL COMANDO DE DIRECCION A ENVIAR
	public void comandoAcelerometroY(float valorY) {
		if (valorY < -6.0) {
			findViewById(R.id.giro).setBackgroundResource(
					R.drawable.acel_izq_top);
			comandoDireccion(0);
		}
		if (valorY < -3.0 && valorY > -5.5) {
			findViewById(R.id.giro).setBackgroundResource(
					R.drawable.acel_izq_max);
			comandoDireccion(1);
		}
		if (valorY < -1.7 && valorY > -2.5) {
			findViewById(R.id.giro).setBackgroundResource(
					R.drawable.acel_izq_med);
			comandoDireccion(2);
		}

		if (valorY < 1.2 && valorY > -1.2) {
			findViewById(R.id.giro)
					.setBackgroundResource(R.drawable.acel_recto);
			comandoDireccion(3);
		}
		if (valorY < 2.5 && valorY > 1.7) {
			findViewById(R.id.giro).setBackgroundResource(
					R.drawable.acel_der_med);
			comandoDireccion(4);
		}
		if (valorY < 5.5 && valorY > 3.0) {
			findViewById(R.id.giro).setBackgroundResource(
					R.drawable.acel_der_max);
			comandoDireccion(5);
		}
		if (valorY > 6.0) {
			findViewById(R.id.giro).setBackgroundResource(
					R.drawable.acel_der_top);
			comandoDireccion(6);
		}
	}

	// GESTION DEL ENVIO DE COMANDOS DE VELOCIDAD
	public void comandoVelocidad(int i) {
		ca.inicializaLista(ca.comandosVelocidadPulsados, 5);
		ca.actualizaLista(ca.comandosVelocidadFueronPulsados, i, 5);
		ca.actualizaListaComando(ca.comandosVelocidadPulsados, i, 5);
		if (ca.comparaComando(ca.comandosVelocidadPulsados,
				ca.comandosVelocidadFueronPulsados, i)) {
			ca.actualizaListaComando(ca.comandosVelocidadFueronPulsados, i, 5);
			Log.i("ACELETOMETRO", "COMANDO: " + ca.comandosVelocidad.get(i));
			sendMessage(ca.comandosVelocidad.get(i) + "\r");
		}
	}

	// SELECCIÓN DEL COMANDO DE VELOCIDAD A ENVIAR
	public void comandoAcelerometroX(float valorX) {
		if (valorX > 1.5 && valorX < 2.5) {
			findViewById(R.id.velocidad).setBackgroundResource(
					R.drawable.acel_3);
			comandoVelocidad(0);
		}
		if (valorX > 3.0 && valorX < 4.0) {
			findViewById(R.id.velocidad).setBackgroundResource(
					R.drawable.acel_2);
			comandoVelocidad(1);
		}
		if (valorX > 4.5 && valorX < 6.0) {
			findViewById(R.id.velocidad).setBackgroundResource(
					R.drawable.acel_1);
			comandoVelocidad(2);
		}
		if ((valorX > 6.5 && valorX < 8.0) || valorX < 1.0) {
			findViewById(R.id.velocidad).setBackgroundResource(
					R.drawable.acel_stop);
			comandoVelocidad(3);
		}
		if (valorX > 8.5 && valorX < 11.0) {
			findViewById(R.id.velocidad).setBackgroundResource(
					R.drawable.acel_0);
			comandoVelocidad(4);
		}
	}

}
