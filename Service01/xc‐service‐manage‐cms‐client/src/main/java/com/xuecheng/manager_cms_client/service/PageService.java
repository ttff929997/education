package com.xuecheng.manager_cms_client.service;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsSite;
import com.xuecheng.framework.domain.cms.response.CmsCode;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.manager_cms_client.dao.CmsPageRepository;
import com.xuecheng.manager_cms_client.dao.CmsSiteRepository;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Optional;

@Service
public class PageService {
    private static  final Logger LOGGER = LoggerFactory.getLogger(PageService.class);

    @Autowired
    CmsPageRepository cmsPageRepository;
    @Autowired
    CmsSiteRepository cmsSiteRepository;
    @Autowired
    GridFsTemplate gridFsTemplate;
    @Autowired
    GridFSBucket gridFSBucket;

    public void savePageToServerPath(String pageId){
        Optional<CmsPage> byId = cmsPageRepository.findById(pageId);
        if (!byId.isPresent()) {
            ExceptionCast.throwException(CmsCode.CMS_PAGE_NOTEXISTS); 
        }
        CmsPage cmsPage = byId.get();
        String siteId = cmsPage.getSiteId();
        String pagePhysicalPath = cmsPage.getPagePhysicalPath();
        Optional<CmsSite> byId1 = cmsSiteRepository.findById(siteId);
        if (!byId1.isPresent()) {
            ExceptionCast.throwException(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        CmsSite cmsSite = byId1.get();
        String sitePhysicalPath = cmsSite.getSitePhysicalPath();
        String path = sitePhysicalPath+pagePhysicalPath+cmsPage.getPageName();

        String pageHtmlFileId = cmsPage.getHtmlFileId();
        InputStream inputStream = this.getFileById(pageHtmlFileId);
        if(inputStream == null){
            LOGGER.error("getFileById InputStream is null ,htmlFileId:{}",pageHtmlFileId);
            return ;
        }
        FileOutputStream fileOutputStream =null;
        try {
            fileOutputStream = new FileOutputStream(new File(path));
            IOUtils.copy(inputStream,fileOutputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public InputStream getFileById(String fileId){
        try {
        GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(fileId)));
        GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
        GridFsResource gridFsResource = new GridFsResource(gridFSFile,gridFSDownloadStream);
        return gridFsResource.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
