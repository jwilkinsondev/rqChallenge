package com.reliaquest.api.ports;

import com.reliaquest.api.models.Employee;

import java.util.List;

public interface GetEmployeesByNameSearch {
    List<Employee> getEmployeesByNameSearch(String name);
}
