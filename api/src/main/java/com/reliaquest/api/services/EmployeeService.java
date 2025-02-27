package com.reliaquest.api.services;

import com.reliaquest.api.exceptions.ExternalApiException;
import com.reliaquest.api.models.Employee;
import com.reliaquest.api.models.EmployeeListResponse;
import com.reliaquest.api.models.EmployeeResponse;
import com.reliaquest.api.ports.GetAllEmployees;
import com.reliaquest.api.ports.GetEmployeeById;
import com.reliaquest.api.ports.GetEmployeesByNameSearch;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class EmployeeService implements GetAllEmployees, GetEmployeesByNameSearch, GetEmployeeById {
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
}
