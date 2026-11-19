package com.example.produtoapi.exception;

/*
Lançada quando a operação viola uma regra de negócio,
como um nome de categoria duplicado ou excluir uma categoria com produtos.
*/
public class ConflitoException extends RuntimeException {

    public ConflitoException(String mensagem) {
        super(mensagem);
    }
}
