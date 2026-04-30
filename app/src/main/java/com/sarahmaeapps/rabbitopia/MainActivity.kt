package com.sarahmaeapps.rabbitopia

import android.os.Bundle
import android.os.Build
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.firestore.FirebaseFirestore
import com.sarahmaeapps.rabbitopia.data.ChatRepository
import com.sarahmaeapps.rabbitopia.model.ChatMessage
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.sarahmaeapps.rabbitopia.ui.screens.*
import com.sarahmaeapps.rabbitopia.ui.theme.RabbitopiaTheme

class MainActivity : ComponentActivity() {
    private val chatRepository = ChatRepository()
    private val adminEmail = "rabbitopiafarm@gmail.com"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        createNotificationChannel()
        fetchAndSaveFCMToken()
        startGlobalMessageListener()

        setContent {
            RabbitopiaTheme {
                var showSplash by remember { mutableStateOf(true) }
                
                // Permission Request for Android 13+
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val launcher = rememberLauncherForActivityResult(
                        ActivityResultContracts.RequestPermission()
                    ) { isGranted ->
                        // Handle result if needed
                    }
                    LaunchedEffect(Unit) {
                        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }

                if (showSplash) {
                    SplashScreen(onAnimationFinished = { showSplash = false })
                } else {
                    AppBackground {
                        RabbitopiaApp()
                    }
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Chat Messages"
            val descriptionText = "Notifications for messages from customers"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("CHAT_CHANNEL", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun fetchAndSaveFCMToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) return@addOnCompleteListener
            val token = task.result
            val email = adminEmail
            
            FirebaseFirestore.getInstance()
                .collection("admins")
                .document(email)
                .set(mapOf("fcmToken" to token), com.google.firebase.firestore.SetOptions.merge())
        }
    }

    private fun startGlobalMessageListener() {
        lifecycleScope.launch {
            chatRepository.getUnreadMessages(adminEmail).collectLatest { unreadMessages ->
                unreadMessages.forEach { msg ->
                    // Trigger a notification if the message is from a customer (not from self)
                    // and it hasn't been notified yet (isRead check is usually enough for new ones)
                    showNotification(msg.senderId, msg.text)
                }
            }
        }
    }

    private fun showNotification(sender: String, text: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = androidx.core.app.NotificationCompat.Builder(this, "CHAT_CHANNEL")
            .setSmallIcon(R.mipmap.rabbitopia_launcher)
            .setContentTitle("New Message from $sender")
            .setContentText(text)
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI)
            .build()

        notificationManager.notify(sender.hashCode(), notification)
    }
}

@Composable
fun AppBackground(content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.fur),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        content()
    }
}

