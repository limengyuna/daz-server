package com.limengyuan.partner.user.controller;

import com.limengyuan.partner.common.dto.vo.ChatConversationVO;
import com.limengyuan.partner.common.dto.vo.ChatMessageVO;
import com.limengyuan.partner.common.dto.vo.GroupChatVO;
import com.limengyuan.partner.common.dto.request.SendMessageRequest;
import com.limengyuan.partner.common.result.Result;
import com.limengyuan.partner.common.util.JwtUtils;
import com.limengyuan.partner.user.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 聊天控制器 - 私聊和群聊相关接口
 * <p>
 * 接口列表:
 * GET /api/chat/conversations 获取私聊会话列表
 * POST /api/chat/conversations?targetUserId=xxx 创建/获取与指定用户的会话
 * GET /api/chat/conversations/{id}/messages 获取私聊消息列表（分页）
 * POST /api/chat/messages/private 发送私聊消息
 * GET /api/chat/group/my 获取我参与的群聊列表
 * GET /api/chat/group/{activityId}/messages 获取群聊消息列表（分页）
 * POST /api/chat/messages/group 发送群聊消息
 */
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    // ============================
    // 私聊会话
    // ============================

    /**
     * 获取当前用户的私聊会话列表
     * GET /api/chat/conversations
     * <p>
     * 请求头需携带: Authorization: Bearer {token}
     */
    @GetMapping("/conversations")
    public Result<List<ChatConversationVO>> getConversations(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = parseUserIdFromToken(authHeader);
        if (userId == null) {
            return Result.error("未登录或 Token 无效");
        }

        return chatService.getConversations(userId);
    }

    /**
     * 创建/获取与指定用户的私聊会话
     * POST /api/chat/conversations?targetUserId=xxx
     * <p>
     * 请求头需携带: Authorization: Bearer {token}
     *
     * @param targetUserId 目标用户ID
     */
    @PostMapping("/conversations")
    public Result<ChatConversationVO> getOrCreateConversation(
            @RequestParam("targetUserId") Long targetUserId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = parseUserIdFromToken(authHeader);
        if (userId == null) {
            return Result.error("未登录或 Token 无效");
        }

        return chatService.getOrCreateConversation(userId, targetUserId);
    }

    /**
     * 获取某个私聊会话的消息列表（分页）
     * GET /api/chat/conversations/{id}/messages?page=0&size=20
     * <p>
     * 请求头需携带: Authorization: Bearer {token}
     *
     * @param conversationId 会话ID
     * @param page           页码（从0开始，默认0）
     * @param size           每页数量（默认20）
     */
    @GetMapping("/conversations/{id}/messages")
    public Result<List<ChatMessageVO>> getPrivateMessages(
            @PathVariable("id") Long conversationId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = parseUserIdFromToken(authHeader);
        if (userId == null) {
            return Result.error("未登录或 Token 无效");
        }

        return chatService.getPrivateMessages(conversationId, userId, page, size);
    }

    // ============================
    // 私聊消息
    // ============================

    /**
     * 发送私聊消息
     * POST /api/chat/messages/private
     * <p>
     * 请求头需携带: Authorization: Bearer {token}
     * <p>
     * 请求体示例:
     * {
     * "receiverId": 2,
     * "content": "你好，一起去吃火锅吗？",
     * "msgType": 1
     * }
     */
    @PostMapping("/messages/private")
    public Result<ChatMessageVO> sendPrivateMessage(
            @Valid @RequestBody SendMessageRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = parseUserIdFromToken(authHeader);
        if (userId == null) {
            return Result.error("未登录或 Token 无效");
        }

        return chatService.sendPrivateMessage(userId, request);
    }

    // ============================
    // 群聊列表
    // ============================

    /**
     * 获取当前用户参与的所有群聊列表
     * GET /api/chat/group/my
     * <p>
     * 请求头需携带: Authorization: Bearer {token}
     * 返回用户发起的活动 + 用户参与并已通过审核的活动的群聊列表
     */
    @GetMapping("/group/my")
    public Result<List<GroupChatVO>> getMyGroupChats(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = parseUserIdFromToken(authHeader);
        if (userId == null) {
            return Result.error("未登录或 Token 无效");
        }

        return chatService.getMyGroupChats(userId);
    }

    // ============================
    // 群聊消息
    // ============================

    /**
     * 获取某个活动群聊的消息列表（分页）
     * GET /api/chat/group/{activityId}/messages?page=0&size=20
     * <p>
     * 请求头需携带: Authorization: Bearer {token}
     *
     * @param activityId 活动ID
     * @param page       页码（从0开始，默认0）
     * @param size       每页数量（默认20）
     */
    @GetMapping("/group/{activityId}/messages")
    public Result<List<ChatMessageVO>> getGroupMessages(
            @PathVariable("activityId") Long activityId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = parseUserIdFromToken(authHeader);
        if (userId == null) {
            return Result.error("未登录或 Token 无效");
        }

        return chatService.getGroupMessages(activityId, page, size);
    }

    /**
     * 发送群聊消息
     * POST /api/chat/messages/group
     * <p>
     * 请求头需携带: Authorization: Bearer {token}
     * <p>
     * 请求体示例:
     * {
     * "activityId": 1,
     * "content": "大家好，我们在哪集合？",
     * "msgType": 1
     * }
     */
    @PostMapping("/messages/group")
    public Result<ChatMessageVO> sendGroupMessage(
            @Valid @RequestBody SendMessageRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = parseUserIdFromToken(authHeader);
        if (userId == null) {
            return Result.error("未登录或 Token 无效");
        }

        return chatService.sendGroupMessage(userId, request);
    }

    // ============================
    // 工具方法
    // ============================

    /**
     * 从 Authorization 请求头中解析用户ID
     */
    private Long parseUserIdFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return JwtUtils.getUserIdFromToken(authHeader);
    }
}
