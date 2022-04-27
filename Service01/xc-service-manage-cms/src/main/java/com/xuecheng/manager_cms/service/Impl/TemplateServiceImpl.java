package com.xuecheng.manager_cms.service.Impl;

import com.xuecheng.framework.domain.cms.CmsSite;
import com.xuecheng.framework.domain.cms.CmsTemplate;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;

import com.xuecheng.manager_cms.dao.CmsTemplateRepository;
import com.xuecheng.manager_cms.service.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class TemplateServiceImpl implements TemplateService {
    @Autowired
    private CmsTemplateRepository cmsTemplateRepository;

    @Override
    public QueryResponseResult findList() {

        CmsTemplate cmsTemplate = new CmsTemplate();
        List<CmsTemplate> all = cmsTemplateRepository.findAll();
        QueryResult<CmsTemplate> queryResult = new QueryResult<CmsTemplate>();
        queryResult.setList(all);
        QueryResponseResult queryResponseResult = new QueryResponseResult(CommonCode.SUCCESS,queryResult);
        return queryResponseResult;
    }
}
