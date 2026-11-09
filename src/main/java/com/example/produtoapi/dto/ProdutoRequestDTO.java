package com.example.produtoapi.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ProdutoRequestDTO(

    @NotBlank(message = "O nome é obrigatório")
    String nome,

    String descricao,

    @NotNull(message = "O preço é obrigatorio")
    @DecimalMin(value = "0.01", message = "O preço deve ser maior que zero")
    BigDecimal preco,

    @NotNull(message = "A quantidade é obrigatória")
    @Min(value = 0, message = "A quantidade não pode ser negativa")
    Integer quantidade
) {}
