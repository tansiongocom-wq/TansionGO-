package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.WatchLater
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ActivityLog
import com.example.data.Attendance
import com.example.data.Client
import com.example.data.Employee
import com.example.data.Lead
import com.example.data.Notification
import com.example.data.Payment
import com.example.data.Task
import com.example.ui.theme.BrightEmerald
import com.example.ui.theme.CorporateBlue
import com.example.ui.theme.ElectricGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: ErpViewModel,
    user: Employee,
    onNavigateToSection: (String) -> Unit
) {
    if (user.role == "Super Admin") {
        AdminDashboard(viewModel, onNavigateToSection)
    } else {
        EmployeeDashboard(viewModel, user)
    }
}

// --- SUPER ADMIN DASHBOARD ---

@Composable
fun AdminDashboard(
    viewModel: ErpViewModel,
    onNavigateToSection: (String) -> Unit
) {
    val emps by viewModel.employees.collectAsState()
    val clnts by viewModel.clients.collectAsState()
    val leds by viewModel.leads.collectAsState()
    val tsks by viewModel.tasks.collectAsState()
    val pays by viewModel.payments.collectAsState()
    val activityLogs by viewModel.logs.collectAsState()

    // Calculating operational KPI statistics dynamically
    val todayDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val todayLeads = leds.filter { it.nextFollowUpDate == todayDateStr }.size
    val totalClients = clnts.size
    val pendingTasks = tsks.count { it.status == "Pending" }
    val completedTasks = tsks.count { it.status == "Completed" }

    val activeEmployees = emps.count { it.status == "Active" }

    // Today's followups (leads with today's follow-up date)
    val todayFollowups = leds.filter { it.nextFollowUpDate == todayDateStr }

    // Payments / Revenue
    val todayRev = pays.filter { it.date == todayDateStr && it.status == "Paid" }.sumOf { it.amount }
    val monthlyRev = pays.filter { it.status == "Paid" }.sumOf { it.amount } // Simplification

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Header Banner
        item {
            AdminWelcomeHeader(onNavigateToSection)
        }

        // Real-Time Statistics Grid Rows (Using customizable Row-Card components)
        item {
            Text(
                text = "Operational Metrics",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                KpiCard(
                    title = "Today's Leads",
                    value = todayLeads.toString(),
                    icon = Icons.Default.Leaderboard,
                    color = BrightEmerald,
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    title = "Total Clients",
                    value = totalClients.toString(),
                    icon = Icons.Default.Business,
                    color = CorporateBlue,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                KpiCard(
                    title = "Pending Work",
                    value = pendingTasks.toString(),
                    icon = Icons.Default.PendingActions,
                    color = Color(0xFFFFB74D),
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    title = "Completed Work",
                    value = completedTasks.toString(),
                    icon = Icons.Default.CheckCircle,
                    color = ElectricGreen,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                KpiCard(
                    title = "Today's Revenue",
                    value = "₹${todayRev.toInt()}",
                    icon = Icons.Default.LocalAtm,
                    color = BrightEmerald,
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    title = "Monthly Revenue",
                    value = "₹${monthlyRev.toInt()}",
                    icon = Icons.Default.MonetizationOn,
                    color = CorporateBlue,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                KpiCard(
                    title = "Active Staff",
                    value = activeEmployees.toString(),
                    icon = Icons.Default.Group,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    title = "Today's Follow-ups",
                    value = todayFollowups.size.toString(),
                    icon = Icons.Default.Schedule,
                    color = Color(0xFF64B5F6),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Charts Section (Utilizing elegant hand-drawn jetpack Canvas elements)
        item {
            Text(
                text = "Performance & Growth Analytics",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Monthly Revenue Trend (INR)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    RevenueGrowthChart()
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Lead Conversions Status",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    LeadConversionDonut()
                }
            }
        }

        // Rapid Broadcast Announcement Box
        item {
            BroadcastBox(viewModel)
        }

        // Recent System Activity Logs
        item {
            Text(
                text = "Recent Employee Activity Logs",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        items(activityLogs.take(5)) { log ->
            ActivityLogItem(log)
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun AdminWelcomeHeader(onNavigateToSection: (String) -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                Brush.linearGradient(listOf(ElectricGreen.copy(alpha = 0.5f), CorporateBlue.copy(alpha = 0.5f))),
                RoundedCornerShape(20.dp)
            )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Welcome, Super Admin 👋",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Manage enterprise staff accounts, monitor operational pipelines, track invoices, and communicate instantly with employees.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { onNavigateToSection("employees") },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricGreen),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add user icon", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Employee", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { onNavigateToSection("leads") },
                    colors = ButtonDefaults.buttonColors(containerColor = CorporateBlue),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Manage Leads", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun KpiCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier.border(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
        }
    }
}

// --- CUSTOM CANVAS CHARTS ---

@Composable
fun RevenueGrowthChart() {
    val primaryColor = BrightEmerald
    val gridColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
    ) {
        val width = size.width
        val height = size.height

        // Draw grid lines
        for (i in 1..4) {
            val y = height * (i.toFloat() / 5f)
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1f
            )
        }

        // Monthly trends data coordinates (Feb, Mar, Apr, May, Jun, Jul)
        val dataPoints = listOf(0.15f, 0.3f, 0.45f, 0.25f, 0.7f, 0.9f)
        val path = Path()

        for (i in dataPoints.indices) {
            val x = width * (i.toFloat() / (dataPoints.size - 1))
            val y = height - (height * dataPoints[i] * 0.8f)

            if (i == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }

            // Draw data point circles
            drawCircle(
                color = primaryColor,
                radius = 5.dp.toPx(),
                center = Offset(x, y)
            )
        }

        // Draw line connection
        drawPath(
            path = path,
            color = primaryColor,
            style = Stroke(width = 3.dp.toPx())
        )
    }
}

@Composable
fun LeadConversionDonut() {
    val interestedColor = BrightEmerald
    val contactedColor = CorporateBlue
    val newColor = Color(0xFFFFB74D)

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
    ) {
        val centerOffset = Offset(size.width / 2f, size.height / 2f)
        val radius = 50.dp.toPx()

        // Hand-render beautiful concentric colored rings representing stages
        drawArc(
            color = newColor,
            startAngle = 0f,
            sweepAngle = 120f,
            useCenter = false,
            topLeft = Offset(centerOffset.x - radius, centerOffset.y - radius),
            size = size.copy(width = radius * 2, height = radius * 2),
            style = Stroke(width = 14.dp.toPx())
        )

        drawArc(
            color = contactedColor,
            startAngle = 120f,
            sweepAngle = 140f,
            useCenter = false,
            topLeft = Offset(centerOffset.x - radius, centerOffset.y - radius),
            size = size.copy(width = radius * 2, height = radius * 2),
            style = Stroke(width = 14.dp.toPx())
        )

        drawArc(
            color = interestedColor,
            startAngle = 260f,
            sweepAngle = 100f,
            useCenter = false,
            topLeft = Offset(centerOffset.x - radius, centerOffset.y - radius),
            size = size.copy(width = radius * 2, height = radius * 2),
            style = Stroke(width = 14.dp.toPx())
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        LegendItem(name = "New (33%)", color = Color(0xFFFFB74D))
        Spacer(modifier = Modifier.width(16.dp))
        LegendItem(name = "Contacted (39%)", color = CorporateBlue)
        Spacer(modifier = Modifier.width(16.dp))
        LegendItem(name = "Interested (28%)", color = BrightEmerald)
    }
}

@Composable
fun LegendItem(name: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = name, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun BroadcastBox(viewModel: ErpViewModel) {
    var txt by remember { mutableStateOf("") }
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = ElectricGreen, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Broadcast Corporate Announcement", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = txt,
                onValueChange = { txt = it },
                placeholder = { Text("Write something to broadcast to all staff...", fontSize = 13.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricGreen,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    if (txt.isNotBlank()) {
                        viewModel.sendMessage("BROADCAST", txt)
                        txt = ""
                    }
                },
                enabled = txt.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = ElectricGreen),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Broadcast", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ActivityLogItem(log: ActivityLog) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.06f), RoundedCornerShape(10.dp))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = log.employeeName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text(
                    text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(log.timestamp)),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = "[${log.action}] ${log.details}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface, lineHeight = 16.sp)
        }
    }
}

// --- EMPLOYEE DASHBOARD ---

@Composable
fun EmployeeDashboard(viewModel: ErpViewModel, user: Employee) {
    val todayAttendance by viewModel.todayAttendance.collectAsState()
    val personalTasks by viewModel.tasks.collectAsState()
    val allNotifications by viewModel.notifications.collectAsState()

    val myTasks = personalTasks.filter { it.assignedEmployeeId == user.employeeId }
    val pendingMyTasksCount = myTasks.count { it.status == "Pending" }
    val completedMyTasksCount = myTasks.count { it.status == "Completed" }

    var leaveReason by remember { mutableStateOf("") }
    var showLeaveForm by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome and Date card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Hello, ${user.name} 👋",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${user.designation} • ${user.department} Department",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Verify your attendance log daily, keep tracking assigned client milestones, and submit your daily report before checking out.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Attendance check in/out punch card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.WatchLater, contentDescription = null, tint = BrightEmerald, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Real-Time Punch Clock", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        if (todayAttendance != null) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (todayAttendance?.checkOutTime != null) Color.Gray.copy(alpha = 0.2f)
                                        else BrightEmerald.copy(alpha = 0.15f)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (todayAttendance?.checkOutTime != null) "Checked Out" else "Active duty",
                                    fontSize = 10.sp,
                                    color = if (todayAttendance?.checkOutTime != null) MaterialTheme.colorScheme.onSurfaceVariant else BrightEmerald,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (todayAttendance == null) {
                        Text(
                            text = "You haven't checked in today yet.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        Button(
                            onClick = { viewModel.checkIn() },
                            colors = ButtonDefaults.buttonColors(containerColor = BrightEmerald, contentColor = Color.Black),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("PUNCH CHECK IN", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        val att = todayAttendance!!
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Row(modifier = Modifier.padding(bottom = 6.dp)) {
                                Text("Check-In Time: ", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(att.checkInTime ?: "--", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                if (att.isLate) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("(Late Entry)", fontSize = 10.sp, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                                }
                            }

                            Row(modifier = Modifier.padding(bottom = 12.dp)) {
                                Text("Check-Out Time: ", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(att.checkOutTime ?: "Currently Checked In", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }

                            if (att.checkOutTime == null) {
                                Button(
                                    onClick = { viewModel.checkOut() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935), contentColor = Color.White),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("PUNCH CHECK OUT", fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .padding(12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Shift Completed. Total working hours recorded: ${att.workingHours} hrs.",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Leave Request Panel
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Event, contentDescription = null, tint = CorporateBlue, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Request Time Off / Leave", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        TextButton(onClick = { showLeaveForm = !showLeaveForm }) {
                            Text(if (showLeaveForm) "Cancel" else "Apply", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    AnimatedVisibility(visible = showLeaveForm) {
                        Column {
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = leaveReason,
                                onValueChange = { leaveReason = it },
                                placeholder = { Text("Write brief reason for leave and date...", fontSize = 13.sp) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CorporateBlue,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    if (leaveReason.isNotBlank()) {
                                        viewModel.submitLeaveRequest(leaveReason)
                                        leaveReason = ""
                                        showLeaveForm = false
                                    }
                                },
                                enabled = leaveReason.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(containerColor = CorporateBlue, contentColor = Color.White),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Submit Request", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Personal Task KPI Overview
        item {
            Text(
                text = "My Task Metrics",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                KpiCard(
                    title = "Pending Tasks",
                    value = pendingMyTasksCount.toString(),
                    icon = Icons.Default.PendingActions,
                    color = Color(0xFFFFB74D),
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    title = "Completed Tasks",
                    value = completedMyTasksCount.toString(),
                    icon = Icons.Default.CheckCircle,
                    color = BrightEmerald,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Announcement & Board broadcast box
        item {
            Text(
                text = "Announcements Feed",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        val nonPersonalNotif = allNotifications.filter { it.recipientEmployeeId == "ALL" }
        if (nonPersonalNotif.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No announcements posted yet.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            items(nonPersonalNotif.take(3)) { announcement ->
                AnnouncementItem(announcement)
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun AnnouncementItem(announcement: Notification) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "📢 SYSTEM BROADCAST", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BrightEmerald, letterSpacing = 1.sp)
                Text(
                    text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(announcement.timestamp)),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = announcement.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = announcement.message, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 16.sp)
        }
    }
}
