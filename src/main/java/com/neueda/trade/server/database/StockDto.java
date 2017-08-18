
package com.neueda.trade.server.database;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.neueda.trade.server.model.Market;
import com.neueda.trade.server.model.Stock;

public class StockDto implements Stock {

    private String ticker;

    private String symbol;

    @JsonDeserialize(as=MarketDto.class)
    private Market market;

    private String description;

    public StockDto() {}

    public StockDto(String ticker) {
		this.setTicker(ticker);
	}

    @Override
    public String toString() {
        return String.format("Stock: {%s %s.%s %s}",
                             ticker, market, symbol, description);
    }

	/* (non-Javadoc)
	 * @see com.neueda.trade.server.model.Stock#getTicker()
	 */
	@Override
	public String getTicker() {
		return ticker;
	}

	/* (non-Javadoc)
	 * @see com.neueda.trade.server.model.Stock#setTicker(java.lang.String)
	 */
	@Override
	public void setTicker(String ticker) {
		this.ticker = ticker;
	}

	/* (non-Javadoc)
	 * @see com.neueda.trade.server.model.Stock#getSymbol()
	 */
	@Override
	public String getSymbol() {
		return symbol;
	}

	/* (non-Javadoc)
	 * @see com.neueda.trade.server.model.Stock#setSymbol(java.lang.String)
	 */
	@Override
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	/* (non-Javadoc)
	 * @see com.neueda.trade.server.model.Stock#getMarket()
	 */
	@Override
	public Market getMarket() {
		return market;
	}

	/* (non-Javadoc)
	 * @see com.neueda.trade.server.model.Stock#setMarket(com.neueda.trade.server.model.Market)
	 */
	@Override
	public void setMarket(Market market) {
		this.market = market;
	}

	/* (non-Javadoc)
	 * @see com.neueda.trade.server.model.Stock#getDescription()
	 */
	@Override
	public String getDescription() {
		return description;
	}

	/* (non-Javadoc)
	 * @see com.neueda.trade.server.model.Stock#setDescription(java.lang.String)
	 */
	@Override
	public void setDescription(String description) {
		this.description = description;
	}

}
