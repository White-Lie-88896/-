package com.sky.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sky.constant.JwtClaimsConstant;
import com.sky.entity.AddressBook;
import com.sky.properties.JwtProperties;
import com.sky.service.AddressBookService;
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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AddressBookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtProperties jwtProperties;

    @MockBean
    private AddressBookService addressBookService;

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
    public void testListAddresses() throws Exception {
        AddressBook address = new AddressBook();
        address.setId(1L);
        address.setConsignee("张三");
        address.setPhone("13800000000");

        Mockito.when(addressBookService.list(any(AddressBook.class))).thenReturn(Collections.singletonList(address));

        mockMvc.perform(get("/user/addressBook/list")
                        .header(jwtProperties.getUserTokenName(), userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data[0].consignee").value("张三"));

        Mockito.verify(addressBookService, Mockito.times(1)).list(any(AddressBook.class));
    }

    @Test
    public void testSaveAddress() throws Exception {
        AddressBook address = new AddressBook();
        address.setConsignee("李四");
        address.setPhone("13900000000");

        Mockito.doNothing().when(addressBookService).save(any(AddressBook.class));

        mockMvc.perform(post("/user/addressBook")
                        .header(jwtProperties.getUserTokenName(), userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(address)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        Mockito.verify(addressBookService, Mockito.times(1)).save(any(AddressBook.class));
    }

    @Test
    public void testGetAddressById() throws Exception {
        AddressBook address = new AddressBook();
        address.setId(2L);
        address.setConsignee("王五");

        Mockito.when(addressBookService.getById(2L)).thenReturn(address);

        mockMvc.perform(get("/user/addressBook/2")
                        .header(jwtProperties.getUserTokenName(), userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.consignee").value("王五"));

        Mockito.verify(addressBookService, Mockito.times(1)).getById(2L);
    }

    @Test
    public void testUpdateAddress() throws Exception {
        AddressBook address = new AddressBook();
        address.setId(2L);
        address.setConsignee("王五修改");

        Mockito.doNothing().when(addressBookService).update(any(AddressBook.class));

        mockMvc.perform(put("/user/addressBook")
                        .header(jwtProperties.getUserTokenName(), userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(address)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        Mockito.verify(addressBookService, Mockito.times(1)).update(any(AddressBook.class));
    }

    @Test
    public void testSetDefaultAddress() throws Exception {
        AddressBook address = new AddressBook();
        address.setId(2L);

        Mockito.doNothing().when(addressBookService).setDefault(any(AddressBook.class));

        mockMvc.perform(put("/user/addressBook/default")
                        .header(jwtProperties.getUserTokenName(), userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(address)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        Mockito.verify(addressBookService, Mockito.times(1)).setDefault(any(AddressBook.class));
    }

    @Test
    public void testDeleteAddress() throws Exception {
        Mockito.doNothing().when(addressBookService).deleteById(2L);

        mockMvc.perform(delete("/user/addressBook")
                        .header(jwtProperties.getUserTokenName(), userToken)
                        .param("id", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        Mockito.verify(addressBookService, Mockito.times(1)).deleteById(2L);
    }

    @Test
    public void testGetDefaultAddress() throws Exception {
        AddressBook address = new AddressBook();
        address.setId(1L);
        address.setConsignee("赵六");
        address.setIsDefault(1);

        Mockito.when(addressBookService.list(any(AddressBook.class))).thenReturn(Collections.singletonList(address));

        mockMvc.perform(get("/user/addressBook/default")
                        .header(jwtProperties.getUserTokenName(), userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.consignee").value("赵六"));

        Mockito.verify(addressBookService, Mockito.times(1)).list(any(AddressBook.class));
    }
}
