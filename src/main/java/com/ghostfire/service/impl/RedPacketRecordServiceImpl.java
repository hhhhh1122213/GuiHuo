package com.ghostfire.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ghostfire.entity.RedPacketRecord;
import com.ghostfire.mapper.RedPacketRecordMapper;
import com.ghostfire.service.RedPacketRecordService;
import org.springframework.stereotype.Service;

@Service
public class RedPacketRecordServiceImpl extends ServiceImpl<RedPacketRecordMapper, RedPacketRecord> implements RedPacketRecordService {
}
