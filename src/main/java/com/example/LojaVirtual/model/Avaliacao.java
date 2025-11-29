package com.example.LojaVirtual.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class Avaliacao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Produto produto;

    @ManyToOne
    private Usuario usuario;

    private Integer nota; // 1 a 5
    
    @Column(length = 500)
    private String comentario;

    private LocalDateTime dataAvaliacao = LocalDateTime.now();
}