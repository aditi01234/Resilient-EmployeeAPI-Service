package com.reliaquest.api.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeResponse;
import com.reliaquest.api.util.TestUtils;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

class EmployeeClientTest {

    private RestTemplate restTemplate;
    private RetryRegistry retryRegistry;
    private CircuitBreakerRegistry circuitBreakerRegistry;
    private EmployeeApiClient client;

    @BeforeEach
    void setUp() {
        restTemplate = Mockito.mock(RestTemplate.class);
        retryRegistry = RetryRegistry.ofDefaults();
        circuitBreakerRegistry = CircuitBreakerRegistry.ofDefaults();
        client = new EmployeeApiClient(restTemplate, retryRegistry, circuitBreakerRegistry);
        TestUtils.setField(client, "baseUrl", "http://localhost:8081");
    }

    @Test
    void getEmployeeById_retriesOn429AndSucceeds() {
        EmployeeResponse resp = new EmployeeResponse();
        Employee emp = new Employee();
        emp.setEmployee_name("John");
        emp.setId("1");
        resp.setData(List.of(emp));
        when(restTemplate.exchange(
                        URI.create("http://localhost:8081/employees/1"),
                        HttpMethod.GET,
                        HttpEntity.EMPTY,
                        EmployeeResponse.class))
                .thenThrow(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS))
                .thenReturn(new ResponseEntity<>(resp, HttpStatus.OK));
        EmployeeResponse result = client.getEmployeeById("1");
        assertEquals("John", result.getData().get(0).getEmployee_name());
        verify(restTemplate, times(2))
                .exchange(
                        URI.create("http://localhost:8081/employees/1"),
                        HttpMethod.GET,
                        HttpEntity.EMPTY,
                        EmployeeResponse.class);
    }

    @Test
    void getEmployeeById_failsAfterMaxRetries() {
        when(restTemplate.exchange(
                        any(URI.class),
                        any(HttpMethod.class),
                        any(HttpEntity.class),
                        Mockito.<Class<EmployeeResponse>>any()))
                .thenThrow(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS));
        assertThrows(com.reliaquest.api.exception.EmployeeApiException.class, () -> client.getEmployeeById("1"));
    }
}
