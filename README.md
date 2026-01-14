# 🛒 LojaVirtual (E-commerce Fullstack)

Sistema completo de comércio eletrônico desenvolvido com **Java Spring Boot** e **Thymeleaf**. O projeto utiliza renderização no lado do servidor (SSR) para entregar uma experiência robusta e segura, gerenciando desde o catálogo de produtos até o carrinho de compras e painel administrativo.

## 🚀 Tecnologias Utilizadas

* **Java 17**
* **Spring Boot 3** (Web, Security, Data JPA)
* **Thymeleaf** (Template Engine para Frontend)
* **PostgreSQL** (Banco de Dados Relacional)
* **Docker & Docker Compose** (Containerização do Banco)
* **Bootstrap 5** (Estilização e Responsividade)
* **Maven** (Gerenciamento de dependências)

## ✨ Funcionalidades

### 👤 Área do Cliente
* **Autenticação:** Cadastro e Login de usuários com Spring Security.
* **Catálogo:** Visualização e busca de produtos.
* **Carrinho de Compras:** Adicionar/remover itens e finalizar pedido.
* **Lista de Desejos (Wishlist):** Salvar produtos favoritos.
* **Histórico:** Visualização de compras realizadas.
* **Perfil:** Gerenciamento de dados cadastrais.

### 🛡️ Área Administrativa
* **Controle de Acesso:** Acesso restrito a usuários com Role `ADMIN`.
* **Gestão de Produtos:** Cadastro e edição de produtos.
* **Gestão de Categorias:** Organização do catálogo.
* **Cadastro de Admins:** Interface para registrar novos administradores.

## 🔧 Como Rodar o Projeto

### Pré-requisitos
* Java 17+ instalado
* Docker e Docker Compose instalados

### Passo a Passo

1.  **Clone o repositório**
    ```bash
    git clone [https://github.com/SEU-USUARIO/LojaVirtual.git](https://github.com/SEU-USUARIO/LojaVirtual.git)
    cd LojaVirtual
    ```

2.  **Suba o Banco de Dados com Docker**
    O projeto já possui um arquivo `docker-compose.yml` configurado para iniciar o PostgreSQL.
    ```bash
    docker-compose up -d
    ```

3.  **Execute a Aplicação**
    Utilize o wrapper do Maven para rodar o projeto:
    ```bash
    ./mvnw spring-boot:run
    ```

4.  **Acesse no Navegador**
    O sistema estará disponível em: `http://localhost:8080`

## 🗄️ Estrutura do Banco de Dados

O sistema utiliza o **PostgreSQL** e cria automaticamente as tabelas baseadas nas entidades JPA:
* `usuarios` (Clientes e Administradores)
* `produtos` & `categorias`
* `compras` & `itens_compra`
* `carrinho` & `itens_carrinho`
* `avaliacoes`

## ⚙️ Configuração

As configurações principais estão no arquivo `src/main/resources/application.properties`.
O projeto utiliza variáveis de ambiente ou configurações padrão para conectar ao container Docker:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/lojavirtual
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.jpa.hibernate.ddl-auto=update
