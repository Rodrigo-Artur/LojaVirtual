package com.example.LojaVirtual.config;

import com.example.LojaVirtual.model.Usuario;
import com.example.LojaVirtual.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UsuarioRepository usuarioRepo;

    @Override
    public void run(String... args) throws Exception {
        if (usuarioRepo.count() == 0) {
            Usuario u1 = new Usuario();
            u1.setNome("João Vendedor");
            usuarioRepo.save(u1);

            Usuario u2 = new Usuario();
            u2.setNome("Maria Compradora");
            usuarioRepo.save(u2);
            
            System.out.println("--- USUÁRIOS DE TESTE CRIADOS ---");
        }
    }
}