package com.phep.casesvc;

import com.phep.casesvc.testsupport.DbIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CaseApiIT extends DbIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void create_case_returns_201_and_location() throws Exception {
        String createJson = """
        {"firstName":"Ann","lastName":"Lee","dob":"1990-04-11"}
    """;

        var result = mockMvc.perform(post("/cases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", startsWith("/cases/")))
                .andExpect(jsonPath("$.firstName").value("Ann"))
                .andExpect(jsonPath("$.lastName").value("Lee"))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andReturn();

        String id = com.jayway.jsonpath.JsonPath.read(
                result.getResponse().getContentAsString(), "$.caseId");

        mockMvc.perform(get("/cases/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Ann"))
                .andExpect(jsonPath("$.status").value("OPEN"));

        mockMvc.perform(patch("/cases/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {"status":"CLOSED"}
            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"));
    }
}
