package com.qc.casserver.pojo.cas;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class Attributes implements Serializable {
    @JacksonXmlProperty(localName = "cas:display_name")
    private String name;

    @JacksonXmlProperty(localName = "cas:user_email")
    private String email;
}
