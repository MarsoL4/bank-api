package br.com.fiap.bank_api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class IntegrantesController {
    @GetMapping
    public String index(){
        return "Cauan da Cruz Ferreira, 558238 / Enzo Giuseppe Marsola, 556310";
    }
}
