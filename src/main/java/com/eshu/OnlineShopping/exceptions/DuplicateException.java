package com.eshu.OnlineShopping.exceptions;

public class DuplicateException extends RuntimeException {
    public DuplicateException(String resource, String name) {
        super(resource+" with "+name+" already exists.");
    }
}
