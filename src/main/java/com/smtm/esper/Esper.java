package com.smtm.esper;

import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.UpdateListener;
import com.espertech.esper.client.time.CurrentTimeEvent;
import com.smtm.Tick;
import com.smtm.TickListener;
import com.smtm.lmax.Config;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * User: <a href="mailto:simeon.petkov@methodia.com">Simeon Petkov</a>
 * Date: 10/31/13
 * Time: 8:22 PM
 * (c) 2012 Methodia Ltd., Sofia, Bulgaria
 */
public enum Esper implements TickListener {

    API;

    private static final Logger LOG = Logger.getLogger(Esper.class);

    private Map<String, String> instrumentsStreams = new HashMap<String, String>();

    private EPServiceProvider service;

    public void init() {
        for (String instrument : Config.<String[]>get(Config.Key.LMAX_INSTRUMENTS)) {
            instrumentsStreams.put(instrument, instrument.replace("/", ""));
        }
        configure();
        initStreams();
    }

    @Override
    public void tick(Tick tick) {
        if (service.getEPRuntime().getCurrentTime() != tick.getTimestamp().getTime()) {
            CurrentTimeEvent timeEvent = new CurrentTimeEvent(tick.getTimestamp().getTime());
            service.getEPRuntime().sendEvent(timeEvent);
        }
        service.getEPRuntime().sendEvent(tick);
    }

    private void configure() {
        final Configuration configuration = new Configuration();
        configuration.configure("slepota.esper.cfg.xml");
        configuration.getEngineDefaults().getThreading().setInternalTimerEnabled(false);
        service = EPServiceProviderManager.getDefaultProvider(configuration);
        service.getEPRuntime().sendEvent(new CurrentTimeEvent(0));
    }

    private void initStreams() {

        for (String instrument : Config.<String[]>get(Config.Key.LMAX_INSTRUMENTS)) {

            service.getEPAdministrator().createEPL(instrumentize(Epl.AVGPRICE_24H_RAW, instrument),
                    instrumentize("<INSTRUMENT-STREAM>_AVGPRICE_24H_RAW", instrument));

            service.getEPAdministrator().createEPL(instrumentize(Epl.AVGPRICE_3H_RAW, instrument),
                    instrumentize("<INSTRUMENT-STREAM>_AVGPRICE_3H_RAW", instrument));
            service.getEPAdministrator().createEPL(instrumentize(Epl.AVGPRICE_15MIN_RAW, instrument),
                    instrumentize("<INSTRUMENT-STREAM>_AVGPRICE_15MIN_RAW", instrument));

            service.getEPAdministrator().createEPL(instrumentize(Epl.AVGPRICE_24H_15MIN, instrument),
                    instrumentize("<INSTRUMENT-STREAM>_AVGPRICE_24H_15MIN", instrument));
            service.getEPAdministrator().createEPL(instrumentize(Epl.AVGPRICE_3H_15MIN, instrument),
                    instrumentize("<INSTRUMENT-STREAM>_AVGPRICE_3H_15MIN", instrument));
            service.getEPAdministrator().createEPL(instrumentize(Epl.AVGPRICE_15MIN_15MIN, instrument),
                    instrumentize("<INSTRUMENT-STREAM>_AVGPRICE_15MIN_15MIN", instrument));

            service.getEPAdministrator().createEPL(instrumentize(Epl.TRIPLE_AVG_DOWN, instrument),
                    instrumentize("<INSTRUMENT-STREAM>_TRIPLE_AVG_DOWN", instrument));
            service.getEPAdministrator().createEPL(instrumentize(Epl.TRIPLE_AVG_UP, instrument),
                    instrumentize("<INSTRUMENT-STREAM>_TRIPLE_AVG_UP", instrument));

            service.getEPAdministrator().createEPL(instrumentize(Epl.TRIPLE_AVG_DOWN_SWITCH, instrument),
                    instrumentize("<INSTRUMENT-STREAM>_TRIPLE_AVG_DOWN_SWITCH", instrument));
            service.getEPAdministrator().createEPL(instrumentize(Epl.TRIPLE_AVG_UP_SWITCH, instrument),
                    instrumentize("<INSTRUMENT-STREAM>_TRIPLE_AVG_UP_SWITCH", instrument));

            LOG.info("Initialized streams prefixed (" + instrumentsStreams.get(instrument) + ") for " + instrument);
        }

    }

    public String instrumentize(String statement, String instrument) {
        String instrumented = statement.replaceAll("<INSTRUMENT-NAME>", instrument)
                .replaceAll("<INSTRUMENT-STREAM>", instrumentsStreams.get(instrument));
        return instrumented;
    }

    public void addListener(String stream, UpdateListener listener) {
        service.getEPAdministrator().getStatement(stream).addListener(listener);
    }

    public void detachListener(String stream, UpdateListener listener) {
        service.getEPAdministrator().getStatement(stream).removeListener(listener);
    }

}
