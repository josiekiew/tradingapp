package com.neueda.trade.server.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.neueda.trade.server.model.BuySell;
import com.neueda.trade.server.model.DtoFactory;
import com.neueda.trade.server.model.Trade;
import com.neueda.trade.server.model.TradeDao;
import com.neueda.trade.server.monitoring.TradeStats;
import com.neueda.trade.server.rules.Model;
import com.neueda.trade.server.rules.Operations;



@RestController
@RequestMapping("/trades")
public class TradeController {

    private static final Logger logger = LoggerFactory.getLogger(TradeController.class);

    @Autowired
    private TradeDao tradeDao;

    @Autowired
    private Operations operations;

    @Autowired
    private Model model;

    @Autowired
    private DtoFactory factory;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public List<Trade> list() {
        List<Trade> trades = tradeDao.findAll();
        return trades;
    }

    @RequestMapping(value = "/place", method = {RequestMethod.POST, RequestMethod.PUT},
                    consumes = {MediaType.APPLICATION_JSON_VALUE})
    public Trade place(@Validated @RequestBody Trade trade) {
    	logger.info("REST place: {}", trade);
    	return operations.place(trade);
    }

    @RequestMapping(value = "/place", method = {RequestMethod.POST},
            consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
	public Trade place(@RequestParam Map<String,String> form) {
    	Trade trade = factory.createTrade();
    	trade.setTransid(form.get("transid"));
    	trade.setStock(factory.createStock(form.get("stock")));
		  trade.setPtime(model.parseTime(form.get("ptime")));
		  trade.setPrice(Double.parseDouble(form.get("price")));
		  trade.setVolume(Integer.parseInt(form.get("volume")));
		  trade.setBuysell(Model.fromJson(form.get("buysell"), BuySell.class));

    	logger.info("REST form place: {}", trade);
    	return operations.place(trade);
    }

    @RequestMapping(value = "/find/{id}", method = RequestMethod.GET)
    public Trade find(@PathVariable int id) {
        return tradeDao.findById(id);
    }

    @RequestMapping(value = "/cancel/{transid}", method = {RequestMethod.GET, RequestMethod.DELETE})
    public Trade cancel(@PathVariable String transid) {
    	logger.info("REST cancel: id={}", transid);
        return operations.cancel(transid);
    }

    @SuppressWarnings("serial")
    @RequestMapping(value = "/reconcile", method = {RequestMethod.GET, RequestMethod.PUT})
    public Map<String, Integer> clear() {
        int count = operations.eodReconcile();
        return new HashMap<String, Integer>() {{put("count", count);}};
    }

    // TODO: Extension solutions start here

    @RequestMapping(value = "/modify/{transid}", method = {RequestMethod.POST},
            consumes = {MediaType.APPLICATION_JSON_VALUE})
	public Trade modify(@PathVariable String transid, @RequestBody Trade values) {
    	logger.info("REST modify: transid={} price={} volume={}", transid, values.getPrice(), values.getVolume());
		return operations.modify(transid, values.getPrice(), values.getVolume());
	}

    @RequestMapping(value = "/modify", method = {RequestMethod.POST},
            consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
	public Trade modify(@RequestParam String transid, @RequestParam double price, @RequestParam int volume) {
    	logger.info("REST form modify: transid={} price={} volume={}", transid, price, volume);
		return operations.modify(transid, price, volume);
    }

    @RequestMapping(value = "/accept/{transid}", method = {RequestMethod.GET, RequestMethod.POST})
    public Trade accept(@PathVariable String transid) {
    	logger.info("REST accept: id={}", transid);
        return operations.accept(transid);
    }

    @RequestMapping(value = "/deny/{transid}", method = {RequestMethod.GET, RequestMethod.POST})
    public Trade deny(@PathVariable String transid) {
    	logger.info("REST deny: id={}", transid);
        return operations.deny(transid);
    }

    @RequestMapping(value = "/execute/{transid}", method = {RequestMethod.GET, RequestMethod.POST})
    public Trade execute(@PathVariable String transid) {
    	logger.info("REST execute: id={}", transid);
        return operations.execute(transid);
    }

    @RequestMapping(value = "/reject/{transid}", method = {RequestMethod.GET, RequestMethod.POST})
    public Trade reject(@PathVariable String transid) {
    	logger.info("REST reject: id={}", transid);
        return operations.reject(transid);
    }

    @RequestMapping(value = "/settle/{transid}", method = {RequestMethod.GET, RequestMethod.POST})
    public Trade settle(@PathVariable String transid) {
        return operations.settle(transid);
    }

	@Autowired
	private TradeStats tradeStats;

    @RequestMapping(value = "/stats", method = RequestMethod.GET)
    public TradeStats stats() {
        return tradeStats;
    }

}
