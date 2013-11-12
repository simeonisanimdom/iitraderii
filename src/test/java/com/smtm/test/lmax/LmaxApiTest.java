package com.smtm.test.lmax;

import com.smtm.MovingAveragesSpeculator;
import com.smtm.StrategyValue;
import com.smtm.lmax.Config;
import com.smtm.lmax.Lmax;
import org.junit.Test;

/**
 * User: <a href="mailto:simeon.petkov@methodia.com">Simeon Petkov</a>
 * Date: 10/31/13
 * Time: 2:58 PM
 * (c) 2012 Methodia Ltd., Sofia, Bulgaria
 */
public class LmaxApiTest {

    @Test
    public void testLmaxApi() {
        Lmax.API.setLookBackOnly(true);
//        Config.put(Config.Key.LMAX_INSTRUMENTS, new String[]{"EUR/USD"});
        MovingAveragesSpeculator.INSTANCE.start();
        System.out.println("\n\n");
        System.out.println("Course diff: " + StrategyValue.INSTANCE.get());
        System.out.println("Profits: " + StrategyValue.INSTANCE.getProfits());
        System.out.println("Losses: " + StrategyValue.INSTANCE.getLosses());
    }

}
