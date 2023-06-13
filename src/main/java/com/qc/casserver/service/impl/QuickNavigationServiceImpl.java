package com.qc.casserver.service.impl;



import com.qc.casserver.common.CustomException;
import com.qc.casserver.common.R;
import com.qc.casserver.pojo.dh.QuickNavigationItemResult;
import com.qc.casserver.pojo.dh.QuickNavigationResult;
import com.qc.casserver.pojo.entity.QuickNavigationCategorize;
import com.qc.casserver.pojo.entity.QuickNavigationItem;
import com.qc.casserver.pojo.entity.User;
import com.qc.casserver.service.QuickNavigationCategorizeService;
import com.qc.casserver.service.QuickNavigationItemService;
import com.qc.casserver.service.QuickNavigationService;
import com.qc.casserver.service.UserService;
import com.qc.casserver.utils.ThreadLocalUtil;
import com.qc.casserver.utils.permissionstringsplit.MySplit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class QuickNavigationServiceImpl implements QuickNavigationService {

    private final QuickNavigationItemService quickNavigationItemService;
    private final QuickNavigationCategorizeService quickNavigationCategorizeService;

    private final UserService userService;
    @Autowired
    public QuickNavigationServiceImpl(QuickNavigationItemService quickNavigationItemService, QuickNavigationCategorizeService quickNavigationCategorizeService, UserService userService) {
        this.quickNavigationItemService = quickNavigationItemService;
        this.quickNavigationCategorizeService = quickNavigationCategorizeService;
        this.userService = userService;
    }

    @Override
    public R<List<QuickNavigationResult>> list(Long userId) {
        User currentUser = ThreadLocalUtil.getCurrentUser();
        if (currentUser==null){
            return R.error("异常1");
        }
        if (userId==null){
            return R.error("异常");
        }
        List<QuickNavigationCategorize> quickNavigationCategorizes = quickNavigationCategorizeService.list();
        List<QuickNavigationItem> quickNavigationItems = quickNavigationItemService.list();
        List<QuickNavigationResult> quickNavigationResults = new ArrayList<>();


        for (QuickNavigationCategorize quickNavigationCategorize:
                quickNavigationCategorizes) {
            QuickNavigationResult quickNavigationResult = new QuickNavigationResult();
            quickNavigationResult.setId(String.valueOf(quickNavigationCategorize.getId()));
            quickNavigationResult.setName(quickNavigationCategorize.getName());
            List<QuickNavigationItem> result1 = quickNavigationItems.stream()
                    .filter(item -> item.getCategorizeId().equals(quickNavigationCategorize.getId()))
                    .collect(Collectors.toList());

            List<QuickNavigationItemResult> quickNavigationItemResults = new ArrayList<>();
            for (QuickNavigationItem quickNavigationItem:
                    result1) {
                QuickNavigationItemResult quickNavigationItemResult = new QuickNavigationItemResult();
                quickNavigationItemResult.setId(String.valueOf(quickNavigationItem.getId()));
                quickNavigationItemResult.setName(quickNavigationItem.getName());
                quickNavigationItemResult.setPath(quickNavigationItem.getPath());
                quickNavigationItemResult.setImage(quickNavigationItem.getImage());
                quickNavigationItemResult.setIntroduction(quickNavigationItem.getIntroduction());
                quickNavigationItemResult.setType(quickNavigationItem.getType());
                //markdown
                quickNavigationItemResult.setContent(MySplit.splitString(quickNavigationItem.getContent(),currentUser.getPermission()));
                quickNavigationItemResult.setCategorizeId(String.valueOf(quickNavigationItem.getCategorizeId()));
                QuickNavigationCategorize quickNavigationCategorizeServiceById = quickNavigationCategorizeService.getById(quickNavigationItem.getCategorizeId());
                if (quickNavigationCategorizeServiceById==null){
                    throw new CustomException("运行异常");
                }
                quickNavigationItemResult.setCategorizeName(quickNavigationCategorizeServiceById.getName());
                String permission = quickNavigationItem.getPermission();
                if (permission.contains(",")){
                    List<Integer> integerList = new ArrayList<>();
                    String[] split = permission.split(",");
                    for (String s:
                         split) {
                        integerList.add(Integer.valueOf(s));
                    }
                    quickNavigationItemResult.setPermission(integerList);
                }else {
                    List<Integer> integerList = new ArrayList<>();
                    integerList.add(Integer.valueOf(permission));
                    quickNavigationItemResult.setPermission(integerList);
                }
                quickNavigationItemResults.add(quickNavigationItemResult);
            }
            quickNavigationResult.setItem(quickNavigationItemResults);
            quickNavigationResults.add(quickNavigationResult);
        }
        return R.success(quickNavigationResults);
    }
}
