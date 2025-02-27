package com.reliaquest.api.ports;

import com.reliaquest.api.models.Employee;
import java.util.Optional;

public interface GetEmployeeById {
    Optional<Employee> getEmployeeById(String employeeId);
}
