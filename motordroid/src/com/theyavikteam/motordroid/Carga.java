package com.theyavikteam.motordroid;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;

//Actividad de pantalla de carga
public class Carga extends Activity {
		//Tiempo de carga
	    private final int SPLASH_DISPLAY_LENGTH = 3000;
	 
	    //Cuando se crea la actividad la ejecutamos durante el tiempo indicado y damos paso a la actividad Motordroid
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        requestWindowFeature(Window.FEATURE_NO_TITLE);
	        setContentView(R.layout.carga);
	 
	        new Handler().postDelayed(new Runnable() {
	           
	            @Override
	            public void run() {
	                Intent mainIntent = new Intent(Carga.this, Motordroid.class);
	                Carga.this.startActivity(mainIntent);
	                Carga.this.finish();
	            }
	        }, SPLASH_DISPLAY_LENGTH);
	    }
	}
