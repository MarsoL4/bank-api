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

import br.com.fiap.bank_api.dto.PixRequest;
import br.com.fiap.bank_api.model.Conta;
import br.com.fiap.bank_api.repository.ContaRepository;
import jakarta.validation.Valid;

@RestController
@RequestMapping("contas")
public class ContaController {
    
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private ContaRepository repository;

    @GetMapping
    public List<Conta> index() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Conta> findById(@PathVariable Long id) {
        return repository.findById(id)
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

        conta.setAtiva("S"); // Conta sempre começa ativa com "S"

        log.info("Cadastrando conta número " + conta.getNumero());
        repository.save(conta);
        return ResponseEntity.status(HttpStatus.CREATED).body(conta);
    }

    @PutMapping("/{id}/encerrar")
    public ResponseEntity<String> encerrarConta(@PathVariable Long id) {
        Optional<Conta> contaOpt = repository.findById(id);
        if (contaOpt.isPresent()) {
            Conta conta = contaOpt.get();
            conta.setAtiva("N"); // Conta encerrada recebe "N"
            repository.save(conta);
            return ResponseEntity.ok("Conta encerrada com sucesso.");
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}/depositar")
    public ResponseEntity<Object> depositar(@PathVariable Long id, @RequestBody double valor) {
        if (valor <= 0) {
            return ResponseEntity.badRequest().body("O valor do depósito deve ser maior que zero.");
        }

        Optional<Conta> contaOpt = repository.findById(id);
        if (contaOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Conta conta = contaOpt.get();
        if (!conta.getAtiva().equals("S")) {
            return ResponseEntity.badRequest().body("Conta inativa. Não é possível depositar.");
        }

        conta.setSaldoInicial(conta.getSaldoInicial() + valor);
        repository.save(conta);

        return ResponseEntity.ok(conta);
    }

    @PutMapping("/{id}/sacar")
    public ResponseEntity<Object> sacar(@PathVariable Long id, @RequestBody double valor) {
        if (valor <= 0) {
            return ResponseEntity.badRequest().body("O valor do saque deve ser maior que zero.");
        }

        Optional<Conta> contaOpt = repository.findById(id);
        if (contaOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Conta conta = contaOpt.get();
        if (!conta.getAtiva().equals("S")) {
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
    public ResponseEntity<Object> transferirPix(@RequestBody PixRequest request) {
        
        if (request.getValor() <= 0) {
            return ResponseEntity.badRequest().body("O valor do PIX deve ser maior que zero.");
        }

        Optional<Conta> contaOrigemOpt = repository.findById(request.getOrigemId());
        Optional<Conta> contaDestinoOpt = repository.findById(request.getDestinoId());

        if (contaOrigemOpt.isEmpty() || contaDestinoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Conta contaOrigem = contaOrigemOpt.get();
        Conta contaDestino = contaDestinoOpt.get();

        if (!contaOrigem.getAtiva().equals("S") || !contaDestino.getAtiva().equals("S")) {
            return ResponseEntity.badRequest().body("Uma das contas está inativa. PIX não permitido.");
        }

        if (contaOrigem.getSaldoInicial() < request.getValor()) {
            return ResponseEntity.badRequest().body("Saldo insuficiente para realizar o PIX.");
        }

        contaOrigem.setSaldoInicial(contaOrigem.getSaldoInicial() - request.getValor());
        contaDestino.setSaldoInicial(contaDestino.getSaldoInicial() + request.getValor());

        repository.save(contaOrigem);
        repository.save(contaDestino);

        return ResponseEntity.ok("PIX de R$ " + request.getValor() + " realizado com sucesso para a conta " + request.getDestinoId());
    }
}