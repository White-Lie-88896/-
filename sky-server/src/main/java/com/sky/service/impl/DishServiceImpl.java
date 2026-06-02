package com.sky.service.impl;

import ch.qos.logback.core.joran.util.beans.BeanUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.constant.StatusConstant;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetMealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import lombok.extern.slf4j.XSlf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.beans.beancontext.BeanContext;
import java.util.List;

/**
 * 菜品业务层实现类
 */
@Service
@Slf4j
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetMealDishMapper setMealDishMapper;

    /**
     * 新增菜品及对应的口味数据
     *
     * @param dishDTO 菜品数据传输对象
     */
    @Override
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {
        log.info("新增菜品数据: {}", dishDTO);
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        dishMapper.insert(dish);

        // 获取 insert 语句生成的主键值
        Long dishId = dish.getId();
        log.info("插入后的菜品实体: {}, 生成的主键ID为: {}", dish, dishId);

        // 向口味表插入n条数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && !flavors.isEmpty()) {
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishId);
            });
            // 向口味表插入n条数据
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    /**
     * 菜品分页查询
     *
     * @param page       页码
     * @param pageSize   每页记录数
     * @param name       菜品名称（模糊查询）
     * @param categoryId 分类ID
     * @param status     状态（0：停售，1：启售）
     * @return 分页查询结果封装对象
     */
    @Override
    public PageResult page(Integer page, Integer pageSize, String name, Integer categoryId, Integer status) {
        // 开始分页查询：第一个参数是页码，第二个参数是每页显示的记录数
        PageHelper.startPage(page, pageSize);

        DishPageQueryDTO dishPageQueryDTO = new DishPageQueryDTO();
        dishPageQueryDTO.setName(name);
        dishPageQueryDTO.setCategoryId(categoryId);
        dishPageQueryDTO.setStatus(status);

        // PageHelper 会拦截 SQL 并自动封装结果。
        // pageList 本质上是一个 List，里面存放了从数据库查出来的多条 DishVO 数据。
        Page<DishVO> pageList = dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(pageList.getTotal(), pageList.getResult());
    }

    @Override
    public List<Dish> list(Long categoryId) {
        Dish dish = new Dish();
        dish.setCategoryId(categoryId);
        dish.setStatus(StatusConstant.ENABLE);
        return dishMapper.list(dish);
    }

    @Override
    public DishVO getById(Long id) {
        // 1. 根据id查询菜品基本信息
        DishVO dishVO = dishMapper.selectByPrimaryKey(id);
        if (dishVO == null) {
            return null;
        }

        // 3. 根据菜品id查询对应的口味数据
        List<DishFlavor> flavors = dishFlavorMapper.select(id);
        dishVO.setFlavors(flavors);

        return dishVO;
    }

    @Override
    @Transactional
    public void deleteByIds(List<Long> ids) {
        // 判断菜品是不是正在售卖，售卖中不能删除
        for (Long id : ids) {
            Dish dish = dishMapper.selectById(id);
            if (dish.getStatus() == StatusConstant.ENABLE) {
                throw new DeletionNotAllowedException("菜品正在售卖中，不能删除");
            }
        }
        // 判断菜品是不是和套餐有关联，若有，无法删除
        // 有一个表是setmeal_dish
        for (Long id : ids) {
            // 根据id查询setmeal_dish表中是否有该菜品关联的套餐关系
            SetmealDish setmealDish =  setMealDishMapper.selectByDishId(id);
            if (setmealDish != null) {
                throw new DeletionNotAllowedException("菜品和套餐有关联，不能删除");
            }
        }

        // 校验通过，批量删除菜品表中的菜品数据
        dishMapper.deleteByIds(ids);

        // 4. 批量删除菜品关联的口味数据
        dishFlavorMapper.deleteByDishIds(ids);

    }

    /**
     * 修改菜品及对应的口味数据
     *
     * @param dishDTO 菜品数据传输对象
     */
    @Override
    @Transactional
    public void updateWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        dishMapper.update(dish);

        // 先删除当前菜品对应的所有口味数据
        Long dishId = dishDTO.getId();
        // 将一个单独的菜品 ID 包装成一个轻量级的 List 集合，然后传递给批量删除方法，从而删除该菜品对应的所有旧口味数据。
        dishFlavorMapper.deleteByDishIds(java.util.Collections.singletonList(dishId));

        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && !flavors.isEmpty()) {
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishId);
            });
            // 向口味表插入n条数据
            dishFlavorMapper.insertBatch(flavors);
        }


    }

    /**
     * 菜品起售停售
     *
     * @param status 状态
     * @param id     菜品ID
     */
    @Override
    @Transactional
    public void startOrStop(Integer status, Long id) {
        Dish dish = new Dish();
        dish.setStatus(status);
        dish.setId(id);
        dishMapper.update(dish);

        if (status == StatusConstant.DISABLE) {
            // TODO 待实现：如果是停售操作，还需要将包含当前菜品的套餐也停售
//            List<Long> dishIds = java.util.Collections.singletonList(id);
//            // 根据菜品id查询对应的套餐id
//            List<Long> setmealIds = setMealDishMapper.getSetmealIdsByDishIds(dishIds);
//            if (setmealIds != null && !setmealIds.isEmpty()) {
//                for (Long setmealId : setmealIds) {
//                    // 这里可以调用 setmealMapper 更新套餐状态为停售
//                    // 或者抛出异常提醒用户，根据具体业务需求而定
//                }
//            }
        }
    }

    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    @Override
    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> dishList = dishMapper.list(dish);

        List<DishVO> dishVOList = new java.util.ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d, dishVO);

            // 根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishFlavorMapper.select(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        return dishVOList;
    }
}
