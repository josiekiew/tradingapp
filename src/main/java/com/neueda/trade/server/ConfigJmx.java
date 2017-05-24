package com.neueda.trade.server;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("jmx")
@ComponentScan("com.neueda.trade.jmx")
public class ConfigJmx {

}
