package com.qc.casserver.service.impl;


import com.qc.casserver.common.CustomException;
import com.qc.casserver.common.MyString;
import com.qc.casserver.common.R;
import com.qc.casserver.config.LoginConfig;
import com.qc.casserver.config.MinIoProperties;
import com.qc.casserver.pojo.EmailCode;
import com.qc.casserver.service.CaptchaService;
import com.qc.casserver.service.CommonService;
import com.qc.casserver.service.IRedisService;
import com.qc.casserver.utils.MinIoUtil;
import com.qc.casserver.utils.VerCodeGenerateUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class CommonServiceImpl implements CommonService {
    @Autowired
    MinIoProperties minIoProperties;
    private final IRedisService iRedisService;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private LoginConfig loginConfig;

    @Autowired
    private CaptchaService captchaService;



    @Autowired
    public CommonServiceImpl(IRedisService iRedisService) {
        this.iRedisService = iRedisService;
    }

    public R<String> uploadFileTOMinio(MultipartFile file) {
        try {
            String fileUrl = MinIoUtil.upload(minIoProperties.getBucketName(), file);
            log.info("imageUrl={}",fileUrl);
            String[] split = fileUrl.split("\\?");
            String split1 = split[0].split(minIoProperties.getBucketName()+"/")[1];

            return R.successOnlyObject(split1);
        }catch (Exception e){
            e.printStackTrace();
            throw new CustomException(e.getMessage());
        }
    }

    public String getFileFromMinio(String id){
        if (StringUtils.isEmpty(id)){
            return "";
        }
        return minIoProperties.getUrl()+"/"+minIoProperties.getBucketName()+"/"+id;
    }

    @Override
    public R<String> sendEmailCode(EmailCode emailCode) {


        if (loginConfig.isNeedCaptcha()){
            if (StringUtils.isEmpty(emailCode.getRandomCode())||StringUtils.isEmpty(emailCode.getVerificationCode())){
                throw new CustomException("验证码缺少参数");
            }
            //需要验证码
            String msg = captchaService.checkImageCode(emailCode.getRandomCode(), emailCode.getVerificationCode());
            if (StringUtils.isNotBlank(msg)) {
                throw new CustomException("需要验证码!");
            }
        }

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom("3482238110@qq.com");

        message.setTo(emailCode.getEmail());

        message.setSubject("您本次的验证码是");

        String verCode = VerCodeGenerateUtil.generateVerCode();
        //需要同时存入redis,key使用emailcode:userid

        message.setText("尊敬的用户"
                +",您好:\n"
                + "\n本次请求的邮件验证码为:" + verCode + ",本验证码 5 分钟内效，请及时输入。（请勿泄露此验证码）\n"
                + "\n如非本人操作，请忽略该邮件。\n(这是一封通过自动发送的邮件，请不要直接回复）");


        mailSender.send(message);
        log.info("发送验证码{}",emailCode.getEmail());
        iRedisService.setWithTime(MyString.pre_email_redis +emailCode.getEmail(),verCode, 300L);
        return R.successOnlyMsg("发送成功,请前往你的邮箱获取验证码");
    }

    @Override
    public R<Integer> havaEmailCode(EmailCode emailCode) {
        if (StringUtils.isEmpty(emailCode.getEmail())){
            return R.error("null");
        }
        if (StringUtils.isEmpty(emailCode.getEmailCode())){
            return R.error("null");
        }
        String mailCode = iRedisService.getValue(MyString.pre_email_redis+emailCode.getEmail());
        if (emailCode.getEmailCode().equals(mailCode)){
            return R.success(1);
        }
        return R.success(0);
    }
}