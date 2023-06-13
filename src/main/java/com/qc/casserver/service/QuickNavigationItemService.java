package com.qc.casserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qc.casserver.common.R;
import com.qc.casserver.pojo.dh.QuickNavigationItemResult;
import com.qc.casserver.pojo.entity.PageData;
import com.qc.casserver.pojo.entity.QuickNavigationItem;


public interface QuickNavigationItemService extends IService<QuickNavigationItem> {
    boolean hasId(Long valueOf);

    R<PageData<QuickNavigationItemResult>> listNavFenLeiItem(Integer pageNum, Integer pageSize, String name, String selectCate);

    R<String> createNavItem(QuickNavigationItem quickNavigationItem);

    R<String> deleteNavigationItem(String id);

    R<String> updataForQuickNavigationItem(QuickNavigationItem quickNavigationItem);
}
