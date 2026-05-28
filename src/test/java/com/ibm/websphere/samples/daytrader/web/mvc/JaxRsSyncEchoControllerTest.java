package com.ibm.websphere.samples.daytrader.web.mvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class JaxRsSyncEchoControllerTest {

    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new JaxRsSyncEchoController()).build();

    @Test
    void echoesTextAtThePreservedJaxRsPath() throws Exception {
        mockMvc.perform(get("/jaxrs/sync/echoText").param("input", "hello-boot3"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(content().string("hello-boot3"));
    }

    @Test
    void echoesJsonAtThePreservedJaxRsPath() throws Exception {
        mockMvc.perform(post("/jaxrs/sync/echoJSON")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"prop0001\":\"alpha\",\"prop0016\":\"omega\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{\"prop0001\":\"alpha\",\"prop0016\":\"omega\"}"));
    }

    @Test
    void echoesXmlAtThePreservedJaxRsPath() throws Exception {
        String payload = "<XMLObject><prop0001>alpha</prop0001><x>omega</x></XMLObject>";

        mockMvc.perform(post("/jaxrs/sync/echoXML")
                        .contentType(MediaType.APPLICATION_XML)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML))
                .andExpect(content().xml(payload));
    }
}