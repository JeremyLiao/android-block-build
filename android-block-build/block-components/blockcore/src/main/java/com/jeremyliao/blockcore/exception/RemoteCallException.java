package com.jeremyliao.blockcore.exception;

/**
 * Created by liaohailiang on 2019/2/27.
 */
public class RemoteCallException extends Exception {

    public RemoteCallException(String message) {
        super(message);
    }

    public RemoteCallException(String message, Throwable cause) {
        super(message, cause);
    }
}
