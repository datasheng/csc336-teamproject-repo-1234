package com.campusevents.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for PubSubService.
 * These tests require actual GCP credentials and will publish real messages.
 * 
 * Tests are skipped if GCP_PROJECT_ID environment variable is not set.
 * 
 * To run these tests locally:
 * 1. Set GOOGLE_APPLICATION_CREDENTIALS to the ABSOLUTE path of your service account key
 *    Example: export GOOGLE_APPLICATION_CREDENTIALS="/full/path/to/service-account-key.json"
 * 2. Set GCP_PROJECT_ID to your GCP project ID
 *    Example: export GCP_PROJECT_ID="your-project-id"
 * 3. Ensure the Pub/Sub topic exists in your project
 * 4. Run: mvn test -Dtest=PubSubServiceIntegrationTest
 * 
 * NOTE: The .env file may have a relative path for GOOGLE_APPLICATION_CREDENTIALS,
 * but the GCP library requires an absolute path. Before running tests, export the
 * absolute path manually.
 */
@EnabledIfEnvironmentVariable(named = "GCP_PROJECT_ID", matches = ".+")
class PubSubServiceIntegrationTest {
    
    private static String projectId;
    private static String topicId;
    private static PubSubService pubSubService;
    
    @BeforeAll
    static void setUp() {
        projectId = System.getenv("GCP_PROJECT_ID");
        topicId = System.getenv().getOrDefault("PUBSUB_TOPIC", "event-updates");
        
        assertNotNull(projectId, "GCP_PROJECT_ID must be set for integration tests");
        assertFalse(projectId.isBlank(), "GCP_PROJECT_ID must not be blank");
        
        // Verify credentials file exists and is an absolute path
        String credentialsPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
        if (credentialsPath != null && !credentialsPath.isBlank()) {
            File credentialsFile = new File(credentialsPath);
            assertTrue(credentialsFile.isAbsolute(), 
                "GOOGLE_APPLICATION_CREDENTIALS must be an absolute path. " +
                "Current value: " + credentialsPath + ". " +
                "Please use the full path, e.g., /path/to/credentials.json");
            assertTrue(credentialsFile.exists(), 
                "Credentials file does not exist: " + credentialsPath);
        }
        
        pubSubService = new PubSubService(projectId, topicId);
    }
    
    @Nested
    @DisplayName("Real Pub/Sub Publishing Tests")
    class RealPublishingTests {
        
        @Test
        @DisplayName("Should successfully publish a raw message to Pub/Sub")
        void shouldPublishRawMessage() {
            Map<String, Object> message = Map.of(
                "type", "INTEGRATION_TEST",
                "timestamp", System.currentTimeMillis(),
                "source", "PubSubServiceIntegrationTest"
            );
            
            String messageId = pubSubService.publishMessage(message);
            
            assertNotNull(messageId, "Message ID should not be null on successful publish");
            assertFalse(messageId.isBlank(), "Message ID should not be blank");
            System.out.println("Successfully published message with ID: " + messageId);
        }
        
        @Test
        @DisplayName("Should successfully publish EVENT_CREATED notification")
        void shouldPublishEventCreated() {
            assertDoesNotThrow(() -> {
                pubSubService.publishEventCreated(999L, 1L, 1L);
            });
            System.out.println("Successfully published EVENT_CREATED notification");
        }
        
        @Test
        @DisplayName("Should successfully publish EVENT_UPDATED notification")
        void shouldPublishEventUpdated() {
            assertDoesNotThrow(() -> {
                pubSubService.publishEventUpdated(999L);
            });
            System.out.println("Successfully published EVENT_UPDATED notification");
        }
        
        @Test
        @DisplayName("Should successfully publish TICKET_PURCHASED notification")
        void shouldPublishTicketPurchased() {
            assertDoesNotThrow(() -> {
                pubSubService.publishTicketPurchased(999L, 1L, "integration-test");
            });
            System.out.println("Successfully published TICKET_PURCHASED notification");
        }
    }
    
    @Nested
    @DisplayName("Message Content Tests")
    class MessageContentTests {
        
        @Test
        @DisplayName("Should publish message with complex nested data")
        void shouldPublishComplexNestedData() {
            Map<String, Object> nestedData = new HashMap<>();
            nestedData.put("level1", Map.of(
                "level2", Map.of(
                    "level3", "deepValue"
                )
            ));
            nestedData.put("type", "NESTED_TEST");
            nestedData.put("timestamp", System.currentTimeMillis());
            
            String messageId = pubSubService.publishMessage(nestedData);
            assertNotNull(messageId);
        }
        
        @Test
        @DisplayName("Should publish message with array data")
        void shouldPublishArrayData() {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "ARRAY_TEST");
            message.put("items", List.of("item1", "item2", "item3"));
            message.put("numbers", List.of(1, 2, 3, 4, 5));
            
            String messageId = pubSubService.publishMessage(message);
            assertNotNull(messageId);
        }
        
        @Test
        @DisplayName("Should publish message with special characters")
        void shouldPublishSpecialCharacters() {
            Map<String, Object> message = Map.of(
                "type", "SPECIAL_CHAR_TEST",
                "unicode", "Hello 世界 🎉 مرحبا",
                "quotes", "He said \"hello\" and 'goodbye'",
                "newlines", "line1\nline2\nline3"
            );
            
            String messageId = pubSubService.publishMessage(message);
            assertNotNull(messageId);
        }
        
