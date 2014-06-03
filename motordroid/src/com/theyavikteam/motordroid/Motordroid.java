package com.theyavikteam.motordroid;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

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
	public static final String TAG = "LEDv0";
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_motordroid);
		
		setTitle("");

		final ToggleButton botonLed = (ToggleButton) findViewById(R.id.luces);
		botonLed.setOnClickListener(new View.OnClickListener() {
			public void onClick(View vv) {
				if (botonLed.isChecked()) {
					if (D)Log.e("BotonLed", "Encendiendo..");
					sendMessage("W\r");
					findViewById(R.id.luces).setBackgroundResource(R.drawable.led_faro_blanco);
				} else {
					if (D)Log.e("BotonLed", "Apagando..");
					sendMessage("W\r");
					findViewById(R.id.luces).setBackgroundResource(R.drawable.led_faro_azul);
				}
			}
		});

		final ToggleButton botonManual = (ToggleButton) findViewById(R.id.modo1);
		botonManual.setOnClickListener(new View.OnClickListener() {
			public void onClick(View vv) {
				if (botonManual.isChecked()) {
					modo1 = true;
					if (D)Log.e("Boton Manual", "Modo Manual Activo");
					findViewById(R.id.modo1).setBackgroundResource(R.drawable.modo_man_sel);

				} else {
					if (D)
						Log.e("Boton Manual", "Modo Manual Inactivo");
					modo1 = false;
					findViewById(R.id.modo1).setBackgroundResource(R.drawable.modo_man);
				}
			}
		});
		
		final ToggleButton botonVolante = (ToggleButton) findViewById(R.id.modo2);
		botonVolante.setOnClickListener(new View.OnClickListener() {
			public void onClick(View vv) {
				if (botonVolante.isChecked()) {
					modo2 = true;
					if (D)Log.e("Boton Volante", "Modo Volante Activo");
					findViewById(R.id.modo2).setBackgroundResource(R.drawable.modo_vol_sel);

				} else {
					if (D)
						Log.e("Boton Volante", "Modo Volante Inactivo");
					modo2 = false;
					findViewById(R.id.modo2).setBackgroundResource(R.drawable.modo_vol);
				}
			}
		});
		
		final ToggleButton botonExplorador = (ToggleButton) findViewById(R.id.modo3);
		botonExplorador.setOnClickListener(new View.OnClickListener() {
			public void onClick(View vv) {
				if (botonExplorador.isChecked()) {
					modo3 = true;
					if (D)Log.e("Boton Explorador", "Modo Explorador Activo");
					findViewById(R.id.modo3).setBackgroundResource(R.drawable.modo_exp_sel);

				} else {
					if (D)
						Log.e("Boton Explorador", "Modo Explorador Inactivo");
					modo3 = false;
					findViewById(R.id.modo3).setBackgroundResource(R.drawable.modo_exp);
				}
			}
		});
		
		final ToggleButton botonCircuito = (ToggleButton) findViewById(R.id.modo4);
		botonCircuito.setOnClickListener(new View.OnClickListener() {
			public void onClick(View vv) {
				if (botonCircuito.isChecked()) {
					modo4 = true;
					if (D)Log.e("Boton Circuito", "Modo Circuito Activo");
					findViewById(R.id.modo4).setBackgroundResource(R.drawable.modo_cir_sel);

				} else {
					if (D)
						Log.e("Boton Circuito", "Modo Circuito Inactivo");
					modo4 = false;
					findViewById(R.id.modo4).setBackgroundResource(R.drawable.modo_cir);
				}
			}
		});

		Button BotonAtras = (Button) findViewById(R.id.marchaAtras);
		BotonAtras.setOnTouchListener(new View.OnTouchListener() {
			long timePulsar = 0;
			long timeSoltar = 0;
			long timeDiferencia = 0;

			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					timePulsar = System.currentTimeMillis();
					Log.d("Pressed", "Button B pressed");
					atrasPulsado = true;

				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					timeSoltar = System.currentTimeMillis();
					atrasPulsado = false;
					Log.d("Released", "Button B released");
					timeDiferencia = timeSoltar - timePulsar;
					Log.d("Tiempo", Long.toString(timeDiferencia));
					while (timeDiferencia > 0) {
						timeDiferencia -= 100;
						sendMessage("F\r");
					}
				}

				// TODO Auto-generated method stub
				return false;
			}
		});

		Button BotonDer = (Button) findViewById(R.id.giroDerecha);
		BotonDer.setOnTouchListener(new View.OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {
				// Dentro de 0 milisegundos avísame cada 1000 milisegundos
				TimerTask timerTaskD = new TimerTask() {
					public void run() {
						if (derPulsado == true) {
							sendMessage("R\r");
						}
						// Aquí el código que queremos ejecutar.
					}
				};

				// Aquí se pone en marcha el timer cada segundo.
				Timer timerd = new Timer();
				timerd.schedule(timerTaskD, 0, 1500);
				// timerd.scheduleAtFixedRate(timerTaskD, 0, 3000);
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					Log.d("Pressed", "Button R pressed");
					derPulsado = true;
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					derPulsado = false;
					Log.d("Released", "Button R released");
				}

				// TODO Auto-generated method stub
				return false;
			}
		});

		final Button BotonIzq = (Button) findViewById(R.id.giroIzquierda);
		BotonIzq.setOnTouchListener(new View.OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {
				// Dentro de 0 milisegundos avísame cada 1000 milisegundos
				TimerTask timerTaskI = new TimerTask() {
					public void run() {
						if (izqPulsado == true) {
							sendMessage("L\r");
						}
						// Aquí el código que queremos ejecutar.
					}
				};

				// Aquí se pone en marcha el timer cada segundo.
				Timer timeri = new Timer();
				timeri.schedule(timerTaskI, 0, 1500);
				//timeri.scheduleAtFixedRate(timerTaskI, 0, 700);
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					Log.d("Pressed", "Button L pressed");
					izqPulsado = true;
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					izqPulsado = false;
					Log.d("Released", "Button L released");
				}

				// TODO Auto-generated method stub
				return false;
			}
		});

		final Button BotonAdelante = (Button) findViewById(R.id.acelera);
		BotonAdelante.setOnTouchListener(new View.OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {
				// Dentro de 0 milisegundos avísame cada 1000 milisegundos
				TimerTask timerTaskF = new TimerTask() {
					public void run() {
						if (adelantePulsado == true) {
							sendMessage("A\r");
						}
						// Aquí el código que queremos ejecutar.
					}
				};

				// Aquí se pone en marcha el timer cada segundo.
				Timer timerf = new Timer();
				timerf.schedule(timerTaskF, 0, 1500);
				//timera.scheduleAtFixedRate(timerTaskA, 0, 700);
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					Log.d("Pressed", "Button A pressed");
					adelantePulsado = true;
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					adelantePulsado = false;
					Log.d("Released", "Button A released");
				}

				// TODO Auto-generated method stub
				return false;
			}
		});
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
			if (seleccionador) {
				comandoAcelX = comandoAcelerometroX(curX);
				comandoAcelY = comandoAcelerometroY(curY);
			}
			((TextView) findViewById(R.id.X)).setText("X: " + curX);
			((TextView) findViewById(R.id.comandoX)).setText("Comando: "
					+ comandoAcelX);
			((TextView) findViewById(R.id.Y)).setText("Y: " + curY);
			((TextView) findViewById(R.id.comandoY)).setText("Comando: "
					+ comandoAcelY);
		}
	}

	public char comandoAcelerometroY(float valorY) {
		char res = ' ';
		if (valorY < -3.0) {
			res = 'L';
			sendMessage(res + "\r");
			Log.d("Acelerometro", res + "\r");
		}
		if (valorY > 3.0) {
			res = 'R';
			sendMessage(res + "\r");
			Log.d("Acelerometro", res + "\r");
		}
		return res;
	}

	public char comandoAcelerometroX(float valorX) {
		char res = ' ';
		if (valorX > 2.0 && valorX < 5.0) {
			res = 'A';
			// sendMessage(res + "\r");
			Log.d("Acelerometro", res + "\r");
		}
		if (valorX > 9.0 && valorX < 10.0) {
			res = 'B';
			// sendMessage(res + "\r");
			Log.d("Acelerometro", res + "\r");
		}
		return res;
	}

}
