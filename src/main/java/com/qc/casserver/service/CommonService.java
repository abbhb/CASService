package com.qc.casserver.service;

import com.qc.casserver.common.R;
import com.qc.casserver.pojo.EmailCode;
import org.springframework.web.multipart.MultipartFile;

public interface CommonService {

    R<String> uploadFileTOMinio(MultipartFile file);

    String getFileFromMinio(String id);

    R<String> sendEmailCode(EmailCode email);
}