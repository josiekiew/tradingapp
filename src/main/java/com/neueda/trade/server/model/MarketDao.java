package com.neueda.trade.server.model;

import java.util.List;

import com.neueda.trade.server.database.MarketDto;


public interface MarketDao {
    int rowCount();
    List<MarketDto> findAll();
    Market findById(String id);
    int place(Market market);
}
