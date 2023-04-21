package com.qc.casserver.pojo.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class Ticket implements Serializable {

    /**
     * Ticket Granting Ticket
     */
    private String tgt;

    /**
     * Ticket Granting Cookie
     */
    private String tgc;
    /**
     * Service Ticket
     */
    private String st;



}
