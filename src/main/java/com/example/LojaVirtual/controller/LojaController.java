package com.example.LojaVirtual.controller;

import com.example.LojaVirtual.model.Produto;
import com.example.LojaVirtual.service.LojaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

@Controller
public class LojaController {

    @Autowired private LojaService service;

    public static Long USUARIO_ATUAL_ID = 1L; 

    @ModelAttribute
    public void addAttributes(Model model) {
        model.addAttribute("usuarioAtual", service.buscarUsuario(USUARIO_ATUAL_ID));
        model.addAttribute("todosUsuarios", service.listarUsuarios());
    }
    
    @GetMapping("/trocar-usuario/{id}")
    public String trocarUsuario(@PathVariable Long id) {
        USUARIO_ATUAL_ID = id;
        return "redirect:/";
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("produtos", service.listarProdutos());
        return "produtos/lista";
    }

    @GetMapping("/imagem/{id}")
    @ResponseBody
    public ResponseEntity<byte[]> exibirImagem(@PathVariable Long id) {
        Produto produto = service.buscarProduto(id);
        if (produto.getImagem() != null) {
            return ResponseEntity.ok()
                    .contentType(MediaType.valueOf(produto.getTipoImagem()))
                    .body(produto.getImagem());
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/produto/novo")
    public String novoProdutoForm(Model model) {
        model.addAttribute("produto", new Produto());
        return "produtos/formulario";
    }

    @PostMapping("/produto/salvar")
    public String salvarProduto(@ModelAttribute Produto produto, 
                                @RequestParam("imagemFile") MultipartFile file) throws IOException {
        
        if (produto.getId() != null) {
            Produto antigo = service.buscarProduto(produto.getId());
            produto.setVendedor(antigo.getVendedor());
        }
        
        service.salvarProduto(produto, USUARIO_ATUAL_ID, file);
        return "redirect:/";
    }

    @GetMapping("/produto/editar/{id}")
    public String editarProduto(@PathVariable Long id, Model model, RedirectAttributes redirect) {
        Produto p = service.buscarProduto(id);
        if (!p.getVendedor().getId().equals(USUARIO_ATUAL_ID)) {
            redirect.addFlashAttribute("erro", "Você só pode editar seus próprios produtos.");
            return "redirect:/";
        }
        model.addAttribute("produto", p);
        return "produtos/formulario";
    }

    @GetMapping("/produto/deletar/{id}")
    public String deletarProduto(@PathVariable Long id, RedirectAttributes redirect) {
        Produto p = service.buscarProduto(id);
        if (!p.getVendedor().getId().equals(USUARIO_ATUAL_ID)) {
            redirect.addFlashAttribute("erro", "Você só pode remover seus próprios produtos.");
            return "redirect:/";
        }
        service.deletarProduto(id);
        return "redirect:/";
    }

    @GetMapping("/carrinho")
    public String verCarrinho(Model model) {
        model.addAttribute("itens", service.listarCarrinho(USUARIO_ATUAL_ID));
        Double total = service.listarCarrinho(USUARIO_ATUAL_ID).stream()
            .mapToDouble(i -> i.getSubtotal())
            .sum();
        model.addAttribute("total", total);
        return "carrinho/lista";
    }

    @PostMapping("/carrinho/adicionar")
    public String adicionarAoCarrinho(@RequestParam Long produtoId, @RequestParam Integer quantidade, RedirectAttributes redirect) {
        try {
            service.adicionarAoCarrinho(produtoId, quantidade, USUARIO_ATUAL_ID);
            redirect.addFlashAttribute("sucesso", "Produto adicionado ao carrinho!");
        } catch (Exception e) {
            redirect.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/";
    }

    @PostMapping("/carrinho/atualizar")
    public String atualizarCarrinho(@RequestParam Long itemId, @RequestParam Integer quantidade) {
        service.atualizarQuantidadeCarrinho(itemId, quantidade);
        return "redirect:/carrinho";
    }

    @GetMapping("/carrinho/remover/{id}")
    public String removerDoCarrinho(@PathVariable Long id) {
        service.removerDoCarrinho(id);
        return "redirect:/carrinho";
    }

    // --- ROTA DE FINALIZAR COMPRA ---
    @PostMapping("/carrinho/finalizar")
    public String finalizarCompra(RedirectAttributes redirect) {
        try {
            service.finalizarCompra(USUARIO_ATUAL_ID);
            // Mensagem de sucesso
            redirect.addFlashAttribute("sucesso", "Compra realizada com sucesso! O estoque foi atualizado.");
            // REDIRECT PARA A PÁGINA PRINCIPAL
            return "redirect:/"; 
        } catch (Exception e) {
            // Em caso de erro, volta para o carrinho para o usuário ver o problema
            redirect.addFlashAttribute("erro", "Erro ao finalizar compra: " + e.getMessage());
            return "redirect:/carrinho";
        }
    }
}