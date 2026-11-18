package com.example.produtoapi.exception;

public class CategoriaNaoEncontradaException extends RuntimeException {
    
    public CategoriaNaoEncontradaException(String mensagem) {
        super(mensagem);
    }
}
