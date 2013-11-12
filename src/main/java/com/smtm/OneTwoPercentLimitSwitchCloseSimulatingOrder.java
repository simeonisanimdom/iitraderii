package com.smtm;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;
import com.smtm.esper.Esper;

/**
 * User: <a href="mailto:simeon.petkov@methodia.com">Simeon Petkov</a>
 * Date: 11/10/13
 * Time: 10:06 PM
 * (c) 2012 Methodia Ltd., Sofia, Bulgaria
 */
public class OneTwoPercentLimitSwitchCloseSimulatingOrder implements UpdateListener {

    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(OneTwoPercentLimitSwitchCloseSimulatingOrder.class);

    public enum Direction {
        UP, DOWN;
    }

    private Double targetProfitPercent = 0.001d;
    private Double stopLossPercent = 0.0005d;

    private Double initialPrice;
    private Double targetProfit;
    private Double limitLoss;

    private Direction direction;
    private String[] streams;

    public OneTwoPercentLimitSwitchCloseSimulatingOrder(Direction direction,
                                                        String[] streams) {
        this.direction = direction;
        this.streams = streams;
        for (String stream : streams) {
            Esper.API.addListener(stream, this);
        }
    }

    @Override
    public void update(EventBean[] newEvents, EventBean[] oldEvents) {
        if (newEvents[0].getEventType().getName().contains("SWITCH")) {
            Double currentPrice = (Double) newEvents[0].get("a.price");
            switch (direction) {
                case UP: {
                    if (currentPrice > initialPrice) {
                        LOG.info("SWITCH-CLOSED with PROFIT: " + String.format("%.5f", (currentPrice - initialPrice)));
                        StrategyValue.INSTANCE.put(currentPrice - initialPrice);
                    } else {
                        LOG.info("SWITCH-CLOSED with LOSS: " + String.format("%.5f", (initialPrice - currentPrice)));
                        StrategyValue.INSTANCE.put(-1 * (initialPrice - currentPrice));
                    }
                    break;
                }
                case DOWN: {
                    if (currentPrice < initialPrice) {
                        LOG.info("SWITCH-CLOSED with PROFIT: " + String.format("%.5f", (initialPrice - currentPrice)));
                        StrategyValue.INSTANCE.put(initialPrice - currentPrice);
                    } else {
                        LOG.info("SWITCH-CLOSED with LOSS: " + String.format("%.5f", (currentPrice - initialPrice)));
                        StrategyValue.INSTANCE.put(-1 * (currentPrice - initialPrice));
                    }
                    break;
                }
            }
            detachFromStreams();
        } else {
            if (initialPrice == null) {
                initialPrice = (Double) newEvents[0].get("price");
                switch (direction) {
                    case UP: {
                        targetProfit = initialPrice + initialPrice * targetProfitPercent;
                        limitLoss = initialPrice - initialPrice * stopLossPercent;
                        break;
                    }
                    case DOWN: {
                        targetProfit = initialPrice - initialPrice * targetProfitPercent;
                        limitLoss = initialPrice + initialPrice * stopLossPercent;
                        break;
                    }
                }
            } else {
                Double currentPrice = (Double) newEvents[0].get("price");
                switch (direction) {
                    case UP: {
                        if (currentPrice >= targetProfit) {
                            LOG.info("LIMIT-CLOSED with PROFIT: " + String.format("%.5f", (currentPrice - initialPrice)));
                            StrategyValue.INSTANCE.put(currentPrice- initialPrice);
                            detachFromStreams();
                        } else if (currentPrice <= limitLoss) {
                            LOG.info("LIMIT-CLOSED with LOSS: " + String.format("%.5f", (initialPrice - currentPrice)));
                            StrategyValue.INSTANCE.put(-1 * (initialPrice - currentPrice));
                            detachFromStreams();
                        }
                        break;
                    }
                    case DOWN: {
                        if (currentPrice <= targetProfit) {
                            LOG.info("LIMIT-CLOSED with PROFIT: " + String.format("%.5f", (initialPrice - currentPrice)));
                            StrategyValue.INSTANCE.put(initialPrice - currentPrice);
                            detachFromStreams();
                        } else if (currentPrice >= limitLoss) {
                            LOG.info("LIMIT-CLOSED with LOSS: " + String.format("%.5f", (currentPrice - initialPrice)));
                            StrategyValue.INSTANCE.put(-1 * (currentPrice - initialPrice));
                            detachFromStreams();
                        }
                        break;
                    }
                }
            }
        }
    }

    private void detachFromStreams() {
        for (String stream : streams) {
            Esper.API.detachListener(stream, this);
        }
    }

}
