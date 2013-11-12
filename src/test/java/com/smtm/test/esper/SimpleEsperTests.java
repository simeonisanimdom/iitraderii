package com.smtm.test.esper;

import com.espertech.esper.client.*;
import org.junit.Test;

import java.util.Random;

/**
 * User: <a href="mailto:simeon.petkov@methodia.com">Simeon Petkov</a>
 * Date: 10/21/13
 * Time: 8:06 PM
 * (c) 2012 Methodia Ltd., Sofia, Bulgaria
 */
public class SimpleEsperTests {

    @Test
    public void testSimpleEventProduceMatchConsume() {
        final Configuration configuration = new Configuration();
        configuration.configure("slepota.esper.cfg.xml");
        final EPServiceProvider service = EPServiceProviderManager.getDefaultProvider(configuration);
        final String avgExpression = "select avg(high) from SimpleEvent";
        final EPStatement avgStatement = service.getEPAdministrator().createEPL(avgExpression);
        final SimpleEventListener avgListener = new SimpleEventListener();
        avgStatement.addListener(avgListener);

        for(int counter = 0; counter < 1000; counter++) {
            final SimpleEvent someEvent = new SimpleEvent();
            someEvent.setName("Test event " + (counter + 1));
            someEvent.setHigh(Math.random() * 6 + 5);
            someEvent.setLow(someEvent.getHigh() - 5);
            someEvent.setClose(someEvent.getHigh());
            service.getEPRuntime().sendEvent(someEvent);
        }
    }

    @Test
    public void anotherTaLibTest() {
        final Configuration configuration = new Configuration();
        configuration.configure("slepota.esper.cfg.xml");

        final EPServiceProvider service = EPServiceProviderManager.getDefaultProvider(configuration);

        final String statement = "insert into minval select talib(\"min\", high, 3) as minval from SimpleEvent";

        final EPStatement epStatement = service.getEPAdministrator().createEPL(statement);

        epStatement.addListener(new UpdateListener() {
            @Override
            public void update(EventBean[] eventBeans, EventBean[] eventBeans2) {
                System.out.println(eventBeans[0].get("minval") + "\n");
            }
        });

        for(int counter = 0; counter < 100; counter++) {
            final SimpleEvent someEvent = new SimpleEvent();
            someEvent.setName("Test event " + (counter + 1));
            someEvent.setHigh(counter);
            someEvent.setLow(0);
            someEvent.setClose(0);
            System.out.println(someEvent.getHigh());
            service.getEPRuntime().sendEvent(someEvent);
        }
    }

    @Test
    public void taLibTest() {
        final Configuration configuration = new Configuration();
        configuration.configure("slepota.esper.cfg.xml");

        final EPServiceProvider service = EPServiceProviderManager.getDefaultProvider(configuration);

        final String statement = "insert into StochF select talib(\"stochF\", high, low, close, 3, 2, \"Sma\") as values from SimpleEvent";
        final EPStatement stochF = service.getEPAdministrator().createEPL(statement);
        final SimpleStochFListener stochFListener = new SimpleStochFListener();

        stochF.addListener(stochFListener);

        for(int counter = 0; counter < 10; counter++) {
            final SimpleEvent someEvent = new SimpleEvent();
            someEvent.setName("Test event " + (counter + 1));
            someEvent.setHigh(Math.random() * 6 + 5);
            someEvent.setLow(someEvent.getHigh() - 5);
            someEvent.setClose(someEvent.getHigh());
            service.getEPRuntime().sendEvent(someEvent);
        }
    }

    @Test
    public void simpleRandomTest() {
        System.out.println(new Random().nextInt(5));
        System.out.println(new Random().nextInt(5));
    }

}
