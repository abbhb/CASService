package com.qc.casserver.service;



import com.qc.casserver.common.R;
import com.qc.casserver.pojo.dh.QuickNavigationResult;

import java.util.List;

public interface QuickNavigationService{
    R<List<QuickNavigationResult>> list(Long userId);
}
