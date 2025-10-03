package com.reliaquest.api.client;

import com.reliaquest.api.exception.EmployeeApiException;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeResponse;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import java.net.URI;
import java.util.List;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmployeeApiClient {

    private final RestTemplate restTemplate;
    private final RetryRegistry retryRegistry;
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    @Value("${employee.api.base-url}")
    private String baseUrl;

    private <T> T executeWithResilience(String retryName, String circuitBreakerName, Supplier<T> supplier) {
        Retry retry = retryRegistry.retry(retryName);
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(circuitBreakerName);

        Supplier<T> resilientSupplier = () -> circuitBreaker.executeSupplier(
                () -> Retry.decorateSupplier(retry, supplier).get());

        try {
            return resilientSupplier.get();
        } catch (HttpStatusCodeException ex) {
            log.error(
                    "HTTP error while calling {}: status={}, body={}",
                    retryName,
                    ex.getStatusCode(),
                    ex.getResponseBodyAsString());
            throw new EmployeeApiException("Remote API error: " + ex.getStatusCode(), ex);
        } catch (Exception ex) {
            log.error("Error while calling {}: {}", retryName, ex.toString());
            throw new EmployeeApiException("Remote API call failed", ex);
        }
    }

    public List<Employee> getAllEmployees() {
        EmployeeResponse response = restTemplate.getForObject(baseUrl, EmployeeResponse.class);
        return response != null ? response.getData() : List.of();
    }

    public EmployeeResponse getEmployeeById(String id) {
        return executeWithResilience("employeeApiRetry", "employeeApiCircuitBreaker", () -> {
            URI uri = URI.create(baseUrl + "/employees/" + id);
            ResponseEntity<EmployeeResponse> response =
                    restTemplate.exchange(uri, HttpMethod.GET, HttpEntity.EMPTY, EmployeeResponse.class);
            return response.getBody();
        });
    }

    public Employee createEmployee(Employee request) {
        return executeWithResilience("employeeApiRetry", "employeeApiCircuitBreaker", () -> {
            URI uri = URI.create(baseUrl + "/employees");
            ResponseEntity<Employee> response = restTemplate.postForEntity(uri, request, Employee.class);
            return response.getBody();
        });
    }

    public void deleteEmployeeById(String id) {
        executeWithResilience("employeeApiRetry", "employeeApiCircuitBreaker", () -> {
            URI uri = URI.create(baseUrl + "/api/v1/employee/" + id);
            restTemplate.exchange(uri, HttpMethod.DELETE, HttpEntity.EMPTY, Void.class);
            return null;
        });
    }
}
