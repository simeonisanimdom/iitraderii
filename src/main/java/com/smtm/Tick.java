package com.smtm;

import java.util.Date;

/**
 * User: <a href="mailto:simeon.petkov@methodia.com">Simeon Petkov</a>
 * Date: 10/31/13
 * Time: 2:15 PM
 * (c) 2012 Methodia Ltd., Sofia, Bulgaria
 */
public class Tick {

    private Date timestamp;
    private String instrument;
    private double price;
    private double volume;

    public Tick() {
    }

    public Tick(Date timestamp, double price, double volume, String instrument) {
        this.timestamp = timestamp;
        this.price = price;
        this.volume = volume;
        this.instrument = instrument;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getVolume() {
        return volume;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }

    public String getInstrument() {
        return instrument;
    }

    public void setInstrument(String instrument) {
        this.instrument = instrument;
    }
}
