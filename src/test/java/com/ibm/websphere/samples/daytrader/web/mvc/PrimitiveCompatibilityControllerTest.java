package com.ibm.websphere.samples.daytrader.web.mvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class PrimitiveCompatibilityControllerTest {

    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new PrimitiveCompatibilityController()).build();

    @Test
    void servesPingServletHtmlAtTheLegacyPrimitivePath() throws Exception {
        mockMvc.perform(get("/servlet/PingServlet"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Ping Servlet")))
                .andExpect(content().string(containsString("Hit Count: 1")));
    }

    @Test
    void servesPingServletWriterHtmlAtTheLegacyPrimitivePath() throws Exception {
        mockMvc.perform(get("/servlet/PingServletWriter"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Ping Servlet Writer")))
                .andExpect(content().string(containsString("Hit Count: 1")));
    }

    @Test
    void forwardsPingServlet2JspToTheExistingJspSurfaceWithTheExpectedRequestBean() throws Exception {
        mockMvc.perform(get("/servlet/PingServlet2Jsp"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("/PingServlet2Jsp.jsp"))
                .andExpect(request().attribute("ab", org.hamcrest.Matchers.notNullValue()));
    }

        @Test
        void servesLegacyServletPrimitiveCompatibilityRoutes() throws Exception {
        mockMvc.perform(get("/servlet/ExplicitGC"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("ExplicitGC")))
            .andExpect(content().string(containsString("Hit Count: 1")));

        mockMvc.perform(get("/servlet/PingManagedExecutor"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("PingManagedExecutor")));
        }

        @Test
        void servesLegacyEjbPrimitiveCompatibilityRoutes() throws Exception {
        mockMvc.perform(get("/ejb3/PingServlet2Session"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("PingServlet2Session")))
            .andExpect(content().string(containsString("Hit Count: 1")));

        mockMvc.perform(get("/ejb3/PingServlet2TwoPhase"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("PingServlet2TwoPhase")));
        }
}