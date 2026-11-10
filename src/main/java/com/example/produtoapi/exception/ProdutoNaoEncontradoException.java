package com.example.produtoapi.exception;

public class ProdutoNaoEncontradoException extends RuntimeException {
    
    public ProdutoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
