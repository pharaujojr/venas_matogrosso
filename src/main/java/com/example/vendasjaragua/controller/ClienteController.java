package com.example.vendasjaragua.controller;

import com.example.vendasjaragua.model.Cliente;
import com.example.vendasjaragua.model.VendedorMatoGrosso;
import com.example.vendasjaragua.model.ProdutoMatoGrosso;
import com.example.vendasjaragua.model.Filial;
import com.example.vendasjaragua.repository.ClienteRepository;
import com.example.vendasjaragua.repository.VendedorMatoGrossoRepository;
import com.example.vendasjaragua.repository.ProdutoMatoGrossoRepository;
import com.example.vendasjaragua.repository.FilialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ClienteController {

    private final ClienteRepository clienteRepository;
    private final VendedorMatoGrossoRepository vendedorRepository;
    private final ProdutoMatoGrossoRepository produtoRepository;
    private final FilialRepository filialRepository;

    // ===========================
    // CLIENTES CRUD
    // ===========================

    @GetMapping
    public ResponseEntity<Page<Cliente>> getAllClientes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String filial,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String search
    ) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("dataCadastro").descending());
            Page<Cliente> clientes;

            // Se não informar filial, retorna vazio (obrigatório filtrar por filial)
            if (filial == null || filial.trim().isEmpty()) {
                return new ResponseEntity<>(Page.empty(pageable), HttpStatus.OK);
            }

            if (search != null && !search.trim().isEmpty()) {
                clientes = clienteRepository.searchClientesByFilial(filial, startDate, endDate, search.trim(), pageable);
            } else if (startDate != null && endDate != null) {
                clientes = clienteRepository.findByFilialAndDataBetween(filial, startDate, endDate, pageable);
            } else {
                clientes = clienteRepository.findByFilial(filial, pageable);
            }

            return new ResponseEntity<>(clientes, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cliente> getClienteById(@PathVariable Long id) {
        return clienteRepository.findById(id)
                .map(cliente -> new ResponseEntity<>(cliente, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<Cliente> createCliente(@RequestBody Cliente cliente) {
        try {
            Cliente novoCliente = clienteRepository.save(cliente);
            return new ResponseEntity<>(novoCliente, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cliente> updateCliente(@PathVariable Long id, @RequestBody Cliente cliente) {
        if (!clienteRepository.existsById(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        cliente.setId(id);
        try {
            Cliente updatedCliente = clienteRepository.save(cliente);
            return new ResponseEntity<>(updatedCliente, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCliente(@PathVariable Long id) {
        try {
            if (clienteRepository.existsById(id)) {
                clienteRepository.deleteById(id);
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ===========================
    // VENDEDORES CRUD
    // ===========================

    @GetMapping("/vendedores")
    public ResponseEntity<List<VendedorMatoGrosso>> getAllVendedores(
            @RequestParam(required = false) String filial,
            @RequestParam(required = false, defaultValue = "true") Boolean apenasAtivos
    ) {
        try {
            List<VendedorMatoGrosso> vendedores;
            
            if (filial != null && !filial.trim().isEmpty()) {
                Filial filialObj = filialRepository.findByNome(filial).orElse(null);
                if (filialObj == null) {
                    return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
                }
                vendedores = apenasAtivos 
                    ? vendedorRepository.findByFilialAndAtivoTrue(filialObj)
                    : vendedorRepository.findByFilial(filialObj);
            } else {
                vendedores = apenasAtivos
                    ? vendedorRepository.findByAtivoTrue()
                    : vendedorRepository.findAll();
            }
            
            return new ResponseEntity<>(vendedores, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/vendedores")
    public ResponseEntity<VendedorMatoGrosso> createVendedor(@RequestBody VendedorMatoGrosso vendedor) {
        try {
            return new ResponseEntity<>(vendedorRepository.save(vendedor), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/vendedores/{id}")
    public ResponseEntity<VendedorMatoGrosso> updateVendedor(
            @PathVariable Long id, 
            @RequestBody VendedorMatoGrosso vendedor
    ) {
        try {
            return vendedorRepository.findById(id)
                .map(existingVendedor -> {
                    existingVendedor.setNome(vendedor.getNome());
                    existingVendedor.setFilial(vendedor.getFilial());
                    existingVendedor.setAtivo(vendedor.getAtivo());
                    existingVendedor.setEmail(vendedor.getEmail());
                    existingVendedor.setTelefone(vendedor.getTelefone());
                    return new ResponseEntity<>(vendedorRepository.save(existingVendedor), HttpStatus.OK);
                })
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/vendedores/{id}")
    public ResponseEntity<HttpStatus> deleteVendedor(@PathVariable Long id) {
        try {
            vendedorRepository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ===========================
    // PRODUTOS CRUD
    // ===========================

    @GetMapping("/produtos")
    public ResponseEntity<List<ProdutoMatoGrosso>> getAllProdutos(
            @RequestParam(required = false) String grupo
    ) {
        try {
            List<ProdutoMatoGrosso> produtos;
            
            if (grupo != null && !grupo.trim().isEmpty()) {
                produtos = produtoRepository.findByGrupo(grupo);
            } else {
                produtos = produtoRepository.findAll();
            }
            
            return new ResponseEntity<>(produtos, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/produtos")
    public ResponseEntity<ProdutoMatoGrosso> createProduto(@RequestBody ProdutoMatoGrosso produto) {
        try {
            return new ResponseEntity<>(produtoRepository.save(produto), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/produtos/{id}")
    public ResponseEntity<ProdutoMatoGrosso> updateProduto(
            @PathVariable Long id, 
            @RequestBody ProdutoMatoGrosso produto
    ) {
        try {
            return produtoRepository.findById(id)
                .map(existingProduto -> {
                    existingProduto.setDescricao(produto.getDescricao());
                    existingProduto.setGrupo(produto.getGrupo());
                    existingProduto.setUnidade(produto.getUnidade());
                    return new ResponseEntity<>(produtoRepository.save(existingProduto), HttpStatus.OK);
                })
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/produtos/{id}")
    public ResponseEntity<HttpStatus> deleteProduto(@PathVariable Long id) {
        try {
            produtoRepository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ===========================
    // FILIAIS
    // ===========================

    @GetMapping("/filiais")
    public ResponseEntity<List<String>> getFiliais() {
        try {
            List<String> filiais = clienteRepository.findDistinctFiliais();
            return new ResponseEntity<>(filiais, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ===========================
    // DASHBOARD E ESTATÍSTICAS
    // ===========================

    @GetMapping("/dashboard/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats(
            @RequestParam String filial,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim
    ) {
        if (inicio == null) inicio = LocalDate.of(2000, 1, 1);
        if (fim == null) fim = LocalDate.now().plusYears(100);

        try {
            Long count = clienteRepository.countByFilialAndDataBetween(filial, inicio, fim);
            Double totalDebito = clienteRepository.sumValorDebitoByFilialAndDataBetween(filial, inicio, fim);
            Double totalPago = clienteRepository.sumValorPagoByFilialAndDataBetween(filial, inicio, fim);

            Map<String, Object> stats = new HashMap<>();
            stats.put("quantidade", count != null ? count : 0);
            stats.put("totalDebito", totalDebito != null ? totalDebito : 0.0);
            stats.put("totalPago", totalPago != null ? totalPago : 0.0);
            stats.put("saldo", (totalDebito != null ? totalDebito : 0.0) - (totalPago != null ? totalPago : 0.0));
            stats.put("ticketMedio", count != null && count > 0 ? (totalDebito / count) : 0.0);

            return new ResponseEntity<>(stats, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/dashboard/mensal")
    public ResponseEntity<List<Object[]>> getDashboardMensal(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(required = false) List<String> filiais,
            @RequestParam(required = false) List<String> vendedores
    ) {
        if (inicio == null) inicio = LocalDate.of(2000, 1, 1);
        if (fim == null) fim = LocalDate.now().plusYears(100);

        try {
            return new ResponseEntity<>(
                clienteRepository.findVendasPorMesEFilial(inicio, fim, filiais, vendedores), 
                HttpStatus.OK
            );
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/dashboard/vendedores")
    public ResponseEntity<List<Object[]>> getDashboardVendedores(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(required = false) List<String> filiais,
            @RequestParam(required = false) List<String> vendedores
    ) {
        if (inicio == null) inicio = LocalDate.of(2000, 1, 1);
        if (fim == null) fim = LocalDate.now().plusYears(100);

        try {
            return new ResponseEntity<>(
                clienteRepository.findVendasPorVendedorEFilial(inicio, fim, filiais, vendedores), 
                HttpStatus.OK
            );
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/grupos")
    public ResponseEntity<List<String>> getGruposProdutos() {
        try {
            return new ResponseEntity<>(produtoRepository.findDistinctGrupos(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
