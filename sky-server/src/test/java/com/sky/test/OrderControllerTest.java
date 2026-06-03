package com.sky.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.properties.JwtProperties;
import com.sky.service.OrderService;
import com.sky.utils.JwtUtil;
import com.sky.vo.OrderSubmitVO;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtProperties jwtProperties;

    @MockBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    private String userToken;

    @BeforeEach
    public void setUp() {
        // 生成合法的微信用户JWT令牌以绕过用户端拦截器
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, 100L);
        userToken = JwtUtil.createJWT(
                jwtProperties.getUserSecretKey(),
                jwtProperties.getUserTtl(),
                claims
        );
    }

    @Test
    public void testSubmitOrder() throws Exception {
        OrdersSubmitDTO dto = new OrdersSubmitDTO();
        dto.setAddressBookId(1L);
        dto.setPayMethod(1);
        dto.setPackAmount(5);
        dto.setAmount(new BigDecimal("99.00"));
        dto.setRemark("打包多放餐具");

        OrderSubmitVO expectedVo = OrderSubmitVO.builder()
                .id(999L)
                .orderNumber("1234567890")
                .orderAmount(new BigDecimal("99.00"))
                .orderTime(LocalDateTime.now())
                .build();

        Mockito.when(orderService.submitOrder(any(OrdersSubmitDTO.class))).thenReturn(expectedVo);

        mockMvc.perform(post("/user/order/submit")
                        .header(jwtProperties.getUserTokenName(), userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.id").value(999L))
                .andExpect(jsonPath("$.data.orderNumber").value("1234567890"));

        Mockito.verify(orderService, Mockito.times(1)).submitOrder(any(OrdersSubmitDTO.class));
    }

    @Test
    public void testHistoryOrders() throws Exception {
        PageResult expectedPage = new PageResult(1L, new ArrayList<>());
        Mockito.when(orderService.pageQuery4User(anyInt(), anyInt(), any())).thenReturn(expectedPage);

        mockMvc.perform(get("/user/order/historyOrders")
                        .header(jwtProperties.getUserTokenName(), userToken)
                        .param("page", "1")
                        .param("pageSize", "10")
                        .param("status", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.total").value(1L));

        Mockito.verify(orderService, Mockito.times(1)).pageQuery4User(anyInt(), anyInt(), any());
    }

    @Test
    public void testOrderDetail() throws Exception {
        OrderVO expectedVo = new OrderVO();
        expectedVo.setId(123L);
        expectedVo.setNumber("123456789");
        Mockito.when(orderService.details(anyLong())).thenReturn(expectedVo);

        mockMvc.perform(get("/user/order/orderDetail/123")
                        .header(jwtProperties.getUserTokenName(), userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.id").value(123L));

        Mockito.verify(orderService, Mockito.times(1)).details(123L);
    }

    @Test
    public void testCancelOrder() throws Exception {
        Mockito.doNothing().when(orderService).userCancelById(anyLong());

        mockMvc.perform(put("/user/order/cancel/123")
                        .header(jwtProperties.getUserTokenName(), userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        Mockito.verify(orderService, Mockito.times(1)).userCancelById(123L);
    }

    @Test
    public void testRepetition() throws Exception {
        Mockito.doNothing().when(orderService).repetition(anyLong());

        mockMvc.perform(post("/user/order/repetition/123")
                        .header(jwtProperties.getUserTokenName(), userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        Mockito.verify(orderService, Mockito.times(1)).repetition(123L);
    }
}
