package com.qc.casserver.controller;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.qc.casserver.common.R;
import com.qc.casserver.pojo.UserResult;
import com.qc.casserver.pojo.entity.Ticket;
import com.qc.casserver.pojo.entity.Token;
import com.qc.casserver.pojo.entity.User;
import com.qc.casserver.service.AuthService;
import com.qc.casserver.service.IRedisService;
import com.qc.casserver.service.OauthService;
import com.qc.casserver.utils.JWTUtil;
import com.qc.casserver.utils.RandomName;
import com.qc.casserver.utils.TicketUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

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
    private IRedisService iRedisService;
    /**
     * 返回字符串yesusername或者no
     * @param ticket
     * @param service
     * @return
     */
    @GetMapping(value = "/validate",produces = "text/plain; charset=UTF-8")
    public String serverTicketTOUserInfo(@RequestParam("ticket") String ticket,@RequestParam("service") String service){
        if (ticket==null){
            return "no\n";
        }

        R<UserResult> userInfoByST = authService.getUserInfoByST(ticket,service);
        if (userInfoByST==null){
            return "no\n";
        }
        if (userInfoByST.getCode().equals(1)){
            return "yes\n"+userInfoByST.getData().getUsername()+"\n";
        }
        return "no\n";
    }
    @GetMapping("/login")
    public void casLogin(HttpServletResponse response, @RequestParam("service")String service) throws IOException {
        response.sendRedirect("http://10.15.245.1:55554/?service="+service);
    }

    /**
     * serviceValidate
     * 暂时不支持proxyValidate
     */
    @GetMapping(value = "/serviceValidate",produces = MediaType.APPLICATION_XML_VALUE)
    public String serviceValidate(@RequestParam("ticket") String ticket,@RequestParam("service") String service,String pgtUrl){
        if (ticket==null){
            return "<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'>\n" +
                    "    <cas:authenticationFailure code=\"INVALID_REQUEST\">\n" +
                    "        Ticket or service parameter was missing in the request.\n" +
                    "    </cas:authenticationFailure>\n" +
                    "</cas:serviceResponse>";
        }
        R<UserResult> userInfoByST = authService.getUserInfoByST(ticket,service);
        if (userInfoByST==null){
            return "<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'>\n" +
                    "    <cas:authenticationFailure code=\"INVALID_TICKET\">\n" +
                    "        Ticket "+ticket+" not recognized.\n" +
                    "    </cas:authenticationFailure>\n" +
                    "</cas:serviceResponse>";
        }
        if (userInfoByST.getCode().equals(1)) {
            if (StringUtils.isNotEmpty(pgtUrl)) {
                //生成pgt
                //传统CAS登录
                String pgt = RandomName.getUUID();
                //15过期的st,防止网络缓慢
                iRedisService.setTicket(pgt,userInfoByST.getData().getId());
                return "<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'>\n" +
                        "    <cas:authenticationSuccess>\n" +
                        "        <cas:user>" + userInfoByST.getData().getUsername() + "</cas:user>\n" +
                        "        <cas:proxyGrantingTicket>" + pgt + "</cas:proxyGrantingTicket>\n" +
                        "    </cas:authenticationSuccess>\n" +
                        "</cas:serviceResponse>";

            } else {

                return "<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'>\n" +
                        "    <cas:authenticationSuccess>\n" +
                        "        <cas:user>" + userInfoByST.getData().getUsername() + "</cas:user>\n" +
                        "    </cas:authenticationSuccess>\n" +
                        "</cas:serviceResponse>";

            }
        }

        return "<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'>\n" +
                "    <cas:authenticationFailure code=\"INVALID_TICKET\">\n" +
                "        Ticket "+ticket+" not recognized.\n" +
                "    </cas:authenticationFailure>\n" +
                "</cas:serviceResponse>";
    }
    //proxyValidate
    @GetMapping(value = "/proxyValidate",produces = MediaType.APPLICATION_XML_VALUE)
    public String proxyValidate(@RequestParam("ticket") String ticket,@RequestParam("service") String service,String pgtUrl){
        if (ticket==null){
            return "<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'>\n" +
                    "    <cas:authenticationFailure code=\"INVALID_REQUEST\">\n" +
                    "        Ticket or service parameter was missing in the request.\n" +
                    "    </cas:authenticationFailure>\n" +
                    "</cas:serviceResponse>";
        }
        R<UserResult> userInfoByST = authService.getUserInfoByST(ticket,service);
        if (userInfoByST==null){
            return "<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'>\n" +
                    "    <cas:authenticationFailure code=\"INVALID_TICKET\">\n" +
                    "        Ticket "+ticket+" not recognized.\n" +
                    "    </cas:authenticationFailure>\n" +
                    "</cas:serviceResponse>";
        }
        if (userInfoByST.getCode().equals(1)){
            return "<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'>\n" +
                    "    <cas:authenticationSuccess>\n" +
                    "        <cas:user>"+userInfoByST.getData().getUsername()+"</cas:user>\n" +
                    "        <cas:proxyGrantingTicket>"+ ticket+"</cas:proxyGrantingTicket>\n" +
                    "    </cas:authenticationSuccess>\n" +
                    "</cas:serviceResponse>";
        }
        return "<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'>\n" +
                "    <cas:authenticationFailure code=\"INVALID_TICKET\">\n" +
                "        Ticket "+ticket+" not recognized.\n" +
                "    </cas:authenticationFailure>\n" +
                "</cas:serviceResponse>";
    }
}
