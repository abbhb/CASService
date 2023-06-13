package com.qc.casserver.pojo.dh;

import lombok.Data;

import java.util.List;

@Data
public class QuickNavigationItemResult {
    private String id;
    private String name;
    private String path;
    private List<Integer> permission;
    private String image;
    private String introduction;

    private String categorizeId;

    private String categorizeName;

    /**
     * 0:url
     * 1:md
     */
    private Integer type;

    private String content;
}
