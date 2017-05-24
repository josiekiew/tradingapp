package com.neueda.trade.server;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("mysql")
@ComponentScan("com.neueda.trade.server.mysql")
public class ConfigMySql {

}
