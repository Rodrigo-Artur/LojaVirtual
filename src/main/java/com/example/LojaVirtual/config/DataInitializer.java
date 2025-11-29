package com.example.LojaVirtual.config;

import com.example.LojaVirtual.model.Categoria;
import com.example.LojaVirtual.model.Role;
import com.example.LojaVirtual.model.Usuario;
import com.example.LojaVirtual.repository.CategoriaRepository;
import com.example.LojaVirtual.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired private UsuarioRepository usuarioRepo;
    @Autowired private CategoriaRepository categoriaRepo;

    @Override
    public void run(String... args) throws Exception {
        
        if (categoriaRepo.count() == 0) {
            Categoria c1 = new Categoria(); c1.setNome("Eletrônicos");
            Categoria c2 = new Categoria(); c2.setNome("Roupas");
            Categoria c3 = new Categoria(); c3.setNome("Livros");
            Categoria c4 = new Categoria(); c4.setNome("Casa");
            categoriaRepo.saveAll(Arrays.asList(c1, c2, c3, c4));
            System.out.println("--- CATEGORIAS PADRÃO CRIADAS ---");
        }

        if (usuarioRepo.findByEmail("admin@loja.com").isEmpty()) {
            Usuario admin = new Usuario();
            admin.setNome("Administrador Principal");
            admin.setEmail("admin@loja.com");
            admin.setSenha("admin123");
            admin.setRole(Role.ADMIN);
            // IMPORTANTE: Definir valores nulos ou vazios para bytea se não tiver foto
            admin.setFotoPerfil(null); 
            admin.setTipoFoto(null);
            usuarioRepo.save(admin);
            System.out.println("--- USUÁRIO ADMIN CRIADO (admin@loja.com / admin123) ---");
        }
    }
}