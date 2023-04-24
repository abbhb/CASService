package com.qc.casserver.service;

import com.qc.casserver.pojo.entity.Captcha;
import com.qc.casserver.utils.CaptchaUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.util.UUID;

@Service
public class CaptchaService {
    /**
     * 拼图验证码允许偏差
     **/
    private static Integer ALLOW_DEVIATION = 3;

    @Autowired
    private IRedisService iRedisService;

    /**
     * 校验验证码
     * @param imageKey
     * @param imageCode
     * @return boolean
     **/
    public String checkImageCode(String imageKey, String imageCode) {
        String value = iRedisService.getValue("imageCode:"+imageKey);
        if(StringUtils.isEmpty(value)){
            return "失效了";
        }
        // 根据移动距离判断验证是否成功
        if (Math.abs(Integer.parseInt(value) - Integer.parseInt(imageCode)) > ALLOW_DEVIATION) {
            return "验证失败，请控制拼图对齐缺口";
        }
        return null;
    }
    /**
     * 缓存验证码，有效期10分钟
     * @param key
     * @param code
     **/
    public void saveImageCode(String key, String code) {
        iRedisService.setWithTime("imageCode:"+key,code,10*60L);
    }

    /**
     * 获取验证码拼图（生成的抠图和带抠图阴影的大图及抠图坐标）
     **/
    public Captcha getCaptcha(Captcha captcha) {
        //参数校验
        CaptchaUtils.checkCaptcha(captcha);
        //获取画布的宽高
        int canvasWidth = captcha.getCanvasWidth();
        int canvasHeight = captcha.getCanvasHeight();
        //获取阻塞块的宽高/半径
        int blockWidth = captcha.getBlockWidth();
        int blockHeight = captcha.getBlockHeight();
        int blockRadius = captcha.getBlockRadius();
        //获取资源图
        BufferedImage canvasImage = CaptchaUtils.getBufferedImage(captcha.getPlace());
        //调整原图到指定大小
        canvasImage = CaptchaUtils.imageResize(canvasImage, canvasWidth, canvasHeight);
        //随机生成阻塞块坐标
        int blockX = CaptchaUtils.getNonceByRange(blockWidth, canvasWidth - blockWidth - 10);
        int blockY = CaptchaUtils.getNonceByRange(10, canvasHeight - blockHeight + 1);
        //阻塞块
        BufferedImage blockImage = new BufferedImage(blockWidth, blockHeight, BufferedImage.TYPE_4BYTE_ABGR);
        //新建的图像根据轮廓图颜色赋值，源图生成遮罩
        CaptchaUtils.cutByTemplate(canvasImage, blockImage, blockWidth, blockHeight, blockRadius, blockX, blockY);
        // 移动横坐标
        String nonceStr = UUID.randomUUID().toString().replaceAll("-", "");
        // 缓存
        saveImageCode(nonceStr,String.valueOf(blockX));
        //设置返回参数
        captcha.setNonceStr(nonceStr);
        captcha.setBlockY(blockY);
        captcha.setBlockSrc(CaptchaUtils.toBase64(blockImage, "png"));
        captcha.setCanvasSrc(CaptchaUtils.toBase64(canvasImage, "png"));
        return captcha;
    }
}
