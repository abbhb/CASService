package com.qc.casserver.pojo.dh;

import lombok.Data;

import java.io.Serializable;

@Data
public class selectOptionsResult implements Serializable {
    private String label;

    private String value;
}
