package com.neueda.trade.server.messaging;

import javax.jms.JMSException;
import javax.jms.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import com.neueda.trade.server.model.Trade;
import com.neueda.trade.server.rules.Operations;

/**
 * Class to hold listeners for JMS trade queue
 * 
 * @author neueda
 *
 */
@Component
@Profile("!mock")
public class TradeQueueListener {

    private static final Logger logger = LoggerFactory.getLogger(TradeQueueListener.class);

    @Autowired
    private Operations operations;
    
    @JmsListener(containerFactory = "queueContainerFactory",
    		     destination = "${trade.upstream.queue}",
    		     selector = "Operation = 'Place'")
    public void placeTrade(Trade trade) throws JMSException {
        operations.place(trade);
    }

    @JmsListener(containerFactory = "queueContainerFactory",
		     destination = "${trade.upstream.queue}",
		     selector = "Operation = 'Modify'")
	public Trade modifyTrade(Trade trade) {
    	logger.info("Received from trade queue: modify {}", trade);
		return operations.modify(trade.getTransid(), trade.getPrice(), trade.getVolume());
	}

    @JmsListener(containerFactory = "queueContainerFactory",
                 destination = "tradeQueue",
                 selector = "Operation = 'Cancel'")
    public void cancelTrade(String transid) throws JMSException {
    	logger.info("Trade queue cancel: {}", transid);
    	operations.cancel(transid);
    }   

    @JmsListener(containerFactory = "queueContainerFactory",
            destination = "tradeQueue",
            selector = "Operation = 'Accept'")
	public void acceptTrade(String transid) throws JMSException {
    	logger.info("Trade queue accept: {}", transid);
    	operations.accept(transid);
	}   

    @JmsListener(containerFactory = "queueContainerFactory",
            destination = "tradeQueue",
            selector = "Operation = 'Deny'")
	public void denyTrade(String transid) throws JMSException {
    	logger.info("Trade queue deny: {}", transid);
    	operations.deny(transid);
    }   

    @JmsListener(containerFactory = "queueContainerFactory",
            destination = "tradeQueue",
            selector = "Operation = 'Execute'")
	public void executeTrade(String transid) throws JMSException {
    	logger.info("Trade queue execute: {}", transid);
    	operations.execute(transid);
    }   

    @JmsListener(containerFactory = "queueContainerFactory",
            destination = "tradeQueue",
            selector = "Operation = 'Reject'")
	public void rejectTrade(String transid) throws JMSException {
    	logger.info("Trade queue reject: {}", transid);
    	operations.reject(transid);
    }   
    @JmsListener(containerFactory = "queueContainerFactory",
            destination = "tradeQueue",
            selector = "Operation = 'Settle'")
	public void settleTrade(String transid) throws JMSException {
    	logger.info("Trade queue settle: {}", transid);
    	operations.settle(transid);
    }   

    @JmsListener(containerFactory = "queueContainerFactory",
            destination = "tradeQueue",
            selector = "Operation = 'Reconcile'")
	public void eodReconcile(Message message) throws JMSException {
    	logger.info("Received from trade queue: recocile {}", message);
    	operations.eodReconcile();
	}  

}