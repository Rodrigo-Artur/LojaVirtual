package com.example.LojaVirtual.repository;

import com.example.LojaVirtual.model.Categoria;
import com.example.LojaVirtual.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    List<Produto> findAllByOrderByIdDesc();

    // Busca por Nome (ignorando maiusculas/minusculas)
    List<Produto> findByNomeContainingIgnoreCase(String nome);

    // Busca por Categoria
    List<Produto> findByCategoria(Categoria categoria);

    // Busca por Nome E Categoria
    List<Produto> findByCategoriaAndNomeContainingIgnoreCase(Categoria categoria, String nome);
}