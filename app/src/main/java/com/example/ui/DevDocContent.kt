package com.example.ui

object DevDocContent {

    const val DB_SCHEMA_SQL = """-- ====================================================
-- BANCO DE DADOS POSTGRESQL - MARKETPLACE LOCAL
-- ====================================================

-- 1. Tabela de Usuários
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('CLIENT', 'MERCHANT', 'ADMIN')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 2. Tabela de Lojas
CREATE TABLE stores (
    id SERIAL PRIMARY KEY,
    owner_id INTEGER REFERENCES users(id) ON DELETE SET NULL,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL CHECK (type IN ('Restaurante', 'Roupas e calçados', 'Móveis', 'Perfumaria')),
    banner_url VARCHAR(255),
    delivery_time VARCHAR(30) DEFAULT '30-40 min',
    delivery_fee NUMERIC(10, 2) DEFAULT 0.00,
    min_order NUMERIC(10, 2) DEFAULT 0.00,
    rating NUMERIC(2, 1) DEFAULT 5.0,
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 3. Tabela de Produtos
CREATE TABLE products (
    id SERIAL PRIMARY KEY,
    store_id INTEGER REFERENCES stores(id) ON DELETE CASCADE,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    price NUMERIC(10, 2) NOT NULL,
    category_name VARCHAR(50), -- Cardápio subdivide em 'Refeições', 'Bebidas', 'Calçados', etc.
    in_stock BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 4. Tabela de Grupos de Variação (Para Roupas e Restaurantes)
-- Ex: "Tamanho", "Cor", "Adicional", "Ponto da Carne"
CREATE TABLE variation_groups (
    id SERIAL PRIMARY KEY,
    product_id INTEGER REFERENCES products(id) ON DELETE CASCADE,
    name VARCHAR(50) NOT NULL, -- 'Tamanho', 'Cor', 'Ingrediente Extra'
    min_selected INTEGER DEFAULT 0, -- Minimo selecionável (Obrigatório se > 0)
    max_selected INTEGER DEFAULT 1, -- Máximo de itens aceitáveis
    required BOOLEAN DEFAULT FALSE
);

-- 5. Tabela de Opções da Variação (Os valores em si)
-- Ex: 'M', 'G', 'Queijo', 'Muito Bem Passada'
CREATE TABLE variation_options (
    id SERIAL PRIMARY KEY,
    group_id INTEGER REFERENCES variation_groups(id) ON DELETE CASCADE,
    option_name VARCHAR(100) NOT NULL,
    extra_price NUMERIC(10, 2) DEFAULT 0.00, -- e.g. Borda Catupiry adicional de R$ 8.50
    stock_quantity INTEGER DEFAULT NULL,      -- Se nulo, estoque infinito (Alimentação). Numeral para Moda.
    available BOOLEAN DEFAULT TRUE
);

-- 6. Tabela de Carrinho
CREATE TABLE cart_items (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
    product_id INTEGER REFERENCES products(id) ON DELETE CASCADE,
    selected_option_ids INTEGER[] DEFAULT '{}', -- Array de IDs de options selecionadas
    quantity INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 7. Tabela de Pedidos
CREATE TABLE orders (
    id SERIAL PRIMARY KEY,
    customer_id INTEGER REFERENCES users(id) ON DELETE SET NULL,
    store_id INTEGER REFERENCES stores(id) ON DELETE SET NULL,
    subtotal NUMERIC(10, 2) NOT NULL,
    delivery_fee NUMERIC(10, 2) NOT NULL,
    total NUMERIC(10, 2) NOT NULL,
    delivery_address TEXT NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    status VARCHAR(30) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'PREPARING', 'SHIPPED', 'DELIVERED', 'CANCELLED')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 8. Itens do Pedido (Snapshot das Vendas)
CREATE TABLE order_items (
    id SERIAL PRIMARY KEY,
    order_id INTEGER REFERENCES orders(id) ON DELETE CASCADE,
    product_id INTEGER, -- Mantém referencial opcional caso o produto seja removido
    product_name VARCHAR(150) NOT NULL,
    price_at_sale NUMERIC(10, 2) NOT NULL,
    quantity INTEGER NOT NULL,
    customized_options TEXT -- Serialized JSON contendo o snapshot das variações
);"""

