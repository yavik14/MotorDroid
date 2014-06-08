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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

@SuppressLint("HandlerLeak")
public class Motordroid extends Activity implements SensorEventListener {
	private float curX = 0, curY = 0;

	public boolean atrasPulsado = false;
	public boolean adelantePulsado = false;
	public boolean izqPulsado = false;
	public boolean derPulsado = false;

	public char comandoAcelX;
	public char comandoAcelY;

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
	// variables para el Menu de conexión
	private boolean seleccionador = false;
	public int Opcion = R.menu.conexion;

	private Sensor mAcelerometro;

	public boolean modo1 = false;
	public boolean modo2 = false;
	public boolean modo3 = false;
	public boolean modo4 = false;
	
	public boolean aFueEnviado = false;
	public boolean aAhoraEnviodo = false;
	
	public ToggleButton botonIntermitenteDerecho;
	public ToggleButton botonIntermitenteIzquierdo;

	private ToggleButton botonManual;
	private ToggleButton botonVolante;
	private ToggleButton botonExplorador;
	private ToggleButton botonCircuito;

	private ToggleButton botonLed;
	private ToggleButton botonTriangulo;
	public Button botonAtras;
	public Button botonDer;
	public Button botonIzq;
	public Button botonAdelante;

	public View vistaIModo1;
	public View vistaIModo2;
	public View vistaIModo3;
	public View vistaIModo4;

	
	public View vistaDModo1;
	public View vistaDModo2;
	public View vistaDModo3;
	public View vistaDModo4;

	public View vistaCModo12;
	public View vistaCModo4;

