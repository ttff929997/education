package com.xuecheng.manager_cms.service;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.domain.course.response.CoursePublishResult;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;
import io.swagger.annotations.ApiOperation;

public interface PageService {
    public QueryResponseResult findList( int page, int size, QueryPageRequest queryPageRequest);

    public CmsPageResult add(CmsPage cmsPage);


    public CmsPage findById(String id);

    public CmsPageResult edit(String id,CmsPage cmsPage);

    public ResponseResult delete(String id);

    public String getPageHtml(String pageId);

    public ResponseResult postPage(String pageId);

    public CmsPageResult save(CmsPage cmsPage);

    public CmsPostPageResult postPageQuick(CmsPage cmsPage);

}
