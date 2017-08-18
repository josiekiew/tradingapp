package com.neueda.trade.server.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Class to encapsulate possible trade states and JSON representation
 * 
 * @author Neueda
 *
 */
public enum TradeState {
	@JsonProperty("A") Accept, 
	@JsonProperty("C") Cancel,
	@JsonProperty("D") Deny,
	@JsonProperty("M") Modify,
	@JsonProperty("P") Place,
	@JsonProperty("R") Reject,
	@JsonProperty("S") Settle,
	@JsonProperty("E") Execute
}