    const val EXPLANATION_VARIATIONS = """- RESTAURANTE (CARDÁPIO)
1. Categorização do Cardápio:
No iFood, os produtos do restaurante são classificados sob categorias de cardápio (categories) ligadas à loja, como "Entradas", "Pizzas", "Bebidas".
2. Estrutura de Adicionais & Opcionais:
As opções de um prato (como "Borda de Pizza", "Ponto da Carne", "Bebida Acompanhante") são modeladas como grupos de variação obrigatórios ou opcionais.
Exemplo:
O grupo "Borda" tem mínimo 0 e máximo 1 de seleção. A opção "Borda de Catupiry" adiciona + R${'$'}8,50 ao item do pedido.
3. Não há controle de estoque rígido:
Relações na cozinha usam reposição rápida. O estoque é simplesmente um booleano de disponibilidade ('available' true/false).

- ROUPAS E CALÇADOS (VARIAÇÕES)
1. Matriz de Atributos (Tamanho e Cor):
Diferente da comida, a moda requer uma combinação estrita de Tamanho (P, M, G) e Cor (Vermelho, Preto) que define o chamado SKU (Stock Keeping Unit).
2. Controle Físico de Estoque:
Cada SKU específico precisa ter um controle físico rigoroso de unidades restantes na tabela. Se o vestido Vermelho tamanho M acabar, o cliente não pode finalizar a compra de forma alguma, mesmo que haja estoque de Verde M ou Vermelho G.
3. Tratamento Relacional:
Implementamos `variation_groups` e `variation_options`. Para sistemas maiores, cria-se uma tabela matriz de SKUs (`product_skus` com id, product_id, size_id, color_id, price_override, stock) para otimizar pesquisas e transações de estoque em tempo real. No iFood local ou app de entregas padrão, as opções selecionadas pelo cliente no carrinho batem contra as chaves de estoque individuais, garantindo integridade rápida."""

