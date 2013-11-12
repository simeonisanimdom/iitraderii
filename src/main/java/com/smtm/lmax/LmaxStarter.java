package com.smtm.lmax;

import com.smtm.esper.Esper;

/**
 * User: <a href="mailto:simeon.petkov@methodia.com">Simeon Petkov</a>
 * Date: 11/2/13
 * Time: 3:54 PM
 * (c) 2012 Methodia Ltd., Sofia, Bulgaria
 */
public class LmaxStarter extends Thread {

    @Override
    public void run() {
        Lmax.API.login();
        Lmax.API.init();
        Lmax.API.subscribe();
        Lmax.API.start();
    }
}
