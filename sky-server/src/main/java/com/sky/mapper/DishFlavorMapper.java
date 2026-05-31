package com.sky.mapper;


import com.sky.annotation.AutoFill;
import com.sky.annotation.OperationType;
import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishFlavorMapper {
    void insertBatch(List<DishFlavor> flavors);

    @Select("select * from dish_flavor where dish_id = #{dishId}")
    List<DishFlavor> select(Long dishId);

    void deleteByDishIds(List<Long> ids);
}
