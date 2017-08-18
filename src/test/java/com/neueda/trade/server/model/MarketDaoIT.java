package com.neueda.trade.server.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.neueda.trade.server.TradeServer;
import com.neueda.trade.server.model.MarketDao;

@RunWith( SpringRunner.class )
@SpringBootTest(classes = {TradeServer.class})
@ActiveProfiles("test")
public class MarketDaoIT {

	@Autowired
	private MarketDao marketDao;
	
	private int rows;
	
	@Before
	public void setup() {
		rows = marketDao.rowCount();
	}
	
	@Test
	public void checkStockRowCountReturnsConsistentValue() {
		assertThat(marketDao.rowCount(), equalTo(rows));
	}
	

}
