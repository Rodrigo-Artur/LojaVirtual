package com.example.LojaVirtual.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class ItemCompra {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "compra_id")
    private Compra compra;

    @ManyToOne
    @JoinColumn(name = "produto_id")
    private Produto produto;

    private Integer quantidade;
    
    // Salvamos o preço no momento da compra, pois o preço do produto pode mudar depois
    private Double precoUnitarioSnapshot;

    public Double getSubtotal() {
        return precoUnitarioSnapshot * quantidade;
    }
}