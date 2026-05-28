package com.ibm.websphere.samples.daytrader.web.mvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class TradeScenarioControllerTest {

    @Test
    void respondsToNullScenarioProbeWithoutDependingOnTheTradingSurface() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new TradeScenarioController()).build();

        mockMvc.perform(get("/scenario").param("action", "n"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Hello")));
    }
}