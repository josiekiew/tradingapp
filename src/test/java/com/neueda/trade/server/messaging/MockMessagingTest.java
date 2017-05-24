package com.neueda.trade.server.messaging;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
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
@SpringBootTest(classes = {TradeServer.class})
@ActiveProfiles({"test", "mock"})
public class MockMessagingTest {

    @Mock
    TradeDao tradeDao;
    
    @Mock
    private TradeTopicSender tradeSender;
	
    @InjectMocks
    private TradeQueueListener listener;

    @Spy
	@Autowired
	private Model model;

    @Spy
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
    public void tearDown() {
        verifyNoMoreInteractions(tradeDao, tradeSender);
        monitor.reconcile();
    }
        
	@Test
    public void testSendValidPlaceTradeAndReceiveTopicMessage() throws JMSException {
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss001");
		Date now = new Date();
		Trade trade = factory.createTrade(df.format(now), factory.createStock("NYSE.C"), now, 100.0, 10000, BuySell.Buy);
		
        // Expectations
	    when(tradeDao.place(trade)).thenReturn(trade.getId());
	    when(tradeDao.findById(trade.getId())).thenReturn(trade);
	    when(tradeSender.postPlace(trade)).thenReturn(trade);

        // Execute the method being tested
        listener.placeTrade(trade);
 
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

	@Test
	public void testFullTradeLiofecycleSuccess() throws JMSException {
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss001");
		Date now = new Date();
		Trade trade = factory.createTrade(df.format(now), factory.createStock("NYSE.C"), now, 100.0, 10000, BuySell.Buy);
		
		TradeStats stats = monitor.getStats();
		int totalTrades = stats.getTotalTrades();
		int activeTrades = stats.getActiveTrades();
		int settledTrades = stats.getSettledTrades();
		
	    // Expectations
		
	    when(tradeDao.place(trade)).thenReturn(trade.getId());
	    when(tradeDao.findById(trade.getId())).thenReturn(trade);
	    when(tradeDao.findByTransid(trade.getTransid())).thenReturn(trade);
	    when(tradeDao.modify(trade)).thenReturn(trade.getId());
	    when(tradeDao.updateState(trade)).thenReturn(trade);
	    when(tradeSender.postPlace(trade)).thenReturn(trade);
	    when(tradeSender.postModify(trade)).thenReturn(trade);
	    when(tradeSender.postAccept(trade)).thenReturn(trade);
	    when(tradeSender.postExecute(trade)).thenReturn(trade);
	    when(tradeSender.postSettle(trade)).thenReturn(trade);
	
	    // Run scenario 
	    
	    listener.placeTrade(trade);

	    ArgumentCaptor<Trade> tradeArg = ArgumentCaptor.forClass(Trade.class);
	    verify(tradeDao).place(tradeArg.capture());
	    assertThat(tradeArg.getValue().getTransid(), equalTo(trade.getTransid()));
	    assertThat(tradeArg.getValue().getState(), equalTo(TradeState.Place));
	    verify(tradeSender).postPlace(tradeArg.capture());
	    assertThat(tradeArg.getValue().getTransid(), equalTo(trade.getTransid()));
	    assertThat(tradeArg.getValue().getState(), equalTo(TradeState.Place));

	    trade.setPrice(50);
	    trade.setVolume(5000);
	    listener.modifyTrade(trade);

	    verify(tradeDao).modify(tradeArg.capture());
	    assertThat(tradeArg.getValue().getTransid(), equalTo(trade.getTransid()));
	    assertThat(tradeArg.getValue().getState(), equalTo(TradeState.Modify));
	    assertThat(tradeArg.getValue().getPrice(), equalTo(50.0));
	    assertThat(tradeArg.getValue().getVolume(), equalTo(5000));
	    verify(tradeSender).postModify(tradeArg.capture());
	    assertThat(tradeArg.getValue().getTransid(), equalTo(trade.getTransid()));
	    assertThat(tradeArg.getValue().getState(), equalTo(TradeState.Modify));
	    assertThat(tradeArg.getValue().getPrice(), equalTo(50.0));
	    assertThat(tradeArg.getValue().getVolume(), equalTo(5000));

	    listener.acceptTrade(trade.getTransid());

	    verify(tradeDao, times(1)).updateState(tradeArg.capture());
	    assertThat(tradeArg.getValue().getState(), equalTo(TradeState.Accept));
	    verify(tradeSender).postAccept(tradeArg.capture());
	    assertThat(tradeArg.getValue().getTransid(), equalTo(trade.getTransid()));
	    assertThat(tradeArg.getValue().getState(), equalTo(TradeState.Accept));
	    verify(tradeDao, times(1)).updateState(tradeArg.capture());
	    assertThat(tradeArg.getValue().getState(), equalTo(TradeState.Accept));

	    listener.executeTrade(trade.getTransid());

	    verify(tradeDao, times(2)).updateState(tradeArg.capture());
	    assertThat(tradeArg.getValue().getState(), equalTo(TradeState.Execute));
	    verify(tradeSender).postExecute(tradeArg.capture());
	    assertThat(tradeArg.getValue().getTransid(), equalTo(trade.getTransid()));
	    assertThat(tradeArg.getValue().getState(), equalTo(TradeState.Execute));

	    listener.settleTrade(trade.getTransid());

	    verify(tradeDao, times(3)).updateState(tradeArg.capture());
	    assertThat(tradeArg.getValue().getState(), equalTo(TradeState.Settle));
	    verify(tradeSender).postSettle(tradeArg.capture());
	    assertThat(tradeArg.getValue().getTransid(), equalTo(trade.getTransid()));
	    assertThat(tradeArg.getValue().getState(), equalTo(TradeState.Settle));

	    // Validate interactions
	    
        verify(tradeDao, atLeastOnce()).findById(anyInt());
        verify(tradeDao, atLeastOnce()).findByTransid(trade.getTransid());
        
        // check the monitoring stats
        
		assertThat(stats.getTotalTrades(), equalTo(totalTrades+1));		
		assertThat(stats.getActiveTrades(), equalTo(activeTrades));		
		assertThat(stats.getSettledTrades(), equalTo(settledTrades+1));		

	}

}
