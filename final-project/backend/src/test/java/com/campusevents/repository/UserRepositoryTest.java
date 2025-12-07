package com.campusevents.repository;

import com.campusevents.model.User;
import com.campusevents.util.SqlExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserRepository.
 * Verifies raw SQL queries are executed correctly.
 */
class UserRepositoryTest {
    
    private SqlExecutor sqlExecutor;
    private UserRepository userRepository;
    
    @BeforeEach
    void setUp() {
        sqlExecutor = mock(SqlExecutor.class);
        userRepository = new UserRepository(sqlExecutor);
    }
    
    @Test
    @DisplayName("Should find user by email")
    void shouldFindUserByEmail() {
        // Arrange
        Map<String, Object> row = new HashMap<>();
        row.put("id", 1L);
        row.put("first_name", "John");
        row.put("last_name", "Doe");
        row.put("email", "john@example.com");
        row.put("password", "hashedpassword");
        row.put("campus_id", 100L);
        
        when(sqlExecutor.executeQuery(
            eq("SELECT id, first_name, last_name, email, password, campus_id FROM \"user\" WHERE email = ?"),
            any(Object[].class)
        )).thenReturn(List.of(row));
        
        // Act
        Optional<User> result = userRepository.findByEmail("john@example.com");
        
        // Assert
        assertTrue(result.isPresent());
        User user = result.get();
        assertEquals(1L, user.getId());
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals("john@example.com", user.getEmail());
        assertEquals("hashedpassword", user.getPassword());
        assertEquals(100L, user.getCampusId());
    }
    
    @Test
    @DisplayName("Should return empty when user not found by email")
    void shouldReturnEmptyWhenNotFound() {
        // Arrange
        when(sqlExecutor.executeQuery(
            eq("SELECT id, first_name, last_name, email, password, campus_id FROM \"user\" WHERE email = ?"),
            any(Object[].class)
        )).thenReturn(Collections.emptyList());
        
        // Act
        Optional<User> result = userRepository.findByEmail("nonexistent@example.com");
        
        // Assert
        assertTrue(result.isEmpty());
    }
    
    @Test
    @DisplayName("Should save user and return with generated ID")
    void shouldSaveUser() {
        // Arrange
        User user = new User("John", "Doe", "john@example.com", "hashedpassword", 100L);
        
        when(sqlExecutor.executeInsert(
            eq("INSERT INTO \"user\" (first_name, last_name, email, password, campus_id) VALUES (?, ?, ?, ?, ?)"),
            any(Object[].class)
        )).thenReturn(1L);
        
        // Act
        User savedUser = userRepository.save(user);
        
        // Assert
        assertEquals(1L, savedUser.getId());
        assertEquals("John", savedUser.getFirstName());
        
        verify(sqlExecutor).executeInsert(
            eq("INSERT INTO \"user\" (first_name, last_name, email, password, campus_id) VALUES (?, ?, ?, ?, ?)"),
            argThat(params -> {
                Object[] p = (Object[]) params;
                return p.length == 5 &&
                       "John".equals(p[0]) &&
                       "Doe".equals(p[1]) &&
                       "john@example.com".equals(p[2]) &&
                       "hashedpassword".equals(p[3]) &&
                       Long.valueOf(100L).equals(p[4]);
            })
        );
    }
    
    @Test
    @DisplayName("Should check if email exists")
    void shouldCheckEmailExists() {
        // Arrange
        when(sqlExecutor.exists(
            eq("SELECT 1 FROM \"user\" WHERE email = ?"),
            any(Object[].class)
        )).thenReturn(true);
        
        // Act
        boolean exists = userRepository.existsByEmail("existing@example.com");
        
        // Assert
        assertTrue(exists);
    }
    
    @Test
    @DisplayName("Should find user by ID")
    void shouldFindUserById() {
        // Arrange
        Map<String, Object> row = new HashMap<>();
        row.put("id", 42L);
        row.put("first_name", "Jane");
        row.put("last_name", "Smith");
        row.put("email", "jane@example.com");
        row.put("password", "hashedpassword");
        row.put("campus_id", 200L);
        
        when(sqlExecutor.executeQuery(
            eq("SELECT id, first_name, last_name, email, password, campus_id FROM \"user\" WHERE id = ?"),
            any(Object[].class)
        )).thenReturn(List.of(row));
        
        // Act
        Optional<User> result = userRepository.findById(42L);
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(42L, result.get().getId());
        assertEquals("Jane", result.get().getFirstName());
    }
    
    @Test
    @DisplayName("Should update user password")
    void shouldUpdatePassword() {
        // Arrange
        when(sqlExecutor.executeUpdate(
            eq("UPDATE \"user\" SET password = ? WHERE id = ?"),
            any(Object[].class)
        )).thenReturn(1);
        
        // Act
        int rowsAffected = userRepository.updatePassword(1L, "newhashedpassword");
        
        // Assert
        assertEquals(1, rowsAffected);
        
        verify(sqlExecutor).executeUpdate(
            eq("UPDATE \"user\" SET password = ? WHERE id = ?"),
            argThat(params -> {
                Object[] p = (Object[]) params;
                return "newhashedpassword".equals(p[0]) && Long.valueOf(1L).equals(p[1]);
            })
        );
    }
    
    @Test
    @DisplayName("Should delete user by ID")
    void shouldDeleteUser() {
        // Arrange
        when(sqlExecutor.executeUpdate(
            eq("DELETE FROM \"user\" WHERE id = ?"),
            any(Object[].class)
        )).thenReturn(1);
        
        // Act
        int rowsAffected = userRepository.deleteById(1L);
        
        // Assert
        assertEquals(1, rowsAffected);
    }
}
