package com.neueda.trade.server.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Class to encapsulate possible Buy/Sell states and JSON representation
 * 
 * @author Neueda
 *
 */
public enum BuySell {
    @JsonProperty("B") Buy, 
    @JsonProperty("S") Sell
}
