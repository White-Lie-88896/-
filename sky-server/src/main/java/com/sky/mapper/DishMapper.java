package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.annotation.OperationType;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishMapper {
    Page<DishVO> pageQuery(DishPageQueryDTO dishPageQueryDTO);

    @AutoFill(OperationType.INSERT)
    void insert(Dish dish);

    List<Dish> list(Dish dish);


    DishVO selectByPrimaryKey(Long id);

    @Select("select * from dish where id = #{id}")
    Dish selectById(Long id);

    void deleteByIds(List<Long> ids);

    @AutoFill(OperationType.UPDATE)
    void update(Dish dish);

    /**
     * 根据状态统计菜品数量
     * @param status
     * @return
     */
    @Select("select count(id) from dish where status = #{status}")
    Integer countByStatus(Integer status);
}
