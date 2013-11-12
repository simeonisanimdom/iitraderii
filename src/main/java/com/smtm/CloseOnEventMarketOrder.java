package com.smtm;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;
import com.lmax.api.order.MarketOrderSpecification;
import com.smtm.esper.Esper;
import com.smtm.lmax.Lmax;

/**
 * User: <a href="mailto:simeon.petkov@methodia.com">Simeon Petkov</a>
 * Date: 11/3/13
 * Time: 2:06 AM
 * (c) 2012 Methodia Ltd., Sofia, Bulgaria
 */
public class CloseOnEventMarketOrder implements UpdateListener {

    private String id;
    private MarketOrderSpecification marketOrderSpecification;

    public CloseOnEventMarketOrder() {
    }

    @Override
    public void update(EventBean[] newEvents, EventBean[] oldEvents) {
        Esper.API.detachListener(newEvents[0].getEventType().getName(), this);
        Lmax.API.closeOrder(this);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public MarketOrderSpecification getMarketOrderSpecification() {
        return marketOrderSpecification;
    }

    public void setMarketOrderSpecification(MarketOrderSpecification marketOrderSpecification) {
        this.marketOrderSpecification = marketOrderSpecification;
    }
}
