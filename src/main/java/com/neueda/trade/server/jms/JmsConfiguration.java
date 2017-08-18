package com.neueda.trade.server.jms;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;
import org.springframework.stereotype.Service;
import org.springframework.util.ErrorHandler;

@Configuration
@EnableJms
public class JmsConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(JmsConfiguration.class);

    /**
     * Queue listener needs to regsieter JSON message converter
     * 
     * @param connectionFactory
     * @param errorHandler
     * @return
     */
    @Bean (name="queueContainerFactory")
    public DefaultJmsListenerContainerFactory queueContainerFactory(ConnectionFactory connectionFactory, JmsErrorHandler errorHandler) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setErrorHandler(errorHandler);
        factory.setMessageConverter(jacksonJmsMessageConverter());
        return factory;
    }

    @Service
    public static class JmsErrorHandler implements ErrorHandler {   
        @Override
        public void handleError(Throwable t) {
        	logger.error("Messaging error {}", t.getMessage());
        }
    }

    /**
     * Serialize message content to JSON encoded TextMessage
     * @return JSON converter
     */
    @Bean
    public MessageConverter jacksonJmsMessageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        return converter;
    }


    @Bean(name = "jmsQueueTemplate")
    public JmsTemplate jmsQueueTemplate(ConnectionFactory connectionFactory) throws JMSException {
    	JmsTemplate jmsTemplate = new JmsTemplate(connectionFactory);
    	jmsTemplate.setMessageConverter(jacksonJmsMessageConverter());
    	return jmsTemplate;
    }

    @Bean(name = "jmsTopicTemplate")
    public JmsTemplate jmsTopicTemplate(ConnectionFactory connectionFactory) throws JMSException {
    	JmsTemplate jmsTemplate = new JmsTemplate(connectionFactory);
    	jmsTemplate.setMessageConverter(jacksonJmsMessageConverter());
    	jmsTemplate.setPubSubDomain(true);
    	return jmsTemplate;
    }

}