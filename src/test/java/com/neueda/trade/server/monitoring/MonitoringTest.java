package com.neueda.trade.server.monitoring;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.function.IntSupplier;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.neueda.trade.server.TradeServer;

// TODO: add other lifecycle tests as exercise

@RunWith( SpringJUnit4ClassRunner.class )
@SpringBootTest(classes = {TradeServer.class})
@ActiveProfiles("test")
public class MonitoringTest {

	@Autowired
	private TradeMonitor monitor;

	@After
	public void tearDown() {
		monitor.reconcile();
	}

	@Test
	public void testNewStatsAreEmpty() {
		assertThat(monitor.getStats().getTotalTrades(), equalTo(0));		
	}
	
	private void checkStats (int[] values, IntSupplier... stats) {
		for (int i=0; i<stats.length; i++) {
			assertThat(String.format("check in stats[%d]", i), stats[i].getAsInt(), equalTo(values[i]));
		}
	}

	@Test
	public void testPlaceOneTradeCountsOneActiveTrade() {
		TradeStats stats = monitor.getStats();
		checkStats(new int[] {0, 0}, stats::getTotalTrades, stats::getActiveTrades);
		monitor.place();
		checkStats(new int[] {1, 1}, stats::getTotalTrades, stats::getActiveTrades);
	}
	
	@Test
	public void testPlaceTwoTradesCountsTwoActiveTrade() {
		TradeStats stats = monitor.getStats();
		checkStats(new int[] {0, 0}, stats::getTotalTrades, stats::getActiveTrades);
		monitor.place();
		checkStats(new int[] {1, 1}, stats::getTotalTrades, stats::getActiveTrades);
		monitor.place();
		checkStats(new int[] {2, 2}, stats::getTotalTrades, stats::getActiveTrades);
	}
	
	@Test
	public void testRecconcileClearsAllTrades() {
		TradeStats stats = monitor.getStats();
		checkStats(new int[] {0, 0}, stats::getTotalTrades, stats::getActiveTrades);
		monitor.place();
		checkStats(new int[] {1, 1}, stats::getTotalTrades, stats::getActiveTrades);
		monitor.reconcile();
		checkStats(new int[] {0, 0}, stats::getTotalTrades, stats::getActiveTrades);
	}
	

	@Test
	public void testPlaceTradeAndCancel() {
		TradeStats stats = monitor.getStats();
		monitor.place();
		checkStats(new int[] {1, 1, 0}, stats::getTotalTrades, stats::getActiveTrades, stats::getSettledTrades);
		monitor.cancel();
		checkStats(new int[] {1, 0, 1}, stats::getTotalTrades, stats::getActiveTrades, stats::getCancelledTrades);		
		checkStats(new int[] {0, 0, 0}, stats::getSettledTrades, stats::getDeniedTrades, stats::getRejectedTrades);		
	}

	@Test
	public void testFullSettledTradeLifecycle() {
		TradeStats stats = monitor.getStats();
		monitor.place();
		checkStats(new int[] {1, 1, 0}, stats::getTotalTrades, stats::getActiveTrades, stats::getSettledTrades);
		monitor.accept();
		checkStats(new int[] {1, 1, 0}, stats::getTotalTrades, stats::getActiveTrades, stats::getSettledTrades);			
		monitor.execute();
		checkStats(new int[] {1, 1, 0}, stats::getTotalTrades, stats::getActiveTrades, stats::getSettledTrades);			
		monitor.settle();
		checkStats(new int[] {1, 0, 1}, stats::getTotalTrades, stats::getActiveTrades, stats::getSettledTrades);			
		checkStats(new int[] {0, 0, 0}, stats::getCancelledTrades, stats::getDeniedTrades, stats::getRejectedTrades);		
	}
}
