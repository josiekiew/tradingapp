package com.neueda.trade.server.activemq;

import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.Topic;

import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ActiveMqConfiguration {
    
    @Bean(name="tradeQueue")
    public Queue tradeQueue(@Value("${trade.upstream.queue}") String name)
            throws JMSException {
        return new ActiveMQQueue(name);
    }

	@Bean(name = "tradeTopic")
	public Topic tradeTopic(@Value("${trade.downstream.topic}") String name) throws JMSException {
		return new ActiveMQTopic(name);
	}

}
