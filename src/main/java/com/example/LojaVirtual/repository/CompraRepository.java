package com.example.LojaVirtual.repository;

import com.example.LojaVirtual.model.Compra;
import com.example.LojaVirtual.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompraRepository extends JpaRepository<Compra, Long> {
    List<Compra> findByUsuarioOrderByDataCompraDesc(Usuario usuario);
}