package com.neueda.trade.server.model;

import java.util.List;

import com.neueda.trade.server.database.StockDto;


public interface StockDao {
    int rowCount();
    List<StockDto> findAll();
    Stock findById(String id);
    int place(Stock stock);
}