	public ComandosAcelerometro ca = new ComandosAcelerometro();

		
	//CONTROL DEL MENU PRINCIPAL Y MODOS DE FUNCIONAMIENTO
	public void controlMenu(View v){
		if(v == botonManual){
			vistaIModo1.setVisibility(View.VISIBLE);
			vistaDModo1.setVisibility(View.VISIBLE);
			vistaCModo12.setVisibility(View.VISIBLE);
			
			vistaIModo2.setVisibility(View.GONE);
			vistaDModo2.setVisibility(View.GONE);
			
			vistaIModo3.setVisibility(View.GONE);
			vistaDModo3.setVisibility(View.GONE);
			
			vistaIModo4.setVisibility(View.GONE);
			vistaDModo4.setVisibility(View.GONE);
			vistaCModo4.setVisibility(View.GONE);
			
			if (D)Log.e("Boton Manual", "Modo Manual Activo");
			if(seleccionador){
				sendMessage("1\r");
			}
			Toast.makeText(getApplicationContext(),	"Modo Manual Seleccionado" , Toast.LENGTH_SHORT).show();
			findViewById(R.id.modo1).setBackgroundResource(R.drawable.modo_man_sel);
			modo1 = true;
			modo2 = false;
			modo3 = false;
			modo4 = false;
			findViewById(R.id.modo2).setBackgroundResource(R.drawable.modo_vol);
			findViewById(R.id.modo3).setBackgroundResource(R.drawable.modo_exp);
			findViewById(R.id.modo4).setBackgroundResource(R.drawable.modo_cir);
			botonVolante.setChecked(false);
			botonExplorador.setChecked(false);
			botonCircuito.setChecked(false);
		}else if(v == botonVolante){
			vistaIModo1.setVisibility(View.GONE);
			vistaDModo1.setVisibility(View.GONE);
			vistaCModo12.setVisibility(View.VISIBLE);
			
			vistaIModo2.setVisibility(View.VISIBLE);
			vistaDModo2.setVisibility(View.VISIBLE);
			
			vistaIModo3.setVisibility(View.GONE);
			vistaDModo3.setVisibility(View.GONE);
			
			vistaIModo4.setVisibility(View.GONE);
			vistaDModo4.setVisibility(View.GONE);
			vistaCModo4.setVisibility(View.GONE);
			
			if (D)Log.e("Boton Volante", "Modo Volante Activo");
			if(seleccionador){
				sendMessage("2\r");
			}
			Toast.makeText(getApplicationContext(),	"Modo Volante Seleccionado" , Toast.LENGTH_SHORT).show();
			findViewById(R.id.modo2).setBackgroundResource(R.drawable.modo_vol_sel);
			modo1 = false;
			modo2 = true;
			modo3 = false;
			modo4 = false;
			findViewById(R.id.modo1).setBackgroundResource(R.drawable.modo_man);
			findViewById(R.id.modo3).setBackgroundResource(R.drawable.modo_exp);
			findViewById(R.id.modo4).setBackgroundResource(R.drawable.modo_cir);
			botonManual.setChecked(false);
			botonExplorador.setChecked(false);
			botonCircuito.setChecked(false);
		}else if(v == botonExplorador){
			vistaIModo1.setVisibility(View.GONE);
			vistaDModo1.setVisibility(View.GONE);
			vistaCModo12.setVisibility(View.VISIBLE);
			
			vistaIModo2.setVisibility(View.GONE);
			vistaDModo2.setVisibility(View.GONE);
			
			vistaIModo3.setVisibility(View.VISIBLE);
			vistaDModo3.setVisibility(View.VISIBLE);
			
			vistaIModo4.setVisibility(View.GONE);
			vistaDModo4.setVisibility(View.GONE);
			vistaCModo4.setVisibility(View.GONE);
			
			if (D)Log.e("Boton Explorador", "Modo Explorador Activo");
			if(seleccionador){
				sendMessage("3\r");
			}
			Toast.makeText(getApplicationContext(),	"Modo Explorador Seleccionado" , Toast.LENGTH_SHORT).show();
			findViewById(R.id.modo3).setBackgroundResource(R.drawable.modo_exp_sel);
			modo1 = false;
			modo2 = false;
			modo3 = true;
			modo4 = false;
			findViewById(R.id.modo1).setBackgroundResource(R.drawable.modo_man);
			findViewById(R.id.modo2).setBackgroundResource(R.drawable.modo_vol);
			findViewById(R.id.modo4).setBackgroundResource(R.drawable.modo_cir);
			botonManual.setChecked(false);
			botonVolante.setChecked(false);
			botonCircuito.setChecked(false);
		}else if(v == botonCircuito){
			vistaIModo1.setVisibility(View.GONE);
			vistaDModo1.setVisibility(View.GONE);
			vistaCModo12.setVisibility(View.GONE);
			
			vistaIModo2.setVisibility(View.GONE);
			vistaDModo2.setVisibility(View.GONE);
			
			vistaIModo3.setVisibility(View.GONE);
			vistaDModo3.setVisibility(View.GONE);
			
			vistaIModo4.setVisibility(View.VISIBLE);
			vistaDModo4.setVisibility(View.VISIBLE);
			vistaCModo4.setVisibility(View.VISIBLE);
			
			if (D)Log.e("Boton Circuito", "Modo Circuito Activo");
			if(seleccionador){
				sendMessage("4\r");
			}
			Toast.makeText(getApplicationContext(),	"Modo Circuito Seleccionado" , Toast.LENGTH_SHORT).show();
			findViewById(R.id.modo4).setBackgroundResource(R.drawable.modo_cir_sel);
			modo1 = false;
			modo2 = false;
			modo3 = false;
			modo4 = true;
			findViewById(R.id.modo1).setBackgroundResource(R.drawable.modo_man);
			findViewById(R.id.modo2).setBackgroundResource(R.drawable.modo_vol);
			findViewById(R.id.modo3).setBackgroundResource(R.drawable.modo_exp);
			botonManual.setChecked(false);
			botonVolante.setChecked(false);
			botonExplorador.setChecked(false);
		}
		
	}
	

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_motordroid);
		
		setTitle("");
		
		//habilitaBotones(false);
		
		vistaIModo1 = findViewById(R.id.modo1Direcion);
		vistaIModo2 = findViewById(R.id.modo2Direcion);
		vistaIModo3 = findViewById(R.id.modo3Auto);
		vistaIModo4 = findViewById(R.id.modo4Start);
		
		vistaDModo1 = findViewById(R.id.modo1Velocidad);
		vistaDModo2 = findViewById(R.id.modo2Velocidad);
		vistaDModo3 = findViewById(R.id.modo3Control);
		vistaDModo4 = findViewById(R.id.modo4Stop);
		
		vistaCModo12 = findViewById(R.id.modo12Central);
		vistaCModo4 = findViewById(R.id.modo4Central);
		
		//BOTONES DEL MENU PRINCIPAL
		
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
		

		
		//BOTONES DE LEDS

		botonLed = (ToggleButton) findViewById(R.id.luces);
		botonLed.setOnClickListener(new View.OnClickListener() {
			public void onClick(View vv) {
				if (botonLed.isChecked()) {
					if (D)Log.e("BotonLed", "Luz Blanca");
					sendMessage("W\r");
					findViewById(R.id.luces).setBackgroundResource(R.drawable.luz_blanca);
				} else {
					if (D)Log.e("BotonLed", "Luz Azul");
					sendMessage("W\r");
					findViewById(R.id.luces).setBackgroundResource(R.drawable.luz_azul);
				}
			}
		});
		
		botonTriangulo = (ToggleButton) findViewById(R.id.triangulo);
		botonTriangulo.setOnClickListener(new View.OnClickListener() {
			public void onClick(View vv) {
				if (botonTriangulo.isChecked()) {
					if (D)Log.e("BotonTriangulo", "Encendiendo..");
					sendMessage("E\r");
					findViewById(R.id.triangulo).setBackgroundResource(R.drawable.triangulo_naranja);
					botonIntermitenteDerecho.setChecked(false);
					botonIntermitenteDerecho.setEnabled(false);
					findViewById(R.id.int_derecho).setBackgroundResource(R.drawable.int_derecho_blanco);
					botonIntermitenteIzquierdo.setChecked(false);
					botonIntermitenteIzquierdo.setEnabled(false);
					findViewById(R.id.int_izquierdo).setBackgroundResource(R.drawable.int_izquierdo_blanco);
					
				} else {
					if (D)Log.e("BotonTriangulo", "Apagando..");
					sendMessage("E\r");
					findViewById(R.id.triangulo).setBackgroundResource(R.drawable.triangulo_blanco);
					botonIntermitenteIzquierdo.setEnabled(true);
					botonIntermitenteDerecho.setEnabled(true);
				}
			}
		});
		
		botonIntermitenteIzquierdo = (ToggleButton) findViewById(R.id.int_izquierdo);
		botonIntermitenteIzquierdo.setOnClickListener(new View.OnClickListener() {
			public void onClick(View vv) {
				if (botonIntermitenteIzquierdo.isChecked()) {
					if (D)Log.e("botonIntermitenteIzquierdo", "Encendiendo..");
					sendMessage("I\r");
					findViewById(R.id.int_izquierdo).setBackgroundResource(R.drawable.int_izquierdo_naranja);
					botonIntermitenteDerecho.setChecked(false);
					findViewById(R.id.int_derecho).setBackgroundResource(R.drawable.int_derecho_blanco);
				} else {
					if (D)Log.e("botonIntermitenteIzquierdo", "Apagando..");
					sendMessage("I\r");
					findViewById(R.id.int_izquierdo).setBackgroundResource(R.drawable.int_izquierdo_blanco);
				}
			}
		});
		
		
		botonIntermitenteDerecho = (ToggleButton) findViewById(R.id.int_derecho);
		botonIntermitenteDerecho.setOnClickListener(new View.OnClickListener() {
			public void onClick(View vv) {
				if (botonIntermitenteDerecho.isChecked()) {
					if (D)Log.e("botonIntermitenteDerecho", "Encendiendo..");
					sendMessage("D\r");
					findViewById(R.id.int_derecho).setBackgroundResource(R.drawable.int_derecho_naranja);
					botonIntermitenteIzquierdo.setChecked(false);
					findViewById(R.id.int_izquierdo).setBackgroundResource(R.drawable.int_izquierdo_blanco);
				} else {
					if (D)Log.e("botonIntermitenteDerecho", "Apagando..");
					sendMessage("D\r");
					findViewById(R.id.int_derecho).setBackgroundResource(R.drawable.int_derecho_blanco);
				}
			}
		});

		//BOTONES DE DIRECCION

		botonAtras = (Button) findViewById(R.id.marchaAtras);
		botonAtras.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					sendMessage("B\r");
					Log.d("Pressed", "Button B pressed");
					atrasPulsado = true;

				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					sendMessage("F\r");
					atrasPulsado = false;
					Log.d("Released", "Button B released");
				}

				// TODO Auto-generated method stub
				return false;
			}
		});

		botonDer = (Button) findViewById(R.id.giroDerecha);
		botonDer.setOnTouchListener(new View.OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					Log.d("Pressed", "Button R pressed");
					derPulsado = true;
					sendMessage("R\r");
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					derPulsado = false;
					Log.d("Released", "Button R released");
					sendMessage("F\r");
				}

				// TODO Auto-generated method stub
				return false;
			}
		});

		botonIzq = (Button) findViewById(R.id.giroIzquierda);
		botonIzq.setOnTouchListener(new View.OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					Log.d("Pressed", "Button L pressed");
					izqPulsado = true;
					sendMessage("L\r");
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					izqPulsado = false;
					Log.d("Released", "Button L released");
					sendMessage("F\r");
				}

				// TODO Auto-generated method stub
				return false;
			}
		});

		botonAdelante = (Button) findViewById(R.id.acelera);
		botonAdelante.setOnTouchListener(new View.OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					Log.d("Pressed", "Button A pressed");
					adelantePulsado = true;
					sendMessage("A\r");
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					adelantePulsado = false;
					Log.d("Released", "Button A released");
					sendMessage("F\r");
				}

				// TODO Auto-generated method stub
				return false;
			}
		});
	}
	
	public void habilitaBotones(boolean b){
		if(b){
			botonLed.setEnabled(true);
			botonAdelante.setEnabled(true);
			botonAtras.setEnabled(true);
			botonDer.setEnabled(true);
			botonIzq.setEnabled(true);
			botonIntermitenteDerecho.setEnabled(true);
			botonIntermitenteIzquierdo.setEnabled(true);
			botonTriangulo.setEnabled(true);
		}else{
			botonLed.setEnabled(false);
			botonAdelante.setEnabled(false);
			botonAtras.setEnabled(false);
			botonDer.setEnabled(false);
			botonIzq.setEnabled(false);
			botonIntermitenteDerecho.setEnabled(false);
			botonIntermitenteIzquierdo.setEnabled(false);
			botonTriangulo.setEnabled(false);
		}
	}

	public void onStart() {
		super.onStart();
		ConfigBT();
	}

	public void onDestroy() {
		super.onDestroy();
		if (Servicio_BT != null)
			Servicio_BT.stop();// Detenemos servicio
	}

	public void ConfigBT() {
		// Obtenemos el adaptador de bluetooth
		AdaptadorBT = BluetoothAdapter.getDefaultAdapter();
		if (AdaptadorBT.isEnabled()) {// Si el BT esta encendido,
			if (Servicio_BT == null) {// y el Servicio_BT es nulo, invocamos el
										// Servicio_BT
				Servicio_BT = new ControlBT(this, mHandler);
			}
		} else {
			if (D)
				Log.e("Setup", "Bluetooth apagado...");
			Intent enableBluetooth = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBluetooth, REQUEST_ENABLE_BT);
		}
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Una vez que se ha realizado una actividad regresa un "resultado"...
		switch (requestCode) {

		case REQUEST_ENABLE_BT:// Respuesta de intento de encendido de BT
			if (resultCode == Activity.RESULT_OK) {// BT esta activado,iniciamos
													// servicio
				ConfigBT();
			} else {// No se activo BT, salimos de la app
				finish();
			}

		}// fin de switch case
	}// fin de onActivityResult

	public boolean onPrepareOptionsMenu(Menu menux) {
		// cada vez que se presiona la tecla menu este metodo es llamado
		menux.clear();// limpiamos menu actual
		if (seleccionador == false)
			Opcion = R.menu.conexion;// dependiendo las necesidades
		if (seleccionador == true)
			Opcion = R.menu.desconexion; // crearemos un menu diferente
		getMenuInflater().inflate(Opcion, menux);
		return super.onPrepareOptionsMenu(menux);
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_conexion:
			if (D)
				Log.e("conexion", "conectandonos");
			vibrador = (Vibrator) getSystemService(VIBRATOR_SERVICE);
			vibrador.vibrate(1000);
			String address = "20:13:05:13:23:76";// Direccion Mac del rn42
			BluetoothDevice device = AdaptadorBT.getRemoteDevice(address);
			Servicio_BT.connect(device);
			return true;

		case R.id.action_desconexion:
			if (Servicio_BT != null)
				Servicio_BT.stop();// Detenemos servicio
			return true;
		}// fin de switch de opciones
		return false;
	}// fin de metodo onOptionsItemSelected

	public void sendMessage(String message) {
		if (Servicio_BT.getState() == ControlBT.STATE_CONNECTED) {// checa si
																	// estamos
																	// conectados
																	// a BT
			if (message.length() > 0) { // checa si hay algo que enviar
				byte[] send = message.getBytes();// Obtenemos bytes del mensaje
				if (D)
					Log.e(TAG, "Mensaje enviado:" + message);
				Servicio_BT.write(send); // Mandamos a escribir el mensaje
			}
		} else
			Toast.makeText(this, "No conectado", Toast.LENGTH_SHORT).show();
	}// fin de sendMessage

	final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {

			switch (msg.what) {
			// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
			case Mensaje_Escrito:
				byte[] writeBuf = (byte[]) msg.obj;// buffer de escritura...
				// Construye un String del Buffer
				String writeMessage = new String(writeBuf);
				if (D)
					Log.e(TAG, "Message_write  =w= " + writeMessage);
				break;
			// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
			case Mensaje_Leido:
				byte[] readBuf = (byte[]) msg.obj;// buffer de lectura...
				// Construye un String de los bytes validos en el buffer
				String readMessage = new String(readBuf, 0, msg.arg1);
				if (D)
					Log.e(TAG, "Message_read   =w= " + readMessage);
				break;
			// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
			case Mensaje_Nombre_Dispositivo:
				mConnectedDeviceName = msg.getData().getString(DEVICE_NAME); // Guardamos
																				// nombre
																				// del
																				// dispositivo
				Toast.makeText(getApplicationContext(),
						"Conectado con " + mConnectedDeviceName,
						Toast.LENGTH_SHORT).show();
				seleccionador = true;
				habilitaBotones(seleccionador);
				break;
			// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
			case Mensaje_TOAST:
				Toast.makeText(getApplicationContext(),
						msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
						.show();
				break;
			// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
			case MESSAGE_Desconectado:
				if (D)
					Log.e("Conexion", "DESConectados");
				seleccionador = false;
				habilitaBotones(seleccionador);
				break;
			// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
			}// FIN DE SWITCH CASE PRIMARIO DEL HANDLER
		}// FIN DE METODO INTERNO handleMessage
	};// Fin de Handler

	// ACELEROMETRO
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

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (modo2) {
			curX = event.values[0];
			curY = event.values[1];
			//if (seleccionador) {
				comandoAcelX = comandoAcelerometroX(curX);
				comandoAcelY = comandoAcelerometroY(curY);
			//}
			/*((TextView) findViewById(R.id.X)).setText("X: " + curX);
			((TextView) findViewById(R.id.comandoX)).setText("Comando: "
					+ comandoAcelX);
			((TextView) findViewById(R.id.Y)).setText("Y: " + curY);
			((TextView) findViewById(R.id.comandoY)).setText("Comando: "
					+ comandoAcelY);*/
		}
	}
	
	public void comandoDireccion(int i){
		ca.inicializaLista(ca.comandosDireccionPulsados, 7);
		ca.actualizaLista(ca.comandosDireccionFueronPulsados, i, 7);
		ca.actualizaListaComando(ca.comandosDireccionPulsados, i, 7);
		if (ca.comparaComando(ca.comandosDireccionPulsados, ca.comandosDireccionFueronPulsados, i)){
			ca.actualizaListaComando(ca.comandosDireccionFueronPulsados, i, 7);
			Log.e("ACELETOMETRO", "COMANDO: " + ca.comandosDireccion.get(i));
			sendMessage(ca.comandosDireccion.get(i) + "\r");
		}
	}

	public char comandoAcelerometroY(float valorY) {
		char res = ' ';
		
		if (valorY < -6.7) {
			findViewById(R.id.giro).setBackgroundResource(R.drawable.acel_izq_top);
			comandoDireccion(0);
		}
		if (valorY < -3.2 && valorY > -6.5) {
			findViewById(R.id.giro).setBackgroundResource(R.drawable.acel_izq_max);
			comandoDireccion(1);
		}
		if (valorY < -1.7 && valorY > -3.0) {
			findViewById(R.id.giro).setBackgroundResource(R.drawable.acel_izq_med);
			comandoDireccion(2);
		}
		
		if (valorY < 1.5 && valorY > -1.5) {
			findViewById(R.id.giro).setBackgroundResource(R.drawable.acel_recto);
			comandoDireccion(3);
		}
		if (valorY < 3.0 && valorY > 1.7) {
			findViewById(R.id.giro).setBackgroundResource(R.drawable.acel_der_med);
			comandoDireccion(4);
		}
		if (valorY < 6.5 && valorY > 3.2) {
			findViewById(R.id.giro).setBackgroundResource(R.drawable.acel_der_max);
			comandoDireccion(5);
		}
		if (valorY > 6.7) {
			findViewById(R.id.giro).setBackgroundResource(R.drawable.acel_der_top);
			comandoDireccion(6);
		}
		
		
		
		return res;
	}

	public void comandoVelocidad(int i){
		ca.inicializaLista(ca.comandosVelocidadPulsados,5);
		ca.actualizaLista(ca.comandosVelocidadFueronPulsados, i,5);
		ca.actualizaListaComando(ca.comandosVelocidadPulsados, i,5);
		if (ca.comparaComando(ca.comandosVelocidadPulsados, ca.comandosVelocidadFueronPulsados, i)){
			ca.actualizaListaComando(ca.comandosVelocidadFueronPulsados, i, 5);
			Log.e("ACELETOMETRO", "COMANDO: "+ ca.comandosVelocidad.get(i));
			sendMessage(ca.comandosVelocidad.get(i) + "\r");
			((TextView) findViewById(R.id.valorX)).setText("X: " + curX);
		}
	}
	
	public char comandoAcelerometroX(float valorX) {
		char res = ' ';
		if(valorX > 1.2 && valorX < 2.2){
			findViewById(R.id.velocidad).setBackgroundResource(R.drawable.acel_3);
			comandoVelocidad(0);
		}
		if (valorX > 2.6 && valorX < 4.2) {
			findViewById(R.id.velocidad).setBackgroundResource(R.drawable.acel_2);
			comandoVelocidad(1);
		}
		if(valorX > 4.4 && valorX < 6.2){
			findViewById(R.id.velocidad).setBackgroundResource(R.drawable.acel_1);
			comandoVelocidad(2);
		}
		if((valorX > 6.4 && valorX < 8.4) || valorX < 1.0){
			findViewById(R.id.velocidad).setBackgroundResource(R.drawable.acel_stop);
			comandoVelocidad(3);
		}
		if (valorX > 8.6 && valorX < 11.0) {
			findViewById(R.id.velocidad).setBackgroundResource(R.drawable.acel_0);
			comandoVelocidad(4);
		}
		

		return res;
	}

}
