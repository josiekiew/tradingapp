package com.neueda.trade.server.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.fail;

import java.util.Date;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.neueda.trade.server.TradeException;
import com.neueda.trade.server.TradeServer;
import com.neueda.trade.server.rules.Model;

@RunWith( SpringJUnit4ClassRunner.class )
@SpringBootTest(classes = {TradeServer.class})
@ActiveProfiles("test")
public class TradeTest {

	@Autowired
	private Model model;

	@Autowired
	private DtoFactory factory;
	
	@Test
	public void testCreateEmptyTrade() {
		assertThat(factory.createTrade(), notNullValue());
	}
	
	@Test
	public void testCreateBuyTradeWithValidValues() {
		Date now = new Date();
		Trade trade = factory.createTrade("1234567890", factory.createStock("AA"), now, 100.0, 50, BuySell.Buy);
		assertThat(trade.getTransid(), equalTo("1234567890"));		
		assertThat(trade.getStock().getTicker(), equalTo("AA"));		
		assertThat(trade.getPtime(), equalTo(now));		
		assertThat(trade.getPrice(), equalTo(100.0));	
		assertThat(trade.getVolume(), equalTo(50));		
		assertThat(trade.getBuysell(), equalTo(BuySell.Buy));		
		assertThat(trade.getState(), equalTo(TradeState.Place));		
		assertThat(trade.getStime(), notNullValue());		
	}

	@Test
	public void testCreateSellTradeWithValidValues() {
		Date now = new Date();
		Trade trade = factory.createTrade("12345678901", factory.createStock("BB"), now, 101.0, 51, BuySell.Sell);
		assertThat(trade.getTransid(), equalTo("12345678901"));		
		assertThat(trade.getStock().getTicker(), equalTo("BB"));		
		assertThat(trade.getPtime(), equalTo(now));		
		assertThat(trade.getPrice(), equalTo(101.0));	
		assertThat(trade.getVolume(), equalTo(51));		
		assertThat(trade.getBuysell(), equalTo(BuySell.Sell));		
		assertThat(trade.getState(), equalTo(TradeState.Place));		
		assertThat(trade.getStime(), notNullValue());		
	}

	@Test
	public void testCreateAndValidateBuyTradeWithValidValues() {
		Trade trade = factory.createTrade("1", factory.createStock("AA"), new Date(), 100.0, 50, BuySell.Buy);
		Trade validated = model.validate(trade);
		assertThat(validated.getId(), equalTo(trade.getId()));		
		assertThat(validated.getTransid(), equalTo(trade.getTransid()));		
		assertThat(validated.getState(), equalTo(trade.getState()));		
	}
	
	@Test(expected=TradeException.class)
	public void checkPlaceTradeRejectsInvalidVolume() {
		model.validate(factory.createTrade("1", factory.createStock("AA"), new Date(), 100.0, -99, BuySell.Buy));
	}

	// Rules give access to the exception

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Test
	public void checkPlaceTradeRejectsInvalidPrice() {
		exception.expect(TradeException.class);
	    exception.expectMessage(matchesPattern("price.*greater than zero"));
		model.validate(factory.createTrade("1", factory.createStock("AA"), new Date(), -1.1, 100, BuySell.Buy));
	}
	
	// Only use try/catch if you can't test using a rule
	
	@Test
	public void checkPlaceTradeRejectsNullStock() {
		try {
			model.validate(factory.createTrade("1", null, new Date(), 100.0, 100, BuySell.Buy));
			fail("Trade validation did not throw exception");
		}
		catch (TradeException ex) {
			assertThat(ex.getMessage(), matchesPattern("stock (must be|not) supplied"));
		}
	}

}
