package com.salaboy.payments.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Payment {
    private String reservationId;
    private Double amount;
    private String subject;


    public Payment() {
    }


    public String getReservationId() {
        return reservationId;
    }

    public void setReservationId(String reservationId) {
        this.reservationId = reservationId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    @Override
    public String toString() {
        return "Payment{" +
                "reservationId='" + reservationId + '\'' +
                ", amount=" + amount +
                ", subject='" + subject + '\'' +
                '}';
    }
}
