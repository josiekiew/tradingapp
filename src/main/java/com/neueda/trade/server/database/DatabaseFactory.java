package com.neueda.trade.server.database;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.neueda.trade.server.model.BuySell;
import com.neueda.trade.server.model.DtoFactory;
import com.neueda.trade.server.model.Market;
import com.neueda.trade.server.model.Stock;
import com.neueda.trade.server.model.Trade;
import com.neueda.trade.server.rules.Model;

/**
 * Factory functions to create DTO objects for database access
 * 
 * @author Neueda
 *
 */
@Component
public class DatabaseFactory implements DtoFactory {
	
	public DatabaseFactory(@Autowired ObjectMapper mapper) {
		SimpleModule module = new SimpleModule("CustomModel", Version.unknownVersion());

		SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
		resolver.addMapping(Trade.class, TradeDto.class);
		resolver.addMapping(Stock.class, StockDto.class);
		resolver.addMapping(Market.class, MarketDto.class);
		module.setAbstractTypes(resolver);
		mapper.registerModule(module);	
	}
	
	@JsonCreator
	@Override
	public Market createMarket() {
		return new MarketDto();
	}

	@Override
	public Market createMarket(String ticker) {
		return new MarketDto(ticker);
	}

	@JsonCreator
	@Override
	public Stock createStock() {
		return new StockDto();
	}
	
	@Override
	public Stock createStock(String ticker) {
		return new StockDto(ticker);
	}

	@JsonCreator
	@Override
	public Trade createTrade() {
		return new TradeDto();
	}
	
	@Override
	public Trade createTrade(String transid, Stock stock, Date ptime, double price, int volume, BuySell buysell) {
		return new TradeDto(transid, stock, ptime, price, volume, buysell);
	}

	@Override
	public Trade tradeFromJson(String json) {
		return Model.fromJson(json, TradeDto.class);
	}
	
	@Override
	public Class<? extends Trade> tradeClass() {
		return TradeDto.class;
	}

}
