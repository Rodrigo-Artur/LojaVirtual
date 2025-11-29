package com.example.LojaVirtual.repository;

import com.example.LojaVirtual.model.Avaliacao;
import com.example.LojaVirtual.model.Produto;
import com.example.LojaVirtual.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {
    List<Avaliacao> findByProdutoOrderByDataAvaliacaoDesc(Produto produto);
    boolean existsByUsuarioAndProduto(Usuario usuario, Produto produto);
}