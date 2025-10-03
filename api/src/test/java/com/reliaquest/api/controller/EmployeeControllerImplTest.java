package com.reliaquest.api.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reliaquest.api.exception.EmployeeNotFoundException;
import com.reliaquest.api.model.CreateEmployeeRequest;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.service.EmployeeService;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(EmployeeControllerImpl.class)
class EmployeeControllerImplTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService service;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmployeeControllerImpl employeeController;

    @Test
    void testGetAllEmployees() throws Exception {
        when(service.getAllEmployees())
                .thenReturn(Arrays.asList(new Employee("1", "John", 50000, 28, "Dev", "john@test.com")));

        mockMvc.perform(get("/api/v1/employee"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].employee_name").value("John"));
    }

    @Test
    void testGetEmployeeById_Found() throws Exception {
        Employee emp = new Employee("123", "Alice", 60000, 30, "Engineer", "alice@test.com");
        when(service.getEmployeeById("123")).thenReturn(emp);

        mockMvc.perform(get("/api/v1/employee/123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employee_name").value("Alice"));
    }

    @Test
    void testGetEmployeeById_NotFound() throws Exception {
        when(service.getEmployeeById("999")).thenThrow(new EmployeeNotFoundException("Not found"));

        mockMvc.perform(get("/api/v1/employee/999")).andExpect(status().isNotFound());
    }

    @Test
    void testGetEmployeeBySearchName() throws Exception {
        List<Employee> mockList =
                Arrays.asList(new Employee("1", "Ben Langosh", 50000, 30, "Engineer", "ben@test.com"));
        Mockito.when(service.getEmployeesByNameSearch("Ben Langosh")).thenReturn(mockList);

        mockMvc.perform(get("/api/v1/employee/search/{name}", "Ben Langosh").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].employee_name").value("Ben Langosh"))
                .andExpect(jsonPath("$[0].employee_salary").value(50000));
    }

    @Test
    void testCreateEmployee() throws Exception {
        Employee emp = new Employee("10", "Eve", 70000, 27, "QA", "eve@test.com");
        when(service.createEmployee(any(CreateEmployeeRequest.class))).thenReturn(emp);

        CreateEmployeeRequest request = CreateEmployeeRequest.builder()
                .name("Eve")
                .salary(70000)
                .age(27)
                .title("QA")
                .email("eve@test.com")
                .build();

        ResponseEntity<Employee> response = employeeController.createEmployee(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Eve", response.getBody().getName());
    }
}
