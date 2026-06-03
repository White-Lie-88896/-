package com.sky.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.*;
import com.sky.properties.JwtProperties;
import com.sky.service.OrderService;
import com.sky.utils.JwtUtil;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import com.sky.result.PageResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AdminOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtProperties jwtProperties;

    @MockBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;

    @BeforeEach
    public void setUp() {
        // 生成合法的管理员JWT令牌以绕过管理端拦截器
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, 1L);
        adminToken = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims
        );
    }

    @Test
    public void testConditionSearch() throws Exception {
        PageResult expectedPage = new PageResult(0L, new ArrayList<>());
        Mockito.when(orderService.conditionSearch(any(OrdersPageQueryDTO.class))).thenReturn(expectedPage);

        mockMvc.perform(get("/admin/order/conditionSearch")
                        .header(jwtProperties.getAdminTokenName(), adminToken)
                        .param("page", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        Mockito.verify(orderService, Mockito.times(1)).conditionSearch(any(OrdersPageQueryDTO.class));
    }

    @Test
    public void testStatistics() throws Exception {
        OrderStatisticsVO statistics = new OrderStatisticsVO();
        statistics.setToBeConfirmed(2);
        statistics.setConfirmed(3);
        statistics.setDeliveryInProgress(4);
        Mockito.when(orderService.statistics()).thenReturn(statistics);

        mockMvc.perform(get("/admin/order/statistics")
                        .header(jwtProperties.getAdminTokenName(), adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.toBeConfirmed").value(2))
                .andExpect(jsonPath("$.data.confirmed").value(3));

        Mockito.verify(orderService, Mockito.times(1)).statistics();
    }

    @Test
    public void testDetails() throws Exception {
        OrderVO orderVO = new OrderVO();
        orderVO.setId(123L);
        Mockito.when(orderService.details(anyLong())).thenReturn(orderVO);

        mockMvc.perform(get("/admin/order/details/123")
                        .header(jwtProperties.getAdminTokenName(), adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.id").value(123L));

        Mockito.verify(orderService, Mockito.times(1)).details(123L);
    }

    @Test
    public void testConfirm() throws Exception {
        OrdersConfirmDTO dto = new OrdersConfirmDTO();
        dto.setId(123L);
        Mockito.doNothing().when(orderService).confirm(any(OrdersConfirmDTO.class));

        mockMvc.perform(put("/admin/order/confirm")
                        .header(jwtProperties.getAdminTokenName(), adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        Mockito.verify(orderService, Mockito.times(1)).confirm(any(OrdersConfirmDTO.class));
    }

    @Test
    public void testRejection() throws Exception {
        OrdersRejectionDTO dto = new OrdersRejectionDTO();
        dto.setId(123L);
        dto.setRejectionReason("太忙了");
        Mockito.doNothing().when(orderService).rejection(any(OrdersRejectionDTO.class));

        mockMvc.perform(put("/admin/order/rejection")
                        .header(jwtProperties.getAdminTokenName(), adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        Mockito.verify(orderService, Mockito.times(1)).rejection(any(OrdersRejectionDTO.class));
    }

    @Test
    public void testCancel() throws Exception {
        OrdersCancelDTO dto = new OrdersCancelDTO();
        dto.setId(123L);
        dto.setCancelReason("配送出了意外");
        Mockito.doNothing().when(orderService).cancel(any(OrdersCancelDTO.class));

        mockMvc.perform(put("/admin/order/cancel")
                        .header(jwtProperties.getAdminTokenName(), adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        Mockito.verify(orderService, Mockito.times(1)).cancel(any(OrdersCancelDTO.class));
    }

    @Test
    public void testDelivery() throws Exception {
        Mockito.doNothing().when(orderService).delivery(anyLong());

        mockMvc.perform(put("/admin/order/delivery/123")
                        .header(jwtProperties.getAdminTokenName(), adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        Mockito.verify(orderService, Mockito.times(1)).delivery(123L);
    }

    @Test
    public void testComplete() throws Exception {
        Mockito.doNothing().when(orderService).complete(anyLong());

        mockMvc.perform(put("/admin/order/complete/123")
                        .header(jwtProperties.getAdminTokenName(), adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        Mockito.verify(orderService, Mockito.times(1)).complete(123L);
    }
}
