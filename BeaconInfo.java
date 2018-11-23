package com.conf_beacons_ios;

/**
 */

public class BeaconInfo {

    String deviceMac;
    String intensidad;
    double distancia;
    boolean disponible;

    public BeaconInfo(String deviceMac, String intensidad, double distancia){
        this.deviceMac=deviceMac;
        this.intensidad=intensidad;
        this.distancia=distancia;
        this.disponible=false;
    }

    public String getDeviceMac(){
        return this.deviceMac;
    }

    public String getIntensidad(){
        return this.intensidad;
    }

    public double getDistancia(){
        return this.distancia;
    }

    public boolean getDisponible(){
        return this.disponible;
    }

    public void setDisponible (boolean disponibilidad){
        this.disponible=disponibilidad;
    }
}
