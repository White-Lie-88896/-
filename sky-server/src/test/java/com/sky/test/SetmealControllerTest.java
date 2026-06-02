package com.sky.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sky.constant.JwtClaimsConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.properties.JwtProperties;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.utils.JwtUtil;
import com.sky.vo.SetmealVO;
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
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class SetmealControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtProperties jwtProperties;

    @MockBean
    private SetmealService setmealService;

    @Autowired
    private ObjectMapper objectMapper;

    private String token;

    @BeforeEach
    public void setUp() {
        // 生成合法的JWT令牌以绕过管理端拦截器
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, 1L);
        token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims
        );
    }

    @Test
    public void testSaveSetmeal() throws Exception {
        SetmealDTO setmealDTO = new SetmealDTO();
        setmealDTO.setCategoryId(100L);
        setmealDTO.setName("精选双人套餐");
        setmealDTO.setPrice(new BigDecimal("99.00"));
        setmealDTO.setStatus(StatusConstant.DISABLE);
        setmealDTO.setDescription("美味的双人套餐");
        setmealDTO.setImage("http://oss.com/test.jpg");

        SetmealDish sd = new SetmealDish();
        sd.setDishId(1L);
        sd.setName("宫保鸡丁");
        sd.setPrice(new BigDecimal("38.00"));
        sd.setCopies(1);
        setmealDTO.setSetmealDishes(Collections.singletonList(sd));

        Mockito.doNothing().when(setmealService).saveWithDish(any(SetmealDTO.class));

        mockMvc.perform(post("/admin/setmeal")
                        .header(jwtProperties.getAdminTokenName(), token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(setmealDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        Mockito.verify(setmealService, Mockito.times(1)).saveWithDish(any(SetmealDTO.class));
    }

    @Test
    public void testPageQuery() throws Exception {
        PageResult expectedResult = new PageResult(1L, Collections.singletonList(new SetmealVO()));
        Mockito.when(setmealService.pageQuery(any(SetmealPageQueryDTO.class))).thenReturn(expectedResult);

        mockMvc.perform(get("/admin/setmeal/page")
                        .header(jwtProperties.getAdminTokenName(), token)
                        .param("page", "1")
                        .param("pageSize", "10")
                        .param("name", "双人")
                        .param("categoryId", "100")
                        .param("status", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.total").value(1));

        Mockito.verify(setmealService, Mockito.times(1)).pageQuery(any(SetmealPageQueryDTO.class));
    }

    @Test
    public void testDeleteSetmeal() throws Exception {
        Mockito.doNothing().when(setmealService).deleteBatch(any(List.class));

        mockMvc.perform(delete("/admin/setmeal")
                        .header(jwtProperties.getAdminTokenName(), token)
                        .param("ids", "1,2,3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        Mockito.verify(setmealService, Mockito.times(1)).deleteBatch(eq(Arrays.asList(1L, 2L, 3L)));
    }

    @Test
    public void testGetById() throws Exception {
        SetmealVO setmealVO = new SetmealVO();
        setmealVO.setId(10L);
        setmealVO.setName("金牌双人餐");
        setmealVO.setPrice(new BigDecimal("128.00"));
        setmealVO.setSetmealDishes(new ArrayList<>());
        Mockito.when(setmealService.getByIdWithDish(10L)).thenReturn(setmealVO);

        mockMvc.perform(get("/admin/setmeal/10")
                        .header(jwtProperties.getAdminTokenName(), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.name").value("金牌双人餐"));

        Mockito.verify(setmealService, Mockito.times(1)).getByIdWithDish(10L);
    }

    @Test
    public void testUpdateSetmeal() throws Exception {
        SetmealDTO setmealDTO = new SetmealDTO();
        setmealDTO.setId(10L);
        setmealDTO.setName("升级版双人餐");
        setmealDTO.setPrice(new BigDecimal("138.00"));

        Mockito.doNothing().when(setmealService).update(any(SetmealDTO.class));

        mockMvc.perform(put("/admin/setmeal")
                        .header(jwtProperties.getAdminTokenName(), token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(setmealDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        Mockito.verify(setmealService, Mockito.times(1)).update(any(SetmealDTO.class));
    }

    @Test
    public void testStartOrStop() throws Exception {
        Mockito.doNothing().when(setmealService).startOrStop(eq(1), eq(10L));

        mockMvc.perform(post("/admin/setmeal/status/1")
                        .header(jwtProperties.getAdminTokenName(), token)
                        .param("id", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        Mockito.verify(setmealService, Mockito.times(1)).startOrStop(eq(1), eq(10L));
    }

    @Test
    public void testUserGetList() throws Exception {
        Setmeal setmeal = new Setmeal();
        setmeal.setId(10L);
        setmeal.setName("用户展示套餐");
        Mockito.when(setmealService.list(any(Setmeal.class))).thenReturn(Collections.singletonList(setmeal));

        mockMvc.perform(get("/user/setmeal/list")
                        .param("categoryId", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data[0].name").value("用户展示套餐"));

        Mockito.verify(setmealService, Mockito.times(1)).list(any(Setmeal.class));
    }
}
