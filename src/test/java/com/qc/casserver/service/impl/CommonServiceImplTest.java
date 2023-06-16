package com.qc.casserver.service.impl;

import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

class CommonServiceImplTest {

        @org.junit.jupiter.api.Test
        void uploadFileTOMinio() throws IOException {
                File file = new File("E:\\web\\111\\img\\video.mov");
                MultipartFile cMultiFile = new MockMultipartFile("file", file.getName(), null, Files.newInputStream(file.toPath()));
                CommonServiceImpl commonService = new CommonServiceImpl(null);
                // 传入null 即可实现网络两次元头像
                System.out.println(commonService.uploadFileTOMinio(cMultiFile));
        }


}