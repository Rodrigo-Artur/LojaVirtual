package com.example.LojaVirtual.repository;

import com.example.LojaVirtual.model.ItemCarrinho;
import com.example.LojaVirtual.model.Produto;
import com.example.LojaVirtual.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemCarrinhoRepository extends JpaRepository<ItemCarrinho, Long> {
    List<ItemCarrinho> findByComprador(Usuario comprador);
    Optional<ItemCarrinho> findByCompradorAndProduto(Usuario comprador, Produto produto);
}