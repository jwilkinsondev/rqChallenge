package com.reliaquest.api.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.reliaquest.api.exceptions.EmployeeValidationError;
import com.reliaquest.api.exceptions.ExternalApiException;
import com.reliaquest.api.exceptions.ExternalApiRateLimitException;
import com.reliaquest.api.models.CreateEmployee;
import com.reliaquest.api.models.Employee;
import com.reliaquest.api.models.EmployeeListResponse;
import com.reliaquest.api.models.EmployeeResponse;
import java.util.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@SuppressWarnings({"unchecked", "restTemplate requires ParameterizedTypeReference"})
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
        ExternalApiRateLimitException exception =
                assertThrows(ExternalApiRateLimitException.class, () -> employeeService.getAllEmployees());
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
        Optional<Employee> response = employeeService.getEmployeeById(id.toString());
        assertTrue(response.isPresent());
        assertEquals(employee, response.get());
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
        ExternalApiRateLimitException exception =
                assertThrows(ExternalApiRateLimitException.class, () -> employeeService.getEmployeeById(id.toString()));
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

    @Test
    void getNHighestSalaries() {
        List<Employee> employees = new ArrayList<>();
        Employee employee0 = new Employee(UUID.randomUUID(), "John Doe", "0", 54, "Software Engineer", "foo@bar.com");
        Employee employee1 = new Employee(UUID.randomUUID(), "John Doe", "1", 54, "Software Engineer", "foo@bar.com");
        Employee employee2 = new Employee(UUID.randomUUID(), "John Doe", "2", 54, "Software Engineer", "foo@bar.com");
        Employee employee3 = new Employee(UUID.randomUUID(), "John Doe", "3", 54, "Software Engineer", "foo@bar.com");
        Employee employee4 = new Employee(UUID.randomUUID(), "John Doe", "4", 54, "Software Engineer", "foo@bar.com");
        employees.add(employee0);
        employees.add(employee1);
        employees.add(employee2);
        employees.add(employee3);
        employees.add(employee4);

        List<Employee> response = employeeService.getNHighestSalaries(3, employees);
        assertEquals(3, response.size());
        assertEquals(employee4, response.get(0));
        assertEquals(employee3, response.get(1));
        assertEquals(employee2, response.get(2));
    }

    @Test
    void getNHighestSalariesShouldHandleEmptyList() {
        List<Employee> employees = new ArrayList<>();
        List<Employee> response = employeeService.getNHighestSalaries(3, employees);
        assertEquals(0, response.size());
    }

    @Test
    void getNHighestSalariesShouldHandleListSmallerThanN() {
        List<Employee> employees = new ArrayList<>();
        Employee employee0 = new Employee(UUID.randomUUID(), "John Doe", "0", 54, "Software Engineer", "foo@bar.com");
        Employee employee1 = new Employee(UUID.randomUUID(), "John Doe", "1", 54, "Software Engineer", "foo@bar.com");
        Employee employee2 = new Employee(UUID.randomUUID(), "John Doe", "2", 54, "Software Engineer", "foo@bar.com");
        Employee employee3 = new Employee(UUID.randomUUID(), "John Doe", "3", 54, "Software Engineer", "foo@bar.com");
        Employee employee4 = new Employee(UUID.randomUUID(), "John Doe", "4", 54, "Software Engineer", "foo@bar.com");
        employees.add(employee0);
        employees.add(employee1);
        employees.add(employee2);
        employees.add(employee3);
        employees.add(employee4);

        List<Employee> response = employeeService.getNHighestSalaries(10, employees);
        assertEquals(5, response.size());
        assertEquals(employee4, response.get(0));
        assertEquals(employee0, response.get(4));
    }

    @Test
    void getNHighestSalariesShouldHandleSmallN() {
        List<Employee> employees = new ArrayList<>();
        Employee employee0 = new Employee(UUID.randomUUID(), "John Doe", "0", 54, "Software Engineer", "foo@bar.com");
        Employee employee1 = new Employee(UUID.randomUUID(), "John Doe", "1", 54, "Software Engineer", "foo@bar.com");
        Employee employee2 = new Employee(UUID.randomUUID(), "John Doe", "2", 54, "Software Engineer", "foo@bar.com");
        Employee employee3 = new Employee(UUID.randomUUID(), "John Doe", "3", 54, "Software Engineer", "foo@bar.com");
        Employee employee4 = new Employee(UUID.randomUUID(), "John Doe", "4", 54, "Software Engineer", "foo@bar.com");
        employees.add(employee0);
        employees.add(employee1);
        employees.add(employee2);
        employees.add(employee3);
        employees.add(employee4);

        List<Employee> response = employeeService.getNHighestSalaries(0, employees);
        assertEquals(0, response.size());

        response = employeeService.getNHighestSalaries(-5, employees);
        assertEquals(0, response.size());
    }

    @Test
    void getNHighestSalariesShouldHandleNullList() {
        List<Employee> response = employeeService.getNHighestSalaries(2, null);
        assertEquals(0, response.size());
    }

    @Test
    void getNHighestSalariesShouldHandleParseError() {
        List<Employee> employees = new ArrayList<>();
        Employee employee0 = new Employee(UUID.randomUUID(), "John Doe", "foo", 54, "Software Engineer", "foo@bar.com");
        Employee employee1 = new Employee(UUID.randomUUID(), "John Doe", "foo", 54, "Software Engineer", "foo@bar.com");
        employees.add(employee0);
        employees.add(employee1);

        assertThrows(NumberFormatException.class, () -> employeeService.getNHighestSalaries(2, employees));
    }

    @Test
    void createEmployee() {
        CreateEmployee createEmployee = new CreateEmployee("John Doe", "57000", 54, "Software Engineer");
        Employee employee =
                new Employee(UUID.randomUUID(), "John Doe", "57000", 54, "Software Engineer", "foo@bar.com");

        when(restTemplate.exchange(
                        eq("testEndpoint"),
                        eq(HttpMethod.POST),
                        any(HttpEntity.class),
                        any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(new EmployeeResponse(employee)));

        Employee response = employeeService.createEmployee(createEmployee);
        assertEquals(employee, response);
    }

    @Test
    void createEmployeeShouldHandleApiFailure() {
        CreateEmployee createEmployee = new CreateEmployee("John Doe", "57000", 54, "Software Engineer");
        when(restTemplate.exchange(
                        eq("testEndpoint"),
                        eq(HttpMethod.POST),
                        any(HttpEntity.class),
                        any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(null));

        assertThrows(ExternalApiException.class, () -> employeeService.createEmployee(createEmployee));

        when(restTemplate.exchange(
                        eq("testEndpoint"),
                        eq(HttpMethod.POST),
                        any(HttpEntity.class),
                        any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(new EmployeeResponse(null)));

        assertThrows(ExternalApiException.class, () -> employeeService.createEmployee(createEmployee));
    }

    @Test
    void createEmployeeShouldHandleRateLimit() {
        CreateEmployee createEmployee = new CreateEmployee("John Doe", "57000", 54, "Software Engineer");

        doThrow(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS))
                .when(restTemplate)
                .exchange(
                        eq("testEndpoint"),
                        eq(HttpMethod.POST),
                        any(HttpEntity.class),
                        any(ParameterizedTypeReference.class));
        ExternalApiRateLimitException exception =
                assertThrows(ExternalApiRateLimitException.class, () -> employeeService.createEmployee(createEmployee));
        assertEquals("Failed to create employee. Rate limit exceeded", exception.getMessage());
    }

    @Test
    void createEmployeeShouldHandleHttpErrors() {
        CreateEmployee createEmployee = new CreateEmployee("John Doe", "57000", 54, "Software Engineer");

        doThrow(new HttpClientErrorException(HttpStatus.BAD_GATEWAY))
                .when(restTemplate)
                .exchange(
                        eq("testEndpoint"),
                        eq(HttpMethod.POST),
                        any(HttpEntity.class),
                        any(ParameterizedTypeReference.class));
        ExternalApiException exception =
                assertThrows(ExternalApiException.class, () -> employeeService.createEmployee(createEmployee));
        assertEquals("Failed to create employee.", exception.getMessage());
    }

    @Test
    void deleteEmployeeByName() {
        when(restTemplate.exchange(
                        eq("testEndpoint"),
                        eq(HttpMethod.DELETE),
                        any(HttpEntity.class),
                        any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(Map.of("data", true, "status", "Successfully processed request.")));

        assertTrue(employeeService.deleteEmployeeByName("John Doe"));
    }

    @Test
    void deleteEmployeeByNameShouldHandleFailure() {
        when(restTemplate.exchange(
                        eq("testEndpoint"),
                        eq(HttpMethod.DELETE),
                        any(HttpEntity.class),
                        any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(Map.of("data", false, "status", "Failed to process request.")));

        assertFalse(employeeService.deleteEmployeeByName("John Doe"));
    }

    @Test
    void deleteEmployeeByNameShouldHandleNullBody() {
        when(restTemplate.exchange(
                        eq("testEndpoint"),
                        eq(HttpMethod.DELETE),
                        any(HttpEntity.class),
                        any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(null));

        assertFalse(employeeService.deleteEmployeeByName("John Doe"));
    }

    @Test
    void deleteEmployeeByNameShouldHandleRateLimit() {
        doThrow(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS))
                .when(restTemplate)
                .exchange(
                        eq("testEndpoint"),
                        eq(HttpMethod.DELETE),
                        any(HttpEntity.class),
                        any(ParameterizedTypeReference.class));

        ExternalApiRateLimitException exception = assertThrows(
                ExternalApiRateLimitException.class, () -> employeeService.deleteEmployeeByName("John Doe"));
        assertEquals("Failed to delete employee. Rate limit exceeded", exception.getMessage());
    }

    @Test
    void deleteEmployeeByNameShouldHandleHttpErrors() {
        doThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR))
                .when(restTemplate)
                .exchange(
                        eq("testEndpoint"),
                        eq(HttpMethod.DELETE),
                        any(HttpEntity.class),
                        any(ParameterizedTypeReference.class));

        ExternalApiException exception =
                assertThrows(ExternalApiException.class, () -> employeeService.deleteEmployeeByName("John Doe"));
        assertEquals("Failed to delete employee.", exception.getMessage());
    }

    @Test
    void validateCreateEmployee() {
        CreateEmployee createEmployee = new CreateEmployee("John Doe", "57000", 54, "Software Engineer");
        assertDoesNotThrow(() -> employeeService.validateCreateEmployee(createEmployee));
    }

    @Test
    void validateCreateEmployeeShouldHandleNullName() {
        CreateEmployee createEmployee = new CreateEmployee(null, "57000", 54, "Software Engineer");
        EmployeeValidationError error = assertThrows(
                EmployeeValidationError.class, () -> employeeService.validateCreateEmployee(createEmployee));
        assertTrue(error.getMessage().contains("Name cannot be null or blank."));
    }

    @Test
    void validateCreateEmployeeShouldHandleBlankName() {
        CreateEmployee createEmployee = new CreateEmployee(" ", "57000", 54, "Software Engineer");
        EmployeeValidationError error = assertThrows(
                EmployeeValidationError.class, () -> employeeService.validateCreateEmployee(createEmployee));
        assertTrue(error.getMessage().contains("Name cannot be null or blank."));
    }

    @Test
    void validateCreateEmployeeShouldHandleNullSalary() {
        CreateEmployee createEmployee = new CreateEmployee(" ", null, 54, "Software Engineer");
        EmployeeValidationError error = assertThrows(
                EmployeeValidationError.class, () -> employeeService.validateCreateEmployee(createEmployee));
        assertTrue(error.getMessage().contains("Salary must not be null."));
    }

    @Test
    void validateCreateEmployeeShouldHandleSalaryParseError() {
        CreateEmployee createEmployee = new CreateEmployee(" ", "foo", 54, "Software Engineer");
        EmployeeValidationError error = assertThrows(
                EmployeeValidationError.class, () -> employeeService.validateCreateEmployee(createEmployee));
        assertTrue(error.getMessage().contains("Salary string must parse to an integer. "));
    }

    @Test
    void validateCreateEmployeeShouldHandleNegativeSalary() {
        CreateEmployee createEmployee = new CreateEmployee(" ", "-25", 54, "Software Engineer");
        EmployeeValidationError error = assertThrows(
                EmployeeValidationError.class, () -> employeeService.validateCreateEmployee(createEmployee));
        assertTrue(error.getMessage().contains("Salary must be greater than zero. "));
    }

    @Test
    void validateCreateEmployeeShouldHandleZeroSalary() {
        CreateEmployee createEmployee = new CreateEmployee(" ", "0", 54, "Software Engineer");
        EmployeeValidationError error = assertThrows(
                EmployeeValidationError.class, () -> employeeService.validateCreateEmployee(createEmployee));
        assertTrue(error.getMessage().contains("Salary must be greater than zero. "));
    }

    @Test
    void validateCreateEmployeeShouldHandleNullAge() {
        CreateEmployee createEmployee = new CreateEmployee(" ", "0", null, "Software Engineer");
        EmployeeValidationError error = assertThrows(
                EmployeeValidationError.class, () -> employeeService.validateCreateEmployee(createEmployee));
        assertTrue(error.getMessage().contains("Age must not be null"));
    }

    @Test
    void validateCreateEmployeeShouldHandleAgeRange() {
        EmployeeValidationError error = assertThrows(
                EmployeeValidationError.class,
                () -> employeeService.validateCreateEmployee(new CreateEmployee(" ", "0", 15, "Software Engineer")));
        assertTrue(error.getMessage().contains("Age must be between 16 and 75. "));

        error = assertThrows(
                EmployeeValidationError.class,
                () -> employeeService.validateCreateEmployee(new CreateEmployee(" ", "0", 76, "Software Engineer")));
        assertTrue(error.getMessage().contains("Age must be between 16 and 75. "));
    }

    @Test
    void validateCreateEmployeeShouldHandleNullTitle() {
        CreateEmployee createEmployee = new CreateEmployee(" ", "0", null, null);
        EmployeeValidationError error = assertThrows(
                EmployeeValidationError.class, () -> employeeService.validateCreateEmployee(createEmployee));
        assertTrue(error.getMessage().contains("Title must not be null or blank. "));
    }

    @Test
    void validateCreateEmployeeShouldHandleBlankTitle() {
        CreateEmployee createEmployee = new CreateEmployee(" ", "0", null, " ");
        EmployeeValidationError error = assertThrows(
                EmployeeValidationError.class, () -> employeeService.validateCreateEmployee(createEmployee));
        assertTrue(error.getMessage().contains("Title must not be null or blank. "));
    }
}
