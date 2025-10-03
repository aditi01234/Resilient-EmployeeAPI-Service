package com.reliaquest.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reliaquest.api.model.CreateEmployeeRequest;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.service.EmployeeService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/employee")
@RequiredArgsConstructor
@Slf4j
public class EmployeeControllerImpl implements IEmployeeController {

    private final EmployeeService service;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    @GetMapping
    public ResponseEntity<List<Employee>> getAllEmployees() {
        log.info("GET /api/v1/employee called");
        List<Employee> employees = service.getAllEmployees();
        log.debug("Fetched {} employees", employees.size());
        return ResponseEntity.ok(employees);
    }

    @Override
    @GetMapping("/search/{name}")
    public ResponseEntity<List<Employee>> getEmployeesByNameSearch(@PathVariable String name) {
        log.info("GET /api/v1/employee/search/{} called", name);
        List<Employee> employees = service.getEmployeesByNameSearch(name);
        log.debug("Found {} employees matching '{}'", employees.size(), name);
        return ResponseEntity.ok(employees);
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<Employee> getEmployeeById(@PathVariable String id) {
        log.info("GET /api/v1/employee/{} called", id);
        Employee emp = service.getEmployeeById(id);
        log.debug("Fetched employee: {}", emp);
        return ResponseEntity.ok(emp);
    }

    @Override
    @GetMapping("/highest-salary")
    public ResponseEntity<Integer> getHighestSalaryOfEmployees() {
        log.info("GET /api/v1/employee/highest-salary called");
        Integer highest = service.getHighestSalaryOfEmployees();
        log.debug("Highest salary: {}", highest);
        return ResponseEntity.ok(highest);
    }

    @Override
    @GetMapping("/top-ten-highest-earning")
    public ResponseEntity<List<String>> getTopTenHighestEarningEmployeeNames() {
        log.info("GET /api/v1/employee/top-ten-highest-earning called");
        List<String> topTen = service.getTopTenHighestEarningEmployeeNames();
        log.debug("Top earning employees: {}", topTen);
        return ResponseEntity.ok(topTen);
    }

    @Override
    public ResponseEntity<Employee> createEmployee(@RequestBody Object request) {
        log.info("POST /api/v1/employee called with request: {}", request);
        CreateEmployeeRequest createRequest = objectMapper.convertValue(request, CreateEmployeeRequest.class);

        Employee response = service.createEmployee(createRequest);
        log.debug("Created employee: {}", response);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteEmployeeById(@PathVariable String id) {
        log.info("DELETE /api/v1/employee/{} called", id);
        String deletedName = service.deleteEmployeeById(id);
        log.debug("Deleted employee: {}", deletedName);
        return ResponseEntity.noContent()
                .header("Deleted-Employee", deletedName)
                .build();
    }
}
