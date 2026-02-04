package com.example.vendasjaragua.controller;

import com.example.vendasjaragua.model.MetaMensal;
import com.example.vendasjaragua.model.Filial;
import com.example.vendasjaragua.repository.MetaMensalRepository;
import com.example.vendasjaragua.repository.FilialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/metas")
public class MetaMensalController {
    
    @Autowired
    private MetaMensalRepository metaMensalRepository;
    
    @Autowired
    private FilialRepository filialRepository;
    
    @GetMapping("/{mes}/{ano}")
    public ResponseEntity<List<Map<String, Object>>> getMetasByMesAno(
            @PathVariable Integer mes, 
            @PathVariable Integer ano) {
        
        List<MetaMensal> metas = metaMensalRepository.findByMesAndAno(mes, ano);
        List<Map<String, Object>> response = new java.util.ArrayList<>();
        
        for (MetaMensal meta : metas) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", meta.getId());
            item.put("filialId", meta.getFilial().getId());
            item.put("filialNome", meta.getFilial().getNome());
            item.put("mes", meta.getMes());
            item.put("ano", meta.getAno());
            item.put("metaValor", meta.getMetaValor());
            response.add(item);
        }
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/salvar")
    public ResponseEntity<Map<String, String>> salvarMetas(@RequestBody Map<String, Object> payload) {
        try {
            Integer mes = (Integer) payload.get("mes");
            Integer ano = (Integer) payload.get("ano");
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> metas = (List<Map<String, Object>>) payload.get("metas");
            
            for (Map<String, Object> metaData : metas) {
                Long filialId = ((Number) metaData.get("filialId")).longValue();
                BigDecimal metaValor = new BigDecimal(metaData.get("metaValor").toString());
                
                Filial filial = filialRepository.findById(filialId)
                        .orElseThrow(() -> new RuntimeException("Filial não encontrada"));
                
                MetaMensal meta = metaMensalRepository.findByFilialAndMesAndAno(filial, mes, ano)
                        .orElse(new MetaMensal());
                
                meta.setFilial(filial);
                meta.setMes(mes);
                meta.setAno(ano);
                meta.setMetaValor(metaValor);
                
                metaMensalRepository.save(meta);
            }
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Metas salvas com sucesso");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erro ao salvar metas: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
