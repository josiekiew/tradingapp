package com.neueda.trade.server.model;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public interface Market {

    @NotNull(message = "ticker must be supplied")
    @Size(min = 1, max = 12, message = "ticker cannot more than {max} characters")
	String getTicker();
	void setTicker(String ticker);

    @Size(min = 0, max = 60, message = "description cannot more than {max} characters")
	String getDescription();
	void setDescription(String description);

}