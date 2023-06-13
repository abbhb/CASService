package com.qc.casserver.pojo.dh;

import lombok.Data;

import java.util.List;

@Data
public class QuickNavigationResult {
    private String id;
    private String name;

    private List<QuickNavigationItemResult> item;
}
