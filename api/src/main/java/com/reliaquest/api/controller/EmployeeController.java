package com.reliaquest.api.controller;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import com.reliaquest.api.models.Employee;
import com.reliaquest.api.services.EmployeeService;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class EmployeeController implements IEmployeeController {
    public static final Logger logger = LoggerFactory.getLogger(EmployeeController.class);
    private final EmployeeService employeeService;

    @Autowired
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @Override
    public ResponseEntity<List> getAllEmployees() {
        try {
            return ResponseEntity.ok(employeeService.getAllEmployees());
        } catch (Exception e) {
            logger.error("Error getting all employees", e);
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<List> getEmployeesByNameSearch(String searchString) {
        try {
            return ResponseEntity.ok(employeeService.getEmployeesByNameSearch(searchString));
        } catch (Exception e) {
            logger.error("Error searching employees", e);
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
        } catch (Exception e) {
            logger.error("Error getting employee by id", e);
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).build();
        }
    }

    //    todo implement the rest of the methods
    @Override
    public ResponseEntity<Integer> getHighestSalaryOfEmployees() {
        return null;
    }

    @Override
    public ResponseEntity<List<String>> getTopTenHighestEarningEmployeeNames() {
        return null;
    }

    @Override
    public ResponseEntity createEmployee(Object employeeInput) {
        return null;
    }

    @Override
    public ResponseEntity<String> deleteEmployeeById(String id) {
        return null;
    }
}
