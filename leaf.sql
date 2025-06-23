/*
 Navicat Premium Data Transfer

 Source Server         : local
 Source Server Type    : MySQL
 Source Server Version : 80021 (8.0.21)
 Source Host           : localhost:3306
 Source Schema         : leaf

 Target Server Type    : MySQL
 Target Server Version : 80021 (8.0.21)
 File Encoding         : 65001

 Date: 24/06/2025 01:10:02
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for leaf_alloc
-- ----------------------------
DROP TABLE IF EXISTS `leaf_alloc`;
CREATE TABLE `leaf_alloc`  (
  `biz_tag` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '区分业务',
  `max_id` bigint NOT NULL DEFAULT 1 COMMENT '表示该 biz_tag 目前所被分配的 ID 号段的最大值',
  `step` int NOT NULL COMMENT '表示每次分配的号段长度',
  `description` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`biz_tag`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of leaf_alloc
-- ----------------------------
INSERT INTO `leaf_alloc` VALUES ('leaf-segment-comment-id', 1, 2000, '评论 ID', '2025-02-10 23:05:54');
INSERT INTO `leaf_alloc` VALUES ('leaf-segment-LittleRedBook-id', 10100, 2000, '小红书 ID', '2025-01-14 00:42:46');
INSERT INTO `leaf_alloc` VALUES ('leaf-segment-test', 10001, 2000, 'Test leaf Segment Mode Get Id', '2025-01-26 20:26:06');
INSERT INTO `leaf_alloc` VALUES ('leaf-segment-user-id', 100, 2000, '用户 ID', '2025-01-14 00:47:11');

SET FOREIGN_KEY_CHECKS = 1;
