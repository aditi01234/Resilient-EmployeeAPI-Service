package com.reliaquest.api.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.reliaquest.api.client.EmployeeApiClient;
import com.reliaquest.api.exception.EmployeeNotFoundException;
import com.reliaquest.api.model.CreateEmployeeRequest;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeResponse;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DefaultEmployeeServiceTest {

    @Mock
    private EmployeeApiClient client;

    @InjectMocks
    private DefaultEmployeeService service;

    @Test
    void testGetAllEmployees() {
        List<Employee> mockList = Arrays.asList(
                new Employee("1", "Alice", 50000, 30, "Engineer", "alice@test.com"),
                new Employee("2", "Bob", 70000, 35, "Manager", "bob@test.com"));
        when(client.getAllEmployees()).thenReturn(mockList);
        List<Employee> result = service.getAllEmployees();
        assertEquals(2, result.size());
        assertEquals("Alice", result.get(0).getEmployee_name());
    }

    @Test
    void testGetEmployeeById_Found() {
        Employee emp = new Employee("123", "Charlie", 80000, 40, "Lead", "charlie@test.com");
        EmployeeResponse resp = new EmployeeResponse();
        resp.setData(List.of(emp));
        resp.setStatus("success");
        when(client.getEmployeeById("123")).thenReturn(resp);

        Employee result = service.getEmployeeById("123");
        assertEquals("Charlie", result.getEmployee_name());
    }

    @Test
    void testGetEmployeeById_NotFound() {
        when(client.getEmployeeById("999")).thenReturn(null);
        assertThrows(EmployeeNotFoundException.class, () -> service.getEmployeeById("999"));
    }

    @Test
    void testGetHighestSalaryOfEmployees() {
        List<Employee> mockList = Arrays.asList(
                new Employee("1", "Alice", 50000, 30, "Engineer", "alice@test.com"),
                new Employee("2", "Bob", 120000, 35, "Manager", "bob@test.com"));

        when(client.getAllEmployees()).thenReturn(mockList);
        Integer highest = service.getHighestSalaryOfEmployees();
        assertEquals(120000, highest);
    }

    @Test
    void testGetTopTenHighestEarningEmployeeNames() {
        List<Employee> mockList = Arrays.asList(
                new Employee("1", "Alice", 50000, 30, "Engineer", "alice@test.com"),
                new Employee("2", "Bob", 70000, 35, "Manager", "bob@test.com"),
                new Employee("3", "Charlie", 120000, 40, "Lead", "charlie@test.com"));

        when(client.getAllEmployees()).thenReturn(mockList);
        List<String> top = service.getTopTenHighestEarningEmployeeNames();
        assertEquals(3, top.size());
        assertEquals("Charlie", top.get(0));
    }

    @Test
    void testCreateEmployee() {
        CreateEmployeeRequest req = new CreateEmployeeRequest();
        req.setName("Daisy");
        req.setSalary(90000);
        req.setAge(32);
        req.setTitle("Analyst");

        Employee emp = new Employee("10", "Daisy", 90000, 32, "Analyst", "daisy@test.com");
        when(client.createEmployee(any(Employee.class))).thenReturn(emp);
        Employee result = service.createEmployee(req);
        assertNotNull(result);
        assertEquals("Daisy", result.getEmployee_name());
        verify(client, times(1)).createEmployee(any(Employee.class));
    }

    @Test
    void testDeleteEmployeeById() {
        Employee emp = new Employee("55", "Eve", 60000, 29, "Dev", "eve@test.com");
        EmployeeResponse resp = new EmployeeResponse();
        resp.setData(List.of(emp));
        when(client.getEmployeeById("55")).thenReturn(resp);
        String result = service.deleteEmployeeById("55");
        assertEquals("Eve", result);
    }
}
