package com.qc.casserver.pojo.cas;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.qc.casserver.pojo.entity.User;
import lombok.Data;

import java.io.Serializable;

@Data
@JacksonXmlRootElement(localName = "cas:authenticationSuccess")
public class AuthenticationSuccess implements Serializable {
    @JacksonXmlProperty(localName = "cas:user")
    private String username;

    @JacksonXmlProperty(localName = "cas:proxyGrantingTicket")
    private String proxyGrantingTicket;

    @JacksonXmlProperty(localName = "cas:attributes")
    private Attributes attributes;
}
