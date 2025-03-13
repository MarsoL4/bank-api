package br.com.fiap.bank_api.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.fiap.bank_api.model.Conta;
import br.com.fiap.bank_api.repository.ContaRepository;

@RestController
@RequestMapping("contas")
public class ContaController {
    

    @Autowired
    private ContaRepository repository;

    @GetMapping
    public List<Conta> index(){
        return repository.findAll();
    }
}
