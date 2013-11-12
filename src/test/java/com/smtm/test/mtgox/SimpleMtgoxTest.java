package com.smtm.test.mtgox;

import com.xeiam.xchange.Exchange;
import com.xeiam.xchange.ExchangeFactory;
import com.xeiam.xchange.ExchangeSpecification;
import com.xeiam.xchange.currency.Currencies;
import com.xeiam.xchange.dto.account.AccountInfo;
import com.xeiam.xchange.dto.marketdata.Ticker;
import com.xeiam.xchange.mtgox.v2.MtGoxExchange;
import com.xeiam.xchange.service.polling.PollingAccountService;
import com.xeiam.xchange.service.polling.PollingMarketDataService;
import org.junit.Test;

import java.io.IOException;

/**
 * User: <a href="mailto:simeon.petkov@methodia.com">Simeon Petkov</a>
 * Date: 11/10/13
 * Time: 8:09 PM
 * (c) 2012 Methodia Ltd., Sofia, Bulgaria
 */
public class SimpleMtgoxTest {

    @Test
    public void getMarketData() throws IOException {
        Exchange mtGoxExchange = ExchangeFactory.INSTANCE.createExchange(MtGoxExchange.class.getName());
        PollingMarketDataService marketDataService = mtGoxExchange.getPollingMarketDataService();
        Ticker ticker = marketDataService.getTicker(Currencies.BTC, Currencies.EUR);
        String btcEur = ticker.getLast().toString();
        System.out.println("Current exchange rate for BTC / EUR: " + btcEur);
    }

    @Test
    public void getAccountInfo() throws IOException {
        Exchange mtGoxExchange = createExchange();
        PollingAccountService accountService = mtGoxExchange.getPollingAccountService();
        AccountInfo accountInfo = accountService.getAccountInfo();
        System.out.println("AccountInfo as String: " + accountInfo.toString());
    }

    private Exchange createExchange() {
        ExchangeSpecification exSpec = new ExchangeSpecification(MtGoxExchange.class);
        exSpec.setSecretKey("WUvlaWKg1/R/ScCfC5HhEtmFqA9mDsfjBWiQn0bT55HEF4+Sb11A9GBRpn0yDwfY2Gv9twYCu8P4wTtOuS1M5A==");
        exSpec.setApiKey("89dd66d2-4a64-4a35-89e9-565ac4b3a78a");
        exSpec.setSslUri("https://data.mtgox.com");
        exSpec.setPlainTextUriStreaming("ws://websocket.mtgox.com");
        exSpec.setSslUriStreaming("wss://websocket.mtgox.com");
        return ExchangeFactory.INSTANCE.createExchange(exSpec);
    }

}
