package com.qc.casserver.utils;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.Random;

public class ImageUtil {

    //BufferedImage 转base64
    public static String GetBase64FromImage(BufferedImage img){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            // 设置图片的格式
            ImageIO.write(img, "png", stream);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Base64.Encoder encoder = Base64.getEncoder();
        String base64 = encoder.encodeToString(stream.toByteArray());
        return  "data:image/png;base64,"+base64;
    }

    /**
     * 将MultipartFile 图片文件编码为base64
     */
    public static String generateBase64(MultipartFile file){
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("图片不能为空！");
        }
        String fileName = file.getOriginalFilename();
        String fileType = fileName.substring(fileName.lastIndexOf("."));
        String contentType = file.getContentType();
        byte[] imageBytes = null;
        String base64EncoderImg="";
        try {
            imageBytes = file.getBytes();
            String s = Base64.getEncoder().encodeToString(imageBytes);
            /**
             * 1.Java使用BASE64Encoder 需要添加图片头（"data:" + contentType + ";base64,"），
             *   其中contentType是文件的内容格式。
             * 2.Java中在使用BASE64Enconder().encode()会出现字符串换行问题，这是因为RFC 822中规定，
             *   每72个字符中加一个换行符号，这样会造成在使用base64字符串时出现问题，
             *   所以我们在使用时要先用replaceAll("[\\s*\t\n\r]", "")解决换行的问题。
             */
            base64EncoderImg = "data:" + contentType + ";base64," + s;
            base64EncoderImg = base64EncoderImg.replaceAll("[\\s*\t\n\r]", "");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return base64EncoderImg;
    }

    public static void drawStringOnImage(Graphics2D graphics, String str){
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        //设置字体
        Font font = new Font("黑体", Font.PLAIN, 36);
        graphics.setFont(font);
        //设置颜色
        graphics.setColor(Color.BLACK);
        //向画板上写字
        graphics.drawString(str, 70, 340);
        //释放资源
        graphics.dispose();
    }

    public static BufferedImage resize(BufferedImage image, int i, int i1) throws IOException {
        // https://blog.csdn.net/lcj_star/article/details/76637931
        return Thumbnails.of(image).size(i, i1).outputFormat("png").outputQuality(0.7).asBufferedImage();
    }


}
