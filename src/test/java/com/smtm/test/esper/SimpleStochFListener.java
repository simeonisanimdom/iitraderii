package com.smtm.test.esper;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

/**
 * User: <a href="mailto:simeon.petkov@methodia.com">Simeon Petkov</a>
 * Date: 11/6/13
 * Time: 4:12 PM
 * (c) 2012 Methodia Ltd., Sofia, Bulgaria
 */
public class SimpleStochFListener implements UpdateListener {
    @Override
    public void update(EventBean[] newEvents, EventBean[] oldEvents) {
        if (newEvents[0].get("values") != null) {
            try {
                System.out.println("fastk " + newEvents[0].get("values").getClass().getField("fastk").get(newEvents[0].get("values")));
                System.out.println("fastd " + newEvents[0].get("values").getClass().getField("fastd").get(newEvents[0].get("values")));
            } catch (IllegalAccessException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (NoSuchFieldException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }
}
