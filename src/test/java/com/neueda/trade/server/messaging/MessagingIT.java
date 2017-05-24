package com.neueda.trade.server.messaging;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.neueda.trade.server.TradeServer;
import com.neueda.trade.server.model.BuySell;
import com.neueda.trade.server.model.DtoFactory;
import com.neueda.trade.server.model.Trade;
import com.neueda.trade.server.model.TradeState;
import com.neueda.trade.server.monitoring.TradeMonitor;
import com.neueda.trade.server.monitoring.TradeStats;
import com.neueda.trade.server.rules.Model;

@RunWith( SpringRunner.class )
@SpringBootTest(classes = {TradeServer.class})
@ActiveProfiles("test")
public class MessagingIT {

	@Autowired
	@Qualifier("jmsQueueTemplate")
	JmsTemplate jmsQueueTemplate;

	@Autowired
	@Qualifier("tradeQueue")
	private Queue tradeQueue;

	@Autowired
	@Qualifier("tradeTopic")
	private Topic tradeTopic;

	@Autowired
	private Model model;
	
	@Autowired
	TradeMonitor monitor;
	
    @Autowired
    private DtoFactory factory;

	@Autowired
	private TradeTopicReader reader;
	
	@After
	public void tearDown() {
		monitor.reconcile();
	}

	@Test
    public void testSendValidPlaceTradeAndReceiveTopicMessage() throws JMSException, InterruptedException {
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss001");
		Date now = new Date();
		Trade trade = factory.createTrade(df.format(now), factory.createStock("NYSE.C"), now, 101.0, 10001, BuySell.Buy);
		jmsQueueTemplate.convertAndSend(tradeQueue, trade, message -> {
			message.setStringProperty("Operation", "Place");
			return message;
		});
		Message message = reader.queue.take();
        assertThat(message, is(notNullValue()));
        assertThat(message, instanceOf(TextMessage.class));
        Trade notification = factory.tradeFromJson(((TextMessage)message).getText());
        model.validate(trade);
        assertThat(notification.getTransid(), equalTo(trade.getTransid()));
        assertThat(notification.getState(), equalTo(TradeState.Place));

        // check the monitoring stats
        
		TradeStats stats = monitor.getStats();
		assertThat(stats.getTotalTrades(), equalTo(1));		
		assertThat(stats.getActiveTrades(), equalTo(1));		

	}

	@Test
    public void testFullTradeLifeCycle() throws JMSException, InterruptedException {
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss002");
		Date now = new Date();
		Trade trade = factory.createTrade(df.format(now), factory.createStock("NYSE.C"), now, 102.0, 10002, BuySell.Buy);
		
		jmsQueueTemplate.convertAndSend(tradeQueue, trade, message -> {
			message.setStringProperty("Operation", "Place");
			return message;
		});
		
		Message response = reader.queue.take();
        assertThat(response, instanceOf(TextMessage.class));
        assertThat(response.getStringProperty("Operation"), equalTo("Place"));
        Trade notification = factory.tradeFromJson(((TextMessage)response).getText());
        model.validate(notification);
        assertThat(notification.getTransid(), equalTo(trade.getTransid()));
        assertThat(notification.getState(), equalTo(TradeState.Place));

	    trade.setPrice(52);
	    trade.setVolume(5002);
		jmsQueueTemplate.convertAndSend(tradeQueue, trade, message -> {
			message.setStringProperty("Operation", "Modify");
			return message;
		});
		
		response = reader.queue.take();
        assertThat(response.getStringProperty("Operation"), equalTo("Modify"));      
        notification = factory.tradeFromJson(((TextMessage)response).getText());
        assertThat(notification.getTransid(), equalTo(trade.getTransid()));
        assertThat(notification.getPrice(), equalTo(52.0));
        assertThat(notification.getVolume(), equalTo(5002));
        assertThat(notification.getState(), equalTo(TradeState.Modify));

		jmsQueueTemplate.convertAndSend(tradeQueue, trade.getTransid(), message -> {
			message.setStringProperty("Operation", "Accept");
			return message;
		});
		
		response = reader.queue.take();        
        assertThat(response.getStringProperty("Operation"), equalTo("Accept"));
        notification = factory.tradeFromJson(((TextMessage)response).getText());
        assertThat(notification.getTransid(), equalTo(trade.getTransid()));
        assertThat(notification.getState(), equalTo(TradeState.Accept));

		jmsQueueTemplate.convertAndSend(tradeQueue, trade.getTransid(), message -> {
			message.setStringProperty("Operation", "Execute");
			return message;
		});
		
		response = reader.queue.take();        
        assertThat(response.getStringProperty("Operation"), equalTo("Execute"));
        notification = factory.tradeFromJson(((TextMessage)response).getText());
        assertThat(notification.getTransid(), equalTo(trade.getTransid()));
        assertThat(notification.getState(), equalTo(TradeState.Execute));

		jmsQueueTemplate.convertAndSend(tradeQueue, trade.getTransid(), message -> {
			message.setStringProperty("Operation", "Settle");
			return message;
		});
		
		response = reader.queue.take();        
        assertThat(response.getStringProperty("Operation"), equalTo("Settle"));
        notification = factory.tradeFromJson(((TextMessage)response).getText());
        assertThat(notification.getTransid(), equalTo(trade.getTransid()));
        assertThat(notification.getState(), equalTo(TradeState.Settle));

        // check the monitoring stats
        
		TradeStats stats = monitor.getStats();
		assertThat(stats.getTotalTrades(), equalTo(1));		
		assertThat(stats.getActiveTrades(), equalTo(0));		
		assertThat(stats.getSettledTrades(), equalTo(1));		

    }

}
