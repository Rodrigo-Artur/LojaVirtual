package com.example.LojaVirtual.repository;

import com.example.LojaVirtual.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;




@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {}
