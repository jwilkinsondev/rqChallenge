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

    //    todo handle rate limit response for all these methods
    //    todo return bad request status as appropriate
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

    @Override
    public ResponseEntity<Integer> getHighestSalaryOfEmployees() {
        try {
            List<Employee> employees = employeeService.getAllEmployees();
            List<Employee> highestPaidEmployees = employeeService.getNHighestSalaries(1, employees);
            int highestSalary = Integer.parseInt(highestPaidEmployees.get(0).salary());
            return ResponseEntity.ok(highestSalary);
        } catch (Exception e) {
            logger.error("Error getting highest salary of employees", e);
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
        } catch (Exception e) {
            logger.error("Error getting top ten highest earning employee names", e);
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).build();
        }
    }

    //    todo implement the rest of the methods
    @Override
    public ResponseEntity createEmployee(Object employeeInput) {
        return null;
    }

    @Override
    public ResponseEntity<String> deleteEmployeeById(String id) {
        return null;
    }
}
