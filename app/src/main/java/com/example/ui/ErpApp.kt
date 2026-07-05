package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Employee
import com.example.ui.theme.BrightEmerald
import com.example.ui.theme.TansionGoTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ErpApp(viewModel: ErpViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()

    TansionGoTheme {
        if (currentUser == null) {
            LoginScreen(viewModel = viewModel)
        } else {
            val user = currentUser!!
            val configuration = LocalConfiguration.current
            val isWideScreen = configuration.screenWidthDp >= 600

            // Navigation state
            var currentPillar by remember { mutableStateOf("dashboard") }

            // Sub-sections tracker for nested modules
            var activeSalesSubSection by remember { mutableStateOf("leads") }
            var activeOpsSubSection by remember { mutableStateOf("tasks") }
            var activeCollabSubSection by remember { mutableStateOf("chat") }
            var showProfileDialog by remember { mutableStateOf(false) }

            Row(modifier = Modifier.fillMaxSize()) {
                // Adaptive Navigation Rail (For wide/tablet/desktop screens)
                if (isWideScreen) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .fillMaxHeight()
                            .border(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Icon(
                            imageVector = Icons.Default.Business,
                            contentDescription = null,
                            tint = BrightEmerald,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.weight(1f))

                        NavigationRailItem(
                            selected = currentPillar == "dashboard",
                            onClick = { currentPillar = "dashboard" },
                            icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                            label = { Text("Dashboard", fontSize = 10.sp) }
                        )

                        NavigationRailItem(
                            selected = currentPillar == "sales",
                            onClick = { currentPillar = "sales" },
                            icon = { Icon(Icons.Default.Leaderboard, contentDescription = "Sales") },
                            label = { Text("Sales & CRM", fontSize = 10.sp) }
                        )

                        NavigationRailItem(
                            selected = currentPillar == "operations",
                            onClick = { currentPillar = "operations" },
                            icon = { Icon(Icons.Default.Assignment, contentDescription = "Ops") },
                            label = { Text("Operations", fontSize = 10.sp) }
                        )

                        NavigationRailItem(
                            selected = currentPillar == "collab",
                            onClick = { currentPillar = "collab" },
                            icon = { Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Collab") },
                            label = { Text("Collab", fontSize = 10.sp) }
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        IconButton(onClick = { viewModel.logout() }) {
                            Icon(Icons.Default.ExitToApp, contentDescription = "Log Out", tint = Color.Red)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                // Primary Content Area
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Business,
                                        contentDescription = null,
                                        tint = BrightEmerald,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "TansionGO ERP",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        letterSpacing = (-0.5).sp
                                    )
                                }
                            },
                            actions = {
                                // Notification Badge indicator
                                val allNotifications by viewModel.notifications.collectAsState()
                                val myUnreadNotifs = allNotifications.filter { (it.recipientEmployeeId == user.employeeId || it.recipientEmployeeId == "ALL") && !it.isRead }

                                Box(
                                    modifier = Modifier
                                        .padding(end = 8.dp)
                                        .clickable {
                                            myUnreadNotifs.forEach { viewModel.markNotificationAsRead(it.notificationId) }
                                        }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = "Notifications list",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (myUnreadNotifs.isNotEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(Color.Red)
                                                .align(Alignment.TopEnd)
                                        )
                                    }
                                }

                                Text(
                                    text = user.employeeId,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable { showProfileDialog = true }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                IconButton(
                                    onClick = { viewModel.logout() },
                                    modifier = Modifier.testTag("logout_button")
                                ) {
                                    Icon(Icons.Default.ExitToApp, contentDescription = "Logout icon", tint = Color.Red)
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                titleContentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.border(0.dp, Color.Transparent)
                        )
                    },
                    bottomBar = {
                        // Compact bottom bar (For Mobile layout)
                        if (!isWideScreen) {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f))
                            ) {
                                NavigationBarItem(
                                    selected = currentPillar == "dashboard",
                                    onClick = { currentPillar = "dashboard" },
                                    icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
                                    label = { Text("Dashboard", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                                )

                                NavigationBarItem(
                                    selected = currentPillar == "sales",
                                    onClick = { currentPillar = "sales" },
                                    icon = { Icon(Icons.Default.Leaderboard, contentDescription = null) },
                                    label = { Text("Sales & CRM", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                                )

                                NavigationBarItem(
                                    selected = currentPillar == "operations",
                                    onClick = { currentPillar = "operations" },
                                    icon = { Icon(Icons.Default.Assignment, contentDescription = null) },
                                    label = { Text("Operations", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                                )

                                NavigationBarItem(
                                    selected = currentPillar == "collab",
                                    onClick = { currentPillar = "collab" },
                                    icon = { Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null) },
                                    label = { Text("Collab", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        AnimatedContent(
                            targetState = currentPillar,
                            label = "pillar_animations"
                        ) { pillar ->
                            when (pillar) {
                                "dashboard" -> DashboardScreen(
                                    viewModel = viewModel,
                                    user = user,
                                    onNavigateToSection = { targetSection ->
                                        when (targetSection) {
                                            "employees" -> {
                                                currentPillar = "operations"
                                                activeOpsSubSection = "employees"
                                            }
                                            "leads" -> {
                                                currentPillar = "sales"
                                                activeSalesSubSection = "leads"
                                            }
                                        }
                                    }
                                )

                                "sales" -> {
                                    // Pillar 2: Sales & CRM sub-sections switcher
                                    Column(modifier = Modifier.fillMaxSize()) {
                                        TabRow(
                                            selectedTabIndex = when (activeSalesSubSection) {
                                                "leads" -> 0
                                                "clients" -> 1
                                                else -> 2
                                            }
                                        ) {
                                            Tab(
                                                selected = activeSalesSubSection == "leads",
                                                onClick = { activeSalesSubSection = "leads" }
                                            ) {
                                                Text("Leads Sourcing", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            }
                                            Tab(
                                                selected = activeSalesSubSection == "clients",
                                                onClick = { activeSalesSubSection = "clients" }
                                            ) {
                                                Text("Corporate Clients", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            }
                                            Tab(
                                                selected = activeSalesSubSection == "search",
                                                onClick = { activeSalesSubSection = "search" }
                                            ) {
                                                Text("Search Indices", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            }
                                        }

                                        when (activeSalesSubSection) {
                                            "leads" -> LeadsSection(viewModel = viewModel, user = user)
                                            "clients" -> ClientsSection(viewModel = viewModel, user = user)
                                            "search" -> SearchSection(viewModel = viewModel, user = user)
                                        }
                                    }
                                }

                                "operations" -> {
                                    // Pillar 3: Operations (Tasks, Daily Reports, Attendance punch logs, Staff Directory if admin)
                                    Column(modifier = Modifier.fillMaxSize()) {
                                        val opsTabs = if (user.role == "Super Admin") {
                                            listOf("tasks", "attendance", "work_reports", "employees")
                                        } else {
                                            listOf("tasks", "attendance", "work_reports")
                                        }

                                        TabRow(
                                            selectedTabIndex = opsTabs.indexOf(activeOpsSubSection).coerceAtLeast(0)
                                        ) {
                                            opsTabs.forEach { tabKey ->
                                                val labelText = when (tabKey) {
                                                    "tasks" -> "Milestones"
                                                    "attendance" -> "Punch Logs"
                                                    "work_reports" -> "Daily Reports"
                                                    else -> "Staff Directory"
                                                }
                                                Tab(
                                                    selected = activeOpsSubSection == tabKey,
                                                    onClick = { activeOpsSubSection = tabKey }
                                                ) {
                                                    Text(labelText, modifier = Modifier.padding(10.dp), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                                }
                                            }
                                        }

                                        when (activeOpsSubSection) {
                                            "tasks" -> TasksSection(viewModel = viewModel, user = user)
                                            "attendance" -> AttendanceSection(viewModel = viewModel, user = user)
                                            "work_reports" -> DailyReportsSection(viewModel = viewModel, user = user)
                                            "employees" -> EmployeesSection(viewModel = viewModel, user = user)
                                        }
                                    }
                                }

                                "collab" -> {
                                    // Pillar 4: Collaboration (Chat, Documents, Reports PDF/Excel, Company settings if admin)
                                    Column(modifier = Modifier.fillMaxSize()) {
                                        val collabTabs = if (user.role == "Super Admin") {
                                            listOf("chat", "documents", "exports", "settings")
                                        } else {
                                            listOf("chat", "documents")
                                        }

                                        TabRow(
                                            selectedTabIndex = collabTabs.indexOf(activeCollabSubSection).coerceAtLeast(0)
                                        ) {
                                            collabTabs.forEach { tabKey ->
                                                val labelText = when (tabKey) {
                                                    "chat" -> "Chat Room"
                                                    "documents" -> "Documents Hub"
                                                    "exports" -> "Reports"
                                                    else -> "Settings"
                                                }
                                                Tab(
                                                    selected = activeCollabSubSection == tabKey,
                                                    onClick = { activeCollabSubSection = tabKey }
                                                ) {
                                                    Text(labelText, modifier = Modifier.padding(10.dp), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                                }
                                            }
                                        }

                                        when (activeCollabSubSection) {
                                            "chat" -> ChatSection(viewModel = viewModel, user = user)
                                            "documents" -> DocumentsSection(viewModel = viewModel, user = user)
                                            "exports" -> ReportsSection(viewModel = viewModel, user = user)
                                            "settings" -> SettingsSection(viewModel = viewModel, user = user)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (showProfileDialog) {
                ProfileDialog(
                    user = user,
                    onDismiss = { showProfileDialog = false },
                    onSavePassword = { newPass ->
                        viewModel.updateSelfPassword(newPass)
                    }
                )
            }
        }
    }
}

@Composable
fun ProfileDialog(
    user: Employee,
    onDismiss: () -> Unit,
    onSavePassword: (String) -> Unit
) {
    var newPassword by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = "My Profile & Security",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Employee Details",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("ID:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(user.employeeId, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Name:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(user.name, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Role:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(user.role, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Dept:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(user.department, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Desg:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(user.designation, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Change Login Password",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password") },
                    placeholder = { Text("Enter new password") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Text(
                    text = "Note: Your Employee ID (${user.employeeId}) is your permanent company identifier and cannot be changed directly for security audit reasons. Contact CEO Admin for ID modifications.",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 14.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (newPassword.isNotBlank()) {
                        onSavePassword(newPassword.trim())
                        onDismiss()
                    }
                },
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Change Password")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
