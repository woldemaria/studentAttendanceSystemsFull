package com.attendance.system.server;

import com.attendance.system.dao.DatabaseManager;
import com.attendance.system.util.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Server startup utility for the Student Attendance System.
 * Handles RMI registry setup, server binding, and graceful shutdown.
 */
public class ServerLauncher {
    private static final Logger logger = LoggerFactory.getLogger(ServerLauncher.class);
    
    // Configuration constants
    private static final String DEFAULT_SERVICE_NAME = "AttendanceService";
    private static final int DEFAULT_RMI_PORT = 1099;
    private static final int DEFAULT_SERVER_PORT = 0; // Use anonymous port
    
    // Server components
    private Registry rmiRegistry;
    private AttendanceServer attendanceServer;
    private ScheduledExecutorService scheduledExecutor;
    private final CountDownLatch shutdownLatch = new CountDownLatch(1);
    
    // Configuration
    private final String serviceName;
    private final int rmiPort;
    private final int serverPort;
    private final boolean createRegistry;
    
    /**
     * Creates a new ServerLauncher with default configuration.
     */
    public ServerLauncher() {
        this.serviceName = ConfigManager.getString("rmi.service.name", DEFAULT_SERVICE_NAME);
        this.rmiPort = ConfigManager.getInt("rmi.registry.port", DEFAULT_RMI_PORT);
        this.serverPort = ConfigManager.getInt("rmi.server.port", DEFAULT_SERVER_PORT);
        this.createRegistry = ConfigManager.getBoolean("rmi.create.registry", true);
    }
    
    /**
     * Creates a new ServerLauncher with custom configuration.
     */
    public ServerLauncher(String serviceName, int rmiPort, int serverPort, boolean createRegistry) {
        this.serviceName = serviceName;
        this.rmiPort = rmiPort;
        this.serverPort = serverPort;
        this.createRegistry = createRegistry;
    }
    
    /**
     * Starts the RMI server.
     * @throws Exception if server startup fails
     */
    public void start() throws Exception {
        logger.info("Starting Student Attendance System Server...");
        
        try {
            // Initialize database
            initializeDatabase();
            
            // Set up RMI system properties
            setupRMIProperties();
            
            // Create or locate RMI registry
            setupRMIRegistry();
            
            // Create and export the server
            createAndExportServer();
            
            // Bind server to registry
            bindServerToRegistry();
            
            // Start monitoring and maintenance tasks
            startMaintenanceTasks();
            
            // Add shutdown hook
            addShutdownHook();
            
            logger.info("Server started successfully!");
            logger.info("Service name: {}", serviceName);
            logger.info("RMI registry port: {}", rmiPort);
            logger.info("Server listening on port: {}", getServerPort());
            logger.info("Server is ready to accept client connections.");
            
        } catch (Exception e) {
            logger.error("Failed to start server", e);
            cleanup();
            throw e;
        }
    }
    
    /**
     * Stops the server gracefully.
     */
    public void stop() {
        logger.info("Stopping Student Attendance System Server...");
        
        try {
            // Unbind from registry
            if (rmiRegistry != null) {
                try {
                    rmiRegistry.unbind(serviceName);
                    logger.info("Service unbound from registry");
                } catch (Exception e) {
                    logger.warn("Failed to unbind service from registry", e);
                }
            }
            
            // Cleanup resources
            cleanup();
            
            logger.info("Server stopped successfully");
            
        } catch (Exception e) {
            logger.error("Error during server shutdown", e);
        } finally {
            shutdownLatch.countDown();
        }
    }
    
    /**
     * Waits for the server to be shut down.
     * @throws InterruptedException if interrupted while waiting
     */
    public void waitForShutdown() throws InterruptedException {
        shutdownLatch.await();
    }
    
    /**
     * Gets the actual port the server is listening on.
     * @return server port number
     */
    public int getServerPort() {
        if (attendanceServer != null) {
            try {
                // Get the port from the exported object
                return UnicastRemoteObject.getPort(attendanceServer);
            } catch (Exception e) {
                logger.debug("Could not determine server port", e);
            }
        }
        return serverPort;
    }
    
