package com.limengyuan.partner.user.controller;

import com.limengyuan.partner.common.dto.request.UpdateCreditRequest;
import com.limengyuan.partner.common.result.Result;
import com.limengyuan.partner.user.service.CreditScoreService;
import org.springframework.web.bind.annotation.*;

/**
 * 用户信誉分控制器 - 内部接口
 *
 * 仅供微服务间调用（如 post 模块通过 OpenFeign 调用）
 * 路径以 /internal 开头，网关不配置此路由，外部无法访问
 */
@RestController
@RequestMapping("/internal/user/credit")
public class UserCreditController {

    private final CreditScoreService creditScoreService;

    public UserCreditController(CreditScoreService creditScoreService) {
        this.creditScoreService = creditScoreService;
    }

    /**
     * 重新计算指定用户的信誉分
     * PUT /internal/user/credit/recalculate
     *
     * 由 post 模块在提交评价后通过 OpenFeign 调用
     */
    @PutMapping("/recalculate")
    public Result<Integer> recalculateCreditScore(@RequestBody UpdateCreditRequest request) {
        if (request.getUserId() == null) {
            return Result.error("用户ID不能为空");
        }
        return creditScoreService.recalculate(request.getUserId(), request.getReason());
    }
}
