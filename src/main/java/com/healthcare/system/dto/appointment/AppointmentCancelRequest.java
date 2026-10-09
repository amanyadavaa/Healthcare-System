package com.healthcare.system.dto.appointment;

public class AppointmentCancelRequest {

    private String reason;

    public AppointmentCancelRequest() {}

    public AppointmentCancelRequest(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
