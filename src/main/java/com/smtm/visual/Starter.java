package com.smtm.visual;

import com.smtm.esper.Esper;
import com.smtm.esper.chart.TripleMALookBackChartListener;
import com.smtm.lmax.Config;
import com.smtm.lmax.Lmax;
import com.smtm.lmax.LmaxStarter;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import java.io.Serializable;

/**
 * User: <a href="mailto:simeon.petkov@methodia.com">Simeon Petkov</a>
 * Date: 11/2/13
 * Time: 2:38 PM
 * (c) 2012 Methodia Ltd., Sofia, Bulgaria
 */
@ManagedBean(name = "starterBean")
@ApplicationScoped
public class Starter implements Serializable {

    public Starter() {
    }

    public void start(MAChartBean maChartBean) {

        Esper.API.init();

        TripleMALookBackChartListener tripleMALookBackChartListener = new TripleMALookBackChartListener(Config.<Integer>get(Config.Key.LMAX_LOOKBACK_HOURS));
        tripleMALookBackChartListener.addChartModelUpdateListener(maChartBean);

        Esper.API.addListener("EURUSD_AVGPRICE_24H_15MIN", tripleMALookBackChartListener);
        Esper.API.addListener("EURUSD_AVGPRICE_3H_15MIN", tripleMALookBackChartListener);
        Esper.API.addListener("EURUSD_AVGPRICE_15MIN_15MIN", tripleMALookBackChartListener);

        Lmax.API.addTickListener(Esper.API);

        LmaxStarter lmaxStarter = new LmaxStarter();
        lmaxStarter.start();
    }

}
