package com.xuecheng.manager_cms.web.controller;

import com.xuecheng.api.cms.CmsConfControllerApi;
import com.xuecheng.framework.domain.cms.CmsConfig;
import com.xuecheng.manager_cms.service.CmsConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cms/conf")
public class CmsConfigController implements CmsConfControllerApi {
    @Autowired
    CmsConfigService cmsConfigService;

    @RequestMapping("/get/{id}")
    @Override
    public CmsConfig findById(@PathVariable("id") String id) {
        CmsConfig cmsConfig = cmsConfigService.findById(id);
        return cmsConfig;
    }
}
