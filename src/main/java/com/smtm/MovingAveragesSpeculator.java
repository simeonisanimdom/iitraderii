package com.smtm;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;
import com.lmax.api.FixedPointNumber;
import com.smtm.esper.Esper;
import com.smtm.lmax.Config;
import com.smtm.lmax.Lmax;
import org.apache.log4j.Logger;

/**
 * User: <a href="mailto:simeon.petkov@methodia.com">Simeon Petkov</a>
 * Date: 11/1/13
 * Time: 3:46 PM
 * (c) 2012 Methodia Ltd., Sofia, Bulgaria
 */
public enum MovingAveragesSpeculator {

    INSTANCE;

    private static Logger LOG = Logger.getLogger(MovingAveragesSpeculator.class);

    public void start() {
        Esper.API.init();

        for (final String instrument : Config.<String[]>get(Config.Key.LMAX_INSTRUMENTS)) {

            Esper.API.addListener(Esper.API.instrumentize("<INSTRUMENT-STREAM>_TRIPLE_AVG_DOWN_SWITCH", instrument), new UpdateListener() {
                @Override
                public void update(EventBean[] newEvents, EventBean[] oldEvents) {
                    //TODO - too many evetns received - should be just one?
                    LOG.info(String.format("SHORT\t%s\t%.5f\t%td.%tm %tl:%tM %tZ",
                            newEvents[0].getEventType().getName().split("_")[0],
                            newEvents[0].get("a.price"),
                            newEvents[0].get("a.timestamp"),
                            newEvents[0].get("a.timestamp"),
                            newEvents[0].get("a.timestamp"),
                            newEvents[0].get("a.timestamp"),
                            newEvents[0].get("a.timestamp")));
                    if (Lmax.API.getProcessingState().equals(Lmax.ProcessingState.PROCESSING_LIVE)) {
                        CloseOnEventMarketOrder closeOnEventMarketOrder = new CloseOnEventMarketOrder();
                        Lmax.API.placeOrder(instrument, FixedPointNumber.valueOf("-10"), closeOnEventMarketOrder);
                        Esper.API.addListener(Esper.API.instrumentize("<INSTRUMENT-STREAM>_TRIPLE_AVG_UP_SWITCH", instrument), closeOnEventMarketOrder);
                    } else {
                        OneTwoPercentLimitSwitchCloseSimulatingOrder simulatedOrder = new OneTwoPercentLimitSwitchCloseSimulatingOrder(OneTwoPercentLimitSwitchCloseSimulatingOrder.Direction.DOWN,
                                new String[]{Esper.API.instrumentize("<INSTRUMENT-STREAM>_TRIPLE_AVG_UP_SWITCH", instrument),
                                Esper.API.instrumentize("<INSTRUMENT-STREAM>_AVGPRICE_15MIN_RAW", instrument)});
                    }
                }
            });

            Esper.API.addListener(Esper.API.instrumentize("<INSTRUMENT-STREAM>_TRIPLE_AVG_UP_SWITCH", instrument), new UpdateListener() {
                @Override
                public void update(EventBean[] newEvents, EventBean[] oldEvents) {
                    //TODO - too many evetns received - should be just one?
                    LOG.info(String.format("LONG\t%s\t%.5f\t%td.%tm %tl:%tM %tZ",
                            newEvents[0].getEventType().getName().split("_")[0],
                            newEvents[0].get("a.price"),
                            newEvents[0].get("a.timestamp"),
                            newEvents[0].get("a.timestamp"),
                            newEvents[0].get("a.timestamp"),
                            newEvents[0].get("a.timestamp"),
                            newEvents[0].get("a.timestamp")));
                    if (Lmax.API.getProcessingState().equals(Lmax.ProcessingState.PROCESSING_LIVE)) {
                        CloseOnEventMarketOrder closeOnEventMarketOrder = new CloseOnEventMarketOrder();
                        Lmax.API.placeOrder(instrument, FixedPointNumber.valueOf("10"), closeOnEventMarketOrder);
                        Esper.API.addListener(Esper.API.instrumentize("<INSTRUMENT-STREAM>_TRIPLE_AVG_DOWN_SWITCH", instrument), closeOnEventMarketOrder);
                    } else {
                        OneTwoPercentLimitSwitchCloseSimulatingOrder simulatedOrder = new OneTwoPercentLimitSwitchCloseSimulatingOrder(OneTwoPercentLimitSwitchCloseSimulatingOrder.Direction.UP,
                                new String[]{Esper.API.instrumentize("<INSTRUMENT-STREAM>_TRIPLE_AVG_DOWN_SWITCH", instrument),
                                        Esper.API.instrumentize("<INSTRUMENT-STREAM>_AVGPRICE_15MIN_RAW", instrument)});

                    }
                }
            });

            LOG.info("Registered triple MA up/down listeners on instrument " + instrument);

        }

        Lmax.API.addTickListener(Esper.API);
        Lmax.API.login();
        Lmax.API.init();
        Lmax.API.subscribe();
        Lmax.API.start();
    }

    public void stop() {
        Lmax.API.unsubscribe();
    }

}
