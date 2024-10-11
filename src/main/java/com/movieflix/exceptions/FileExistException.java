package com.movieflix.exceptions;

public class FileExistException extends  RuntimeException{

    public FileExistException(String message){
        super(message);
    }

}
