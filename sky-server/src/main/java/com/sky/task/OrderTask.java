package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单定时任务类
 */
@Component
@Slf4j
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 处理支付超时订单
     * 每分钟触发一次
     */
    @Scheduled(cron = "0 * * * * ?")
    public void processTimeoutOrder() {
        log.info("定时处理支付超时订单：{}", LocalDateTime.now());

        // 查询 15 分钟前下单且未支付的订单
        LocalDateTime timeThreshold = LocalDateTime.now().minusMinutes(15);
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTimeLT(Orders.PENDING_PAYMENT, timeThreshold);

        if (ordersList != null && !ordersList.isEmpty()) {
            log.info("发现超时未支付订单数量：{}", ordersList.size());
            for (Orders orders : ordersList) {
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelReason("订单超时，自动取消");
                orders.setCancelTime(LocalDateTime.now());
                orderMapper.update(orders);
            }
        }
    }

    /**
     * 自动处理一直处于派送中的订单
     * 每天凌晨1点触发一次
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void processDeliveryOrder() {
        log.info("定时处理一直处于派送中的订单：{}", LocalDateTime.now());

        // 查询 1 小时前下单且处于派送中状态的订单
        LocalDateTime timeThreshold = LocalDateTime.now().minusHours(1);
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTimeLT(Orders.DELIVERY_IN_PROGRESS, timeThreshold);

        if (ordersList != null && !ordersList.isEmpty()) {
            log.info("发现派送中超时未确认订单数量：{}", ordersList.size());
            for (Orders orders : ordersList) {
                orders.setStatus(Orders.COMPLETED);
                orderMapper.update(orders);
            }
        }
    }
}
