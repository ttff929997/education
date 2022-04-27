package com.xuecheng.framework.domain.cms.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class QueryPageRequest {
    @ApiModelProperty("站点id")
    //站点ID
    private String siteId;
    @ApiModelProperty("页面ID")
    //页面ID
    private String pageId;
    @ApiModelProperty("页面名称")
    //页面名称
    private String pageName;
    @ApiModelProperty("页面别名")
    //别名
    private String pageAliase;
    @ApiModelProperty("模版id")
    //模版id
    private String templateId;
}
