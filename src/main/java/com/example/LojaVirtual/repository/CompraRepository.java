package com.example.LojaVirtual.repository;

import com.example.LojaVirtual.model.*; // Importa todas as models
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompraRepository extends JpaRepository<Compra, Long> {
    List<Compra> findByUsuarioOrderByDataCompraDesc(Usuario usuario);
}
