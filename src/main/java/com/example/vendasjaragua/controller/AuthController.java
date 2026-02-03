package com.example.vendasjaragua.controller;

import com.example.vendasjaragua.model.Usuario;
import com.example.vendasjaragua.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");
        
        Optional<Usuario> usuario = usuarioRepository.findByUsername(username);
        
        Map<String, Object> response = new HashMap<>();
        
        if (usuario.isPresent() && usuario.get().getPassword().equals(password)) {
            response.put("success", true);
            response.put("role", usuario.get().getRole());
            response.put("username", usuario.get().getUsername());
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Usuário ou senha incorretos");
            return ResponseEntity.status(401).body(response);
        }
    }
}
