package com.qc.casserver.controller;


import com.qc.casserver.common.CustomException;
import com.qc.casserver.common.R;
import com.qc.casserver.common.annotation.NeedLogin;
import com.qc.casserver.common.annotation.PermissionCheck;
import com.qc.casserver.pojo.dh.QuickNavigationCategorizeResult;
import com.qc.casserver.pojo.dh.QuickNavigationItemResult;
import com.qc.casserver.pojo.dh.QuickNavigationResult;
import com.qc.casserver.pojo.dh.selectOptionsResult;
import com.qc.casserver.pojo.entity.PageData;
import com.qc.casserver.pojo.entity.QuickNavigationCategorize;
import com.qc.casserver.pojo.entity.QuickNavigationItem;
import com.qc.casserver.service.QuickNavigationCategorizeService;
import com.qc.casserver.service.QuickNavigationItemService;
import com.qc.casserver.service.QuickNavigationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController//@ResponseBody+@Controller
@RequestMapping("/quicknavigation")
@CrossOrigin("*")
@Slf4j
public class QuickNavigationController {


    private final QuickNavigationCategorizeService quickNavigationCategorizeService;
    private final QuickNavigationService quickNavigationService;
    private final QuickNavigationItemService quickNavigationItemService;

    @Autowired
    public QuickNavigationController(QuickNavigationCategorizeService quickNavigationCategorizeService, QuickNavigationService quickNavigationService, QuickNavigationItemService quickNavigationItemService) {
        this.quickNavigationCategorizeService = quickNavigationCategorizeService;
        this.quickNavigationService = quickNavigationService;
        this.quickNavigationItemService = quickNavigationItemService;
    }

    @NeedLogin
    @GetMapping("/list")
    //后期可以传回token拿到用户信息
    public R<List<QuickNavigationResult>> list(String userId) {
        if (StringUtils.isEmpty(userId)){
            return R.error("传参错误");
        }
        return quickNavigationService.list(Long.valueOf(userId));
    }

    /**
     * 导航分类管理系统\管理员
     * @return
     */
    @NeedLogin
    @PermissionCheck("1")
    @GetMapping("/listnavfenlei")
    //后期可以传回token拿到用户信息
    public R<PageData<QuickNavigationCategorizeResult>> listNavFenLei(Integer pageNum, Integer pageSize, String name) {
        return quickNavigationCategorizeService.listNavFenLei(pageNum,pageSize,name);

    }

    /**
     * 权限等用注解后期实现,通过过滤器
     * @param quickNavigationItem
     * @return
     */
    @NeedLogin
    @PermissionCheck("1")
    @PostMapping("/createItem")
    public R<String> createItem(@RequestBody QuickNavigationItem quickNavigationItem){
//        System.out.println("quickNavigationItem = " + quickNavigationItem);

        return quickNavigationItemService.createNavItem(quickNavigationItem);

    }

    /**
     * 权限等用注解后期实现,通过过滤器
     * @param quickNavigationCategorize
     * @return
     */
    @NeedLogin
    @PermissionCheck("1")
    @PostMapping("/createCategorize")
    public R<String> createCategorize(@RequestBody QuickNavigationCategorize quickNavigationCategorize){
//        System.out.println("quickNavigationCategorize = " + quickNavigationCategorize);

        return quickNavigationCategorizeService.createNavCategorize(quickNavigationCategorize);

    }

    /**
     * 导航分类管理系统
     * @return
     */
    @NeedLogin
    @GetMapping("/listnavfenleiitem")
    //后期可以传回token拿到用户信息
    public R<PageData<QuickNavigationItemResult>> listNavFenLeiItem(Integer pageNum, Integer pageSize, String name, String selectCate) {
//        log.info("selectCate={}",selectCate);
        return quickNavigationItemService.listNavFenLeiItem(pageNum,pageSize,name,selectCate);

    }

    /**
     * @return 返回分类选择的列表
     */
    @NeedLogin
    @GetMapping("/getCategorizeSelectOptionsList")
    //后期可以传回token拿到用户信息
    public R<List<selectOptionsResult>> getCategorizeSelectOptionsList() {
        return quickNavigationCategorizeService.getCategorizeSelectOptionsList();

    }



    @NeedLogin
    @PermissionCheck("1")
    @PutMapping("/updataforquicknavigationcategorize")
    public R<String> updataForQuickNavigationCategorize(@RequestBody QuickNavigationCategorize quickNavigation){

        if (StringUtils.isEmpty(quickNavigation.getName())){
            return R.error("更新失败");
        }
        if (quickNavigation.getId()==null){
            return R.error("更新失败");
        }
        return quickNavigationCategorizeService.updataForQuickNavigationCategorize(quickNavigation);
    }

    @NeedLogin
    @PermissionCheck("1")
    @PutMapping("/updataforquicknavigationitem")
    public R<String> updataForQuickNavigationItem(@RequestBody QuickNavigationItem quickNavigationItem){

        if (StringUtils.isEmpty(quickNavigationItem.getName())){
            return R.error("更新失败");
        }
        if (quickNavigationItem.getId()==null){
            return R.error("更新失败");
        }
        if (StringUtils.isEmpty(quickNavigationItem.getPermission())){
            return R.error("更新失败");
        }

        if (quickNavigationItem.getType()==null){
            throw new CustomException("必参缺少");
        }
        if (quickNavigationItem.getType().equals(0)) {
            if(StringUtils.isEmpty(quickNavigationItem.getPath())){
                throw new CustomException("必参缺少");
            }
        }
        if (quickNavigationItem.getType().equals(1)) {
            if(StringUtils.isEmpty(quickNavigationItem.getContent())){
                throw new CustomException("必参缺少");
            }
        }
        return quickNavigationItemService.updataForQuickNavigationItem(quickNavigationItem);
    }

    @NeedLogin
    @PermissionCheck("1")
    @DeleteMapping("/deleteCategorize")
    public R<String> deleteNavigationCategorize(String id){
        log.info("id = {}",id);
        if (StringUtils.isEmpty(id)){
            return R.error("无操作对象");
        }
        return quickNavigationCategorizeService.deleteNavigationCategorize(id);

    }
    @NeedLogin
    @PermissionCheck("1")
    @DeleteMapping("/deleteItem")
    public R<String> deleteNavigationItem(String id){
        log.info("id = {}",id);
        if (StringUtils.isEmpty(id)){
            return R.error("无操作对象");
        }
        return quickNavigationItemService.deleteNavigationItem(id);

    }

}
