package com.neueda.trade.server.model;

import java.util.Date;

/**
 * Factory functions required to create DTOs
 * 
 * @author Neueda
 *
 */
public interface DtoFactory {
	Stock createStock();
	Stock createStock(String ticker);
	
	Market createMarket();
	Market createMarket(String ticker);

	Trade createTrade();
	Trade createTrade(String transid, Stock stock, 
		     Date ptime, double price, int volume, 
		     BuySell buysell);
	
	Trade tradeFromJson(String json);
	Class<? extends Trade> tradeClass();
}
