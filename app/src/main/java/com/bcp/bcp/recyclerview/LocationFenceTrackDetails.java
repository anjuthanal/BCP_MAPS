package com.bcp.bcp.recyclerview;

/**
 * Created by anjup on 4/14/16.
 */
public class LocationFenceTrackDetails {

    private String address;
    private String status;
    private String time;

   public LocationFenceTrackDetails(){}

    LocationFenceTrackDetails(String address, String status, String time){
        this.address = address;
        this.status = status;
        this.time = time;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
