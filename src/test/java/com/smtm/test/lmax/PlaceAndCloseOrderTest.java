package com.smtm.test.lmax;

import com.lmax.api.FixedPointNumber;
import com.smtm.CloseOnEventMarketOrder;
import com.smtm.lmax.Lmax;
import org.junit.Test;

/**
 * User: <a href="mailto:simeon.petkov@methodia.com">Simeon Petkov</a>
 * Date: 11/5/13
 * Time: 4:05 PM
 * (c) 2012 Methodia Ltd., Sofia, Bulgaria
 */
public class PlaceAndCloseOrderTest {

    @Test
    public void testPlaceAndCloseOrder() {
        LmaxStarter starter = new LmaxStarter();
        starter.start();
        try {
            Thread.sleep(3000l);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        CloseOnEventMarketOrder eurUsdListeningOrder = new CloseOnEventMarketOrder();
        Lmax.API.placeOrder("EUR/USD", FixedPointNumber.valueOf("10.0"), eurUsdListeningOrder);
//        CloseOnEventMarketOrder gbpUsdListeningOrder = new CloseOnEventMarketOrder();
//        Lmax.API.placeOrder("GBP/USD", FixedPointNumber.valueOf("10.0"), gbpUsdListeningOrder);
        try {
            Thread.sleep(10000l);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Lmax.API.closeOrder(eurUsdListeningOrder);
//        Lmax.API.closeOrder(gbpUsdListeningOrder);
        try {
            Thread.sleep(10000l);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    class LmaxStarter extends Thread {
        @Override
        public void run() {
            Lmax.API.login();
            Lmax.API.init();
            Lmax.API.start();
        }
    }

}
