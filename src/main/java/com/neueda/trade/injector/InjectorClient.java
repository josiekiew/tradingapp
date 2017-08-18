package com.neueda.trade.injector;

import com.neueda.trade.server.model.Trade;
import com.neueda.trade.server.model.TradeState;

/**
 * Interface for injector message delivery system
 * 
 * @author Neueda
 *
 */
public interface InjectorClient {

	void place(Trade trade);

	void modify(String transid, double price, int volume);

	void updateState(String transid, TradeState state);

}