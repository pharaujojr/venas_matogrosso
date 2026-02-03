package com.example.vendasjaragua.config;

import com.example.vendasjaragua.model.Produto;
import com.example.vendasjaragua.model.Time;
import com.example.vendasjaragua.model.Usuario;
import com.example.vendasjaragua.model.Vendedor;
import com.example.vendasjaragua.repository.ProdutoRepository;
import com.example.vendasjaragua.repository.TimeRepository;
import com.example.vendasjaragua.repository.UsuarioRepository;
import com.example.vendasjaragua.repository.VendedorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
@RequiredArgsConstructor
public class DataSeeder {

    private final TimeRepository timeRepository;
    private final VendedorRepository vendedorRepository;
    private final ProdutoRepository produtoRepository;
    private final UsuarioRepository usuarioRepository;

    @Bean
    public CommandLineRunner seedData() {
        return args -> {
            // Seed usuarios
            if (usuarioRepository.count() == 0) {
                Usuario admin = new Usuario("solturi", "Solturi2025.", "admin");
                Usuario andressa = new Usuario("andressa", "andressa", "vendas");
                usuarioRepository.saveAll(Arrays.asList(admin, andressa));
            }
            
            // Ensure default product exists
            if (produtoRepository.findByDescricao("Não Especificado").isEmpty()) {
                Produto p = new Produto();
                p.setDescricao("Não Especificado");
                p.setGrupo("Não Especificado");
                p.setUnidade("UN");
                produtoRepository.save(p);
            }

            if (timeRepository.count() == 0) {
                Time timeAlpha = new Time();
                timeAlpha.setNome("Alpha");
                timeAlpha.setLider("Roberto");
                timeAlpha = timeRepository.save(timeAlpha);

                Time timeBeta = new Time();
                timeBeta.setNome("Beta");
                timeBeta.setLider("Fernanda");
                timeBeta = timeRepository.save(timeBeta);

                Vendedor v1 = new Vendedor();
                v1.setNome("João Silva");
                v1.setTime(timeAlpha);

                Vendedor v2 = new Vendedor();
                v2.setNome("Maria Souza");
                v2.setTime(timeAlpha);

                Vendedor v3 = new Vendedor();
                v3.setNome("Carlos Pereira");
                v3.setTime(timeBeta);

                vendedorRepository.saveAll(Arrays.asList(v1, v2, v3));
            }
        };
    }
}
