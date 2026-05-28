package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class MarketplaceRepository(private val db: AppDatabase) {

    val allUsers: Flow<List<UserEntity>> = db.userDao().getAllUsersFlow()
    val allStores: Flow<List<StoreEntity>> = db.storeDao().getAllStoresFlow()
    val approvedStores: Flow<List<StoreEntity>> = db.storeDao().getApprovedStoresFlow()
    val allOrders: Flow<List<OrderEntity>> = db.orderDao().getAllOrdersFlow()

    suspend fun getUserByEmail(email: String): UserEntity? = db.userDao().getUserByEmail(email)
    suspend fun getUserById(id: Int): UserEntity? = db.userDao().getUserById(id)
    suspend fun registerUser(user: UserEntity): Long = db.userDao().insertUser(user)

    fun getProductsByStoreFlow(storeId: Int): Flow<List<ProductEntity>> = db.productDao().getProductsByStoreFlow(storeId)
    suspend fun getProductsByStore(storeId: Int): List<ProductEntity> = db.productDao().getProductsByStore(storeId)
    suspend fun addProduct(product: ProductEntity): Long = db.productDao().insertProduct(product)
    suspend fun deleteProduct(id: Int) = db.productDao().deleteProductById(id)

    fun getVariationsByProductFlow(productId: Int): Flow<List<VariationOptionEntity>> = db.variationDao().getVariationsByProductFlow(productId)
    suspend fun getVariationsByProduct(productId: Int): List<VariationOptionEntity> = db.variationDao().getVariationsByProduct(productId)
    suspend fun addVariation(variation: VariationOptionEntity): Long = db.variationDao().insertVariation(variation)
    suspend fun deleteProductVariations(productId: Int) = db.variationDao().deleteVariationsByProduct(productId)

    suspend fun getStoreByOwner(ownerId: Int): StoreEntity? = db.storeDao().getStoreByOwner(ownerId)
    suspend fun addStore(store: StoreEntity): Long = db.storeDao().insertStore(store)
    suspend fun updateStoreStatus(storeId: Int, status: String) = db.storeDao().updateStoreStatus(storeId, status)

    fun getCartItems(userId: Int): Flow<List<CartItemEntity>> = db.cartDao().getCartItemsFlow(userId)
    suspend fun addToCart(cartItem: CartItemEntity) = db.cartDao().insertCartItem(cartItem)
    suspend fun updateCartQuantity(itemId: Int, quantity: Int) = db.cartDao().updateQuantity(itemId, quantity)
    suspend fun deleteCartItem(itemId: Int) = db.cartDao().deleteCartItem(itemId)
    suspend fun clearCart(userId: Int) = db.cartDao().clearCart(userId)

    fun getOrdersByUser(customerId: Int): Flow<List<OrderEntity>> = db.orderDao().getOrdersByUserFlow(customerId)
    fun getOrdersByStore(storeId: Int): Flow<List<OrderEntity>> = db.orderDao().getOrdersByStoreFlow(storeId)
    suspend fun placeOrder(order: OrderEntity): Long = db.orderDao().insertOrder(order)
    suspend fun addOrderItem(orderItem: OrderItemEntity) = db.orderDao().insertOrderItem(orderItem)
    suspend fun updateOrderStatus(orderId: Int, status: String) = db.orderDao().updateOrderStatus(orderId, status)
    suspend fun getOrderItems(orderId: Int): List<OrderItemEntity> = db.orderDao().getOrderItems(orderId)

    suspend fun preloadDemoDataIfEmpty() {
        val users = allUsers.firstOrNull()
        if (users.isNullOrEmpty()) {
            // 1. Seed standard Roles
            val cId = registerUser(UserEntity(name = "Gilmar Brito", email = "gilmarcoutobrito@gmail.com", role = "CLIENT", avatarColor = -0xbbc40))
            val mId = registerUser(UserEntity(name = "Joaquim Merceeiro", email = "joaquim@bairromarket.com", role = "MERCHANT", avatarColor = -0x1c37b))
            val aId = registerUser(UserEntity(name = "Administrador", email = "admin@bairromarket.com", role = "ADMIN", avatarColor = -0xba5400))

            // 2. Seed Standard approved store representing Pizza Pizzeria Bella Notte (Restaurante)
            val store1Id = addStore(StoreEntity(
                name = "Pizzeria Bella Notte",
                type = "Restaurante",
                bannerUrl = "", // handled by vector placeholders or Coil loader with fallback drawables
                deliveryTime = "25-35 min",
                deliveryFee = 0.0,
                minOrder = 30.0,
                rating = 4.8,
                status = "APPROVED",
                ownerId = mId.toInt()
            )).toInt()

            // Products for Store 1
            val p1 = addProduct(ProductEntity(storeId = store1Id, name = "Pizza Margherita Especial", description = "Molho artesanal de tomate, muçarela de búfala, manjericão fresco e azeite trufado.", price = 54.90, categoryName = "Pizzas", imageIndex = 1)).toInt()
            val p2 = addProduct(ProductEntity(storeId = store1Id, name = "Calzone de Nutella", description = "Massa leve recheada com Nutella original, morangos frescos e açúcar de confeiteiro.", price = 38.00, categoryName = "Pizzas", imageIndex = 2)).toInt()
            val p3 = addProduct(ProductEntity(storeId = store1Id, name = "Limonada Italiana Fresca", description = "Suco de limão siciliano, água gaseificada, hortelã e gelo.", price = 14.00, categoryName = "Bebidas", imageIndex = 3)).toInt()

            // Variations for Pizzas
            addVariation(VariationOptionEntity(productId = p1, groupName = "Adicional", optionName = "Borda de Catupiry", extraPrice = 8.50))
            addVariation(VariationOptionEntity(productId = p1, groupName = "Adicional", optionName = "Queijo Extra", extraPrice = 6.00))
            addVariation(VariationOptionEntity(productId = p1, groupName = "Tamanho", optionName = "Broto", extraPrice = -15.00))
            addVariation(VariationOptionEntity(productId = p1, groupName = "Tamanho", optionName = "Média", extraPrice = 0.0))
            addVariation(VariationOptionEntity(productId = p1, groupName = "Tamanho", optionName = "Grande", extraPrice = 12.00))

            addVariation(VariationOptionEntity(productId = p2, groupName = "Adicional", optionName = "Morango extra", extraPrice = 4.00))
            addVariation(VariationOptionEntity(productId = p2, groupName = "Adicional", optionName = "Sorvete de Creme", extraPrice = 7.00))

            // 3. Seed Standard approved store representing Clothing Store (Roupas)
            val store2Id = addStore(StoreEntity(
                name = "Urban Style Moda",
                type = "Roupas e calçados",
                bannerUrl = "",
                deliveryTime = "15-25 min",
                deliveryFee = 5.90,
                minOrder = 50.0,
                rating = 4.9,
                status = "APPROVED",
                ownerId = 99 // dummy
            )).toInt()

            val c1 = addProduct(ProductEntity(storeId = store2Id, name = "Blazer Oversized em Linho Premium", description = "Eleve seu guarda-roupa com o nosso Blazer Oversized em Linho Premium. Confeccionado com mistura exclusiva de linho e viscose para caimento perfeito.", price = 459.90, categoryName = "Inverno", imageIndex = 4)).toInt()
            val c2 = addProduct(ProductEntity(storeId = store2Id, name = "Vestido Midi de Seda", description = "Um caimento gracioso com tecido nobre de toque macio. Perfeito para passeios exuberantes de tarde ou festas tranquilas.", price = 299.90, categoryName = "Primavera", imageIndex = 5)).toInt()
            val c3 = addProduct(ProductEntity(storeId = store2Id, name = "Loafer Couro Premium", description = "Confeccionado em couro legítimo polido com solado antiderrapante. Alta durabilidade e elegância atemporal.", price = 580.00, categoryName = "Calçados", imageIndex = 6)).toInt()

            // Variations for Blazer (c1) - size and colors!
            addVariation(VariationOptionEntity(productId = c1, groupName = "Tamanho", optionName = "P", extraPrice = 0.0))
            addVariation(VariationOptionEntity(productId = c1, groupName = "Tamanho", optionName = "M", extraPrice = 0.0))
            addVariation(VariationOptionEntity(productId = c1, groupName = "Tamanho", optionName = "G", extraPrice = 0.0))
            addVariation(VariationOptionEntity(productId = c1, groupName = "Tamanho", optionName = "GG", extraPrice = 20.00))
            addVariation(VariationOptionEntity(productId = c1, groupName = "Cor", optionName = "Bege Areia", extraPrice = 0.0))
            addVariation(VariationOptionEntity(productId = c1, groupName = "Cor", optionName = "Preto", extraPrice = 0.0))
            addVariation(VariationOptionEntity(productId = c1, groupName = "Cor", optionName = "Branco", extraPrice = 0.0))

            // Variations for Vestido (c2)
            addVariation(VariationOptionEntity(productId = c2, groupName = "Tamanho", optionName = "P", extraPrice = 0.0))
            addVariation(VariationOptionEntity(productId = c2, groupName = "Tamanho", optionName = "M", extraPrice = 0.0))
            addVariation(VariationOptionEntity(productId = c2, groupName = "Tamanho", optionName = "G", extraPrice = 0.0))
            addVariation(VariationOptionEntity(productId = c2, groupName = "Cor", optionName = "Azul Marinho", extraPrice = 0.0))
            addVariation(VariationOptionEntity(productId = c2, groupName = "Cor", optionName = "Verde Esmeralda", extraPrice = 15.00))

            // 4. Seed Standard approved store representing Furniture Store
            val store3Id = addStore(StoreEntity(
                name = "Home Decor Studio",
                type = "Móveis",
                bannerUrl = "",
                deliveryTime = "1-3 dias",
                deliveryFee = 49.90,
                minOrder = 100.0,
                rating = 4.7,
                status = "APPROVED",
                ownerId = 100 // dummy
            )).toInt()

            val f1 = addProduct(ProductEntity(storeId = store3Id, name = "Cadeira Ergo Nordic", description = "Cadeira de design escandinavo contemporâneo com assento acolchoado giratório e pernas em madeira de faia.", price = 1150.00, categoryName = "Sala", imageIndex = 7)).toInt()
            addVariation(VariationOptionEntity(productId = f1, groupName = "Combinação", optionName = "Pernas Caramelo + Assento Off-white", extraPrice = 0.0))
            addVariation(VariationOptionEntity(productId = f1, groupName = "Combinação", optionName = "Pernas Pretas + Assento Grafite", extraPrice = 50.00))

            // 5. Seed Sweet Perfumaria store
            val store4Id = addStore(StoreEntity(
                name = "Acqua Perfumaria",
                type = "Perfumaria",
                bannerUrl = "",
                deliveryTime = "30-50 min",
                deliveryFee = 7.90,
                minOrder = 20.0,
                rating = 4.6,
                status = "APPROVED",
                ownerId = 101 // dummy
            )).toInt()

            addProduct(ProductEntity(storeId = store4Id, name = "Essence Classique 100ml", description = "Fragrância floral oriental sensual com notas de flor de laranjeira, jasmim e baunilha refinada.", price = 349.90, categoryName = "Importados", imageIndex = 8))

            // 6. Seed a Pending store to show off Admin approval action!
            addStore(StoreEntity(
                name = "Hamburgueria do Beco",
                type = "Restaurante",
                bannerUrl = "",
                deliveryTime = "35-50 min",
                deliveryFee = 6.00,
                minOrder = 25.0,
                rating = 4.2,
                status = "PENDING",
                ownerId = 102
            ))
        }
    }
}
