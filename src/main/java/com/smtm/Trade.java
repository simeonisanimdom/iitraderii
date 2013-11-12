package com.smtm;

/**
 * User: <a href="mailto:simeon.petkov@methodia.com">Simeon Petkov</a>
 * Date: 11/11/13
 * Time: 8:45 PM
 * (c) 2012 Methodia Ltd., Sofia, Bulgaria
 */
public class Trade {

    private String id;
    private Action action;

    public enum Action {
        LONG, SHORT, CLOSE;
    }

    public Trade() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

}
