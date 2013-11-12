package com.smtm.esper.chart;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;
import com.smtm.visual.ChartModelUpdateListener;
import com.smtm.visual.ChartModelUpdateProvider;
import org.primefaces.model.chart.CartesianChartModel;
import org.primefaces.model.chart.ChartSeries;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * User: <a href="mailto:simeon.petkov@methodia.com">Simeon Petkov</a>
 * Date: 11/1/13
 * Time: 2:40 PM
 * (c) 2012 Methodia Ltd., Sofia, Bulgaria
 */
public class TripleMALookBackChartListener implements UpdateListener, ChartModelUpdateProvider {

    private static final DateFormat dateFormat = new SimpleDateFormat("dd.MM HH:mm");

    private ChartModelUpdateListener chartModelUpdateListener;

    private int hours;

    private List<EventBean> eventCache24h = new LinkedList<EventBean>();
    private List<EventBean> eventCache3h = new LinkedList<EventBean>();
    private List<EventBean> eventCache15min = new LinkedList<EventBean>();

    private boolean update24h = false;
    private boolean update3h = false;
    private boolean update15min = false;

    public TripleMALookBackChartListener(int hours) {
        this.hours = hours;
    }

    @Override
    public void update(EventBean[] newEvents, EventBean[] oldEvents) {
        if (newEvents[0].getEventType().getName().endsWith("AVGPRICE_24H_15MIN")) {
            if (eventCache24h.size() >= 96) {
                eventCache24h.remove(0);
            }
            eventCache24h.add(newEvents[0]);
            update24h = true;
        }
        if (newEvents[0].getEventType().getName().endsWith("AVGPRICE_3H_15MIN")) {
            if (eventCache3h.size() >= 96) {
                eventCache3h.remove(0);
            }
            eventCache3h.add(newEvents[0]);
            update3h = true;
        }
        if (newEvents[0].getEventType().getName().endsWith("AVGPRICE_15MIN_15MIN")) {
            if (eventCache15min.size() >= 96) {
                eventCache15min.remove(0);
            }
            eventCache15min.add(newEvents[0]);
            update15min = true;
        }
        if (update15min && update3h && update24h) {
            updateChartModel();
            update15min = false;
            update3h = false;
            update24h = false;
        }
    }

    private void updateChartModel() {
        ChartSeries series24h = new ChartSeries("24H");
        for (EventBean event : eventCache24h) {
            series24h.set(dateFormat.format((Date) event.get("timestamp")), (Double) event.get("price"));
        }

        ChartSeries series3h = new ChartSeries("3H");
        for (EventBean event : eventCache3h) {
            series3h.set(dateFormat.format((Date) event.get("timestamp")), (Double) event.get("price"));
        }

        ChartSeries series15min = new ChartSeries("15min");
        for (EventBean event : eventCache15min) {
            series15min.set(dateFormat.format((Date) event.get("timestamp")), (Double) event.get("price"));
        }

        CartesianChartModel model = new CartesianChartModel();
        model.addSeries(series15min);
        model.addSeries(series3h);
        model.addSeries(series24h);

        if (chartModelUpdateListener != null) {
            chartModelUpdateListener.updateChartModel(model);
        }
    }

    public int getHours() {
        return hours;
    }

    public void setHours(int hours) {
        this.hours = hours;
    }

    @Override
    public void addChartModelUpdateListener(ChartModelUpdateListener listener) {
        chartModelUpdateListener = listener;
    }
}
