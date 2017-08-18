package com.neueda.trade.server.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.neueda.trade.server.database.StockDto;
import com.neueda.trade.server.model.Stock;
import com.neueda.trade.server.model.StockDao;


@RestController
@RequestMapping("/stocks")
public class StockController {

    @Autowired
    private StockDao stockDao;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public List<StockDto> list() {
        List<StockDto> stocks = stockDao.findAll();
        return stocks;
    }
    
    @RequestMapping(value = "/find/{id}", method = RequestMethod.GET)
    public Stock find(@PathVariable String id) {
        return stockDao.findById(id);
    }
}
