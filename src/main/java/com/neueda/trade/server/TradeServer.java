package com.neueda.trade.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * This starts the standalone Spring Boot web server.
 * 
 * @author Neueda
 *
 */
@SpringBootApplication
public class TradeServer {
    private static final Logger logger = LoggerFactory.getLogger(TradeServer.class);

    public static void main(String[] args) throws Exception {
    	logger.info("Trading application starting");
    	SpringApplication.run(TradeServer.class, args);
    } 
}


