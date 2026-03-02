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

 Date: 28/02/2026 18:59:24
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
  `start_time` datetime NOT NULL COMMENT '活动开始时间',
  `max_participants` int NOT NULL DEFAULT 2 COMMENT '最大参与人数 (含发起人)',
  `payment_type` tinyint NULL DEFAULT 1 COMMENT '费用方式: 1-AA制, 2-发起人请客, 3-免费, 4-各付各的',
  `status` tinyint NULL DEFAULT 0 COMMENT '状态: 0-招募中, 1-已满员, 2-活动结束(群聊禁言), 3-已取消',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `end_time` datetime NULL DEFAULT NULL COMMENT '活动结束时间',
  `registration_end_time` datetime NULL DEFAULT NULL COMMENT '报名结束时间',
  PRIMARY KEY (`activity_id`) USING BTREE,
  INDEX `idx_initiator`(`initiator_id` ASC) USING BTREE,
  INDEX `idx_start_time`(`start_time` ASC) USING BTREE,
  INDEX `idx_geo`(`latitude` ASC, `longitude` ASC) USING BTREE COMMENT '简单的地理位置索引'
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '搭子活动表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of activities
-- ----------------------------
INSERT INTO `activities` VALUES (1, 2, '标题，去锦城湖', '1，先去，\n2后去', '[\"https://partner-3847.oss-cn-chengdu.aliyuncs.com/activity/2026/01/02/afa32003e4e2452b969354eae44c3721.jpg\"]', '锦城湖', '锦城湖', NULL, NULL, 'null', '2025-12-27 09:00:00', 4, 1, 0, '2025-12-27 23:08:23', '2026-01-02 19:00:25', NULL, NULL);
INSERT INTO `activities` VALUES (2, 2, '三岔湖', '计划美好', '[\"https://partner-3847.oss-cn-chengdu.aliyuncs.com/activity/2026/01/02/2221206daa3f447686ed36cd12afb4d6.jpg\"]', '112，山茶壶', '112，山茶壶', NULL, NULL, 'null', '2026-01-02 09:00:00', 4, 1, 0, '2026-01-02 18:55:16', '2026-01-02 18:55:16', NULL, NULL);
INSERT INTO `activities` VALUES (3, 1, '京都樱花季 | 寻找摄影搭子', '计划去京都看樱花，穿和服拍照。主要逛清水寺和伏见稻荷，晚上去居酒屋。我是E人，好相处，希望能找个审美在线的女生或者男生互拍。', '[\"https://partner-3847.oss-cn-chengdu.aliyuncs.com/activity/2026/01/02/9ccb1c86078a4ffab4b7538b552b51d0.jpg\"]', '东京', '东京', NULL, NULL, 'null', '2026-01-05 09:00:00', 3, 1, 0, '2026-01-02 19:23:08', '2026-01-02 19:23:08', NULL, NULL);
INSERT INTO `activities` VALUES (4, 2, '行程名字', '第七怒', '[\"https://partner-3847.oss-cn-chengdu.aliyuncs.com/activity/2026/02/21/61b83616e90c402fb2eb424e1f602164.jpg\"]', '到你们', '到你们', NULL, NULL, '[14, 1]', '2026-03-21 09:00:00', 3, 1, 0, '2026-02-21 12:40:11', '2026-02-21 12:40:11', '2026-03-24 23:59:59', '2026-03-21 23:59:59');

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
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '活动分类表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of categories
-- ----------------------------
INSERT INTO `categories` VALUES (1, '自驾', '/static/icons/road.png', 100, 1);
INSERT INTO `categories` VALUES (2, '露营', '/static/icons/tent.png', 98, 1);
INSERT INTO `categories` VALUES (3, '徒步', '/static/icons/hike.png', 96, 1);
INSERT INTO `categories` VALUES (4, '美食', '/static/icons/food.png', 94, 1);
INSERT INTO `categories` VALUES (5, '海岛', '/static/icons/sea.png', 92, 1);
INSERT INTO `categories` VALUES (6, '摄影', '/static/icons/cam.png', 90, 1);
INSERT INTO `categories` VALUES (7, '骑行', '/static/icons/bike.png', 88, 1);
INSERT INTO `categories` VALUES (8, '潜水', '/static/icons/dive.png', 86, 1);
INSERT INTO `categories` VALUES (9, '滑雪', '/static/icons/ski.png', 84, 1);
INSERT INTO `categories` VALUES (10, '看展', '/static/icons/art.png', 82, 1);
INSERT INTO `categories` VALUES (11, '爬山', '/static/icons/mount.png', 80, 1);
INSERT INTO `categories` VALUES (12, '探险', '/static/icons/adv.png', 78, 1);
INSERT INTO `categories` VALUES (13, '音乐节', '/static/icons/music.png', 76, 1);
INSERT INTO `categories` VALUES (14, 'CityWalk', '/static/icons/walk.png', 74, 1);

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
-- Records of chat_conversations
-- ----------------------------

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
-- Records of chat_messages
-- ----------------------------

