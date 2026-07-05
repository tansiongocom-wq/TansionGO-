package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "employees")
data class Employee(
    @PrimaryKey val employeeId: String, // e.g., EMP101
    val name: String,
    val mobileNumber: String,
    val email: String,
    val department: String,
    val designation: String,
    val dateOfJoining: String,
    val status: String, // "Active", "Inactive"
    val loginPassword: String,
    val role: String // "Super Admin", "Employee"
)

@Entity(tableName = "clients")
data class Client(
    @PrimaryKey val clientId: String, // e.g., CLNT201
    val companyName: String,
    val ownerName: String,
    val contactNumber: String,
    val email: String,
    val address: String,
    val gstNumber: String,
    val servicePurchased: String,
    val paymentStatus: String, // "Paid", "Pending", "Overdue"
    val invoiceNumber: String,
    val remarks: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "leads")
data class Lead(
    @PrimaryKey(autoGenerate = true) val leadId: Int = 0,
    val name: String,
    val email: String,
    val phone: String,
    val source: String, // "Social Media", "Website", "Referral", "Cold Call", "Other"
    val status: String, // "New", "Contacted", "Interested", "Closed Won", "Closed Lost"
    val priority: String, // "High", "Medium", "Low"
    val interestedServices: String,
    val clientNotes: String,
    val nextFollowUpDate: String, // YYYY-MM-DD
    val reminderTime: String, // HH:MM
    val assignedEmployeeId: String?,
    val assignedEmployeeName: String?
)

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val taskId: Int = 0,
    val title: String,
    val description: String,
    val assignedEmployeeId: String,
    val assignedEmployeeName: String,
    val priority: String, // "High", "Medium", "Low"
    val deadline: String, // YYYY-MM-DD
    val progressPercent: Int = 0, // 0-100
    val status: String, // "Pending", "Completed", "Cancelled"
    val comments: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "attendance")
data class Attendance(
    @PrimaryKey(autoGenerate = true) val attendanceId: Int = 0,
    val employeeId: String,
    val date: String, // YYYY-MM-DD
    val checkInTime: String?, // HH:MM:SS
    val checkOutTime: String?, // HH:MM:SS
    val workingHours: Double = 0.0,
    val isLate: Boolean = false,
    val leaveRequestStatus: String? = null, // "Pending", "Approved", "Rejected"
    val leaveReason: String? = null
)

@Entity(tableName = "daily_work_reports")
data class DailyWorkReport(
    @PrimaryKey(autoGenerate = true) val reportId: Int = 0,
    val employeeId: String,
    val employeeName: String,
    val date: String, // YYYY-MM-DD
    val todayWork: String,
    val callsDone: Int,
    val meetingsDone: Int,
    val leadsGenerated: Int,
    val clientsConverted: Int,
    val problemsFaced: String,
    val tomorrowPlan: String,
    val timeSubmitted: String // HH:MM
)

@Entity(tableName = "internal_messages")
data class InternalMessage(
    @PrimaryKey(autoGenerate = true) val messageId: Int = 0,
    val senderId: String,
    val senderName: String,
    val receiverId: String, // EmployeeId or "BROADCAST"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val attachmentType: String? = null, // "Image", "File"
    val attachmentName: String? = null
)

@Entity(tableName = "notifications")
data class Notification(
    @PrimaryKey(autoGenerate = true) val notificationId: Int = 0,
    val recipientEmployeeId: String, // "ALL" or specific EmployeeId
    val title: String,
    val message: String,
    val type: String, // "Task", "Meeting", "Payment", "FollowUp", "Announcement"
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

@Entity(tableName = "payments")
data class Payment(
    @PrimaryKey(autoGenerate = true) val paymentId: Int = 0,
    val clientId: String,
    val companyName: String,
    val invoiceNumber: String,
    val amount: Double,
    val status: String, // "Paid", "Pending", "Overdue"
    val date: String // YYYY-MM-DD
)

@Entity(tableName = "documents")
data class Document(
    @PrimaryKey(autoGenerate = true) val documentId: Int = 0,
    val title: String,
    val type: String, // "Employee", "Client", "Invoice", "Agreement", "Image", "PDF", "Excel"
    val fileName: String,
    val fileSize: String,
    val uploadDate: String, // YYYY-MM-DD
    val uploadedBy: String, // Name of person who uploaded
    val ownerId: String? // EmployeeId or ClientId
)

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val key: String,
    val value: String
)

@Entity(tableName = "activity_logs")
data class ActivityLog(
    @PrimaryKey(autoGenerate = true) val logId: Int = 0,
    val employeeId: String,
    val employeeName: String,
    val action: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)
