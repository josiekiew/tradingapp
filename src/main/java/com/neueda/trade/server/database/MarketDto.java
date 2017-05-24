
package com.neueda.trade.server.database;

import com.neueda.trade.server.model.Market;

public class MarketDto implements Market {

    private String ticker;

    private String description;

    public MarketDto() {}

    public MarketDto(String ticker) {
		this.setTicker(ticker);
	}


	@Override
    public String toString() {
        return String.format("Market: {%s %s}",
                             ticker, description);
    }

	/* (non-Javadoc)
	 * @see com.neueda.trade.server.model.Market#getTicker()
	 */
	@Override
	public String getTicker() {
		return ticker;
	}

	/* (non-Javadoc)
	 * @see com.neueda.trade.server.model.Market#setTicker(java.lang.String)
	 */
	@Override
	public void setTicker(String ticker) {
		this.ticker = ticker;
	}

	/* (non-Javadoc)
	 * @see com.neueda.trade.server.model.Market#getDescription()
	 */
	@Override
	public String getDescription() {
		return description;
	}

	/* (non-Javadoc)
	 * @see com.neueda.trade.server.model.Market#setDescription(java.lang.String)
	 */
	@Override
	public void setDescription(String description) {
		this.description = description;
	}

}
