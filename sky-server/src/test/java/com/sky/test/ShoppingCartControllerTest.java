package com.sky.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.properties.JwtProperties;
import com.sky.service.ShoppingCartService;
import com.sky.utils.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ShoppingCartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtProperties jwtProperties;

    @MockBean
    private ShoppingCartService shoppingCartService;

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
    public void testAddShoppingCart() throws Exception {
        ShoppingCartDTO shoppingCartDTO = new ShoppingCartDTO();
        shoppingCartDTO.setDishId(1L);
        shoppingCartDTO.setDishFlavor("少糖");

        Mockito.doNothing().when(shoppingCartService).addShoppingCart(any(ShoppingCartDTO.class));

        mockMvc.perform(post("/user/shoppingCart/add")
                        .header(jwtProperties.getUserTokenName(), userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(shoppingCartDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        Mockito.verify(shoppingCartService, Mockito.times(1)).addShoppingCart(any(ShoppingCartDTO.class));
    }

    @Test
    public void testShowShoppingCart() throws Exception {
        ShoppingCart item = new ShoppingCart();
        item.setId(1L);
        item.setName("烤鸭");
        item.setNumber(2);
        Mockito.when(shoppingCartService.showShoppingCart()).thenReturn(Collections.singletonList(item));

        mockMvc.perform(get("/user/shoppingCart/list")
                        .header(jwtProperties.getUserTokenName(), userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data[0].name").value("烤鸭"))
                .andExpect(jsonPath("$.data[0].number").value(2));

        Mockito.verify(shoppingCartService, Mockito.times(1)).showShoppingCart();
    }

    @Test
    public void testSubShoppingCart() throws Exception {
        ShoppingCartDTO shoppingCartDTO = new ShoppingCartDTO();
        shoppingCartDTO.setDishId(1L);

        Mockito.doNothing().when(shoppingCartService).subShoppingCart(any(ShoppingCartDTO.class));

        mockMvc.perform(post("/user/shoppingCart/sub")
                        .header(jwtProperties.getUserTokenName(), userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(shoppingCartDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        Mockito.verify(shoppingCartService, Mockito.times(1)).subShoppingCart(any(ShoppingCartDTO.class));
    }

    @Test
    public void testCleanShoppingCart() throws Exception {
        Mockito.doNothing().when(shoppingCartService).cleanShoppingCart();

        mockMvc.perform(delete("/user/shoppingCart/clean")
                        .header(jwtProperties.getUserTokenName(), userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        Mockito.verify(shoppingCartService, Mockito.times(1)).cleanShoppingCart();
    }
}
