package com.zzzi.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zzzi.reggie.common.R;
import com.zzzi.reggie.entity.Category;
import com.zzzi.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author zzzi
 * @date 2023/12/2 14:32
 * 分类管理的接口
 */
@Slf4j
@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    //分页查询的功能
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize) {
        log.info("分类管理的分页查询：page:{},pageSize:{}", page, pageSize);
        Page pageInfo = new Page(page, pageSize);


        //分页查询时增加一个排序条件，这样会按照分类的排序字段排序
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(Category::getSort);
        categoryService.page(pageInfo, queryWrapper);
        //查询到分页的数据之后，将其返回
        return R.success(pageInfo);
    }

    //新增菜品分类和套餐分类
    @PostMapping
    public R<Category> addCategory(@RequestBody Category category) {
        log.info("新增菜品分类cattegory:{}", category);

        categoryService.save(category);
        return R.success(category);
    }

    //删除分类
    //当分类关联着菜品或者套餐时，就不能进行删除
    //分类会在菜品或者套餐中关联一个分类id，标记当前菜品或者套餐属于哪个分类
    @DeleteMapping
    public R<String> deleteCategory(Long ids) {
        log.info("根据id删除分类id：{}", ids);
        //调用自定义的删除函数
        categoryService.remove(ids);
        return R.success("删除成功");
    }

    //修改分类
    @PutMapping
    public R<String> updateCategory(@RequestBody Category category) {
        log.info("修改分类:{}", category);
        categoryService.updateById(category);
        return R.success("修改分类成功");
    }

    //查询菜品分类
    @GetMapping("/list")
    //直接根据传递过来的id获取到所有的菜品分类
    //主要是将其封装成什么结果返回
    public R<List<Category>> list(Category category) {
        log.info("传递过来的分类类型为：{}", category.getType());

        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(category.getType() != null, Category::getType, category.getType());
        //先按找排序字段升序，在按照更新时间降序
        queryWrapper.orderByAsc(Category::getSort);
        queryWrapper.orderByDesc(Category::getUpdateTime);

        List<Category> list = categoryService.list(queryWrapper);
        return R.success(list);
    }

}
