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

@Service
public class LojaService {

    @Autowired private ProdutoRepository produtoRepo;
    @Autowired private ItemCarrinhoRepository carrinhoRepo;
    @Autowired private UsuarioRepository usuarioRepo;

    // --- Usuário ---
    public Usuario buscarUsuario(Long id) {
        return usuarioRepo.findById(id).orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
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
    public void salvarProduto(Produto produto, Long vendedorId, MultipartFile file) throws IOException {
        Usuario vendedor = buscarUsuario(vendedorId);
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
    public List<ItemCarrinho> listarCarrinho(Long usuarioId) {
        Usuario comprador = buscarUsuario(usuarioId);
        return carrinhoRepo.findByComprador(comprador);
    }

    @Transactional
    public void adicionarAoCarrinho(Long produtoId, Integer quantidade, Long usuarioId) throws Exception {
        Produto produto = buscarProduto(produtoId);
        Usuario comprador = buscarUsuario(usuarioId);

        if (produto.getVendedor().getId().equals(usuarioId)) {
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

    // --- FINALIZAR COMPRA (CORRIGIDO) ---
    @Transactional
    public void finalizarCompra(Long usuarioId) throws Exception {
        Usuario comprador = buscarUsuario(usuarioId);
        List<ItemCarrinho> itens = carrinhoRepo.findByComprador(comprador);

        if (itens.isEmpty()) {
            throw new Exception("Seu carrinho está vazio!");
        }

        for (ItemCarrinho item : itens) {
            // CORREÇÃO CRÍTICA: Carregamos o produto do repositório para garantir 
            // que estamos manipulando a entidade gerenciada correta.
            Produto produto = produtoRepo.findById(item.getProduto().getId())
                    .orElseThrow(() -> new Exception("Produto não encontrado"));

            // Validação de estoque
            if (item.getQuantidade() > produto.getQuantidadeEstoque()) {
                throw new Exception("Estoque insuficiente para o produto: " + produto.getNome());
            }

            // Atualiza o estoque
            int novoEstoque = produto.getQuantidadeEstoque() - item.getQuantidade();
            produto.setQuantidadeEstoque(novoEstoque);
            
            // Salva explicitamente o produto atualizado
            produtoRepo.save(produto);

            // Remove o item do carrinho
            carrinhoRepo.delete(item);
        }
    }
}