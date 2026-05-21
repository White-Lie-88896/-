package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;

@Service
public class
EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;


    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    @Override
    public void save(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();

        // 拷贝employeeDTO里面的属性到employee
        BeanUtils.copyProperties(employeeDTO,employee);

        // 设置默认密码为MD5加密后的123456
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));
        // 设置用户默认状态为启动
        employee.setStatus(StatusConstant.ENABLE);
        // 设置createTime和updateTime
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());

        employee.setCreateUser(BaseContext.getCurrentId());
        employee.setUpdateUser(BaseContext.getCurrentId());

        employeeMapper.insert(employee);


    }

    @Override
    public PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO) {
        int pageSize = employeePageQueryDTO.getPageSize();
        int page = employeePageQueryDTO.getPage();
        PageHelper.startPage(page, pageSize);
        // Page继承了ArrayList，本身就是一个List，会把Mybatis查询返回的数据，employee对象，一个一个加到这个List中去。
        Page<Employee> employees = employeeMapper.pageQuery(employeePageQueryDTO);
        long total = employees.getTotal();
        return new PageResult(total, employees.getResult());

    }

    @Override
    public void updateStatus(Integer status, Long id) {
        // 调用employeeMapper中的方法，根据id修改status

        Employee employee = new Employee();
        employee.setStatus(status);
        employee.setId(id);
//        // 根据id查出对应的employee实体所有属性出来
//        Employee employee = employeeMapper.selectById(id);
//        // 取出当前id对应实体的状态信息
//        Integer OldStatus = employee.getStatus();
//
//        // 判断当前状态，设置新的状态
//        if(OldStatus == StatusConstant.DISABLE){
//            employee.setStatus(StatusConstant.ENABLE);
//        }else{
//            employee.setStatus(StatusConstant.DISABLE);
//        }

        // 直接传一个实体进去修改，是最好的方式，之后可以复用
        employeeMapper.update(employee);
    }


}
