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

    // --- NOVOS CAMPOS PARA FOTO DE PERFIL ---
    @Lob
    private byte[] fotoPerfil;
    
    private String tipoFoto; // ex: image/jpeg
}