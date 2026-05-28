package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import kotlinx.coroutines.launch

// "Sleek Interface" Lavender & Royal Purple color theme matching high fidelity style specifications
val PrimaryBlue = Color(0xFF6750A4)
val SurfaceTint = Color(0xFF6750A4)
val SecondaryAmber = Color(0xFF7D5260)
val SecondaryAmberContainer = Color(0xFFE8DEF8)
val TertiaryGreen = Color(0xFF006A60)
val OnSurfaceVariant = Color(0xFF49454F)
val ErrorRed = Color(0xFFB3261E)
val SurfaceBgLight = Color(0xFFFEF7FF)
val CardBorderClr = Color(0xFFCAC4D0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppUI(viewModel: MarketplaceViewModel) {
    val activeTab by viewModel.activeTab.collectAsState()
    val activeUser by viewModel.currentUser.collectAsState()
    val activeStore by viewModel.activeStore.collectAsState()
    val activeProduct by viewModel.activeProduct.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()

    // Checkout modal trigger trigger
    var showCheckoutDialog by remember { mutableStateOf(false) }
    var checkoutAddress by remember { mutableStateOf("Rua das Flores, 123 - Bloco B") }
    var checkoutPayment by remember { mutableStateOf("Cartão de Crédito - Nubank") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalMall,
                            contentDescription = "BairroMarket Logo",
                            tint = PrimaryBlue,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = "BairroMarket",
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue,
                            fontSize = 20.sp
                        )
                    }
                },
                actions = {
                    // Floating interactive user perspective selection
                    var dropdownExpanded by remember { mutableStateOf(false) }
                    IconButton(
                        onClick = { dropdownExpanded = true },
                        modifier = Modifier.testTag("user_role_dropdown_btn")
                    ) {
                        Icon(
                            imageVector = when (activeUser?.role) {
                                "ADMIN" -> Icons.Default.AdminPanelSettings
                                "MERCHANT" -> Icons.Default.Storefront
                                else -> Icons.Default.Person
                            },
                            contentDescription = "Switch Persona",
                            tint = when (activeUser?.role) {
                                "ADMIN" -> ErrorRed
                                "MERCHANT" -> TertiaryGreen
                                else -> PrimaryBlue
                            }
                        )
                    }

                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Visualizar como: CLIENTE") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryBlue) },
                            onClick = {
                                dropdownExpanded = false
                                val clientUsr = allUsers.find { it.role == "CLIENT" }
                                if (clientUsr != null) viewModel.switchUserFlow(clientUsr)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Visualizar como: LOJISTA") },
                            leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = null, tint = TertiaryGreen) },
                            onClick = {
                                dropdownExpanded = false
                                val merchUsr = allUsers.find { it.role == "MERCHANT" }
                                if (merchUsr != null) viewModel.switchUserFlow(merchUsr)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Visualizar como: ADMIN") },
                            leadingIcon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = ErrorRed) },
                            onClick = {
                                dropdownExpanded = false
                                val adminUsr = allUsers.find { it.role == "ADMIN" }
                                if (adminUsr != null) viewModel.switchUserFlow(adminUsr)
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.navigationBarsPadding()
            ) {
                NavigationBarItem(
                    selected = activeTab == "HOME" && activeStore == null && activeProduct == null,
                    onClick = { viewModel.selectTab("HOME") },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Início", maxLines = 1) },
                    modifier = Modifier.testTag("nav_home_tab")
                )
                NavigationBarItem(
                    selected = activeTab == "SEARCH",
                    onClick = { viewModel.selectTab("SEARCH") },
                    icon = { Icon(Icons.Default.Search, contentDescription = "Pesquisar") },
                    label = { Text("Pesquisa", maxLines = 1) },
                    modifier = Modifier.testTag("nav_search_tab")
                )
                NavigationBarItem(
                    selected = activeTab == "CART",
                    onClick = { viewModel.selectTab("CART") },
                    icon = {
                        val cartItems by viewModel.cartItems.collectAsState()
                        BadgedBox(
                            badge = {
                                if (cartItems.isNotEmpty()) {
                                    Badge { Text(cartItems.sumOf { it.quantity }.toString()) }
                                }
                            }
                        ) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = "Carrinho")
                        }
                    },
                    label = { Text("Carrinho", maxLines = 1) },
                    modifier = Modifier.testTag("nav_cart_tab")
                )
                NavigationBarItem(
                    selected = activeTab == "PROFILE",
                    onClick = { viewModel.selectTab("PROFILE") },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Minha Conta") },
                    label = { Text("Minha Conta", maxLines = 1) },
                    modifier = Modifier.testTag("nav_profile_tab")
                )
                NavigationBarItem(
                    selected = activeTab == "TECH_DOCS",
                    onClick = { viewModel.selectTab("TECH_DOCS") },
                    icon = { Icon(Icons.Default.IntegrationInstructions, contentDescription = "Dev Zone") },
                    label = { Text("Dev Hub", maxLines = 1) },
                    modifier = Modifier.testTag("nav_dev_tab")
                )
            }
        },
        containerColor = SurfaceBgLight
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Screen router state
            when {
                activeProduct != null -> {
                    ProductCustomizerScreen(viewModel = viewModel)
                }
                activeStore != null -> {
                    StoreMenuScreen(viewModel = viewModel)
                }
                else -> {
                    when (activeTab) {
                        "HOME" -> HomeScreen(viewModel = viewModel)
                        "SEARCH" -> SearchScreen(viewModel = viewModel)
                        "CART" -> CartScreen(
                            viewModel = viewModel,
                            onCheckoutClick = { showCheckoutDialog = true }
                        )
                        "PROFILE" -> ProfileScreen(viewModel = viewModel)
                        "TECH_DOCS" -> DevDocsScreen()
                    }
                }
            }
        }
    }

    // Checkout Details dialog
    if (showCheckoutDialog) {
        AlertDialog(
            onDismissRequest = { showCheckoutDialog = false },
            title = { Text("Revisão de Endereço & Pagamento") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Insira o endereço de entrega do seu bairro e o método de pagamento para concluir o pedido simulado.",
                        fontSize = 14.sp
                    )

                    OutlinedTextField(
                        value = checkoutAddress,
                        onValueChange = { checkoutAddress = it },
                        label = { Text("Endereço de Entrega") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = checkoutPayment,
                        onValueChange = { checkoutPayment = it },
                        label = { Text("Forma de Pagamento") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.checkoutActiveCart(checkoutAddress, checkoutPayment)
                        showCheckoutDialog = false
                    },
                    modifier = Modifier.testTag("confirm_order_dialog_btn")
                ) {
                    Text("Confirmar Pedido")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCheckoutDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

// 1. CLIENT / INÍCIO HOME SCREEN
@Composable
fun HomeScreen(viewModel: MarketplaceViewModel) {
    val approvedStores by viewModel.approvedStores.collectAsState()
    val activeUser by viewModel.currentUser.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // Welcome and Persona context Card header
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = PrimaryBlue.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Olá, ${activeUser?.name ?: "Visitante"}!",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = PrimaryBlue
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = when (activeUser?.role) {
                                "ADMIN" -> "Painel Administrativo: Aprove e gerencie lojas locais do bairro."
                                "MERCHANT" -> "Painel Lojista: Publique produtos, customize cardápios/variações e gerencie pedidos."
                                else -> "Compre de lojas perto de você: restaurantes, roupas, calçados e perfumarias."
                            },
                            fontSize = 12.sp,
                            color = OnSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(activeUser?.avatarColor ?: 0xFFFCFCFC.toInt())),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (activeUser?.name?.firstOrNull() ?: "?").toString(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }

        // Persona Portal View routing
        if (activeUser?.role == "ADMIN") {
            item { AdminPortalTab(viewModel) }
        } else if (activeUser?.role == "MERCHANT") {
            item { MerchantPortalTab(viewModel) }
        } else {
            // Customer core view
            // Search field trigger trigger
            item {
                OutlinedTextField(
                    value = "",
                    onValueChange = { },
                    placeholder = { Text("Buscar lojas, restaurantes e produtos do bairro...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PrimaryBlue) },
                    readOnly = true,
                    enabled = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clickable { viewModel.selectTab("SEARCH") },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor = PrimaryBlue.copy(alpha = 0.3f),
                        disabledPlaceholderColor = OnSurfaceVariant.copy(alpha = 0.7f)
                    )
                )
            }

            // Promotional campaign carousel
            item {
                FeaturedAdsCarousel()
            }

            // Categories list filter horizontal
            item {
                Text(
                    text = "Categorias recomendadas",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        CategoryRoundChip("Restaurantes", Icons.Default.Restaurant, Color(0xFFF9A825)) {
                            viewModel.selectTab("SEARCH")
                            viewModel.searchQuery.value = "Restaurante"
                        }
                    }
                    item {
                        CategoryRoundChip("Moda", Icons.Default.Checkroom, Color(0xFF1E88E5)) {
                            viewModel.selectTab("SEARCH")
                            viewModel.searchQuery.value = "Moda"
                        }
                    }
                    item {
                        CategoryRoundChip("Móveis", Icons.Default.Chair, Color(0xFF43A047)) {
                            viewModel.selectTab("SEARCH")
                            viewModel.searchQuery.value = "Móveis"
                        }
                    }
                    item {
                        CategoryRoundChip("Perfumaria", Icons.Default.SelfImprovement, Color(0xFFE91E63)) {
                            viewModel.selectTab("SEARCH")
                            viewModel.searchQuery.value = "Perfumaria"
                        }
                    }
                }
            }

            // Store lists
            item {
                Text(
                    text = "Lojas do Bairro em Destaque",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 12.dp)
                )
            }

            if (approvedStores.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Store,
                            contentDescription = null,
                            tint = OnSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Nenhuma loja aprovada disponível neste momento.",
                            color = OnSurfaceVariant,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(approvedStores) { store ->
                    StoreHorizontalCard(store = store, onClick = { viewModel.selectStore(store) })
                }
            }
        }
    }
}

@Composable
fun StoreHorizontalCard(store: StoreEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() }
            .testTag("store_card_${store.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, CardBorderClr)
    ) {
        Column {
            // Fake beautiful styled banner image via canvas drawing based on category
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(
                        Brush.linearGradient(
                            colors = when (store.type) {
                                "Restaurante" -> listOf(Color(0xFFFF8A65), Color(0xFFE64A19))
                                "Roupas e calçados" -> listOf(Color(0xFF64B5F6), Color(0xFF1565C0))
                                "Móveis" -> listOf(Color(0xFF81C784), Color(0xFF2E7D32))
                                else -> listOf(Color(0xFFF06292), Color(0xFFC2185B))
                            }
                        )
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                ) {
                    // Rating Pill Top Right
                    Box(
                        modifier = Modifier
                            .wrapContentSize()
                            .align(Alignment.TopEnd)
                            .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = store.rating.toString(), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Black)
                        }
                    }

                    // Shop Type Icon Bottom Left
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .align(Alignment.BottomStart)
                            .background(Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (store.type) {
                                "Restaurante" -> Icons.Default.Restaurant
                                "Roupas e calçados" -> Icons.Default.Checkroom
                                "Móveis" -> Icons.Default.Chair
                                else -> Icons.Default.SelfImprovement
                            },
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Info Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = store.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${store.type} • ${store.deliveryTime}",
                        color = OnSurfaceVariant,
                        fontSize = 12.sp
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (store.deliveryFee == 0.0) "Entrega Grátis" else "R$ %.2f".format(store.deliveryFee),
                        fontWeight = FontWeight.Bold,
                        color = if (store.deliveryFee == 0.0) TertiaryGreen else PrimaryBlue,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Ped. Mínimo: R$ %.2f".format(store.minOrder),
                        color = OnSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
fun FeaturedAdsCarousel() {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(top = 16.dp)
    ) {
        // Banner 1
        item {
            Card(
                modifier = Modifier
                    .width(280.dp)
                    .height(115.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF0D47A1), Color(0xFF1976D2))
                            )
                        )
                        .padding(16.dp)
                ) {
                    Column(modifier = Modifier.align(Alignment.CenterStart)) {
                        Text(text = "CONEXÃO DIGITAL", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Eletrônicos com 30% OFF", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = "Compre fones, carregadores e relógios para entrega imediata.", color = Color.White.copy(alpha = 0.9f), fontSize = 10.sp)
                    }
                }
            }
        }

        // Banner 2
        item {
            Card(
                modifier = Modifier
                    .width(280.dp)
                    .height(115.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFFD84315), Color(0xFFFF8F00))
                            )
                        )
                        .padding(16.dp)
                ) {
                    Column(modifier = Modifier.align(Alignment.CenterStart)) {
                        Text(text = "FESTIVAL LANCHES", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Taxa de Entrega Grátis", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = "Consulte pizzarias e hamburguerias parceiras do bairro.", color = Color.White.copy(alpha = 0.9f), fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryRoundChip(title: String, icon: ImageVector, iconBgColor: Color, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .width(80.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(iconBgColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconBgColor,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// 2. STORE MENU SCREEN (CARDÁPIO)
@Composable
fun StoreMenuScreen(viewModel: MarketplaceViewModel) {
    val store by viewModel.activeStore.collectAsState()
    val products by viewModel.getProductsByStoreFlow(store?.id ?: 0).collectAsState(emptyList())

    store?.let { st ->
        Column(modifier = Modifier.fillMaxSize()) {
            // Header back button banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.selectStore(null) }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = PrimaryBlue)
                }
                Text(
                    text = st.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.Black
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                // Info Section
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, CardBorderClr),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(st.type, color = PrimaryBlue, fontWeight = FontWeight.Bold)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp))
                                    Text(" ${st.rating}")
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Tempo estimado: ${st.deliveryTime}", fontSize = 13.sp)
                            Text("Entrega: " + if (st.deliveryFee == 0.0) "Grátis" else "R$ %.2f".format(st.deliveryFee), fontSize = 13.sp)
                        }
                    }
                }

                // List products with customizable option button options
                if (products.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Nenhum produto cadastrado nesta loja.", color = OnSurfaceVariant)
                        }
                    }
                } else {
                    item {
                        Text(
                            text = "Produtos & Serviços",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }

                    items(products) { prod ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, CardBorderClr),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clickable { viewModel.selectProduct(prod) }
                                .testTag("product_card_${prod.id}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = prod.name,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = prod.description,
                                        fontSize = 11.sp,
                                        color = OnSurfaceVariant,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "R$ %.2f".format(prod.price),
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryBlue
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    // Custom color category flag
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(PrimaryBlue.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = when (st.type) {
                                                "Restaurante" -> Icons.Default.Restaurant
                                                "Roupas e calçados" -> Icons.Default.Checkroom
                                                "Móveis" -> Icons.Default.Chair
                                                else -> Icons.Default.SelfImprovement
                                            },
                                            contentDescription = null,
                                            tint = PrimaryBlue
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Customizar",
                                        color = PrimaryBlue,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// 3. PRODUCT CUSTOMIZATION SCREEN (VARIATION CHIPS)
@Composable
fun ProductCustomizerScreen(viewModel: MarketplaceViewModel) {
    val prod by viewModel.activeProduct.collectAsState()
    val store by viewModel.activeStore.collectAsState()
    val variations by viewModel.activeProductVariations.collectAsState()
    val selectedVariations by viewModel.selectedVariationsMap.collectAsState()
    val quantity by viewModel.activeProductQuantity.collectAsState()

    prod?.let { pr ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Header banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.selectProduct(null) }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = PrimaryBlue)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Customizar Item",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        fontSize = 16.sp
                    )
                    Text(store?.name ?: "Loja Local", fontSize = 12.sp, color = OnSurfaceVariant)
                }
            }

            // Product generic presentation information
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.radialGradient(
                                colors = listOf(PrimaryBlue.copy(0.1f), PrimaryBlue.copy(0.2f))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (store?.type) {
                            "Restaurante" -> Icons.Default.Restaurant
                            "Roupas e calçados" -> Icons.Default.Checkroom
                            "Móveis" -> Icons.Default.Chair
                            else -> Icons.Default.SelfImprovement
                        },
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(64.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = pr.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = pr.description, fontSize = 13.sp, color = OnSurfaceVariant)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Preço Base: R$ %.2f".format(pr.price),
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue,
                    fontSize = 16.sp
                )

                Divider(modifier = Modifier.padding(vertical = 16.dp))

                // Group variations and output customizable selectors
                if (variations.isEmpty()) {
                    Text(
                        text = "Produto padrão de tamanho único disponível para adicionar ao carrinho.",
                        fontSize = 13.sp,
                        color = OnSurfaceVariant,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                } else {
                    variations.groupBy { it.groupName }.forEach { (groupName, options) ->
                        Text(
                            text = groupName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.Black,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        // If single choice: sizing sizing, coloring colors, etc.
                        val isSingleChoice = groupName == "Tamanho" || groupName == "Cor" || groupName == "Estilo" || groupName == "Combinação"
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            options.forEach { opt ->
                                val isSelected = selectedVariations[groupName]?.contains(opt.optionName) == true
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        viewModel.toggleVariationOption(groupName, opt, isSingleChoice)
                                    },
                                    label = {
                                        Text(
                                            text = if (opt.extraPrice == 0.0) opt.optionName
                                            else if (opt.extraPrice > 0.0) "${opt.optionName} (+R$ %.2f)".format(opt.extraPrice)
                                            else "${opt.optionName} (-R$ %.2f)".format(-opt.extraPrice)
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PrimaryBlue.copy(alpha = 0.15f),
                                        selectedLabelColor = PrimaryBlue
                                    ),
                                    modifier = Modifier.testTag("chip_${groupName}_${opt.optionName}")
                                )
                            }
                        }
                    }
                }

                // Quantity selector
                Divider(modifier = Modifier.padding(vertical = 16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Quantidade",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedIconButton(
                            onClick = { if (quantity > 1) viewModel.activeProductQuantity.value = quantity - 1 },
                            shape = CircleShape,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = null, tint = PrimaryBlue)
                        }

                        Text(
                            text = quantity.toString(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        OutlinedIconButton(
                            onClick = { viewModel.activeProductQuantity.value = quantity + 1 },
                            shape = CircleShape,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = PrimaryBlue)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Bottom Cart trigger details
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, CardBorderClr),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Total do Item", fontSize = 12.sp, color = OnSurfaceVariant)
                            Text(
                                text = "R$ %.2f".format(viewModel.getActiveProductTotalPrice()),
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue,
                                fontSize = 20.sp
                            )
                        }

                        Button(
                            onClick = { viewModel.addActiveProductToCart() },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            modifier = Modifier.testTag("add_item_to_cart_btn")
                        ) {
                            Icon(Icons.Default.AddShoppingCart, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Adicionar")
                        }
                    }
                }
            }
        }
    }
}

