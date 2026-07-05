package com.example.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.theme.BrightEmerald
import com.example.ui.theme.CorporateBlue
import com.example.ui.theme.ElectricGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ==========================================
// 1. LEAD MANAGEMENT SECTION
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeadsSection(viewModel: ErpViewModel, user: Employee) {
    val leds by viewModel.leads.collectAsState()
    val emps by viewModel.employees.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf("All") }
    var selectedPriorityFilter by remember { mutableStateOf("All") }

    var showCreateDialog by remember { mutableStateOf(false) }

    // Filter leads based on user role (Employees only see their assigned work)
    val userLeads = if (user.role == "Super Admin") leds else leds.filter { it.assignedEmployeeId == user.employeeId }

    val filteredLeads = userLeads.filter { lead ->
        val matchesSearch = lead.name.contains(searchQuery, ignoreCase = true) ||
                lead.interestedServices.contains(searchQuery, ignoreCase = true)
        val matchesStatus = selectedStatusFilter == "All" || lead.status == selectedStatusFilter
        val matchesPriority = selectedPriorityFilter == "All" || lead.priority == selectedPriorityFilter
        matchesSearch && matchesStatus && matchesPriority
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Search & Filter header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search Leads...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            )

            Button(
                onClick = { showCreateDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(52.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("New")
            }
        }

        // Dropdown Filter Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Status filter button
            var showStatusDrop by remember { mutableStateOf(false) }
            Box(modifier = Modifier.weight(1f)) {
                OutlinedCard(
                    onClick = { showStatusDrop = true },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Status: $selectedStatusFilter", fontSize = 11.sp)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(14.dp))
                    }
                }
                DropdownMenu(expanded = showStatusDrop, onDismissRequest = { showStatusDrop = false }) {
                    listOf("All", "New", "Contacted", "Interested", "Closed Won", "Closed Lost").forEach { status ->
                        DropdownMenuItem(
                            text = { Text(status) },
                            onClick = { selectedStatusFilter = status; showStatusDrop = false }
                        )
                    }
                }
            }

            // Priority filter button
            var showPriorityDrop by remember { mutableStateOf(false) }
            Box(modifier = Modifier.weight(1f)) {
                OutlinedCard(
                    onClick = { showPriorityDrop = true },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Priority: $selectedPriorityFilter", fontSize = 11.sp)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(14.dp))
                    }
                }
                DropdownMenu(expanded = showPriorityDrop, onDismissRequest = { showPriorityDrop = false }) {
                    listOf("All", "High", "Medium", "Low").forEach { p ->
                        DropdownMenuItem(
                            text = { Text(p) },
                            onClick = { selectedPriorityFilter = p; showPriorityDrop = false }
                        )
                    }
                }
            }
        }

        // List of leads
        if (filteredLeads.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No matching leads found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredLeads) { lead ->
                    LeadCard(lead, viewModel, user, emps)
                }
            }
        }
    }

    // CREATE LEAD DIALOG
    if (showCreateDialog) {
        var name by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var source by remember { mutableStateOf("Website") }
        var status by remember { mutableStateOf("New") }
        var priority by remember { mutableStateOf("Medium") }
        var services by remember { mutableStateOf("") }
        var notes by remember { mutableStateOf("") }
        var followUp by remember { mutableStateOf("2026-07-05") }
        var assignedId by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Create Operational Lead") },
            text = {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Client Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email ID") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Contact Number") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = services,
                            onValueChange = { services = it },
                            label = { Text("Interested Services") },
                            placeholder = { Text("ERP Web app, UI Kit...") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Client Notes / Requirements") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = followUp,
                            onValueChange = { followUp = it },
                            label = { Text("Next Follow Up Date") },
                            placeholder = { Text("YYYY-MM-DD") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        // Assign employee
                        var showAssignDrop by remember { mutableStateOf(false) }
                        val activeEmpName = emps.find { it.employeeId == assignedId }?.name ?: "Assign Employee"
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { showAssignDrop = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(activeEmpName)
                            }
                            DropdownMenu(expanded = showAssignDrop, onDismissRequest = { showAssignDrop = false }) {
                                emps.forEach { emp ->
                                    DropdownMenuItem(
                                        text = { Text("${emp.name} (${emp.department})") },
                                        onClick = { assignedId = emp.employeeId; showAssignDrop = false }
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            val selectedEmpName = emps.find { it.employeeId == assignedId }?.name
                            viewModel.createLead(
                                name = name,
                                email = email,
                                phone = phone,
                                source = source,
                                status = status,
                                priority = priority,
                                interestedServices = services,
                                clientNotes = notes,
                                nextFollowUpDate = followUp,
                                reminderTime = "10:00",
                                assignedEmpId = assignedId,
                                assignedEmpName = selectedEmpName
                            )
                            showCreateDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Save Lead")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun LeadCard(lead: Lead, viewModel: ErpViewModel, user: Employee, employees: List<Employee>) {
    var expanded by remember { mutableStateOf(false) }

    val badgeColor = when (lead.priority) {
        "High" -> Color(0xFFE53935)
        "Medium" -> Color(0xFFFFB74D)
        else -> BrightEmerald
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (expanded) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f),
                RoundedCornerShape(14.dp)
            )
            .clickable { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = lead.name, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Services: ${lead.interestedServices}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(badgeColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(text = lead.status, fontSize = 10.sp, color = badgeColor, fontWeight = FontWeight.Bold)
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f), modifier = Modifier.padding(bottom = 12.dp))

                    Text(text = "Email: ${lead.email}", fontSize = 12.sp)
                    Text(text = "Phone: ${lead.phone}", fontSize = 12.sp)
                    Text(text = "Source: ${lead.source}", fontSize = 12.sp)
                    Text(text = "Priority Level: ${lead.priority}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = badgeColor)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Client Notes:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(text = lead.clientNotes, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Next Follow-Up: ${lead.nextFollowUpDate} at ${lead.reminderTime}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text(text = "Assigned To: ${lead.assignedEmployeeName ?: "Unassigned"}", fontSize = 12.sp)

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Quick update status button
                        Button(
                            onClick = {
                                val nextStatus = when (lead.status) {
                                    "New" -> "Contacted"
                                    "Contacted" -> "Interested"
                                    "Interested" -> "Closed Won"
                                    else -> "New"
                                }
                                viewModel.updateLead(lead.copy(status = nextStatus))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text("Next Status", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }

                        if (user.role == "Super Admin") {
                            Spacer(modifier = Modifier.width(10.dp))
                            IconButton(onClick = { viewModel.deleteLead(lead) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Lead", tint = Color.Red)
                            }
                        }
                    }
                }
            }
        }
    }
}


// ==========================================
// 2. CLIENT MANAGEMENT SECTION
// ==========================================

@Composable
fun ClientsSection(viewModel: ErpViewModel, user: Employee) {
    val clnts by viewModel.clients.collectAsState()
    var showAddClientDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredClients = clnts.filter {
        it.companyName.contains(searchQuery, ignoreCase = true) ||
                it.ownerName.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search Corporate Clients...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            )

            if (user.role == "Super Admin") {
                Button(
                    onClick = { showAddClientDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(52.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add")
                }
            }
        }

        if (filteredClients.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No corporate clients registered.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredClients) { client ->
                    ClientCard(client, viewModel, user)
                }
            }
        }
    }

    // ADD CLIENT DIALOG (Admin Only)
    if (showAddClientDialog) {
        var cid by remember { mutableStateOf("CLNT${(300..999).random()}") }
        var cname by remember { mutableStateOf("") }
        var owner by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var address by remember { mutableStateOf("") }
        var gst by remember { mutableStateOf("") }
        var service by remember { mutableStateOf("ERP Customization") }
        var status by remember { mutableStateOf("Paid") }
        var invoice by remember { mutableStateOf("INV-2026-${(100..999).random()}") }

        AlertDialog(
            onDismissRequest = { showAddClientDialog = false },
            title = { Text("Register New Corporate Client") },
            text = {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        OutlinedTextField(
                            value = cname,
                            onValueChange = { cname = it },
                            label = { Text("Company Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = owner,
                            onValueChange = { owner = it },
                            label = { Text("Owner / Representative Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Contact Number") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Billing Email") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text("Registered Address") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = gst,
                            onValueChange = { gst = it },
                            label = { Text("GST Number") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        // Service selection
                        var serviceDrop by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(onClick = { serviceDrop = true }, modifier = Modifier.fillMaxWidth()) {
                                Text(service)
                            }
                            DropdownMenu(expanded = serviceDrop, onDismissRequest = { serviceDrop = false }) {
                                listOf("ERP Customization", "Cloud Hosting Integration", "CRM Solutions").forEach { s ->
                                    DropdownMenuItem(text = { Text(s) }, onClick = { service = s; serviceDrop = false })
                                }
                            }
                        }
                    }
                    item {
                        // Payment status selection
                        var payDrop by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(onClick = { payDrop = true }, modifier = Modifier.fillMaxWidth()) {
                                Text("Payment: $status")
                            }
                            DropdownMenu(expanded = payDrop, onDismissRequest = { payDrop = false }) {
                                listOf("Paid", "Pending", "Overdue").forEach { s ->
                                    DropdownMenuItem(text = { Text(s) }, onClick = { status = s; payDrop = false })
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (cname.isNotBlank()) {
                            viewModel.createClient(
                                clientId = cid,
                                companyName = cname,
                                ownerName = owner,
                                contactNumber = phone,
                                email = email,
                                address = address,
                                gstNumber = gst,
                                servicePurchased = service,
                                paymentStatus = status,
                                invoiceNumber = invoice,
                                remarks = "Successfully onboarded."
                            )
                            showAddClientDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Register Client")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddClientDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ClientCard(client: Client, viewModel: ErpViewModel, user: Employee) {
    var expanded by remember { mutableStateOf(false) }
    var mockDocUploadDialog by remember { mutableStateOf(false) }

    val statusColor = when (client.paymentStatus) {
        "Paid" -> BrightEmerald
        "Pending" -> Color(0xFFFFB74D)
        else -> Color(0xFFE53935)
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (expanded) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f),
                RoundedCornerShape(14.dp)
            )
            .clickable { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = client.companyName, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Owner: ${client.ownerName}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(statusColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(text = client.paymentStatus, fontSize = 10.sp, color = statusColor, fontWeight = FontWeight.Bold)
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f), modifier = Modifier.padding(bottom = 12.dp))

                    Text(text = "Client ID: ${client.clientId}", fontSize = 12.sp)
                    Text(text = "Billing Email: ${client.email}", fontSize = 12.sp)
                    Text(text = "Phone: ${client.contactNumber}", fontSize = 12.sp)
                    Text(text = "Address: ${client.address}", fontSize = 12.sp)
                    Text(text = "GSTIN: ${client.gstNumber}", fontSize = 12.sp)
                    Text(text = "Product / Service Purchased: ${client.servicePurchased}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text(text = "Invoice Number: ${client.invoiceNumber}", fontSize = 12.sp)
                    Text(text = "Operational Remarks: ${client.remarks}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = { mockDocUploadDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Simulate Doc Upload", fontSize = 10.sp)
                        }

                        if (user.role == "Super Admin") {
                            Spacer(modifier = Modifier.width(10.dp))
                            IconButton(onClick = { viewModel.deleteClient(client) }) {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                            }
                        }
                    }
                }
            }
        }
    }

    if (mockDocUploadDialog) {
        var docTitle by remember { mutableStateOf("") }
        var docFilename by remember { mutableStateOf("ClientContract.pdf") }
        var docType by remember { mutableStateOf("Agreement") }

        AlertDialog(
            onDismissRequest = { mockDocUploadDialog = false },
            title = { Text("Simulate Uploading Document") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = docTitle,
                        onValueChange = { docTitle = it },
                        label = { Text("Document Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = docFilename,
                        onValueChange = { docFilename = it },
                        label = { Text("Simulated File Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    // Type selector dropdown
                    var docTypeDrop by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(onClick = { docTypeDrop = true }, modifier = Modifier.fillMaxWidth()) {
                            Text("Category: $docType")
                        }
                        DropdownMenu(expanded = docTypeDrop, onDismissRequest = { docTypeDrop = false }) {
                            listOf("Agreement", "Invoice", "Client Spec", "Image", "Excel").forEach { t ->
                                DropdownMenuItem(text = { Text(t) }, onClick = { docType = t; docTypeDrop = false })
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (docTitle.isNotBlank()) {
                            viewModel.uploadDocumentMock(
                                title = docTitle,
                                type = docType,
                                filename = docFilename,
                                size = "${(1..5).random()}.${(0..9).random()} MB",
                                ownerId = client.clientId
                            )
                            mockDocUploadDialog = false
                        }
                    }
                ) {
                    Text("Upload")
                }
            },
            dismissButton = {
                TextButton(onClick = { mockDocUploadDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}


// ==========================================
// 3. TASK MANAGEMENT SECTION
// ==========================================

@Composable
fun TasksSection(viewModel: ErpViewModel, user: Employee) {
    val tsks by viewModel.tasks.collectAsState()
    val emps by viewModel.employees.collectAsState()

    var showCreateTaskDialog by remember { mutableStateOf(false) }

    val myTasks = if (user.role == "Super Admin") tsks else tsks.filter { it.assignedEmployeeId == user.employeeId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (user.role == "Super Admin") "All Enterprise Tasks" else "My Assigned Tasks",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            if (user.role == "Super Admin") {
                Button(
                    onClick = { showCreateTaskDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Task", fontSize = 12.sp)
                }
            }
        }

        if (myTasks.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No tasks active.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(myTasks) { task ->
                    TaskCard(task, viewModel, user)
                }
            }
        }
    }

    // CREATE TASK DIALOG
    if (showCreateTaskDialog) {
        var title by remember { mutableStateOf("") }
        var desc by remember { mutableStateOf("") }
        var assignedId by remember { mutableStateOf("") }
        var priority by remember { mutableStateOf("Medium") }
        var deadline by remember { mutableStateOf("2026-07-10") }

        AlertDialog(
            onDismissRequest = { showCreateTaskDialog = false },
            title = { Text("Create Operational Task") },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    item {
                        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Task Title") }, modifier = Modifier.fillMaxWidth())
                    }
                    item {
                        OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Detailed Description") }, modifier = Modifier.fillMaxWidth())
                    }
                    item {
                        // Assign employee
                        var showAssignDrop by remember { mutableStateOf(false) }
                        val activeEmpName = emps.find { it.employeeId == assignedId }?.name ?: "Assign Staff"
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(onClick = { showAssignDrop = true }, modifier = Modifier.fillMaxWidth()) {
                                Text(activeEmpName)
                            }
                            DropdownMenu(expanded = showAssignDrop, onDismissRequest = { showAssignDrop = false }) {
                                emps.forEach { emp ->
                                    DropdownMenuItem(
                                        text = { Text("${emp.name} (${emp.designation})") },
                                        onClick = { assignedId = emp.employeeId; showAssignDrop = false }
                                    )
                                }
                            }
                        }
                    }
                    item {
                        // Priority Selection
                        var showPrioDrop by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(onClick = { showPrioDrop = true }, modifier = Modifier.fillMaxWidth()) {
                                Text("Priority: $priority")
                            }
                            DropdownMenu(expanded = showPrioDrop, onDismissRequest = { showPrioDrop = false }) {
                                listOf("High", "Medium", "Low").forEach { p ->
                                    DropdownMenuItem(text = { Text(p) }, onClick = { priority = p; showPrioDrop = false })
                                }
                            }
                        }
                    }
                    item {
                        OutlinedTextField(value = deadline, onValueChange = { deadline = it }, label = { Text("Deadline") }, placeholder = { Text("YYYY-MM-DD") }, modifier = Modifier.fillMaxWidth())
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val empName = emps.find { it.employeeId == assignedId }?.name
                        if (title.isNotBlank() && assignedId.isNotBlank() && empName != null) {
                            viewModel.createTask(
                                title = title,
                                description = desc,
                                assignedEmpId = assignedId,
                                assignedEmpName = empName,
                                priority = priority,
                                deadline = deadline
                            )
                            showCreateTaskDialog = false
                        }
                    }
                ) {
                    Text("Save Task")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateTaskDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun TaskCard(task: Task, viewModel: ErpViewModel, user: Employee) {
    var expanded by remember { mutableStateOf(false) }
    var showProgressSheet by remember { mutableStateOf(false) }

    val statusColor = when (task.status) {
        "Completed" -> BrightEmerald
        "Cancelled" -> Color.Red
        else -> Color(0xFFFFB74D)
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (expanded) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f),
                RoundedCornerShape(14.dp)
            )
            .clickable { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = task.title, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Assigned To: ${task.assignedEmployeeName}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(statusColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(text = task.status, fontSize = 10.sp, color = statusColor, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Simple loading bar showing completion progress
            LinearProgressIndicator(
                progress = task.progressPercent / 100f,
                color = statusColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "${task.progressPercent}% Completed", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = "Due: ${task.deadline}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f), modifier = Modifier.padding(bottom = 12.dp))

                    Text(text = "Description:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(text = task.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    if (task.comments.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Activity Timeline:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(text = task.comments, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { showProgressSheet = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text("Update Milestone", fontSize = 11.sp)
                        }

                        if (user.role == "Super Admin") {
                            Spacer(modifier = Modifier.width(10.dp))
                            IconButton(onClick = { viewModel.deleteTask(task) }) {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showProgressSheet) {
        var prg by remember { mutableStateOf(task.progressPercent) }
        var status by remember { mutableStateOf(task.status) }
        var comment by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showProgressSheet = false },
            title = { Text("Update Task Milestones") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Text("Completion Level: $prg%", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Slider(
                        value = prg.toFloat(),
                        onValueChange = {
                            prg = it.toInt()
                            status = if (prg >= 100) "Completed" else "Pending"
                        },
                        valueRange = 0f..100f
                    )

                    var statusDrop by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(onClick = { statusDrop = true }, modifier = Modifier.fillMaxWidth()) {
                            Text("Status: $status")
                        }
                        DropdownMenu(expanded = statusDrop, onDismissRequest = { statusDrop = false }) {
                            listOf("Pending", "Completed", "Cancelled").forEach { s ->
                                DropdownMenuItem(text = { Text(s) }, onClick = { status = s; statusDrop = false })
                            }
                        }
                    }

                    OutlinedTextField(
                        value = comment,
                        onValueChange = { comment = it },
                        label = { Text("Progress Update Note") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateTaskProgress(task.taskId, prg, comment, status)
                        showProgressSheet = false
                    }
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showProgressSheet = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}


// ==========================================
// 4. ATTENDANCE & SHIFTS MODULE
// ==========================================

@Composable
fun AttendanceSection(viewModel: ErpViewModel, user: Employee) {
    val allAttendance by viewModel.attendance.collectAsState()
    val emps by viewModel.employees.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                Text("Punch Logs", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                Text("Time Off Requests", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }) {
                Text("Holidays 2026", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        when (selectedTab) {
            0 -> {
                // Punch logs list
                val list = if (user.role == "Super Admin") allAttendance.filter { it.leaveRequestStatus == null }
                else allAttendance.filter { it.employeeId == user.employeeId && it.leaveRequestStatus == null }

                if (list.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No punch logs found for today.")
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(list) { log ->
                            val staffName = emps.find { it.employeeId == log.employeeId }?.name ?: "Unknown Staff"
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(staffName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        Text(log.date, fontSize = 11.sp)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row {
                                        Text("Punch In: ", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(log.checkInTime ?: "--", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Text("Punch Out: ", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(log.checkOutTime ?: "Active", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                    if (log.isLate) {
                                        Text("⚠️ Late Entry Registered", fontSize = 10.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            1 -> {
                // Leave timeoff requests
                val list = if (user.role == "Super Admin") allAttendance.filter { it.leaveRequestStatus != null }
                else allAttendance.filter { it.employeeId == user.employeeId && it.leaveRequestStatus != null }

                if (list.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No active leave requests.")
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(list) { request ->
                            val staffName = emps.find { it.employeeId == request.employeeId }?.name ?: "Unknown Staff"
                            val statColor = when (request.leaveRequestStatus) {
                                "Approved" -> BrightEmerald
                                "Rejected" -> Color.Red
                                else -> Color(0xFFFFB74D)
                            }
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(staffName, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(statColor.copy(alpha = 0.15f)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                            Text(request.leaveRequestStatus ?: "Pending", fontSize = 9.sp, color = statColor, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Requested Date: ${request.date}", fontSize = 11.sp)
                                    Text("Reason: ${request.leaveReason}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                                    if (user.role == "Super Admin" && request.leaveRequestStatus == "Pending") {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Button(
                                                onClick = { viewModel.approveLeaveRequest(request.attendanceId, true) },
                                                colors = ButtonDefaults.buttonColors(containerColor = BrightEmerald, contentColor = Color.Black),
                                                modifier = Modifier.height(30.dp),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                                            ) {
                                                Text("Approve", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Button(
                                                onClick = { viewModel.approveLeaveRequest(request.attendanceId, false) },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red, contentColor = Color.White),
                                                modifier = Modifier.height(30.dp),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                                            ) {
                                                Text("Reject", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            2 -> {
                // Static corporate holidays 2026
                val holidays = listOf(
                    "Jan 26 - Republic Day",
                    "Mar 13 - Holi Festival",
                    "Apr 10 - Good Friday",
                    "May 01 - International Labour Day",
                    "Aug 15 - Independence Day",
                    "Oct 02 - Gandhi Jayanti",
                    "Oct 21 - Dussehra",
                    "Nov 08 - Diwali Festival of Lights",
                    "Dec 25 - Christmas Holiday"
                )
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(holidays) { hol ->
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.EventNote, contentDescription = null, tint = BrightEmerald, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(hol, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        }
    }
}


// ==========================================
// 5. DAILY WORK REPORT SUBMISSION
// ==========================================

@Composable
fun DailyReportsSection(viewModel: ErpViewModel, user: Employee) {
    val reports by viewModel.reports.collectAsState()

    var showForm by remember { mutableStateOf(false) }

    var todayWork by remember { mutableStateOf("") }
    var calls by remember { mutableStateOf("0") }
    var meetings by remember { mutableStateOf("0") }
    var leadsGen by remember { mutableStateOf("0") }
    var clConverted by remember { mutableStateOf("0") }
    var blockers by remember { mutableStateOf("") }
    var tomorrowPlan by remember { mutableStateOf("") }

    val userReports = if (user.role == "Super Admin") reports else reports.filter { it.employeeId == user.employeeId }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Daily Work Reports Log", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                if (user.role == "Employee") {
                    Button(onClick = { showForm = !showForm }, shape = RoundedCornerShape(10.dp)) {
                        Text(if (showForm) "Cancel" else "Submit Report", fontSize = 11.sp)
                    }
                }
            }
        }

        if (showForm) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Draft Today's Work Report", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                        OutlinedTextField(value = todayWork, onValueChange = { todayWork = it }, label = { Text("What did you work on today?") }, modifier = Modifier.fillMaxWidth())

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = calls, onValueChange = { calls = it }, label = { Text("Calls Done") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = meetings, onValueChange = { meetings = it }, label = { Text("Meetings Done") }, modifier = Modifier.weight(1f))
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = leadsGen, onValueChange = { leadsGen = it }, label = { Text("Leads Generated") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = clConverted, onValueChange = { clConverted = it }, label = { Text("Clients Closed") }, modifier = Modifier.weight(1f))
                        }

                        OutlinedTextField(value = blockers, onValueChange = { blockers = it }, label = { Text("Blockers / Problems Faced") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = tomorrowPlan, onValueChange = { tomorrowPlan = it }, label = { Text("Tomorrow's Plan") }, modifier = Modifier.fillMaxWidth())

                        Button(
                            onClick = {
                                if (todayWork.isNotBlank()) {
                                    viewModel.submitDailyWorkReport(
                                        todayWork = todayWork,
                                        callsDone = calls.toIntOrNull() ?: 0,
                                        meetingsDone = meetings.toIntOrNull() ?: 0,
                                        leadsGenerated = leadsGen.toIntOrNull() ?: 0,
                                        clientsConverted = clConverted.toIntOrNull() ?: 0,
                                        problemsFaced = blockers,
                                        tomorrowPlan = tomorrowPlan
                                    )
                                    showForm = false
                                    todayWork = ""
                                    blockers = ""
                                    tomorrowPlan = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrightEmerald, contentColor = Color.Black),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("SUBMIT ERP WORK REPORT", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        if (userReports.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Text("No submitted reports found.")
                }
            }
        } else {
            items(userReports) { rpt ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(rpt.employeeName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text("${rpt.date} @ ${rpt.timeSubmitted}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Today's Work accomplishments:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(rpt.todayWork, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)

                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text("📞 Calls: ${rpt.callsDone}", fontSize = 11.sp)
                            Text("🤝 Mtgs: ${rpt.meetingsDone}", fontSize = 11.sp)
                            Text("⚡ Leads: ${rpt.leadsGenerated}", fontSize = 11.sp)
                            Text("💼 Sales: ${rpt.clientsConverted}", fontSize = 11.sp)
                        }

                        if (rpt.problemsFaced.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("🛑 Challenges faced: ${rpt.problemsFaced}", fontSize = 11.sp, color = Color.Red)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Tomorrow's Agenda: ${rpt.tomorrowPlan}", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}


// ==========================================
// 6. INTERNAL CHAT & COLLABORATION
// ==========================================

@Composable
fun ChatSection(viewModel: ErpViewModel, user: Employee) {
    val allMsg by viewModel.messages.collectAsState()
    val emps by viewModel.employees.collectAsState()

    var activeChatPartnerId by remember { mutableStateOf("BROADCAST") }
    var messageText by remember { mutableStateOf("") }

    val chatPartners = listOf(Employee("BROADCAST", "📢 Global Broadcast Announcement", "", "", "Management", "CEO", "", "Active", "", "Super Admin")) +
            emps.filter { it.employeeId != user.employeeId }

    val visibleMessages = allMsg.filter { msg ->
        if (activeChatPartnerId == "BROADCAST") {
            msg.receiverId == "BROADCAST"
        } else {
            (msg.senderId == user.employeeId && msg.receiverId == activeChatPartnerId) ||
                    (msg.senderId == activeChatPartnerId && msg.receiverId == user.employeeId)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Chat partner picker row
        Text("Conversations Room", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(chatPartners) { partner ->
                val isSelected = partner.employeeId == activeChatPartnerId
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                        .clickable { activeChatPartnerId = partner.employeeId }
                        .border(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        partner.name,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f))

        // Chat message list
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                .border(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                .padding(12.dp)
        ) {
            if (visibleMessages.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No messages here. Say hello!", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                    items(visibleMessages) { m ->
                        val isMyMessage = m.senderId == user.employeeId
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isMyMessage) Arrangement.End else Arrangement.Start
                        ) {
                            Card(
                                shape = RoundedCornerShape(
                                    topStart = 12.dp,
                                    topEnd = 12.dp,
                                    bottomStart = if (isMyMessage) 12.dp else 0.dp,
                                    bottomEnd = if (isMyMessage) 0.dp else 12.dp
                                ),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isMyMessage) BrightEmerald else MaterialTheme.colorScheme.surface
                                ),
                                modifier = Modifier.widthIn(max = 260.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    if (!isMyMessage) {
                                        Text(m.senderName, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }
                                    Text(m.content, fontSize = 13.sp, color = if (isMyMessage) Color.Black else MaterialTheme.colorScheme.onSurface)

                                    if (m.attachmentType != null) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color.Gray.copy(alpha = 0.15f))
                                                .padding(6.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(12.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("[${m.attachmentType}] ${m.attachmentName}", fontSize = 10.sp, maxLines = 1)
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

        // Action inputs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Document attachment shortcut
            var showAttachmentMenu by remember { mutableStateOf(false) }
            IconButton(onClick = { showAttachmentMenu = true }) {
                Icon(Icons.Default.AttachFile, contentDescription = "Simulate File Attach")
            }

            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                placeholder = { Text("Write message...", fontSize = 13.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            )

            Button(
                onClick = {
                    if (messageText.isNotBlank()) {
                        viewModel.sendMessage(activeChatPartnerId, messageText)
                        messageText = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrightEmerald, contentColor = Color.Black),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(52.dp)
            ) {
                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
            }

            // Simulated Attachment Dialogue Box
            if (showAttachmentMenu) {
                AlertDialog(
                    onDismissRequest = { showAttachmentMenu = false },
                    title = { Text("Simulate Sending Attachment") },
                    text = { Text("Attach a mocked corporate document draft immediately inside this private room thread.") },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.sendMessage(
                                    recipientId = activeChatPartnerId,
                                    content = "📎 Sent document reference.",
                                    attachType = "Agreement",
                                    attachName = "TansionGO_PartnerDeal.pdf"
                                )
                                showAttachmentMenu = false
                            }
                        ) {
                            Text("Attach PartnerDeal.pdf")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAttachmentMenu = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}


// ==========================================
// 7. DOCUMENTS SECTION
// ==========================================

@Composable
fun DocumentsSection(viewModel: ErpViewModel, user: Employee) {
    val docs by viewModel.documents.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Document Repository Center", fontSize = 18.sp, fontWeight = FontWeight.Bold)

        if (docs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Repository is empty.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(docs) { doc ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                val ic = when (doc.type) {
                                    "Agreement" -> Icons.Default.Gavel
                                    "Invoice" -> Icons.Default.ReceiptLong
                                    else -> Icons.Default.Description
                                }
                                Box(
                                    modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(imageVector = ic, contentDescription = null, tint = BrightEmerald)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(text = doc.title, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text(text = "${doc.fileName} (${doc.fileSize})", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(text = "Uploaded By: ${doc.uploadedBy} • ${doc.uploadDate}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            Row {
                                IconButton(onClick = {
                                    Toast.makeText(context, "Simulating download of ${doc.fileName}... Safe!", Toast.LENGTH_SHORT).show()
                                }) {
                                    Icon(Icons.Default.Download, contentDescription = "Download")
                                }
                                if (user.role == "Super Admin") {
                                    IconButton(onClick = { viewModel.deleteDocument(doc) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
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


// ==========================================
// 8. REPORTS & ANALYTICS SECTION
// ==========================================

@Composable
fun ReportsSection(viewModel: ErpViewModel, user: Employee) {
    val context = LocalContext.current

    if (user.role != "Super Admin") {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Access Restricted to Super Admin.")
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Enterprise PDF/Excel Reports", fontSize = 18.sp, fontWeight = FontWeight.Bold)

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Sales & Conversion Reports", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text("Export daily pipelines, lead conversion rates, invoice details, and client details immediately.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            Toast.makeText(context, "Exporting Lead_Conversion_Report_2026.pdf ...", Toast.LENGTH_LONG).show()
                            viewModel.uploadDocumentMock("Lead Conversion Report 2026", "PDF", "Lead_Conversion_Report_2026.pdf", "3.1 MB", null)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CorporateBlue),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Export PDF", fontSize = 11.sp)
                    }

                    Button(
                        onClick = {
                            Toast.makeText(context, "Exporting Sales_Ledger_Q2_2026.xlsx ...", Toast.LENGTH_LONG).show()
                            viewModel.uploadDocumentMock("Sales Ledger Q2 2026", "Excel", "Sales_Ledger_Q2_2026.xlsx", "1.2 MB", null)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrightEmerald, contentColor = Color.Black),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Export Excel", fontSize = 11.sp)
                    }
                }
            }
        }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Staff Performance Audit", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text("Export comprehensive working hours audits, late entries logs, and task completion speed tables.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Button(
                    onClick = {
                        Toast.makeText(context, "Generating Staff_Attendance_Audit_Jul2026.xlsx ...", Toast.LENGTH_LONG).show()
                        viewModel.uploadDocumentMock("Staff Attendance Audit July 2026", "Excel", "Staff_Attendance_Audit_Jul2026.xlsx", "4.5 MB", null)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Article, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Export Attendance & Tasks Spreadsheet")
                }
            }
        }
    }
}


// ==========================================
// 9. GLOBAL SEARCH BAR
// ==========================================

@Composable
fun SearchSection(viewModel: ErpViewModel, user: Employee) {
    val emps by viewModel.employees.collectAsState()
    val clnts by viewModel.clients.collectAsState()
    val leds by viewModel.leads.collectAsState()
    val tsks by viewModel.tasks.collectAsState()

    var query by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Search ERP across Clients, Tasks, Leads...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        if (query.isBlank()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Type something above to explore ERP indexing...")
            }
        } else {
            val sLeds = leds.filter { it.name.contains(query, ignoreCase = true) || it.interestedServices.contains(query, ignoreCase = true) }
            val sCls = clnts.filter { it.companyName.contains(query, ignoreCase = true) || it.ownerName.contains(query, ignoreCase = true) }
            val sTsks = tsks.filter { it.title.contains(query, ignoreCase = true) || it.assignedEmployeeName.contains(query, ignoreCase = true) }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (sLeds.isNotEmpty()) {
                    item { Text("Leads Matches (${sLeds.size})", fontWeight = FontWeight.Bold, color = BrightEmerald) }
                    items(sLeds) { LeadCard(it, viewModel, user, emps) }
                }

                if (sCls.isNotEmpty()) {
                    item { Text("Clients Matches (${sCls.size})", fontWeight = FontWeight.Bold, color = CorporateBlue) }
                    items(sCls) { ClientCard(it, viewModel, user) }
                }

                if (sTsks.isNotEmpty()) {
                    item { Text("Tasks Matches (${sTsks.size})", fontWeight = FontWeight.Bold, color = Color(0xFFFFB74D)) }
                    items(sTsks) { TaskCard(it, viewModel, user) }
                }

                if (sLeds.isEmpty() && sCls.isEmpty() && sTsks.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                            Text("No records found matching query.")
                        }
                    }
                }
            }
        }
    }
}


// ==========================================
// 10. ENTERPRISE SETTINGS SECTION
// ==========================================

@Composable
fun SettingsSection(viewModel: ErpViewModel, user: Employee) {
    val context = LocalContext.current

    if (user.role != "Super Admin") {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Access Restricted to Super Admin.")
        }
        return
    }

    var companyName by remember { mutableStateOf("TansionGO Private Limited") }
    var businessInfo by remember { mutableStateOf("B2B Software and Enterprise IT Solutions Provider") }
    var hrs by remember { mutableStateOf("09:30 to 18:30") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Company Configuration", fontSize = 18.sp, fontWeight = FontWeight.Bold)

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Business Profile", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                OutlinedTextField(value = companyName, onValueChange = { companyName = it }, label = { Text("Enterprise Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = businessInfo, onValueChange = { businessInfo = it }, label = { Text("Profile description") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = hrs, onValueChange = { hrs = it }, label = { Text("Standard Working Hours") }, modifier = Modifier.fillMaxWidth())

                Button(
                    onClick = {
                        viewModel.updateSetting("company_name", companyName)
                        viewModel.updateSetting("business_info", businessInfo)
                        viewModel.updateSetting("working_hours", hrs)
                        Toast.makeText(context, "Company profile saved!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrightEmerald, contentColor = Color.Black),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Configuration")
                }
            }
        }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Database Management & Backups", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text("Schedule security snapshot storage blocks to safeguard all user databases offline.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            viewModel.backupDatabaseMock()
                            Toast.makeText(context, "SQLite ERP Backup completed successfully!", Toast.LENGTH_LONG).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CorporateBlue),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Create Backup", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            viewModel.restoreDatabaseMock()
                            Toast.makeText(context, "System configuration restored successfully!", Toast.LENGTH_LONG).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f), contentColor = MaterialTheme.colorScheme.onSurface),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Restore DB", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}


// ==========================================
// 11. EMPLOYEES STAFF DIRECTORY (Admin Only)
// ==========================================

@Composable
fun EmployeesSection(viewModel: ErpViewModel, user: Employee) {
    val emps by viewModel.employees.collectAsState()
    var showAddStaff by remember { mutableStateOf(false) }

    if (user.role != "Super Admin") {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Access Restricted to Super Admin.")
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Staff Employee Directory", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Button(onClick = { showAddStaff = true }, shape = RoundedCornerShape(10.dp)) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("New Staff", fontSize = 12.sp)
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(emps) { emp ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text(emp.name, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Text("${emp.designation} • ${emp.department}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(if (emp.status == "Active") BrightEmerald.copy(alpha = 0.15f) else Color.Red.copy(alpha = 0.15f)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                                Text(emp.status, fontSize = 10.sp, color = if (emp.status == "Active") BrightEmerald else Color.Red, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Staff ID: ${emp.employeeId} | Mob: ${emp.mobileNumber}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Email ID: ${emp.email} | Joined: ${emp.dateOfJoining}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Suspend/Activate button
                            Button(
                                onClick = {
                                    val nextStatus = if (emp.status == "Active") "Inactive" else "Active"
                                    viewModel.updateEmployeeStatus(emp, nextStatus)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(32.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                            ) {
                                Text(if (emp.status == "Active") "Suspend" else "Activate", fontSize = 10.sp)
                            }

                            // Reset Password button
                            var showResetAlert by remember { mutableStateOf(false) }
                            Button(
                                onClick = { showResetAlert = true },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurface),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(32.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                            ) {
                                Text("Reset Pass", fontSize = 10.sp)
                            }

                            if (emp.employeeId != "EMP101") {
                                Spacer(modifier = Modifier.weight(1f))
                                IconButton(onClick = { viewModel.deleteEmployee(emp) }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red, modifier = Modifier.size(18.dp))
                                }
                            }

                            if (showResetAlert) {
                                var newPass by remember { mutableStateOf("newpassword") }
                                AlertDialog(
                                    onDismissRequest = { showResetAlert = false },
                                    title = { Text("Reset Employee Password") },
                                    text = {
                                        OutlinedTextField(
                                            value = newPass,
                                            onValueChange = { newPass = it },
                                            label = { Text("New Login Password") },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    },
                                    confirmButton = {
                                        Button(onClick = {
                                            viewModel.resetEmployeePassword(emp, newPass)
                                            showResetAlert = false
                                        }) {
                                            Text("Reset")
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { showResetAlert = false }) {
                                            Text("Cancel")
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // ADD STAFF DIALOG
    if (showAddStaff) {
        var eid by remember { mutableStateOf("EMP${(105..999).random()}") }
        var sname by remember { mutableStateOf("") }
        var smobile by remember { mutableStateOf("") }
        var semail by remember { mutableStateOf("") }
        var sdept by remember { mutableStateOf("Sales") }
        var sdesg by remember { mutableStateOf("Sales Manager") }
        var spass by remember { mutableStateOf("password") }

        AlertDialog(
            onDismissRequest = { showAddStaff = false },
            title = { Text("Onboard New Enterprise Staff") },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    item { OutlinedTextField(value = eid, onValueChange = { eid = it }, label = { Text("Staff Employee ID") }, modifier = Modifier.fillMaxWidth()) }
                    item { OutlinedTextField(value = sname, onValueChange = { sname = it }, label = { Text("Employee Name") }, modifier = Modifier.fillMaxWidth()) }
                    item { OutlinedTextField(value = smobile, onValueChange = { smobile = it }, label = { Text("Mobile Number") }, modifier = Modifier.fillMaxWidth()) }
                    item { OutlinedTextField(value = semail, onValueChange = { semail = it }, label = { Text("Company Email") }, modifier = Modifier.fillMaxWidth()) }
                    item {
                        var deptDrop by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(onClick = { deptDrop = true }, modifier = Modifier.fillMaxWidth()) { Text("Dept: $sdept") }
                            DropdownMenu(expanded = deptDrop, onDismissRequest = { deptDrop = false }) {
                                listOf("Sales", "Development", "Design", "HR").forEach { d ->
                                    DropdownMenuItem(text = { Text(d) }, onClick = { sdept = d; deptDrop = false })
                                }
                            }
                        }
                    }
                    item {
                        var desgDrop by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(onClick = { desgDrop = true }, modifier = Modifier.fillMaxWidth()) { Text("Desg: $sdesg") }
                            DropdownMenu(expanded = desgDrop, onDismissRequest = { desgDrop = false }) {
                                listOf("Sales Manager", "Senior Engineer", "UI/UX Designer", "HR Manager").forEach { d ->
                                    DropdownMenuItem(text = { Text(d) }, onClick = { sdesg = d; desgDrop = false })
                                }
                            }
                        }
                    }
                    item { OutlinedTextField(value = spass, onValueChange = { spass = it }, label = { Text("Initial Login Password") }, modifier = Modifier.fillMaxWidth()) }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (sname.isNotBlank() && eid.isNotBlank()) {
                            viewModel.createEmployee(
                                empId = eid,
                                name = sname,
                                mobile = smobile,
                                email = semail,
                                dept = sdept,
                                desg = sdesg,
                                doj = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                                pass = spass
                            )
                            showAddStaff = false
                        }
                    }
                ) {
                    Text("Register Staff")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddStaff = false }) { Text("Cancel") }
            }
        )
    }
}
