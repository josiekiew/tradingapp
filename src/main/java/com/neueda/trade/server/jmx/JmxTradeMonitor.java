package com.neueda.trade.server.jmx;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

import com.neueda.trade.server.monitoring.TradeMonitor;
import com.neueda.trade.server.monitoring.TradeStats;

/**
 * Created by Neueda
 */
@Component
@ManagedResource(objectName = "com.neueda.trade.jmx:name=TradeStats",
        description = "View of trade states")
public class JmxTradeMonitor implements TradeMonitor {

	@Autowired
	private TradeStats stats;

	@Override
	public TradeStats getStats() {
		return stats;
	}
	
    @Override
	public void place() {
		stats.place();
	}
    
    @Override
	public void update() {
		return;
	}

    @Override
	public void cancel() {
		stats.cancel();
	}

	@Override
	public void accept() {
		return;
	}

	@Override
    public void deny() {
		stats.deny();
	}

	@Override
	public void execute() {
		return;
	}
	
	@Override
    public void reject() {
		stats.reject();
	}

	@Override   
    public void settle() {
		stats.settle();
	}

	/**
	 * EOD reconciliation clears down the stats
	 * @return number of outstanding active trades which should be zero on a good day
	 */
	@Override    
	public int reconcile() {
		return stats.reconcile();
	}
	
	// JMX exposed properties from this point

	@ManagedAttribute
	public int getTotalTrades() {
		return stats.getTotalTrades();
	}

    @ManagedAttribute
	public int getActiveTrades() {
		return stats.getActiveTrades();
	}

    @ManagedAttribute
	public int getPlacedTrades() {
		return stats.getPlacedTrades();
	}

    @ManagedAttribute
	public int getCancelledTrades() {
		return stats.getCancelledTrades();
	}

    @ManagedAttribute
	public int getDeniedTrades() {
		return stats.getDeniedTrades();
	}

    @ManagedAttribute
	public int getRejectedTrades() {
		return stats.getRejectedTrades();
	}

    @ManagedAttribute
	public int getSettledTrades() {
		return stats.getSettledTrades();
	}

}