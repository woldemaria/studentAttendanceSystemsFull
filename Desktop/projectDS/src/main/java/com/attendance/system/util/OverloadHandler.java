package com.attendance.system.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;

/**
 * Handles system overload conditions gracefully by queuing requests and managing load.
 * Implements request queuing, priority handling, and automatic recovery mechanisms.
 */
public class OverloadHandler {
    private static final Logger logger = LoggerFactory.getLogger(OverloadHandler.class);
    
    private static OverloadHandler instance;
    
    // Request queue with priority support
    private final PriorityQueue<QueuedRequest> requestQueue;
    private final BlockingQueue<QueuedRequest> processingQueue;
    
    // Configuration
    private static final int MAX_QUEUE_SIZE = 500;
    private static final int PROCESSING_THREADS = 5;
    private static final long REQUEST_TIMEOUT_MS = 30000; // 30 seconds
    private static final double OVERLOAD_THRESHOLD = 0.8; // 80% capacity
    private static final double RECOVERY_THRESHOLD = 0.5; // 50% capacity
    
    // State tracking
    private volatile boolean isOverloaded = false;
    private final AtomicInteger activeRequests = new AtomicInteger(0);
    private final AtomicInteger queuedRequests = new AtomicInteger(0);
    private final AtomicLong rejectedRequests = new AtomicLong(0);
    private final AtomicLong processedRequests = new AtomicLong(0);
    
    // Maximum concurrent requests
    private int maxConcurrentRequests = 100;
    
    private OverloadHandler() {
        this.requestQueue = new PriorityQueue<>(Comparator.comparingInt(QueuedRequest::getPriority).reversed());
        this.processingQueue = new LinkedBlockingQueue<>(MAX_QUEUE_SIZE);
        
        startRequestProcessors();
    }
    
    public static synchronized OverloadHandler getInstance() {
        if (instance == null) {
            instance = new OverloadHandler();
        }
        return instance;
    }
    
    /**
     * Submits a request for processing.
     * @param request the request to process
     * @return true if request was accepted, false if rejected due to overload
     */
    public boolean submitRequest(Request request) {
        return submitRequest(request, RequestPriority.NORMAL);
    }
    
