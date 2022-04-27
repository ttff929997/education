package com.xuecheng.manager_cms.service.Impl;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsSite;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.manager_cms.dao.CmsSiteRepository;
import com.xuecheng.manager_cms.service.SiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class SiteServiceImpl implements SiteService {
    @Autowired
    private CmsSiteRepository cmsSiteRepository;

    @Override
    public QueryResponseResult findList() {

        CmsSite cmsSite = new CmsSite();
        List<CmsSite> all = cmsSiteRepository.findAll();
        QueryResult<CmsSite> queryResult = new QueryResult<CmsSite>();
        queryResult.setList(all);
        QueryResponseResult queryResponseResult = new QueryResponseResult(CommonCode.SUCCESS,queryResult);
        return queryResponseResult;
    }
}
