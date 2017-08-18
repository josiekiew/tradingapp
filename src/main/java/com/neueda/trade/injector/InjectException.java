package com.neueda.trade.injector;

@SuppressWarnings("serial")
public class InjectException extends RuntimeException
{
    public InjectException(String msg, Object... objs) {
        super(String.format(msg, objs));
    }
    
    public InjectException(Throwable t, String msg, Object... objs) {
        super(String.format(msg, objs), t);
    }
}