// 4. SEARCH SCREEN
@Composable
fun SearchScreen(viewModel: MarketplaceViewModel) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val approvedStores by viewModel.approvedStores.collectAsState()

    val filteredStores = remember(searchQuery, approvedStores) {
        if (searchQuery.isBlank()) approvedStores
        else approvedStores.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.type.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.searchQuery.value = it },
            placeholder = { Text("Buscar restaurantes, mercado, calçados...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PrimaryBlue) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Limpar")
                    }
                }
            },
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryBlue,
                unfocusedBorderColor = CardBorderClr
            )
        )

        // Category direct filters Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Restaurante", "Roupas e calçados", "Móveis", "Perfumaria").forEach { category ->
                val isSelected = searchQuery == category
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        viewModel.searchQuery.value = if (isSelected) "" else category
                    },
                    label = { Text(category) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryBlue,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("search_filter_$category")
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            if (filteredStores.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(64.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Nenhuma loja localizada para o filtro buscado.",
                            color = OnSurfaceVariant,
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        TextButton(onClick = { viewModel.searchQuery.value = "" }) {
                            Text("Limpar Busca")
                        }
                    }
                }
            } else {
                items(filteredStores) { store ->
                    StoreHorizontalCard(store = store, onClick = { viewModel.selectStore(store) })
                }
            }
        }
    }
}

