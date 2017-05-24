package com.neueda.trade.server.rules;

import java.util.Arrays;
import java.util.Date;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.neueda.trade.server.TradeException;
import com.neueda.trade.server.messaging.TradeTopicSender;
import com.neueda.trade.server.model.Trade;
import com.neueda.trade.server.model.TradeDao;
import com.neueda.trade.server.model.TradeState;
import com.neueda.trade.server.monitoring.TradeMonitor;

@Component
public class Operations {

    private static final Logger logger = LoggerFactory.getLogger(Operations.class);

    
    @Autowired
    private TradeDao tradeDao;

	@Autowired
	private TradeTopicSender tradeSender;

	@Autowired
	private Model model;

	@Autowired
	private TradeMonitor monitor;

    public Trade place(Trade trade) {
    	trade.setState(TradeState.Place);
    	trade.setStime(new Date());
		model.validate(trade);
        int id = tradeDao.place(trade);
        trade = tradeDao.findById(id);
        logger.info("Place trade succeeded: {}", trade);
        monitor.place();
        return tradeSender.postPlace(trade);
    }


 	public int eodReconcile() {
 		int count = tradeDao.clear();
 		logger.info("EOD reconcilliation suceeded: removed {} rows", count);
        monitor.reconcile();
        return tradeSender.postEodReconcile(count);
 	}

    // TODO: Extension solutions start here
    
    public Trade modify(String transid, double price, int volume) {
    	Trade trade = tradeDao.findByTransid(transid);
		if (!stateInList(trade.getState(), Model.allowedStates.get(TradeState.Modify))) {
			throw new TradeException("Cannot change state to Modify: invalid current state %s", trade.getState());
		}
    	trade.setPrice(price);
    	trade.setVolume(volume);
    	trade.setState(TradeState.Modify);
    	trade.setStime(new Date());
    	model.validate(trade);
        trade = tradeDao.findById(tradeDao.modify(trade));
        logger.info("Modify trade succeeded: {}", trade);
        return tradeSender.postModify(trade);
    }

    private Trade checkState(String transid, TradeState newState, 
    		Function<Trade, Trade> dao, Function<Trade, Trade> send, Procedure stats) {
        Trade trade = tradeDao.findByTransid(transid);
		if (!stateInList(trade.getState(), Model.allowedStates.get(newState))) {
			throw new TradeException("Cannot change state to %s: invalid current state %s", newState, trade.getState());
		}
		trade.setState(newState);
		trade.setStime(new Date());
		trade = dao.apply(trade);
        logger.info("{} trade succeeded: {}", newState.name(), trade);
        if (stats != null) {
        	stats.call();
        }
		return send.apply(trade);    	
    }

    public Trade cancel(String transid) {
    	return checkState(transid, TradeState.Cancel, tradeDao::updateState, tradeSender::postCancel, monitor::cancel);
    }

    public Trade accept(String transid) {
    	return checkState(transid, TradeState.Accept, tradeDao::updateState, tradeSender::postAccept, monitor::accept);
    }

    public Trade deny(String transid) {
    	return checkState(transid, TradeState.Deny, tradeDao::updateState, tradeSender::postDeny, monitor::deny);
    }

    public Trade execute(String transid) {
    	return checkState(transid, TradeState.Execute, tradeDao::updateState, tradeSender::postExecute, monitor::update);
    }
    
    public Trade reject(String transid) {
    	return checkState(transid, TradeState.Reject, tradeDao::updateState, tradeSender::postReject, monitor::reject);
    }
    
    public Trade settle(String transid) {
    	return checkState(transid, TradeState.Settle, tradeDao::updateState, tradeSender::postSettle, monitor::settle);
    }

    /**
     * Check if input string is in a list of possible values
     * @param input string 
     * @param items array of strings to check against
     * @return true if input string in permitted items
     */
    private static boolean stateInList(TradeState input, TradeState[] items) {
        return items==null ? false : Arrays.stream(items).parallel().anyMatch(input::equals);
    }
    
    /**
     * Define predictae interface missing from java.util.function
     * Procedure has no paramaters and returns void
     */
	private static interface Procedure
	{
	    void call();
	}
}
