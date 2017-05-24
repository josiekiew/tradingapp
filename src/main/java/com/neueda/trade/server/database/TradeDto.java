
package com.neueda.trade.server.database;

import java.util.Date;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.neueda.trade.server.model.BuySell;
import com.neueda.trade.server.model.Stock;
import com.neueda.trade.server.model.Trade;
import com.neueda.trade.server.model.TradeState;

/**
 * DTO for Trade table
 * 
 * Defines validation annotations but does not enforce validation.
 * 
 * See the {@link com.neueda.trade.server.rules.Model Model class} for model validation methods.
 * 
 * @author Neueda
 *
 */
public class TradeDto implements Trade {
		
    private int id;

    private String transid;

    @JsonDeserialize(as=StockDto.class)
    private Stock stock;

    private Date ptime;

    private double price;
    
    private int volume;

    private BuySell buysell;

    private TradeState state;

    private Date stime;

    public TradeDto() { }    

    /**
     * Convenience CTR to create a "new" trade, always sets id to 1 and state to O.
     * 
     * @param transid transaction ID
     * @param stock reference to stock ticker
     * @param ptime transaction placement time
     * @param price price in dollars
     * @param volume stock count
     * @param buysell B or S to indicate if trade is a Buy or Sell
     */
    public TradeDto(String transid, Stock stock, 
    		     Date ptime, double price, int volume, 
    		     BuySell buysell) {
		this.setTransid(transid);
		this.setStock(stock);
		this.setPtime(ptime);
		this.setPrice(price);
		this.setVolume(volume);
		this.setBuysell(buysell);
		this.setState(TradeState.Place);
		this.setStime(new Date());
	}


	@Override
    public String toString() {
        return String.format("Trade: {id=%d transid=%s ptime=%s %s %d@%.4f %1s %1s stime=%s}", id, transid, ptime, stock, volume, price, buysell, state, stime);
    }
    
    /* (non-Javadoc)
	 * @see com.neueda.trade.server.model.Trade#getId()
	 */
    @Override
	public int getId() {
        return id;
    }

	/* (non-Javadoc)
	 * @see com.neueda.trade.server.model.Trade#setId(int)
	 */
	@Override
	public void setId(int id) {
		this.id = id;
	}
	
	/* (non-Javadoc)
	 * @see com.neueda.trade.server.model.Trade#getTransid()
	 */
	@Override
	public String getTransid() {
		return transid;
	}

	/* (non-Javadoc)
	 * @see com.neueda.trade.server.model.Trade#setTransid(java.lang.String)
	 */
	@Override
	public void setTransid(String transid) {
		this.transid = transid;
	}

	/* (non-Javadoc)
	 * @see com.neueda.trade.server.model.Trade#getStock()
	 */
	@Override
	public Stock getStock() {
		return stock;
	}

	/* (non-Javadoc)
	 * @see com.neueda.trade.server.model.Trade#setStock(com.neueda.trade.server.model.Stock)
	 */
	@Override
	public void setStock(Stock stock) {
		this.stock = stock;
	}

	/* (non-Javadoc)
	 * @see com.neueda.trade.server.model.Trade#getPtime()
	 */
	@Override
	public Date getPtime() {
		return ptime;
	}

	/* (non-Javadoc)
	 * @see com.neueda.trade.server.model.Trade#setPtime(java.util.Date)
	 */
	@Override
	public void setPtime(Date time) {
		this.ptime = time;
	}

	/* (non-Javadoc)
	 * @see com.neueda.trade.server.model.Trade#getPrice()
	 */
	@Override
	public double getPrice() {
		return price;
	}

	/* (non-Javadoc)
	 * @see com.neueda.trade.server.model.Trade#setPrice(double)
	 */
	@Override
	public void setPrice(double price) {
		this.price = price;
	}

	/* (non-Javadoc)
	 * @see com.neueda.trade.server.model.Trade#getVolume()
	 */
	@Override
	public int getVolume() {
		return volume;
	}

	/* (non-Javadoc)
	 * @see com.neueda.trade.server.model.Trade#setVolume(int)
	 */
	@Override
	public void setVolume(int volume) {
		this.volume = volume;
	}

	/* (non-Javadoc)
	 * @see com.neueda.trade.server.model.Trade#getBuysell()
	 */
	@Override
	public BuySell getBuysell() {
		return buysell;
	}

	/* (non-Javadoc)
	 * @see com.neueda.trade.server.model.Trade#setBuysell(com.neueda.trade.server.model.BuySell)
	 */
	@Override
	public void setBuysell(BuySell buysell) {
		this.buysell = buysell;
	}

	/* (non-Javadoc)
	 * @see com.neueda.trade.server.model.Trade#getState()
	 */
	@Override
	public TradeState getState() {
		return state;
	}

	/* (non-Javadoc)
	 * @see com.neueda.trade.server.model.Trade#setState(com.neueda.trade.server.model.TradeState)
	 */
	@Override
	public void setState(TradeState state) {
		this.state = state;
	}

	/* (non-Javadoc)
	 * @see com.neueda.trade.server.model.Trade#getStime()
	 */
	@Override
	public Date getStime() {
		return stime;
	}

	/* (non-Javadoc)
	 * @see com.neueda.trade.server.model.Trade#setStime(java.util.Date)
	 */
	@Override
	public void setStime(Date stime) {
		this.stime = stime;
	}

}
