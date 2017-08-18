package com.neueda.trade.injector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Profile;

/**
 * Upstraem application to inject trades into the system
 * 
 * @author Neueda
 *
 */
@SpringBootApplication
@Profile("injector")
@ComponentScan({"com.neueda.trade.injector", "com.neueda.trade.server.database"})
public class Injector implements CommandLineRunner {

	private static final Logger logger = LoggerFactory.getLogger(Injector.class);

	public static void main(String[] args) throws Exception {
		logger.info("Starting trade injector");
		new SpringApplicationBuilder(Injector.class).web(false).properties("spring.profiles.active=prod").profiles("injector").run(args);
	}
	
    @Autowired
    private ApplicationContext appContext;

    @Autowired
    private InjectTrades injector;
   
	@Override
	public void run(String... args) throws InterruptedException {
		logger.info("Running injector");
		injector.run();
		logger.info("Stopping injector");	
		SpringApplication.exit(appContext, () -> 0);
	}

}
