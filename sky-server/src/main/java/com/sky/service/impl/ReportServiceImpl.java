package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 营业额统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        // 1. 生成日期列表
        List<LocalDate> dateList = getLocalDateList(begin, end);

        // 2. 统计每日的营业额
        List<Double> turnoverList = new ArrayList<>();
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            Map map = new HashMap();
            map.put("begin", beginTime);
            map.put("end", endTime);
            map.put("status", Orders.COMPLETED); // 已完成订单（5）

            Double turnover = orderMapper.sumByMap(map);
            turnover = turnover == null ? 0.0 : turnover;
            turnoverList.add(turnover);
        }

        // 3. 封装VO并返回
        String dateListStr = dateList.stream().map(LocalDate::toString).collect(Collectors.joining(","));
        String turnoverListStr = turnoverList.stream().map(Object::toString).collect(Collectors.joining(","));

        return TurnoverReportVO.builder()
                .dateList(dateListStr)
                .turnoverList(turnoverListStr)
                .build();
    }

    /**
     * 用户统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        // 1. 生成日期列表
        List<LocalDate> dateList = getLocalDateList(begin, end);

        // 2. 统计每日的新增用户和用户总量
        List<Integer> newUserList = new ArrayList<>();
        List<Integer> totalUserList = new ArrayList<>();

        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            // 统计总用户数：create_time <= endTime
            Map map = new HashMap();
            map.put("end", endTime);
            Integer totalUser = userMapper.countByMap(map);
            totalUser = totalUser == null ? 0 : totalUser;

            // 统计新增用户数：beginTime <= create_time <= endTime
            map.put("begin", beginTime);
            Integer newUser = userMapper.countByMap(map);
            newUser = newUser == null ? 0 : newUser;

            newUserList.add(newUser);
            totalUserList.add(totalUser);
        }

        // 3. 封装VO返回
        String dateListStr = dateList.stream().map(LocalDate::toString).collect(Collectors.joining(","));
        String newUserListStr = newUserList.stream().map(Object::toString).collect(Collectors.joining(","));
        String totalUserListStr = totalUserList.stream().map(Object::toString).collect(Collectors.joining(","));

        return UserReportVO.builder()
                .dateList(dateListStr)
                .newUserList(newUserListStr)
                .totalUserList(totalUserListStr)
                .build();
    }

    /**
     * 订单统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public OrderReportVO getOrdersStatistics(LocalDate begin, LocalDate end) {
        // 1. 生成日期列表
        List<LocalDate> dateList = getLocalDateList(begin, end);

        // 2. 统计每日的订单数据
        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> validOrderCountList = new ArrayList<>();

        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            Map map = new HashMap();
            map.put("begin", beginTime);
            map.put("end", endTime);

            // 统计当天总订单数
            Integer orderCount = orderMapper.countByMap(map);
            orderCount = orderCount == null ? 0 : orderCount;

            // 统计当天有效订单数 (status = 5)
            map.put("status", Orders.COMPLETED);
            Integer validOrderCount = orderMapper.countByMap(map);
            validOrderCount = validOrderCount == null ? 0 : validOrderCount;

            orderCountList.add(orderCount);
            validOrderCountList.add(validOrderCount);
        }

        // 3. 累加计算区间总数与完成率
        Integer totalOrderCount = orderCountList.stream().mapToInt(Integer::intValue).sum();
        Integer validOrderCount = validOrderCountList.stream().mapToInt(Integer::intValue).sum();

        Double orderCompletionRate = 0.0;
        if (totalOrderCount > 0) {
            orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;
        }

        // 4. 封装VO并返回
        String dateListStr = dateList.stream().map(LocalDate::toString).collect(Collectors.joining(","));
        String orderCountListStr = orderCountList.stream().map(Object::toString).collect(Collectors.joining(","));
        String validOrderCountListStr = validOrderCountList.stream().map(Object::toString).collect(Collectors.joining(","));

        return OrderReportVO.builder()
                .dateList(dateListStr)
                .orderCountList(orderCountListStr)
                .validOrderCountList(validOrderCountListStr)
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    /**
     * 销量前10商品排行
     * @param begin
     * @param end
     * @return
     */
    @Override
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        Map map = new HashMap();
        map.put("begin", beginTime);
        map.put("end", endTime);

        List<GoodsSalesDTO> salesTop10 = orderMapper.getSalesTop10(map);

        String nameList = salesTop10.stream().map(GoodsSalesDTO::getName).collect(Collectors.joining(","));
        String numberList = salesTop10.stream().map(x -> x.getNumber().toString()).collect(Collectors.joining(","));

        return SalesTop10ReportVO.builder()
                .nameList(nameList)
                .numberList(numberList)
                .build();
    }

    /**
     * 根据开始日期和结束日期构建 LocalDate 列表
     * @param begin
     * @param end
     * @return
     */
    private List<LocalDate> getLocalDateList(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        for (LocalDate date = begin; !date.isAfter(end); date = date.plusDays(1)) {
            dateList.add(date);
        }
        return dateList;
    }
}
