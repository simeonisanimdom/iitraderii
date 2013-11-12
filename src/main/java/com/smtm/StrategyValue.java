package com.smtm;

/**
 * User: <a href="mailto:simeon.petkov@methodia.com">Simeon Petkov</a>
 * Date: 11/10/13
 * Time: 10:59 PM
 * (c) 2012 Methodia Ltd., Sofia, Bulgaria
 */
public enum StrategyValue {

    INSTANCE;

    private Double value = 0d;
    private int profits = 0;
    private int losses = 0;

    public void put(Double value) {
        this.value += value;
        if(value > 0) {
            profits++;
        } else {
            losses++;
        }
    }

    public Double get() {
        return value;
    }

    public int getProfits() {
        return profits;
    }

    public int getLosses() {
        return losses;
    }
}
