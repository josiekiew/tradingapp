
package com.neueda.trade.server.model;

import java.util.Date;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * DTO interface for Trades
 * 
 * Defines validation annotations but does not enforce validation.
 * 
 * See the {@link com.neueda.trade.server.model.Model Model class} for model validation methods.
 * 
 * @author Neueda
 *
 */
public interface Trade {
	@Min(value = 0, message = "id must be greater than zero")
	public int getId() ;
	public void setId(int id);

    @NotNull(message = "transaction ID must be supplied")
    @Size(min = 1, max = 20, message = "tranasction ID must be between {min} and {max} characters")
	public String getTransid();
	public void setTransid(String transid);

    @NotNull(message = "stock must be supplied")
	public Stock getStock();
	public void setStock(Stock stock);

    @NotNull(message = "placement timestamp must be supplied")
	public Date getPtime();
	public void setPtime(Date time);
	
    @DecimalMin(value="0.01", message = "price must be greater than zero")
	public double getPrice();
	public void setPrice(double price);
	
    @Min(value = 1, message = "volume must be greater than zero")
	public int getVolume();
	public void setVolume(int volume);
	
    @NotNull(message = "buy or sell must be supplied")
	public BuySell getBuysell();
	public void setBuysell(BuySell buysell);
	
	public TradeState getState();
	public void setState(TradeState state);
	
	public Date getStime();
	public void setStime(Date stime);
}
