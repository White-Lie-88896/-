package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.DishMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.WorkspaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class WorkspaceServiceImpl implements WorkspaceService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    /**
     * 根据时间段统计营业数据
     * @return
     */
    @Override
    public BusinessDataVO getBusinessData() {
        LocalDateTime todayBegin = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

        // 1. 统计营业额
        Map map = new HashMap();
        map.put("begin", todayBegin);
        map.put("end", todayEnd);
        map.put("status", Orders.COMPLETED); // 已完成订单（5）
        Double turnover = orderMapper.sumByMap(map);
        turnover = turnover == null ? 0.0 : turnover;

        // 2. 有效订单数
        Integer validOrderCount = orderMapper.countByMap(map);
        validOrderCount = validOrderCount == null ? 0 : validOrderCount;

        // 3. 今日订单总数
        map.remove("status");
        Integer totalOrderCount = orderMapper.countByMap(map);
        totalOrderCount = totalOrderCount == null ? 0 : totalOrderCount;

        // 4. 订单完成率
        Double orderCompletionRate = 0.0;
        if (totalOrderCount > 0) {
            orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;
        }

        // 5. 平均客单价
        Double unitPrice = 0.0;
        if (validOrderCount > 0) {
            unitPrice = turnover / validOrderCount;
        }

        // 6. 新增用户数
        map.clear();
        map.put("begin", todayBegin);
        map.put("end", todayEnd);
        Integer newUsers = userMapper.countByMap(map);
        newUsers = newUsers == null ? 0 : newUsers;

        return BusinessDataVO.builder()
                .turnover(turnover)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .unitPrice(unitPrice)
                .newUsers(newUsers)
                .build();
    }

    /**
     * 查询订单管理数据
     * @return
     */
    @Override
    public OrderOverViewVO getOrderOverView() {
        LocalDateTime todayBegin = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

        Map map = new HashMap();
        map.put("begin", todayBegin);
        map.put("end", todayEnd);

        // 全部订单
        Integer allOrders = orderMapper.countByMap(map);
        allOrders = allOrders == null ? 0 : allOrders;

        // 待接单 Orders.TO_BE_CONFIRMED (2)
        map.put("status", Orders.TO_BE_CONFIRMED);
        Integer waitingOrders = orderMapper.countByMap(map);
        waitingOrders = waitingOrders == null ? 0 : waitingOrders;

        // 待派送 Orders.CONFIRMED (3)
        map.put("status", Orders.CONFIRMED);
        Integer deliveredOrders = orderMapper.countByMap(map);
        deliveredOrders = deliveredOrders == null ? 0 : deliveredOrders;

        // 已完成 Orders.COMPLETED (5)
        map.put("status", Orders.COMPLETED);
        Integer completedOrders = orderMapper.countByMap(map);
        completedOrders = completedOrders == null ? 0 : completedOrders;

        // 已取消 Orders.CANCELLED (6)
        map.put("status", Orders.CANCELLED);
        Integer cancelledOrders = orderMapper.countByMap(map);
        cancelledOrders = cancelledOrders == null ? 0 : cancelledOrders;

        return OrderOverViewVO.builder()
                .allOrders(allOrders)
                .waitingOrders(waitingOrders)
                .deliveredOrders(deliveredOrders)
                .completedOrders(completedOrders)
                .cancelledOrders(cancelledOrders)
                .build();
    }

    /**
     * 查询菜品总览
     * @return
     */
    @Override
    public DishOverViewVO getDishOverView() {
        // 已启售数量
        Integer sold = dishMapper.countByStatus(1);
        sold = sold == null ? 0 : sold;

        // 已停售数量
        Integer discontinued = dishMapper.countByStatus(0);
        discontinued = discontinued == null ? 0 : discontinued;

        return DishOverViewVO.builder()
                .sold(sold)
                .discontinued(discontinued)
                .build();
    }

    /**
     * 查询套餐总览
     * @return
     */
    @Override
    public SetmealOverViewVO getSetmealOverView() {
        // 已启售数量
        Integer sold = setmealMapper.countByStatus(1);
        sold = sold == null ? 0 : sold;

        // 已停售数量
        Integer discontinued = setmealMapper.countByStatus(0);
        discontinued = discontinued == null ? 0 : discontinued;

        return SetmealOverViewVO.builder()
                .sold(sold)
                .discontinued(discontinued)
                .build();
    }
}
