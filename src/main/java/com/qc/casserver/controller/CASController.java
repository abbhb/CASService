package com.qc.casserver.controller;

import com.qc.casserver.common.R;
import com.qc.casserver.pojo.UserResult;
import com.qc.casserver.service.AuthService;
import com.qc.casserver.service.IRedisService;
import com.qc.casserver.utils.RandomName;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 原始CAS认证接口
 * CAS1.0
 */
@CrossOrigin("*")
@Slf4j
@RestController//@ResponseBody+@Controller
@RequestMapping("/cas-v1")
public class CASController {
    @Autowired
    private AuthService authService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private IRedisService iRedisService;

    /**
     * 返回字符串yesusername或者no
     *
     * @param ticket
     * @param service
     * @return
     */
    @GetMapping(value = "/validate", produces = "text/plain; charset=UTF-8")
    public String serverTicketTOUserInfo(@RequestParam("ticket") String ticket,@RequestParam("service") String service){
        if (ticket==null){
            return "no\n";
        }

        R<UserResult> userInfoByST = authService.getUserInfoByST(ticket,service);
        if (userInfoByST==null){
            return "no\n";
        }
        if (userInfoByST.getCode().equals(1)) {
            return "yes\n" + userInfoByST.getData().getUsername() + "\n";
        }
        return "no\n";
    }

    @GetMapping("/login")
    public void casLogin(HttpServletResponse response, @RequestParam("service") String service) throws IOException {
        response.sendRedirect("http://10.15.247.254:55554/?service=" + service);
    }

    @GetMapping(value = "/serviceValidate", produces = MediaType.APPLICATION_XML_VALUE)
    public String serviceValidate(HttpServletResponse response, String ticket, String service, String pgtUrl, String format) {
        if (ticket == null) {
            return "<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'>\n" +
                    "    <cas:authenticationFailure code=\"INVALID_REQUEST\">\n" +
                    "        Ticket or service parameter was missing in the request.\n" +
                    "    </cas:authenticationFailure>\n" +
                    "</cas:serviceResponse>";
        }
        return this.CasP3ServiceAndProxyValidate(response, ticket, service, pgtUrl, format);

    }

    //proxyValidate
    @GetMapping(value = "/proxyValidate", produces = MediaType.APPLICATION_XML_VALUE)
    public String proxyValidate(HttpServletResponse response, String ticket, String service, String pgtUrl, String format) {
        if (ticket == null) {
            return "<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'>\n" +
                    "    <cas:authenticationFailure code=\"INVALID_REQUEST\">\n" +
                    "        Ticket or service parameter was missing in the request.\n" +
                    "    </cas:authenticationFailure>\n" +
                    "</cas:serviceResponse>";
        }
        return this.CasP3ServiceAndProxyValidate(response, ticket, service, pgtUrl, format);
    }

    @GetMapping(value = "/proxy", produces = MediaType.APPLICATION_XML_VALUE)
    public String proxyValidate(String pgt, String targetService, String format) {
        if (StringUtils.isEmpty(pgt) || StringUtils.isEmpty(targetService)) {
            return "<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'>\n" +
                    "    <cas:authenticationFailure code=\"INVALID_REQUEST\">\n" +
                    "        Ticket or service parameter was missing in the request.\n" +
                    "    </cas:authenticationFailure>\n" +
                    "</cas:serviceResponse>";
        }
        String value = iRedisService.getValue(pgt);
        if (StringUtils.isEmpty(value)) {
            return "<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'>\n" +
                    "    <cas:authenticationFailure code=\"INVALID_REQUEST\">\n" +
                    "        Ticket or service parameter was missing in the request.\n" +
                    "    </cas:authenticationFailure>\n" +
                    "</cas:serviceResponse>";
        }
        String pgts = value.split("<cas:proxyGrantingTicket>")[1].split("</cas:proxyGrantingTicket>")[0];
        return "<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'>\n" +
                "    <cas:authenticationSuccess>\n" +
                "        <cas:proxyTicket>" + pgts + "</cas:proxyTicket>\n" +
                "    </cas:authenticationSuccess>\n" +
                "</cas:serviceResponse>";
    }

