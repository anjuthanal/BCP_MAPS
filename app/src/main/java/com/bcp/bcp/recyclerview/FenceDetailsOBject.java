package com.bcp.bcp.recyclerview;

/**
 * Created by anjup on 3/31/16.
 */
public class FenceDetailsOBject {

    private String fAddress;
    private String fStatus;
    private String fTime;

    FenceDetailsOBject (String fAddress, String fStatus,String fTime){
        this.fAddress = fAddress;
        this.fStatus = fStatus;
        this.fTime = fTime;
    }

    public String getfAddress() {
        return fAddress;
    }

    public void setfAddress(String fAddress) {
        this.fAddress = fAddress;
    }

    public String getfStatus() {
        return fStatus;
    }

    public void setfStatus(String fStatus) {
        this.fStatus = fStatus;
    }

    public String getfTime() {
        return fTime;
    }

    public void setfTime(String fTime) {
        this.fTime = fTime;
    }
}