// 5. CLIENT CART SCREEN
@Composable
fun CartScreen(
    viewModel: MarketplaceViewModel,
    onCheckoutClick: () -> Unit
) {
    val items by viewModel.cartItems.collectAsState()

    if (items.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = null,
                tint = OnSurfaceVariant.copy(alpha = 0.3f),
                modifier = Modifier.size(100.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Seu carrinho local está vazio.",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = OnSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Volte ao painel principal das lojas e adicione pizzas, roupas de linho ou calçados para ver a simulação relacional de checkout.",
                color = OnSurfaceVariant,
                textAlign = TextAlign.Center,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { viewModel.selectTab("HOME") },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("Explorar Lojas")
            }
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Text(
                            text = "Meu Carrinho",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color.Black
                        )
                        val numStores = items.map { it.storeId }.distinct().size
                        Text(
                            text = "Você tem ${items.sumOf { it.quantity }} itens de $numStores lojas diferentes do bairro.",
                            fontSize = 13.sp,
                            color = OnSurfaceVariant
                        )
                    }

                    items(items) { item ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, CardBorderClr),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.productName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = Color.Black
                                        )
                                        if (item.selectedVariations.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = item.selectedVariations,
                                                fontSize = 11.sp,
                                                color = TertiaryGreen,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "R$ %.2f cada".format(item.price),
                                            fontSize = 13.sp,
                                            color = OnSurfaceVariant
                                        )
                                    }

                                    IconButton(
                                        onClick = { viewModel.removeCartItem(item.id) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Remove",
                                            tint = ErrorRed,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                Divider(modifier = Modifier.padding(vertical = 12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        OutlinedIconButton(
                                            onClick = { viewModel.updateCartItemQuantity(item.id, false) },
                                            modifier = Modifier.size(32.dp),
                                            shape = CircleShape
                                        ) {
                                            Icon(Icons.Default.Remove, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(16.dp))
                                        }
                                        Text(
                                            text = item.quantity.toString(),
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 12.dp)
                                        )
                                        OutlinedIconButton(
                                            onClick = { viewModel.updateCartItemQuantity(item.id, true) },
                                            modifier = Modifier.size(32.dp),
                                            shape = CircleShape
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(16.dp))
                                        }
                                    }

                                    Text(
                                        text = "R$ %.2f".format(item.price * item.quantity),
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryBlue,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Summary Checkout panel
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                border = BorderStroke(1.dp, CardBorderClr),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    val subtotal = items.sumOf { it.price * it.quantity }
                    // Each unique store incurs 5.90 delivery
                    val uniqueStores = items.map { it.storeId }.distinct().size
                    val totalDelivery = uniqueStores * 5.90
                    val grandTotal = subtotal + totalDelivery

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Subtotal", color = OnSurfaceVariant)
                        Text("R$ %.2f".format(subtotal), fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Taxa de Entrega ($uniqueStores lojas)", color = OnSurfaceVariant)
                        Text("R$ %.2f".format(totalDelivery), fontWeight = FontWeight.Bold, color = TertiaryGreen)
                    }

                    Divider(modifier = Modifier.padding(vertical = 12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Total", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(
                            text = "R$ %.2f".format(grandTotal),
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue,
                            fontSize = 20.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { onCheckoutClick() },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("checkout_finish_btn")
                    ) {
                        Text("Finalizar Compra ($uniqueStores Pedidos)", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// 6. CLIENT ORDER STATS / PROFILE
@Composable
fun ProfileScreen(viewModel: MarketplaceViewModel) {
    val orders by viewModel.clientOrders.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Acompanhar Pedidos",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = Color.Black
        )
        Text(
            text = "Histórico de compras e timelines em tempo real das entregas.",
            fontSize = 12.sp,
            color = OnSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (orders.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, CardBorderClr),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.ListAlt, contentDescription = null, tint = OnSurfaceVariant, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Você ainda não fez nenhum pedido nesta conta.",
                        textAlign = TextAlign.Center,
                        color = OnSurfaceVariant
                    )
                }
            }
        } else {
            orders.forEach { ord ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, CardBorderClr),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Dynamic Badge status
                        val statusInfo = when (ord.status) {
                            "PENDING" -> Pair("Pendente", Color(0xFFFFB300))
                            "PREPARING" -> Pair("Preparo", Color(0xFF1E88E5))
                            "SHIPPED" -> Pair("A caminho", Color(0xFF43A047))
                            else -> Pair("Entregue", Color(0xFF757575))
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = ord.storeName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color.Black
                               )
                                Text(
                                    text = "Valor Total: R$ %.2f".format(ord.totalPrice),
                                    fontSize = 12.sp,
                                    color = PrimaryBlue,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .background(statusInfo.second.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = statusInfo.first,
                                    color = statusInfo.second,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Real-time timeline tracker visual
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val activeIndex = when (ord.status) {
                                "PENDING" -> 0
                                "PREPARING" -> 1
                                "SHIPPED" -> 2
                                else -> 3
                            }

                            val steps = listOf("Pendente", "Preparo", "Trânsito", "Entregue")
                            steps.forEachIndexed { idx, label ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (idx <= activeIndex) statusInfo.second else Color.LightGray
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (idx < activeIndex) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                        } else {
                                            Text(text = (idx + 1).toString(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = label,
                                        fontSize = 10.sp,
                                        fontWeight = if (idx == activeIndex) FontWeight.Bold else FontWeight.Normal,
                                        color = if (idx == activeIndex) statusInfo.second else OnSurfaceVariant
                                    )
                                }

                                if (idx < 3) {
                                    Divider(
                                        color = if (idx < activeIndex) statusInfo.second else Color.LightGray,
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(bottom = 12.dp)
                                    )
                                }
                            }
                        }

                        // Loaded inner item details details
                        var expandedOrder by remember { mutableStateOf(false) }
                        var nestedItems by remember { mutableStateOf<List<OrderItemEntity>>(emptyList()) }

                        TextButton(
                            onClick = {
                                expandedOrder = !expandedOrder
                                if (expandedOrder) {
                                    coroutineScope.launch {
                                        nestedItems = viewModel.repository.getOrderItems(ord.id)
                                    }
                                }
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text(if (expandedOrder) "Ocultar Detalhes" else "Ver Detalhes do Pedido", fontSize = 12.sp)
                        }

                        if (expandedOrder) {
                            Column(modifier = Modifier.padding(top = 8.dp)) {
                                Text("Endereço: " + ord.deliveryAddress, fontSize = 11.sp, color = OnSurfaceVariant)
                                Text("Pagamento: " + ord.paymentMethod, fontSize = 11.sp, color = OnSurfaceVariant)
                                Spacer(modifier = Modifier.height(8.dp))

                                nestedItems.forEach { item ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = "${item.quantity}x ${item.productName}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text(text = "R$ %.2f".format(item.pricePerUnit * item.quantity), fontSize = 12.sp, color = PrimaryBlue)
                                    }
                                    if (item.selectedVariations.isNotEmpty()) {
                                        Text(text = item.selectedVariations, fontSize = 10.sp, color = TertiaryGreen)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// 7. DEVELOPER DOCUMENTS ZONE TAB
@Composable
fun DevDocsScreen() {
    var activeSubTab by remember { mutableStateOf("SCHEMA") } // "SCHEMA", "VARIATIONS", "ROUTERS", "ARCH"
    val clipboard = LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Central do Desenvolvedor",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = Color.Black
        )
        Text(
            text = "Código de backend Node.js, Banco de dados e Solução de Escalabilidade pedida.",
            fontSize = 12.sp,
            color = OnSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                Pair("Modelagem DB", "SCHEMA"),
                Pair("Opcionais e Variáveis", "VARIATIONS"),
                Pair("Rotas Express/Node", "ROUTERS"),
                Pair("Arquitetura e Escala", "ARCH")
            ).forEach { (lbl, subTab) ->
                val isSelected = activeSubTab == subTab
                FilterChip(
                    selected = isSelected,
                    onClick = { activeSubTab = subTab },
                    label = { Text(lbl) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryBlue,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("dev_tab_$subTab")
                )
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .background(Color(0xFF232429), RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFF33353D), RoundedCornerShape(12.dp))
                .padding(8.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (activeSubTab) {
                            "SCHEMA" -> "PostgreSQL DDL Schema"
                            "VARIATIONS" -> "Como modelar Cardápios vs Roupas"
                            "ROUTERS" -> "Rotas Principais (Express.js)"
                            else -> "Padrões de Alta Disponibilidade"
                        },
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    val textToCopy = when (activeSubTab) {
                        "SCHEMA" -> DevDocContent.DB_SCHEMA_SQL
                        "VARIATIONS" -> DevDocContent.EXPLANATION_VARIATIONS
                        "ROUTERS" -> DevDocContent.BACKEND_ROUTERS
                        else -> DevDocContent.SCALABILITY_SUGGESTION
                    }

                    TextButton(
                        onClick = { clipboard.setText(AnnotatedString(textToCopy)) }
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copiar", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copiar Cód.", color = Color.LightGray, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    SelectionContainer {
                        Text(
                            text = when (activeSubTab) {
                                "SCHEMA" -> DevDocContent.DB_SCHEMA_SQL
                                "VARIATIONS" -> DevDocContent.EXPLANATION_VARIATIONS
                                "ROUTERS" -> DevDocContent.BACKEND_ROUTERS
                                else -> DevDocContent.SCALABILITY_SUGGESTION
                            },
                            color = Color(0xFFA9B2C3),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }
    }
}

// 8. LOJISTA PORTAL TAB
@Composable
fun MerchantPortalTab(viewModel: MarketplaceViewModel) {
    val store by viewModel.merchantStore.collectAsState()
    val orders by viewModel.merchantOrders.collectAsState()

    var showAddProductDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        if (store == null) {
            Text(
                text = "Cadastrar minha Loja",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.Black
            )
            Text(
                text = "Insira as credenciais do seu estabelecimento de bairro abaixo.",
                fontSize = 12.sp,
                color = OnSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            var nameInput by remember { mutableStateOf("") }
            var typeInput by remember { mutableStateOf("Restaurante") }
            var minOrderInput by remember { mutableStateOf("20.00") }

            OutlinedTextField(
                value = nameInput,
                onValueChange = { nameInput = it },
                label = { Text("Nome Fantasia da Loja") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text("Ramo de Atuação / Segmento", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Restaurante", "Roupas e calçados", "Móveis", "Perfumaria").forEach { opt ->
                    val isSelected = typeInput == opt
                    FilterChip(
                        selected = isSelected,
                        onClick = { typeInput = opt },
                        label = { Text(opt) },
                        modifier = Modifier.testTag("merchant_segment_chip_$opt")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = minOrderInput,
                onValueChange = { minOrderInput = it },
                label = { Text("Valor do Pedido Mínimo (R$)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (nameInput.isNotBlank()) {
                        viewModel.registerNewStoreByMerchant(
                            name = nameInput,
                            type = typeInput,
                            minOrder = minOrderInput.toDoubleOrNull() ?: 20.00
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("register_store_merchant_btn")
            ) {
                Text("Confirmar Termos e Solicitar Abertura")
            }
        } else {
            // Store exists and is approved or pending
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = store!!.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.Black
                    )
                    Text(
                        text = "Ramo: ${store!!.type} • Ped. Mín: R${'$'} %.2f".format(store!!.minOrder),
                        fontSize = 12.sp,
                        color = OnSurfaceVariant
                    )
                }

                // Status banner
                val s = store!!.status
                Box(
                    modifier = Modifier
                        .background(
                            if (s == "APPROVED") TertiaryGreen.copy(0.12f) else ErrorRed.copy(0.12f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (s == "APPROVED") "Aprovada" else "Aguardando Aprovação",
                        color = if (s == "APPROVED") TertiaryGreen else ErrorRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (store!!.status != "APPROVED") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(0.04f)),
                    border = BorderStroke(1.dp, ErrorRed.copy(0.2f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Sua loja está aguardando homologação do Administrador.",
                            fontWeight = FontWeight.Bold,
                            color = ErrorRed
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Como lojista simulado, você pode mudar o login para o usuário 'ADMIN' no menu de conta no canto superior direito para aprovar sua própria loja em 1 clique!",
                            fontSize = 12.sp,
                            color = OnSurfaceVariant
                        )
                    }
                }
            } else {
                // Approved: Store manager buttons
                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Gerenciar Pedidos Recebidos", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Button(
                        onClick = { showAddProductDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = TertiaryGreen),
                        modifier = Modifier.testTag("open_merchant_add_product_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Adicionar Produto", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (orders.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Nenhum pedido recebido ainda nesta loja.", color = OnSurfaceVariant)
                    }
                } else {
                    orders.forEach { ord ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, CardBorderClr),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Pedido #${ord.id}", fontWeight = FontWeight.Bold, color = Color.Black)
                                        Text("Total: R$ %.2f".format(ord.totalPrice), fontSize = 12.sp, color = PrimaryBlue)
                                        Text("Endereço: ${ord.deliveryAddress}", fontSize = 11.sp, color = OnSurfaceVariant)
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = when (ord.status) {
                                                "PENDING" -> "Pendente"
                                                "PREPARING" -> "Preparando"
                                                "SHIPPED" -> "A Caminho"
                                                else -> "Entregue"
                                            },
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = when (ord.status) {
                                                "PENDING" -> Color(0xFFFFB300)
                                                "PREPARING" -> Color(0xFF1E88E5)
                                                "SHIPPED" -> Color(0xFF43A047)
                                                else -> Color(0xFF757575)
                                            }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Alterar status:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    
                                    if (ord.status == "PENDING") {
                                        Button(
                                            onClick = { viewModel.updateMerchantOrderStatus(ord.id, "PREPARING") },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                            modifier = Modifier.testTag("action_prepare_${ord.id}")
                                        ) {
                                            Text("Preparar", fontSize = 10.sp)
                                        }
                                    }

                                    if (ord.status == "PREPARING") {
                                        Button(
                                            onClick = { viewModel.updateMerchantOrderStatus(ord.id, "SHIPPED") },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047)),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                            modifier = Modifier.testTag("action_ship_${ord.id}")
                                        ) {
                                            Text("Despachar", fontSize = 10.sp)
                                        }
                                    }

                                    if (ord.status == "SHIPPED") {
                                        Button(
                                            onClick = { viewModel.updateMerchantOrderStatus(ord.id, "DELIVERED") },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF757575)),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                            modifier = Modifier.testTag("action_deliver_${ord.id}")
                                        ) {
                                            Text("Concluir", fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddProductDialog) {
        var prodName by remember { mutableStateOf("") }
        var prodDesc by remember { mutableStateOf("") }
        var prodPrice by remember { mutableStateOf("") }
        var prodCategory by remember { mutableStateOf("Principal") }

        // Customizable variation presets configuration checkbox (To demonstrate variations!)
        var applyPizzaPreset by remember { mutableStateOf(false) }
        var applyClothesPreset by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAddProductDialog = false },
            title = { Text("Adicionar Novo Produto") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = prodName,
                        onValueChange = { prodName = it },
                        label = { Text("Nome do Produto") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = prodDesc,
                        onValueChange = { prodDesc = it },
                        label = { Text("Descrição Detalhada") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = prodPrice,
                        onValueChange = { prodPrice = it },
                        label = { Text("Preço (R$)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = prodCategory,
                        onValueChange = { prodCategory = it },
                        label = { Text("Categoria do Cardápio / Setor") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Matriz de Variações Simulada:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = applyPizzaPreset, onCheckedChange = {
                            applyPizzaPreset = it
                            if (it) applyClothesPreset = false
                        })
                        Text("Variações de Pizzaria (Broto, Grande, Borda)", fontSize = 12.sp)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = applyClothesPreset, onCheckedChange = {
                            applyClothesPreset = it
                            if (it) applyPizzaPreset = false
                        })
                        Text("Variações de Moda (P, M, G, Cores)", fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val priceNum = prodPrice.toDoubleOrNull() ?: 10.00
                        val vars = mutableListOf<Pair<String, List<Pair<String, Double>>>>()

                        if (applyPizzaPreset) {
                            vars.add(Pair("Tamanho", listOf(Pair("Pequena", -10.00), Pair("Média", 0.0), Pair("Grande", 12.00))))
                            vars.add(Pair("Borda", listOf(Pair("Sem Borda", 0.0), Pair("Borda Catupiry", 8.00))))
                        } else if (applyClothesPreset) {
                            vars.add(Pair("Tamanho", listOf(Pair("P", 0.0), Pair("M", 0.0), Pair("G", 0.0))))
                            vars.add(Pair("Cor", listOf(Pair("Azul", 0.0), Pair("Verde", 0.0), Pair("Branco", 0.0))))
                        }

                        viewModel.addProductByMerchant(
                            name = prodName,
                            description = prodDesc,
                            price = priceNum,
                            category = prodCategory,
                            variationsInput = vars
                        )
                        showAddProductDialog = false
                    },
                    modifier = Modifier.testTag("submit_merchant_product_btn")
                ) {
                    Text("Salvar Produto")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddProductDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

// 9. ADMIN PORTAL TAB
@Composable
fun AdminPortalTab(viewModel: MarketplaceViewModel) {
    val stores by viewModel.allStores.collectAsState()
    val orders by viewModel.allOrders.collectAsState()

    val pendingStores = remember(stores) { stores.filter { it.status == "PENDING" } }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Homologação de Lojas",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = Color.Black
        )
        Text(
            text = "Verifique solicitações de novos estabelecimentos querendo vender no bairro.",
            fontSize = 12.sp,
            color = OnSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (pendingStores.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = TertiaryGreen.copy(0.04f)),
                border = BorderStroke(1.dp, TertiaryGreen.copy(0.2f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = TertiaryGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Nenhuma loja pendente de aprovação no momento.", color = Color.Black, fontSize = 13.sp)
                }
            }
        } else {
            pendingStores.forEach { st ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, CardBorderClr)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = st.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(text = "Ramo: ${st.type} | Proprietário ID: ${st.ownerId}", fontSize = 12.sp, color = OnSurfaceVariant)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { viewModel.adminApproveStore(st.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = TertiaryGreen),
                                modifier = Modifier.testTag("admin_approve_${st.id}")
                            ) {
                                Text("Aprovar Loja")
                            }

                            OutlinedButton(
                                onClick = { viewModel.adminRejectStore(st.id) },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                                modifier = Modifier.testTag("admin_reject_${st.id}")
                            ) {
                                Text("Rejeitar")
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "Métricas Plataforma (Global)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, CardBorderClr),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total de Pedidos:")
                    Text(text = orders.size.toString(), fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Vendas Brutas:")
                    Text(text = "R$ %.2f".format(orders.sumOf { it.totalPrice }), fontWeight = FontWeight.Bold, color = TertiaryGreen)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Lojas Registradas:")
                    Text(text = stores.size.toString(), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
