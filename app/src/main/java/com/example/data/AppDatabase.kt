package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Int): UserEntity?

    @Query("SELECT * FROM users")
    fun getAllUsersFlow(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long
}

@Dao
interface StoreDao {
    @Query("SELECT * FROM stores ORDER BY id DESC")
    fun getAllStoresFlow(): Flow<List<StoreEntity>>

    @Query("SELECT * FROM stores WHERE status = 'APPROVED' ORDER BY rating DESC")
    fun getApprovedStoresFlow(): Flow<List<StoreEntity>>

    @Query("SELECT * FROM stores WHERE ownerId = :ownerId LIMIT 1")
    suspend fun getStoreByOwner(ownerId: Int): StoreEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStore(store: StoreEntity): Long

    @Query("UPDATE stores SET status = :status WHERE id = :storeId")
    suspend fun updateStoreStatus(storeId: Int, status: String)
}

@Dao
interface ProductDao {
    @Query("SELECT * FROM products WHERE storeId = :storeId ORDER BY id ASC")
    fun getProductsByStoreFlow(storeId: Int): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE storeId = :storeId ORDER BY id ASC")
    suspend fun getProductsByStore(storeId: Int): List<ProductEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProductById(id: Int)
}

@Dao
interface VariationDao {
    @Query("SELECT * FROM variation_options WHERE productId = :productId")
    fun getVariationsByProductFlow(productId: Int): Flow<List<VariationOptionEntity>>

    @Query("SELECT * FROM variation_options WHERE productId = :productId")
    suspend fun getVariationsByProduct(productId: Int): List<VariationOptionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVariation(variation: VariationOptionEntity): Long

    @Query("DELETE FROM variation_options WHERE productId = :productId")
    suspend fun deleteVariationsByProduct(productId: Int)
}

@Dao
interface CartDao {
    @Query("SELECT * FROM cart_items WHERE userId = :userId")
    fun getCartItemsFlow(userId: Int): Flow<List<CartItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(cartItem: CartItemEntity)

    @Query("UPDATE cart_items SET quantity = :quantity WHERE id = :itemId")
    suspend fun updateQuantity(itemId: Int, quantity: Int)

    @Query("DELETE FROM cart_items WHERE id = :itemId")
    suspend fun deleteCartItem(itemId: Int)

    @Query("DELETE FROM cart_items WHERE userId = :userId")
    suspend fun clearCart(userId: Int)
}

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY timestamp DESC")
    fun getAllOrdersFlow(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE customerId = :customerId ORDER BY timestamp DESC")
    fun getOrdersByUserFlow(customerId: Int): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE storeId = :storeId ORDER BY timestamp DESC")
    fun getOrdersByStoreFlow(storeId: Int): Flow<List<OrderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity): Long

    @Query("UPDATE orders SET status = :status WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: Int, status: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItem(orderItem: OrderItemEntity)

    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    suspend fun getOrderItems(orderId: Int): List<OrderItemEntity>
}

@Database(
    entities = [
        UserEntity::class,
        StoreEntity::class,
        ProductEntity::class,
        VariationOptionEntity::class,
        CartItemEntity::class,
        OrderEntity::class,
        OrderItemEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun storeDao(): StoreDao
    abstract fun productDao(): ProductDao
    abstract fun variationDao(): VariationDao
    abstract fun cartDao(): CartDao
    abstract fun orderDao(): OrderDao
}
