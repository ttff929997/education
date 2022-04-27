package com.xuecheng.manager_cms.service;

import com.xuecheng.framework.domain.cms.CmsConfig;
import com.xuecheng.framework.model.response.QueryResponseResult;

public interface CmsConfigService {
    public CmsConfig findById(String id);
}
