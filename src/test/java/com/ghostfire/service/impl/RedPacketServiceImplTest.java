package com.ghostfire.service.impl;

import com.ghostfire.config.BloomFilterHelper;
import com.ghostfire.entity.RedPacket;
import com.ghostfire.mapper.RedPacketMapper;
import com.ghostfire.mapper.RedPacketRecordMapper;
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
class RedPacketServiceImplTest {

    @Mock RedPacketMapper redPacketMapper;
    @Mock RedPacketRecordMapper redPacketRecordMapper;
    @Mock UserStatService userStatService;
    @Mock MedalService medalService;
    @Mock BloomFilterHelper bloomFilter;
    @Mock WalletService walletService;

    RedPacketServiceImpl redPacketService;

    @BeforeEach
    void setUp() {
        redPacketService = new RedPacketServiceImpl(userStatService, medalService, redPacketRecordMapper, bloomFilter, walletService);
        ReflectionTestUtils.setField(redPacketService, "baseMapper", redPacketMapper);
    }

    private RedPacket activePacket(Long remainCount) {
        RedPacket p = new RedPacket();
        p.setId(1L);
        p.setUserId(10L);
        p.setTotalAmount(1000L);
        p.setTotalCount(5);
        p.setRemainCount(remainCount.intValue());
        p.setType(RED_PACKET_RANDOM);
        p.setStatus(RED_PACKET_ACTIVE);
        p.setExpireTime(LocalDateTime.now().plusHours(1));
        return p;
    }

    @Test
    void grab_packetNotFound_throwsException() {
        when(redPacketMapper.selectOne(any())).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> redPacketService.grab(2L, 1L));
        assertEquals("红包不存在", ex.getMessage());
    }

    @Test
    void grab_expired_throwsException() {
        RedPacket p = activePacket(3L);
        p.setExpireTime(LocalDateTime.now().minusHours(1));
        when(redPacketMapper.selectOne(any())).thenReturn(p);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> redPacketService.grab(2L, 1L));
        assertTrue(ex.getMessage().contains("已抢完或已过期"));
    }

    @Test
    void grab_alreadyGrabbed_throwsException() {
        RedPacket p = activePacket(3L);
        when(redPacketMapper.selectOne(any())).thenReturn(p);
        // 布隆过滤器说"可能已抢"，走 DB 检查
        when(bloomFilter.mightContain(anyString(), anyString())).thenReturn(true);
        when(redPacketRecordMapper.selectCount(any())).thenReturn(1L);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> redPacketService.grab(2L, 1L));
        assertTrue(ex.getMessage().contains("已经抢过"));
    }

    @Test
    void grab_finishedPacket_throwsException() {
        RedPacket p = activePacket(0L);
        p.setStatus(RED_PACKET_FINISHED);
        when(redPacketMapper.selectOne(any())).thenReturn(p);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> redPacketService.grab(2L, 1L));
        assertTrue(ex.getMessage().contains("已抢完或已过期"));
    }
}
