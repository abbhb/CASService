package com.qc.casserver.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qc.casserver.common.CustomException;
import com.qc.casserver.common.R;
import com.qc.casserver.mapper.GoogleAuthenticatorMapper;
import com.qc.casserver.pojo.entity.GoogleAuthenticator;
import com.qc.casserver.pojo.entity.User;
import com.qc.casserver.pojo.vo.GoogleAuthenticatorR;
import com.qc.casserver.service.GoogleAuthenticatorService;
import com.qc.casserver.utils.ThreadLocalUtil;
import com.qc.casserver.utils.googleauthenticator.GoogleAuthenticatorUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.iherus.codegen.qrcode.SimpleQrcodeGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Base64;

@Service
@Slf4j
public class GoogleAuthenticatorServiceImpl extends ServiceImpl<GoogleAuthenticatorMapper, GoogleAuthenticator> implements GoogleAuthenticatorService {
    /**
     * @param state 需要修改的状态
     * @return
     */
    @Transactional
    @Override
    public R<String> changeState(Integer state) {
        if (state == null) {
            throw new CustomException("state异常");
        }
        User currentUser = ThreadLocalUtil.getCurrentUser();
        if (currentUser == null) {
            throw new CustomException("currentUser异常");
        }
        LambdaQueryWrapper<GoogleAuthenticator> googleAuthenticatorLambdaQueryWrapper = new LambdaQueryWrapper<>();
        googleAuthenticatorLambdaQueryWrapper.eq(GoogleAuthenticator::getUserId, currentUser.getId());

        GoogleAuthenticator one = this.getOne(googleAuthenticatorLambdaQueryWrapper);
        if (one == null && state.equals(1)) {
            one = new GoogleAuthenticator();
            one.setState(state);
            one.setUserId(currentUser.getId());
            one.setVerify(0);
            one.setSecret(GoogleAuthenticatorUtil.getSecretKey());
            this.save(one);
        } else if (one == null && state.equals(0)) {
            throw new CustomException("请刷新重试！");
        } else if (one != null && state.equals(0)) {
            this.removeById(one.getId());
        } else {
            one.setState(state);
            one.setVerify(0);
            this.updateById(one);
        }

        return R.successOnlyMsg("切换成功");
    }

    @Override
    public R<GoogleAuthenticatorR> getSecret() {
        User currentUser = ThreadLocalUtil.getCurrentUser();
        if (currentUser == null) {
            throw new CustomException("currentUser异常");
        }
        LambdaQueryWrapper<GoogleAuthenticator> googleAuthenticatorLambdaQueryWrapper = new LambdaQueryWrapper<>();
        googleAuthenticatorLambdaQueryWrapper.eq(GoogleAuthenticator::getUserId, currentUser.getId());
        GoogleAuthenticator one = this.getOne(googleAuthenticatorLambdaQueryWrapper);
        if (one == null) {
            return R.success(null);
        }
        GoogleAuthenticatorR googleAuthenticatorR = new GoogleAuthenticatorR();
        googleAuthenticatorR.setId(String.valueOf(one.getId()));
        googleAuthenticatorR.setVerify(one.getVerify());
        googleAuthenticatorR.setState(one.getState());
        googleAuthenticatorR.setSecret(one.getSecret());
        googleAuthenticatorR.setUserId(String.valueOf(one.getUserId()));

        // 生成二维码内容
        String qrCodeText = GoogleAuthenticatorUtil.getQrCodeText(one.getSecret(), "AIENSSO", "");
        // 生成二维码输出
        BufferedImage image = new SimpleQrcodeGenerator().generate(qrCodeText).getImage();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", os);
            googleAuthenticatorR.setQrCode("data:image/png;base64," + new String(Base64.getEncoder().encode(os.toByteArray())));
            os.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return R.success(googleAuthenticatorR);
    }

    @Transactional
    @Override
    public R<GoogleAuthenticatorR> verify(GoogleAuthenticator googleAuthenticator, Long code) {
        User currentUser = ThreadLocalUtil.getCurrentUser();
        if (currentUser == null) {
            throw new CustomException("currentUser异常");
        }
        if (code == null) {
            throw new CustomException("code");
        }

        LambdaQueryWrapper<GoogleAuthenticator> googleAuthenticatorLambdaQueryWrapper = new LambdaQueryWrapper<>();
        googleAuthenticatorLambdaQueryWrapper.eq(GoogleAuthenticator::getUserId, currentUser.getId());
        GoogleAuthenticator one = this.getOne(googleAuthenticatorLambdaQueryWrapper);
        if (one == null) {
            throw new CustomException("GoogleAuthenticator异常");
        }
        if ((one.getState().equals(1) && one.getVerify().equals(1)) || one.getState().equals(0)) {
            throw new CustomException("请重试!");
        }
        boolean b = GoogleAuthenticatorUtil.checkCode(one.getSecret(), code, System.currentTimeMillis());
        if (!b) {
            return R.error("绑定失败:验证失败");
        }
        one.setVerify(1);
        boolean b1 = this.updateById(one);
        if (!b1) {
            throw new CustomException("请重试!");
        }
        GoogleAuthenticatorR googleAuthenticatorR = new GoogleAuthenticatorR();
        BeanUtil.copyProperties(one, googleAuthenticatorR, false);
        return R.success(googleAuthenticatorR);
    }
}
