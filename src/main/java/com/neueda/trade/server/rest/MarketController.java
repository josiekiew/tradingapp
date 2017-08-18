package com.neueda.trade.server.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.neueda.trade.server.database.MarketDto;
import com.neueda.trade.server.model.Market;
import com.neueda.trade.server.model.MarketDao;


@RestController
@RequestMapping("/markets")
public class MarketController {

    @Autowired
    private MarketDao marketDao;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public List<MarketDto> list() {
        List<MarketDto> markets = marketDao.findAll();
        return markets;
    }
    
    @RequestMapping(value = "/find/{id}", method = RequestMethod.GET)
    public Market find(@PathVariable String id) {
        return marketDao.findById(id);
    }
}