    @GetMapping(value = "/p3/proxyValidate", produces = MediaType.APPLICATION_XML_VALUE)
    public String CasP3ServiceAndProxyValidate(HttpServletResponse response, String ticket, String service, String pgtUrl, String format) {
        if (StringUtils.isEmpty(ticket) || StringUtils.isEmpty(service)) {
            return "<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'>\n" +
                    "    <cas:authenticationFailure code=\"INVALID_REQUEST\">\n" +
                    "        Ticket or service parameter was missing in the request.\n" +
                    "    </cas:authenticationFailure>\n" +
                    "</cas:serviceResponse>";
        }
        R<UserResult> userInfoByST = authService.getUserInfoByST(ticket, service);
        if (userInfoByST == null) {
            return "<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'>\n" +
                    "    <cas:authenticationFailure code=\"INVALID_TICKET\">\n" +
                    "        Ticket " + ticket + " not recognized.\n" +
                    "    </cas:authenticationFailure>\n" +
                    "</cas:serviceResponse>";
        }
        if (userInfoByST.getCode().equals(1)) {

            String sa = "<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'>\n" +
                    "    <cas:authenticationSuccess>\n" +
                    "        <cas:user>" + userInfoByST.getData().getUsername() + "</cas:user>\n" +
                    "        <cas:proxyGrantingTicket>" + ticket + "</cas:proxyGrantingTicket>\n" +
                    "        <cas:proxies>" + "</cas:proxies>\n" +
                    "        <cas:attributes>" + "</cas:attributes>\n" +
                    "    </cas:authenticationSuccess>\n" +
                    "</cas:serviceResponse>";
            if (StringUtils.isNotEmpty(pgtUrl)) {
                String pgt = RandomName.getUUID();
                iRedisService.setWithTime(pgt, sa, 3600 * 2L);
                String pgtUrlas = pgtUrl + "?pgtId=" + pgt + "&pgtIou=" + ticket;
                restTemplate.getForObject(pgtUrlas, String.class);
            }
            return sa;
        }
        return "<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'>\n" +
                "    <cas:authenticationFailure code=\"INVALID_TICKET\">\n" +
                "        Ticket " + ticket + " not recognized.\n" +
                "    </cas:authenticationFailure>\n" +
                "</cas:serviceResponse>";
    }

    @GetMapping(value = "/p3/serviceValidate", produces = MediaType.APPLICATION_XML_VALUE)
    public String CasP3ServiceAndProxyValidates(HttpServletResponse response, String ticket, String service, String pgtUrl, String format) {
        if (StringUtils.isEmpty(ticket) || StringUtils.isEmpty(service)) {
            return "<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'>\n" +
                    "    <cas:authenticationFailure code=\"INVALID_REQUEST\">\n" +
                    "        Ticket or service parameter was missing in the request.\n" +
                    "    </cas:authenticationFailure>\n" +
                    "</cas:serviceResponse>";
        }
        R<UserResult> userInfoByST = authService.getUserInfoByST(ticket, service);
        if (userInfoByST == null) {
            return "<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'>\n" +
                    "    <cas:authenticationFailure code=\"INVALID_TICKET\">\n" +
                    "        Ticket "+ticket+" not recognized.\n" +
                    "    </cas:authenticationFailure>\n" +
                    "</cas:serviceResponse>";
        }
        if (userInfoByST.getCode().equals(1)) {

            String sa = "<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'>\n" +
                    "    <cas:authenticationSuccess>\n" +
                    "        <cas:user>" + userInfoByST.getData().getUsername() + "</cas:user>\n" +
                    "        <cas:proxyGrantingTicket>" + ticket + "</cas:proxyGrantingTicket>\n" +
                    "        <cas:proxies>" + "</cas:proxies>\n" +
                    "        <cas:attributes>" + "</cas:attributes>\n" +
                    "    </cas:authenticationSuccess>\n" +
                    "</cas:serviceResponse>";
            if (StringUtils.isNotEmpty(pgtUrl)) {
                String pgt = RandomName.getUUID();
                iRedisService.setWithTime(pgt, sa, 3600 * 2L);
                String pgtUrlas = pgtUrl + "?pgtId=" + pgt + "&pgtIou=" + ticket;
                restTemplate.getForObject(pgtUrlas, String.class);
            }
            return sa;
        }
        return "<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'>\n" +
                "    <cas:authenticationFailure code=\"INVALID_TICKET\">\n" +
                "        Ticket "+ticket+" not recognized.\n" +
                "    </cas:authenticationFailure>\n" +
                "</cas:serviceResponse>";
    }
}
