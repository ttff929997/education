package com.xuecheng.manager_cms.service.Impl;

import com.xuecheng.framework.domain.system.SysDictionary;
import com.xuecheng.manager_cms.dao.SysDictionaryRepository;
import com.xuecheng.manager_cms.service.SysDictionaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SysDictionaryServiceImpl implements SysDictionaryService {
    @Autowired
    SysDictionaryRepository sysDictionaryRepository;

    @Override
    public SysDictionary getByType(String type) {
        return sysDictionaryRepository.findBydType(type);
    }
}
