package com.haishi.LittleRedBook.note.biz.service.impl;

import com.haishi.LittleRedBook.note.biz.domain.dataobject.ChannelDO;
import com.haishi.LittleRedBook.note.biz.domain.mapper.ChannelDOMapper;
import com.haishi.LittleRedBook.note.biz.service.ChannelService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChannelServiceImpl implements ChannelService {

    @Resource
    private ChannelDOMapper channelDOMapper;

    @Override
    public List<ChannelDO> findAll() {
        return channelDOMapper.findAll();
    }
}
