package com.xuecheng.api.cms;

import com.xuecheng.framework.domain.cms.CmsConfig;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value="cms模块管理接口",description = "cms模块管理接口")
public interface CmsConfControllerApi {
    @ApiOperation("分页查询页面列表")
    public CmsConfig findById(String id);


}
