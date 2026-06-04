package com.ghostfire.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.ghostfire.entity.UserCheckIn;
import com.ghostfire.entity.UserStat;
import com.ghostfire.mapper.UserCheckInMapper;
import com.ghostfire.service.MedalService;
import com.ghostfire.service.UserStatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckInServiceImplTest {

    @Mock UserStatService userStatService;
    @Mock MedalService medalService;
    @Mock UserCheckInMapper userCheckInMapper;

    CheckInServiceImpl checkInService;

    @BeforeEach
    void setUp() {
        checkInService = new CheckInServiceImpl(userStatService, medalService);
        ReflectionTestUtils.setField(checkInService, "baseMapper", userCheckInMapper);
    }

    @Test
    void checkIn_firstTime_streakIs1() {
        Long userId = 1L;
        @SuppressWarnings("unchecked")
        com.baomidou.mybatisplus.core.mapper.BaseMapper<UserStat> statMapper = mock(com.baomidou.mybatisplus.core.mapper.BaseMapper.class);
        when(userStatService.getBaseMapper()).thenReturn(statMapper);
        when(userCheckInMapper.insert(any())).thenReturn(1);
        when(userCheckInMapper.existsByUserAndDate(eq(userId), any(LocalDate.class))).thenReturn(false);

        UserCheckIn result = checkInService.checkIn(userId);

        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(LocalDate.now(), result.getCheckDate());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<LambdaUpdateWrapper<UserStat>> captor = ArgumentCaptor.forClass(LambdaUpdateWrapper.class);
        verify(statMapper, atLeastOnce()).update(isNull(), captor.capture());

        boolean hasStreakReset = captor.getAllValues().stream()
                .anyMatch(w -> w.getSqlSet().contains("streak_count = 1"));
        assertTrue(hasStreakReset, "首次签到 streak_count 应设为 1");

        verify(medalService).checkAutoAward(userId);
    }

    @Test
    void checkIn_consecutiveDay_streakIncrements() {
        Long userId = 1L;
        @SuppressWarnings("unchecked")
        com.baomidou.mybatisplus.core.mapper.BaseMapper<UserStat> statMapper = mock(com.baomidou.mybatisplus.core.mapper.BaseMapper.class);
        when(userStatService.getBaseMapper()).thenReturn(statMapper);
        when(userCheckInMapper.insert(any())).thenReturn(1);
        when(userCheckInMapper.existsByUserAndDate(eq(userId), eq(LocalDate.now().minusDays(1)))).thenReturn(true);

        checkInService.checkIn(userId);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<LambdaUpdateWrapper<UserStat>> captor = ArgumentCaptor.forClass(LambdaUpdateWrapper.class);
        verify(statMapper, atLeastOnce()).update(isNull(), captor.capture());

        boolean hasStreakIncrement = captor.getAllValues().stream()
                .anyMatch(w -> w.getSqlSet().contains("streak_count = streak_count + 1"));
        assertTrue(hasStreakIncrement, "连续签到 streak_count 应 +1");
    }

    @Test
    void checkIn_alreadyCheckedIn_throwsException() {
        Long userId = 1L;
        when(userCheckInMapper.insert(any())).thenThrow(DuplicateKeyException.class);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> checkInService.checkIn(userId));
        assertEquals("今天已签到", ex.getMessage());
    }
}
