package com.neueda.trade.injector;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.UncategorizedJmsException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;

import com.neueda.trade.server.database.StockDto;
import com.neueda.trade.server.model.BuySell;
import com.neueda.trade.server.model.DtoFactory;
import com.neueda.trade.server.model.Stock;
import com.neueda.trade.server.model.Trade;
import com.neueda.trade.server.model.TradeState;
import com.neueda.trade.server.rules.Model;

/**
 * Upstraem application to inject trades into the system
 * 
 * @author Neueda
 *
 */
@Component
public class InjectTrades  {

	private static final Logger logger = LoggerFactory.getLogger(InjectTrades.class);
	
	private static final String[] stocks = {
			"FTSE.AA", "FTSE.BARC", "FTSE.LLOY", "FTSE.VM",
			"NYSE.C", "NYSE.BAC", "NYSE.JPM", "NYSE.ORCL",
			"NASDAQ.AAPL", "NASDAQ.GOOG", "NASDAQ.IBM", "NASDAQ.TEAM"
		};

	private static final Stock badStock = new StockDto("BAD.STCK");

	private static final double GBP = 1.3030;
	private static final double USD = 1.0000;
	
	private static final double[] prices = {
		246.0*GBP, 213.0*GBP,  72.0*GBP, 310.0*GBP,
		 61.0*USD,  23.0*USD,  85.0*USD,  44.0*USD,
		153.0*USD, 934.0*USD, 152.0*USD,  35.0*USD
	};
	
	@SuppressWarnings("serial")
	private static final Map<TradeState, TradeState> lifcycle = new HashMap<TradeState, TradeState>() {{
		put(TradeState.Place, TradeState.Accept);
		put(TradeState.Modify, TradeState.Accept);
		put(TradeState.Accept, TradeState.Execute);
		put(TradeState.Execute, TradeState.Settle);
	}};
	
	private static final List<TradeState> endStates = Arrays.asList(
		TradeState.Cancel, TradeState.Deny, TradeState.Reject, TradeState.Settle
	); 

	/**
	 * Variance as a fraction above/below sleep delay
	 */
	private static final double DELAY_VAR = 0.5;	
	/**
	 * sets sequence counter limit - effective maximum number of trades at the same time (ms)
	 */
	private static final int SEQ_LIMIT = 100;
	
	/**
	 * fraction the trade price can vary around its mean
	 */
	private static final double PRICE_VAR = 0.15;
	/*
	 * Scaling factor for volume range
	 */
	private static final int VOLUME_SCALE = 1000;
	/**
	 * min and max volumes per trade
	 */
	private static final int[] VOLUME_RANGE = {1, 101};
	
	/**
	 * number of trades to generate
	 */
	@Autowired
	@Value(value="${trade.injector.limit:10}")
	private int injectorLimit;

	/**
	 * number per minute
	 */
	@Autowired
	@Value("${trade.injector.freq:3600}")
	private int injectorFreq;

	/**
	 * fraction of trades that may cause an error
	 */
	@Autowired
	@Value("${trade.injector.error.rate:0}")
	private double injectorErrorRate;

	/**
	 * allow creation of place trades
	 */
	@Autowired
	@Value("${trade.injector.error.place:false}")
	private boolean injectorErrorPlace;

	@Autowired
	private InjectorClient client;
	
    @Autowired
    private DtoFactory factory;

	private SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmssSSS");
	private ThreadLocalRandom random = ThreadLocalRandom.current();
		
	private Date lastDate = new Date();
	private int seqNumber;
	
	private enum Action {
		Place, Update, Both;
	}
	
	private Action randomAction() {
		int scale = random.nextInt(100);
		if (scale > 80) {
			return Action.Both;
		}
		if (scale > 10) {
			return Action.Update;
		}
		return Action.Place;
	}
	
	private boolean injectFault() {
		return injectorErrorRate > 0 ? random.nextDouble(100.0)<injectorErrorRate: false;
	}
	
