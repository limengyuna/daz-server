package com.limengyuan.partner.user.service;

import com.limengyuan.partner.common.dto.vo.ChatConversationVO;
import com.limengyuan.partner.common.dto.vo.ChatMessageVO;
import com.limengyuan.partner.common.dto.vo.GroupChatVO;
import com.limengyuan.partner.common.dto.request.SendMessageRequest;
import com.limengyuan.partner.common.entity.ChatConversation;
import com.limengyuan.partner.common.entity.ChatMessage;
import com.limengyuan.partner.common.result.Result;
import com.limengyuan.partner.user.mapper.ChatConversationMapper;
import com.limengyuan.partner.user.mapper.ChatMessageMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 聊天服务层 - 封装私聊和群聊的业务逻辑
 */
@Service
@Transactional
public class ChatService {

    private final ChatMessageMapper chatMessageMapper;
    private final ChatConversationMapper chatConversationMapper;

    public ChatService(ChatMessageMapper chatMessageMapper, ChatConversationMapper chatConversationMapper) {
        this.chatMessageMapper = chatMessageMapper;
        this.chatConversationMapper = chatConversationMapper;
    }

    // ============================
    // 私聊会话
    // ============================

    /**
     * 获取当前用户的所有私聊会话列表
     */
    public Result<List<ChatConversationVO>> getConversations(Long userId) {
        List<ChatConversationVO> conversations = chatConversationMapper.findConversationsByUserId(userId);
        return Result.success(conversations);
    }

    /**
     * 获取或创建与目标用户的私聊会话
     * <p>
     * 保证 user_a_id 始终为较小的ID，user_b_id 为较大的ID
     */
    public Result<ChatConversationVO> getOrCreateConversation(Long currentUserId, Long targetUserId) {
        if (currentUserId.equals(targetUserId)) {
            return Result.error("不能和自己聊天");
        }

        // 确保 userAId < userBId
        Long userAId = Math.min(currentUserId, targetUserId);
        Long userBId = Math.max(currentUserId, targetUserId);

        // 查找已有会话
        ChatConversation existing = chatConversationMapper.findConversationByUsers(userAId, userBId);

        Long conversationId;
        if (existing != null) {
            conversationId = existing.getConversationId();
        } else {
            // 创建新会话（MP 的 insert 自动回填 ID）
            ChatConversation newConv = ChatConversation.builder()
                    .userAId(userAId)
                    .userBId(userBId)
                    .build();
            chatConversationMapper.insert(newConv);
            conversationId = newConv.getConversationId();
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
     */
    public Result<List<ChatMessageVO>> getPrivateMessages(Long conversationId, Long userId, int page, int size) {
        // 查询会话，校验权限
        ChatConversation conversation = chatConversationMapper.selectById(conversationId);
        if (conversation == null) {
            return Result.error("会话不存在");
        }

        // 校验当前用户是否是会话参与者
        if (!conversation.getUserAId().equals(userId) && !conversation.getUserBId().equals(userId)) {
            return Result.error("无权查看该会话");
        }

        int offset = page * size;
        List<ChatMessageVO> messages = chatMessageMapper.findPrivateMessages(
                conversation.getUserAId(), conversation.getUserBId(), size, offset);
        return Result.success(messages);
    }

    /**
     * 发送私聊消息
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
        ChatConversation conversation = chatConversationMapper.findConversationByUsers(userAId, userBId);
        Long conversationId;
        if (conversation != null) {
            conversationId = conversation.getConversationId();
        } else {
            ChatConversation newConv = ChatConversation.builder()
                    .userAId(userAId)
                    .userBId(userBId)
                    .build();
            chatConversationMapper.insert(newConv);
            conversationId = newConv.getConversationId();
            if (conversationId == null) {
                return Result.error("创建会话失败");
            }
        }

        // 构建消息实体并插入（MP 的 insert 自动回填 messageId）
        ChatMessage message = ChatMessage.builder()
                .senderId(senderId)
                .receiverId(request.getReceiverId())
                .content(request.getContent())
                .msgType(request.getMsgType() != null ? request.getMsgType() : 1)
                .build();

        chatMessageMapper.insert(message);
        if (message.getMessageId() == null) {
            return Result.error("发送消息失败");
        }

        // 更新会话最后消息
        // 截取消息预览（最多255字符）
        String preview = request.getContent();
        if (preview != null && preview.length() > 255) {
            preview = preview.substring(0, 255);
        }
        chatConversationMapper.updateConversationLastMessage(conversationId, preview);

        // 构建返回 VO
        ChatMessageVO vo = ChatMessageVO.builder()
                .messageId(message.getMessageId())
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
     */
    public Result<List<GroupChatVO>> getMyGroupChats(Long userId) {
        List<GroupChatVO> groupChats = chatMessageMapper.findGroupChatsByUserId(userId);
        return Result.success(groupChats);
    }

    // ============================
    // 群聊消息
    // ============================

    /**
     * 获取群聊消息列表（分页）
     */
    public Result<List<ChatMessageVO>> getGroupMessages(Long activityId, int page, int size) {
        int offset = page * size;
        List<ChatMessageVO> messages = chatMessageMapper.findGroupMessages(activityId, size, offset);
        return Result.success(messages);
    }

    /**
     * 发送群聊消息
     */
    public Result<ChatMessageVO> sendGroupMessage(Long senderId, SendMessageRequest request) {
        if (request.getActivityId() == null) {
            return Result.error("活动ID不能为空");
        }

        // 构建消息实体并插入（MP 的 insert 自动回填 messageId）
        ChatMessage message = ChatMessage.builder()
                .senderId(senderId)
                .activityId(request.getActivityId())
                .content(request.getContent())
                .msgType(request.getMsgType() != null ? request.getMsgType() : 1)
                .build();

        chatMessageMapper.insert(message);
        if (message.getMessageId() == null) {
            return Result.error("发送消息失败");
        }

        // 构建返回 VO
        ChatMessageVO vo = ChatMessageVO.builder()
                .messageId(message.getMessageId())
                .senderId(senderId)
                .content(request.getContent())
                .msgType(message.getMsgType())
                .build();

        return Result.success("发送成功", vo);
    }
}
