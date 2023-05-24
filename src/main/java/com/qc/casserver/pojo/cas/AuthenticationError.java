package com.qc.casserver.pojo.cas;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

import java.io.Serializable;

@Data
@JacksonXmlRootElement(localName = "cas:serviceResponse")
public class AuthenticationError implements Serializable {
    @JacksonXmlProperty(isAttribute = true)
    private final String code = "INVALID_TICKET";

    @JacksonXmlProperty(isAttribute = true)
    private final String xmlns = "http://www.yale.edu/tp/cas";

    @JacksonXmlProperty(localName = "cas:authenticationFailure")
    private String message;

}
