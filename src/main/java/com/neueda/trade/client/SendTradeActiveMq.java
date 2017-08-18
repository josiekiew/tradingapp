package com.neueda.trade.client;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MessageConverter;

import com.neueda.trade.server.model.BuySell;
import com.neueda.trade.server.model.DtoFactory;
import com.neueda.trade.server.model.Trade;

/**
 * Example upstream client to place a trade and wait for any topic message
 * Requires an external ActiveMQ queue so uses the "prod2 configuration properties.
 * 
 * @author Neueda
 *
 */
@SpringBootApplication
@Profile("sendtrade")
@ComponentScan({"com.neueda.trade.server.database", "com.neueda.trade.activemq", "com.neueda.trade.server.jms"})
public class SendTradeActiveMq implements CommandLineRunner {

	private static final Logger logger = LoggerFactory.getLogger(SendTradeActiveMq.class);

	public static void main(String[] args) throws Exception {
		logger.info("Starting send client");
		new SpringApplicationBuilder(SendTradeActiveMq.class).web(false).properties("spring.profiles.active=dev").profiles("sendtrade").run(args);
	}

	@Autowired
	private MessageConverter messageConverter;

    @Bean (name="topicContainerFactory")
    public DefaultJmsListenerContainerFactory topicContainerFactory(ConnectionFactory connectionFactory) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setPubSubDomain(true);
    	jmsTemplate.setMessageConverter(messageConverter);
        return factory;
    }

    private CountDownLatch latch = new CountDownLatch(1);

	@JmsListener(containerFactory = "topicContainerFactory",
			     destination = "${trade.downstream.topic}")
	public void consumeTrade(Message message) throws JMSException {
		logger.info("JMS topic listener: {}", message.toString());
		logger.info("Stopping send client");
		latch.countDown();
	}

	@Autowired
	@Qualifier("jmsQueueTemplate")
	private JmsTemplate jmsTemplate;

	@Autowired
	@Qualifier("tradeQueue")
	private Queue tradeQueue;
	
    @Autowired
    private DtoFactory factory;

    @Autowired
    private ApplicationContext appContext;
   
	@Override
	public void run(String... args) {
		logger.info("Running send client");
		Date now = new Date();
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss001");
		Trade trade = factory.createTrade(df.format(now), factory.createStock("NYSE.C"), now, 100.0, 10000, BuySell.Buy);

		jmsTemplate.convertAndSend(tradeQueue, trade, message -> {
			message.setStringProperty("Operation", "Place");
			logger.info("JMS place trade: {}", message.toString());
			return message;
		});
		logger.info("Waiting for topic message");	
		try {latch.await();} catch (InterruptedException ex) {}
		logger.info("Stopping send client");	
		SpringApplication.exit(appContext, () -> 0);
	}

}
