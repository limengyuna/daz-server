package com.limengyuan.partner.user.service;

import com.limengyuan.partner.common.dto.ChatConversationVO;
import com.limengyuan.partner.common.dto.ChatMessageVO;
import com.limengyuan.partner.common.dto.GroupChatVO;
import com.limengyuan.partner.common.dto.SendMessageRequest;
import com.limengyuan.partner.common.entity.ChatConversation;
import com.limengyuan.partner.common.entity.ChatMessage;
import com.limengyuan.partner.common.result.Result;
import com.limengyuan.partner.user.mapper.ChatMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 聊天服务层 - 封装私聊和群聊的业务逻辑
 */
@Service
@Transactional
public class ChatService {

    private final ChatMapper chatMapper;

    public ChatService(ChatMapper chatMapper) {
        this.chatMapper = chatMapper;
    }

    // ============================
    // 私聊会话
    // ============================

    /**
     * 获取当前用户的所有私聊会话列表
     *
     * @param userId 当前用户ID
     * @return 会话列表
     */
    public Result<List<ChatConversationVO>> getConversations(Long userId) {
        List<ChatConversationVO> conversations = chatMapper.findConversationsByUserId(userId);
        return Result.success(conversations);
    }

    /**
     * 获取或创建与目标用户的私聊会话
     * <p>
     * 保证 user_a_id 始终为较小的ID，user_b_id 为较大的ID
     *
     * @param currentUserId 当前用户ID
     * @param targetUserId  目标用户ID
     * @return 会话信息
     */
    public Result<ChatConversationVO> getOrCreateConversation(Long currentUserId, Long targetUserId) {
        if (currentUserId.equals(targetUserId)) {
            return Result.error("不能和自己聊天");
        }

        // 确保 userAId < userBId
        Long userAId = Math.min(currentUserId, targetUserId);
        Long userBId = Math.max(currentUserId, targetUserId);

        // 查找已有会话
        ChatConversation existing = chatMapper.findConversationByUsers(userAId, userBId).orElse(null);

        Long conversationId;
        if (existing != null) {
            conversationId = existing.getConversationId();
        } else {
            // 创建新会话
            conversationId = chatMapper.insertConversation(userAId, userBId);
            if (conversationId == null) {
                return Result.error("创建会话失败");
            }
        }

        // 返回包含对方用户信息的 VO
        ChatConversationVO vo = ChatConversationVO.builder()
                .conversationId(conversationId)
                .otherUserId(targetUserId)
                .lastMessageContent(existing != null ? existing.getLastMessageContent() : null)
                .lastMessageTime(existing != null ? existing.getLastMessageTime() : null)
                .build();

        return Result.success(vo);
    }

    // ============================
    // 私聊消息
    // ============================

    /**
     * 获取私聊消息列表（分页）
     * <p>
     * 会校验当前用户是否是该会话的参与者
     *
     * @param conversationId 会话ID
     * @param userId         当前用户ID（用于权限校验）
     * @param page           页码（从0开始）
     * @param size           每页数量
     * @return 消息列表
     */
    public Result<List<ChatMessageVO>> getPrivateMessages(Long conversationId, Long userId, int page, int size) {
        // 查询会话，校验权限
        ChatConversation conversation = chatMapper.findConversationById(conversationId).orElse(null);
        if (conversation == null) {
            return Result.error("会话不存在");
        }

        // 校验当前用户是否是会话参与者
        if (!conversation.getUserAId().equals(userId) && !conversation.getUserBId().equals(userId)) {
            return Result.error("无权查看该会话");
        }

        List<ChatMessageVO> messages = chatMapper.findPrivateMessages(
                conversation.getUserAId(), conversation.getUserBId(), page, size);
        return Result.success(messages);
    }

    /**
     * 发送私聊消息
     * <p>
     * 自动查找或创建会话，插入消息并更新会话最后消息
     *
     * @param senderId 发送者ID
     * @param request  发送消息请求
     * @return 发送结果
     */
    public Result<ChatMessageVO> sendPrivateMessage(Long senderId, SendMessageRequest request) {
        if (request.getReceiverId() == null) {
            return Result.error("接收者ID不能为空");
        }
        if (senderId.equals(request.getReceiverId())) {
            return Result.error("不能给自己发消息");
        }

        // 确保 userAId < userBId
        Long userAId = Math.min(senderId, request.getReceiverId());
        Long userBId = Math.max(senderId, request.getReceiverId());

        // 查找或创建会话
        ChatConversation conversation = chatMapper.findConversationByUsers(userAId, userBId).orElse(null);
        Long conversationId;
        if (conversation != null) {
            conversationId = conversation.getConversationId();
        } else {
            conversationId = chatMapper.insertConversation(userAId, userBId);
            if (conversationId == null) {
                return Result.error("创建会话失败");
            }
        }

        // 构建消息实体
        ChatMessage message = ChatMessage.builder()
                .senderId(senderId)
                .receiverId(request.getReceiverId())
                .content(request.getContent())
                .msgType(request.getMsgType() != null ? request.getMsgType() : 1)
                .build();

        // 插入消息
        Long messageId = chatMapper.insertMessage(message);
        if (messageId == null) {
            return Result.error("发送消息失败");
        }

        // 更新会话最后消息
        chatMapper.updateConversationLastMessage(conversationId, request.getContent());

        // 构建返回 VO
        ChatMessageVO vo = ChatMessageVO.builder()
                .messageId(messageId)
                .senderId(senderId)
                .content(request.getContent())
                .msgType(message.getMsgType())
                .build();

        return Result.success("发送成功", vo);
    }

    // ============================
    // 群聊列表
    // ============================

    /**
     * 获取当前用户参与的所有群聊列表
     *
     * @param userId 当前用户ID
     * @return 群聊列表
     */
    public Result<List<GroupChatVO>> getMyGroupChats(Long userId) {
        List<GroupChatVO> groupChats = chatMapper.findGroupChatsByUserId(userId);
        return Result.success(groupChats);
    }

    // ============================
    // 群聊消息
    // ============================

    /**
     * 获取群聊消息列表（分页）
     *
     * @param activityId 活动ID
     * @param page       页码（从0开始）
     * @param size       每页数量
     * @return 消息列表
     */
    public Result<List<ChatMessageVO>> getGroupMessages(Long activityId, int page, int size) {
        List<ChatMessageVO> messages = chatMapper.findGroupMessages(activityId, page, size);
        return Result.success(messages);
    }

    /**
     * 发送群聊消息
     *
     * @param senderId 发送者ID
     * @param request  发送消息请求
     * @return 发送结果
     */
    public Result<ChatMessageVO> sendGroupMessage(Long senderId, SendMessageRequest request) {
        if (request.getActivityId() == null) {
            return Result.error("活动ID不能为空");
        }

        // 构建消息实体
        ChatMessage message = ChatMessage.builder()
                .senderId(senderId)
                .activityId(request.getActivityId())
                .content(request.getContent())
                .msgType(request.getMsgType() != null ? request.getMsgType() : 1)
                .build();

        // 插入消息
        Long messageId = chatMapper.insertMessage(message);
        if (messageId == null) {
            return Result.error("发送消息失败");
        }

        // 构建返回 VO
        ChatMessageVO vo = ChatMessageVO.builder()
                .messageId(messageId)
                .senderId(senderId)
                .content(request.getContent())
                .msgType(message.getMsgType())
                .build();

        return Result.success("发送成功", vo);
    }
}
