package com.attendance.system.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.RMISocketFactory;

/**
 * Configuration utility for SSL/TLS encryption in RMI communications.
 * Provides methods to configure secure RMI connections using SSL/TLS.
 */
public class RMISSLConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(RMISSLConfiguration.class);
    
    private static final String SSL_PROTOCOL = "TLSv1.2";
    private static final String KEYSTORE_PATH_ENV = "ATTENDANCE_KEYSTORE_PATH";
    private static final String KEYSTORE_PASSWORD_ENV = "ATTENDANCE_KEYSTORE_PASSWORD";
    private static final String TRUSTSTORE_PATH_ENV = "ATTENDANCE_TRUSTSTORE_PATH";
    private static final String TRUSTSTORE_PASSWORD_ENV = "ATTENDANCE_TRUSTSTORE_PASSWORD";
    
    /**
     * Configures SSL/TLS for RMI communications.
     * Sets up system properties for keystore and truststore.
     * @return true if configuration was successful
     */
    public static boolean configureSSL() {
        try {
            // Load keystore configuration
            String keystorePath = System.getenv(KEYSTORE_PATH_ENV);
            String keystorePassword = System.getenv(KEYSTORE_PASSWORD_ENV);
            
            if (keystorePath != null && !keystorePath.isEmpty()) {
                System.setProperty("javax.net.ssl.keyStore", keystorePath);
                if (keystorePassword != null && !keystorePassword.isEmpty()) {
                    System.setProperty("javax.net.ssl.keyStorePassword", keystorePassword);
                }
                logger.info("Keystore configured: " + keystorePath);
            } else {
                logger.warn("Keystore path not configured. RMI SSL may not work properly.");
            }
            
            // Load truststore configuration
            String truststorePath = System.getenv(TRUSTSTORE_PATH_ENV);
            String truststorePassword = System.getenv(TRUSTSTORE_PASSWORD_ENV);
            
            if (truststorePath != null && !truststorePath.isEmpty()) {
                System.setProperty("javax.net.ssl.trustStore", truststorePath);
                if (truststorePassword != null && !truststorePassword.isEmpty()) {
                    System.setProperty("javax.net.ssl.trustStorePassword", truststorePassword);
                }
                logger.info("Truststore configured: " + truststorePath);
            } else {
                logger.warn("Truststore path not configured. RMI SSL may not work properly.");
            }
            
            // Set SSL protocol version
            System.setProperty("javax.net.ssl.protocols", SSL_PROTOCOL);
            logger.info("SSL protocol configured: " + SSL_PROTOCOL);
            
            return true;
        } catch (Exception e) {
            logger.error("Failed to configure SSL/TLS for RMI", e);
            return false;
        }
    }
    
    /**
     * Creates an SSL socket factory for RMI client connections.
     * @return SSLSocketFactory for RMI clients
     */
    public static SSLSocketFactory createSSLSocketFactory() {
        try {
            SSLContext sslContext = SSLContext.getInstance(SSL_PROTOCOL);
            sslContext.init(null, null, null);
            return sslContext.getSocketFactory();
        } catch (Exception e) {
            logger.error("Failed to create SSL socket factory", e);
            return null;
        }
    }
    
    /**
     * Creates an SSL server socket factory for RMI server connections.
     * @return SSLServerSocketFactory for RMI server
     */
    public static SSLServerSocketFactory createSSLServerSocketFactory() {
        try {
            SSLContext sslContext = SSLContext.getInstance(SSL_PROTOCOL);
            sslContext.init(null, null, null);
            return sslContext.getServerSocketFactory();
        } catch (Exception e) {
            logger.error("Failed to create SSL server socket factory", e);
            return null;
        }
    }
    
    /**
     * Configures RMI socket factories for SSL/TLS encryption.
     * Should be called before binding RMI objects.
     * @return true if configuration was successful
     */
    public static boolean configureRMISocketFactories() {
        try {
            SSLSocketFactory clientSocketFactory = createSSLSocketFactory();
            SSLServerSocketFactory serverSocketFactory = createSSLServerSocketFactory();
            
            if (clientSocketFactory == null || serverSocketFactory == null) {
                logger.error("Failed to create SSL socket factories");
                return false;
            }
            
            // Create custom RMI socket factory
            RMISSLSocketFactory rmiSSLSocketFactory = new RMISSLSocketFactory(
                clientSocketFactory, serverSocketFactory
            );
            
            // Set the RMI socket factory
            RMISocketFactory.setSocketFactory(rmiSSLSocketFactory);
            logger.info("RMI socket factories configured for SSL/TLS");
            
            return true;
        } catch (IOException e) {
            logger.error("Failed to configure RMI socket factories", e);
            return false;
        }
    }
    
    /**
     * Enables SSL/TLS debugging for troubleshooting.
     * Should only be used in development environments.
     */
    public static void enableSSLDebug() {
        System.setProperty("javax.net.debug", "ssl:handshake");
        logger.info("SSL/TLS debugging enabled");
    }
    
    /**
     * Disables SSL/TLS debugging.
     */
    public static void disableSSLDebug() {
        System.clearProperty("javax.net.debug");
        logger.info("SSL/TLS debugging disabled");
    }
}
