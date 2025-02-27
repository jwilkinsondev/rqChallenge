package com.reliaquest.api.services;

import com.reliaquest.api.exceptions.EmployeeValidationError;
import com.reliaquest.api.exceptions.ExternalApiException;
import com.reliaquest.api.models.Employee;
import com.reliaquest.api.models.EmployeeListResponse;
import com.reliaquest.api.models.EmployeeResponse;
import com.reliaquest.api.ports.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class EmployeeService
        implements CreateEmployee,
                DeleteEmployeeByName,
                GetAllEmployees,
                GetEmployeesByNameSearch,
                GetEmployeeById,
                GetNHighestSalaries {
    private final String employeesEndpoint;
    private final RestTemplate restTemplate;

    public EmployeeService(@Value("${endpoints.employees}") String employeesEndpoint, RestTemplate restTemplate) {
        this.employeesEndpoint = employeesEndpoint;
        this.restTemplate = restTemplate;
    }

    // todo add backoff strategy for endpoints

    @Override
    public List<Employee> getAllEmployees() {
        try {
            ResponseEntity<EmployeeListResponse> response = restTemplate.exchange(
                    employeesEndpoint, HttpMethod.GET, null, new ParameterizedTypeReference<>() {});
            return response.getBody() != null ? response.getBody().data() : List.of();
        } catch (HttpClientErrorException e) {
            if (HttpStatus.TOO_MANY_REQUESTS == e.getStatusCode()) {
                throw new ExternalApiException("Failed to retrieve employees. Rate limit exceeded");
            } else {
                throw new ExternalApiException("Failed to retrieve employees");
            }
        }
    }

    @Override
    public List<Employee> getEmployeesByNameSearch(String name) {
        List<Employee> employees = getAllEmployees();
        return employees.stream()
                .filter(employee -> employee.name().toLowerCase().contains(name.toLowerCase()))
                .toList();
    }

    @Override
    public Optional<Employee> getEmployeeById(String employeeId) {
        try {
            String url = employeesEndpoint + "/" + employeeId;
            ResponseEntity<EmployeeResponse> response =
                    restTemplate.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<>() {});
            if (response.getBody() == null || response.getBody().data() == null) {
                return Optional.empty();
            }
            return Optional.of(response.getBody().data());
        } catch (HttpClientErrorException e) {
            if (HttpStatus.TOO_MANY_REQUESTS == e.getStatusCode()) {
                throw new ExternalApiException("Failed to retrieve employee. Rate limit exceeded");
            } else {
                throw new ExternalApiException("Failed to retrieve employee");
            }
        }
    }

    @Override
    public List<Employee> getNHighestSalaries(int n, List<Employee> employees) throws NumberFormatException {
        if (n <= 0 || employees == null) {
            return List.of();
        }
        PriorityQueue<Employee> pq = new PriorityQueue<>(Comparator.comparingInt(e -> Integer.parseInt(e.salary())));

        for (Employee employee : employees) {
            pq.offer(employee);
            if (pq.size() > n) {
                pq.poll();
            }
        }
        ArrayList<Employee> highSalaries = new ArrayList<>(pq);
        Collections.reverse(highSalaries);
        return highSalaries;
    }

    @Override
    public Employee createEmployee(com.reliaquest.api.models.CreateEmployee createEmployee) {

        try {
            HttpEntity<com.reliaquest.api.models.CreateEmployee> request = new HttpEntity<>(createEmployee);
            ResponseEntity<EmployeeResponse> response = restTemplate.exchange(
                    employeesEndpoint, HttpMethod.POST, request, new ParameterizedTypeReference<>() {});
            if (response.getBody() == null || response.getBody().data() == null) {
                throw new ExternalApiException("Failed to create employee");
            }
            return response.getBody().data();
        } catch (HttpClientErrorException e) {
            if (HttpStatus.TOO_MANY_REQUESTS == e.getStatusCode()) {
                throw new ExternalApiException("Failed to create employee. Rate limit exceeded");
            } else {
                throw new ExternalApiException("Failed to create employee.");
            }
        }
    }

    @Override
    public boolean deleteEmployeeByName(String name) {
        // todo flush out create and delete logic and error handling and rate limiting
        try {
            Map<String, String> requestBody = Collections.singletonMap("name", name);
            HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody);
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    employeesEndpoint, HttpMethod.DELETE, request, new ParameterizedTypeReference<>() {});
            if (response.getBody() == null) {
                return false;
            }
            return (Boolean) response.getBody().get("data");
        } catch (HttpClientErrorException e) {
            if (HttpStatus.TOO_MANY_REQUESTS == e.getStatusCode()) {
                throw new ExternalApiException("Failed to delete employee. Rate limit exceeded");
            } else {
                throw new ExternalApiException("Failed to delete employee.");
            }
        }
    }

    public void validateCreateEmployee(com.reliaquest.api.models.CreateEmployee createEmployee)
            throws EmployeeValidationError {
        String message = "";
        if (createEmployee.name() == null || createEmployee.name().isBlank()) {
            message += "Name cannot be null or blank. ";
        }
        if (createEmployee.salary() == null) {
            message += "Salary must not be null. ";
        } else {
            try {
                int salary = Integer.parseInt(createEmployee.salary());
                if (salary <= 0) {
                    message += "Salary must be greater than zero. ";
                }
            } catch (NumberFormatException e) {
                message += "Salary string must parse to an integer. ";
            }
        }
        if (createEmployee.age() == null) {
            message += "Age must not be null. ";
        } else if (createEmployee.age() < 16 || createEmployee.age() > 75) {
            message += "Age must be between 16 and 75. ";
        }
        if (createEmployee.title() == null || createEmployee.title().isBlank()) {
            message += "Title must not be null or blank. ";
        }
        if (!message.isBlank()) {
            throw new EmployeeValidationError(message);
        }
    }
}
