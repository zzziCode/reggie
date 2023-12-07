package com.zzzi.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zzzi.reggie.common.R;
import com.zzzi.reggie.entity.Employee;
import com.zzzi.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;


@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    /**
     * 员工登录
     *
     * @param request  这个参数用来操作session
     * @param employee 这个参数用来封装前端传递来的参数，将其封装成一个实体
     * @return
     */
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee) {

        //1、将页面提交的密码password进行md5加密处理
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        //2、根据页面提交的用户名username查询数据库】
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        //设置查询条件，查询一条记录
        queryWrapper.eq(Employee::getUsername, employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper);

        //3、如果没有查询到则返回登录失败结果
        if (emp == null) {
            return R.error("用户不存在");
        }

        //4、密码比对，如果不一致则返回登录失败结果
        if (!emp.getPassword().equals(password)) {
            return R.error("密码错误");
        }

        //5、查看员工状态，如果为已禁用状态，则返回员工已禁用结果
        if (emp.getStatus() == 0) {
            return R.error("账号已禁用");
        }

        //6、登录成功，将员工id存入Session并返回登录成功结果
        //有了session就不用每次都需要登录了，只要还在登录状态，那么就直接操作页面
        request.getSession().setAttribute("employee", emp.getId());
        log.info("session:{}", request.getSession());
        return R.success(emp);
    }
    /**@author zzzi
     * @date 2023/12/1 14:53
     * 保存session到服务端的目的是为了进行验证，前端的cookie中会携带当前用户登录之后的sessionId
     * 下次访问页面时的cookie中就保存了这个sessionId，所以后端会用这个sessionId进行验证，如果保存的session中有这个
     * sessionId，那么就直接访问页面，否则要求登录
     */

    /**
     * 员工退出
     *
     * @param request 这个参数用来操作session
     * @return
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request) {
        //清理Session中保存的当前登录员工的id，代表当前用户退出登录
        //下一次再访问页面就需要先登录，然后重新创建一个新的session
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }

    /**
     * @author zzzi
     * @date 2023/12/2 9:04
     * 新增员工的操作
     */
    @PostMapping
    public R<String> save(HttpServletRequest request, @RequestBody Employee employee) {
        log.info("employee:{}", employee);

        //设置当前用户没填充完的属性
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        //employee.setCreateTime(LocalDateTime.now());
        //employee.setUpdateTime(LocalDateTime.now());

        //拿到当前请求的用户sessionId
        //Long empId = (Long) request.getSession().getAttribute("employee");
        //employee.setCreateUser(empId);
        //employee.setUpdateUser(empId);

        //调用业务层接口
        employeeService.save(employee);

        //返回统一封装的结果
        return R.success("员工添加成功");
    }

    //员工信息的分页查询
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        log.info("page={},pageSize={},name={}", page, pageSize, name);

        //分页构造器
        Page pageInfo = new Page(page, pageSize);
        //条件构造器，如果传递了name，不仅需要分页，查询时还有条件
        LambdaQueryWrapper<Employee> lambdaQueryWrapper = new LambdaQueryWrapper();
        //name不为空才构造这个条件
        lambdaQueryWrapper.like(StringUtils.isNotEmpty(name), Employee::getName, name);
        lambdaQueryWrapper.orderByDesc(Employee::getUpdateTime);

        employeeService.page(pageInfo, lambdaQueryWrapper);
        return R.success(pageInfo);
    }

    /**
     * @author zzzi
     * @date 2023/12/2 13:26
     * 编写一个统一的更新方法，不管是编辑还是禁用启用都调用这个方法即可
     * 可以共用的原因是因为禁用的代码在前端
     * 后端接收到的数据已经是修改完状态的数据了
     * 每次更新都需要更新当前记录的更新时间和更新人
     */
    @PutMapping
    public R<String> update(@RequestBody Employee employee) {

        log.info("employee:{}", employee);

        //更新当前记录
        //employee.setUpdateTime(LocalDateTime.now());
        //employee.setUpdateUser((Long) request.getSession().getAttribute("employee"));

        employeeService.updateById(employee);
        return R.success("员工信息修改成功");
    }

    //根据id查询到用户信息并返回
    @GetMapping("/{id}")
    public R<Employee> selectById(@PathVariable String id) {
        log.info("按照id查询员工的id:{}", id);
        //创建一个条件查询器，输入查询的条件
        LambdaQueryWrapper<Employee> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getId,Long.valueOf(id));

        Employee employee = employeeService.getOne(queryWrapper);

        return R.success(employee);
    }

}
