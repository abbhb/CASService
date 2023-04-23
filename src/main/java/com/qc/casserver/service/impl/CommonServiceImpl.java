package com.qc.casserver.service.impl;


import com.qc.casserver.common.CustomException;
import com.qc.casserver.common.MyString;
import com.qc.casserver.common.R;
import com.qc.casserver.config.MinIoProperties;
import com.qc.casserver.service.CommonService;
import com.qc.casserver.service.IRedisService;
import com.qc.casserver.service.UserService;
import com.qc.casserver.utils.MinIoUtil;
import com.qc.casserver.utils.VerCodeGenerateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class CommonServiceImpl implements CommonService {
    @Autowired
    MinIoProperties minIoProperties;
    private final IRedisService iRedisService;

    private final UserService userService;
    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    public CommonServiceImpl(IRedisService iRedisService, UserService userService) {
        this.iRedisService = iRedisService;
        this.userService = userService;
    }

    public R<String> uploadFileTOMinio(MultipartFile file) {
        try {
            String fileUrl = MinIoUtil.upload(minIoProperties.getBucketName(), file);
            log.info("imageUrl={}",fileUrl);
            String[] split = fileUrl.split("\\?");

            return R.successOnlyObject(split[0]);
        }catch (Exception e){
            e.printStackTrace();
            throw new CustomException(e.getMessage());
        }
    }

    @Override
    public R<String> sendEmailCode(String email) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom("3482238110@qq.com");

        message.setTo(email);

        message.setSubject("您本次的验证码是");

        String verCode = VerCodeGenerateUtil.generateVerCode();
        //需要同时存入redis,key使用emailcode:userid

        message.setText("尊敬的用户"
                +",您好:\n"
                + "\n本次请求的邮件验证码为:" + verCode + ",本验证码 5 分钟内效，请及时输入。（请勿泄露此验证码）\n"
                + "\n如非本人操作，请忽略该邮件。\n(这是一封通过自动发送的邮件，请不要直接回复）");


        mailSender.send(message);
        log.info("发送验证码{}",email);
        iRedisService.setTokenWithTime(MyString.pre_email_redis +email,verCode, 300L);
        return R.success("发送成功,请前往你的邮箱获取验证码");
    }
}