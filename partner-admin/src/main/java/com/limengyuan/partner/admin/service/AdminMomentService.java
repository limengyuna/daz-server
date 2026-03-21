package com.limengyuan.partner.admin.service;

import com.limengyuan.partner.admin.mapper.AdminMomentMapper;
import com.limengyuan.partner.common.dto.PageResult;
import com.limengyuan.partner.common.dto.vo.MomentVO;
import com.limengyuan.partner.common.result.Result;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 管理员端 - 动态管理服务
 */
@Service
public class AdminMomentService {

    private final AdminMomentMapper adminMomentMapper;

    public AdminMomentService(AdminMomentMapper adminMomentMapper) {
        this.adminMomentMapper = adminMomentMapper;
    }

    /**
     * 分页查询动态列表
     *
     * @param keyword 搜索关键词（内容）
     * @param status  动态状态筛选（可为 null）
     * @param userId  用户ID筛选（可为 null）
     * @param page    页码，从0开始
     * @param size    每页数量
     */
    public Result<PageResult<MomentVO>> getMomentList(String keyword, Integer status, Long userId, int page, int size) {
        int offset = page * size;
        List<MomentVO> moments = adminMomentMapper.findMomentsPage(keyword, status, userId, size, offset);
        long total = adminMomentMapper.countMoments(keyword, status, userId);
        return Result.success(PageResult.of(moments, total, page, size));
    }

    /**
     * 根据动态ID查询详情
     *
     * @param momentId 动态ID
     */
    public Result<MomentVO> getMomentDetail(Long momentId) {
        MomentVO moment = adminMomentMapper.findMomentById(momentId);
        if (moment == null) {
            return Result.error("动态不存在");
        }
        return Result.success(moment);
    }

    /**
     * 屏蔽动态
     */
    public Result<Void> blockMoment(Long momentId) {
        int rows = adminMomentMapper.updateMomentStatus(momentId, 2);
        if (rows == 0) {
            return Result.error("动态不存在");
        }
        return Result.success("已屏蔽该动态", null);
    }

    /**
     * 恢复动态
     */
    public Result<Void> restoreMoment(Long momentId) {
        int rows = adminMomentMapper.updateMomentStatus(momentId, 1);
        if (rows == 0) {
            return Result.error("动态不存在");
        }
        return Result.success("已恢复该动态", null);
    }
}
