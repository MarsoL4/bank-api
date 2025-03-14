package br.com.fiap.bank_api.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
public class Conta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long numero;

    @Column(nullable = false)
    private Integer agencia;

    @Column(nullable = false)
    private String nomeTitular;

    @Column(nullable = false, unique = true)
    private String cpf;

    @Column(nullable = false)
    private LocalDate dataAbertura;

    @Column(nullable = false)
    private double saldoInicial;

    @Column(nullable = false)
    private boolean ativa = true;  // Conta sempre começa ativa

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoConta tipo;
}
