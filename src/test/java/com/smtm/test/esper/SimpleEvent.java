package com.smtm.test.esper;

/**
 * User: <a href="mailto:simeon.petkov@methodia.com">Simeon Petkov</a>
 * Date: 10/21/13
 * Time: 8:12 PM
 * (c) 2012 Methodia Ltd., Sofia, Bulgaria
 */
public class SimpleEvent {

    private String name;

    private double high;
    private double low;
    private double close;

    public SimpleEvent() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getHigh() {
        return high;
    }

    public void setHigh(double high) {
        this.high = high;
    }

    public double getLow() {
        return low;
    }

    public void setLow(double low) {
        this.low = low;
    }

    public double getClose() {
        return close;
    }

    public void setClose(double close) {
        this.close = close;
    }
}
