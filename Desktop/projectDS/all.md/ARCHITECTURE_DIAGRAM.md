# System Architecture Diagram
## Client-Server Deployment

---

## 🏗️ High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                         NETWORK LAYER                                │
│                    (LAN / VPN / Internet)                           │
└─────────────────────────────────────────────────────────────────────┘
                                  │
        ┌─────────────────────────┼─────────────────────────┐
        │                         │                         │
        ▼                         ▼                         ▼
┌───────────────┐         ┌───────────────┐         ┌───────────────┐
│  CLIENT PC 1  │         │  CLIENT PC 2  │         │  CLIENT PC 3  │
│               │         │               │         │               │
│ ┌───────────┐ │         │ ┌───────────┐ │         │ ┌───────────┐ │
│ │ Swing GUI │ │         │ │ Swing GUI │ │         │ │ Swing GUI │ │
│ │  Client   │ │         │ │  Client   │ │         │ │  Client   │ │
│ └─────┬─────┘ │         │ └─────┬─────┘ │         │ └─────┬─────┘ │
│       │       │         │       │       │         │       │       │
│   RMI Client  │         │   RMI Client  │         │   RMI Client  │
└───────┼───────┘         └───────┼───────┘         └───────┼───────┘
        │                         │                         │
        │         RMI over TCP/IP (Port 1099)              │
        │                         │                         │
        └─────────────────────────┼─────────────────────────┘
                                  │
                                  ▼
                    ┌─────────────────────────┐
                    │     SERVER PC           │
                    │                         │
                    │  ┌──────────────────┐   │
                    │  │  RMI Registry    │   │
                    │  │  (Port 1099)     │   │
                    │  └────────┬─────────┘   │
                    │           │             │
                    │  ┌────────▼─────────┐   │
                    │  │ AttendanceServer │   │
                    │  │  (RMI Service)   │   │
                    │  └────────┬─────────┘   │
                    │           │             │
                    │  ┌────────▼─────────┐   │
                    │  │ Business Logic   │   │
                    │  │  - Auth Service  │   │
                    │  │  - Attendance    │   │
                    │  │  - User Mgmt     │   │
                    │  │  - Reports       │   │
                    │  └────────┬─────────┘   │
                    │           │             │
                    │  ┌────────▼─────────┐   │
                    │  │   DAO Layer      │   │
                    │  │  - UserDAO       │   │
                    │  │  - AttendanceDAO │   │
                    │  │  - CourseDAO     │   │
                    │  └────────┬─────────┘   │
                    │           │             │
                    │  ┌────────▼─────────┐   │
                    │  │ Connection Pool  │   │
                    │  │   (HikariCP)     │   │
                    │  └────────┬─────────┘   │
                    │           │             │
                    │  ┌────────▼─────────┐   │
                    │  │  MySQL Database  │   │
                    │  │  (Port 3306)     │   │
                    │  │                  │   │
                    │  │  - USERS         │   │
                    │  │  - STUDENTS      │   │
                    │  │  - TEACHERS      │   │
                    │  │  - COURSES       │   │
                    │  │  - ATTENDANCE    │   │
                    │  │  - ENROLLMENTS   │   │
                    │  └──────────────────┘   │
                    └─────────────────────────┘
```

---

## 🔄 Request Flow Diagram

### Example: Student Login

```
┌─────────────┐                                    ┌─────────────┐
│   CLIENT    │                                    │   SERVER    │
└──────┬──────┘                                    └──────┬──────┘
       │                                                  │
       │  1. User enters credentials                     │
       │     (username, password)                        │
       │                                                  │
       │  2. authenticateUser(username, password)        │
       ├─────────────────────────────────────────────────>│
       │                                                  │
       │                                    3. Validate   │
       │                                       credentials│
       │                                                  │
       │                                    4. Check DB   │
       │                                       ┌──────────┤
       │                                       │  MySQL   │
       │                                       └──────────┤
       │                                                  │
       │                                    5. Create     │
       │                                       session    │
       │                                                  │
       │  6. Return AuthenticatedUser + sessionToken     │
       │<─────────────────────────────────────────────────┤
       │                                                  │
       │  7. Store session token                         │
       │     Show main dashboard                         │
       │                                                  │
       │  8. Subsequent requests include sessionToken    │
       ├─────────────────────────────────────────────────>│
       │                                                  │
       │  9. Validate session                            │
       │                                                  │
       │  10. Return requested data                      │
       │<─────────────────────────────────────────────────┤
       │                                                  │
