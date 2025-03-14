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
 
    // Buscar todas as contas
    @GetMapping
    public List<Conta> index() {
        return repository.findAll();
    }
 
    // Buscar conta por Número da Conta (antigo ID)
    @GetMapping("/{numero}")
    public ResponseEntity<Conta> findByNumero(@PathVariable Long numero) {
        return repository.findById(numero)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
 
    // Buscar conta por CPF
    @GetMapping("/cpf/{cpf}")
    public ResponseEntity<Conta> findByCpf(@PathVariable String cpf) {
        Optional<Conta> conta = repository.findAll().stream()
                .filter(c -> c.getCpf().equals(cpf))
                .findFirst();
        return conta.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
 
    // Criar conta
    @PostMapping("cadastrar")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Object> create(@Valid @RequestBody Conta conta) {
        // Validações
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
}
 
    // Encerrar Conta
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
 
    // Depósito
    @PutMapping("/{numero}/depositar")
    public ResponseEntity<Object> depositar(@PathVariable Long numero, @RequestParam double valor) {
        if (valor <= 0) {
            return ResponseEntity.badRequest().body("O valor do depósito deve ser maior que zero.");
        }
        return repository.findById(numero)
                .map(conta -> {
                    if (!conta.isAtiva()) {
                        return ResponseEntity.badRequest().body("Conta inativa. Não é possível depositar.");
                    }
                    conta.setSaldoInicial(conta.getSaldoInicial() + valor);
                    repository.save(conta);
                    return ResponseEntity.ok(conta);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
