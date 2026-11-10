package com.example.produtoapi.exception;

import java.time.LocalDateTime;

public record ErrorResponse(
    int status,
    String erro,
    String mensagem,
    LocalDateTime timestamp
) {
}
