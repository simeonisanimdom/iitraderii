package com.smtm.test.esper;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;
import org.apache.log4j.Logger;

/**
 * User: <a href="mailto:simeon.petkov@methodia.com">Simeon Petkov</a>
 * Date: 10/21/13
 * Time: 8:23 PM
 * (c) 2012 Methodia Ltd., Sofia, Bulgaria
 */
public class SimpleEventListener implements UpdateListener {

    private static final Logger LOG = Logger.getLogger(SimpleEventListener.class);

    @Override
    public void update(EventBean[] newEvents, EventBean[] oldEvents) {
        EventBean event = newEvents[0];
        LOG.info("New average: " + event.get("avg(high)"));
    }

}
