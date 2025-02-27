package com.reliaquest.api.controller;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.TOO_MANY_REQUESTS;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reliaquest.api.exceptions.EmployeeValidationError;
import com.reliaquest.api.exceptions.ExternalApiRateLimitException;
import com.reliaquest.api.models.CreateEmployee;
import com.reliaquest.api.models.Employee;
import com.reliaquest.api.services.EmployeeService;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Controller
@SuppressWarnings({"rawtypes", "Necessary to match interface"})
public class EmployeeController implements IEmployeeController {
    public static final Logger logger = LoggerFactory.getLogger(EmployeeController.class);
    private final EmployeeService employeeService;
    private final ObjectMapper objectMapper;

    @Autowired
    public EmployeeController(EmployeeService employeeService, ObjectMapper objectMapper) {
        this.employeeService = employeeService;
        this.objectMapper = objectMapper;
    }

    @Override
    public ResponseEntity<List> getAllEmployees() {
        try {
            return ResponseEntity.ok(employeeService.getAllEmployees());
        } catch (ExternalApiRateLimitException e) {
            logger.debug(e.getMessage());
            return ResponseEntity.status(TOO_MANY_REQUESTS).build();
        } catch (Exception e) {
            logger.debug("Error getting all employees", e);
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<List> getEmployeesByNameSearch(String searchString) {
        try {
            return ResponseEntity.ok(employeeService.getEmployeesByNameSearch(searchString));
        } catch (ExternalApiRateLimitException e) {
            logger.debug(e.getMessage());
            return ResponseEntity.status(TOO_MANY_REQUESTS).build();
        } catch (Exception e) {
            logger.debug("Error searching employees", e);
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity getEmployeeById(String id) {
        try {
            Optional<Employee> employee = employeeService.getEmployeeById(id);
            if (employee.isEmpty()) {
                return ResponseEntity.notFound().build();
            } else {
                return ResponseEntity.ok(employee.get());
            }
        } catch (ExternalApiRateLimitException e) {
            logger.debug(e.getMessage());
            return ResponseEntity.status(TOO_MANY_REQUESTS).build();
        } catch (Exception e) {
            logger.debug("Error getting employee by id", e);
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<Integer> getHighestSalaryOfEmployees() {
        try {
            List<Employee> employees = employeeService.getAllEmployees();
            List<Employee> highestPaidEmployees = employeeService.getNHighestSalaries(1, employees);
            int highestSalary = Integer.parseInt(highestPaidEmployees.get(0).salary());
            return ResponseEntity.ok(highestSalary);
        } catch (ExternalApiRateLimitException e) {
            logger.debug(e.getMessage());
            return ResponseEntity.status(TOO_MANY_REQUESTS).build();
        } catch (Exception e) {
            logger.debug("Error getting highest salary of employees", e);
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<List<String>> getTopTenHighestEarningEmployeeNames() {
        try {
            List<Employee> employees = employeeService.getAllEmployees();
            List<Employee> highestPaidEmployees = employeeService.getNHighestSalaries(10, employees);
            List<String> names =
                    highestPaidEmployees.stream().map(Employee::name).toList();
            return ResponseEntity.ok(names);
        } catch (ExternalApiRateLimitException e) {
            logger.debug(e.getMessage());
            return ResponseEntity.status(TOO_MANY_REQUESTS).build();
        } catch (Exception e) {
            logger.debug("Error getting top ten highest earning employee names", e);
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity createEmployee(Object employeeInput) {
        try {
            CreateEmployee createEmployee = objectMapper.convertValue(employeeInput, CreateEmployee.class);
            try {
                employeeService.validateCreateEmployee(createEmployee);
            } catch (EmployeeValidationError e) {
                return ResponseEntity.badRequest().build();
            }
            Employee employee = employeeService.createEmployee(createEmployee);
            URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(employee.id())
                    .toUri();
            return ResponseEntity.created(location).build();
        } catch (ExternalApiRateLimitException e) {
            logger.debug(e.getMessage());
            return ResponseEntity.status(TOO_MANY_REQUESTS).build();
        } catch (Exception e) {
            logger.debug("Error creating employee", e);
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<String> deleteEmployeeById(String id) {
        try {
            Employee employee = employeeService.getEmployeeById(id).orElse(null);
            if (employee == null) {
                return ResponseEntity.notFound().build();
            }
            if (employee.name().isBlank()) {
                return ResponseEntity.badRequest().body("Employee name is blank");
            }
            if (employeeService.deleteEmployeeByName(employee.name())) {
                return ResponseEntity.ok(employee.name());
            } else {
                return ResponseEntity.internalServerError().build();
            }
        } catch (ExternalApiRateLimitException e) {
            logger.debug(e.getMessage());
            return ResponseEntity.status(TOO_MANY_REQUESTS).build();
        } catch (Exception e) {
            logger.debug("Error deleting employee by id", e);
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).build();
        }
    }
}
