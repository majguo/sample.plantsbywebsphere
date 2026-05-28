package com.ibm.websphere.samples.daytrader.web.mvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import com.ibm.websphere.samples.daytrader.config.RuntimeSettingsService;
import com.ibm.websphere.samples.daytrader.impl.direct.TradeDirectDBUtils;

@ExtendWith(MockitoExtension.class)
class TradeConfigControllerTest {

    @Mock
    private RuntimeSettingsService runtimeSettings;

    @Mock
    private TradeDirectDBUtils dbUtils;

    private MockMvc mockMvc() {
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/");
        viewResolver.setSuffix(".jsp");
        return MockMvcBuilders.standaloneSetup(new TradeConfigController(runtimeSettings, dbUtils))
                .setViewResolvers(viewResolver)
                .build();
    }

    @Test
    void showsCurrentConfigurationWhenNoActionIsProvided() throws Exception {
        MockMvc mockMvc = mockMvc();

        mockMvc.perform(get("/config"))
                .andExpect(status().isOk())
                .andExpect(view().name("config"))
                .andExpect(model().attributeExists("tradeConfig"))
                .andExpect(model().attributeExists("status"));

        verifyNoInteractions(runtimeSettings, dbUtils);
    }

    @Test
    void updatesMutableRuntimeSettingsThroughTheBootService() throws Exception {
        MockMvc mockMvc = mockMvc();

        mockMvc.perform(post("/config")
                        .param("action", "updateConfig")
                        .param("OrderProcessingMode", "1")
                        .param("WebInterface", "2")
                        .param("MaxUsers", "321")
                        .param("MaxQuotes", "654")
                        .param("marketSummaryInterval", "9")
                        .param("primIterations", "4")
                        .param("ListQuotePriceChangeFrequency", "30")
                        .param("EnablePublishQuotePriceChange", "on")
                        .param("EnableLongRun", "on")
                        .param("DisplayOrderAlerts", "on"))
                .andExpect(status().isOk())
                .andExpect(view().name("config"));

        verify(runtimeSettings).setOrderProcessingMode(1);
        verify(runtimeSettings).setWebInterface(2);
        verify(runtimeSettings).setMaxUsers(321);
        verify(runtimeSettings).setMaxQuotes(654);
        verify(runtimeSettings).setMarketSummaryInterval(9);
        verify(runtimeSettings).setPrimIterations(4);
        verify(runtimeSettings).setListQuotePriceChangeFrequency(30);
        verify(runtimeSettings).setPublishQuotePriceChange(true);
        verify(runtimeSettings).setLongRun(true);
        verify(runtimeSettings).setDisplayOrderAlerts(true);
    }

    @Test
    void buildDbReRendersTheConfigSurfaceAfterRunningTheAdminUtility() throws Exception {
        MockMvc mockMvc = mockMvc();
        when(runtimeSettings.getMaxUsers()).thenReturn(15000);

        mockMvc.perform(post("/config").param("action", "buildDB"))
                .andExpect(status().isOk())
                .andExpect(view().name("config"))
                .andExpect(model().attribute("status", "DayTrader Database Built - 15000users createdCurrent DayTrader Configuration:"));

        verify(dbUtils).buildDB(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.isNull());
        verify(runtimeSettings).getMaxUsers();
    }
}