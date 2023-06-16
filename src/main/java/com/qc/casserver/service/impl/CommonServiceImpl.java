package com.qc.casserver.service.impl;


import com.qc.casserver.common.CustomException;
import com.qc.casserver.common.MyString;
import com.qc.casserver.common.R;
import com.qc.casserver.config.LoginConfig;
import com.qc.casserver.pojo.EmailCode;
import com.qc.casserver.service.CaptchaService;
import com.qc.casserver.service.CommonService;
import com.qc.casserver.service.IRedisService;
import com.qc.casserver.utils.ImageUtil;
import com.qc.casserver.utils.VerCodeGenerateUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


@Service
@Slf4j
public class CommonServiceImpl implements CommonService {
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
//        try {
//            String fileUrl = MinIoUtil.upload(minIoProperties.getBucketName(), file);
//            log.info("imageUrl={}",fileUrl);
//            String[] split = fileUrl.split("\\?");
//            String split1 = split[0].split(minIoProperties.getBucketName()+"/")[1];
//
//            return R.successOnlyObject(split1);
//        }catch (Exception e){
//            e.printStackTrace();
//            throw new CustomException(e.getMessage());
//        }
        /* String s = ImageToBase64.generateBase64(file);*/
        // 小奇实现的图片算法
        BufferedImage image = new BufferedImage(360, 360, BufferedImage.TYPE_INT_BGR);
        try {
            ClassPathResource classPathResource = new ClassPathResource("/img/default_avatar.png");
            InputStream inputStreamImg = classPathResource.getInputStream();
            BufferedImage image_read = ImageIO.read(inputStreamImg);
            if (image_read == null){
                throw new CustomException("无法读取默认头像");
            }
            image = image_read;
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(network_check()){
            // 有网络连接
            try {
                URL url = new URL("https://img.xjh.me/random_img.php?return=302");
                HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5 * 1000);
                InputStream inputStreamImg = conn.getInputStream();
                BufferedImage image_read = ImageIO.read(inputStreamImg);
                if (image_read == null){
                    throw new CustomException("无法从网络读取头像");
                }
                image = image_read;
                image_read = ImageUtil.resize(image, 360, 360);
                if (image_read == null){
                    throw new CustomException("图像无法被压缩");
                }
                image = image_read;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Graphics2D graphics = image.createGraphics();
        if (file == null){
            // 允许接口空调用
            /*ImageUtil.drawStringOnImage(graphics, "图片文件为空");*/
            String s = ImageUtil.GetBase64FromImage(image);
            return R.successOnlyObject(s);
        }
        if (file.isEmpty()){
            ImageUtil.drawStringOnImage(graphics, "图片文件为空");
            String s = ImageUtil.GetBase64FromImage(image);
            return R.successOnlyObject(s);
        }
        // 限制图片大小为3M
        if (file.getSize() > 1024 * 1024 * 30){
            ImageUtil.drawStringOnImage(graphics, "图片文件过大");
            String s = ImageUtil.GetBase64FromImage(image);
            return R.successOnlyObject(s);
        }
        try {
            InputStream inputStreamImg = file.getInputStream();
            BufferedImage image_read = ImageIO.read(inputStreamImg);
            if (image_read == null){
                throw new CustomException("上传的文件不是图片");
            }
            image = image_read;
        } catch (Exception e) {
            // 上传了个什么JB玩让我读不了
            e.printStackTrace();
            ImageUtil.drawStringOnImage(graphics, "图片文件破损");
            String s = ImageUtil.GetBase64FromImage(image);
            return R.successOnlyObject(s);
        }
        // 压缩图片到360*360
        try {
            BufferedImage image_read = ImageUtil.resize(image, 360, 360);
            if (image_read == null){
                throw new CustomException("图像无法被压缩");
            }
            image = image_read;
        } catch (Exception e) {
            // 什么JB图片，压缩不了
            e.printStackTrace();
            ImageUtil.drawStringOnImage(graphics, "图片压缩失败");
            String s = ImageUtil.GetBase64FromImage(image);
            return R.successOnlyObject(s);
        }
        String s = ImageUtil.GetBase64FromImage(image);
        return R.successOnlyObject(s);
    }

    public String getFileFromMinio(String id) {
        //升级为base64
//        if (StringUtils.isEmpty(id)){
//            return "";
//        }
//        return minIoProperties.getUrl()+"/"+minIoProperties.getBucketName()+"/"+id;
        return id;
    }

    @Override
    public R<String> sendEmailCode(EmailCode emailCode) {


        if (loginConfig.isNeedCaptcha()) {
            if (StringUtils.isEmpty(emailCode.getRandomCode()) || StringUtils.isEmpty(emailCode.getVerificationCode())) {
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
                + ",您好:\n"
                + "\n本次请求的邮件验证码为:" + verCode + ",本验证码 5 分钟内效，请及时输入。（请勿泄露此验证码）\n"
                + "\n如非本人操作，请忽略该邮件。\n(这是一封通过自动发送的邮件，请不要直接回复）");


        mailSender.send(message);
        log.info("发送验证码{}", emailCode.getEmail());
        iRedisService.setWithTime(MyString.pre_email_redis + emailCode.getEmail(), verCode, 300L);
        return R.successOnlyMsg("发送成功,请前往你的邮箱获取验证码");
    }

    @Override
    public R<Integer> havaEmailCode(EmailCode emailCode) {
        if (StringUtils.isEmpty(emailCode.getEmail())) {
            return R.error("null");
        }
        if (StringUtils.isEmpty(emailCode.getEmailCode())) {
            return R.error("null");
        }
        String mailCode = iRedisService.getValue(MyString.pre_email_redis + emailCode.getEmail());
        if (emailCode.getEmailCode().equals(mailCode)) {
            return R.success(1);
        }
        return R.success(0);
    }

    private static boolean network_check() {
        // 204 检测联网大法
        String Check_url = "https://connect.rom.miui.com/generate_204";
        try {
            URL url = new URL(Check_url);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            // 设置连接方式：get
            connection.setRequestMethod("GET");
            // 设置连接主机服务器的超时时间：15000毫秒
            connection.setConnectTimeout(3000);
            // 设置读取远程返回的数据时间：60000毫秒
            connection.setReadTimeout(3000);
            // 发送请求
            connection.connect();
            return connection.getResponseCode() == 204;
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }
}