package com.reliaquest.api.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.reliaquest.api.exceptions.ExternalApiException;
import com.reliaquest.api.models.Employee;
import com.reliaquest.api.models.EmployeeResponse;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

class EmployeeServiceTest {
    EmployeeService employeeService;
    AutoCloseable closeable;

    @Mock
    RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        employeeService = new EmployeeService("testEndpoint", restTemplate);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
        employeeService = null;
    }

    @Test
    void getAllEmployees() {
        List<Employee> employees = List.of();
        EmployeeResponse body = new EmployeeResponse(employees);
        when(restTemplate.exchange(
                        eq("testEndpoint"), eq(HttpMethod.GET), eq(null), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(body));
        List<Employee> response = employeeService.getAllEmployees();
        assertEquals(employees, response);
    }

    @Test
    void getAllEmployeesShouldHandleNull() {
        when(restTemplate.exchange(
                        eq("testEndpoint"), eq(HttpMethod.GET), eq(null), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(null));
        ExternalApiException exception =
                assertThrows(ExternalApiException.class, () -> employeeService.getAllEmployees());
        assertEquals("Failed to retrieve employees", exception.getMessage());
    }

    @Test
    void getAllEmployeesShouldHandleNon200() {
        when(restTemplate.exchange(
                        eq("testEndpoint"), eq(HttpMethod.GET), eq(null), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST).build());
        ExternalApiException exception =
                assertThrows(ExternalApiException.class, () -> employeeService.getAllEmployees());
        assertEquals("Failed to retrieve employees", exception.getMessage());
    }

    @Test
    void getAllEmployeesShouldHandleRateLimit() {
        when(restTemplate.exchange(
                        eq("testEndpoint"), eq(HttpMethod.GET), eq(null), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build());
        ExternalApiException exception =
                assertThrows(ExternalApiException.class, () -> employeeService.getAllEmployees());
        assertEquals("Failed to retrieve employees. Rate limit exceeded", exception.getMessage());
    }
}
