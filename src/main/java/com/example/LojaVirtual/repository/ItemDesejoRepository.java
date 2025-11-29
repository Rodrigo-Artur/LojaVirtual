package com.example.LojaVirtual.repository;

import com.example.LojaVirtual.model.ItemCarrinho;
import com.example.LojaVirtual.model.ItemDesejo;
import com.example.LojaVirtual.model.Produto;
import com.example.LojaVirtual.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemDesejoRepository extends JpaRepository<ItemDesejo, Long> {
    List<ItemDesejo> findByUsuario(Usuario usuario);
    Optional<ItemDesejo> findByUsuarioAndProduto(Usuario usuario, Produto produto);
    boolean existsByUsuarioAndProduto(Usuario usuario, Produto produto);
}