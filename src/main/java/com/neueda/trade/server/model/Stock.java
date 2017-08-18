package com.neueda.trade.server.model;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public interface Stock {

    @NotNull(message = "ticker must be supplied")
    @Size(min = 1, max = 16, message = "ticker cannot more than {max} characters")
	String getTicker();
	void setTicker(String ticker);

    @NotNull(message = "symbol must be supplied")
    @Size(min = 1, max = 4, message = "symbol cannot more than {max} characters")
	String getSymbol();
	void setSymbol(String symbol);

    @NotNull(message = "market must be supplied")
	Market getMarket();
	void setMarket(Market market);

    @Size(min = 0, max = 60, message = "description cannot more than {max} characters")
	String getDescription();
	void setDescription(String description);

}