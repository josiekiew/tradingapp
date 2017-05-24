package com.neueda.trade.server;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("activemq")
@ComponentScan("com.neueda.trade.activemq")
public class ConfigActiveMq {

}
