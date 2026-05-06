# Student Attendance System - RMI Server

This document describes the RMI server components of the Student Attendance System.

## Server Components

### AttendanceServer
The main RMI server implementation that provides remote access to attendance management functionality.

**Features:**
- Thread-safe session management for concurrent clients
- Comprehensive error handling and logging
- Role-based access control enforcement
- Connection monitoring and statistics
- Data encryption support
- Server capacity management

**Key Methods:**
- Authentication: `authenticateUser()`, `validateSession()`, `logout()`
- User Management: `createUser()`, `updateUser()`, `deleteUser()`, `getAllUsers()`
- Course Management: `createCourse()`, `updateCourse()`, `getAllActiveCourses()`
- Attendance Operations: `markAttendance()`, `updateAttendance()`, `getAttendanceRecords()`
- System Monitoring: `isHealthy()`, `getServerInfo()`, `getSystemStatistics()`

### ServerLauncher
Utility class for starting and managing the RMI server.

**Features:**
- RMI registry setup and management
- Server binding and unbinding
- Graceful shutdown procedures
- Configuration management
- Maintenance task scheduling
- Command-line argument parsing

## Configuration

Server configuration is managed through `server.properties`:

```properties
# RMI Configuration
rmi.service.name=AttendanceService
rmi.registry.port=1099
rmi.server.port=0
rmi.create.registry=true
rmi.server.hostname=localhost

# Security Configuration
server.encryption.enabled=true
server.max.concurrent.users=100
```

## Starting the Server

### Using Maven (if available)
```bash
mvn exec:java -Dexec.mainClass="com.attendance.system.server.ServerLauncher"
```

### Using Java directly
```bash
java -cp target/classes:target/dependency/* com.attendance.system.server.ServerLauncher
```

### Command Line Options
```bash
java ServerLauncher [options]

Options:
  -n, --service-name <name>    RMI service name (default: AttendanceService)
  -r, --rmi-port <port>        RMI registry port (default: 1099)
  -s, --server-port <port>     Server port (default: anonymous)
  --no-create-registry         Don't create registry, use existing one
  -h, --help                   Show help message
```

### Examples
```bash
# Start with default settings
java ServerLauncher

# Start with custom service name and port
java ServerLauncher -n MyAttendanceService -r 2099

# Start with specific server port
java ServerLauncher -s 8080 --no-create-registry
```

## Server Architecture

### Session Management
- Thread-safe concurrent session handling
- 30-minute session timeout (configurable)
- Automatic session cleanup
- Force logout capabilities for administrators

### Security Features
- Optional data encryption for RMI communications
- Role-based access control enforcement
- Account lockout after failed login attempts
- Comprehensive audit logging

### Error Handling
- Comprehensive exception handling with proper error codes
- Graceful degradation under load
- Connection monitoring and recovery
- Detailed logging for troubleshooting

### Performance Features
- Connection pooling for database access
- Configurable server capacity limits
- Health monitoring and statistics
- Maintenance task scheduling

## Monitoring and Statistics

The server provides comprehensive monitoring through:

- **Health Checks**: Database connectivity and server load monitoring
- **Connection Statistics**: Active connections, total requests, uptime
- **System Statistics**: User counts, course counts, attendance records
- **Performance Metrics**: Request processing times, error rates

## Database Integration

The server integrates with the existing database layer:

- **UserDAO**: User management and authentication
- **CourseDAO**: Course and enrollment management  
- **AttendanceDAO**: Attendance record operations
- **DatabaseManager**: Connection pooling and health monitoring

## Client Integration

Clients connect to the server using standard RMI:

```java
// Client connection example
Registry registry = LocateRegistry.getRegistry("localhost", 1099);
AttendanceService service = (AttendanceService) registry.lookup("AttendanceService");

// Authenticate user
AuthenticatedUser auth = service.authenticateUser("username", "password");
String sessionToken = auth.getSessionToken();

// Use service methods
List<Course> courses = service.getAllActiveCourses(sessionToken);
```

## Troubleshooting

### Common Issues

1. **Port Already in Use**
   - Change RMI registry port: `-r <port>`
   - Use existing registry: `--no-create-registry`

2. **Database Connection Errors**
   - Check `database.properties` configuration
   - Verify MySQL server is running
   - Check network connectivity

3. **RMI Registry Issues**
   - Ensure no firewall blocking ports
   - Check hostname resolution
   - Verify RMI security settings

### Logging

Server logs are configured through Logback. Key log categories:

- `com.attendance.system.server`: Server operations
- `com.attendance.system.dao`: Database operations
- `com.attendance.system.service`: Business logic
- `ROOT`: General application logs

## Security Considerations

1. **Network Security**
   - Use firewalls to restrict RMI port access
   - Consider VPN for remote access
   - Enable RMI SSL if needed

2. **Data Security**
   - Enable encryption in configuration
   - Use strong database passwords
   - Regular security updates

3. **Access Control**
   - Implement proper user role assignments
   - Regular audit of user accounts
   - Monitor failed login attempts

## Production Deployment

For production deployment:

1. **Configuration**
   - Set appropriate server capacity limits
   - Configure proper logging levels
   - Enable security features

2. **Monitoring**
   - Set up health check monitoring
   - Configure alerting for critical issues
   - Regular backup procedures

3. **Maintenance**
   - Schedule regular maintenance windows
   - Plan for graceful shutdowns
   - Monitor system performance