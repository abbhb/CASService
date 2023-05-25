package com.qc.casserver.pojo.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PageData<T> implements Serializable {
    @JsonProperty("count_id")
    private String countId;

    private Long current;

    private Long pages;

    private List<T> records;

    private Long size;

    @JsonProperty("max_limit")
    private Long MaxLimit;

    private Long total;
}