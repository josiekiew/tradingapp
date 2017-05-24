package com.neueda.trade.server;

@SuppressWarnings("serial")
public class TradeException extends RuntimeException
{
    public TradeException(String msg, Object... objs) {
        super(String.format(msg, objs));
    }
}

