package com.bcp.bcp.database;

import java.util.Date;

/**
 * Created by anjup on 3/31/16.
 */
public class FenceTiming {

    int id;
    String fenceAddress;
    String status;
    String Datetime;

    public FenceTiming(){}

    public FenceTiming(int id, String fenceAddress, String status, String Datetime){

        this.id = id;
        this.fenceAddress = fenceAddress;
        this.status = status;
        this.Datetime = Datetime;

    }
    public FenceTiming( String fenceAddress, String status, String Datetime){

        this.fenceAddress = fenceAddress;
        this.status = status;
        this.Datetime = Datetime;

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFenceAddress() {
        return fenceAddress;
    }

    public void setFenceAddress(String fenceAddress) {
        this.fenceAddress = fenceAddress;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDatetime() {
        return Datetime;
    }

    public void setDatetime(String datetime) {
        Datetime = datetime;
    }
}
