package com.phep.casesvc;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.hamcrest.Matchers.startsWith;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
public class CaseApiIT {

    @Container
    static PostgreSQLContainer<?> pg = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("phep")
            .withUsername("phep")
            .withPassword("phep");

    @DynamicPropertySource
    static void dbProps(DynamicPropertyRegistry reg) {
        reg.add("spring.datasource.url", pg::getJdbcUrl);
        reg.add("spring.datasource.username", pg::getUsername);
        reg.add("spring.datasource.password", pg::getPassword);
    }

    @Autowired
    MockMvc mockMvc;

    @Test
    void create_case_returns_201_and_location() throws Exception {
        var createJson = """
                        {"firstName": "Ann","lastName": "Lee","dob":"1990-04-11"}
                        """;
        var location =  mockMvc.perform(post("/cases")
                .contentType("application/json")
                .content(createJson))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", startsWith("/cases/")))
                .andExpect(jsonPath("$.firstName").value("Ann"))
                .andExpect(jsonPath("$.lastName").value("Lee"))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andReturn();


        String id = com.jayway.jsonpath.JsonPath.read(
                location.getResponse().getContentAsString(), "$.caseId");

        // fetch
        mockMvc.perform(get("/cases/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Ann"))
                .andExpect(jsonPath("$.status").value("OPEN"));

        mockMvc.perform(patch("/cases/" + id)
                        .contentType("application/json")
                        .content("""
                                  {"status":"CLOSED"}
                                 """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"));


    }
}
