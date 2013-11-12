package com.smtm.lmax;

import com.lmax.api.*;
import com.lmax.api.account.LoginCallback;
import com.lmax.api.account.LoginRequest;
import com.lmax.api.heartbeat.HeartbeatCallback;
import com.lmax.api.heartbeat.HeartbeatEventListener;
import com.lmax.api.heartbeat.HeartbeatRequest;
import com.lmax.api.heartbeat.HeartbeatSubscriptionRequest;
import com.lmax.api.marketdata.AggregateHistoricMarketDataRequest;
import com.lmax.api.marketdata.HistoricMarketDataRequest;
import com.lmax.api.marketdata.HistoricMarketDataSubscriptionRequest;
import com.lmax.api.order.*;
import com.lmax.api.orderbook.*;
import com.smtm.CloseOnEventMarketOrder;
import com.smtm.Tick;
import com.smtm.TickListener;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;
import java.util.zip.GZIPInputStream;

/**
 * User: <a href="mailto:simeon.petkov@gmail.com">Simeon Petkov</a>
 * Date: 10/31/13
 * Time: 1:56 PM
 */
public enum Lmax implements LoginCallback,
        HistoricMarketDataEventListener,
        OrderBookEventListener,
        ExecutionEventListener,
        HeartbeatEventListener,
        OrderEventListener,
        SessionDisconnectedListener {

    API;

    /*--- Configurations ---*/
    private int minLookBackHours = Config.<Integer>get(Config.Key.LMAX_LOOKBACK_HOURS);
    private boolean lookBackOnly = false;

    public enum ProcessingState {
        IDLE, PROCESSING_HISTORIC, FINISHED_PROCESSING_HISTORIC, PROCESSING_CACHE, PROCESSING_LIVE, DISCONNECTED;
    }

    private Date lastHeartBeat = null;

    private ProcessingState processingState = ProcessingState.IDLE;

    private List<Tick> liveDataCache = new ArrayList<Tick>();

    private List<TickListener> tickListeners = new ArrayList<TickListener>();

    private Session session;

    private static final Logger LOG = Logger.getLogger(Lmax.class);

    private Map<String, Instrument> instrumentsByName;
    private Map<Long, Instrument> instrumentsById;

    private Map<String, Order> workingOrders = new HashMap<String, Order>();

    private Map<String, Double> lastTradedPrice = new HashMap<String, Double>();

    private Map<Long, Boolean> historyProcessing = new HashMap<Long, Boolean>();

    public void init() {
        session.registerExecutionEventListener(this);
        session.registerOrderEventListener(this);
        session.subscribe(new HeartbeatSubscriptionRequest(), new Callback() {
            public void onSuccess() {
                LOG.info("Subscribed for LMAX heartbeats");
            }

            @Override
            public void onFailure(final FailureResponse failureResponse) {
                LOG.error("Subscribing to LMAX heartbeats failed: " + failureResponse.getMessage());
            }
        });
        session.subscribe(new ExecutionSubscriptionRequest(), new Callback() {
            @Override
            public void onSuccess() {
                LOG.info("Subscribed for LMAX executions");
            }

            @Override
            public void onFailure(FailureResponse failureResponse) {
                LOG.error("Subscribing to LMAX executions failed: " + failureResponse.getMessage());
            }
        });
        initInstruments(new SearchInstrumentRequest("CURRENCY", 0), 0);
    }

    public void subscribe() {
        if (minLookBackHours != 0 && !getProcessingState().equals(ProcessingState.DISCONNECTED)) {
            setProcessingState(ProcessingState.PROCESSING_HISTORIC);
        }
        if (!lookBackOnly) {
            subscribeToLiveData();
        }
        if (minLookBackHours != 0 && !getProcessingState().equals(ProcessingState.DISCONNECTED)) {
            subscribeToHistoricData();
        }
    }

    public void start() {
        session.start();
    }

    private void requestHeartbeat() {
        session.requestHeartbeat(new HeartbeatRequest(nextInstructionId()),
                new HeartbeatCallback() {
                    @Override
                    public void onSuccess(String token) {
                        LOG.info("Requested heartbeat (" + token + ")");
                    }

                    @Override
                    public void onFailure(FailureResponse failureResponse) {
                        LOG.error("Requesting heartbeat failed: " + failureResponse.getMessage());
                    }
                });
    }

    public void unsubscribe() {
        session.stop();
        setProcessingState(ProcessingState.IDLE);
    }

    private void initInstruments(final SearchInstrumentRequest searchInstrumentRequest, final long offsetInstrumentId) {
        if(getProcessingState().equals(ProcessingState.DISCONNECTED)) {
            instrumentsById = new HashMap<Long, Instrument>();
            instrumentsByName = new HashMap<String, Instrument>();
        }
        session.searchInstruments(searchInstrumentRequest, new SearchInstrumentCallback() {
            @Override
            public void onSuccess(List<Instrument> instruments, boolean hasMoreResults) {
                addInstruments(instruments);
                if (hasMoreResults) {
                    initInstruments(new SearchInstrumentRequest("CURRENCY", offsetInstrumentId + 25), offsetInstrumentId + 25);
                }
                LOG.info("Loaded " + instruments.size() + " instruments");
            }

            @Override
            public void onFailure(FailureResponse failureResponse) {
                LOG.error("Loading instruments failed with " + failureResponse.getMessage());
            }
        });
        initHistoryProcessing();
    }

    private void initHistoryProcessing() {
        for (String instrumentName : Config.<String[]>get(Config.Key.LMAX_INSTRUMENTS)) {
            historyProcessing.put(instrumentsByName.get(instrumentName).getId(), Boolean.FALSE);
        }
    }

    private void addInstruments(List<Instrument> instruments) {
        if (instrumentsByName == null) {
            instrumentsByName = new HashMap<String, Instrument>();
        }
        for (Instrument instrument : instruments) {
            instrumentsByName.put(instrument.getName(), instrument);
        }
        if (instrumentsById == null) {
            instrumentsById = new HashMap<Long, Instrument>();
        }
        for (Instrument instrument : instruments) {
            instrumentsById.put(instrument.getId(), instrument);
        }
    }

    private void subscribeToLiveData() {
        session.registerOrderBookEventListener(this);
        for (final String instrument : Config.<String[]>get(Config.Key.LMAX_INSTRUMENTS)) {
            session.subscribe(new OrderBookSubscriptionRequest(instrumentsByName.get(instrument).getId()), new Callback() {
                public void onSuccess() {
                    LOG.info("Subscribed for market events for instrument " + instrument);
                }

                public void onFailure(FailureResponse failureResponse) {
                    LOG.error("Failed to subscribe: " + failureResponse.getMessage());
                }
            });
        }
    }

    @Override
    public void notify(OrderBookEvent orderBookEvent) {
        keepAlive();
        if (processingState.equals(ProcessingState.PROCESSING_HISTORIC)) {
            liveDataCache.add(fromOrderBookEvent(orderBookEvent));
        } else if (processingState.equals(ProcessingState.FINISHED_PROCESSING_HISTORIC)) {
            setProcessingState(ProcessingState.PROCESSING_CACHE);
            for (Tick tick : liveDataCache) {
                tick(tick);
            }
            setProcessingState(ProcessingState.PROCESSING_LIVE);
        } else if (processingState.equals(ProcessingState.PROCESSING_LIVE)) {
            tick(fromOrderBookEvent(orderBookEvent));
        } else if (processingState.equals(ProcessingState.DISCONNECTED)) {
            setProcessingState(ProcessingState.PROCESSING_LIVE);
            tick(fromOrderBookEvent(orderBookEvent));
        } else {
            //should be just a few - no effect on overall correctness
            LOG.warn("1 order book event for instrument " + orderBookEvent.getInstrumentId() + " ignored");
        }
    }

    private void keepAlive() {
        Date now = new Date();
        if (lastHeartBeat == null) {
            lastHeartBeat = now;
            requestHeartbeat();
        } else if (now.getTime() > (lastHeartBeat.getTime() + Config.<Long>get(Config.Key.LMAX_HEARTBEAT_MILLIS))) {
            lastHeartBeat = now;
            requestHeartbeat();
        }
    }

    private Tick fromOrderBookEvent(OrderBookEvent orderBookEvent) {
        Tick tick = new Tick();
        tick.setPrice((FixedPointNumbers.doubleValue(orderBookEvent.getValuationAskPrice()) + FixedPointNumbers.doubleValue(orderBookEvent.getValuationBidPrice())) / 2);
        tick.setTimestamp(new Date(orderBookEvent.getTimeStamp()));
        tick.setVolume(orderBookEvent.getAskPrices().size() + orderBookEvent.getBidPrices().size());
        tick.setInstrument(instrumentsById.get(orderBookEvent.getInstrumentId()).getName());
        return tick;
    }

    private void subscribeToHistoricData() {

        session.registerHistoricMarketDataEventListener(this);

        Calendar calendar = Calendar.getInstance();
        final Date toDate = calendar.getTime();
        calendar.add(Calendar.HOUR_OF_DAY, -minLookBackHours);
        final Date fromDate = calendar.getTime();

        for (final String instrument : Config.<String[]>get(Config.Key.LMAX_INSTRUMENTS)) {
            session.subscribe(new HistoricMarketDataSubscriptionRequest(), new Callback() {
                public void onSuccess() {
                    final HistoricMarketDataRequest request = new AggregateHistoricMarketDataRequest(nextInstructionId(),
                            instrumentsByName.get(instrument).getId(),
                            fromDate,
                            toDate,
                            HistoricMarketDataRequest.Format.CSV,
                            HistoricMarketDataRequest.Resolution.MINUTE,
                            AggregateHistoricMarketDataRequest.Option.ASK);

                    session.requestHistoricMarketData(request, new Callback() {
                        public void onSuccess() {
                            LOG.info("Historic data for instrument " + instrument + " for period " + fromDate + " - " + toDate + " ready for processing");
                        }

                        public void onFailure(final FailureResponse failureResponse) {
                            LOG.error("Failed to request historic market data: " + failureResponse.getMessage());
                        }
                    });
                }

                public void onFailure(final FailureResponse failureResponse) {
                    LOG.error("Failed to subscribe: " + failureResponse);
                }
            });
        }
    }

    @Override
    public void notify(HistoricMarketDataEvent historicMarketDataEvent) {
        long instrumentIdCopy = 0;
        for (final URL url : historicMarketDataEvent.getUrls()) {
            final long instrumentId = Long.parseLong(url.toString().split("/")[5]);
            instrumentIdCopy = instrumentId;
            session.openUrl(url, new UrlCallback() {
                public void onSuccess(final URL url, final InputStream inputStream) {
                    processHistoricFile(inputStream, instrumentId);
                }

                public void onFailure(final FailureResponse failureResponse) {
                    LOG.error("Failed to open url: " + failureResponse.getMessage());
                }
            });
        }
        LOG.info("Processed historic data for instrument " + instrumentIdCopy);
        handleHistoricProcessing(instrumentIdCopy);
    }

    private void handleHistoricProcessing(long instrumentId) {
        historyProcessing.put(instrumentId, Boolean.TRUE);
        if (!historyProcessing.values().contains(Boolean.FALSE)) {
            setProcessingState(ProcessingState.FINISHED_PROCESSING_HISTORIC);
            if (isLookBackOnly()) {
                unsubscribe();
            }
        }
    }

    public void login() {
        LmaxApi lmaxApi = new LmaxApi(Config.<String>get(Config.Key.LMAX_URL));
        lmaxApi.login(new LoginRequest(Config.<String>get(Config.Key.LMAX_USER),
                Config.<String>get(Config.Key.LMAX_PASS),
                Config.<LoginRequest.ProductType>get(Config.Key.LMAX_PRODUCT)),
                this);
        LOG.info("Logged in to LMAX " + Config.get(Config.Key.LMAX_PRODUCT) + " as " + Config.Key.LMAX_USER);
    }

    public void addTickListener(TickListener tickListener) {
        if (tickListeners == null) {
            tickListeners = new ArrayList<TickListener>();
        }
        tickListeners.add(tickListener);
    }

    public void removeTickListener(TickListener tickListener) {
        for (TickListener registeredListener : tickListeners) {
            if (registeredListener.equals(tickListener)) {
                tickListeners.remove(registeredListener);
            }
        }
    }

    private void tick(Tick tick) {
//        LOG.debug(tick.getTimestamp() + "\t" + tick.getInstrument() + "\t" + tick.getPrice() + "\t" + tick.getVolume());
        lastTradedPrice.put(tick.getInstrument(), tick.getPrice());
        for (TickListener tickListener : tickListeners) {
            tickListener.tick(tick);
        }
    }

    /*--- LMAX Callbacks ---*/

    @Override
    public void onLoginSuccess(Session session) {
        this.session = session;
        session.registerHeartbeatListener(this);
    }

    @Override
    public void onLoginFailure(FailureResponse failureResponse) {
        LOG.error("Login failure: " + failureResponse.getMessage());
    }

    private void processHistoricFile(InputStream compressedInputStream, long instrumentId) {
        try {
            final GZIPInputStream decompressedInputStream = new GZIPInputStream(compressedInputStream);
            final InputStreamReader streamReader = new InputStreamReader(decompressedInputStream);
            final BufferedReader lineReader = new BufferedReader(streamReader);

            String line;
            while ((line = lineReader.readLine()) != null) {
                if (!(line.length() == 0) && !line.startsWith("INTERVAL_START_TIMESTAMP")) {
                    String[] tickProperties = line.split(",");
                    if (tickProperties.length == 11 && !tickProperties[0].isEmpty()
                            && !tickProperties[2].isEmpty()
                            && !tickProperties[3].isEmpty()
                            && !tickProperties[5].isEmpty()
                            && !tickProperties[6].isEmpty()
                            && !tickProperties[7].isEmpty()) {
                        Tick tick = new Tick();
                        tick.setTimestamp(new Date(Long.parseLong(tickProperties[0])));
                        tick.setInstrument(instrumentsById.get(instrumentId).getName());
                        tick.setPrice((Double.parseDouble(tickProperties[2]) + Double.parseDouble(tickProperties[3])) / 2);
                        tick.setVolume(Double.parseDouble(tickProperties[5]) + Double.parseDouble(tickProperties[6]) + Double.parseDouble(tickProperties[7]));
                        tick(tick);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to process historic file", e);
        }

    }

    public String nextInstructionId() {
        return String.valueOf(new Random().nextInt(100000));
    }

    public void placeOrder(String instrument, FixedPointNumber quantity, CloseOnEventMarketOrder closeOnEventMarketOrder) {
        final String instructionId = nextInstructionId();
        MarketOrderSpecification marketOrderSpecification = new MarketOrderSpecification(instrumentsByName.get(instrument).getId(),
                instructionId,
                quantity,
                TimeInForce.IMMEDIATE_OR_CANCEL,
                FixedPointNumbers.toFixedPointNumber(getLimit(instrument, Config.Key.LMAX_STOP_LOSS_OFFSET_PERCENT),
                        instrumentsByName.get(instrument).getOrderBook().getPriceIncrement()),
                FixedPointNumbers.toFixedPointNumber(getLimit(instrument, Config.Key.LMAX_PROFIT_TARGET_OFFSET_PERCENT),
                        instrumentsByName.get(instrument).getOrderBook().getPriceIncrement()));
        if (closeOnEventMarketOrder != null) {
            closeOnEventMarketOrder.setId(instructionId);
            closeOnEventMarketOrder.setMarketOrderSpecification(marketOrderSpecification);
        }
        session.placeMarketOrder(marketOrderSpecification, new OrderCallback() {
            @Override
            public void onSuccess(String s) {
                LOG.info("Placing market order " + instructionId + " (" + s + ")");
            }

            @Override
            public void onFailure(FailureResponse failureResponse) {
                LOG.error("Failed placing market order + " + instructionId + ": " + failureResponse.getMessage());
            }
        });
    }

    private double getLimit(String instrument, Config.Key key) {
        return lastTradedPrice.get(instrument) / 100 * Config.<Integer>get(key);
    }

    @Override
    public void notify(Execution execution) {
        if (execution.getOrder().getOrderType().equals(OrderType.MARKET)) {
            workingOrders.put(execution.getOrder().getInstructionId(), execution.getOrder());
            LOG.info("Market order " + execution.getOrder().getInstructionId() + " placed");
        } else if (execution.getOrder().getOrderType().equals(OrderType.CLOSE_OUT_ORDER_POSITION)) {
            workingOrders.remove(execution.getOrder().getOriginalInstructionId());
            LOG.info("Market order " + execution.getOrder().getOriginalInstructionId() + " closed");
        } else {
            //TODO - close notifications don't arrive as expected, handle close events with order handling (below)
            LOG.warn("Received unknown order execution notification");
        }
    }

    @Override
    public void notify(Order order) {
        //TODO - handle order and execution events as in tutorial
    }


    public void closeOrder(CloseOnEventMarketOrder closeOnEventMarketOrder) {
        final Order orderToClose = workingOrders.get(closeOnEventMarketOrder.getId());
        String instructionId = nextInstructionId();
        ClosingOrderSpecification closingOrderSpecification = new ClosingOrderSpecification(instructionId,
                orderToClose.getInstrumentId(),
                orderToClose.getInstructionId(),
                orderToClose.getQuantity().negate());
        session.placeClosingOrder(closingOrderSpecification, new OrderCallback() {
            @Override
            public void onSuccess(String instructionId) {
                LOG.info("Closing order " + orderToClose.getInstructionId() + " (" + instructionId + ")");
            }

            @Override
            public void onFailure(FailureResponse failureResponse) {
                LOG.error("Failed closing order " + orderToClose.getInstructionId() + ": " + failureResponse.getMessage());
            }
        });
    }

    @Override
    public void notify(long l, String token) {
        LOG.info("Received heartbeat (" + token + ")");
    }

    @Override
    public void notifySessionDisconnected() {
        setProcessingState(ProcessingState.DISCONNECTED);
        login();
        init();
        subscribe();
        start();
    }

    /*--- Configuration Getters & Setters ---*/

    public int geMinLookBackDays() {
        return minLookBackHours;
    }

    public void setMinLookBackDays(int minLookBackHours) {
        this.minLookBackHours = minLookBackHours;
    }

    public boolean isLookBackOnly() {
        return lookBackOnly;
    }

    public void setLookBackOnly(boolean lookBackOnly) {
        this.lookBackOnly = lookBackOnly;
    }

    public ProcessingState getProcessingState() {
        return processingState;
    }

    public void setProcessingState(ProcessingState processingState) {
        LOG.info("Processing state changed to " + processingState.toString());
        this.processingState = processingState;
    }

}
