package com.example.LojaVirtual.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class ItemCarrinho {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Produto produto;

    @ManyToOne
    private Usuario comprador;

    private Integer quantidade;

    public Double getSubtotal() {
        return produto.getPreco() * quantidade;
    }
}