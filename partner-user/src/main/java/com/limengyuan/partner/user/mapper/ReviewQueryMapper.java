package com.limengyuan.partner.user.mapper;

import com.limengyuan.partner.common.entity.Review;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 评价查询 Mapper（只读）
 * 仅用于信誉分计算，只提供查询方法，不修改 reviews 表
 */
@Mapper
public interface ReviewQueryMapper {

    /**
     * 查询某用户收到的所有评价（用于信誉分计算）
     * 返回评分和创建时间，用于加权衰减算法
     */
    @Select("SELECT review_id, score, created_at FROM reviews WHERE reviewee_id = #{revieweeId}")
    List<Review> findAllByRevieweeId(@Param("revieweeId") Long revieweeId);
}
