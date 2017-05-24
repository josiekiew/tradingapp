package com.neueda.trade.server.rest;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.neueda.trade.server.TradeException;
import com.neueda.trade.server.model.Trade;
import com.neueda.trade.server.model.TradeDao;
import com.neueda.trade.server.rules.Operations;



@RestController
@Profile({"dev","test"})
@RequestMapping("/tradestest")
public class TradeTestController {

    @Autowired
    private TradeDao tradeDao;

    @Autowired
    private Operations operations;
    
    @RequestMapping(value = "/setstate", method = {RequestMethod.POST},
            consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
	public Trade setState(@RequestParam Map<String,String> form) {
    	String transid = form.get("transid");
    	String state = form.get("state");
    	switch (state) {
    	case "A": operations.accept(transid); break;
    	case "C": operations.cancel(transid); break;
    	case "D": operations.deny(transid); break;
    	case "E": operations.execute(transid); break;
    	case "M": operations.execute(transid); break;
    	case "R": operations.reject(transid); break;
    	case "S": operations.settle(transid); break;
    	default:
    		throw new TradeException("Invalid state value: '%s'", state);
    	}
    	return tradeDao.findByTransid(transid);
    }

}
