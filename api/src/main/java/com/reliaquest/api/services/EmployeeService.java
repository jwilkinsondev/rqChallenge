package com.reliaquest.api.services;

import com.reliaquest.api.exceptions.ExternalApiException;
import com.reliaquest.api.models.Employee;
import com.reliaquest.api.models.EmployeeResponse;
import com.reliaquest.api.ports.GetAllEmployees;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class EmployeeService implements GetAllEmployees {
    private final String employeesEndpoint;
    private final RestTemplate restTemplate;

    public EmployeeService(@Value("${endpoints.employees}") String employeesEndpoint, RestTemplate restTemplate) {
        this.employeesEndpoint = employeesEndpoint;
        this.restTemplate = restTemplate;
    }

    @Override
    public List<Employee> getAllEmployees() {
        try {
            ResponseEntity<EmployeeResponse> response = restTemplate.exchange(
                    employeesEndpoint, HttpMethod.GET, null, new ParameterizedTypeReference<>() {});
            return response.getBody() != null ? response.getBody().data() : List.of();
        } catch (HttpClientErrorException e) {
            // todo add backoff strategy
            if (HttpStatus.TOO_MANY_REQUESTS == e.getStatusCode()) {
                throw new ExternalApiException("Failed to retrieve employees. Rate limit exceeded");
            } else {
                throw new ExternalApiException("Failed to retrieve employees");
            }
        }
    }
}
