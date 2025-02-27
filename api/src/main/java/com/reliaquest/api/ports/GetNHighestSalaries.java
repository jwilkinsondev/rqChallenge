package com.reliaquest.api.ports;

import com.reliaquest.api.models.Employee;
import java.util.List;

public interface GetNHighestSalaries {
    List<Employee> getNHighestSalaries(int n, List<Employee> employees);
}
