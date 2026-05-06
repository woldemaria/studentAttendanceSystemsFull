package com.attendance.system.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.RMISocketFactory;

/**
 * Custom RMI socket factory that uses SSL/TLS for encryption.
 * Implements both RMISocketFactory and RMIServerSocketFactory for complete SSL/TLS support.
 */
public class RMISSLSocketFactory extends RMISocketFactory implements RMIServerSocketFactory {
    private static final Logger logger = LoggerFactory.getLogger(RMISSLSocketFactory.class);
    
    private final SSLSocketFactory clientSocketFactory;
    private final SSLServerSocketFactory serverSocketFactory;
    
    /**
     * Creates an RMI SSL socket factory.
     * @param clientSocketFactory the SSL socket factory for client connections
     * @param serverSocketFactory the SSL server socket factory for server connections
     */
    public RMISSLSocketFactory(SSLSocketFactory clientSocketFactory, 
                               SSLServerSocketFactory serverSocketFactory) {
        this.clientSocketFactory = clientSocketFactory;
        this.serverSocketFactory = serverSocketFactory;
    }
    
    /**
     * Creates a client socket with SSL/TLS encryption.
     * @param host the host to connect to
     * @param port the port to connect to
     * @return an SSL socket
     * @throws IOException if socket creation fails
     */
    @Override
    public Socket createSocket(String host, int port) throws IOException {
        try {
            Socket socket = clientSocketFactory.createSocket(host, port);
            logger.debug("Created SSL client socket to " + host + ":" + port);
            return socket;
        } catch (IOException e) {
            logger.error("Failed to create SSL client socket to " + host + ":" + port, e);
            throw e;
        }
    }
    
    /**
     * Creates a server socket with SSL/TLS encryption.
     * @param port the port to listen on
     * @return an SSL server socket
     * @throws IOException if socket creation fails
     */
    @Override
    public ServerSocket createServerSocket(int port) throws IOException {
        try {
            ServerSocket serverSocket = serverSocketFactory.createServerSocket(port);
            logger.debug("Created SSL server socket on port " + port);
            return serverSocket;
        } catch (IOException e) {
            logger.error("Failed to create SSL server socket on port " + port, e);
            throw e;
        }
    }
    
    /**
     * Gets the hash code for this factory.
     * @return hash code
     */
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
    
    /**
     * Checks equality with another object.
     * @param obj the object to compare
     * @return true if equal
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof RMISSLSocketFactory;
    }
}
