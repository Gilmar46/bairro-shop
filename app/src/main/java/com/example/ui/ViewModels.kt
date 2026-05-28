package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MarketplaceViewModel(val repository: MarketplaceRepository) : ViewModel() {

    // Active User simulation: Client, Merchant, or Admin
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser = _currentUser.asStateFlow()

    // Screen State
    private val _activeTab = MutableStateFlow("HOME") // "HOME", "SEARCH", "CART", "PROFILE", "TECH_DOCS"
    val activeTab = _activeTab.asStateFlow()

    // Navigation sub-screens: detail store and detailed product variations selection
    private val _activeStore = MutableStateFlow<StoreEntity?>(null)
    val activeStore = _activeStore.asStateFlow()

    private val _activeProduct = MutableStateFlow<ProductEntity?>(null)
    val activeProduct = _activeProduct.asStateFlow()

    // Variations loaded for the currently customizable active product
    private val _activeProductVariations = MutableStateFlow<List<VariationOptionEntity>>(emptyList())
    val activeProductVariations = _activeProductVariations.asStateFlow()

    // Temporary user's selections for active product customization
    // Key: GroupName (e.g. "Tamanho", "Cor", "Adicional"), Value: List of Selected Option Names
    val selectedVariationsMap = MutableStateFlow<Map<String, List<String>>>(emptyMap())
    val activeProductQuantity = MutableStateFlow(1)

    // Store Listings
    val allStores: StateFlow<List<StoreEntity>> = repository.allStores
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val approvedStores: StateFlow<List<StoreEntity>> = repository.approvedStores
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All Users for Admin
    val allUsers: StateFlow<List<UserEntity>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All Orders for Admin
    val allOrders: StateFlow<List<OrderEntity>> = repository.allOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Cart for Current Client user
    private val _cartItems = MutableStateFlow<List<CartItemEntity>>(emptyList())
    val cartItems = _cartItems.asStateFlow()

    // Orders placed by Active Client user
    private val _clientOrders = MutableStateFlow<List<OrderEntity>>(emptyList())
    val clientOrders = _clientOrders.asStateFlow()

    // Merchant Store and Merchant Orders
    val merchantStore = MutableStateFlow<StoreEntity?>(null)
    private val _merchantOrders = MutableStateFlow<List<OrderEntity>>(emptyList())
    val merchantOrders = _merchantOrders.asStateFlow()

    // Search query
    val searchQuery = MutableStateFlow("")

    fun getProductsByStoreFlow(storeId: Int): Flow<List<ProductEntity>> {
        return repository.getProductsByStoreFlow(storeId)
    }

    init {
        viewModelScope.launch {
            repository.preloadDemoDataIfEmpty()
            // Set default simulated client Gilmar Brito
            val gilmar = repository.getUserByEmail("gilmarcoutobrito@gmail.com")
            _currentUser.value = gilmar
            
            // Map cart & orders dynamically
            launch {
                currentUser.collect { user ->
                    if (user != null) {
                        // Subscribe cart dynamically
                        repository.getCartItems(user.id).collect { items ->
                            _cartItems.value = items
                        }
                    }
                }
            }

            launch {
                currentUser.collect { user ->
                    if (user != null) {
                        // Subscribe orders dynamically
                        repository.getOrdersByUser(user.id).collect { orders ->
                            _clientOrders.value = orders
                        }
                    }
                }
            }

            launch {
                currentUser.collect { user ->
                    if (user != null) {
                        if (user.role == "MERCHANT") {
                            val store = repository.getStoreByOwner(user.id)
                            merchantStore.value = store
                            if (store != null) {
                                repository.getOrdersByStore(store.id).collect { orders ->
                                    _merchantOrders.value = orders
                                }
                            }
                        } else {
                            merchantStore.value = null
                            _merchantOrders.value = emptyList()
                        }
                    }
                }
            }
        }
    }

    fun switchUserFlow(user: UserEntity) {
        viewModelScope.launch {
            _currentUser.value = user
            _activeStore.value = null
            _activeProduct.value = null
            _activeTab.value = if (user.role == "MERCHANT") "HOME" else "HOME"
        }
    }

    fun selectTab(tab: String) {
        _activeTab.value = tab
        _activeStore.value = null
        _activeProduct.value = null
    }

    fun selectStore(store: StoreEntity?) {
        _activeStore.value = store
        _activeProduct.value = null
    }

    fun selectProduct(product: ProductEntity?) {
        _activeProduct.value = product
        if (product != null) {
            viewModelScope.launch {
                val variations = repository.getVariationsByProduct(product.id)
                _activeProductVariations.value = variations
                
                // Pre-populate default single choice variations
                val defaultMap = mutableMapOf<String, List<String>>()
                variations.groupBy { it.groupName }.forEach { (group, opts) ->
                    // For "Tamanho", "Cor" or "Estilo", auto-select the first option by default
                    if (group == "Tamanho" || group == "Cor" || group == "Estilo" || group == "Combinação") {
                        opts.firstOrNull()?.let {
                            defaultMap[group] = listOf(it.optionName)
                        }
                    } else {
                        // Multi-choice like "Adicional", leave empty
                        defaultMap[group] = emptyList()
                    }
                }
                selectedVariationsMap.value = defaultMap
                activeProductQuantity.value = 1
            }
        } else {
            _activeProductVariations.value = emptyList()
            selectedVariationsMap.value = emptyMap()
            activeProductQuantity.value = 1
        }
    }

    fun toggleVariationOption(groupName: String, option: VariationOptionEntity, isSingleChoice: Boolean) {
        val currentSelections = selectedVariationsMap.value.toMutableMap()
        val groupList = currentSelections[groupName]?.toMutableList() ?: mutableListOf()

        if (isSingleChoice) {
            groupList.clear()
            groupList.add(option.optionName)
        } else {
            if (groupList.contains(option.optionName)) {
                groupList.remove(option.optionName)
            } else {
                groupList.add(option.optionName)
            }
        }
        currentSelections[groupName] = groupList
        selectedVariationsMap.value = currentSelections
    }

    fun getActiveProductTotalPrice(): Double {
        val prod = _activeProduct.value ?: return 0.0
        var total = prod.price
        
        selectedVariationsMap.value.forEach { (group, selectedOptNames) ->
            _activeProductVariations.value.filter { it.groupName == group && selectedOptNames.contains(it.optionName) }
                .forEach { total += it.extraPrice }
        }
        return total * activeProductQuantity.value
    }

    fun addActiveProductToCart() {
        val user = _currentUser.value ?: return
        val prod = _activeProduct.value ?: return
        val store = _activeStore.value ?: return

        viewModelScope.launch {
            // Build selections string
            val parts = mutableListOf<String>()
            selectedVariationsMap.value.forEach { (group, list) ->
                if (list.isNotEmpty()) {
                    parts.add("$group: ${list.joinToString(", ")}")
                }
            }
            val selectionString = parts.joinToString(" | ")

            val cartItem = CartItemEntity(
                userId = user.id,
                productId = prod.id,
                storeId = store.id,
                productName = prod.name,
                price = getActiveProductTotalPrice() / activeProductQuantity.value,
                logoIndex = prod.imageIndex,
                selectedVariations = selectionString,
                quantity = activeProductQuantity.value
            )
            repository.addToCart(cartItem)
            
            // Go to Cart tab automatically to show off experience
            _activeProduct.value = null
            _activeStore.value = null
            _activeTab.value = "CART"
        }
    }

    fun updateCartItemQuantity(id: Int, flag: Boolean) {
        viewModelScope.launch {
            _cartItems.value.find { it.id == id }?.let { item ->
                val newQty = if (flag) item.quantity + 1 else item.quantity - 1
                if (newQty > 0) {
                    repository.updateCartQuantity(item.id, newQty)
                } else {
                    repository.deleteCartItem(item.id)
                }
            }
        }
    }

    fun removeCartItem(id: Int) {
        viewModelScope.launch {
            repository.deleteCartItem(id)
        }
    }

    fun checkoutActiveCart(address: String, payment: String) {
        val user = _currentUser.value ?: return
        val items = _cartItems.value
        if (items.isEmpty()) return

        viewModelScope.launch {
            // Group cart items by store so we place separate orders per store (like real marketplaces!)
            val storesGrouped = items.groupBy { it.storeId }
            storesGrouped.forEach { (storeId, storeItems) ->
                // Fetch store name
                val storeObj = allStores.value.find { it.id == storeId }
                val sName = storeObj?.name ?: "Mercado Geral"
                val deliveryFee = storeObj?.deliveryFee ?: 5.90
                val totalItemsCost = storeItems.sumOf { it.price * it.quantity }

                val orderId = repository.placeOrder(OrderEntity(
                    customerId = user.id,
                    storeId = storeId,
                    storeName = sName,
                    totalPrice = totalItemsCost + deliveryFee,
                    deliveryAddress = address,
                    paymentMethod = payment,
                    status = "PENDING"
                ))

                // Place nested order items
                storeItems.forEach { cItem ->
                    repository.addOrderItem(OrderItemEntity(
                        orderId = orderId.toInt(),
                        productId = cItem.productId,
                        productName = cItem.productName,
                        quantity = cItem.quantity,
                        pricePerUnit = cItem.price,
                        selectedVariations = cItem.selectedVariations
                    ))
                }
            }

            // Clear Cart and Switch to Profile order tracker!
            repository.clearCart(user.id)
            _activeTab.value = "PROFILE"
        }
    }

    // MERCHANT: Register Store
    fun registerNewStoreByMerchant(name: String, type: String, minOrder: Double) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val store = StoreEntity(
                name = name,
                type = type,
                bannerUrl = "",
                deliveryTime = "30-45 min",
                deliveryFee = 5.90,
                minOrder = minOrder,
                rating = 5.0,
                status = "PENDING", // needs admin approval
                ownerId = user.id
            )
            repository.addStore(store)
            // Reload merchant store state
            val storeObj = repository.getStoreByOwner(user.id)
            merchantStore.value = storeObj
        }
    }

    // MERCHANT: Add Product with Variations
    fun addProductByMerchant(
        name: String,
        description: String,
        price: Double,
        category: String,
        variationsInput: List<Pair<String, List<Pair<String, Double>>>> // Optional variations input list
    ) {
        val store = merchantStore.value ?: return
        viewModelScope.launch {
            val pId = repository.addProduct(ProductEntity(
                storeId = store.id,
                name = name,
                description = description,
                price = price,
                categoryName = category,
                imageIndex = (1..9).random()
            )).toInt()

            // Save variations if provided
            variationsInput.forEach { (group, list) ->
                list.forEach { (optName, extraPrice) ->
                    repository.addVariation(VariationOptionEntity(
                        productId = pId,
                        groupName = group,
                        optionName = optName,
                        extraPrice = extraPrice
                    ))
                }
            }
        }
    }

    // MERCHANT: Change Order Status
    fun updateMerchantOrderStatus(orderId: Int, nextStatus: String) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, nextStatus)
        }
    }

    // ADMIN: Approve Store
    fun adminApproveStore(storeId: Int) {
        viewModelScope.launch {
            repository.updateStoreStatus(storeId, "APPROVED")
        }
    }

    // ADMIN: Disapprove / Reject Store
    fun adminRejectStore(storeId: Int) {
        viewModelScope.launch {
            repository.updateStoreStatus(storeId, "REJECTED")
        }
    }
}

class MarketplaceViewModelFactory(private val repository: MarketplaceRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MarketplaceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MarketplaceViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