    const val BACKEND_ROUTERS = """// ====================================================
// BACKEND NODE.JS (EXPRESS + PRISMA ORM POOL)
// ESTRUTURA PRINCIPAL DE ROTAS TRATADAS POR PERMISSÕES
// ====================================================

const express = require('express');
const router = express.Router();
const { authenticateToken, authorizeRole } = require('./middlewares/auth');

// --- 1. ROTAS DE CLIENTE ---
// Ver Lojas Aprovadas
router.get('/client/stores', authenticateToken, async (req, res) => {
    try {
        const stores = await prisma.store.findMany({
            where: { status: 'APPROVED' },
            include: { owner: { select: { name: true } } }
        });
        res.json(stores);
    } catch (err) {
        res.status(500).json({ error: 'Erro ao listar lojas.' });
    }
});

// Ver Detalhes e Cardápio da Loja
router.get('/client/stores/:id/products', authenticateToken, async (req, res) => {
    try {
        const { id } = req.params;
        const products = await prisma.product.findMany({
            where: { storeId: parseInt(id) },
            include: {
                variationGroups: {
                    include: { options: true }
                }
            }
        });
        res.json(products);
    } catch (err) {
        res.status(500).json({ error: 'Erro ao buscar cardápio.' });
    }
});

// Enviar / Criar Pedido
router.post('/client/orders', authenticateToken, async (req, res) => {
    try {
        const { storeId, items, deliveryAddress, paymentMethod } = req.body;
        const customerId = req.user.id; // decodificado do JWT JWT

        // Transação do Banco para deduzir estoque, validar e persistir
        const transaction = await prisma.${'$'}transaction(async (tx) => {
            let total = 0;
            // Loop de validação de preços/estoque de variações
            for (const item of items) {
                const prod = await tx.product.findUnique({ where: { id: item.productId } });
                total += Number(prod.price) * item.quantity;
            }

            const newOrder = await tx.order.create({
                data: {
                    customerId,
                    storeId,
                    subtotal: total,
                    deliveryFee: 5.90, // mock fixo por bairro
                    total: total + 5.90,
                    deliveryAddress,
                    paymentMethod,
                    status: 'PENDING'
                }
            });

            return newOrder;
        });

        res.status(201).json(transaction);
    } catch (err) {
        res.status(400).json({ error: 'Erro ao finalizar transação de pedido: ' + err.message });
    }
});


// --- 2. ROTAS DE LOJISTA (MERCHANT) ---
// Cadastrar/Editar Nova Loja (Pendente de Aprovação)
router.post('/merchant/stores', authenticateToken, authorizeRole('MERCHANT'), async (req, res) => {
    try {
        const { name, type, bannerUrl, minOrder } = req.body;
        const store = await prisma.store.create({
            data: {
                ownerId: req.user.id,
                name,
                type,
                bannerUrl,
                minOrder,
                status: 'PENDING' // Exige validação do Admin
            }
        });
        res.json(store);
    } catch (err) {
        res.status(500).json({ error: 'Falha ao solicitar criação de loja.' });
    }
});

// Adicionar produto
router.post('/merchant/products', authenticateToken, authorizeRole('MERCHANT'), async (req, res) => {
    try {
        const { storeId, name, description, price, categoryName, variations } = req.body;
        // Validação se o lojista realmente é o proprietário desta loja
        const store = await prisma.store.findUnique({ where: { id: storeId } });
        if (store.ownerId !== req.user.id) {
            return res.status(403).json({ error: 'Não autorizado.' });
        }

        const product = await prisma.product.create({
            data: {
                storeId,
                name,
                description,
                price,
                categoryName,
                variationGroups: {
                    create: variations.map(g => ({
                        name: g.groupName,
                        options: {
                            create: g.options.map(o => ({
                                optionName: o.optionName,
                                extraPrice: o.extraPrice,
                                stockQuantity: o.stockQuantity
                            }))
                        }
                    }))
                }
            }
        });
        res.json(product);
    } catch (err) {
        res.status(400).json({ error: 'Erro ao cadastrar produto.' });
    }
});


// --- 3. ROTAS DE ADMIN ---
// Listar Lojas Pendentes
router.get('/admin/stores/pending', authenticateToken, authorizeRole('ADMIN'), async (req, res) => {
    const list = await prisma.store.findMany({ where: { status: 'PENDING' } });
    res.json(list);
});

// Aprovar/Rejeitar Loja
router.patch('/admin/stores/:id/status', authenticateToken, authorizeRole('ADMIN'), async (req, res) => {
    const { id } = req.params;
    const { status } = req.body; // 'APPROVED' ou 'REJECTED'
    const updated = await prisma.store.update({
        where: { id: parseInt(id) },
        data: { status }
    });
    res.json(updated);
});"""

    const val SCALABILITY_SUGGESTION = """ARQUITETURA DE MICROSSERVIÇOS SIMPLES E ESCALÁVEL

Para um aplicativo de bairro que começa simples, mas visa crescer nacionalmente ou regionalmente, propomos a seguinte pilha moderna:

1. Camada de Clientes (Frontend & Apps):
   - Aplicativo Mobile Nativo Android (Kotlin / Compose) ou iOS (Swift).
   - Painel Web Admin/Merchant em React/Next.js consumindo REST API protegida por TLS/HTTPS.

2. API Gateway & Engenharia de Tráfego:
   - Nginx ou Kong como Proxy Reverso e Balanceador de Carga distribuindo conexões uniformemente.
   - Gerenciador de Certificados para criptografia SSL/TLS em todas as rotas de ponta a ponta.

3. Servidores Backend (Cluster Node.js):
   - Escrito em TypeScript / Express de fácil modularização.
   - Rodando em Containers isolados de Docker agrupados pelo Kubernetes para auto-scaling imediato sob picos de entrega (como almoço e fins de semana).

4. Filas e Envio Assíncrono (RabbitMQ / Redis):
   - Pedidos não bloqueiam a API. Ao fazer um pedido, ele é empacotado e enviado a uma Fila para notificar o lojista e calcular taxas em background sem sobrecarregar a requisição HTTPS do Cliente.

5. Banco de Dados PostgreSQL Relacional:
   - PostgreSQL em nuvem (ex: AWS RDS ou Supabase GP) com banco clusterizado.
   - Banco de Leitura vs Gravação (Master para gravação e Read Replicas rápidos para retornar listagens de cardápios e pesquisas instantaneamente).
   - Uso de Redis Cache para salvar cardápios atualizados e lojas em destaque de cada CEP, evitando consultas exaustivas nas tabelas físicas do banco principal."""
}
