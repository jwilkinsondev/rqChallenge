package com.reliaquest.api.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import com.reliaquest.api.models.Employee;
import com.reliaquest.api.services.EmployeeService;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
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
        when(employeeService.getAllEmployees()).thenThrow(new RuntimeException("An error occurred"));
        ResponseEntity<List> result = employeeController.getAllEmployees();

        assertNotNull(result);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertNull(result.getBody());
    }

    @Test
    void getEmployeesByNameSearch() {}

    @Test
    void getEmployeeById() {}

    @Test
    void getHighestSalaryOfEmployees() {}

    @Test
    void getTopTenHighestEarningEmployeeNames() {}

    @Test
    void createEmployee() {}

    @Test
    void deleteEmployeeById() {}
}