    /**
     * Submits a request with priority.
     * @param request the request to process
     * @param priority request priority
     * @return true if request was accepted, false if rejected due to overload
     */
    public boolean submitRequest(Request request, RequestPriority priority) {
        // Check if system is overloaded
        if (isSystemOverloaded()) {
            // Reject low priority requests during overload
            if (priority == RequestPriority.LOW) {
                rejectedRequests.incrementAndGet();
                logger.warn("Request rejected due to system overload: {}", request.getOperationName());
                return false;
            }
            
            // Queue high priority requests
            if (priority == RequestPriority.HIGH) {
                try {
                    QueuedRequest queuedRequest = new QueuedRequest(request, priority);
                    processingQueue.offer(queuedRequest, REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS);
                    queuedRequests.incrementAndGet();
                    logger.debug("High priority request queued: {}", request.getOperationName());
                    return true;
                } catch (InterruptedException e) {
                    rejectedRequests.incrementAndGet();
                    logger.warn("Failed to queue high priority request: {}", request.getOperationName());
                    return false;
                }
            }
        }
        
        // Process request immediately if capacity available
        if (activeRequests.get() < maxConcurrentRequests) {
            activeRequests.incrementAndGet();
            processedRequests.incrementAndGet();
            
            try {
                request.execute();
            } catch (Exception e) {
                logger.error("Error executing request: {}", request.getOperationName(), e);
                // Exception is caught and logged, no need to rethrow
            } finally {
                activeRequests.decrementAndGet();
                checkRecovery();
            }
            
            return true;
        }
        
        // Queue request if capacity not available
        try {
            QueuedRequest queuedRequest = new QueuedRequest(request, priority);
            processingQueue.offer(queuedRequest, REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            queuedRequests.incrementAndGet();
            logger.debug("Request queued: {}", request.getOperationName());
            return true;
        } catch (InterruptedException e) {
            rejectedRequests.incrementAndGet();
            logger.warn("Failed to queue request: {}", request.getOperationName());
            return false;
        }
    }
    
    /**
     * Checks if the system is currently overloaded.
     * @return true if system is overloaded
     */
    public boolean isSystemOverloaded() {
        double loadFactor = (double) activeRequests.get() / maxConcurrentRequests;
        
        if (loadFactor >= OVERLOAD_THRESHOLD && !isOverloaded) {
            isOverloaded = true;
            logger.warn("System overload detected. Load factor: {}", String.format("%.2f", loadFactor));
        }
        
        return isOverloaded;
    }
    
    /**
     * Checks if system has recovered from overload.
     */
    private void checkRecovery() {
        if (isOverloaded) {
            double loadFactor = (double) activeRequests.get() / maxConcurrentRequests;
            
            if (loadFactor <= RECOVERY_THRESHOLD) {
                isOverloaded = false;
                logger.info("System recovered from overload. Load factor: {}", String.format("%.2f", loadFactor));
            }
        }
    }
    
    /**
     * Gets the current system load factor (0.0 to 1.0).
     * @return load factor
     */
    public double getLoadFactor() {
        return (double) activeRequests.get() / maxConcurrentRequests;
    }
    
    /**
     * Gets overload handler statistics.
     * @return map containing statistics
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("activeRequests", activeRequests.get());
        stats.put("queuedRequests", queuedRequests.get());
        stats.put("rejectedRequests", rejectedRequests.get());
        stats.put("processedRequests", processedRequests.get());
        stats.put("maxConcurrentRequests", maxConcurrentRequests);
        stats.put("loadFactor", String.format("%.2f", getLoadFactor()));
        stats.put("isOverloaded", isOverloaded);
        stats.put("queueSize", processingQueue.size());
        
        return stats;
    }
    
    /**
     * Sets the maximum concurrent requests.
     * @param maxRequests maximum concurrent requests
     */
    public void setMaxConcurrentRequests(int maxRequests) {
        this.maxConcurrentRequests = maxRequests;
        logger.info("Max concurrent requests updated to: {}", maxRequests);
    }
    
    /**
     * Gets the maximum concurrent requests.
     * @return maximum concurrent requests
     */
    public int getMaxConcurrentRequests() {
        return maxConcurrentRequests;
    }
    
    /**
     * Clears all queued requests.
     */
    public void clearQueue() {
        processingQueue.clear();
        queuedRequests.set(0);
        logger.info("Request queue cleared");
    }
    
    /**
     * Starts request processor threads.
     */
    private void startRequestProcessors() {
        for (int i = 0; i < PROCESSING_THREADS; i++) {
            Thread processorThread = new Thread(() -> {
                while (true) {
                    try {
                        QueuedRequest queuedRequest = processingQueue.poll(1, TimeUnit.SECONDS);
                        
                        if (queuedRequest != null) {
                            // Check if request has timed out
                            if (queuedRequest.hasTimedOut()) {
                                logger.warn("Queued request timed out: {}", queuedRequest.getRequest().getOperationName());
                                queuedRequests.decrementAndGet();
                                continue;
                            }
                            
                            // Process request
                            activeRequests.incrementAndGet();
                            try {
                                queuedRequest.getRequest().execute();
                                processedRequests.incrementAndGet();
                            } finally {
                                activeRequests.decrementAndGet();
                                queuedRequests.decrementAndGet();
                                checkRecovery();
                            }
                        }
                        
                    } catch (InterruptedException e) {
                        logger.debug("Request processor thread interrupted");
                        break;
                    } catch (Exception e) {
                        logger.error("Error processing queued request", e);
                    }
                }
            });
            
            processorThread.setDaemon(true);
            processorThread.setName("RequestProcessor-" + i);
            processorThread.start();
        }
    }
    
    /**
     * Functional interface for requests.
     */
    public interface Request {
        void execute() throws Exception;
        String getOperationName();
    }
    
    /**
     * Request priority levels.
     */
    public enum RequestPriority {
        LOW(1),
        NORMAL(2),
        HIGH(3);
        
        private final int value;
        
        RequestPriority(int value) {
            this.value = value;
        }
        
        public int getValue() {
            return value;
        }
    }
    
    /**
     * Inner class for queued requests.
     */
    private static class QueuedRequest {
        private final Request request;
        private final RequestPriority priority;
        private final long queuedAt;
        
        public QueuedRequest(Request request, RequestPriority priority) {
            this.request = request;
            this.priority = priority;
            this.queuedAt = System.currentTimeMillis();
        }
        
        public Request getRequest() {
            return request;
        }
        
        public int getPriority() {
            return priority.getValue();
        }
        
        public boolean hasTimedOut() {
            return System.currentTimeMillis() - queuedAt > REQUEST_TIMEOUT_MS;
        }
    }
    
    /**
     * Atomic wrapper for integer values.
     */
    private static class AtomicInteger {
        private int value = 0;
        
        public AtomicInteger() {
            this.value = 0;
        }
        
        public AtomicInteger(int initialValue) {
            this.value = initialValue;
        }
        
        public synchronized void incrementAndGet() {
            value++;
        }
        
        public synchronized void decrementAndGet() {
            value--;
        }
        
        public synchronized int get() {
            return value;
        }
        
        public synchronized void set(int newValue) {
            value = newValue;
        }
    }
    
    /**
     * Atomic wrapper for long values.
     */
    private static class AtomicLong {
        private long value = 0;
        
        public AtomicLong() {
            this.value = 0;
        }
        
        public AtomicLong(int initialValue) {
            this.value = initialValue;
        }
        
        public synchronized void incrementAndGet() {
            value++;
        }
        
        public synchronized long get() {
            return value;
        }
        
        public synchronized void set(long newValue) {
            value = newValue;
        }
    }
}
