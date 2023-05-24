/*
 Navicat Premium Data Transfer

 Source Server         : 正式
 Source Server Type    : MySQL
 Source Server Version : 80031
 Source Host           : 10.15.245.1:13306
 Source Schema         : casserver

 Target Server Type    : MySQL
 Target Server Version : 80031
 File Encoding         : 65001

 Date: 24/05/2023 11:47:06
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for invite_code
-- ----------------------------
DROP TABLE IF EXISTS `invite_code`;
CREATE TABLE `invite_code`  (
  `id` bigint NOT NULL COMMENT 'inviteCode的邀请码',
  `invite_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '邀请码',
  `persistence` int NOT NULL COMMENT '持久化，1为持久化，用完可以接着用',
  `is_deleted` int NOT NULL COMMENT '逻辑删除 1为已删除 0为不删除',
  `usage_count` int NULL DEFAULT 0 COMMENT '使用次数',
  `create_user` bigint NOT NULL COMMENT '谁创建的,只有系统管理员有权限全看到和管理',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for oauth
-- ----------------------------
DROP TABLE IF EXISTS `oauth`;
CREATE TABLE `oauth`  (
  `id` bigint NOT NULL,
  `client_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '客户端id',
  `client_secret` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '客户端秘钥',
  `redirect_uri` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '回调地址，成功回调会自动加上返回参数code',
  `client_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '客户端name',
  `grant_type` int NOT NULL DEFAULT 1 COMMENT '授权类型：grant_type ，1为authorization_code（授权码模式）',
  `create_time` datetime NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT NULL,
  `is_deleted` int NOT NULL DEFAULT 0,
  `delete_time` bigint NOT NULL DEFAULT 1 COMMENT '默认为1，删除为时间戳',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `name`(`client_name` ASC, `delete_time` ASC) USING BTREE,
  UNIQUE INDEX `client_id`(`client_id` ASC, `delete_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for permission
-- ----------------------------
DROP TABLE IF EXISTS `permission`;
CREATE TABLE `permission`  (
  `id` int NOT NULL,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `weight` int NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint NOT NULL COMMENT '用户id',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户昵称',
  `permission` int NOT NULL COMMENT '权限,10为系统管理员，1为管理员，2为用户，后面数字留给其他权限限制',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码',
  `create_time` datetime NOT NULL COMMENT '注册时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `is_deleted` int NOT NULL COMMENT '逻辑删除',
  `sex` varchar(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '性别',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像',
  `status` int NOT NULL COMMENT '状态，1为正常，0为封号',
  `salt` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码加盐',
  `phone` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
  `student_id` bigint NULL DEFAULT NULL COMMENT '学号',
  `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '绑定邮箱',
  `delete_time` bigint NOT NULL DEFAULT 1 COMMENT '默认为1，删除时写入时间戳',
  `openid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '开放id',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC, `is_deleted` ASC, `delete_time` ASC) USING BTREE,
  UNIQUE INDEX `email`(`email` ASC, `is_deleted` ASC, `delete_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

SET FOREIGN_KEY_CHECKS = 1;
