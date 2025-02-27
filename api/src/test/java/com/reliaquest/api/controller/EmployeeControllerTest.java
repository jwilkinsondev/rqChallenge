package com.reliaquest.api.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reliaquest.api.exceptions.EmployeeValidationError;
import com.reliaquest.api.exceptions.ExternalApiException;
import com.reliaquest.api.models.CreateEmployee;
import com.reliaquest.api.models.Employee;
import com.reliaquest.api.services.EmployeeService;
import java.net.URI;
import java.util.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

class EmployeeControllerTest {
    EmployeeController employeeController;
    AutoCloseable closeable;

    @Mock
    EmployeeService employeeService;

    @Mock
    ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        employeeController = new EmployeeController(employeeService, objectMapper);
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
    void getEmployeeByIdShouldHandleEmptyResult() {
        UUID id = UUID.randomUUID();
        when(employeeService.getEmployeeById(id.toString())).thenReturn(Optional.empty());
        ResponseEntity<Employee> result = employeeController.getEmployeeById(id.toString());
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
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
    void getHighestSalaryOfEmployees() {
        ArrayList<Employee> employees = new ArrayList<>();
        employees.add(new Employee(UUID.randomUUID(), "john", "123", 26, "IT Technician", "jdoe@test.com"));
        employees.add(new Employee(UUID.randomUUID(), "joe", "456", 26, "IT Technician", "jdoe@test.com"));
        employees.add(new Employee(UUID.randomUUID(), "mary", "789", 26, "IT Technician", "jdoe@test.com"));

        when(employeeService.getAllEmployees()).thenReturn(employees);
        when(employeeService.getNHighestSalaries(1, employees)).thenReturn(Collections.singletonList(employees.get(2)));
        ResponseEntity<Integer> result = employeeController.getHighestSalaryOfEmployees();
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(Integer.parseInt(employees.get(2).salary()), result.getBody());
    }

    @Test
    void getHighestSalaryOfEmployeesHandlesSalaryParseError() {
        ArrayList<Employee> employees = new ArrayList<>();
        employees.add(new Employee(UUID.randomUUID(), "john", "123", 26, "IT Technician", "jdoe@test.com"));
        employees.add(new Employee(UUID.randomUUID(), "joe", "456", 26, "IT Technician", "jdoe@test.com"));
        employees.add(new Employee(UUID.randomUUID(), "mary", "foo", 26, "IT Technician", "jdoe@test.com"));

        when(employeeService.getAllEmployees()).thenReturn(employees);
        when(employeeService.getNHighestSalaries(1, employees)).thenReturn(Collections.singletonList(employees.get(2)));
        ResponseEntity<Integer> result = employeeController.getHighestSalaryOfEmployees();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertNull(result.getBody());
    }

    @Test
    void getHighestSalaryOfEmployeesHandlesDownstreamError() {
        doThrow(new ExternalApiException("An error occurred"))
                .when(employeeService)
                .getAllEmployees();

        ResponseEntity<Integer> result = employeeController.getHighestSalaryOfEmployees();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertNull(result.getBody());
    }

    @Test
    void getTopTenHighestEarningEmployeeNames() {
        ArrayList<Employee> employees = new ArrayList<>();
        employees.add(new Employee(UUID.randomUUID(), "john", "123", 26, "IT Technician", "jdoe@test.com"));
        employees.add(new Employee(UUID.randomUUID(), "joe", "456", 26, "IT Technician", "jdoe@test.com"));
        employees.add(new Employee(UUID.randomUUID(), "mary", "789", 26, "IT Technician", "jdoe@test.com"));

        when(employeeService.getAllEmployees()).thenReturn(employees);
        when(employeeService.getNHighestSalaries(10, employees)).thenReturn(employees);
        ResponseEntity<List<String>> result = employeeController.getTopTenHighestEarningEmployeeNames();
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(employees.size(), Objects.requireNonNull(result.getBody()).size());
        assertEquals(employees.get(0).name(), result.getBody().get(0));
        assertEquals(employees.get(1).name(), result.getBody().get(1));
        assertEquals(employees.get(2).name(), result.getBody().get(2));
    }

    @Test
    void getTopTenHighestEarningEmployeeNamesHandlesSalaryParseError() {
        ArrayList<Employee> employees = new ArrayList<>();
        employees.add(new Employee(UUID.randomUUID(), "john", "123", 26, "IT Technician", "jdoe@test.com"));
        employees.add(new Employee(UUID.randomUUID(), "joe", "456", 26, "IT Technician", "jdoe@test.com"));
        employees.add(new Employee(UUID.randomUUID(), "mary", "foo", 26, "IT Technician", "jdoe@test.com"));

        when(employeeService.getAllEmployees()).thenReturn(employees);
        doThrow(NumberFormatException.class).when(employeeService).getNHighestSalaries(10, employees);
        ResponseEntity<List<String>> result = employeeController.getTopTenHighestEarningEmployeeNames();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertNull(result.getBody());
    }

    @Test
    void createEmployee() {
        Object inputObject = new Object() {};

        CreateEmployee createEmployee = new CreateEmployee("john doe", "123456", 26, "IT Technician");
        when(objectMapper.convertValue(inputObject, CreateEmployee.class)).thenReturn(createEmployee);
        Employee createdEmployee = new Employee(
                UUID.randomUUID(),
                createEmployee.name(),
                createEmployee.salary(),
                createEmployee.age(),
                createEmployee.title(),
                "foo@bar.com");
        when(employeeService.createEmployee(createEmployee)).thenReturn(createdEmployee);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        ResponseEntity<Employee> result = employeeController.createEmployee(inputObject);
        verify(employeeService, times(1)).validateCreateEmployee(createEmployee);
        verify(employeeService, times(1)).createEmployee(createEmployee);
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(
                URI.create("http://localhost/" + createdEmployee.id()),
                result.getHeaders().getLocation());
    }

    @Test
    void createEmployeeShouldHandleArgumentException() {
        Object inputObject = new Object() {};
        when(objectMapper.convertValue(inputObject, CreateEmployee.class))
                .thenThrow(new IllegalArgumentException("Invalid input"));

        ResponseEntity<Employee> result = employeeController.createEmployee(inputObject);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertNull(result.getBody());
    }

    @Test
    void createEmployeeShouldHandleInputValidation() {
        Object inputObject = new Object() {};
        CreateEmployee createEmployee = new CreateEmployee("john doe", "123456", 26, "IT Technician");
        when(objectMapper.convertValue(inputObject, CreateEmployee.class)).thenReturn(createEmployee);
        doThrow(new EmployeeValidationError("Name cannot be blank"))
                .when(employeeService)
                .validateCreateEmployee(createEmployee);

        ResponseEntity<Employee> result = employeeController.createEmployee(inputObject);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertEquals("Name cannot be blank", result.getBody());
    }

    @Test
    void createEmployeeShouldHandleCreationError() {
        Object inputObject = new Object() {};

        CreateEmployee createEmployee = new CreateEmployee("john doe", "123456", 26, "IT Technician");
        when(objectMapper.convertValue(inputObject, CreateEmployee.class)).thenReturn(createEmployee);
        doThrow(new ExternalApiException("Unable to create employee"))
                .when(employeeService)
                .createEmployee(createEmployee);

        ResponseEntity<Employee> result = employeeController.createEmployee(inputObject);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertNull(result.getBody());
    }

    @Test
    void deleteEmployeeById() {
        UUID id = UUID.randomUUID();
        Employee employee = new Employee(id, "john doe", "123", 26, "IT Technician", "foo@bar.com");
        when(employeeService.getEmployeeById(id.toString())).thenReturn(Optional.of(employee));
        when(employeeService.deleteEmployeeByName("john doe")).thenReturn(true);

        ResponseEntity<String> result = employeeController.deleteEmployeeById(id.toString());
        verify(employeeService, times(1)).getEmployeeById(id.toString());
        verify(employeeService, times(1)).deleteEmployeeByName("john doe");
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("john doe", result.getBody());
    }

    @Test
    void deleteEmployeeByIdShouldHandleMissingEmployee() {
        UUID id = UUID.randomUUID();
        when(employeeService.getEmployeeById(id.toString())).thenReturn(Optional.empty());

        ResponseEntity<String> result = employeeController.deleteEmployeeById(id.toString());
        verify(employeeService, times(1)).getEmployeeById(id.toString());
        verify(employeeService, never()).deleteEmployeeByName(anyString());
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertNull(result.getBody());
    }

    @Test
    void deleteEmployeeByIdShouldHandleMissingName() {
        UUID id = UUID.randomUUID();
        Employee employee = new Employee(id, "", "123", 26, "IT Technician", "foo@bar.com");
        when(employeeService.getEmployeeById(id.toString())).thenReturn(Optional.of(employee));

        ResponseEntity<String> result = employeeController.deleteEmployeeById(id.toString());
        verify(employeeService, times(1)).getEmployeeById(id.toString());
        verify(employeeService, never()).deleteEmployeeByName(anyString());
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertEquals("Employee name is blank", result.getBody());
    }

    @Test
    void deleteEmployeeByIdShouldHandleDeleteError() {
        UUID id = UUID.randomUUID();
        Employee employee = new Employee(id, "john doe", "123", 26, "IT Technician", "foo@bar.com");
        when(employeeService.getEmployeeById(id.toString())).thenReturn(Optional.of(employee));
        when(employeeService.deleteEmployeeByName("john doe")).thenReturn(false);

        ResponseEntity<String> result = employeeController.deleteEmployeeById(id.toString());
        verify(employeeService, times(1)).getEmployeeById(id.toString());
        verify(employeeService, times(1)).deleteEmployeeByName("john doe");
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertNull(result.getBody());
    }

    @Test
    void deleteEmployeeByIdShouldHandleException() {
        doThrow(new ExternalApiException("An error occurred"))
                .when(employeeService)
                .getEmployeeById(anyString());
        UUID id = UUID.randomUUID();

        ResponseEntity<String> result = employeeController.deleteEmployeeById(id.toString());
        verify(employeeService, times(1)).getEmployeeById(id.toString());
        verify(employeeService, never()).deleteEmployeeByName(anyString());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertNull(result.getBody());
    }
}
