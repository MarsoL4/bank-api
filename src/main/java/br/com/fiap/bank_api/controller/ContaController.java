package br.com.fiap.bank_api.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import br.com.fiap.bank_api.model.Conta;
import br.com.fiap.bank_api.repository.ContaRepository;
import jakarta.validation.Valid;

@RestController
@RequestMapping("contas")
public class ContaController {
    
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private ContaRepository repository;

    //Busca todas as contas cadastradas
    @GetMapping
    public List<Conta> index() {
        return repository.findAll();
    }

 
    @GetMapping("/{numero}")
    public ResponseEntity<Conta> findByNumero(@PathVariable Long numero) {
        return repository.findById(numero)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    @GetMapping("/cpf/{cpf}")
    public ResponseEntity<Conta> findByCpf(@PathVariable String cpf) {
        Optional<Conta> conta = repository.findAll().stream()
                .filter(c -> c.getCpf().equals(cpf))
                .findFirst();
        return conta.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

 
    @PostMapping("cadastrar")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Object> create(@Valid @RequestBody Conta conta) {
        if (conta.getDataAbertura().isAfter(LocalDate.now())) {
            return ResponseEntity.badRequest().body("A data de abertura não pode ser no futuro.");
        }
        if (conta.getSaldoInicial() < 0) {
            return ResponseEntity.badRequest().body("O saldo inicial não pode ser negativo.");
        }

        conta.setAtiva(true); // Conta nova deve começar ativa

        log.info("Cadastrando conta " + conta.getNumero());
        repository.save(conta);
        return ResponseEntity.status(HttpStatus.CREATED).body(conta);
    }

 
    @PutMapping("/{numero}/encerrar")
    public ResponseEntity<String> encerrarConta(@PathVariable Long numero) {
        Optional<Conta> contaOpt = repository.findById(numero);
        if (contaOpt.isPresent()) {
            Conta conta = contaOpt.get();
            conta.setAtiva(false);
            repository.save(conta);
            return ResponseEntity.ok("Conta encerrada com sucesso.");
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{numero}/depositar")
    public ResponseEntity<Object> depositar(@PathVariable Long numero, @RequestParam double valor) {
        if (valor <= 0) {
            return ResponseEntity.badRequest().body("O valor do depósito deve ser maior que zero.");
        }
    
        Optional<Conta> contaOpt = repository.findById(numero);
        if (contaOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
    
        Conta conta = contaOpt.get();
        if (!conta.isAtiva()) {
            return ResponseEntity.badRequest().body("Conta inativa. Não é possível depositar.");
        }
    
        conta.setSaldoInicial(conta.getSaldoInicial() + valor);
        repository.save(conta);
    
        return ResponseEntity.ok(conta);
    }
    
    
    @PutMapping("/{numero}/sacar")
    public ResponseEntity<Object> sacar(@PathVariable Long numero, @RequestParam double valor) {
        if (valor <= 0) {
            return ResponseEntity.badRequest().body("O valor do saque deve ser maior que zero.");
        }
    
        Optional<Conta> contaOpt = repository.findById(numero);
        if (contaOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
    
        Conta conta = contaOpt.get();
        if (!conta.isAtiva()) {
            return ResponseEntity.badRequest().body("Conta inativa. Não é possível sacar.");
        }
    
        if (conta.getSaldoInicial() < valor) {
            return ResponseEntity.badRequest().body("Saldo insuficiente.");
        }
    
        conta.setSaldoInicial(conta.getSaldoInicial() - valor);
        repository.save(conta);
    
        return ResponseEntity.ok(conta);
    }
       

 
    @PutMapping("/pix")
    public ResponseEntity<Object> transferirPix(
            @RequestParam Long origemNumero, 
            @RequestParam Long destinoNumero, 
            @RequestParam double valor) {
        
        if (valor <= 0) {
            return ResponseEntity.badRequest().body("O valor do PIX deve ser maior que zero.");
        }
    
        Optional<Conta> contaOrigemOpt = repository.findById(origemNumero);
        Optional<Conta> contaDestinoOpt = repository.findById(destinoNumero);
    
        if (contaOrigemOpt.isEmpty() || contaDestinoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
    
        Conta contaOrigem = contaOrigemOpt.get();
        Conta contaDestino = contaDestinoOpt.get();
    
        if (!contaOrigem.isAtiva() || !contaDestino.isAtiva()) {
            return ResponseEntity.badRequest().body("Uma das contas está inativa. PIX não permitido.");
        }
    
        if (contaOrigem.getSaldoInicial() < valor) {
            return ResponseEntity.badRequest().body("Saldo insuficiente para realizar o PIX.");
        }
    
        // Transferência de valores
        contaOrigem.setSaldoInicial(contaOrigem.getSaldoInicial() - valor);
        contaDestino.setSaldoInicial(contaDestino.getSaldoInicial() + valor);
    
        repository.save(contaOrigem);
        repository.save(contaDestino);
    
        return ResponseEntity.ok("PIX de R$ " + valor + " realizado com sucesso para a conta " + destinoNumero);
    }    
}