    /**
     * Checks if the server is running.
     * @return true if server is running
     */
    public boolean isRunning() {
        return attendanceServer != null && rmiRegistry != null;
    }
    
    // Private helper methods
    
    private void initializeDatabase() throws Exception {
        logger.info("Initializing database connection...");
        
        try {
            DatabaseManager.getInstance().initializeConnectionPool();
            DatabaseManager.getInstance().testConnection();
            logger.info("Database connection established successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize database", e);
            throw new Exception("Database initialization failed", e);
        }
    }
    
    private void setupRMIProperties() {
        logger.debug("Setting up RMI system properties...");
        
        // Set RMI server hostname if specified
        String rmiHostname = ConfigManager.getString("rmi.server.hostname", null);
        if (rmiHostname != null && !rmiHostname.isEmpty()) {
            System.setProperty("java.rmi.server.hostname", rmiHostname);
            logger.info("RMI server hostname set to: {}", rmiHostname);
        }
        
        // Set RMI server port if specified
        if (serverPort > 0) {
            System.setProperty("java.rmi.server.port", String.valueOf(serverPort));
        }
        
        // Enable RMI logging if configured
        boolean enableRMILogging = ConfigManager.getBoolean("rmi.logging.enabled", false);
        if (enableRMILogging) {
            System.setProperty("java.rmi.server.logCalls", "true");
            System.setProperty("sun.rmi.server.logLevel", "VERBOSE");
        }
        
        // Set security manager if required
        boolean useSecurityManager = ConfigManager.getBoolean("rmi.security.manager.enabled", false);
        if (useSecurityManager && System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
            logger.info("RMI security manager enabled");
        }
    }
    
    private void setupRMIRegistry() throws RemoteException {
        logger.info("Setting up RMI registry on port {}...", rmiPort);
        
        try {
            if (createRegistry) {
                // Create a new registry
                rmiRegistry = LocateRegistry.createRegistry(rmiPort);
                logger.info("Created new RMI registry on port {}", rmiPort);
            } else {
                // Locate existing registry
                rmiRegistry = LocateRegistry.getRegistry(rmiPort);
                logger.info("Located existing RMI registry on port {}", rmiPort);
            }
            
            // Test registry connectivity
            rmiRegistry.list();
            
        } catch (RemoteException e) {
            if (createRegistry) {
                logger.error("Failed to create RMI registry on port {}", rmiPort, e);
            } else {
                logger.error("Failed to locate RMI registry on port {}", rmiPort, e);
            }
            throw e;
        }
    }
    
    private void createAndExportServer() throws RemoteException {
        logger.info("Creating and exporting attendance server...");
        
        try {
            // Create server instance
            if (serverPort > 0) {
                attendanceServer = new AttendanceServer();
                // Export with specific port
                UnicastRemoteObject.exportObject(attendanceServer, serverPort);
            } else {
                // Use anonymous port (handled by UnicastRemoteObject constructor)
                attendanceServer = new AttendanceServer();
            }
            
            logger.info("Attendance server created and exported successfully");
            
        } catch (RemoteException e) {
            logger.error("Failed to create and export server", e);
            throw e;
        }
    }
    
    private void bindServerToRegistry() throws RemoteException, AlreadyBoundException {
        logger.info("Binding server to registry with name '{}'...", serviceName);
        
        try {
            // Check if service is already bound
            try {
                rmiRegistry.lookup(serviceName);
                logger.warn("Service '{}' is already bound. Rebinding...", serviceName);
                rmiRegistry.rebind(serviceName, attendanceServer);
            } catch (java.rmi.NotBoundException e) {
                // Service not bound, bind it
                rmiRegistry.bind(serviceName, attendanceServer);
            }
            
            logger.info("Server bound to registry successfully");
            
        } catch (RemoteException | AlreadyBoundException e) {
            logger.error("Failed to bind server to registry", e);
            throw e;
        }
    }
    
