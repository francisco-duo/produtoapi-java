package com.example.produtoapi.dto;

import java.math.BigDecimal;

// Resposta da consulta de valor total do estoque (soma de preco * quantidade)
public record ValorEstoqueDTO(
    BigDecimal valorTotal
) {}
