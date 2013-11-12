package com.smtm.esper.chart;

import com.google.gson.Gson;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import java.util.*;

/**
 * User: <a href="mailto:simeon.petkov@methodia.com">Simeon Petkov</a>
 * Date: 11/11/13
 * Time: 2:15 AM
 * (c) 2012 Methodia Ltd., Sofia, Bulgaria
 */
@ManagedBean(name = "highchartBean")
@ApplicationScoped
public class HighchartBean {

    private static final int WINDOW_SIZE = 40000;

    private Object[] raw;
    private Object[] maShort;
    private Object[] maMiddle;
    private Object[] maLong;

    private String maShortDataString;
    private String maMiddleDataString;
    private String maLongDataString;

    public HighchartBean() {
        initData();
    }

    public Object[] getMaShort() {
        return maShort;
    }

    public void setMaShort(Object[] maShort) {
        this.maShort = maShort;
    }

    public String getMaShortDataString() {
        maShortDataString = new Gson().toJson(maShort);
        return maShortDataString;
    }

    public void setMaShortDataString(String maShortDataString) {
        this.maShortDataString = maShortDataString;
    }

    public Object[] getMaMiddle() {
        return maMiddle;
    }

    public void setMaMiddle(Object[] maMiddle) {
        this.maMiddle = maMiddle;
    }

    public Object[] getMaLong() {
        return maLong;
    }

    public void setMaLong(Object[] maLong) {
        this.maLong = maLong;
    }

    public String getMaMiddleDataString() {
        return maMiddleDataString;
    }

    public void setMaMiddleDataString(String maMiddleDataString) {
        this.maMiddleDataString = maMiddleDataString;
    }

    public String getMaLongDataString() {
        return maLongDataString;
    }

    public void setMaLongDataString(String maLongDataString) {
        this.maLongDataString = maLongDataString;
    }

    public Object[] getRaw() {
        return raw;
    }

    public void setRaw(Object[] raw) {
        this.raw = raw;
    }

    private void initData() {
        Calendar cal = Calendar.getInstance();
        List outer = new ArrayList();
        cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) - 1);
        for (int i = 0; i <= 40000; i++) {
            cal.add(Calendar.MINUTE, 1);
            Object[] inner = new Object[]{cal.getTime().getTime(), new Random().nextInt(5)};
            outer.add(inner);
        }

        maShort = outer.toArray();
        maShortDataString = new Gson().toJson(maShort);
    }

}
