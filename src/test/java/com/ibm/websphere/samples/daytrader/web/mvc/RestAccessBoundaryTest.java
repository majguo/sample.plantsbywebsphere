package com.ibm.websphere.samples.daytrader.web.mvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.websphere.samples.daytrader.application.TradeServicesFacade;
import com.ibm.websphere.samples.daytrader.entities.QuoteDataBean;
import com.ibm.websphere.samples.daytrader.streaming.StreamingHub;

@ExtendWith(MockitoExtension.class)
class RestAccessBoundaryTest {

    @Mock
    private TradeServicesFacade tradeServicesFacade;

    @Mock
    private StreamingHub streamingHub;

    private final CompatibilitySessionFacade sessionFacade = new CompatibilitySessionFacade();

    private MockMvc mockMvc() {
        return MockMvcBuilders.standaloneSetup(
                        new QuoteRestController(tradeServicesFacade),
                        new BroadcastEventsController(streamingHub))
                .setMessageConverters(new MappingJackson2HttpMessageConverter(new ObjectMapper()))
                .addInterceptors(new CompatibilitySessionAccessInterceptor(sessionFacade, SessionAccessRequirement.AUTHENTICATED))
                .build();
    }

    private MockHttpSession authenticatedSession() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("uidBean", "uid:1");
        return session;
    }

    @Test
    void rejectsAnonymousQuoteRequests() throws Exception {
        MockMvc mockMvc = mockMvc();

        mockMvc.perform(get("/rest/quotes/s:0"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/rest/quotes")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("symbols", "s:0"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(tradeServicesFacade);
    }

    @Test
    void rejectsAnonymousBroadcastSubscriptionRequests() throws Exception {
        MockMvc mockMvc = mockMvc();

        mockMvc.perform(get("/rest/broadcastevents"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(streamingHub);
    }

    @Test
    void allowsAuthenticatedQuoteRequests() throws Exception {
        MockMvc mockMvc = mockMvc();
        when(tradeServicesFacade.getQuote("s:0")).thenReturn(
                new QuoteDataBean("s:0", "Sample", 1d, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, 0d));

        mockMvc.perform(get("/rest/quotes/s:0").session(authenticatedSession()))
                .andExpect(status().isOk());

        verify(tradeServicesFacade).getQuote("s:0");
    }

    @Test
    void allowsAuthenticatedBroadcastSubscriptionRequests() throws Exception {
        MockMvc mockMvc = mockMvc();
        when(streamingHub.registerBroadcastEmitter()).thenReturn(new org.springframework.web.servlet.mvc.method.annotation.SseEmitter());

        mockMvc.perform(get("/rest/broadcastevents").session(authenticatedSession()))
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted());

        verify(streamingHub).registerBroadcastEmitter();
    }
}