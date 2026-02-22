/*
 Navicat Premium Data Transfer

 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 80043 (8.0.43)
 Source Host           : localhost:3306
 Source Schema         : zhao_dazi_db

 Target Server Type    : MySQL
 Target Server Version : 80043 (8.0.43)
 File Encoding         : 65001

 Date: 26/12/2025 23:33:09
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for activities
-- ----------------------------
DROP TABLE IF EXISTS `activities`;
CREATE TABLE `activities`  (
  `activity_id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `initiator_id` bigint UNSIGNED NOT NULL COMMENT '发起人ID (外键)',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标题 (如: 周末去吃火锅)',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '详细描述/要求',
  `images` json NULL COMMENT '活动配图 - JSON数组',
  `location_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '地点名称',
  `location_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '详细地址',
  `latitude` decimal(10, 8) NULL DEFAULT NULL COMMENT '纬度',
  `longitude` decimal(11, 8) NULL DEFAULT NULL COMMENT '经度',
  `category_ids` json NOT NULL COMMENT '分类ID列表 - JSON数组，支持多分类',
  `start_time` datetime NULL DEFAULT NULL COMMENT '活动开始时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '活动结束时间',
  `registration_end_time` datetime NULL DEFAULT NULL COMMENT '报名结束时间',
  `max_participants` int NOT NULL DEFAULT 2 COMMENT '最大参与人数 (含发起人)',
  `payment_type` tinyint NULL DEFAULT 1 COMMENT '费用方式: 1-AA制, 2-发起人请客, 3-免费, 4-各付各的',
  `status` tinyint NULL DEFAULT 0 COMMENT '状态: 0-招募中, 1-已满员, 2-活动结束(群聊禁言), 3-已取消',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`activity_id`) USING BTREE,
  INDEX `idx_initiator`(`initiator_id` ASC) USING BTREE,
  INDEX `idx_start_time`(`start_time` ASC) USING BTREE,
  INDEX `idx_geo`(`latitude` ASC, `longitude` ASC) USING BTREE COMMENT '简单的地理位置索引'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '搭子活动表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for categories
-- ----------------------------
DROP TABLE IF EXISTS `categories`;
CREATE TABLE `categories`  (
  `category_id` int UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类名称',
  `icon_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '分类图标',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序权重',
  `is_active` tinyint NULL DEFAULT 1 COMMENT '是否启用',
  PRIMARY KEY (`category_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '活动分类表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for chat_conversations
-- ----------------------------
DROP TABLE IF EXISTS `chat_conversations`;
CREATE TABLE `chat_conversations`  (
  `conversation_id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_a_id` bigint UNSIGNED NOT NULL COMMENT '用户A (ID较小者)',
  `user_b_id` bigint UNSIGNED NOT NULL COMMENT '用户B (ID较大者)',
  `last_message_content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '最后一条消息预览',
  `last_message_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后聊天时间',
  PRIMARY KEY (`conversation_id`) USING BTREE,
  UNIQUE INDEX `uniq_conversation`(`user_a_id` ASC, `user_b_id` ASC) USING BTREE,
  INDEX `idx_user_a`(`user_a_id` ASC, `last_message_time` ASC) USING BTREE,
  INDEX `idx_user_b`(`user_b_id` ASC, `last_message_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '私聊会话列表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for chat_messages
-- ----------------------------
DROP TABLE IF EXISTS `chat_messages`;
CREATE TABLE `chat_messages`  (
  `message_id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `sender_id` bigint UNSIGNED NOT NULL COMMENT '发送者ID',
  `receiver_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '接收者ID (1v1私聊时必填, 群聊为NULL)',
  `activity_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '活动ID (群聊时必填, 私聊为NULL)',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消息内容',
  `msg_type` tinyint NULL DEFAULT 1 COMMENT '类型: 1-文本, 2-图片, 3-位置',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`message_id`) USING BTREE,
  INDEX `idx_group_chat`(`activity_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_p2p_chat`(`sender_id` ASC, `receiver_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_p2p_chat_rev`(`receiver_id` ASC, `sender_id` ASC, `created_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '聊天消息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for notifications
-- ----------------------------
DROP TABLE IF EXISTS `notifications`;
CREATE TABLE `notifications`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` bigint UNSIGNED NOT NULL COMMENT '接收人',
  `type` tinyint NOT NULL COMMENT '类型: 1-有人报名, 2-审核通过, 3-审核拒绝, 4-活动即将开始',
  `source_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '关联ID (如activity_id)',
  `content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '通知内容',
  `is_read` tinyint NULL DEFAULT 0 COMMENT '0-未读, 1-已读',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_read`(`user_id` ASC, `is_read` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '消息通知表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for participants
-- ----------------------------
DROP TABLE IF EXISTS `participants`;
CREATE TABLE `participants`  (
  `participant_id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `activity_id` bigint UNSIGNED NOT NULL COMMENT '关联活动ID',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '申请人ID',
  `status` tinyint NULL DEFAULT 0 COMMENT '状态: 0-申请中, 1-已通过(群成员), 2-已拒绝, 3-主动退出',
  `apply_msg` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '申请留言 (如: 我有空，离得近)',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`participant_id`) USING BTREE,
  UNIQUE INDEX `uniq_activity_user`(`activity_id` ASC, `user_id` ASC) USING BTREE COMMENT '防止重复报名',
  INDEX `idx_user_status`(`user_id` ASC, `status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '活动参与记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for reviews
-- ----------------------------
DROP TABLE IF EXISTS `reviews`;
CREATE TABLE `reviews`  (
  `review_id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `activity_id` bigint UNSIGNED NOT NULL,
  `reviewer_id` bigint UNSIGNED NOT NULL COMMENT '评价人',
  `reviewee_id` bigint UNSIGNED NOT NULL COMMENT '被评价人',
  `score` tinyint NOT NULL COMMENT '评分 1-5 星',
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '评价内容',
  `tags` json NULL COMMENT '评价标签 (如: 守时, 幽默)',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`review_id`) USING BTREE,
  INDEX `idx_reviewee`(`reviewee_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户评价表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user_follows
-- ----------------------------
DROP TABLE IF EXISTS `user_follows`;
CREATE TABLE `user_follows`  (
  `follow_id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `follower_id` bigint UNSIGNED NOT NULL COMMENT '关注者ID (谁关注了)',
  `followee_id` bigint UNSIGNED NOT NULL COMMENT '被关注者ID (被谁关注)',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
  PRIMARY KEY (`follow_id`) USING BTREE,
  UNIQUE INDEX `uniq_follow`(`follower_id` ASC, `followee_id` ASC) USING BTREE COMMENT '防止重复关注',
  INDEX `idx_follower`(`follower_id` ASC) USING BTREE COMMENT '查询某用户的关注列表',
  INDEX `idx_followee`(`followee_id` ASC) USING BTREE COMMENT '查询某用户的粉丝列表'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户关注关系表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `user_id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名/登录账号',
  `password_hash` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '加密后的密码',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '昵称',
  `avatar_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像链接',
  `gender` tinyint NULL DEFAULT 0 COMMENT '性别: 0-未知, 1-男, 2-女',
  `birthday` date NULL DEFAULT NULL COMMENT '生日，用于计算年龄',
  `city` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '所在城市',
  `bio` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '个性签名/简介',
  `tags` json NULL COMMENT '个人标签(如: 社牛, 准时, 90后) - JSON格式存储',
  `credit_score` int NULL DEFAULT 100 COMMENT '信用分/靠谱值',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态: 0-封禁, 1-正常',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE INDEX `uniq_username`(`username` ASC) USING BTREE,
  INDEX `idx_city`(`city` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for moments
-- ----------------------------
DROP TABLE IF EXISTS `moments`;
CREATE TABLE `moments`  (
  `moment_id`     bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '动态ID',
  `user_id`       bigint UNSIGNED NOT NULL COMMENT '发布用户ID (外键 -> users)',
  `content`       varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '动态正文',
  `images`        json NULL COMMENT '配图URL数组 - JSON数组，最多9张',
  `location_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '地点名称',
  `location_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '详细地址',
  `visibility`    tinyint NULL DEFAULT 0 COMMENT '可见范围: 0-公开, 1-仅关注者, 2-仅自己',
  `like_count`    int UNSIGNED NULL DEFAULT 0 COMMENT '点赞数 (冗余字段，提升查询性能)',
  `comment_count` int UNSIGNED NULL DEFAULT 0 COMMENT '评论数 (冗余字段)',
  `view_count`    int UNSIGNED NULL DEFAULT 0 COMMENT '浏览数 (冗余字段)',
  `status`        tinyint NULL DEFAULT 1 COMMENT '状态: 0-已删除, 1-正常, 2-审核屏蔽',
  `created_at`    timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  `updated_at`    timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`moment_id`) USING BTREE,
  INDEX `idx_user_created`(`user_id` ASC, `created_at` DESC) USING BTREE COMMENT '查询某人动态列表',
  INDEX `idx_status_created`(`status` ASC, `created_at` DESC) USING BTREE COMMENT '广场/时间线查询'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户动态表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for moment_likes
-- ----------------------------
DROP TABLE IF EXISTS `moment_likes`;
CREATE TABLE `moment_likes`  (
  `like_id`    bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '点赞ID',
  `moment_id`  bigint UNSIGNED NOT NULL COMMENT '动态ID (外键 -> moments)',
  `user_id`    bigint UNSIGNED NOT NULL COMMENT '点赞用户ID (外键 -> users)',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
  PRIMARY KEY (`like_id`) USING BTREE,
  UNIQUE INDEX `uniq_moment_user`(`moment_id` ASC, `user_id` ASC) USING BTREE COMMENT '防止重复点赞',
  INDEX `idx_user_likes`(`user_id` ASC, `created_at` DESC) USING BTREE COMMENT '查询某用户的点赞记录'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '动态点赞记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for moment_comments
-- ----------------------------
DROP TABLE IF EXISTS `moment_comments`;
CREATE TABLE `moment_comments`  (
  `comment_id`  bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `moment_id`   bigint UNSIGNED NOT NULL COMMENT '所属动态ID (外键 -> moments)',
  `user_id`     bigint UNSIGNED NOT NULL COMMENT '评论用户ID (外键 -> users)',
  `parent_id`   bigint UNSIGNED NULL DEFAULT NULL COMMENT '父评论ID，NULL表示一级评论，非NULL表示回复某条评论',
  `reply_to_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '被回复人的用户ID，用于"回复@xxx"展示',
  `content`     varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '评论内容',
  `status`      tinyint NULL DEFAULT 1 COMMENT '状态: 0-已删除, 1-正常',
  `created_at`  timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
  PRIMARY KEY (`comment_id`) USING BTREE,
  INDEX `idx_moment_created`(`moment_id` ASC, `created_at` ASC) USING BTREE COMMENT '查询动态下的所有评论',
  INDEX `idx_parent`(`parent_id` ASC) USING BTREE COMMENT '查询某条评论下的回复'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '动态评论表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
