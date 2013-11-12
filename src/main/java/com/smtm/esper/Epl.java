package com.smtm.esper;

/**
 * User: <a href="mailto:simeon.petkov@methodia.com">Simeon Petkov</a>
 * Date: 11/1/13
 * Time: 12:30 PM
 * (c) 2012 Methodia Ltd., Sofia, Bulgaria
 */
public interface Epl {

    //--- RAW ----------------------------------------------------------------------------------------------------------

    public static final String AVGPRICE_24H_RAW = "insert into <INSTRUMENT-STREAM>_AVGPRICE_24H_RAW " +
            "select timestamp, avg(price) as price " +
            "from Tick(instrument = '<INSTRUMENT-NAME>').win:time(168 hours)";

    public static final String AVGPRICE_3H_RAW = "insert into <INSTRUMENT-STREAM>_AVGPRICE_3H_RAW " +
            "select timestamp, avg(price) as price " +
            "from Tick(instrument = '<INSTRUMENT-NAME>').win:time(24 hours)";

    public static final String AVGPRICE_15MIN_RAW = "insert into <INSTRUMENT-STREAM>_AVGPRICE_15MIN_RAW " +
            "select timestamp, avg(price) as price " +
            "from Tick(instrument = '<INSTRUMENT-NAME>').win:time(3 hours)";

    //--- 15 MIN -------------------------------------------------------------------------------------------------------

    public static final String AVGPRICE_24H_15MIN = "insert into <INSTRUMENT-STREAM>_AVGPRICE_24H_15MIN " +
            "select max(timestamp) as timestamp, avg(price) as price " +
            "from <INSTRUMENT-STREAM>_AVGPRICE_24H_RAW.win:time_batch(15 min)";

    public static final String AVGPRICE_3H_15MIN = "insert into <INSTRUMENT-STREAM>_AVGPRICE_3H_15MIN " +
            "select max(timestamp) as timestamp, avg(price) as price " +
            "from <INSTRUMENT-STREAM>_AVGPRICE_3H_RAW.win:time_batch(15 min)";

    public static final String AVGPRICE_15MIN_15MIN = "insert into <INSTRUMENT-STREAM>_AVGPRICE_15MIN_15MIN " +
            "select max(timestamp) as timestamp, avg(price) as price " +
            "from <INSTRUMENT-STREAM>_AVGPRICE_15MIN_RAW.win:time_batch(15 min)";

    // PATTERNS --------------------------------------------------------------------------------------------------------

    public static final String TRIPLE_AVG_DOWN = "insert into <INSTRUMENT-STREAM>_TRIPLE_AVG_DOWN " +
            "select a.timestamp as timestamp, a.price as price " +
            "from <INSTRUMENT-STREAM>_AVGPRICE_15MIN_15MIN.std:lastevent() a, <INSTRUMENT-STREAM>_AVGPRICE_3H_15MIN.std:lastevent() b, <INSTRUMENT-STREAM>_AVGPRICE_24H_15MIN.std:lastevent() c " +
            "where a.timestamp=b.timestamp and b.timestamp=c.timestamp and a.price < b.price and b.price < c.price";
    public static final String TRIPLE_AVG_UP = "insert into <INSTRUMENT-STREAM>_TRIPLE_AVG_UP " +
            "select a.timestamp as timestamp, a.price as price " +
            "from <INSTRUMENT-STREAM>_AVGPRICE_15MIN_15MIN.std:lastevent() a, <INSTRUMENT-STREAM>_AVGPRICE_3H_15MIN.std:lastevent() b, <INSTRUMENT-STREAM>_AVGPRICE_24H_15MIN.std:lastevent() c " +
            "where a.timestamp=b.timestamp and b.timestamp=c.timestamp and a.price > b.price and b.price > c.price";

    //--- EVENTS -------------------------------------------------------------------------------------------------------

    public static final String TRIPLE_AVG_DOWN_SWITCH = "insert into <INSTRUMENT-STREAM>_TRIPLE_AVG_DOWN_SWITCH " +
            "select a.timestamp, a.price " +
            "from pattern [every (a=<INSTRUMENT-STREAM>_TRIPLE_AVG_UP -> b=<INSTRUMENT-STREAM>_TRIPLE_AVG_DOWN)]";

    public static final String TRIPLE_AVG_UP_SWITCH = "insert into <INSTRUMENT-STREAM>_TRIPLE_AVG_UP_SWITCH " +
            "select a.timestamp, a.price " +
            "from pattern [every (a=<INSTRUMENT-STREAM>_TRIPLE_AVG_DOWN -> b=<INSTRUMENT-STREAM>_TRIPLE_AVG_UP)]";

}
