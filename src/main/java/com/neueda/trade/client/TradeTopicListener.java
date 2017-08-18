package com.neueda.trade.client;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;

/**
 * Example upstream client to place a trade and wait for any topic message
 * Requires an external ActiveMQ queue so uses the "prod2 configuration properties.
 * 
 * @author Neueda
 *
 */
@Configuration
@EnableAutoConfiguration
@Profile("tradelistener")
public class TradeTopicListener implements CommandLineRunner {

	private static final Logger logger = LoggerFactory.getLogger(TradeTopicListener.class);

	public static void main(String[] args) throws Exception {
		logger.info("Starting monitor client");
		new SpringApplicationBuilder(TradeTopicListener.class).web(false).properties("spring.profiles.active=prod").profiles("tradelistener").run(args);
	}

    @Bean (name="topicContainerFactory")
    public DefaultJmsListenerContainerFactory topicContainerFactory(ConnectionFactory connectionFactory) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setPubSubDomain(true);
        return factory;
    }

	@JmsListener(containerFactory = "topicContainerFactory",
			     destination = "${trade.downstream.topic}")
	public void consumeTrade(Message message) throws JMSException {
		logger.info("JMS topic monitored: {}", message.toString());
	}

	@Override
	public void run(String... args) {
		logger.info("Waiting for messages");
	}

}
