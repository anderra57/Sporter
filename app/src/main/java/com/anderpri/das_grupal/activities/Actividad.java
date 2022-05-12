package com.anderpri.das_grupal.activities;

public class Actividad {
    String name;
    String description;
    String fecha; //lo cambiaré a algun formato de fecha, por ahora es mas simple así
    String city;
    String image;
    String latitude;
    String longitude;

    public Actividad(String name, String description, String fecha, String city, String image, String latitude, String longitude){
        this.name=name;
        this.description=description;
        this.fecha=fecha;
        this.city=city;
        this.image = image;
        this.latitude = latitude;
        this.longitude = longitude;
    }

}
