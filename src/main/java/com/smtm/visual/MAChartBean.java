package com.smtm.visual;

import org.primefaces.model.chart.CartesianChartModel;

import javax.annotation.PostConstruct;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import java.io.Serializable;

/**
 * User: <a href="mailto:simeon.petkov@methodia.com">Simeon Petkov</a>
 * Date: 11/2/13
 * Time: 2:41 PM
 * (c) 2012 Methodia Ltd., Sofia, Bulgaria
 */
@ManagedBean(name = "maChartBean")
@ApplicationScoped
public class MAChartBean implements ChartModelUpdateListener, Serializable {

    @ManagedProperty(value = "#{starterBean}")
    private Starter starter;

    private CartesianChartModel model = new CartesianChartModel();

    public MAChartBean() {
    }

    @PostConstruct
    public void start() {
        starter.start(this);
    }

    @Override
    public void updateChartModel(CartesianChartModel model) {
        if (model != null
                && model.getSeries() != null
                && model.getSeries().size() > 0
                && model.getSeries().get(0).getData() != null
                && model.getSeries().get(0).getData().size() > 0) {
            setModel(model);
        }
    }

    public CartesianChartModel getModel() {
        return model;
    }

    public void setModel(CartesianChartModel model) {
        this.model = model;
    }

    public Starter getStarter() {
        return starter;
    }

    public void setStarter(Starter starter) {
        this.starter = starter;
    }
}
