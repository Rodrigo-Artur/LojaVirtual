package com.example.LojaVirtual.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Entity
@Data
public class Produto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome é obrigatório")
    private String nome;

    private String descricao;

    @NotNull(message = "O preço é obrigatório")
    @Min(value = 0, message = "O preço deve ser positivo")
    private Double preco;

    @NotNull
    @Min(0)
    private Integer quantidadeEstoque;

    @ManyToOne
    @JoinColumn(name = "vendedor_id")
    private Usuario vendedor;
    
    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    private byte[] imagem;
    
    private String tipoImagem;
}