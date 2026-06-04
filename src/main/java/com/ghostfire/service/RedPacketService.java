package com.ghostfire.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ghostfire.dto.RedPacketDto;
import com.ghostfire.entity.RedPacket;
import com.ghostfire.entity.RedPacketRecord;

public interface RedPacketService extends IService<RedPacket> {
    RedPacket create(Long userId, RedPacketDto dto);
    RedPacketRecord grab(Long userId, Long packetId);
}
