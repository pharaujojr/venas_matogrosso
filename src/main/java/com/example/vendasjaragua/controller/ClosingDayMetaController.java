package com.example.vendasjaragua.controller;

import com.example.vendasjaragua.model.ClosingDayMeta;
import com.example.vendasjaragua.repository.ClosingDayMetaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/closing-day")
public class ClosingDayMetaController {
    
    @Autowired
    private ClosingDayMetaRepository metaRepository;
    
    @GetMapping("/metas")
    public ResponseEntity<List<ClosingDayMeta>> getAllMetas() {
        return ResponseEntity.ok(metaRepository.findAll());
    }
    
    @PutMapping("/metas")
    public ResponseEntity<Void> saveMetas(@RequestBody Map<String, Object> metasMap) {
        metasMap.forEach((filialNome, metaValor) -> {
            ClosingDayMeta meta = metaRepository.findByFilialNome(filialNome)
                    .orElse(new ClosingDayMeta());
            meta.setFilialNome(filialNome);
            
            // Convert metaValor to BigDecimal
            if (metaValor instanceof Number) {
                meta.setMetaValor(new java.math.BigDecimal(metaValor.toString()));
            }
            
            metaRepository.save(meta);
        });
        
        return ResponseEntity.ok().build();
    }
}
