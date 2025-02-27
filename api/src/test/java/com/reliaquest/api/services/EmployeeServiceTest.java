package com.reliaquest.api.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.reliaquest.api.exceptions.ExternalApiException;
import com.reliaquest.api.models.Employee;
import com.reliaquest.api.models.EmployeeListResponse;
import com.reliaquest.api.models.EmployeeResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
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
        EmployeeListResponse body = new EmployeeListResponse(employees);
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
        List<Employee> result = employeeService.getAllEmployees();
        assertEquals(0, result.size());
    }

    @Test
    void getAllEmployeesShouldHandleNon200() {
        doThrow(new HttpClientErrorException(HttpStatus.BAD_GATEWAY))
                .when(restTemplate)
                .exchange(eq("testEndpoint"), eq(HttpMethod.GET), eq(null), any(ParameterizedTypeReference.class));
        ExternalApiException exception =
                assertThrows(ExternalApiException.class, () -> employeeService.getAllEmployees());
        assertEquals("Failed to retrieve employees", exception.getMessage());
    }

    @Test
    void getAllEmployeesShouldHandleRateLimit() {
        doThrow(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS))
                .when(restTemplate)
                .exchange(eq("testEndpoint"), eq(HttpMethod.GET), eq(null), any(ParameterizedTypeReference.class));
        ExternalApiException exception =
                assertThrows(ExternalApiException.class, () -> employeeService.getAllEmployees());
        assertEquals("Failed to retrieve employees. Rate limit exceeded", exception.getMessage());
    }

    @Test
    void getEmployeesByNameSearch() {
        List<Employee> employees = new ArrayList<>();
        Employee employee1 =
                new Employee(UUID.randomUUID(), "John Doe", "57000", 54, "Software Engineer", "foo@bar.com");
        Employee employee2 =
                new Employee(UUID.randomUUID(), "Billy Sue", "57000", 54, "Software Engineer", "foo@bar.com");
        Employee employee3 =
                new Employee(UUID.randomUUID(), "Bob Test", "57000", 54, "Software Engineer", "foo@bar.com");
        Employee employee4 =
                new Employee(UUID.randomUUID(), "Alice Tester", "57000", 54, "Software Engineer", "foo@bar.com");
        Employee employee5 =
                new Employee(UUID.randomUUID(), "Foo Bar", "57000", 54, "Software Engineer", "foo@bar.com");
        employees.add(employee1);
        employees.add(employee2);
        employees.add(employee3);
        employees.add(employee4);
        employees.add(employee5);
        EmployeeListResponse body = new EmployeeListResponse(employees);

        when(restTemplate.exchange(
                        eq("testEndpoint"), eq(HttpMethod.GET), eq(null), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(body));

        List<Employee> response = employeeService.getEmployeesByNameSearch("Test");
        assertEquals(2, response.size());
        assertEquals(employees.get(2), response.get(0));
        assertEquals(employees.get(3), response.get(1));
    }

    @Test
    void getEmployeesByNameSearchReturnsEmptyListForNoMatches() {
        List<Employee> employees = new ArrayList<>();
        Employee employee =
                new Employee(UUID.randomUUID(), "John Doe", "57000", 54, "Software Engineer", "foo@bar.com");
        employees.add(employee);

        EmployeeListResponse body = new EmployeeListResponse(employees);

        when(restTemplate.exchange(
                        eq("testEndpoint"), eq(HttpMethod.GET), eq(null), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(body));

        List<Employee> response = employeeService.getEmployeesByNameSearch("Test");
        assertEquals(0, response.size());
    }

    @Test
    void getEmployeesByNameSearchIsCaseInsensitive() {
        List<Employee> employees = new ArrayList<>();
        Employee employee =
                new Employee(UUID.randomUUID(), "John Doe", "57000", 54, "Software Engineer", "foo@bar.com");
        employees.add(employee);

        EmployeeListResponse body = new EmployeeListResponse(employees);

        when(restTemplate.exchange(
                        eq("testEndpoint"), eq(HttpMethod.GET), eq(null), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(body));

        List<Employee> response = employeeService.getEmployeesByNameSearch("John Doe");
        assertEquals(employees.get(0), response.get(0));

        employees = new ArrayList<>();
        employee = new Employee(UUID.randomUUID(), "JOHN doe", "57000", 54, "Software Engineer", "foo@bar.com");
        employees.add(employee);

        body = new EmployeeListResponse(employees);

        when(restTemplate.exchange(
                        eq("testEndpoint"), eq(HttpMethod.GET), eq(null), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(body));

        response = employeeService.getEmployeesByNameSearch("John Doe");
        assertEquals(employees.get(0), response.get(0));
    }

    @Test
    void getEmployeesByNameSearchHandlesPartialMatch() {
        List<Employee> employees = new ArrayList<>();
        Employee employee =
                new Employee(UUID.randomUUID(), "one fish two fish", "57000", 54, "Software Engineer", "foo@bar.com");
        employees.add(employee);

        EmployeeListResponse body = new EmployeeListResponse(employees);

        when(restTemplate.exchange(
                        eq("testEndpoint"), eq(HttpMethod.GET), eq(null), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(body));

        List<Employee> response = employeeService.getEmployeesByNameSearch("sh tw");
        assertEquals(employees.get(0), response.get(0));
    }

    @Test
    void getEmployeeById() {
        UUID id = UUID.randomUUID();
        Employee employee = new Employee(id, "John Doe", "57000", 54, "Software Engineer", "foo@bar.com");
        when(restTemplate.exchange(
                        eq("testEndpoint/" + id), eq(HttpMethod.GET), eq(null), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(new EmployeeResponse(employee)));
        Employee response = employeeService.getEmployeeById(id.toString()).get();
        assertEquals(employee, response);
    }

    @Test
    void getEmployeeByIdShouldHandleNullBody() {
        UUID id = UUID.randomUUID();
        when(restTemplate.exchange(
                        eq("testEndpoint/" + id), eq(HttpMethod.GET), eq(null), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(null));
        Optional<Employee> result = employeeService.getEmployeeById(id.toString());
        assertTrue(result.isEmpty());
    }

    @Test
    void getEmployeeByIdShouldHandleNullData() {
        UUID id = UUID.randomUUID();
        when(restTemplate.exchange(
                        eq("testEndpoint/" + id), eq(HttpMethod.GET), eq(null), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(new EmployeeResponse(null)));
        Optional<Employee> result = employeeService.getEmployeeById(id.toString());
        assertTrue(result.isEmpty());
    }

    @Test
    void getEmployeeByIdShouldHandleRateLimit() {
        UUID id = UUID.randomUUID();
        doThrow(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS))
                .when(restTemplate)
                .exchange(
                        eq("testEndpoint/" + id), eq(HttpMethod.GET), eq(null), any(ParameterizedTypeReference.class));
        ExternalApiException exception =
                assertThrows(ExternalApiException.class, () -> employeeService.getEmployeeById(id.toString()));
        assertEquals("Failed to retrieve employee. Rate limit exceeded", exception.getMessage());
    }

    @Test
    void getEmployeeByIdShouldHandleNon200() {
        UUID id = UUID.randomUUID();
        doThrow(new HttpClientErrorException(HttpStatus.BAD_GATEWAY))
                .when(restTemplate)
                .exchange(
                        eq("testEndpoint/" + id), eq(HttpMethod.GET), eq(null), any(ParameterizedTypeReference.class));
        ExternalApiException exception =
                assertThrows(ExternalApiException.class, () -> employeeService.getEmployeeById(id.toString()));
        assertEquals("Failed to retrieve employee", exception.getMessage());
    }
}
