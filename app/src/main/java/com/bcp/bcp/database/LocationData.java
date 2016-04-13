package com.bcp.bcp.database;

/**
 * Created by anjup on 4/13/16.
 */
public class LocationData {

    int id;
    String locAddress;
    String locDatetime;

    public LocationData(){}

    public LocationData(int id, String locAddress,String Datetime){

        this.id = id;
        this.locAddress = locAddress;
        this.locDatetime = locDatetime;

    }
    public LocationData( String locAddress, String locDatetime){

        this.locAddress = locAddress;
        this.locDatetime = locDatetime;

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLocAddress() {
        return locAddress;
    }

    public void setLocAddress(String locAddress) {
        this.locAddress = locAddress;
    }

    public String getLocDatetime() {
        return locDatetime;
    }

    public void setLocDatetime(String locDatetime) {
        this.locDatetime = locDatetime;
    }
}
