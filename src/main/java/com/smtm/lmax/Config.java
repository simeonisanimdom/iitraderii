package com.smtm.lmax;

import com.lmax.api.account.LoginRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * User: <a href="mailto:simeon.petkov@methodia.com">Simeon Petkov</a>
 * Date: 10/31/13
 * Time: 1:57 PM
 * (c) 2012 Methodia Ltd., Sofia, Bulgaria
 */
public class Config {

    public static enum Key {
        LMAX_INSTRUMENTS,
        LMAX_LOOKBACK_HOURS,
        LMAX_URL,
        LMAX_USER,
        LMAX_PASS,
        LMAX_PRODUCT,
        LMAX_HEARTBEAT_MILLIS,
        LMAX_PROFIT_TARGET_OFFSET_PERCENT,
        LMAX_STOP_LOSS_OFFSET_PERCENT;
    }

    private static Map<Key, Object> configs = new HashMap<Key, Object>();

    static {
        configs.put(Key.LMAX_INSTRUMENTS, new String[]{"EUR/USD",
                "GBP/USD",
                "AUD/USD",
                "USD/JPY",
                "EUR/GBP",
                "EUR/CHF",
                "USD/CHF",
                "EUR/JPY",
                "GBP/JPY",
                "EUR/CAD",
                "USD/CAD"});
        configs.put(Key.LMAX_HEARTBEAT_MILLIS, 300000l); //=5 minutes
        configs.put(Key.LMAX_LOOKBACK_HOURS, 168 * 5);
        configs.put(Key.LMAX_URL, "https://testapi.lmaxtrader.com");
        configs.put(Key.LMAX_USER, "iitraderii");
        configs.put(Key.LMAX_PASS, "tradetrade666");
        configs.put(Key.LMAX_PRODUCT, LoginRequest.ProductType.CFD_DEMO);
        configs.put(Key.LMAX_PROFIT_TARGET_OFFSET_PERCENT, 10);
        configs.put(Key.LMAX_STOP_LOSS_OFFSET_PERCENT, 5);
    }

    public static <T> T get(final Key key) {
        return (T) configs.get(key);
    }

    public static <T> void put(final Key key, final T value) {
        configs.put(key, value);
    }

}
