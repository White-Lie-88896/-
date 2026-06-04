package com.sky.test;

import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.User;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MockDataTest {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Test
    public void insertMockData() {
        System.out.println("====== 开始插入测试数据 ======");

        // 1. 模拟插入用户数据
        User user1 = User.builder()
                .openid("mock_user_openid_1")
                .name("测试用户A")
                .phone("13800000001")
                .sex("1")
                .createTime(LocalDateTime.now()) // 今天注册
                .build();
        userMapper.insert(user1);

        User user2 = User.builder()
                .openid("mock_user_openid_2")
                .name("测试用户B")
                .phone("13800000002")
                .sex("0")
                .createTime(LocalDateTime.now().minusDays(1)) // 昨天注册
                .build();
        userMapper.insert(user2);

        User user3 = User.builder()
                .openid("mock_user_openid_3")
                .name("测试用户C")
                .phone("13800000003")
                .sex("1")
                .createTime(LocalDateTime.now().minusDays(2)) // 前天注册
                .build();
        userMapper.insert(user3);

        System.out.println("用户数据插入完成，用户ID：" + user1.getId() + ", " + user2.getId() + ", " + user3.getId());

        // 2. 模拟插入订单数据 (今日、昨日、前天)
        LocalDateTime today = LocalDateTime.now();
        LocalDateTime yesterday = today.minusDays(1);
        LocalDateTime twoDaysAgo = today.minusDays(2);

        // --- 今日订单 ---
        // 今日已完成订单 1
        Orders order1 = Orders.builder()
                .number("M100001")
                .status(Orders.COMPLETED)
                .userId(user1.getId())
                .addressBookId(1L)
                .orderTime(today.withHour(12).withMinute(0))
                .checkoutTime(today.withHour(12).withMinute(30))
                .payMethod(1)
                .payStatus(Orders.PAID)
                .amount(new BigDecimal("88.00"))
                .phone(user1.getPhone())
                .consignee(user1.getName())
                .address("北京市朝阳区阳光大道1号")
                .deliveryStatus(1)
                .tablewareStatus(1)
                .build();
        orderMapper.insert(order1);

        // 今日已完成订单 2
        Orders order2 = Orders.builder()
                .number("M100002")
                .status(Orders.COMPLETED)
                .userId(user2.getId())
                .addressBookId(1L)
                .orderTime(today.withHour(14).withMinute(0))
                .checkoutTime(today.withHour(14).withMinute(45))
                .payMethod(1)
                .payStatus(Orders.PAID)
                .amount(new BigDecimal("120.00"))
                .phone(user2.getPhone())
                .consignee(user2.getName())
                .address("北京市海淀区中关村大街10号")
                .deliveryStatus(1)
                .tablewareStatus(1)
                .build();
        orderMapper.insert(order2);

        // 今日待接单订单 3
        Orders order3 = Orders.builder()
                .number("M100003")
                .status(Orders.TO_BE_CONFIRMED)
                .userId(user1.getId())
                .addressBookId(1L)
                .orderTime(today.withHour(18).withMinute(0))
                .payMethod(1)
                .payStatus(Orders.PAID)
                .amount(new BigDecimal("45.00"))
                .phone(user1.getPhone())
                .consignee(user1.getName())
                .address("北京市朝阳区阳光大道1号")
                .deliveryStatus(1)
                .tablewareStatus(1)
                .build();
        orderMapper.insert(order3);

        // 今日待派送订单 4
        Orders order4 = Orders.builder()
                .number("M100004")
                .status(Orders.CONFIRMED)
                .userId(user3.getId())
                .addressBookId(1L)
                .orderTime(today.withHour(19).withMinute(0))
                .payMethod(1)
                .payStatus(Orders.PAID)
                .amount(new BigDecimal("60.00"))
                .phone(user3.getPhone())
                .consignee(user3.getName())
                .address("北京市西城区金融街2号")
                .deliveryStatus(1)
                .tablewareStatus(1)
                .build();
        orderMapper.insert(order4);

        // 今日已取消订单 5
        Orders order5 = Orders.builder()
                .number("M100005")
                .status(Orders.CANCELLED)
                .userId(user2.getId())
                .addressBookId(1L)
                .orderTime(today.withHour(10).withMinute(0))
                .payMethod(1)
                .payStatus(Orders.UN_PAID)
                .amount(new BigDecimal("30.00"))
                .phone(user2.getPhone())
                .consignee(user2.getName())
                .address("北京市海淀区中关村大街10号")
                .cancelReason("用户超时未支付")
                .cancelTime(today.withHour(10).withMinute(15))
                .deliveryStatus(1)
                .tablewareStatus(1)
                .build();
        orderMapper.insert(order5);

        // --- 昨日订单 ---
        // 昨日已完成订单 6
        Orders order6 = Orders.builder()
                .number("M100006")
                .status(Orders.COMPLETED)
                .userId(user2.getId())
                .addressBookId(1L)
                .orderTime(yesterday.withHour(12).withMinute(30))
                .checkoutTime(yesterday.withHour(13).withMinute(0))
                .payMethod(1)
                .payStatus(Orders.PAID)
                .amount(new BigDecimal("150.00"))
                .phone(user2.getPhone())
                .consignee(user2.getName())
                .address("北京市海淀区中关村大街10号")
                .deliveryStatus(1)
                .tablewareStatus(1)
                .build();
        orderMapper.insert(order6);

        // 昨日已完成订单 7
        Orders order7 = Orders.builder()
                .number("M100007")
                .status(Orders.COMPLETED)
                .userId(user3.getId())
                .addressBookId(1L)
                .orderTime(yesterday.withHour(18).withMinute(30))
                .checkoutTime(yesterday.withHour(19).withMinute(15))
                .payMethod(1)
                .payStatus(Orders.PAID)
                .amount(new BigDecimal("95.00"))
                .phone(user3.getPhone())
                .consignee(user3.getName())
                .address("北京市西城区金融街2号")
                .deliveryStatus(1)
                .tablewareStatus(1)
                .build();
        orderMapper.insert(order7);

        // --- 前天订单 ---
        // 前天已完成订单 8
        Orders order8 = Orders.builder()
                .number("M100008")
                .status(Orders.COMPLETED)
                .userId(user3.getId())
                .addressBookId(1L)
                .orderTime(twoDaysAgo.withHour(12).withMinute(0))
                .checkoutTime(twoDaysAgo.withHour(12).withMinute(45))
                .payMethod(1)
                .payStatus(Orders.PAID)
                .amount(new BigDecimal("200.00"))
                .phone(user3.getPhone())
                .consignee(user3.getName())
                .address("北京市西城区金融街2号")
                .deliveryStatus(1)
                .tablewareStatus(1)
                .build();
        orderMapper.insert(order8);

        System.out.println("订单数据插入完成，订单ID：" + order1.getId() + ", " + order2.getId() + ", " + order6.getId() + ", " + order7.getId() + ", " + order8.getId());

        // 3. 模拟插入订单明细数据
        List<OrderDetail> details = new ArrayList<>();

        // Order 1 (88.00): 宫保鸡丁 * 2 (单价 44.00)
        details.add(OrderDetail.builder()
                .orderId(order1.getId())
                .name("宫保鸡丁")
                .number(2)
                .amount(new BigDecimal("44.00"))
                .build());

        // Order 2 (120.00): 鱼香肉丝 * 3 (单价 30.00), 宫保鸡丁 * 1 (单价 30.00)
        details.add(OrderDetail.builder()
                .orderId(order2.getId())
                .name("鱼香肉丝")
                .number(3)
                .amount(new BigDecimal("30.00"))
                .build());
        details.add(OrderDetail.builder()
                .orderId(order2.getId())
                .name("宫保鸡丁")
                .number(1)
                .amount(new BigDecimal("30.00"))
                .build());

        // Order 6 (150.00): 水煮鱼 * 1 (单价 150.00)
        details.add(OrderDetail.builder()
                .orderId(order6.getId())
                .name("水煮鱼")
                .number(1)
                .amount(new BigDecimal("150.00"))
                .build());

        // Order 7 (95.00): 宫保鸡丁 * 1 (单价 30.00), 水煮鱼 * 1 (单价 65.00)
        details.add(OrderDetail.builder()
                .orderId(order7.getId())
                .name("宫保鸡丁")
                .number(1)
                .amount(new BigDecimal("30.00"))
                .build());
        details.add(OrderDetail.builder()
                .orderId(order7.getId())
                .name("水煮鱼")
                .number(1)
                .amount(new BigDecimal("65.00"))
                .build());

        // Order 8 (200.00): 水煮鱼 * 2 (单价 100.00)
        details.add(OrderDetail.builder()
                .orderId(order8.getId())
                .name("水煮鱼")
                .number(2)
                .amount(new BigDecimal("100.00"))
                .build());

        orderDetailMapper.insertBatch(details);
        System.out.println("订单明细批量插入完成！");
        System.out.println("====== 测试数据模拟成功 ======");
    }

    @Test
    public void insertBulkMockData() {
        System.out.println("====== 开始批量生成几百条测试数据 ======");
        java.util.Random random = new java.util.Random();
        java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd");
        int userSeq = 100;
        int orderSeq = 10000;

        // 1. 先生成 30 天以来的测试用户，存入 ID 列表
        List<Long> userIdList = new ArrayList<>();
        for (int i = 30; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            int newUsersCount = random.nextInt(3) + 1; // 每天 1 到 3 名新用户
            for (int u = 0; u < newUsersCount; u++) {
                User user = User.builder()
                        .openid("bulk_user_openid_" + (userSeq++))
                        .name("测试用户_" + userSeq)
                        .phone("139" + String.format("%08d", userSeq))
                        .sex(String.valueOf(random.nextInt(2)))
                        .createTime(LocalDateTime.of(date, LocalTime.of(random.nextInt(24), random.nextInt(60))))
                        .build();
                userMapper.insert(user);
                userIdList.add(user.getId());
            }
        }

        String[] dishNames = {"宫保鸡丁", "鱼香肉丝", "水煮鱼", "麻婆豆腐", "酸辣土豆丝", "扬州炒饭", "可口可乐", "雪碧", "红烧肉", "回锅肉"};
        BigDecimal[] dishPrices = {
            new BigDecimal("38.00"), new BigDecimal("28.00"), new BigDecimal("88.00"),
            new BigDecimal("18.00"), new BigDecimal("15.00"), new BigDecimal("20.00"),
            new BigDecimal("3.00"), new BigDecimal("3.00"), new BigDecimal("48.00"),
            new BigDecimal("35.00")
        };

        // 2. 遍历 30 天生成每一天的订单
        for (int i = 30; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            int ordersCount = random.nextInt(8) + 4; // 每天 4 到 11 笔订单

            for (int o = 0; o < ordersCount; o++) {
                // 挑选当前天或之前注册的用户ID
                int usersAvailable = userIdList.size() - (30 - i);
                if (usersAvailable <= 0) {
                    usersAvailable = userIdList.size();
                }
                Long userId = userIdList.get(random.nextInt(Math.min(userIdList.size(), usersAvailable)));

                // 挑选状态
                int status = Orders.COMPLETED;
                if (i == 0) { // 今天可以有一些进行中或取消的订单
                    int r = random.nextInt(10);
                    if (r < 2) status = Orders.TO_BE_CONFIRMED;
                    else if (r < 4) status = Orders.CONFIRMED;
                    else if (r < 6) status = Orders.DELIVERY_IN_PROGRESS;
                    else if (r < 8) status = Orders.COMPLETED;
                    else status = Orders.CANCELLED;
                } else { // 历史订单主要是已完成，少量已取消
                    status = random.nextInt(10) < 9 ? Orders.COMPLETED : Orders.CANCELLED;
                }

                int payStatus = (status == Orders.CANCELLED) ? Orders.UN_PAID : Orders.PAID;
                if (status == Orders.CANCELLED && random.nextBoolean()) {
                    payStatus = Orders.REFUND;
                }

                // 产生 1 到 3 件商品明细
                int itemsCount = random.nextInt(3) + 1;
                BigDecimal orderAmount = BigDecimal.ZERO;
                List<OrderDetail> details = new ArrayList<>();
                for (int item = 0; item < itemsCount; item++) {
                    int dishIndex = random.nextInt(dishNames.length);
                    String dishName = dishNames[dishIndex];
                    BigDecimal price = dishPrices[dishIndex];
                    int qty = random.nextInt(3) + 1;
                    BigDecimal detailAmount = price.multiply(new BigDecimal(qty));
                    orderAmount = orderAmount.add(detailAmount);

                    details.add(OrderDetail.builder()
                            .name(dishName)
                            .number(qty)
                            .amount(price)
                            .build());
                }

                // 打包费与餐具
                int packFee = random.nextInt(4) + 2;
                orderAmount = orderAmount.add(new BigDecimal(packFee));

                LocalDateTime orderTime = LocalDateTime.of(date, LocalTime.of(random.nextInt(12) + 10, random.nextInt(60)));
                LocalDateTime checkoutTime = orderTime.plusMinutes(random.nextInt(30) + 15);

                Orders order = Orders.builder()
                        .number("B" + date.format(dtf) + (orderSeq++))
                        .status(status)
                        .userId(userId)
                        .addressBookId(1L)
                        .orderTime(orderTime)
                        .checkoutTime(status == Orders.COMPLETED ? checkoutTime : null)
                        .payMethod(1)
                        .payStatus(payStatus)
                        .amount(orderAmount)
                        .phone("138" + String.format("%08d", random.nextInt(99999999)))
                        .consignee("随机收货人")
                        .address("测试生成收货地址")
                        .deliveryStatus(1)
                        .tablewareStatus(1)
                        .packAmount(packFee)
                        .tablewareNumber(random.nextInt(3) + 1)
                        .build();

                orderMapper.insert(order);

                // 绑定并批量写入明细
                for (OrderDetail detail : details) {
                    detail.setOrderId(order.getId());
                }
                orderDetailMapper.insertBatch(details);
            }
        }
        System.out.println("====== 批量测试数据生成完成，共生成 " + (userSeq - 100) + " 名用户和 " + (orderSeq - 10000) + " 笔订单！ ======");
    }
}
