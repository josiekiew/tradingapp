package com.neueda.trade.server.messaging;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;

/**
 * Helper class to maintain subscription to topic during duration
 * of message testing. Cannot use synchronous reads as this doesn't maintain
 * the session - hence message loss as this isn't a durable subscription
 * 
 * @author Neueda
 *
 */
@Configuration
@Profile("test")
public class TradeTopicReader {

    @Bean (name="topicContainerFactory")
    public DefaultJmsListenerContainerFactory topicContainerFactory(ConnectionFactory connectionFactory) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setPubSubDomain(true);
        return factory;
    }
    
	@JmsListener(containerFactory = "topicContainerFactory",
		     destination = "${trade.downstream.topic}")
	public void monitorTopic(Message message) throws JMSException {
		if (message != null) {
			queue.offer(message);
		}
	}

	public BlockingQueue<Message> queue = new LinkedBlockingQueue<>();

}
