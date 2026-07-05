package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ActivityLog
import com.example.data.AppDatabase
import com.example.data.Attendance
import com.example.data.Client
import com.example.data.DailyWorkReport
import com.example.data.DatabaseInitializer
import com.example.data.Document
import com.example.data.Employee
import com.example.data.ErpRepository
import com.example.data.InternalMessage
import com.example.data.Lead
import com.example.data.Notification
import com.example.data.Payment
import com.example.data.SettingsEntity
import com.example.data.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ErpViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ErpRepository

    // State flows representing the database tables
    val employees: StateFlow<List<Employee>>
    val clients: StateFlow<List<Client>>
    val leads: StateFlow<List<Lead>>
    val tasks: StateFlow<List<Task>>
    val attendance: StateFlow<List<Attendance>>
    val reports: StateFlow<List<DailyWorkReport>>
    val messages: StateFlow<List<InternalMessage>>
    val notifications: StateFlow<List<Notification>>
    val payments: StateFlow<List<Payment>>
    val documents: StateFlow<List<Document>>
    val settings: StateFlow<List<SettingsEntity>>
    val logs: StateFlow<List<ActivityLog>>

    // Current Session State
    private val _currentUser = MutableStateFlow<Employee?>(null)
    val currentUser: StateFlow<Employee?> = _currentUser.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    // Active attendance for the logged in user today
    private val _todayAttendance = MutableStateFlow<Attendance?>(null)
    val todayAttendance: StateFlow<Attendance?> = _todayAttendance.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = ErpRepository(database.erpDao())

        // Seed the database if empty on launch
        viewModelScope.launch {
            DatabaseInitializer.seedIfEmpty(repository)
        }

        // Initialize state flows
        employees = repository.allEmployees.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
        clients = repository.allClients.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
        leads = repository.allLeads.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
        tasks = repository.allTasks.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
        attendance = repository.allAttendance.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
        reports = repository.allReports.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
        messages = repository.allMessages.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
        notifications = repository.allNotifications.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
        payments = repository.allPayments.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
        documents = repository.allDocuments.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
        settings = repository.allSettings.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
        logs = repository.allLogs.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    }

    // --- Authentication ---

    fun login(employeeId: String, password: String) {
        viewModelScope.launch {
            val user = repository.getEmployeeById(employeeId.trim())
            if (user == null) {
                _loginError.value = "Invalid Employee ID"
                return@launch
            }
            if (user.status == "Inactive") {
                _loginError.value = "Your account is suspended. Contact Super Admin."
                return@launch
            }
            if (user.loginPassword != password) {
                _loginError.value = "Incorrect password"
                return@launch
            }

            _loginError.value = null
            _currentUser.value = user
            logActivity(user.employeeId, user.name, "Login", "Successfully logged into TansionGO ERP.")
            checkTodayAttendance(user.employeeId)
        }
    }

    fun logout() {
        val user = _currentUser.value
        if (user != null) {
            logActivity(user.employeeId, user.name, "Logout", "Logged out of TansionGO ERP.")
        }
        _currentUser.value = null
        _todayAttendance.value = null
    }

    fun forgotPasswordMock(employeeId: String) {
        viewModelScope.launch {
            val user = repository.getEmployeeById(employeeId.trim())
            if (user == null) {
                _loginError.value = "No account found with this ID"
            } else {
                _loginError.value = "Password reset request sent. Please contact CEO Admin to reset your password."
                logActivity(user.employeeId, user.name, "Forgot Password", "Requested password reset support.")
            }
        }
    }

    // --- Activity Logging helper ---
    private fun logActivity(employeeId: String, name: String, action: String, details: String) {
        viewModelScope.launch {
            repository.insertLog(ActivityLog(employeeId = employeeId, employeeName = name, action = action, details = details))
        }
    }

    // --- Attendance and Punch clock ---

    private fun getCurrentDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    private fun getCurrentTimeString(): String {
        return SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
    }

    fun checkTodayAttendance(employeeId: String) {
        viewModelScope.launch {
            val date = getCurrentDateString()
            val att = repository.getAttendanceForEmployeeAndDate(employeeId, date)
            _todayAttendance.value = att
        }
    }

    fun checkIn() {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val date = getCurrentDateString()
            val time = getCurrentTimeString()

            // Check if already checked in
            val existing = repository.getAttendanceForEmployeeAndDate(user.employeeId, date)
            if (existing != null) return@launch

            // Check if late (e.g. after 09:30 AM)
            val isLate = checkIfLate(time)

            val newAttendance = Attendance(
                employeeId = user.employeeId,
                date = date,
                checkInTime = time,
                checkOutTime = null,
                workingHours = 0.0,
                isLate = isLate,
                leaveRequestStatus = null,
                leaveReason = null
            )
            repository.insertAttendance(newAttendance)
            checkTodayAttendance(user.employeeId)

            logActivity(user.employeeId, user.name, "Attendance Check In", "Checked in at $time. Late: $isLate")
            triggerInstantNotification(user.employeeId, "Checked In", "You checked in successfully today at $time.")
        }
    }

    fun checkOut() {
        val user = _currentUser.value ?: return
        val att = _todayAttendance.value ?: return
        if (att.checkOutTime != null) return // Already checked out

        viewModelScope.launch {
            val time = getCurrentTimeString()
            val hours = calculateWorkingHours(att.checkInTime ?: "09:00:00", time)

            val updatedAttendance = att.copy(
                checkOutTime = time,
                workingHours = hours
            )
            repository.insertAttendance(updatedAttendance)
            checkTodayAttendance(user.employeeId)

            logActivity(user.employeeId, user.name, "Attendance Check Out", "Checked out at $time. Total hours: $hours")
            triggerInstantNotification(user.employeeId, "Checked Out", "You checked out successfully today at $time. Working Hours: $hours hrs.")
        }
    }

    fun submitLeaveRequest(reason: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val date = getCurrentDateString()
            val leaveRequest = Attendance(
                employeeId = user.employeeId,
                date = date,
                checkInTime = null,
                checkOutTime = null,
                workingHours = 0.0,
                isLate = false,
                leaveRequestStatus = "Pending",
                leaveReason = reason
            )
            repository.insertAttendance(leaveRequest)
            checkTodayAttendance(user.employeeId)

            logActivity(user.employeeId, user.name, "Leave Request", "Submitted leave request for $date. Reason: $reason")
            triggerInstantNotification("EMP101", "New Leave Request", "${user.name} requested leave for $date.")
        }
    }

    fun approveLeaveRequest(attendanceId: Int, approve: Boolean) {
        val admin = _currentUser.value ?: return
        if (admin.role != "Super Admin") return
        viewModelScope.launch {
            // Retrieve all attendance to find the one to update
            val allAtt = repository.allAttendance.first()
            val item = allAtt.find { it.attendanceId == attendanceId } ?: return@launch
            val status = if (approve) "Approved" else "Rejected"
            val updated = item.copy(leaveRequestStatus = status)
            repository.insertAttendance(updated)

            logActivity(admin.employeeId, admin.name, "Leave Approval", "Leave request #$attendanceId marked as $status")
            triggerInstantNotification(item.employeeId, "Leave Status Updated", "Your leave request for ${item.date} has been $status.")
        }
    }

    private fun checkIfLate(time: String): Boolean {
        return try {
            val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val threshold = sdf.parse("09:30:00")
            val current = sdf.parse(time)
            current.after(threshold)
        } catch (e: Exception) {
            false
        }
    }

    private fun calculateWorkingHours(checkIn: String, checkOut: String): Double {
        return try {
            val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val inDate = sdf.parse(checkIn)
            val outDate = sdf.parse(checkOut)
            val diff = outDate.time - inDate.time
            val hours = diff.toDouble() / (1000 * 60 * 60)
            String.format(Locale.US, "%.2f", hours).toDouble()
        } catch (e: Exception) {
            8.0
        }
    }

    // --- Work Reports ---

    fun submitDailyWorkReport(
        todayWork: String,
        callsDone: Int,
        meetingsDone: Int,
        leadsGenerated: Int,
        clientsConverted: Int,
        problemsFaced: String,
        tomorrowPlan: String
    ) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val date = getCurrentDateString()
            val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

            val report = DailyWorkReport(
                employeeId = user.employeeId,
                employeeName = user.name,
                date = date,
                todayWork = todayWork,
                callsDone = callsDone,
                meetingsDone = meetingsDone,
                leadsGenerated = leadsGenerated,
                clientsConverted = clientsConverted,
                problemsFaced = problemsFaced,
                tomorrowPlan = tomorrowPlan,
                timeSubmitted = time
            )
            repository.insertReport(report)

            logActivity(user.employeeId, user.name, "Work Report Submission", "Submitted work report for $date.")
            triggerInstantNotification("EMP101", "New Work Report", "${user.name} submitted their daily report.")
        }
    }

    // --- Task Management ---

    fun createTask(
        title: String,
        description: String,
        assignedEmpId: String,
        assignedEmpName: String,
        priority: String,
        deadline: String
    ) {
        val admin = _currentUser.value ?: return
        if (admin.role != "Super Admin") return
        viewModelScope.launch {
            val task = Task(
                title = title,
                description = description,
                assignedEmployeeId = assignedEmpId,
                assignedEmployeeName = assignedEmpName,
                priority = priority,
                deadline = deadline,
                progressPercent = 0,
                status = "Pending"
            )
            repository.insertTask(task)
            logActivity(admin.employeeId, admin.name, "Create Task", "Assigned task '$title' to $assignedEmpName")
            triggerInstantNotification(assignedEmpId, "New Task Assigned", "Admin assigned: '$title'. Deadline: $deadline")
        }
    }

    fun updateTaskProgress(taskId: Int, progress: Int, comment: String, status: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            // Fetch task
            val allTs = repository.allTasks.first()
            val task = allTs.find { it.taskId == taskId } ?: return@launch

            val updated = task.copy(
                progressPercent = progress,
                status = status,
                comments = if (comment.isNotEmpty()) "${task.comments}\n[${getCurrentDateString()}] $comment" else task.comments
            )
            repository.insertTask(updated)

            logActivity(user.employeeId, user.name, "Update Task", "Updated task #$taskId progress to $progress% ($status)")
            if (user.role == "Employee") {
                triggerInstantNotification("EMP101", "Task Updated", "${user.name} updated task: '${task.title}' to $progress%.")
            }
        }
    }

    fun deleteTask(task: Task) {
        val admin = _currentUser.value ?: return
        if (admin.role != "Super Admin") return
        viewModelScope.launch {
            repository.deleteTask(task)
            logActivity(admin.employeeId, admin.name, "Delete Task", "Deleted task '${task.title}'")
        }
    }

    // --- Lead Management ---

    fun createLead(
        name: String,
        email: String,
        phone: String,
        source: String,
        status: String,
        priority: String,
        interestedServices: String,
        clientNotes: String,
        nextFollowUpDate: String,
        reminderTime: String,
        assignedEmpId: String?,
        assignedEmpName: String?
    ) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val lead = Lead(
                name = name,
                email = email,
                phone = phone,
                source = source,
                status = status,
                priority = priority,
                interestedServices = interestedServices,
                clientNotes = clientNotes,
                nextFollowUpDate = nextFollowUpDate,
                reminderTime = reminderTime,
                assignedEmployeeId = assignedEmpId,
                assignedEmployeeName = assignedEmpName
            )
            repository.insertLead(lead)
            logActivity(user.employeeId, user.name, "Create Lead", "Created lead for '$name'")
            if (assignedEmpId != null && assignedEmpId != user.employeeId) {
                triggerInstantNotification(assignedEmpId, "Lead Assigned", "You have been assigned a lead: '$name'")
            }
        }
    }

    fun updateLead(lead: Lead) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.insertLead(lead)
            logActivity(user.employeeId, user.name, "Update Lead", "Updated lead ID #${lead.leadId} for '${lead.name}'")
        }
    }

    fun deleteLead(lead: Lead) {
        val user = _currentUser.value ?: return
        if (user.role != "Super Admin") return
        viewModelScope.launch {
            repository.deleteLead(lead)
            logActivity(user.employeeId, user.name, "Delete Lead", "Deleted lead for '${lead.name}'")
        }
    }

    // --- Client Management ---

    fun createClient(
        clientId: String,
        companyName: String,
        ownerName: String,
        contactNumber: String,
        email: String,
        address: String,
        gstNumber: String,
        servicePurchased: String,
        paymentStatus: String,
        invoiceNumber: String,
        remarks: String
    ) {
        val admin = _currentUser.value ?: return
        if (admin.role != "Super Admin") return
        viewModelScope.launch {
            val cl = Client(
                clientId = clientId,
                companyName = companyName,
                ownerName = ownerName,
                contactNumber = contactNumber,
                email = email,
                address = address,
                gstNumber = gstNumber,
                servicePurchased = servicePurchased,
                paymentStatus = paymentStatus,
                invoiceNumber = invoiceNumber,
                remarks = remarks
            )
            repository.insertClient(cl)
            logActivity(admin.employeeId, admin.name, "Create Client", "Registered client '$companyName'")

            // Log corresponding payment entry
            val amt = when (servicePurchased) {
                "ERP Customization" -> 5500.0
                "Cloud Hosting Integration" -> 2500.0
                else -> 1500.0
            }
            repository.insertPayment(
                Payment(
                    clientId = clientId,
                    companyName = companyName,
                    invoiceNumber = invoiceNumber,
                    amount = amt,
                    status = paymentStatus,
                    date = getCurrentDateString()
                )
            )
        }
    }

    fun updateClient(client: Client) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.insertClient(client)
            logActivity(user.employeeId, user.name, "Update Client", "Updated client details for '${client.companyName}'")
        }
    }

    fun deleteClient(client: Client) {
        val admin = _currentUser.value ?: return
        if (admin.role != "Super Admin") return
        viewModelScope.launch {
            repository.deleteClient(client)
            logActivity(admin.employeeId, admin.name, "Delete Client", "Deleted client '${client.companyName}'")
        }
    }

    // --- Employee Management (Admin Only) ---

    fun createEmployee(
        empId: String,
        name: String,
        mobile: String,
        email: String,
        dept: String,
        desg: String,
        doj: String,
        pass: String
    ) {
        val admin = _currentUser.value ?: return
        if (admin.role != "Super Admin") return
        viewModelScope.launch {
            val emp = Employee(
                employeeId = empId.trim(),
                name = name,
                mobileNumber = mobile,
                email = email,
                department = dept,
                designation = desg,
                dateOfJoining = doj,
                status = "Active",
                loginPassword = pass,
                role = "Employee"
            )
            repository.insertEmployee(emp)
            logActivity(admin.employeeId, admin.name, "Create Employee", "Created employee account: '$name' (ID: $empId)")
            triggerInstantNotification(empId, "Welcome!", "Your TansionGO account has been created. Use password '$pass' to log in.")
        }
    }

    fun updateEmployeeStatus(employee: Employee, status: String) {
        val admin = _currentUser.value ?: return
        if (admin.role != "Super Admin") return
        viewModelScope.launch {
            val updated = employee.copy(status = status)
            repository.insertEmployee(updated)
            logActivity(admin.employeeId, admin.name, "Update Employee Status", "Changed status of ${employee.name} to $status")
            triggerInstantNotification(employee.employeeId, "Account Updated", "Your account status was updated to $status by admin.")
        }
    }

    fun resetEmployeePassword(employee: Employee, newPass: String) {
        val admin = _currentUser.value ?: return
        if (admin.role != "Super Admin") return
        viewModelScope.launch {
            val updated = employee.copy(loginPassword = newPass)
            repository.insertEmployee(updated)
            logActivity(admin.employeeId, admin.name, "Password Reset", "Reset password for ${employee.name}")
            triggerInstantNotification(employee.employeeId, "Security Update", "Your login password has been reset by the Admin.")
        }
    }

    fun updateSelfPassword(newPass: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val updated = user.copy(loginPassword = newPass)
            repository.insertEmployee(updated)
            _currentUser.value = updated // Update the flow state in memory too!
            logActivity(user.employeeId, user.name, "Password Update", "Updated self login password.")
        }
    }

    fun deleteEmployee(employee: Employee) {
        val admin = _currentUser.value ?: return
        if (admin.role != "Super Admin") return
        viewModelScope.launch {
            repository.deleteEmployee(employee)
            logActivity(admin.employeeId, admin.name, "Delete Employee", "Deleted employee account: ${employee.name}")
        }
    }

    // --- Instant Notifications ---

    private fun triggerInstantNotification(employeeId: String, title: String, message: String) {
        viewModelScope.launch {
            repository.insertNotification(
                Notification(
                    recipientEmployeeId = employeeId,
                    title = title,
                    message = message,
                    type = "Announcement",
                    timestamp = System.currentTimeMillis(),
                    isRead = false
                )
            )
        }
    }

    fun markNotificationAsRead(id: Int) {
        viewModelScope.launch {
            repository.markNotificationRead(id)
        }
    }

    // --- Settings & Admin Control Panel ---

    fun updateSetting(key: String, value: String) {
        val admin = _currentUser.value ?: return
        if (admin.role != "Super Admin") return
        viewModelScope.launch {
            repository.insertSetting(SettingsEntity(key, value))
            logActivity(admin.employeeId, admin.name, "Update Setting", "Updated preference '$key' to '$value'")
        }
    }

    fun backupDatabaseMock() {
        val admin = _currentUser.value ?: return
        if (admin.role != "Super Admin") return
        viewModelScope.launch {
            logActivity(admin.employeeId, admin.name, "Database Backup", "Completed cloud security backup snapshot successfully.")
        }
    }

    fun restoreDatabaseMock() {
        val admin = _currentUser.value ?: return
        if (admin.role != "Super Admin") return
        viewModelScope.launch {
            logActivity(admin.employeeId, admin.name, "Database Restore", "Restored system configuration from safe state.")
        }
    }

    // --- Chat Room Engine ---

    fun sendMessage(recipientId: String, content: String, attachType: String? = null, attachName: String? = null) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val msg = InternalMessage(
                senderId = user.employeeId,
                senderName = user.name,
                receiverId = recipientId,
                content = content,
                attachmentType = attachType,
                attachmentName = attachName
            )
            repository.insertMessage(msg)
            logActivity(user.employeeId, user.name, "Chat Send Message", "Sent message to '$recipientId'")
            if (recipientId != "BROADCAST") {
                triggerInstantNotification(recipientId, "New Message from ${user.name}", content)
            }
        }
    }

    // --- Mock Documents uploads ---

    fun uploadDocumentMock(title: String, type: String, filename: String, size: String, ownerId: String?) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val doc = Document(
                title = title,
                type = type,
                fileName = filename,
                fileSize = size,
                uploadDate = getCurrentDateString(),
                uploadedBy = user.name,
                ownerId = ownerId
            )
            repository.insertDocument(doc)
            logActivity(user.employeeId, user.name, "Upload Document", "Uploaded document: '$title'")
        }
    }

    fun deleteDocument(doc: Document) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.deleteDocument(doc)
            logActivity(user.employeeId, user.name, "Delete Document", "Deleted document: '${doc.title}'")
        }
    }
}
