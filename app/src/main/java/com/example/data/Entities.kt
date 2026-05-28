package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val email: String,
    val role: String, // "CLIENT", "MERCHANT", "ADMIN"
    val avatarColor: Int
)

@Entity(tableName = "stores")
data class StoreEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val type: String, // "Restaurante", "Roupas e calçados", "Móveis", "Perfumaria"
    val bannerUrl: String,
    val deliveryTime: String,
    val deliveryFee: Double,
    val minOrder: Double,
    val rating: Double,
    val status: String, // "PENDING", "APPROVED"
    val ownerId: Int
)

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val storeId: Int,
    val name: String,
    val description: String,
    val price: Double,
    val categoryName: String, // e.g. "Bebidas", "Pizzas", "Primavera", "Inverno"
    val imageIndex: Int // Helper for fallback beautiful vector mock images/illustrations
)

@Entity(tableName = "variation_options")
data class VariationOptionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productId: Int,
    val groupName: String, // "Tamanho", "Cor", "Acompanhamento", "Adicional", "Estilo"
    val optionName: String, // "P", "M", "G", "Queijo Extra", "Borda de Catupiry"
    val extraPrice: Double
)

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val productId: Int,
    val storeId: Int,
    val productName: String,
    val price: Double,
    val logoIndex: Int,
    val selectedVariations: String, // Serialized textual summary, e.g. "Tamanho: G, Borda: Catupiry"
    val quantity: Int
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val customerId: Int,
    val storeId: Int,
    val storeName: String,
    val totalPrice: Double,
    val deliveryAddress: String,
    val paymentMethod: String,
    val status: String, // "PENDING" (Pendente), "PREPARING" (Preparando), "SHIPPED" (A Caminho), "DELIVERED" (Entregue)
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "order_items")
data class OrderItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val orderId: Int,
    val productId: Int,
    val productName: String,
    val quantity: Int,
    val pricePerUnit: Double,
    val selectedVariations: String
)
