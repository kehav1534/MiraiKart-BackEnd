package com.eshu.OnlineShopping.exceptions;

public class NotFoundException extends RuntimeException{
    public NotFoundException(String resourceName, int id){
        super(resourceName+ " not found with id : "+ id);
    }

    public NotFoundException(String resourceName, String identifier){
        super(resourceName+ " not found for : "+ identifier);
    }
}
