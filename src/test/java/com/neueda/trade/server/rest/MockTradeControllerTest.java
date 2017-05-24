package com.neueda.trade.server.rest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.jms.JMSException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.neueda.trade.server.TradeServer;
import com.neueda.trade.server.messaging.TradeTopicSender;
import com.neueda.trade.server.model.BuySell;
import com.neueda.trade.server.model.DtoFactory;
import com.neueda.trade.server.model.Trade;
import com.neueda.trade.server.model.TradeDao;
import com.neueda.trade.server.model.TradeState;
import com.neueda.trade.server.monitoring.TradeMonitor;
import com.neueda.trade.server.monitoring.TradeStats;
import com.neueda.trade.server.rules.Model;
import com.neueda.trade.server.rules.Operations;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TradeServer.class)
@ActiveProfiles({"test", "mock"})
public class MockTradeControllerTest {
	
    @Mock
    TradeDao tradeDao;

    @Mock
    private TradeTopicSender tradeSender;
	
    @InjectMocks
    private TradeController controller;

    @Spy
	@Autowired
	private Model model;
    
    @Autowired
    private DtoFactory factory;

    @Spy
    @InjectMocks
    private Operations operations;
    
    @Spy
 	@Autowired
 	private TradeMonitor monitor;


    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
	}

    @After
    public void after() {
        verifyNoMoreInteractions(tradeDao);
    }
 
	@Test
    public void testPlaceValidTradeCallsDao() throws JMSException {
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss001");
		Date now = new Date();
		Trade trade = factory.createTrade(df.format(now), factory.createStock("NYSE.C"), now, 100.0, 10000, BuySell.Buy);
		
        // Expectations
	    when(tradeDao.place(trade)).thenReturn(trade.getId());
	    when(tradeDao.findById(trade.getId())).thenReturn(trade);
	    when(tradeSender.postPlace(trade)).thenReturn(trade);

        // Execute the method being tested
        controller.place(trade);
 
        // Validation
        ArgumentCaptor<Trade> tradeArg = ArgumentCaptor.forClass(Trade.class);
        verify(tradeDao).place(tradeArg.capture());
        assertThat(tradeArg.getValue().getTransid(), equalTo(trade.getTransid()));
        assertThat(tradeArg.getValue().getState(), equalTo(TradeState.Place));
        
        verify(tradeDao).findById(anyInt());
        
        verify(tradeSender).postPlace(tradeArg.capture());
        assertThat(tradeArg.getValue().getTransid(), equalTo(trade.getTransid()));
        assertThat(tradeArg.getValue().getState(), equalTo(TradeState.Place));
        
        // check the monitoring stats
        
		TradeStats stats = monitor.getStats();
		assertThat(stats.getTotalTrades(), equalTo(1));		
		assertThat(stats.getActiveTrades(), equalTo(1));		

    }

	// TODO: exercise to add full lifecycle test
}