```

---

## 📊 Data Flow Diagram

### Example: Mark Attendance

```
┌──────────┐         ┌──────────┐         ┌──────────┐         ┌──────────┐
│ Teacher  │         │  Client  │         │  Server  │         │ Database │
│   GUI    │         │   App    │         │   RMI    │         │  MySQL   │
└────┬─────┘         └────┬─────┘         └────┬─────┘         └────┬─────┘
     │                    │                    │                    │
     │ 1. Select student  │                    │                    │
     │    & mark present  │                    │                    │
     ├───────────────────>│                    │                    │
     │                    │                    │                    │
     │                    │ 2. markAttendance()│                    │
     │                    │    (sessionToken,  │                    │
     │                    │     record)        │                    │
     │                    ├───────────────────>│                    │
     │                    │                    │                    │
     │                    │                    │ 3. Validate session│
     │                    │                    │    & permissions   │
     │                    │                    │                    │
     │                    │                    │ 4. Validate record │
     │                    │                    │    (business rules)│
     │                    │                    │                    │
     │                    │                    │ 5. INSERT INTO     │
     │                    │                    │    ATTENDANCE_     │
     │                    │                    │    RECORDS         │
     │                    │                    ├───────────────────>│
     │                    │                    │                    │
     │                    │                    │ 6. Success/Failure │
     │                    │                    │<───────────────────┤
     │                    │                    │                    │
     │                    │ 7. Return result   │                    │
     │                    │<───────────────────┤                    │
     │                    │                    │                    │
     │ 8. Show success    │                    │                    │
     │    message         │                    │                    │
     │<───────────────────┤                    │                    │
     │                    │                    │                    │
```

---

## 🌐 Network Topology

### Local Area Network (LAN) Setup

```
                    ┌─────────────────┐
                    │   Router/Switch │
                    │  192.168.1.1    │
                    └────────┬────────┘
                             │
            ┌────────────────┼────────────────┐
            │                │                │
    ┌───────▼──────┐  ┌──────▼──────┐  ┌─────▼───────┐
    │  Server PC   │  │ Client PC 1 │  │ Client PC 2 │
    │ 192.168.1.100│  │192.168.1.101│  │192.168.1.102│
    │              │  │             │  │             │
    │ Port 1099    │  │             │  │             │
    │ (RMI Server) │  │             │  │             │
    │              │  │             │  │             │
    │ Port 3306    │  │             │  │             │
    │ (MySQL)      │  │             │  │             │
    └──────────────┘  └─────────────┘  └─────────────┘
```

### Wide Area Network (WAN) Setup with VPN

```
                    ┌─────────────────┐
                    │  Internet/WAN   │
                    └────────┬────────┘
                             │
                    ┌────────▼────────┐
                    │   VPN Server    │
                    │   10.8.0.1      │
                    └────────┬────────┘
                             │
            ┌────────────────┼────────────────┐
            │                │                │
    ┌───────▼──────┐  ┌──────▼──────┐  ┌─────▼───────┐
    │  Server PC   │  │ Client PC 1 │  │ Client PC 2 │
    │  10.8.0.10   │  │  10.8.0.11  │  │  10.8.0.12  │
    │              │  │             │  │             │
    │ Public IP:   │  │ Public IP:  │  │ Public IP:  │
    │ 203.0.113.10 │  │ 198.51.100.5│  │ 192.0.2.15  │
    │              │  │             │  │             │
    │ VPN Tunnel   │  │ VPN Tunnel  │  │ VPN Tunnel  │
    └──────────────┘  └─────────────┘  └─────────────┘
