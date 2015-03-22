package com.mylibrary;

/**
 * @author Radim Kratochvil
 * @author Michael Le <lemichael@mail.muni.cz>
 */
public class ServiceFailureException extends RuntimeException {
    public ServiceFailureException(String msg) { super(msg); }
    public ServiceFailureException(Throwable cause) { super(cause); }
    public ServiceFailureException(String message, Throwable cause) { super(message, cause); }
}
