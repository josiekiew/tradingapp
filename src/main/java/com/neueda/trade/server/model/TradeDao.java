package com.neueda.trade.server.model;

import java.util.List;


public interface TradeDao {
    int rowCount();
    List<Trade> findAll();
    Trade findById(int id);
    int place(Trade trade);
    Trade updateState(Trade trade);
    int clear();

    // TODO: Extension solutions start here
    
    Trade findByTransid(String transid);
    int modify(Trade trade);
}
