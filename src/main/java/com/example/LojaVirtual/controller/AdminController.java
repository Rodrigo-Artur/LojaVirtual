package com.example.LojaVirtual.controller;

import com.example.LojaVirtual.model.Usuario;
import com.example.LojaVirtual.service.LojaService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired private LojaService service;

    private Usuario getUsuarioLogado(HttpSession session) {
        return (Usuario) session.getAttribute("usuarioLogado");
    }

    // Middleware simples para checar se é admin
    private boolean isAdmin(HttpSession session) {
        Usuario u = getUsuarioLogado(session);
        return u != null && u.getRole().toString().equals("ADMIN");
    }

    @GetMapping("/categorias")
    public String gerenciarCategorias(Model model, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/";
        model.addAttribute("categorias", service.listarCategorias());
        return "admin/categorias";
    }

    @PostMapping("/categorias/nova")
    public String novaCategoria(@RequestParam String nome, HttpSession session, RedirectAttributes redirect) {
        if (!isAdmin(session)) return "redirect:/";
        try {
            service.criarCategoria(nome, getUsuarioLogado(session));
            redirect.addFlashAttribute("sucesso", "Categoria criada!");
        } catch (Exception e) {
            redirect.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/admin/categorias";
    }

    @GetMapping("/novo-admin")
    public String novoAdminForm(HttpSession session) {
        if (!isAdmin(session)) return "redirect:/";
        return "admin/registro-admin";
    }

    @PostMapping("/novo-admin")
    public String salvarAdmin(Usuario novoAdmin, HttpSession session, RedirectAttributes redirect) {
        if (!isAdmin(session)) return "redirect:/";
        try {
            service.cadastrarAdmin(novoAdmin, getUsuarioLogado(session));
            redirect.addFlashAttribute("sucesso", "Novo administrador criado com sucesso!");
        } catch (Exception e) {
            redirect.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/admin/novo-admin";
    }
}