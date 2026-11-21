package com.example.produtoapi.dto;

import java.math.BigDecimal;

// Resposta da consulta de valor do estoque agrupado por categoria
public record ValorEstoqueCategoriaDTO(
    String categoria,
    BigDecimal valorTotal
) {}
