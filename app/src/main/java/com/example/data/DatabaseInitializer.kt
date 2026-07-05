package com.example.data

class DatabaseInitializer {
    companion object {
        suspend fun seedIfEmpty(repository: ErpRepository) {
            // Check if any employee exists. If not, seed the database.
            val existing = repository.getEmployeeById("EMP101")
            if (existing != null) return

            // 1. Seed Employees
            val employees = listOf(
                Employee(
                    employeeId = "EMP101",
                    name = "CEO Admin",
                    mobileNumber = "+91 98765 43210",
                    email = "admin@tansiongo.com",
                    department = "Executive",
                    designation = "Super Admin",
                    dateOfJoining = "2026-01-01",
                    status = "Active",
                    loginPassword = "admin",
                    role = "Super Admin"
                ),
                Employee(
                    employeeId = "EMP102",
                    name = "John Doe",
                    mobileNumber = "+91 98765 43211",
                    email = "john@tansiongo.com",
                    department = "Sales",
                    designation = "Sales Manager",
                    dateOfJoining = "2026-02-15",
                    status = "Active",
                    loginPassword = "password",
                    role = "Employee"
                ),
                Employee(
                    employeeId = "EMP103",
                    name = "Jane Smith",
                    mobileNumber = "+91 98765 43212",
                    email = "jane@tansiongo.com",
                    department = "Development",
                    designation = "Senior Engineer",
                    dateOfJoining = "2026-03-01",
                    status = "Active",
                    loginPassword = "password",
                    role = "Employee"
                ),
                Employee(
                    employeeId = "EMP104",
                    name = "Robert Johnson",
                    mobileNumber = "+91 98765 43213",
                    email = "robert@tansiongo.com",
                    department = "Design",
                    designation = "UI/UX Designer",
                    dateOfJoining = "2026-04-10",
                    status = "Active",
                    loginPassword = "password",
                    role = "Employee"
                )
            )

            for (emp in employees) {
                repository.insertEmployee(emp)
            }

            // 2. Seed Clients
            val clients = listOf(
                Client(
                    clientId = "CLNT201",
                    companyName = "Apex Global Inc.",
                    ownerName = "Harvey Specter",
                    contactNumber = "+91 90000 11111",
                    email = "harvey@apex.com",
                    address = "123 Wall Street, Mumbai",
                    gstNumber = "27AAAAA1111A1Z1",
                    servicePurchased = "ERP Customization",
                    paymentStatus = "Paid",
                    invoiceNumber = "INV-2026-001",
                    remarks = "High profile client, key stakeholder",
                    createdAt = System.currentTimeMillis() - 10 * 24 * 60 * 60 * 1000L
                ),
                Client(
                    clientId = "CLNT202",
                    companyName = "Zenith Tech Labs",
                    ownerName = "Louis Litt",
                    contactNumber = "+91 90000 22222",
                    email = "louis@zenith.com",
                    address = "456 Silicon Valley, Bangalore",
                    gstNumber = "29BBBBB2222B2Z2",
                    servicePurchased = "Cloud Hosting Integration",
                    paymentStatus = "Pending",
                    invoiceNumber = "INV-2026-002",
                    remarks = "Payment follow-up scheduled for next week",
                    createdAt = System.currentTimeMillis() - 5 * 24 * 60 * 60 * 1000L
                ),
                Client(
                    clientId = "CLNT203",
                    companyName = "Nova Retail Ltd.",
                    ownerName = "Donna Paulsen",
                    contactNumber = "+91 90000 33333",
                    email = "donna@nova.com",
                    address = "789 Trade Tower, Delhi",
                    gstNumber = "07CCCCC3333C3Z3",
                    servicePurchased = "CRM Solutions",
                    paymentStatus = "Overdue",
                    invoiceNumber = "INV-2026-003",
                    remarks = "Follow-up calls unanswered",
                    createdAt = System.currentTimeMillis() - 15 * 24 * 60 * 60 * 1000L
                )
            )

            for (cl in clients) {
                repository.insertClient(cl)
            }

            // 3. Seed Leads
            val leads = listOf(
                Lead(
                    name = "Alex Mercer",
                    email = "alex@gentec.com",
                    phone = "+91 95555 12345",
                    source = "Website",
                    status = "Interested",
                    priority = "High",
                    interestedServices = "ERP Customization",
                    clientNotes = "Looking for a tailored mobile app for their factory workers.",
                    nextFollowUpDate = "2026-07-05",
                    reminderTime = "10:30",
                    assignedEmployeeId = "EMP102",
                    assignedEmployeeName = "John Doe"
                ),
                Lead(
                    name = "Samantha Cole",
                    email = "sam@vortex.co",
                    phone = "+91 95555 54321",
                    source = "Social Media",
                    status = "New",
                    priority = "Medium",
                    interestedServices = "UI/UX Design UI kit",
                    clientNotes = "Inquired via LinkedIn about web redesign.",
                    nextFollowUpDate = "2026-07-06",
                    reminderTime = "14:00",
                    assignedEmployeeId = "EMP102",
                    assignedEmployeeName = "John Doe"
                ),
                Lead(
                    name = "Michael Scott",
                    email = "michael@dundermifflin.com",
                    phone = "+91 95555 99999",
                    source = "Referral",
                    status = "Contacted",
                    priority = "Low",
                    interestedServices = "CRM Solutions",
                    clientNotes = "Wants to organize paper supply sales tracking.",
                    nextFollowUpDate = "2026-07-08",
                    reminderTime = "11:00",
                    assignedEmployeeId = "EMP102",
                    assignedEmployeeName = "John Doe"
                )
            )

            for (ld in leads) {
                repository.insertLead(ld)
            }

            // 4. Seed Tasks
            val tasks = listOf(
                Task(
                    title = "Review ERP Architecture Specs",
                    description = "Analyze scalability requirements and write DB index design guidelines.",
                    assignedEmployeeId = "EMP103",
                    assignedEmployeeName = "Jane Smith",
                    priority = "High",
                    deadline = "2026-07-10",
                    progressPercent = 40,
                    status = "Pending"
                ),
                Task(
                    title = "Design Mockups for Mobile Apps",
                    description = "Create cohesive dashboard and reporting flow visual mockups.",
                    assignedEmployeeId = "EMP104",
                    assignedEmployeeName = "Robert Johnson",
                    priority = "High",
                    deadline = "2026-07-06",
                    progressPercent = 100,
                    status = "Completed"
                ),
                Task(
                    title = "Follow Up with Apex Global",
                    description = "Call Harvey Specter to finalize the hosting server size.",
                    assignedEmployeeId = "EMP102",
                    assignedEmployeeName = "John Doe",
                    priority = "Medium",
                    deadline = "2026-07-05",
                    progressPercent = 0,
                    status = "Pending"
                )
            )

            for (ts in tasks) {
                repository.insertTask(ts)
            }

            // 5. Seed Attendance
            val attendanceRecords = listOf(
                Attendance(
                    employeeId = "EMP102",
                    date = "2026-07-04",
                    checkInTime = "09:15:00",
                    checkOutTime = "18:00:00",
                    workingHours = 8.75,
                    isLate = false
                ),
                Attendance(
                    employeeId = "EMP103",
                    date = "2026-07-04",
                    checkInTime = "09:45:00",
                    checkOutTime = null,
                    workingHours = 8.0,
                    isLate = true // Late entry (past 09:30)
                ),
                Attendance(
                    employeeId = "EMP104",
                    date = "2026-07-04",
                    checkInTime = "09:05:00",
                    checkOutTime = "17:30:00",
                    workingHours = 8.41,
                    isLate = false
                )
            )

            for (att in attendanceRecords) {
                repository.insertAttendance(att)
            }

            // 6. Seed Daily Work Reports
            val report = DailyWorkReport(
                employeeId = "EMP102",
                employeeName = "John Doe",
                date = "2026-07-04",
                todayWork = "Contacted 5 warm leads, completed follow up documentation for Zenith Tech.",
                callsDone = 12,
                meetingsDone = 2,
                leadsGenerated = 2,
                clientsConverted = 1,
                problemsFaced = "Slow response from retail merchants database.",
                tomorrowPlan = "Focus purely on closing Zenith Tech agreement.",
                timeSubmitted = "17:55"
            )
            repository.insertReport(report)

            // 7. Seed Chat Messages
            val messages = listOf(
                InternalMessage(
                    senderId = "EMP101",
                    senderName = "CEO Admin",
                    receiverId = "BROADCAST",
                    content = "Welcome to the new TansionGO ERP system! Use the chat, tasks, and reports to manage your daily tasks. Feel free to contact executive support for queries."
                ),
                InternalMessage(
                    senderId = "EMP102",
                    senderName = "John Doe",
                    receiverId = "EMP101",
                    content = "Hello Admin, I have successfully checked in and updated my leads for today."
                ),
                InternalMessage(
                    senderId = "EMP101",
                    senderName = "CEO Admin",
                    receiverId = "EMP102",
                    content = "Excellent work John, please make sure you upload the invoice details for CLNT201 Apex Global once finalized."
                )
            )

            for (msg in messages) {
                repository.insertMessage(msg)
            }

            // 8. Seed Notifications
            val notifications = listOf(
                Notification(
                    recipientEmployeeId = "EMP102",
                    title = "New Task Assigned",
                    message = "Admin assigned task: 'Follow Up with Apex Global'",
                    type = "Task"
                ),
                Notification(
                    recipientEmployeeId = "ALL",
                    title = "Welcome Announcement",
                    message = "CEO Admin Broadcasted: TansionGO ERP is officially live!",
                    type = "Announcement"
                )
            )

            for (notif in notifications) {
                repository.insertNotification(notif)
            }

            // 9. Seed Payments
            val payments = listOf(
                Payment(
                    clientId = "CLNT201",
                    companyName = "Apex Global Inc.",
                    invoiceNumber = "INV-2026-001",
                    amount = 5500.0,
                    status = "Paid",
                    date = "2026-06-25"
                ),
                Payment(
                    clientId = "CLNT202",
                    companyName = "Zenith Tech Labs",
                    invoiceNumber = "INV-2026-002",
                    amount = 2500.0,
                    status = "Pending",
                    date = "2026-07-02"
                )
            )

            for (pay in payments) {
                repository.insertPayment(pay)
            }

            // 10. Seed Documents
            val docs = listOf(
                Document(
                    title = "Apex Global Master Service Agreement",
                    type = "Agreement",
                    fileName = "MSA_ApexGlobal.pdf",
                    fileSize = "2.4 MB",
                    uploadDate = "2026-06-25",
                    uploadedBy = "CEO Admin",
                    ownerId = "CLNT201"
                ),
                Document(
                    title = "Zenith Custom Design Spec",
                    type = "Client",
                    fileName = "ZenithSpec_v1.pdf",
                    fileSize = "1.2 MB",
                    uploadDate = "2026-07-01",
                    uploadedBy = "Robert Johnson",
                    ownerId = "CLNT202"
                )
            )

            for (dc in docs) {
                repository.insertDocument(dc)
            }

            // 11. Seed Settings
            val settings = listOf(
                SettingsEntity("company_name", "TansionGO Private Limited"),
                SettingsEntity("business_info", "B2B Software and Enterprise IT Solutions Provider"),
                SettingsEntity("working_hours", "09:30 to 18:30"),
                SettingsEntity("departments", "Executive, Sales, Development, Design, HR"),
                SettingsEntity("designations", "CEO, CTO, Senior Engineer, Sales Manager, UI/UX Designer, Specialist")
            )

            for (st in settings) {
                repository.insertSetting(st)
            }

            // 12. Seed Activity Logs
            val logs = listOf(
                ActivityLog(
                    employeeId = "EMP101",
                    employeeName = "CEO Admin",
                    action = "System Setup",
                    details = "Initialized database schemas and seeded core operational settings."
                ),
                ActivityLog(
                    employeeId = "EMP102",
                    employeeName = "John Doe",
                    action = "Attendance",
                    details = "Checked In today at 09:15 AM."
                )
            )

            for (lg in logs) {
                repository.insertLog(lg)
            }
        }
    }
}
