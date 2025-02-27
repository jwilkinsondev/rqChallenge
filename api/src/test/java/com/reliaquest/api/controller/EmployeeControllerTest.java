package com.reliaquest.api.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.reliaquest.api.exceptions.ExternalApiException;
import com.reliaquest.api.models.Employee;
import com.reliaquest.api.services.EmployeeService;
import java.util.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class EmployeeControllerTest {
    EmployeeController employeeController;
    AutoCloseable closeable;

    @Mock
    EmployeeService employeeService;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        employeeController = new EmployeeController(employeeService);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
        employeeController = null;
    }

    @Test
    void getAllEmployees() {
        ArrayList<Employee> employees = new ArrayList<>();
        employees.add(new Employee(UUID.randomUUID(), "john", "doe", 26, "IT Technician", "jdoe@test.com"));

        when(employeeService.getAllEmployees()).thenReturn(employees);
        ResponseEntity<List> result = employeeController.getAllEmployees();

        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(employees, result.getBody());
        assertEquals(1, Objects.requireNonNull(result.getBody()).size());
        assertEquals(employees.get(0), result.getBody().get(0));
    }

    @Test
    void getAllEmployeesShouldHandleException() {
        when(employeeService.getAllEmployees()).thenThrow(new ExternalApiException("An error occurred"));
        ResponseEntity<List> result = employeeController.getAllEmployees();

        assertNotNull(result);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertNull(result.getBody());
    }

    @Test
    void getEmployeesByNameSearch() {
        ArrayList<Employee> employees = new ArrayList<>();
        employees.add(new Employee(UUID.randomUUID(), "john", "doe", 26, "IT Technician", "jdoe@test.com"));

        when(employeeService.getEmployeesByNameSearch("john")).thenReturn(employees);
        ResponseEntity<List> result = employeeController.getEmployeesByNameSearch("john");
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(employees, result.getBody());
        assertEquals(1, Objects.requireNonNull(result.getBody()).size());
        assertEquals(employees.get(0), result.getBody().get(0));
    }

    @Test
    void getEmployeesByNameSearchShouldHandleException() {
        when(employeeService.getEmployeesByNameSearch("john")).thenThrow(new ExternalApiException("An error occurred"));

        ResponseEntity<List> result = employeeController.getEmployeesByNameSearch("john");
        assertNotNull(result);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertNull(result.getBody());
    }

    @Test
    void getEmployeeById() {
        UUID id = UUID.randomUUID();
        Employee employee = new Employee(id, "john doe", "45678", 26, "IT Technician", "foo@bar.com");

        when(employeeService.getEmployeeById(id.toString())).thenReturn(Optional.of(employee));
        ResponseEntity<Employee> result = employeeController.getEmployeeById(id.toString());
        verify(employeeService, times(1)).getEmployeeById(id.toString());
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(employee, result.getBody());
    }

    @Test
    void getEmployeeByIdShouldHandleException() {
        UUID id = UUID.randomUUID();
        when(employeeService.getEmployeeById(id.toString())).thenThrow(new ExternalApiException("An error occurred"));

        ResponseEntity<Employee> result = employeeController.getEmployeeById(id.toString());
        assertNotNull(result);
        verify(employeeService, times(1)).getEmployeeById(id.toString());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertNull(result.getBody());
    }

    @Test
    void getHighestSalaryOfEmployees() {}

    @Test
    void getTopTenHighestEarningEmployeeNames() {}

    @Test
    void createEmployee() {}

    @Test
    void deleteEmployeeById() {}
}
