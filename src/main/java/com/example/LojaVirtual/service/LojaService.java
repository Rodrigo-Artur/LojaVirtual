package com.example.LojaVirtual.service;

import com.example.LojaVirtual.model.*;
import com.example.LojaVirtual.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class LojaService {

    @Autowired private ProdutoRepository produtoRepo;
    @Autowired private ItemCarrinhoRepository carrinhoRepo;
    @Autowired private UsuarioRepository usuarioRepo;
    @Autowired private ItemDesejoRepository desejoRepo;
    @Autowired private CompraRepository compraRepo;
    @Autowired private ItemCompraRepository itemCompraRepo;
    @Autowired private AvaliacaoRepository avaliacaoRepo;

    // --- Autenticação e Usuário ---
    @Transactional(readOnly = true)
    public Usuario buscarUsuario(Long id) {
        return usuarioRepo.findById(id).orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }
    
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

    // --- FINALIZAR COMPRA (ATUALIZADO PARA HISTÓRICO) ---
    @Transactional
    public void finalizarCompra(Usuario comprador) throws Exception {
        List<ItemCarrinho> itensCarrinho = carrinhoRepo.findByComprador(comprador);

        if (itensCarrinho.isEmpty()) {
            throw new Exception("Seu carrinho está vazio!");
        }

        // Cria a entidade Compra
        Compra compra = new Compra();
        compra.setUsuario(comprador);
        List<ItemCompra> itensCompra = new ArrayList<>();

        for (ItemCarrinho ic : itensCarrinho) {
            Produto produto = produtoRepo.findById(ic.getProduto().getId())
                    .orElseThrow(() -> new Exception("Produto não encontrado"));

            if (ic.getQuantidade() > produto.getQuantidadeEstoque()) {
                throw new Exception("Estoque insuficiente para o produto: " + produto.getNome());
            }

            // Atualiza Estoque
            int novoEstoque = produto.getQuantidadeEstoque() - ic.getQuantidade();
            produto.setQuantidadeEstoque(novoEstoque);
            produtoRepo.save(produto);

            // Cria Item de Histórico
            ItemCompra itemCompra = new ItemCompra();
            itemCompra.setCompra(compra);
            itemCompra.setProduto(produto);
            itemCompra.setQuantidade(ic.getQuantidade());
            itemCompra.setPrecoUnitarioSnapshot(produto.getPreco());
            itensCompra.add(itemCompra);

            // Remove do carrinho
            carrinhoRepo.delete(ic);
        }
        
        compra.setItens(itensCompra);
        compraRepo.save(compra);
    }

    // --- LISTA DE DESEJOS ---
    @Transactional(readOnly = true)
    public List<ItemDesejo> listarDesejos(Usuario usuario) {
        return desejoRepo.findByUsuario(usuario);
    }

    @Transactional(readOnly = true)
    public boolean estaNaListaDeDesejos(Usuario usuario, Produto produto) {
        return desejoRepo.existsByUsuarioAndProduto(usuario, produto);
    }

    @Transactional
    public void alternarDesejo(Long produtoId, Usuario usuario) throws Exception {
        Produto produto = buscarProduto(produtoId);
        if (produto.getVendedor().getId().equals(usuario.getId())) {
             throw new Exception("Você não pode adicionar seu próprio produto à lista de desejos!");
        }
        Optional<ItemDesejo> existente = desejoRepo.findByUsuarioAndProduto(usuario, produto);
        if (existente.isPresent()) {
            desejoRepo.delete(existente.get());
        } else {
            ItemDesejo novo = new ItemDesejo();
            novo.setUsuario(usuario);
            novo.setProduto(produto);
            desejoRepo.save(novo);
        }
    }

    @Transactional
    public void removerDesejo(Long idDesejo) {
        desejoRepo.deleteById(idDesejo);
    }

    // --- HISTÓRICO DE COMPRAS ---
    @Transactional(readOnly = true)
    public List<Compra> listarHistorico(Usuario usuario) {
        return compraRepo.findByUsuarioOrderByDataCompraDesc(usuario);
    }

    // --- AVALIAÇÕES ---
    
    @Transactional(readOnly = true)
    public List<Avaliacao> listarAvaliacoes(Produto produto) {
        return avaliacaoRepo.findByProdutoOrderByDataAvaliacaoDesc(produto);
    }

    @Transactional(readOnly = true)
    public double calcularMediaAvaliacoes(Produto produto) {
        List<Avaliacao> avaliacoes = listarAvaliacoes(produto);
        if (avaliacoes.isEmpty()) return 0.0;
        return avaliacoes.stream().mapToInt(Avaliacao::getNota).average().orElse(0.0);
    }

    @Transactional(readOnly = true)
    public boolean podeAvaliar(Usuario usuario, Produto produto) {
        // 1. Não pode ser o dono
        if (produto.getVendedor().getId().equals(usuario.getId())) return false;
        // 2. Tem que ter comprado o produto
        boolean comprou = itemCompraRepo.existsByCompraUsuarioAndProduto(usuario, produto);
        // 3. Não pode ter avaliado ainda (opcional, aqui vou bloquear múltiplas avaliações)
        boolean jaAvaliou = avaliacaoRepo.existsByUsuarioAndProduto(usuario, produto);
        
        return comprou && !jaAvaliou;
    }

    @Transactional
    public void salvarAvaliacao(Long produtoId, Integer nota, String comentario, Usuario usuario) throws Exception {
        Produto produto = buscarProduto(produtoId);
        
        if (!podeAvaliar(usuario, produto)) {
            throw new Exception("Você não tem permissão para avaliar este produto (Necessário comprar antes ou já avaliou).");
        }

        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setProduto(produto);
        avaliacao.setUsuario(usuario);
        avaliacao.setNota(nota);
        avaliacao.setComentario(comentario);
        
        avaliacaoRepo.save(avaliacao);
    }
}