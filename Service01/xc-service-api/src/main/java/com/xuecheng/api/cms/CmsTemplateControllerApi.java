package com.xuecheng.api.cms;

import com.xuecheng.framework.domain.cms.CmsConfig;
import com.xuecheng.framework.model.response.QueryResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value="cms模板接口",description = "cms模板")
public interface CmsTemplateControllerApi {
    @ApiOperation("模板列表")
    public QueryResponseResult findList();

    @ApiOperation("根据id查询CMS配置信息")
    public CmsConfig getmodel(String id);
}
