package com.neueda.trade.server.rest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.neueda.trade.server.TradeException;
import com.neueda.trade.server.TradeServer;
import com.neueda.trade.server.model.Trade;
import com.neueda.trade.server.model.TradeDao;

@RunWith( SpringJUnit4ClassRunner.class )
@SpringBootTest(classes = {TradeServer.class})
@ActiveProfiles("test")
public class TradeControllerIT {

	@Autowired
	TradeController rest;

	@Autowired
	TradeDao tradeDao;

	@Test
	public void checkListAllTradesReturnsCorrectNumberOfTrades() {
		List<Trade> dao = tradeDao.findAll();
		List<Trade> trades = rest.list();
		assertThat(trades.size(), equalTo(dao.size()));
	}

	@Test
	public void checkFindTradeByValidId1ReturnsCorrectTrade() {
		int id = 1;
		Trade trade = rest.find(id);
		assertThat(trade.getId(), equalTo(id));
	}

	@Test(expected=TradeException.class)
	public void checkFindTradeByInvalidIdThrowsTradeException() {
		rest.find(-1);
	}
	
	// TODO: Exercise to add tests

}
