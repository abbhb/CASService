package com.qc.casserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qc.casserver.common.R;
import com.qc.casserver.pojo.dh.QuickNavigationCategorizeResult;
import com.qc.casserver.pojo.entity.PageData;
import com.qc.casserver.pojo.entity.QuickNavigationCategorize;
import com.qc.casserver.pojo.dh.selectOptionsResult;

import java.util.List;

public interface QuickNavigationCategorizeService extends IService<QuickNavigationCategorize> {
    R<PageData<QuickNavigationCategorizeResult>> listNavFenLei(Integer pageNum, Integer pageSize, String name);

    R<String> updataForQuickNavigationCategorize(QuickNavigationCategorize quickNavigation);

    R<String> deleteNavigationCategorize(String id);


    R<List<selectOptionsResult>> getCategorizeSelectOptionsList();

    R<String> createNavCategorize(QuickNavigationCategorize quickNavigationCategorize);
}
