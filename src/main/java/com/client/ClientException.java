package com.client;

/**
 * Created by Юлия on 27.12.2016.
 */
public class ClientException extends Exception {
    public ClientException(){
    }

    public ClientException(String message){
        super(message);
    }

    public ClientException(Throwable cause){
        super(cause);
    }

    public ClientException(String message, Throwable cause){
        super(message, cause);
    }

    public ClientException(String message, Throwable cause,
                           boolean enableSuppression, boolean writableStackTrace){
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