```

---

## 🔐 Security Layers

```
┌─────────────────────────────────────────────────────────────┐
│                    SECURITY LAYERS                          │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  Layer 1: Network Security                                 │
│  ┌───────────────────────────────────────────────────┐     │
│  │ • Firewall (UFW/iptables)                         │     │
│  │ • VPN Encryption (Optional)                       │     │
│  │ • Port Restrictions (1099 only)                   │     │
│  └───────────────────────────────────────────────────┘     │
│                          │                                  │
│  Layer 2: Transport Security                               │
│  ┌───────────────────────────────────────────────────┐     │
│  │ • RMI over SSL/TLS (Optional)                     │     │
│  │ • Certificate Validation                          │     │
│  └───────────────────────────────────────────────────┘     │
│                          │                                  │
│  Layer 3: Application Security                             │
│  ┌───────────────────────────────────────────────────┐     │
│  │ • Session Management                              │     │
│  │ • Session Binding (IP + User-Agent)               │     │
│  │ • Session Timeout (30 minutes)                    │     │
│  └───────────────────────────────────────────────────┘     │
│                          │                                  │
│  Layer 4: Authentication                                    │
│  ┌───────────────────────────────────────────────────┐     │
│  │ • Username/Password                               │     │
│  │ • BCrypt Password Hashing                         │     │
│  │ • Account Lockout (5 failed attempts)            │     │
│  │ • Rate Limiting                                   │     │
│  └───────────────────────────────────────────────────┘     │
│                          │                                  │
│  Layer 5: Authorization                                     │
│  ┌───────────────────────────────────────────────────┐     │
│  │ • Role-Based Access Control (RBAC)                │     │
│  │ • Permission Checks                               │     │
│  │ • Resource-Level Authorization                    │     │
│  └───────────────────────────────────────────────────┘     │
│                          │                                  │
│  Layer 6: Data Security                                     │
│  ┌───────────────────────────────────────────────────┐     │
│  │ • AES-256 Encryption (sensitive fields)           │     │
│  │ • SQL Injection Protection (PreparedStatements)   │     │
│  │ • Input Validation                                │     │
│  └───────────────────────────────────────────────────┘     │
│                          │                                  │
│  Layer 7: Audit & Monitoring                               │
│  ┌───────────────────────────────────────────────────┐     │
│  │ • Audit Logging                                   │     │
│  │ • Security Event Tracking                         │     │
│  │ • Anomaly Detection                               │     │
│  └───────────────────────────────────────────────────┘     │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## 📦 Component Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                      CLIENT SIDE                            │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌──────────────────────────────────────────────────┐      │
│  │           Presentation Layer (Swing)             │      │
│  ├──────────────────────────────────────────────────┤      │
│  │ • LoginFrame                                     │      │
│  │ • AdminDashboard                                 │      │
│  │ • TeacherDashboard                               │      │
│  │ • StudentDashboard                               │      │
│  │ • AttendanceMarkingPanel                         │      │
│  │ • UserManagementPanel                            │      │
│  │ • CourseManagementPanel                          │      │
│  │ • ReportGenerationPanel                          │      │
│  └──────────────────┬───────────────────────────────┘      │
│                     │                                       │
│  ┌──────────────────▼───────────────────────────────┐      │
│  │         RMI Client Stub Layer                    │      │
│  ├──────────────────────────────────────────────────┤      │
│  │ • AttendanceService (Remote Interface)           │      │
│  │ • Connection Management                          │      │
│  │ • Session Token Storage                          │      │
│  └──────────────────────────────────────────────────┘      │
│                                                             │
└─────────────────────────────────────────────────────────────┘
                              │
                              │ RMI over TCP/IP
                              │
┌─────────────────────────────▼───────────────────────────────┐
│                      SERVER SIDE                            │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌──────────────────────────────────────────────────┐      │
│  │         RMI Server Skeleton Layer                │      │
│  ├──────────────────────────────────────────────────┤      │
│  │ • RMI Registry (Port 1099)                       │      │
│  │ • AttendanceServer (Remote Object)               │      │
│  │ • Request Dispatcher                             │      │
│  └──────────────────┬───────────────────────────────┘      │
│                     │                                       │
│  ┌──────────────────▼───────────────────────────────┐      │
│  │            Service Layer                         │      │
│  ├──────────────────────────────────────────────────┤      │
│  │ • AuthenticationService                          │      │
│  │ • AttendanceServiceImpl                          │      │
│  │ • NotificationService                            │      │
│  │ • ReportService                                  │      │
│  └──────────────────┬───────────────────────────────┘      │
│                     │                                       │
│  ┌──────────────────▼───────────────────────────────┐      │
│  │            DAO Layer                             │      │
│  ├──────────────────────────────────────────────────┤      │
│  │ • UserDAO                                        │      │
│  │ • AttendanceDAO                                  │      │
│  │ • CourseDAO                                      │      │
│  │ • NotificationDAO                                │      │
│  │ • DatabaseManager (HikariCP)                     │      │
│  └──────────────────┬───────────────────────────────┘      │
│                     │                                       │
│  ┌──────────────────▼───────────────────────────────┐      │
│  │         Utility Layer                            │      │
│  ├──────────────────────────────────────────────────┤      │
│  │ • SecurityUtil (Encryption, Hashing)             │      │
│  │ • DateUtil                                       │      │
│  │ • ConfigManager                                  │      │
│  │ • CacheManager                                   │      │
│  │ • AuditLogger                                    │      │
│  │ • RateLimiter                                    │      │
│  └──────────────────┬───────────────────────────────┘      │
│                     │                                       │
│  ┌──────────────────▼───────────────────────────────┐      │
│  │         Database Layer (MySQL)                   │      │
│  ├──────────────────────────────────────────────────┤      │
│  │ Tables:                                          │      │
│  │ • USERS                                          │      │
│  │ • STUDENTS                                       │      │
│  │ • TEACHERS                                       │      │
│  │ • COURSES                                        │      │
│  │ • ENROLLMENTS                                    │      │
│  │ • ATTENDANCE_RECORDS                             │      │
│  │ • NOTIFICATIONS                                  │      │
│  │ • AUDIT_LOG                                      │      │
│  └──────────────────────────────────────────────────┘      │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## 🔄 Deployment Scenarios

