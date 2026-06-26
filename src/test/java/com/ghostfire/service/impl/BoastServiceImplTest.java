package com.ghostfire.service.impl;

import com.ghostfire.dto.BoastDto;
import com.ghostfire.entity.Boast;
import com.ghostfire.entity.BoastBet;
import com.ghostfire.entity.UserStat;
import com.ghostfire.mapper.BoastMapper;
import com.ghostfire.mapper.UserStatMapper;
import com.ghostfire.service.BoastBetService;
import com.ghostfire.service.MedalService;
import com.ghostfire.service.UserStatService;
import com.ghostfire.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static com.ghostfire.common.Constant.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BoastServiceImplTest {

    @Mock BoastMapper boastMapper;
    @Mock UserStatService userStatService;
    @Mock UserStatMapper userStatMapper;
    @Mock MedalService medalService;
    @Mock BoastBetService boastBetService;
    @Mock WalletService walletService;

    BoastServiceImpl boastService;

    @BeforeEach
    void setUp() {
        boastService = new BoastServiceImpl(userStatService, medalService, boastBetService, walletService);
        ReflectionTestUtils.setField(boastService, "baseMapper", boastMapper);
    }

    private BoastDto validDto() {
        BoastDto dto = new BoastDto();
        dto.setTitle("测试挑战");
        dto.setOptionOne("A");
        dto.setOptionTwo("B");
        dto.setCorrectOption(BOAST_OPTION_ONE);
        dto.setStakeAmount(100L);
        dto.setDeadline(LocalDateTime.now().plusHours(2));
        return dto;
    }

    @Test
    void create_sameOptions_throwsException() {
        BoastDto dto = validDto();
        dto.setOptionTwo("A");

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> boastService.create(dto, 1L));
        assertEquals("两个选项不能相同", ex.getMessage());
    }

    @Test
    void create_pastDeadline_throwsException() {
        BoastDto dto = validDto();
        dto.setDeadline(LocalDateTime.now().minusHours(1));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> boastService.create(dto, 1L));
        assertEquals("截止时间必须晚于当前时间", ex.getMessage());
    }

    @Test
    void create_insufficientCoins_throwsException() {
        BoastDto dto = validDto();
        UserStat stat = new UserStat();
        stat.setCoin(50L);
        when(userStatService.getById(1L)).thenReturn(stat);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> boastService.create(dto, 1L));
        assertTrue(ex.getMessage().contains("金币不够"));
    }

    @Test
    void create_success_savesBoast() {
        BoastDto dto = validDto();
        UserStat stat = new UserStat();
        stat.setCoin(1000L);

        when(userStatService.getById(1L)).thenReturn(stat);
        when(boastMapper.insert(any())).thenReturn(1);

        Boast result = boastService.create(dto, 1L);

        assertNotNull(result);
        assertEquals(dto.getTitle(), result.getTitle());
        assertEquals(dto.getOptionOne(), result.getOptionOne());
        assertEquals(dto.getOptionTwo(), result.getOptionTwo());
        assertEquals(dto.getCorrectOption(), result.getCorrectOption());
        assertEquals(dto.getStakeAmount(), result.getStakeAmount());
        assertEquals(BOAST_ONGOING, result.getResult());
        assertEquals(1L, result.getUserId());
    }

    @Test
    void bet_success_deductsStakeAndWritesBoastBetWalletLog() {
        Boast boast = new Boast();
        boast.setId(7L);
        boast.setUserId(1L);
        boast.setResult(BOAST_ONGOING);
        boast.setDeadline(LocalDateTime.now().plusHours(1));
        boast.setStakeAmount(100L);
        when(boastMapper.selectOne(any())).thenReturn(boast);
        when(boastBetService.count(any())).thenReturn(0L);
        UserStat stat = new UserStat();
        stat.setCoin(200L);
        when(userStatService.getById(2L)).thenReturn(stat);
        when(boastBetService.save(any(BoastBet.class))).thenReturn(true);
        when(userStatService.getBaseMapper()).thenReturn(userStatMapper);
        when(userStatMapper.update(isNull(), any())).thenReturn(1);

        BoastBet result = boastService.bet(7L, BOAST_OPTION_TWO, 2L);

        assertNotNull(result);
        assertEquals(7L, result.getBoastId());
        assertEquals(2L, result.getUserId());
        assertEquals(BOAST_OPTION_TWO, result.getOptionType());
        assertEquals(100L, result.getAmount());
        assertEquals(BOAST_BET_UNSETTLED, result.getResult());
        verify(walletService).changeCoin(eq(2L), eq(-100L), eq(WALLET_BOAST_BET), eq(7L), anyString(), anyString());
    }

    @Test
    void settle_alreadySettled_throwsException() {
        Boast boast = new Boast();
        boast.setId(1L);
        boast.setUserId(1L);
        boast.setResult(BOAST_CREATOR_WIN);
        when(boastMapper.selectOne(any())).thenReturn(boast);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> boastService.settle(1L, BOAST_CREATOR_WIN, 1L));
        assertEquals("挑战已结算", ex.getMessage());
    }

    @Test
    void settle_unauthorizedUser_throwsException() {
        Boast boast = new Boast();
        boast.setId(1L);
        boast.setUserId(1L);
        boast.setResult(BOAST_ONGOING);
        when(boastMapper.selectOne(any())).thenReturn(boast);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> boastService.settle(1L, BOAST_CREATOR_WIN, 999L));
        assertEquals("无权操作", ex.getMessage());
    }

    @Test
    void settle_invalidResult_throwsException() {
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> boastService.settle(1L, 99, 1L));
        assertEquals("结算结果无效", ex.getMessage());
    }
}
