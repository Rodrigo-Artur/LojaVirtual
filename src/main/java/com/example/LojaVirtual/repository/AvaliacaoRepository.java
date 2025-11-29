package com.example.LojaVirtual.repository;

import com.example.LojaVirtual.model.*; // Importa todas as models
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {
    List<Avaliacao> findByProdutoOrderByDataAvaliacaoDesc(Produto produto);
    boolean existsByUsuarioAndProduto(Usuario usuario, Produto produto);
}