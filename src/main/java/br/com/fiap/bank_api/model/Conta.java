package br.com.fiap.bank_api.model;

import jakarta.persistence.Entity;
import lombok.Data;

@Entity
@Data
public class Conta {
    private Long numero;
    //private agencia;
    private String nomeTitular; //obrigatório
    //private cpf; //obrigatório
    //private dataAbertura; //não pode ser no futuro
    private double saldoInicial; //não pode ser negativo
    //private ativa;
    //private tipo; // deve ser APENAS corrente, poupança ou salário

    
}