### Scenario 1: Small School (1 Server, 5 Clients)

```
┌─────────────────────────────────────────────────────┐
│              School LAN (192.168.1.0/24)            │
├─────────────────────────────────────────────────────┤
│                                                     │
│  Server Room:                                       │
│  ┌──────────────┐                                   │
│  │  Server PC   │  192.168.1.100                    │
│  │  - RMI Server│                                   │
│  │  - MySQL DB  │                                   │
│  └──────────────┘                                   │
│                                                     │
│  Admin Office:                                      │
│  ┌──────────────┐                                   │
│  │  Client PC 1 │  192.168.1.101 (Admin)            │
│  └──────────────┘                                   │
│                                                     │
│  Teacher's Room:                                    │
│  ┌──────────────┐  ┌──────────────┐                │
│  │  Client PC 2 │  │  Client PC 3 │                │
│  │ 192.168.1.102│  │ 192.168.1.103│                │
│  │  (Teacher 1) │  │  (Teacher 2) │                │
│  └──────────────┘  └──────────────┘                │
│                                                     │
│  Student Lab:                                       │
│  ┌──────────────┐  ┌──────────────┐                │
│  │  Client PC 4 │  │  Client PC 5 │                │
│  │ 192.168.1.104│  │ 192.168.1.105│                │
│  │  (Student 1) │  │  (Student 2) │                │
│  └──────────────┘  └──────────────┘                │
│                                                     │
└─────────────────────────────────────────────────────┘
```

### Scenario 2: Large University (Multiple Servers, 100+ Clients)

```
┌─────────────────────────────────────────────────────┐
│           University Network                        │
├─────────────────────────────────────────────────────┤
│                                                     │
│  Data Center:                                       │
│  ┌──────────────┐  ┌──────────────┐                │
│  │ Load Balancer│  │  Backup      │                │
│  │ 10.0.0.10    │  │  Server      │                │
│  └──────┬───────┘  │  10.0.0.13   │                │
│         │          └──────────────┘                │
│    ┌────┴────┐                                      │
│    │         │                                      │
│  ┌─▼──────┐ ┌▼─────────┐                           │
│  │Server 1│ │ Server 2 │                           │
│  │10.0.0.11│ │10.0.0.12 │                           │
│  └────┬───┘ └───┬──────┘                           │
│       │         │                                   │
│  ┌────▼─────────▼────┐                             │
│  │  MySQL Cluster    │                             │
│  │  10.0.0.20-22     │                             │
│  └───────────────────┘                             │
│                                                     │
│  Department Buildings (100+ Clients):               │
│  • Engineering: 10.1.0.0/24 (30 clients)            │
│  • Science: 10.2.0.0/24 (25 clients)                │
│  • Arts: 10.3.0.0/24 (20 clients)                   │
│  • Admin: 10.4.0.0/24 (15 clients)                  │
│  • Library: 10.5.0.0/24 (10 clients)                │
│                                                     │
└─────────────────────────────────────────────────────┘
```

---

## 📊 Performance Characteristics

```
┌─────────────────────────────────────────────────────┐
│           Performance Metrics                       │
├─────────────────────────────────────────────────────┤
│                                                     │
│  Operation          │ Response Time │ Throughput   │
│  ───────────────────┼───────────────┼──────────────│
│  Login              │ < 100ms       │ 50 req/sec   │
│  Mark Attendance    │ < 50ms        │ 100 req/sec  │
│  View Records       │ < 100ms       │ 200 req/sec  │
│  Generate Report    │ < 500ms       │ 10 req/sec   │
│  User Management    │ < 200ms       │ 30 req/sec   │
│                                                     │
│  Concurrent Users:  200+                            │
│  Database Queries:  < 50ms (with indexes)           │
│  Network Latency:   < 10ms (LAN)                    │
│  Memory Usage:      2GB (server), 512MB (client)    │
│                                                     │
└─────────────────────────────────────────────────────┘
```

---

**This architecture supports:**
- ✅ Multiple concurrent clients
- ✅ Centralized data management
- ✅ Real-time synchronization
- ✅ Scalable deployment
- ✅ Secure communication
- ✅ High availability (with load balancing)
