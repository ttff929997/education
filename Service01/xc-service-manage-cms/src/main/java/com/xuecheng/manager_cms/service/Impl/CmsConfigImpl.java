package com.xuecheng.manager_cms.service.Impl;

import com.xuecheng.framework.domain.cms.CmsConfig;
import com.xuecheng.framework.domain.cms.CmsSite;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.manager_cms.dao.CmsConfigRepository;
import com.xuecheng.manager_cms.dao.CmsSiteRepository;
import com.xuecheng.manager_cms.service.CmsConfigService;
import com.xuecheng.manager_cms.service.SiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
public class CmsConfigImpl implements CmsConfigService {

    @Autowired
    private CmsConfigRepository cmsConfigRepository;


    @Override
    public CmsConfig findById(String id) {
        Optional<CmsConfig> byId = cmsConfigRepository.findById(id);
        if (byId.isPresent()) {
            CmsConfig cmsConfig = byId.get();
            return cmsConfig;
        }
        return null;
    }
}
