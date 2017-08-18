package com.neueda.trade.server.messaging;

import javax.jms.Topic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import com.neueda.trade.server.model.Trade;

@Component
public class TradeTopicSender {

    private static final Logger logger = LoggerFactory.getLogger(TradeTopicSender.class);

	@Autowired
	@Qualifier("jmsTopicTemplate")
	JmsTemplate jmsTemplate;

	@Autowired
	@Qualifier("tradeTopic")
	private Topic tradeTopic;
	
    private <T> T post(T data, String operation) {
		jmsTemplate.convertAndSend(tradeTopic, data, message -> {
			message.setStringProperty("Operation", operation);
			logger.info("Send to trade topic: {} {}", operation, data);
			return message;
		});
		return data;
    }

    public Trade postPlace(Trade trade) {
		return post(trade, "Place");
    }
    
    public int postEodReconcile(int count) {
		return post(count, "Reconcile");
    }

    public Trade postAccept(Trade trade) {
		return post(trade, "Accept");
    }

    public Trade postCancel(Trade trade) {
		return post(trade, "Cancel");
    }

    public Trade postDeny(Trade trade) {
		return post(trade, "Deny");
    }

    public Trade postExecute(Trade trade) {
		return post(trade, "Execute");
    }

    public Trade postModify(Trade trade) {
		return post(trade, "Modify");
    }

    public Trade postReject(Trade trade) {
		return post(trade, "Reject");
    }

    public Trade postSettle(Trade trade) {
		return post(trade, "Settle");
    }

}