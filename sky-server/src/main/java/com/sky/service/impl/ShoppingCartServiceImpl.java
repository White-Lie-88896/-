package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    /**
     * 添加购物车
     * @param shoppingCartDTO
     */
    @Override
    public void addShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        log.info("添加购物车: {}", shoppingCartDTO);
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        // 获取当前登录用户id
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);

        // 1. 判断当前加入到购物车中的商品是否已经存在了
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);

        // 2. 如果已经存在了，只需要将数量加一即可
        if (list != null && !list.isEmpty()) {
            ShoppingCart cart = list.get(0);
            cart.setNumber(cart.getNumber() + 1);
            shoppingCartMapper.updateNumberById(cart);
        } else {
            // 3. 如果不存在，需要插入一条新的购物车数据

            // 判断本次添加到购物车的是菜品还是套餐
            Long dishId = shoppingCartDTO.getDishId();
            if (dishId != null) {
                // 本次添加到购物车的是菜品
                Dish dish = dishMapper.selectById(dishId);
                shoppingCart.setName(dish.getName());
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setAmount(dish.getPrice());
            } else {
                // 本次添加到购物车的是套餐
                Long setmealId = shoppingCartDTO.getSetmealId();
                Setmeal setmeal = setmealMapper.getById(setmealId);
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setImage(setmeal.getImage());
                shoppingCart.setAmount(setmeal.getPrice());
            }
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.insert(shoppingCart);
        }
    }

    /**
     * 查看购物车
     * @return
     */
    @Override
    public List<ShoppingCart> showShoppingCart() {
        // 获取当前登录用户id
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = ShoppingCart.builder()
                .userId(userId)
                .build();
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        return list;
    }

    /**
     * 减少购物车中一个商品的数量
     * @param shoppingCartDTO
     */
    @Override
    public void subShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        log.info("减少购物车商品数量: {}", shoppingCartDTO);
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        // 获取当前登录用户id
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);

        // 查询当前用户购物车中的特定商品
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        /*
        查询是以 用户 ID + 菜品/套餐 ID + 口味 作为联合条件的。
        在购物车的业务设计中，同一个用户、同一道菜、同一口味 永远只有一条记录 —— 数量用 number 字段来累加，而不是插入多行。
        所以这个 list 的结果要么是空，要么恰好是一条记录，取 get(0) 完全安全。*/
        if (list != null && !list.isEmpty()) {
            ShoppingCart cart = list.get(0);
            Integer number = cart.getNumber();
            if (number > 1) {
                // 如果当前数量大于1，则执行减1操作
                cart.setNumber(number - 1);
                shoppingCartMapper.updateNumberById(cart);
            } else {
                // 如果当前数量为1，再减就没了，所以直接删除该记录
                shoppingCartMapper.deleteById(cart.getId());
            }
        }
    }

    /**
     * 清空购物车
     */
    @Override
    public void cleanShoppingCart() {
        // 获取当前登录用户id
        Long userId = BaseContext.getCurrentId();
        shoppingCartMapper.cleanByUserId(userId);
        log.info("已清空用户 {} 的购物车", userId);
    }
}
