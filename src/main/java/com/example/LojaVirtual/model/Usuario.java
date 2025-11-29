package com.example.LojaVirtual.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String nome;

    @Column(unique = true)
    private String email;

    private String senha;

    // --- NOVOS CAMPOS DE PERFIL ---
    private byte[] fotoPerfil;
    
    private String tipoFoto;

    // --- ROLE DO USUÁRIO ---
    @Enumerated(EnumType.STRING)
    private Role role; // ADMIN ou USER

    // Helper para verificar se é admin no HTML
    public boolean isAdmin() {
        return this.role == Role.ADMIN;
    }
}