    private void startMaintenanceTasks() {
        logger.info("Starting maintenance tasks...");
        
        scheduledExecutor = Executors.newScheduledThreadPool(2);
        
        // Health check task
        scheduledExecutor.scheduleAtFixedRate(() -> {
            try {
                if (attendanceServer != null && !attendanceServer.isHealthy()) {
                    logger.warn("Server health check failed");
                }
            } catch (Exception e) {
                logger.error("Error during health check", e);
            }
        }, 1, 5, TimeUnit.MINUTES);
        
        // Connection monitoring task
        scheduledExecutor.scheduleAtFixedRate(() -> {
            try {
                if (attendanceServer != null) {
                    var serverInfo = attendanceServer.getServerInfo();
                    int activeConnections = (Integer) serverInfo.get("activeConnections");
                    int totalRequests = (Integer) serverInfo.get("totalRequests");
                    
                    logger.debug("Server stats - Active connections: {}, Total requests: {}", 
                            activeConnections, totalRequests);
                }
            } catch (Exception e) {
                logger.debug("Error getting server stats", e);
            }
        }, 1, 1, TimeUnit.MINUTES);
        
        logger.info("Maintenance tasks started");
    }
    
    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutdown hook triggered");
            stop();
        }));
    }
    
    private void cleanup() {
        // Stop maintenance tasks
        if (scheduledExecutor != null && !scheduledExecutor.isShutdown()) {
            scheduledExecutor.shutdown();
            try {
                if (!scheduledExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                    scheduledExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduledExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        // Unexport server
        if (attendanceServer != null) {
            try {
                UnicastRemoteObject.unexportObject(attendanceServer, true);
                logger.info("Server unexported");
            } catch (Exception e) {
                logger.warn("Failed to unexport server", e);
            }
            attendanceServer = null;
        }
        
        // Close database connections
        try {
            DatabaseManager.getInstance().closeConnectionPool();
            logger.info("Database connections closed");
        } catch (Exception e) {
            logger.warn("Failed to close database connections", e);
        }
    }
    
    /**
     * Main method for standalone server execution.
     */
    public static void main(String[] args) {
        try {
            // Parse command line arguments
            ServerConfig config = parseArguments(args);
            
            // Create and start server
            ServerLauncher launcher = new ServerLauncher(
                    config.serviceName,
                    config.rmiPort,
                    config.serverPort,
                    config.createRegistry
            );
            
            launcher.start();
            
            // Wait for shutdown
            launcher.waitForShutdown();
            
        } catch (Exception e) {
            logger.error("Server startup failed", e);
            System.exit(1);
        }
    }
    
    private static ServerConfig parseArguments(String[] args) {
        ServerConfig config = new ServerConfig();
        
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--service-name":
                case "-n":
                    if (i + 1 < args.length) {
                        config.serviceName = args[++i];
                    }
                    break;
                case "--rmi-port":
                case "-r":
                    if (i + 1 < args.length) {
                        config.rmiPort = Integer.parseInt(args[++i]);
                    }
                    break;
                case "--server-port":
                case "-s":
                    if (i + 1 < args.length) {
                        config.serverPort = Integer.parseInt(args[++i]);
                    }
                    break;
                case "--no-create-registry":
                    config.createRegistry = false;
                    break;
                case "--help":
                case "-h":
                    printUsage();
                    System.exit(0);
                    break;
                default:
                    logger.warn("Unknown argument: {}", args[i]);
                    break;
            }
        }
        
        return config;
    }
    
    private static void printUsage() {
        System.out.println("Student Attendance System Server");
        System.out.println("Usage: java ServerLauncher [options]");
        System.out.println();
        System.out.println("Options:");
        System.out.println("  -n, --service-name <name>    RMI service name (default: AttendanceService)");
        System.out.println("  -r, --rmi-port <port>        RMI registry port (default: 1099)");
        System.out.println("  -s, --server-port <port>     Server port (default: anonymous)");
        System.out.println("  --no-create-registry         Don't create registry, use existing one");
        System.out.println("  -h, --help                   Show this help message");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  java ServerLauncher");
        System.out.println("  java ServerLauncher -n MyAttendanceService -r 2099");
        System.out.println("  java ServerLauncher -s 8080 --no-create-registry");
    }
    
    /**
     * Configuration class for server parameters.
     */
    private static class ServerConfig {
        String serviceName = DEFAULT_SERVICE_NAME;
        int rmiPort = DEFAULT_RMI_PORT;
        int serverPort = DEFAULT_SERVER_PORT;
        boolean createRegistry = true;
    }
}