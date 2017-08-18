package com.neueda.trade.server.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.neueda.trade.server.TradeException;
import com.neueda.trade.server.TradeServer;

@RunWith( SpringJUnit4ClassRunner.class )
@SpringBootTest(classes = {TradeServer.class})
@ActiveProfiles("test")
public class TradeDaoIT {

	@Autowired
	private TradeDao tradeDao;
	
	private int rows;
	
	@Before
	public void setup() {
		rows = tradeDao.rowCount();
	}
	
	@Test
	public void checkTradeRowCountReturnsConsistentValue() {
		assertThat(tradeDao.rowCount(), equalTo(rows));
	}
	
	@Test
	public void checkListAllTradesReturnsCorrectRowCount() {
		List<Trade> trades = tradeDao.findAll();
		assertThat(trades.size(), equalTo(rows));		
	}
	
	@Test
	public void checkListAllTradesReturnsUniqueTradeIds() {
		List<Trade> trades = tradeDao.findAll();
		Set<Integer> ids = new HashSet<>();
		for (Trade t : trades) {
			ids.add(t.getId());
		}
		assertThat(ids.size(), equalTo(trades.size()));		
	}

	@Test
	public void checkFindTradeByValidId1ReturnsCorrectTrade() {
		int id = 1;
		Trade trade = tradeDao.findById(id);
		assertThat(trade.getId(), equalTo(id));
	}

	@Test(expected=TradeException.class)
	public void checkFindTradeByInvalidIdThrowsTradeException() {
		tradeDao.findById(-1);
	}

}
