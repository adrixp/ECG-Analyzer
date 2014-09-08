package com.ecg_analyzer;

public class ListaFM {
	
	private String nombre;
	private String tipo;
	
	
	public ListaFM (String nombre, String tipo) {
		this.nombre = nombre;
		this.tipo = tipo;
	}
	public String getNombre() {
		return nombre;
	}
	public String getTipo() {
		return tipo;
	}
}
