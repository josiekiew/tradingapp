package com.neueda.trade.injector;

import javax.jms.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import com.neueda.trade.server.model.DtoFactory;
import com.neueda.trade.server.model.Trade;
import com.neueda.trade.server.model.TradeState;

@Component
@Profile("injector-activemq")
@ComponentScan({"com.neueda.trade.server.jms", "com.neueda.trade.server.activemq"})
public class ActiveMqClient implements InjectorClient {

    @Autowired
    private DtoFactory factory;

	private static final Logger logger = LoggerFactory.getLogger(ActiveMqClient.class);
	
	@Autowired
	@Qualifier("jmsQueueTemplate")
	private JmsTemplate jmsTemplate;

	@Autowired
	@Qualifier("tradeQueue")
	private Queue tradeQueue;
	
	public void place(Trade trade) {
		jmsTemplate.convertAndSend(tradeQueue, trade, message -> {
			message.setStringProperty("Operation", "Place");
			logger.info("Place trade: {}", trade);
			return message;
		});
	}
	
	public void modify(String transid, double price, int volume) {
		Trade trade = factory.createTrade(transid, null, null, price, volume, null);
		jmsTemplate.convertAndSend(tradeQueue, trade, message -> {
			message.setStringProperty("Operation", "Modify");
			logger.info("Modify trade: {}", transid);
			return message;
		});
	}

	public void updateState(String transid, TradeState state) {
		jmsTemplate.convertAndSend(tradeQueue, transid, message -> {
			message.setStringProperty("Operation", state.name());
			logger.info("{} trade: {}", state.name(), transid);
			return message;
		});
	}

}
