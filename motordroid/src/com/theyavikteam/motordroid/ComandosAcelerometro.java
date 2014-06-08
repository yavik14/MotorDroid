package com.theyavikteam.motordroid;

import java.util.ArrayList;
import java.util.List;

public class ComandosAcelerometro {
	
	public List<String> comandosDireccion = new ArrayList<String>();
	public List<String> comandosVelocidad = new ArrayList<String>();
	
	public List<Boolean> comandosDireccionPulsados = new ArrayList<Boolean>();
	public List<Boolean> comandosDireccionFueronPulsados = new ArrayList<Boolean>();
	
	public List<Boolean> comandosVelocidadPulsados = new ArrayList<Boolean>();
	public List<Boolean> comandosVelocidadFueronPulsados = new ArrayList<Boolean>();
	
	public ComandosAcelerometro(){
		addComandos();
		
		inicializaLista(comandosDireccionPulsados, 7);
		inicializaLista(comandosDireccionFueronPulsados, 7);
		
		inicializaLista(comandosVelocidadPulsados, 5);
		inicializaLista(comandosVelocidadFueronPulsados, 5);
	}
	
	public void addComandos(){
		comandosVelocidad.add("A");
		comandosVelocidad.add("B");
		comandosVelocidad.add("C");
		comandosVelocidad.add("D");
		comandosVelocidad.add("E");
		
		comandosDireccion.add("F");
		comandosDireccion.add("G");
		comandosDireccion.add("H");
		comandosDireccion.add("I");
		comandosDireccion.add("J");
		comandosDireccion.add("K");
		comandosDireccion.add("L");
	}
	
	public void inicializaLista(List<Boolean> b, int tam){
		for(int i = 0; i < tam; i++){
			b.add(false);
		}
	}
	
	public void actualizaLista(List<Boolean> l,int index, int tam){
		boolean aux = l.get(index);
		for(int i = 0; i < tam; i++){
			if(i == index){
				l.add(aux);
			}else{
				l.add(false);	
			}
			
		}
	}
	
	public void actualizaListaComando(List<Boolean> l,int index, int tam){
		l.removeAll(l);
		for(int i = 0; i < tam; i++){
			if(i == index){
				l.add(true);
			}else{
				l.add(false);	
			}
			
		}
	}
	
	public boolean comparaComando(List<Boolean> fue, List<Boolean> ahora, int i){
		boolean res = false;
		boolean fuePulsado = fue.get(i);
		boolean ahoraPulsado = ahora.get(i);
		if (fuePulsado != ahoraPulsado){
			res = true;
		}
		return res;
	}
	
	
	
}