        @Test
        @DisplayName("Should publish large message")
        void shouldPublishLargeMessage() {
            StringBuilder largeText = new StringBuilder();
            for (int i = 0; i < 1000; i++) {
                largeText.append("This is line ").append(i).append(" of the large message. ");
            }
            
            Map<String, Object> message = Map.of(
                "type", "LARGE_MESSAGE_TEST",
                "data", largeText.toString(),
                "size", largeText.length()
            );
            
            String messageId = pubSubService.publishMessage(message);
            assertNotNull(messageId);
        }
    }
    
    @Nested
    @DisplayName("Concurrent Publishing Tests")
    class ConcurrentPublishingTests {
        
        @Test
        @DisplayName("Should handle concurrent message publishing")
        void shouldHandleConcurrentPublishing() throws InterruptedException {
            int numThreads = 5;
            int messagesPerThread = 3;
            ExecutorService executor = Executors.newFixedThreadPool(numThreads);
            CountDownLatch latch = new CountDownLatch(numThreads * messagesPerThread);
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failCount = new AtomicInteger(0);
            
            for (int t = 0; t < numThreads; t++) {
                final int threadId = t;
                for (int m = 0; m < messagesPerThread; m++) {
                    final int messageId = m;
                    executor.submit(() -> {
                        try {
                            String result = pubSubService.publishMessage(Map.of(
                                "type", "CONCURRENT_TEST",
                                "threadId", threadId,
                                "messageId", messageId,
                                "timestamp", System.currentTimeMillis()
                            ));
                            if (result != null) {
                                successCount.incrementAndGet();
                            } else {
                                failCount.incrementAndGet();
                            }
                        } catch (Exception e) {
                            failCount.incrementAndGet();
                        } finally {
                            latch.countDown();
                        }
                    });
                }
            }
            
            boolean completed = latch.await(30, TimeUnit.SECONDS);
            executor.shutdown();
            
            assertTrue(completed, "All messages should complete within timeout");
            assertEquals(numThreads * messagesPerThread, successCount.get(), 
                "All messages should be published successfully");
            assertEquals(0, failCount.get(), "No messages should fail");
            
            System.out.println("Successfully published " + successCount.get() + " concurrent messages");
        }
    }
    
    @Nested
    @DisplayName("Message ID Uniqueness Tests")
    class MessageIdUniquenessTests {
        
        @RepeatedTest(3)
        @DisplayName("Should return unique message IDs for each publish")
        void shouldReturnUniqueMessageIds() {
            List<String> messageIds = new ArrayList<>();
            
            for (int i = 0; i < 5; i++) {
                String messageId = pubSubService.publishMessage(Map.of(
                    "type", "UNIQUENESS_TEST",
                    "iteration", i,
                    "timestamp", System.currentTimeMillis()
                ));
                assertNotNull(messageId);
                assertFalse(messageIds.contains(messageId), 
                    "Message ID should be unique: " + messageId);
                messageIds.add(messageId);
            }
            
            assertEquals(5, messageIds.size());
        }
    }
    
    @Nested
    @DisplayName("Service Configuration Tests")
    class ConfigurationTests {
        
        @Test
        @DisplayName("PubSubService should be enabled with valid project ID")
        void shouldBeEnabledWithValidProjectId() {
            String messageId = pubSubService.publishMessage(Map.of(
                "type", "CONFIG_TEST",
                "test", true
            ));
            
            assertNotNull(messageId, "PubSubService should be enabled and returning message IDs");
        }
        
        @Test
        @DisplayName("Should use configured topic")
        void shouldUseConfiguredTopic() {
            // Create a service with a specific topic
            PubSubService customService = new PubSubService(projectId, topicId);
            
            String messageId = customService.publishMessage(Map.of(
                "type", "TOPIC_CONFIG_TEST",
                "topic", topicId
            ));
            
            assertNotNull(messageId);
        }
    }
    
    @Nested
    @DisplayName("Event Workflow Tests")
    class EventWorkflowTests {
        
        @Test
        @DisplayName("Should simulate complete event lifecycle")
        void shouldSimulateEventLifecycle() {
            Long testEventId = System.currentTimeMillis();
            Long organizerId = 1L;
            Long campusId = 1L;
            
            // 1. Create event
            assertDoesNotThrow(() -> {
                pubSubService.publishEventCreated(testEventId, organizerId, campusId);
            }, "Should publish event created");
            
            // 2. Update event
            assertDoesNotThrow(() -> {
                pubSubService.publishEventUpdated(testEventId);
            }, "Should publish event updated");
            
            // 3. Tickets purchased
            for (int i = 0; i < 3; i++) {
                final int userId = i + 1;
                assertDoesNotThrow(() -> {
                    pubSubService.publishTicketPurchased(testEventId, (long) userId, "General");
                }, "Should publish ticket purchased for user " + userId);
            }
            
            System.out.println("Successfully simulated event lifecycle for event: " + testEventId);
        }
    }
}
