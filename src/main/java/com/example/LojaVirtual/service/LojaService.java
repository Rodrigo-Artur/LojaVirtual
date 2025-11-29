package com.example.LojaVirtual.service;

import com.example.LojaVirtual.model.ItemCarrinho;
import com.example.LojaVirtual.model.Produto;
import com.example.LojaVirtual.model.Usuario;
import com.example.LojaVirtual.repository.ItemCarrinhoRepository;
import com.example.LojaVirtual.repository.ProdutoRepository;
import com.example.LojaVirtual.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class LojaService {

    @Autowired private ProdutoRepository produtoRepo;
    @Autowired private ItemCarrinhoRepository carrinhoRepo;
    @Autowired private UsuarioRepository usuarioRepo;

    // --- Autenticação e Usuário ---
    
    // CORREÇÃO: Adicionado @Transactional aqui para permitir leitura da foto (LOB)
    @Transactional(readOnly = true)
    public Usuario buscarUsuario(Long id) {
        return usuarioRepo.findById(id).orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }
    
    // CORREÇÃO: Adicionado @Transactional aqui também, pois o login carrega o usuário e sua foto
    @Transactional(readOnly = true)
    public Usuario autenticar(String email, String senha) throws Exception {
        return usuarioRepo.findByEmailAndSenha(email, senha)
                .orElseThrow(() -> new Exception("Email ou senha inválidos"));
    }

    public void cadastrarUsuario(Usuario usuario) throws Exception {
        if (usuarioRepo.findByEmail(usuario.getEmail()).isPresent()) {
            throw new Exception("Este email já está cadastrado.");
        }
        usuarioRepo.save(usuario);
    }

    @Transactional
    public Usuario atualizarPerfil(Long usuarioId, String nome, String email, String senha, MultipartFile foto) throws Exception {
        Usuario usuario = buscarUsuario(usuarioId);
        
        if (!usuario.getEmail().equals(email)) {
            Optional<Usuario> existente = usuarioRepo.findByEmail(email);
            if (existente.isPresent()) {
                throw new Exception("Email já está em uso por outro usuário.");
            }
        }

        usuario.setNome(nome);
        usuario.setEmail(email);
        
        if (senha != null && !senha.trim().isEmpty()) {
            usuario.setSenha(senha);
        }

        if (foto != null && !foto.isEmpty()) {
            usuario.setFotoPerfil(foto.getBytes());
            usuario.setTipoFoto(foto.getContentType());
        }

        return usuarioRepo.save(usuario);
    }
    
    public List<Usuario> listarUsuarios() { return usuarioRepo.findAll(); }

    // --- Produto ---
    @Transactional(readOnly = true) 
    public List<Produto> listarProdutos() {
        return produtoRepo.findAllByOrderByIdDesc();
    }

    @Transactional(readOnly = true)
    public Produto buscarProduto(Long id) {
        return produtoRepo.findById(id).orElseThrow(() -> new RuntimeException("Produto não encontrado"));
    }

    @Transactional
    public void salvarProduto(Produto produto, Usuario vendedor, MultipartFile file) throws IOException {
        produto.setVendedor(vendedor);

        if (file != null && !file.isEmpty()) {
            produto.setImagem(file.getBytes());
            produto.setTipoImagem(file.getContentType());
        } else {
            if (produto.getId() != null) {
                Produto produtoAntigo = produtoRepo.findById(produto.getId()).orElse(null);
                if (produtoAntigo != null && produtoAntigo.getImagem() != null) {
                    produto.setImagem(produtoAntigo.getImagem());
                    produto.setTipoImagem(produtoAntigo.getTipoImagem());
                }
            }
        }
        
        produtoRepo.save(produto);
    }
    
    @Transactional
    public void deletarProduto(Long id) {
        produtoRepo.deleteById(id);
    }

    // --- Carrinho ---
    @Transactional(readOnly = true)
    public List<ItemCarrinho> listarCarrinho(Usuario comprador) {
        return carrinhoRepo.findByComprador(comprador);
    }

    @Transactional
    public void adicionarAoCarrinho(Long produtoId, Integer quantidade, Usuario comprador) throws Exception {
        Produto produto = buscarProduto(produtoId);

        if (produto.getVendedor().getId().equals(comprador.getId())) {
            throw new Exception("Você não pode comprar seu próprio produto!");
        }

        ItemCarrinho item = carrinhoRepo.findByCompradorAndProduto(comprador, produto)
                .orElse(new ItemCarrinho());
        
        if (item.getId() == null) {
            item.setProduto(produto);
            item.setComprador(comprador);
            item.setQuantidade(0);
        }

        if ((item.getQuantidade() + quantidade) > produto.getQuantidadeEstoque()) {
             throw new Exception("Estoque insuficiente!");
        }

        item.setQuantidade(item.getQuantidade() + quantidade);
        carrinhoRepo.save(item);
    }

    @Transactional
    public void atualizarQuantidadeCarrinho(Long itemId, Integer novaQuantidade) {
        ItemCarrinho item = carrinhoRepo.findById(itemId).orElseThrow();
        if (novaQuantidade <= 0) {
            carrinhoRepo.delete(item);
        } else {
            item.setQuantidade(novaQuantidade);
            carrinhoRepo.save(item);
        }
    }

    @Transactional
    public void removerDoCarrinho(Long itemId) {
        carrinhoRepo.deleteById(itemId);
    }

    @Transactional
    public void finalizarCompra(Usuario comprador) throws Exception {
        List<ItemCarrinho> itens = carrinhoRepo.findByComprador(comprador);

        if (itens.isEmpty()) {
            throw new Exception("Seu carrinho está vazio!");
        }

        for (ItemCarrinho item : itens) {
            Produto produto = produtoRepo.findById(item.getProduto().getId())
                    .orElseThrow(() -> new Exception("Produto não encontrado"));

            if (item.getQuantidade() > produto.getQuantidadeEstoque()) {
                throw new Exception("Estoque insuficiente para o produto: " + produto.getNome());
            }

            int novoEstoque = produto.getQuantidadeEstoque() - item.getQuantidade();
            produto.setQuantidadeEstoque(novoEstoque);
            produtoRepo.save(produto);

            carrinhoRepo.delete(item);
        }
    }
}