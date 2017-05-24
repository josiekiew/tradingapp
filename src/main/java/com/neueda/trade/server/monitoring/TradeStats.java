package com.neueda.trade.server.monitoring;

import org.springframework.stereotype.Component;

// TODO: exercise to add accepted and executed trade states

@Component
public class TradeStats {
    private int totalTrades;
    private int activeTrades;
    private int placedTrades;
    private int cancelledTrades;
    private int deniedTrades;
    private int rejectedTrades;
    private int settledTrades;
    
	public int getTotalTrades() {
		return totalTrades;
	}
	public int getActiveTrades() {
		return activeTrades;
	}
	
	public int getPlacedTrades() {
		return placedTrades;
	}
	public int getCancelledTrades() {
		return cancelledTrades;
	}
	public int getDeniedTrades() {
		return deniedTrades;
	}
	public int getRejectedTrades() {
		return rejectedTrades;
	}
	public int getSettledTrades() {
		return settledTrades;
	}

	public int place() {
		++placedTrades;
		++activeTrades;
		return ++totalTrades;
	}

	public int cancel() {
		--activeTrades;
		return ++cancelledTrades;
	}

	public int deny() {
		--activeTrades;
		return ++deniedTrades;
	}

	public int reject() {
		--activeTrades;
		return ++rejectedTrades;
	}

	public int settle() {
		--activeTrades;
		return ++settledTrades;
	}
	
	/**
	 * EOD reconciliation clears down the stats
	 * @return number of outstanding active trades which should be zero on a good day
	 */
	public int reconcile() {
		int active = activeTrades;
		totalTrades = activeTrades = placedTrades = cancelledTrades = deniedTrades = rejectedTrades = settledTrades = 0;
		return active;
	}

}
