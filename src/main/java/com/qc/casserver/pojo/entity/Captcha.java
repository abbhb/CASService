package com.qc.casserver.pojo.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Captcha {

    /**
     * 随机字符串
     **/
    @JsonProperty("nonce_str")
    private String nonceStr;
    /**
     * 验证值
     **/
    private String value;
    /**
     * 生成的画布的base64
     **/
    @JsonProperty("canvas_src")
    private String canvasSrc;
    /**
     * 画布宽度
     **/
    @JsonProperty("canvas_width")
    private Integer canvasWidth;
    /**
     * 画布高度
     **/
    @JsonProperty("canvas_height")
    private Integer canvasHeight;
    /**
     * 生成的阻塞块的base64
     **/
    @JsonProperty("block_src")
    private String blockSrc;
    /**
     * 阻塞块宽度
     **/
    @JsonProperty("block_width")
    private Integer blockWidth;
    /**
     * 阻塞块高度
     **/
    @JsonProperty("block_height")
    private Integer blockHeight;
    /**
     * 阻塞块凸凹半径
     **/
    @JsonProperty("block_radius")
    private Integer blockRadius;
    /**
     * 阻塞块的横轴坐标
     **/
    @JsonProperty("block_x")
    private Integer blockX;
    /**
     * 阻塞块的纵轴坐标
     **/
    @JsonProperty("block_y")
    private Integer blockY;
    /**
     * 图片获取位置
     **/
    @JsonProperty("place")
    private Integer place;
}
