package br.com.fiap.bank_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.fiap.bank_api.model.Conta;

public interface ContaRepository extends JpaRepository<Conta, Long> {
    
}
