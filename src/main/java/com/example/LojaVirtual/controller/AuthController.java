package com.example.LojaVirtual.controller;

import com.example.LojaVirtual.model.Usuario;
import com.example.LojaVirtual.service.LojaService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    @Autowired
    private LojaService service;

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @PostMapping("/login")
    public String realizarLogin(@RequestParam String email, 
                                @RequestParam String senha, 
                                HttpSession session, 
                                RedirectAttributes redirect) {
        try {
            Usuario usuario = service.autenticar(email, senha);
            session.setAttribute("usuarioLogado", usuario);
            return "redirect:/";
        } catch (Exception e) {
            redirect.addFlashAttribute("erro", e.getMessage());
            return "redirect:/login";
        }
    }

    @GetMapping("/registro")
    public String registroPage(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "auth/registro";
    }

    @PostMapping("/registro")
    public String realizarRegistro(Usuario usuario, RedirectAttributes redirect) {
        try {
            service.cadastrarUsuario(usuario);
            redirect.addFlashAttribute("sucesso", "Cadastro realizado! Faça login.");
            return "redirect:/login";
        } catch (Exception e) {
            redirect.addFlashAttribute("erro", e.getMessage());
            return "redirect:/registro";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}