package com.reliaquest.api.service;

import com.reliaquest.api.client.EmployeeApiClient;
import com.reliaquest.api.exception.EmployeeNotFoundException;
import com.reliaquest.api.model.CreateEmployeeRequest;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeResponse;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Data
@Builder
@Slf4j
public class DefaultEmployeeService implements EmployeeService {

    private final EmployeeApiClient client;

    @Override
    public List<Employee> getAllEmployees() {
        return client.getAllEmployees();
    }

    @Override
    public List<Employee> getEmployeesByNameSearch(String name) {
        return client.getAllEmployees().stream()
                .filter(emp -> emp.getEmployee_name().toLowerCase().contains(name.toLowerCase()))
                .collect(Collectors.toList());
    }

    @Override
    public Employee getEmployeeById(String id) {
        log.info("Fetching employee with ID {}", id);
        EmployeeResponse empResponse = client.getEmployeeById(id);
        if (empResponse == null
                || empResponse.getData() == null
                || empResponse.getData().isEmpty()) {
            throw new EmployeeNotFoundException("Employee not found with ID: " + id);
        }

        Employee emp = empResponse.getData().get(0);
        log.debug("Employee fetched: {}", emp);
        return emp;
    }

    @Override
    public Integer getHighestSalaryOfEmployees() {
        return client.getAllEmployees().stream()
                .map(Employee::getEmployee_salary)
                .max(Integer::compareTo)
                .orElse(0);
    }

    @Override
    public List<String> getTopTenHighestEarningEmployeeNames() {
        return client.getAllEmployees().stream()
                .sorted(Comparator.comparingInt(Employee::getEmployee_salary).reversed())
                .limit(10)
                .map(Employee::getEmployee_name)
                .collect(Collectors.toList());
    }

    @Override
    public Employee createEmployee(CreateEmployeeRequest request) {
        Employee employee = new Employee();
        employee.setEmployee_name(request.getName());
        employee.setEmployee_age(request.getAge());
        employee.setEmployee_title(request.getTitle());
        employee.setEmployee_email(request.getEmail());
        employee.setEmployee_salary(request.getSalary());

        return client.createEmployee(employee);
    }

    @Override
    public String deleteEmployeeById(String id) {
        Employee emp = getEmployeeById(id);
        client.deleteEmployeeById((emp.getId().toString()));
        return emp.getEmployee_name();
    }
}
