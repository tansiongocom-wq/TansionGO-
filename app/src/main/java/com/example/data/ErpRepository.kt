package com.example.data

import kotlinx.coroutines.flow.Flow

class ErpRepository(private val erpDao: ErpDao) {

    // Employees
    val allEmployees: Flow<List<Employee>> = erpDao.getAllEmployees()

    suspend fun getEmployeeById(id: String): Employee? = erpDao.getEmployeeById(id)
    suspend fun getEmployeeByEmail(email: String): Employee? = erpDao.getEmployeeByEmail(email)
    suspend fun insertEmployee(employee: Employee) = erpDao.insertEmployee(employee)
    suspend fun updateEmployee(employee: Employee) = erpDao.updateEmployee(employee)
    suspend fun deleteEmployee(employee: Employee) = erpDao.deleteEmployee(employee)

    // Clients
    val allClients: Flow<List<Client>> = erpDao.getAllClients()
    suspend fun insertClient(client: Client) = erpDao.insertClient(client)
    suspend fun deleteClient(client: Client) = erpDao.deleteClient(client)

    // Leads
    val allLeads: Flow<List<Lead>> = erpDao.getAllLeads()
    suspend fun insertLead(lead: Lead) = erpDao.insertLead(lead)
    suspend fun deleteLead(lead: Lead) = erpDao.deleteLead(lead)

    // Tasks
    val allTasks: Flow<List<Task>> = erpDao.getAllTasks()
    fun getTasksForEmployee(employeeId: String): Flow<List<Task>> = erpDao.getTasksForEmployee(employeeId)
    suspend fun insertTask(task: Task) = erpDao.insertTask(task)
    suspend fun deleteTask(task: Task) = erpDao.deleteTask(task)

    // Attendance
    val allAttendance: Flow<List<Attendance>> = erpDao.getAllAttendance()
    fun getAttendanceForEmployee(employeeId: String): Flow<List<Attendance>> = erpDao.getAttendanceForEmployee(employeeId)
    suspend fun getAttendanceForEmployeeAndDate(employeeId: String, date: String): Attendance? =
        erpDao.getAttendanceForEmployeeAndDate(employeeId, date)
    suspend fun insertAttendance(attendance: Attendance) = erpDao.insertAttendance(attendance)

    // Daily Work Reports
    val allReports: Flow<List<DailyWorkReport>> = erpDao.getAllReports()
    fun getReportsForEmployee(employeeId: String): Flow<List<DailyWorkReport>> = erpDao.getReportsForEmployee(employeeId)
    suspend fun insertReport(report: DailyWorkReport) = erpDao.insertReport(report)

    // Chat
    val allMessages: Flow<List<InternalMessage>> = erpDao.getAllMessages()
    suspend fun insertMessage(message: InternalMessage) = erpDao.insertMessage(message)

    // Notifications
    val allNotifications: Flow<List<Notification>> = erpDao.getAllNotifications()
    fun getNotificationsForEmployee(employeeId: String): Flow<List<Notification>> = erpDao.getNotificationsForEmployee(employeeId)
    suspend fun insertNotification(notification: Notification) = erpDao.insertNotification(notification)
    suspend fun markNotificationRead(notificationId: Int) = erpDao.markNotificationRead(notificationId)

    // Payments
    val allPayments: Flow<List<Payment>> = erpDao.getAllPayments()
    suspend fun insertPayment(payment: Payment) = erpDao.insertPayment(payment)

    // Documents
    val allDocuments: Flow<List<Document>> = erpDao.getAllDocuments()
    suspend fun insertDocument(document: Document) = erpDao.insertDocument(document)
    suspend fun deleteDocument(document: Document) = erpDao.deleteDocument(document)

    // Settings
    val allSettings: Flow<List<SettingsEntity>> = erpDao.getAllSettings()
    suspend fun getSetting(key: String): SettingsEntity? = erpDao.getSetting(key)
    suspend fun insertSetting(setting: SettingsEntity) = erpDao.insertSetting(setting)

    // Activity Logs
    val allLogs: Flow<List<ActivityLog>> = erpDao.getAllLogs()
    suspend fun insertLog(log: ActivityLog) = erpDao.insertLog(log)
}
