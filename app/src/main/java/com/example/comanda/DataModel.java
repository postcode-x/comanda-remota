package com.example.comanda;

public class DataModel {

    String numero;
    String nombre;
    String boleta;
    String elementos;


    public DataModel(String numero, String nombre, String boleta, String elementos) {
        this.numero = numero;
        this.nombre = nombre;
        this.boleta = boleta;
        this.elementos = elementos;

    }

    public String getNumero() {
        return numero;
    }

    public String getNombre() {
        return nombre;
    }

    public String getBoleta() { return boleta; }

    public  String  getElementos() {
        return elementos;
    }

}