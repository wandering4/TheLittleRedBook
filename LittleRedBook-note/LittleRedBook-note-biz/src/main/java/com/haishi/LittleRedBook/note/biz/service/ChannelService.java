package com.haishi.LittleRedBook.note.biz.service;

import com.haishi.LittleRedBook.note.biz.domain.dataobject.ChannelDO;

import java.util.List;

public interface ChannelService {
    List<ChannelDO> findAll();
}
