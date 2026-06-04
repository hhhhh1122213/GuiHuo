package com.ghostfire.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ghostfire.dto.BoastDto;
import com.ghostfire.entity.Boast;
import com.ghostfire.entity.BoastBet;
import com.ghostfire.entity.UserStat;
import com.ghostfire.mapper.BoastMapper;
import com.ghostfire.service.BoastBetService;
import com.ghostfire.service.BoastService;
import com.ghostfire.service.MedalService;
import com.ghostfire.service.UserStatService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.ghostfire.common.Constant.*;

@Service
@RequiredArgsConstructor
public class BoastServiceImpl extends ServiceImpl<BoastMapper, Boast> implements BoastService {
    private final UserStatService userStatService;
    private final MedalService medalService;
    private final BoastBetService boastBetService;

    @Override
    @Transactional
    public Boast create(BoastDto dto, Long userId) {
        // 校验参数
        if (dto.getOptionOne().equals(dto.getOptionTwo())) {
            throw new RuntimeException("两个选项不能相同");
        }
        if (dto.getCorrectOption() != BOAST_OPTION_ONE
                && dto.getCorrectOption() != BOAST_OPTION_TWO) {
            throw new RuntimeException("请选择正确的答案选项");
        }
        if (dto.getDeadline().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("截止时间必须晚于当前时间");
        }

        // 校验金币
        UserStat user = userStatService.getById(userId);
        if (user == null || user.getCoin() < dto.getStakeAmount()) {
            throw new RuntimeException("金币不够啊！靓仔");
        }

        // 构建并保存吹牛记录
        Boast boasts = new Boast();
        boasts.setUserId(userId);
        boasts.setTitle(dto.getTitle());
        boasts.setImage(dto.getImage());
        boasts.setOptionOne(dto.getOptionOne());
        boasts.setOptionTwo(dto.getOptionTwo());
        boasts.setCorrectOption(dto.getCorrectOption());
        boasts.setStakeAmount(dto.getStakeAmount());
        boasts.setDeadline(dto.getDeadline());
        boasts.setResult(BOAST_ONGOING);
        save(boasts);

        // 写钱包流水
        userStatService.addCoin(userId, -dto.getStakeAmount(), WALLET_BOAST_BET, boasts.getId());

        return boasts;
    }

    @Override
    public Page<Boast> list(int page, int size, Integer status) {
        LambdaQueryWrapper<Boast> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            if (status == BOAST_ONGOING) {
                // 进行中：result=0 且未过截止时间
                wrapper.eq(Boast::getResult, BOAST_ONGOING)
                        .gt(Boast::getDeadline, LocalDateTime.now());
            } else {
                // 已结束：result!=0 或已过截止时间
                wrapper.and(w -> w.ne(Boast::getResult, BOAST_ONGOING)
                        .or()
                        .le(Boast::getDeadline, LocalDateTime.now()));
            }
        }
        wrapper.orderByDesc(Boast::getCreateTime);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public Boast detail(Long id) {
        Boast boast = getById(id);
        if (boast == null) {
            throw new RuntimeException("挑战不存在");
        }
        return boast;
    }

    @Override
    @Transactional
    public BoastBet bet(Long boastId, Integer optionType, Long userId) {
        // 校验挑战存在
        Boast boast = getById(boastId);
        if (boast == null) {
            throw new RuntimeException("挑战不存在");
        }

        // 校验进行中
        if (boast.getResult() != BOAST_ONGOING) {
            throw new RuntimeException("挑战已结束");
        }

        // 校验截止时间
        if (boast.getDeadline().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("挑战已过截止时间");
        }

        // 校验不能自己跟自己赌
        if (userId.equals(boast.getUserId())) {
            throw new RuntimeException("不能参与自己发起的挑战");
        }

        // 校验选项
        if (optionType != BOAST_OPTION_ONE && optionType != BOAST_OPTION_TWO) {
            throw new RuntimeException("请选择有效选项");
        }

        // 校验重复下注
        long count = boastBetService.count(new LambdaQueryWrapper<BoastBet>()
                .eq(BoastBet::getBoastId, boastId)
                .eq(BoastBet::getUserId, userId));
        if (count > 0) {
            throw new RuntimeException("已参与过该挑战");
        }

        // 校验金币
        UserStat user = userStatService.getById(userId);
        if (user == null || user.getCoin() < boast.getStakeAmount()) {
            throw new RuntimeException("金币不够啊！靓仔");
        }

        // 插入下注记录
        BoastBet bet = new BoastBet();
        bet.setBoastId(boastId);
        bet.setUserId(userId);
        bet.setOptionType(optionType);
        bet.setAmount(boast.getStakeAmount());
        bet.setResult(BOAST_BET_UNSETTLED);
        try {
            boastBetService.save(bet);
        } catch (DuplicateKeyException e) {
            throw new RuntimeException("已参与过该挑战");
        }

        // 写钱包流水
        userStatService.addCoin(userId, -boast.getStakeAmount(), WALLET_BOAST_BET, boastId);

        // 更新 user_stat 吹牛次数
        userStatService.getBaseMapper().update(null,
                new LambdaUpdateWrapper<UserStat>()
                        .eq(UserStat::getUserId, userId)
                        .setSql("boast_count = boast_count + 1"));

        return bet;
    }

