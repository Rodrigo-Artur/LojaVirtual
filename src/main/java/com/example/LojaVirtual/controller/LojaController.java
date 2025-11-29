package com.example.LojaVirtual.controller;

import com.example.LojaVirtual.model.Produto;
import com.example.LojaVirtual.model.Usuario;
import com.example.LojaVirtual.service.LojaService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class LojaController {

    @Autowired private LojaService service;

    private Usuario getUsuarioLogado(HttpSession session) {
        return (Usuario) session.getAttribute("usuarioLogado");
    }

    @ModelAttribute
    public void addAttributes(Model model, HttpSession session) {
        Usuario u = getUsuarioLogado(session);
        if (u != null) {
            model.addAttribute("usuarioAtual", u);
        }
    }

    // --- HOME (LISTA DE PRODUTOS) ---
    @GetMapping("/")
    public String index(@RequestParam(required = false) String termo,
                        @RequestParam(required = false) Long categoriaId,
                        Model model, HttpSession session) {
        if (getUsuarioLogado(session) == null) return "redirect:/login";
        
        // Passa as categorias para o dropdown de filtro
        model.addAttribute("categorias", service.listarCategorias());
        model.addAttribute("termoAtual", termo);
        model.addAttribute("categoriaAtual", categoriaId);

        // Busca filtrada
        List<Produto> produtos = service.listarProdutos(termo, categoriaId);
        model.addAttribute("produtos", produtos);

        Map<Long, Double> medias = new HashMap<>();
        for (Produto p : produtos) {
            medias.put(p.getId(), service.calcularMediaAvaliacoes(p));
        }
        model.addAttribute("medias", medias);

        return "produtos/lista";
    }

    // --- DETALHES DO PRODUTO ---
    @GetMapping("/produto/{id}")
    public String verDetalhesProduto(@PathVariable Long id, Model model, HttpSession session) {
        Usuario u = getUsuarioLogado(session);
        if (u == null) return "redirect:/login";
        
        try {
            Produto produto = service.buscarProduto(id);
            model.addAttribute("produto", produto);
            
            boolean estaNosDesejos = service.estaNaListaDeDesejos(u, produto);
            model.addAttribute("estaNosDesejos", estaNosDesejos);

            model.addAttribute("avaliacoes", service.listarAvaliacoes(produto));
            model.addAttribute("mediaAvaliacoes", service.calcularMediaAvaliacoes(produto));
            model.addAttribute("podeAvaliar", service.podeAvaliar(u, produto));
            
            return "produtos/detalhe";
        } catch (Exception e) {
            return "redirect:/";
        }
    }
    
    @PostMapping("/produto/{id}/avaliar")
    public String avaliarProduto(@PathVariable Long id, 
                                 @RequestParam Integer nota, 
                                 @RequestParam String comentario, 
                                 HttpSession session, 
                                 RedirectAttributes redirect) {
        Usuario u = getUsuarioLogado(session);
        if (u == null) return "redirect:/login";
        
        try {
            service.salvarAvaliacao(id, nota, comentario, u);
            redirect.addFlashAttribute("sucesso", "Avaliação enviada com sucesso!");
        } catch (Exception e) {
            redirect.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/produto/" + id;
    }

    // --- HISTÓRICO ---
    @GetMapping("/historico")
    public String verHistorico(Model model, HttpSession session) {
        Usuario u = getUsuarioLogado(session);
        if (u == null) return "redirect:/login";
        
        model.addAttribute("compras", service.listarHistorico(u));
        return "usuario/historico";
    }

    // --- OUTROS MÉTODOS (IMAGEM, PERFIL, CRUD, CARRINHO) MANTIDOS IGUAIS ---

    @GetMapping("/imagem/{id}")
    @ResponseBody
    public ResponseEntity<byte[]> exibirImagemProduto(@PathVariable Long id) {
        Produto produto = service.buscarProduto(id);
        if (produto.getImagem() != null) {
            return ResponseEntity.ok()
                    .contentType(MediaType.valueOf(produto.getTipoImagem()))
                    .body(produto.getImagem());
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/usuario/foto/{id}")
    @ResponseBody
    public ResponseEntity<byte[]> exibirFotoPerfil(@PathVariable Long id) {
        Usuario usuario = service.buscarUsuario(id);
        if (usuario.getFotoPerfil() != null) {
            return ResponseEntity.ok()
                    .contentType(MediaType.valueOf(usuario.getTipoFoto()))
                    .body(usuario.getFotoPerfil());
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/perfil")
    public String paginaPerfil(Model model, HttpSession session) {
        Usuario u = getUsuarioLogado(session);
        if (u == null) return "redirect:/login";
        Usuario usuarioAtualizado = service.buscarUsuario(u.getId());
        model.addAttribute("usuario", usuarioAtualizado);
        return "usuario/perfil";
    }

    @PostMapping("/perfil/atualizar")
    public String atualizarPerfil(@RequestParam String nome,
                                  @RequestParam String email,
                                  @RequestParam(required = false) String senha,
                                  @RequestParam("foto") MultipartFile foto,
                                  HttpSession session,
                                  RedirectAttributes redirect) {
        Usuario u = getUsuarioLogado(session);
        if (u == null) return "redirect:/login";
        try {
            Usuario usuarioAtualizado = service.atualizarPerfil(u.getId(), nome, email, senha, foto);
            session.setAttribute("usuarioLogado", usuarioAtualizado);
            redirect.addFlashAttribute("sucesso", "Perfil atualizado com sucesso!");
        } catch (Exception e) {
            redirect.addFlashAttribute("erro", "Erro ao atualizar perfil: " + e.getMessage());
        }
        return "redirect:/perfil";
    }

    @GetMapping("/desejos")
    public String verListaDesejos(Model model, HttpSession session) {
        Usuario u = getUsuarioLogado(session);
        if (u == null) return "redirect:/login";
        model.addAttribute("itensDesejo", service.listarDesejos(u));
        return "usuario/desejos";
    }

    @PostMapping("/desejos/toggle")
    public String alternarDesejo(@RequestParam Long produtoId, HttpSession session, RedirectAttributes redirect) {
        Usuario u = getUsuarioLogado(session);
        if (u == null) return "redirect:/login";
        try {
            service.alternarDesejo(produtoId, u);
        } catch (Exception e) {
            redirect.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/produto/" + produtoId;
    }

    @GetMapping("/desejos/remover/{id}")
    public String removerDesejo(@PathVariable Long id, HttpSession session) {
        if (getUsuarioLogado(session) == null) return "redirect:/login";
        service.removerDesejo(id);
        return "redirect:/desejos";
    }

    @GetMapping("/produto/novo")
    public String novoProdutoForm(Model model, HttpSession session) {
        if (getUsuarioLogado(session) == null) return "redirect:/login";
        model.addAttribute("produto", new Produto());
        // Envia categorias para o select do form
        model.addAttribute("categorias", service.listarCategorias());
        return "produtos/formulario";
    }

    @PostMapping("/produto/salvar")
    public String salvarProduto(@ModelAttribute Produto produto, 
                                @RequestParam("imagemFile") MultipartFile file,
                                HttpSession session) throws IOException {
        Usuario u = getUsuarioLogado(session);
        if (u == null) return "redirect:/login";
        
        if (produto.getId() != null) {
            Produto antigo = service.buscarProduto(produto.getId());
            produto.setVendedor(antigo.getVendedor());
            // Mantem categoria antiga se vier nula (opcional)
            if(produto.getCategoria() == null) produto.setCategoria(antigo.getCategoria());
        }
        
        service.salvarProduto(produto, u, file);
        return "redirect:/";
    }

    @GetMapping("/produto/editar/{id}")
    public String editarProduto(@PathVariable Long id, Model model, RedirectAttributes redirect, HttpSession session) {
        Usuario u = getUsuarioLogado(session);
        if (u == null) return "redirect:/login";
        Produto p = service.buscarProduto(id);
        if (!p.getVendedor().getId().equals(u.getId())) {
            redirect.addFlashAttribute("erro", "Sem permissão.");
            return "redirect:/";
        }
        model.addAttribute("produto", p);
        model.addAttribute("categorias", service.listarCategorias());
        return "produtos/formulario";
    }

    @GetMapping("/produto/deletar/{id}")
    public String deletarProduto(@PathVariable Long id, RedirectAttributes redirect, HttpSession session) {
        Usuario u = getUsuarioLogado(session);
        if (u == null) return "redirect:/login";
        Produto p = service.buscarProduto(id);
        if (!p.getVendedor().getId().equals(u.getId())) {
            redirect.addFlashAttribute("erro", "Você só pode remover seus próprios produtos.");
            return "redirect:/";
        }
        service.deletarProduto(id);
        return "redirect:/";
    }

    @GetMapping("/carrinho")
    public String verCarrinho(Model model, HttpSession session) {
        Usuario u = getUsuarioLogado(session);
        if (u == null) return "redirect:/login";
        model.addAttribute("itens", service.listarCarrinho(u));
        Double total = service.listarCarrinho(u).stream().mapToDouble(i -> i.getSubtotal()).sum();
        model.addAttribute("total", total);
        return "carrinho/lista";
    }

    @PostMapping("/carrinho/adicionar")
    public String adicionarAoCarrinho(@RequestParam Long produtoId, @RequestParam Integer quantidade, RedirectAttributes redirect, HttpSession session) {
        Usuario u = getUsuarioLogado(session);
        if (u == null) return "redirect:/login";
        try {
            service.adicionarAoCarrinho(produtoId, quantidade, u);
            redirect.addFlashAttribute("sucesso", "Produto adicionado ao carrinho!");
        } catch (Exception e) {
            redirect.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/produto/" + produtoId;
    }

    @PostMapping("/carrinho/atualizar")
    public String atualizarCarrinho(@RequestParam Long itemId, @RequestParam Integer quantidade, HttpSession session) {
        if (getUsuarioLogado(session) == null) return "redirect:/login";
        service.atualizarQuantidadeCarrinho(itemId, quantidade);
        return "redirect:/carrinho";
    }

    @GetMapping("/carrinho/remover/{id}")
    public String removerDoCarrinho(@PathVariable Long id, HttpSession session) {
        if (getUsuarioLogado(session) == null) return "redirect:/login";
        service.removerDoCarrinho(id);
        return "redirect:/carrinho";
    }

    @PostMapping("/carrinho/finalizar")
    public String finalizarCompra(RedirectAttributes redirect, HttpSession session) {
        Usuario u = getUsuarioLogado(session);
        if (u == null) return "redirect:/login";
        try {
            service.finalizarCompra(u);
            redirect.addFlashAttribute("sucesso", "Compra realizada com sucesso! Verifique seu histórico.");
            return "redirect:/historico";
        } catch (Exception e) {
            redirect.addFlashAttribute("erro", "Erro ao finalizar compra: " + e.getMessage());
            return "redirect:/carrinho";
        }
    }
    
}