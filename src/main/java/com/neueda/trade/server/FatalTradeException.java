package com.neueda.trade.server;

@SuppressWarnings("serial")
public class FatalTradeException extends TradeException
{
    public FatalTradeException(String msg, Object... objs) {
        super(String.format(msg, objs));
    }
}