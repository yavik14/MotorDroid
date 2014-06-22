package com.theyavikteam.motordroid;

import java.util.ArrayList;
import java.util.List;

//Clase auxiliar para el control de los estadosy comandos a enviar haciendo uso del acelerómetro
public class ComandosAcelerometro {
	//Dos listas con los comandos a enviar, ya sean de direccion o de velocidad
	public List<String> comandosDireccion = new ArrayList<String>();
	public List<String> comandosVelocidad = new ArrayList<String>();
	
	//Dos listas con la comprobacion de los estados de direccion, si estan pulsado y fueron pulsados anteriormente
	public List<Boolean> comandosDireccionPulsados = new ArrayList<Boolean>();
	public List<Boolean> comandosDireccionFueronPulsados = new ArrayList<Boolean>();
	
	//Dos listas con la comprobacion de los estados de velocidad, si estan pulsado y fueron pulsados anteriormente
	public List<Boolean> comandosVelocidadPulsados = new ArrayList<Boolean>();
	public List<Boolean> comandosVelocidadFueronPulsados = new ArrayList<Boolean>();
	
	//Constructor
	public ComandosAcelerometro(){
		//Rellenamos las listas de comandos
		addComandos();
		
		//Inicializamos las listas de estados de direccion y velocidad a false
		inicializaLista(comandosDireccionPulsados, 7);
		inicializaLista(comandosDireccionFueronPulsados, 7);
		
		inicializaLista(comandosVelocidadPulsados, 5);
		inicializaLista(comandosVelocidadFueronPulsados, 5);
	}
	
	//Añadir comandos
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
	
	//Inicializar lista
	public void inicializaLista(List<Boolean> b, int tam){
		for(int i = 0; i < tam; i++){
			b.add(false);
		}
	}
	
	//Actualizar lista dejando tal cual un objeto indicado de ella
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
	
	//Actualizar lista cambiando un objeto indicado a true
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
	
	//Compara el estado actual con el anterior, si antes no estaba activo se devuelve true
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
