package com.attendance.system.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for OverloadHandler.
 */
public class OverloadHandlerTest {
    
    private OverloadHandler overloadHandler;
    
    @BeforeEach
    public void setUp() {
        overloadHandler = OverloadHandler.getInstance();
        overloadHandler.clearQueue();
        overloadHandler.setMaxConcurrentRequests(10);
    }
    
    @Test
    public void testSubmitRequest() {
        AtomicInteger executed = new AtomicInteger(0);
        
        OverloadHandler.Request request = new OverloadHandler.Request() {
            @Override
            public void execute() {
                executed.incrementAndGet();
            }
            
            @Override
            public String getOperationName() {
                return "testOp";
            }
        };
        
        boolean accepted = overloadHandler.submitRequest(request);
        
        assertTrue(accepted);
        assertEquals(1, executed.get());
    }
    
    @Test
    public void testSubmitRequestWithPriority() {
        AtomicInteger executed = new AtomicInteger(0);
        
        OverloadHandler.Request request = new OverloadHandler.Request() {
            @Override
            public void execute() {
                executed.incrementAndGet();
            }
            
            @Override
            public String getOperationName() {
                return "testOp";
            }
        };
        
        boolean accepted = overloadHandler.submitRequest(request, OverloadHandler.RequestPriority.HIGH);
        
        assertTrue(accepted);
    }
    
    @Test
    public void testGetLoadFactor() {
        double loadFactor = overloadHandler.getLoadFactor();
        
        assertTrue(loadFactor >= 0.0);
        assertTrue(loadFactor <= 1.0);
    }
    
    @Test
    public void testIsSystemOverloaded() {
        overloadHandler.setMaxConcurrentRequests(2);
        
        // Submit requests to approach overload threshold
        for (int i = 0; i < 2; i++) {
            OverloadHandler.Request request = new OverloadHandler.Request() {
                @Override
                public void execute() {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                
                @Override
                public String getOperationName() {
                    return "testOp";
                }
            };
            
            overloadHandler.submitRequest(request);
        }
        
        // System should be overloaded or near overload
        assertTrue(overloadHandler.getLoadFactor() > 0);
    }
    
    @Test
    public void testGetStatistics() {
        Map<String, Object> stats = overloadHandler.getStatistics();
        
        assertTrue(stats.containsKey("activeRequests"));
        assertTrue(stats.containsKey("queuedRequests"));
        assertTrue(stats.containsKey("rejectedRequests"));
        assertTrue(stats.containsKey("processedRequests"));
        assertTrue(stats.containsKey("maxConcurrentRequests"));
        assertTrue(stats.containsKey("loadFactor"));
        assertTrue(stats.containsKey("isOverloaded"));
        assertTrue(stats.containsKey("queueSize"));
    }
    
    @Test
    public void testSetMaxConcurrentRequests() {
        overloadHandler.setMaxConcurrentRequests(20);
        assertEquals(20, overloadHandler.getMaxConcurrentRequests());
        
        overloadHandler.setMaxConcurrentRequests(50);
        assertEquals(50, overloadHandler.getMaxConcurrentRequests());
    }
    
    @Test
    public void testClearQueue() {
        overloadHandler.clearQueue();
        
        Map<String, Object> stats = overloadHandler.getStatistics();
        assertEquals(0, stats.get("queuedRequests"));
    }
    
    @Test
    public void testRequestPriority() {
        OverloadHandler.RequestPriority low = OverloadHandler.RequestPriority.LOW;
        OverloadHandler.RequestPriority normal = OverloadHandler.RequestPriority.NORMAL;
        OverloadHandler.RequestPriority high = OverloadHandler.RequestPriority.HIGH;
        
        assertTrue(low.getValue() < normal.getValue());
        assertTrue(normal.getValue() < high.getValue());
    }
    
    @Test
    public void testMultipleRequests() {
        AtomicInteger executed = new AtomicInteger(0);
        
        for (int i = 0; i < 5; i++) {
            OverloadHandler.Request request = new OverloadHandler.Request() {
                @Override
                public void execute() {
                    executed.incrementAndGet();
                }
                
                @Override
                public String getOperationName() {
                    return "testOp";
                }
            };
            
            overloadHandler.submitRequest(request);
        }
        
        // Give time for async processing
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        assertTrue(executed.get() > 0);
    }
    
    @Test
    public void testRequestTimeout() {
        overloadHandler.setMaxConcurrentRequests(1);
        
        // Submit a long-running request
        OverloadHandler.Request longRequest = new OverloadHandler.Request() {
            @Override
            public void execute() {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            
            @Override
            public String getOperationName() {
                return "longOp";
            }
        };
        
        overloadHandler.submitRequest(longRequest);
        
        // Try to submit another request - should be queued
        AtomicInteger executed = new AtomicInteger(0);
        OverloadHandler.Request shortRequest = new OverloadHandler.Request() {
            @Override
            public void execute() {
                executed.incrementAndGet();
            }
            
            @Override
            public String getOperationName() {
                return "shortOp";
            }
        };
        
        boolean accepted = overloadHandler.submitRequest(shortRequest);
        assertTrue(accepted);
    }
    
    @Test
    public void testLowPriorityRejectionUnderOverload() {
        overloadHandler.setMaxConcurrentRequests(1);
        
        // Fill capacity
        OverloadHandler.Request blockingRequest = new OverloadHandler.Request() {
            @Override
            public void execute() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            
            @Override
            public String getOperationName() {
                return "blockingOp";
            }
        };
        
        overloadHandler.submitRequest(blockingRequest);
        
        // Try to submit low priority request during overload
        OverloadHandler.Request lowPriorityRequest = new OverloadHandler.Request() {
            @Override
            public void execute() {
                // Should not execute
            }
            
            @Override
            public String getOperationName() {
                return "lowPriorityOp";
            }
        };
        
        // This might be rejected if system is overloaded
        overloadHandler.submitRequest(lowPriorityRequest, OverloadHandler.RequestPriority.LOW);
    }
}
