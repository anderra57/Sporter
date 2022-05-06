package com.anderpri.das_grupal.activities;

public class Actividad {
    String name;
    String description;
    String fecha; //lo cambiaré a algun formato de fecha, por ahora es mas simple así
    String city;

    public Actividad(String name, String description, String fecha, String city){
        this.name=name;
        this.description=description;
        this.fecha=fecha;
        this.city=city;
    }

}
