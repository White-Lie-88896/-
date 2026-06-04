package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.WorkspaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 工作台相关接口
 */
@RestController
@RequestMapping("/admin/workspace")
@Api(tags = "工作台相关接口")
@Slf4j
public class WorkspaceController {

    @Autowired
    private WorkspaceService workspaceService;

    /**
     * 工作台今日营业数据概览
     * @return
     */
    @GetMapping("/businessData")
    @ApiOperation("工作台今日营业数据概览")
    public Result<BusinessDataVO> getBusinessData() {
        log.info("查询工作台今日营业数据概览");
        BusinessDataVO businessDataVO = workspaceService.getBusinessData();
        return Result.success(businessDataVO);
    }

    /**
     * 查询今日订单概览数据
     * @return
     */
    @GetMapping("/overviewOrders")
    @ApiOperation("查询今日订单概览数据")
    public Result<OrderOverViewVO> getOrderOverView() {
        log.info("查询今日订单概览数据");
        OrderOverViewVO orderOverViewVO = workspaceService.getOrderOverView();
        return Result.success(orderOverViewVO);
    }

    /**
     * 查询菜品总览
     * @return
     */
    @GetMapping("/overviewDishes")
    @ApiOperation("查询菜品总览")
    public Result<DishOverViewVO> getDishOverView() {
        log.info("查询菜品总览");
        DishOverViewVO dishOverViewVO = workspaceService.getDishOverView();
        return Result.success(dishOverViewVO);
    }

    /**
     * 查询套餐总览
     * @return
     */
    @GetMapping("/overviewSetmeals")
    @ApiOperation("查询套餐总览")
    public Result<SetmealOverViewVO> getSetmealOverView() {
        log.info("查询套餐总览");
        SetmealOverViewVO setmealOverViewVO = workspaceService.getSetmealOverView();
        return Result.success(setmealOverViewVO);
    }
}
