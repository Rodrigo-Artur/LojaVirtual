package com.example.LojaVirtual.repository;

import com.example.LojaVirtual.model.*; // Importa todas as models
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemCompraRepository extends JpaRepository<ItemCompra, Long> {
    // Verifica se o usuário já comprou este produto alguma vez
    boolean existsByCompraUsuarioAndProduto(Usuario usuario, Produto produto);
}