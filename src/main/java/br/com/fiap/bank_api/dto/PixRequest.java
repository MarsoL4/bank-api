package br.com.fiap.bank_api.dto;

import lombok.Data;

@Data
public class PixRequest {
    private Long origemId;
    private Long destinoId;
    private double valor;
}