-- ----------------------------
-- Table structure for moment_comments
-- ----------------------------
DROP TABLE IF EXISTS `moment_comments`;
CREATE TABLE `moment_comments`  (
  `comment_id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `moment_id` bigint UNSIGNED NOT NULL COMMENT '所属动态ID',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '评论用户ID',
  `parent_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '父评论ID，NULL为一级评论',
  `reply_to_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '被回复人用户ID',
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '评论内容',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态: 0-已删除, 1-正常',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
  PRIMARY KEY (`comment_id`) USING BTREE,
  INDEX `idx_moment_created`(`moment_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_parent`(`parent_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '动态评论表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of moment_comments
-- ----------------------------
INSERT INTO `moment_comments` VALUES (1, 5, 1, NULL, NULL, '这是什么地方', 1, '2026-02-22 17:58:47');
INSERT INTO `moment_comments` VALUES (2, 5, 1, 1, NULL, '这是那边', 1, '2026-02-22 17:59:02');
INSERT INTO `moment_comments` VALUES (3, 5, 2, 1, NULL, '说清楚一点', 1, '2026-02-23 17:49:47');
INSERT INTO `moment_comments` VALUES (4, 5, 2, NULL, NULL, '好的', 1, '2026-02-23 17:50:21');

-- ----------------------------
-- Table structure for moment_likes
-- ----------------------------
DROP TABLE IF EXISTS `moment_likes`;
CREATE TABLE `moment_likes`  (
  `like_id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '点赞ID',
  `moment_id` bigint UNSIGNED NOT NULL COMMENT '动态ID',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '点赞用户ID',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
  PRIMARY KEY (`like_id`) USING BTREE,
  UNIQUE INDEX `uniq_moment_user`(`moment_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_user_likes`(`user_id` ASC, `created_at` DESC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '动态点赞记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of moment_likes
-- ----------------------------
INSERT INTO `moment_likes` VALUES (2, 4, 1, '2026-02-22 15:52:01');
INSERT INTO `moment_likes` VALUES (5, 1, 1, '2026-02-23 14:56:26');
INSERT INTO `moment_likes` VALUES (6, 5, 1, '2026-02-23 15:13:37');

-- ----------------------------
-- Table structure for moments
-- ----------------------------
DROP TABLE IF EXISTS `moments`;
CREATE TABLE `moments`  (
  `moment_id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '动态ID',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '发布用户ID (外键 -> users)',
  `content` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '动态正文',
  `images` json NULL COMMENT '配图URL数组 - JSON数组，最多9张',
  `location_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '地点名称',
  `location_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '详细地址',
  `visibility` tinyint NULL DEFAULT 0 COMMENT '可见范围: 0-公开, 1-仅关注者, 2-仅自己',
  `like_count` int UNSIGNED NULL DEFAULT 0 COMMENT '点赞数',
  `comment_count` int UNSIGNED NULL DEFAULT 0 COMMENT '评论数',
  `view_count` int UNSIGNED NULL DEFAULT 0 COMMENT '浏览数',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态: 0-已删除, 1-正常, 2-审核屏蔽',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`moment_id`) USING BTREE,
  INDEX `idx_user_created`(`user_id` ASC, `created_at` DESC) USING BTREE,
  INDEX `idx_status_created`(`status` ASC, `created_at` DESC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户动态表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of moments
-- ----------------------------
INSERT INTO `moments` VALUES (1, 1, '111', NULL, '', '', 0, 1, 0, 1, 1, '2026-02-22 11:50:48', '2026-02-23 14:56:27');
INSERT INTO `moments` VALUES (2, 1, '熬三年多酶片', '[\"https://partner-3847.oss-cn-chengdu.aliyuncs.com/moment/2026/02/22/f3d486a4b2244aa98deac3880f4e803d.jpg\"]', '', '', 0, 0, 0, 0, 0, '2026-02-22 12:02:17', '2026-02-22 15:46:42');
INSERT INTO `moments` VALUES (3, 1, '我自己', '[\"https://partner-3847.oss-cn-chengdu.aliyuncs.com/moment/2026/02/22/3e0e0f051abf4de19110733af4302256.jpg\", \"https://partner-3847.oss-cn-chengdu.aliyuncs.com/moment/2026/02/22/48536cfbbafc4e7ca23b922827b733b8.jpg\"]', '', '', 2, 0, 0, 13, 1, '2026-02-22 15:06:34', '2026-02-22 17:48:44');
INSERT INTO `moments` VALUES (4, 2, '探险家的动态', NULL, '', '', 0, 1, 0, 4, 1, '2026-02-22 15:09:57', '2026-02-28 17:24:15');
INSERT INTO `moments` VALUES (5, 1, '多图片', '[\"https://partner-3847.oss-cn-chengdu.aliyuncs.com/moment/2026/02/22/dcea902411d64bbdb6d9100e8ffd7749.jpg\", \"https://partner-3847.oss-cn-chengdu.aliyuncs.com/moment/2026/02/22/f5b9f558bd764bb88629eea133edc82d.jpg\", \"https://partner-3847.oss-cn-chengdu.aliyuncs.com/moment/2026/02/22/3b7c4f3c54164fb7ac08559b4e072e7e.jpg\", \"https://partner-3847.oss-cn-chengdu.aliyuncs.com/moment/2026/02/22/13c744d0e02f47f3a5896205ae2a7a42.jpg\"]', '', '', 0, 1, 4, 3, 1, '2026-02-22 17:58:18', '2026-02-23 17:50:21');
INSERT INTO `moments` VALUES (6, 1, '多图片的', '[\"https://partner-3847.oss-cn-chengdu.aliyuncs.com/moment/2026/02/23/42b5db503fae44b2b135dab3927701c2.jpg\", \"https://partner-3847.oss-cn-chengdu.aliyuncs.com/moment/2026/02/23/e468789a3c8a44979edd90dbb05b279b.jpg\", \"https://partner-3847.oss-cn-chengdu.aliyuncs.com/moment/2026/02/23/d191cd767a7c4d7f91d79d1af2991b9d.jpg\", \"https://partner-3847.oss-cn-chengdu.aliyuncs.com/moment/2026/02/23/aa29fa05c7fe45d98526a822b8fa74f2.png\", \"https://partner-3847.oss-cn-chengdu.aliyuncs.com/moment/2026/02/23/9d897cbc666e41b281808e150310b486.jpg\", \"https://partner-3847.oss-cn-chengdu.aliyuncs.com/moment/2026/02/23/fb1f79242b5e419faba9fdd895a42a0c.jpg\", \"https://partner-3847.oss-cn-chengdu.aliyuncs.com/moment/2026/02/23/5b24afeec732407fbbcff4d1ffcb09aa.jpg\", \"https://partner-3847.oss-cn-chengdu.aliyuncs.com/moment/2026/02/23/2e02e2ed274840deadf9058e558d4123.jpg\", \"https://partner-3847.oss-cn-chengdu.aliyuncs.com/moment/2026/02/23/cbc668ea37934cf0ac76a54b0d90ceed.jpg\"]', '', '', 0, 0, 0, 1, 1, '2026-02-23 15:14:33', '2026-02-28 17:24:10');

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
-- Records of notifications
-- ----------------------------

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
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '活动参与记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of participants
-- ----------------------------
INSERT INTO `participants` VALUES (1, 2, 1, 1, '我想参加这个活动', '2026-01-25 17:46:35', '2026-02-15 18:05:27');
INSERT INTO `participants` VALUES (2, 3, 2, 0, '一起吗，走，', '2026-01-25 18:33:36', '2026-02-28 16:21:22');
INSERT INTO `participants` VALUES (3, 3, 3, 0, '我也想去', '2026-01-25 19:55:44', '2026-01-25 19:55:44');

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
-- Records of reviews
-- ----------------------------

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
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户关注关系表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_follows
-- ----------------------------
INSERT INTO `user_follows` VALUES (1, 2, 1, '2026-02-15 17:31:40');
INSERT INTO `user_follows` VALUES (3, 3, 2, '2026-02-15 20:23:38');
INSERT INTO `user_follows` VALUES (4, 3, 1, '2026-02-15 20:27:17');

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
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES (1, 'zhangsan', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', '徐艺洋', 'https://partner-3847.oss-cn-chengdu.aliyuncs.com/avatar/2026/01/02/1a108599f2894b6fbd9ac63895441f16.jpg', 1, '2026-01-02', '四川', '我叫徐艺洋', '[\"111\"]', 100, 1, '2025-12-20 18:44:41', '2026-01-02 19:24:40');
INSERT INTO `users` VALUES (2, '123456', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', '探险家', 'https://partner-3847.oss-cn-chengdu.aliyuncs.com/avatar/2026/02/10/0534d763b17e4d6a82aa364c809d069a.jpg', 1, '2021-12-28', '成都', '大家好，我是李华。迭戈', '[\"勇敢\", \"真诚\"]', 100, 1, '2025-12-20 18:48:20', '2026-02-10 17:53:10');
INSERT INTO `users` VALUES (3, 'lmy111', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'lmy111', 'https://partner-3847.oss-cn-chengdu.aliyuncs.com/avatar/2026/02/15/3a98a2e6fee5438886a7dd9a0fe11e84.jpg', 1, NULL, '都匀', NULL, NULL, 100, 1, '2026-01-25 19:55:13', '2026-02-15 20:23:22');

SET FOREIGN_KEY_CHECKS = 1;
