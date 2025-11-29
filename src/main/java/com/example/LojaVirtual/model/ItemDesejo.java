package com.example.LojaVirtual.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class ItemDesejo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "produto_id")
    private Produto produto;
}