	private String uniqueTransid() {
		Date nextDate = new Date();	
		if (nextDate.getTime() == lastDate.getTime()) {
			++seqNumber;
			if (seqNumber >= SEQ_LIMIT) {
				seqNumber = 0;
			}
		}
		lastDate = nextDate;
		return String.format("%s-%02d", df.format(nextDate), seqNumber);
	}
	
	private <T> T pick(T[] choice) {
		return choice[random.nextInt(choice.length)];
	}

	private <T> T pick(List<T> choice) {
		return choice.get(random.nextInt(choice.size()));
	}
	
	private <T> int pickIndex(T[] choice) {
		return random.nextInt(choice.length);
	}
	
	private Trade createTrade() {
		String transid = uniqueTransid();
		int stockIndex = pickIndex(stocks);
		double price = prices[stockIndex] + prices[stockIndex]*random.nextDouble(-PRICE_VAR, PRICE_VAR);
		int volume =  random.nextInt(VOLUME_RANGE[0], VOLUME_RANGE[1]) * VOLUME_SCALE;
		return factory.createTrade(transid, factory.createStock(stocks[stockIndex]), new Date(), price, volume, pick(BuySell.values()));
	}
	
	private Trade amendTrade(Trade trade) {
		if (injectFault()) {
			trade.setPrice(-trade.getPrice());
		}
		if (injectFault()) {
			trade.setVolume(-trade.getVolume());
		}
		if (injectFault()) {
			trade.setStock(badStock);
		}
		return trade;
	}

	private void place(Trade trade) {
		try {
			client.place(trade);
			backlog.add(trade);
		}
		catch (InjectException ex) {
			logger.error(ex.getMessage());
		}
	}

    private static boolean stateInList(TradeState state, TradeState[] allowed) {
        return allowed==null ? false : Arrays.stream(allowed).parallel().anyMatch(state::equals);
    }

	List<Trade> backlog = new ArrayList<>();
	
	private boolean processBacklog() {
		try {
			if (backlog.size() == 0) {
				return false;
			}
			Trade trade = pick(backlog);
			TradeState newState = lifcycle.get(trade.getState());
			if (injectFault()) {
					newState = pick(TradeState.values());
			}
			if (newState == TradeState.Modify) {
				trade.setVolume(trade.getVolume()-VOLUME_SCALE);
				client.modify(trade.getTransid(), trade.getPrice(), trade.getVolume());
			}
			else {
				client.updateState(trade.getTransid(), newState);
			}
			if (stateInList(trade.getState(), Model.allowedStates.get(newState))) {
				trade.setState(newState);
				if (endStates.contains(newState)) {
					backlog.remove(trade);
				}
			}
			return true;
		}
		catch (InjectException ex) {
			logger.error(ex.getMessage());
		}
		return false;
	}
	
	private void doInject() throws InterruptedException {
		double delay = injectorFreq>0 ? 3600.0/injectorFreq : 0;
		int tradeCount = 0;
		while (injectorLimit == 0 || tradeCount < injectorLimit || backlog.size() > 0) {
			if (injectorLimit == 0 || tradeCount < injectorLimit) {
				Action action = randomAction();
				if (action == Action.Both || action == Action.Place) {
					Trade trade = createTrade();
					if (injectFault()) {
						trade = amendTrade(trade);
					}
					place(trade);
					++tradeCount;
				}
				if (action == Action.Both || action == Action.Update) {
					processBacklog();
				}
			}
			else if (backlog.size() > 0) {
				processBacklog();
			}
			if (delay > 0) {
				Thread.sleep((long)(delay*1000*random.nextDouble(1-DELAY_VAR, 1+DELAY_VAR)));		
			}
		}
		if (backlog.size() > 0) {
			logger.error("Injector ended with backlog length of {}", backlog.size());
		}
	}
	
	public void run() throws InterruptedException {
		try {
			doInject();
		}
		catch (UncategorizedJmsException ex) {
			logger.error("ActiveMQ server not running");
		}
		catch (ResourceAccessException ex) {
			logger.error("Trade Server not running");
		}
	}
	

}