    @Override
    @Transactional
    public Boast settle(Long id, Integer result, Long userId) {
        // 校验参数
        if (result != BOAST_CREATOR_WIN && result != BOAST_CALLER_WIN) {
            throw new RuntimeException("结算结果无效");
        }

        // 校验挑战存在（SELECT FOR UPDATE 防并发结算）
        Boast boast = getBaseMapper().selectOne(
                new LambdaQueryWrapper<Boast>()
                        .eq(Boast::getId, id)
                        .last("FOR UPDATE"));
        if (boast == null) {
            throw new RuntimeException("挑战不存在");
        }

        // 校验未结算过
        if (boast.getResult() != BOAST_ONGOING) {
            throw new RuntimeException("挑战已结算");
        }

        // 校验权限：发布者或管理员
        boolean isCreator = userId.equals(boast.getUserId());
        boolean isAdmin;
        try {
            isAdmin = cn.dev33.satoken.stp.StpUtil.hasRole("ADMIN");
        } catch (Exception e) {
            isAdmin = false;
        }
        if (!isCreator && !isAdmin) {
            throw new RuntimeException("无权操作");
        }

        // 正确选项
        int correctOption = boast.getCorrectOption();

        // 查询所有下注记录
        List<BoastBet> allBets = boastBetService.list(
                new LambdaQueryWrapper<BoastBet>().eq(BoastBet::getBoastId, id));

        for (BoastBet bet : allBets) {
            Long betUserId = bet.getUserId();
            Long betAmount = bet.getAmount();
            boolean isWin = bet.getOptionType() == correctOption;

            if (isWin) {
                // 赢家：获得赌注的90%
                long winAmount = (long) (betAmount * 0.9);

                // 加金币 + 写流水
                userStatService.addCoin(betUserId, winAmount, WALLET_BOAST_WIN, id);

                // 更新赢家统计
                userStatService.getBaseMapper().update(null,
                        new LambdaUpdateWrapper<UserStat>()
                                .eq(UserStat::getUserId, betUserId)
                                .setSql("boast_win_count = boast_win_count + 1")
                                .setSql("boast_win_total = boast_win_total + {0}", winAmount));
                // boast_best_win = MAX(boast_best_win, winAmount)
                UserStat winnerStat = userStatService.getById(betUserId);
                if (winnerStat != null && (winnerStat.getBoastBestWin() == null || winAmount > winnerStat.getBoastBestWin())) {
                    userStatService.getBaseMapper().update(null,
                            new LambdaUpdateWrapper<UserStat>()
                                    .eq(UserStat::getUserId, betUserId)
                                    .setSql("boast_best_win = {0}", winAmount));
                }

                // 更新下注记录
                bet.setResult(BOAST_BET_WIN);
            } else {
                // 输家：失去赌注，发布者获得

                // 发布者加金币 + 写流水
                userStatService.addCoin(boast.getUserId(), betAmount, WALLET_BOAST_WIN, id);

                // 更新输家统计
                // boast_worst_loss = MAX(boast_worst_loss, betAmount)
                UserStat loserStat = userStatService.getById(betUserId);
                if (loserStat != null && (loserStat.getBoastWorstLoss() == null || betAmount > loserStat.getBoastWorstLoss())) {
                    userStatService.getBaseMapper().update(null,
                            new LambdaUpdateWrapper<UserStat>()
                                    .eq(UserStat::getUserId, betUserId)
                                    .setSql("boast_worst_loss = {0}", betAmount));
                }

                // 更新下注记录
                bet.setResult(BOAST_BET_LOSE);
            }
            medalService.checkAutoAward(betUserId);
        }
        medalService.checkAutoAward(boast.getUserId());

        // 处理创建者赌注：赢了退还本金+更新统计，输了记录最惨失利
        if (result == BOAST_CREATOR_WIN) {
            userStatService.addCoin(boast.getUserId(), boast.getStakeAmount(), WALLET_BOAST_WIN, id);
            userStatService.getBaseMapper().update(null,
                    new LambdaUpdateWrapper<UserStat>()
                            .eq(UserStat::getUserId, boast.getUserId())
                            .setSql("boast_win_count = boast_win_count + 1")
                            .setSql("boast_win_total = boast_win_total + {0}", boast.getStakeAmount()));
        } else {
            UserStat creatorStat = userStatService.getById(boast.getUserId());
            if (creatorStat != null && (creatorStat.getBoastWorstLoss() == null || boast.getStakeAmount() > creatorStat.getBoastWorstLoss())) {
                userStatService.getBaseMapper().update(null,
                        new LambdaUpdateWrapper<UserStat>()
                                .eq(UserStat::getUserId, boast.getUserId())
                                .setSql("boast_worst_loss = {0}", boast.getStakeAmount()));
            }
        }

        // 更新吹牛结果
        LambdaUpdateWrapper<Boast> updateBoast = new LambdaUpdateWrapper<Boast>()
                .eq(Boast::getId, id)
                .set(Boast::getResult, result);
        update(updateBoast);

        // 更新所有下注记录
        boastBetService.updateBatchById(allBets);

        return getById(id);
    }
}