@Composable
fun RabbitopiaApp() {
    val navController = rememberNavController()
    val auth = FirebaseAuth.getInstance()
    var user by remember { mutableStateOf(auth.currentUser) }

    if (user == null) {
        LoginScreen(onLoginSuccess = { user = auth.currentUser })
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "home",
                modifier = Modifier.padding(innerPadding)
            ) {
                composable("home") {
                    HomeScreen(
                        onNavigateToRabbits = { navController.navigate("rabbits") },
                        onNavigateToFeed = { navController.navigate("feed") },
                        onNavigateToCustomers = { navController.navigate("customers") },
                        onNavigateToHousing = { navController.navigate("housing") },
                        onNavigateToSales = { navController.navigate("sales_records") },
                        onNavigateToMedical = { navController.navigate("medical") },
                        onNavigateToCulls = { navController.navigate("culled_rabbits") }
                    )
                }
                composable("rabbits") { 
                    RabbitsListScreen(
                        onNavigateToRabbitDetail = { id -> navController.navigate("rabbit_detail/$id") },
                        onNavigateToAddRabbit = { navController.navigate("add_rabbit") },
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                composable("add_rabbit") { 
                    AddEditRabbitScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                composable(
                    route = "rabbit_detail/{rabbitId}",
                    arguments = listOf(navArgument("rabbitId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val rabbitId = backStackEntry.arguments?.getString("rabbitId") ?: ""
                    RabbitDetailScreen(
                        rabbitId = rabbitId,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToEdit = { id -> navController.navigate("edit_rabbit/$id") },
                        onNavigateToPedigree = { id -> navController.navigate("pedigree/$id") },
                        onNavigateToSOP = { id -> navController.navigate("sop/$id") },
                        onNavigateToSaleCull = { id -> navController.navigate("sale_cull/$id") },
                        onNavigateToWeighIn = { id -> navController.navigate("weigh_in/$id") },
                        onNavigateToBreeding = { id -> navController.navigate("breeding/$id") },
                        onNavigateToLitters = { id -> navController.navigate("litters/$id") },
                        onNavigateToMedical = { navController.navigate("medical") }
                    )
                }
                composable(
                    route = "edit_rabbit/{rabbitId}",
                    arguments = listOf(navArgument("rabbitId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val rabbitId = backStackEntry.arguments?.getString("rabbitId")
                    AddEditRabbitScreen(
                        rabbitId = rabbitId,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                composable(
                    route = "pedigree/{rabbitId}",
                    arguments = listOf(navArgument("rabbitId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val rabbitId = backStackEntry.arguments?.getString("rabbitId") ?: ""
                    PedigreeScreen(rabbitId = rabbitId, onNavigateBack = { navController.popBackStack() })
                }
                
                composable("feed") { FodderScreen(onNavigateBack = { navController.popBackStack() }) }
                composable("customers") { 
                    CustomersScreen(
                        onNavigateToCustomerDetail = { id -> navController.navigate("customer_detail/$id") },
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                composable(
                    route = "customer_detail/{customerId}",
                    arguments = listOf(navArgument("customerId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val customerId = backStackEntry.arguments?.getString("customerId") ?: ""
                    CustomerDetailScreen(
                        customerId = customerId, 
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToChat = { id, name -> navController.navigate("chat/$id/$name") },
                        onNavigateToRabbitDetail = { id -> navController.navigate("rabbit_detail/$id") },
                        onNavigateToPedigree = { id -> navController.navigate("pedigree/$id") }
                    )
                }
                composable(
                    route = "chat/{customerEmail}/{customerName}",
                    arguments = listOf(
                        navArgument("customerEmail") { type = NavType.StringType },
                        navArgument("customerName") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val customerEmail = java.net.URLDecoder.decode(backStackEntry.arguments?.getString("customerEmail") ?: "", "UTF-8")
                    val customerName = backStackEntry.arguments?.getString("customerName") ?: ""
                    ChatScreen(
                        customerEmail = customerEmail,
                        customerName = customerName,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                composable("housing") { 
                    HousingScreen(
                        onNavigateToDetail = { id -> navController.navigate("hutch_detail/$id") },
                        onNavigateBack = { navController.popBackStack() }
                    ) 
                }
                composable(
                    route = "hutch_detail/{hutchId}",
                    arguments = listOf(navArgument("hutchId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val hutchId = backStackEntry.arguments?.getString("hutchId") ?: ""
                    HutchDetailScreen(hutchId = hutchId, onNavigateBack = { navController.popBackStack() })
                }
                composable("sales_records") {
                    SalesScreen(
                        onNavigateToSaleDetail = { id -> navController.navigate("sale_detail/$id") },
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                composable(
                    route = "sale_detail/{saleId}",
                    arguments = listOf(navArgument("saleId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val saleId = backStackEntry.arguments?.getString("saleId") ?: ""
                    SaleDetailScreen(saleId = saleId, onNavigateBack = { navController.popBackStack() })
                }
                composable("medical") { 
                    MedicalScreen(
                        onNavigateToDetail = { id -> navController.navigate("medical_detail/$id") },
                        onNavigateBack = { navController.popBackStack() }
                    ) 
                }
                composable("medical_detail/{recordId}",
                    arguments = listOf(navArgument("recordId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val recordId = backStackEntry.arguments?.getString("recordId") ?: ""
                    MedicalDetailScreen(recordId = recordId, onNavigateBack = { navController.popBackStack() })
                }
                
                composable("culled_rabbits") {
                    CulledRabbitsScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToDetail = { id -> navController.navigate("rabbit_detail/$id") }
                    )
                }

                // New animal specific routes
                composable("sop/{rabbitId}") { backStackEntry ->
                    val rabbitId = backStackEntry.arguments?.getString("rabbitId") ?: ""
                    SOPChecklistScreen(rabbitId = rabbitId, onNavigateBack = { navController.popBackStack() })
                }
                composable("sale_cull/{rabbitId}") { backStackEntry ->
                    val rabbitId = backStackEntry.arguments?.getString("rabbitId") ?: ""
                    SaleCullScreen(rabbitId = rabbitId, onNavigateBack = { navController.popBackStack() })
                }
                composable("weigh_in/{rabbitId}") { backStackEntry ->
                    val rabbitId = backStackEntry.arguments?.getString("rabbitId") ?: ""
                    WeighInScreen(rabbitId = rabbitId, onNavigateBack = { navController.popBackStack() })
                }
                composable("breeding/{rabbitId}") { backStackEntry ->
                    val rabbitId = backStackEntry.arguments?.getString("rabbitId") ?: ""
                    BreedingHistoryScreen(
                        rabbitId = rabbitId, 
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToAdd = { id -> navController.navigate("add_breeding/$id") },
                        onNavigateToDetail = { id -> navController.navigate("breeding_detail/$id") }
                    )
                }
                composable("add_breeding/{rabbitId}") { backStackEntry ->
                    val rabbitId = backStackEntry.arguments?.getString("rabbitId") ?: ""
                    BreedingScreen(rabbitId = rabbitId, onNavigateBack = { navController.popBackStack() })
                }
                composable("breeding_detail/{eventId}") { backStackEntry ->
                    val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
                    BreedingDetailScreen(eventId = eventId, onNavigateBack = { navController.popBackStack() })
                }
                composable("litters/{rabbitId}") { backStackEntry ->
                    val rabbitId = backStackEntry.arguments?.getString("rabbitId") ?: ""
                    LittersScreen(
                        rabbitId = rabbitId, 
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToLitterDetail = { id -> navController.navigate("litter_detail/$id") }
                    )
                }
                composable("litter_detail/{litterId}") { backStackEntry ->
                    val litterId = backStackEntry.arguments?.getString("litterId") ?: ""
                    LitterDetailScreen(litterId = litterId, onNavigateBack = { navController.popBackStack() })
                }
            }
        }
    }
}
