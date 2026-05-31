package com.sky.service;


import com.sky.dto.DishDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;

public interface DishService {
    void saveWithFlavor(DishDTO dishDTO);

    PageResult page(Integer page, Integer pageSize, String name, Integer categoryId, Integer status);

    List<Dish> list(Long categoryId);

    DishVO getById(Long id);

    void deleteByIds(List<Long> ids);

    void updateWithFlavor(DishDTO dishDTO);

    void startOrStop(Integer status, Long id);
}
