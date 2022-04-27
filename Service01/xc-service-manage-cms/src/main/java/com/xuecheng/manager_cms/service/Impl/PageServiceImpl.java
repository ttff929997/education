package com.xuecheng.manager_cms.service.Impl;

import com.alibaba.fastjson.JSON;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsSite;
import com.xuecheng.framework.domain.cms.CmsTemplate;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsCode;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.domain.ucenter.ext.AuthToken;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manager_cms.config.RabbitmqConfig;
import com.xuecheng.manager_cms.dao.CmsPageRepository;
import com.xuecheng.manager_cms.dao.CmsSiteRepository;
import com.xuecheng.manager_cms.dao.CmsTemplateRepository;
import com.xuecheng.manager_cms.service.PageService;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.util.Base64Utils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@Service
public class PageServiceImpl implements PageService {
    @Autowired
    private CmsPageRepository cmsPageRepository;
    @Autowired
    private CmsTemplateRepository cmsTemplateRepository;
    @Autowired
    private CmsSiteRepository cmsSiteRepository;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private GridFsTemplate gridFsTemplate;
    @Autowired
    private GridFSBucket gridFSBucket;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Override
    public QueryResponseResult findList(int page, int size, QueryPageRequest queryPageRequest) {
        if (page<=0){
            page =1;
        }
        page -=1;
        if (size<=0){
            size=10;
        }
        if (queryPageRequest ==null){
             queryPageRequest = new QueryPageRequest();
        }
        //自定义条件查询
        //定义条件匹配器
        ExampleMatcher exampleMatcher = ExampleMatcher.matching()
                .withMatcher("pageAliase", ExampleMatcher.GenericPropertyMatchers.contains());
        //条件值对象
        CmsPage cmsPage = new CmsPage();
        //设置条件值（站点id）
        if (StringUtils.isNotEmpty(queryPageRequest.getSiteId())) {
        cmsPage.setSiteId(queryPageRequest.getSiteId());
        }
        //设置模板id作为查询条件
        if(StringUtils.isNotEmpty(queryPageRequest.getTemplateId())){
            cmsPage.setTemplateId(queryPageRequest.getTemplateId());
        }
        //设置页面别名作为查询条件
        if(StringUtils.isNotEmpty(queryPageRequest.getPageAliase())){
            cmsPage.setPageAliase(queryPageRequest.getPageAliase());
        }
        Example<CmsPage> example = Example.of(cmsPage,exampleMatcher);

        Pageable pageable = PageRequest.of(page,size);
        Page<CmsPage> all = cmsPageRepository.findAll(example,pageable);
        QueryResult<CmsPage> queryResult = new QueryResult<CmsPage>();
        queryResult.setList(all.getContent());
        queryResult.setTotal(all.getTotalElements());
        QueryResponseResult queryResponseResult = new QueryResponseResult(CommonCode.SUCCESS,queryResult);
        return queryResponseResult;
    }

    @Override
    public CmsPageResult add(CmsPage cmsPage) {
        CmsPage findPage = cmsPageRepository.findByPageNameAndSiteIdAndPageWebPath(cmsPage.getPageName(), cmsPage.getSiteId(), cmsPage.getPageWebPath());
        if (findPage !=null){
            ExceptionCast.throwException(CmsCode.CMS_ADDPAGE_EXISTSNAME);
        }
            cmsPage.setPageId(null);
            cmsPageRepository.save(cmsPage);
            CmsPageResult cmsPageResult = new CmsPageResult(CommonCode.SUCCESS,cmsPage);
            return cmsPageResult;
    }

    @Override
    public CmsPage findById(String id) {
        Optional<CmsPage> byId = cmsPageRepository.findById(id);
        if (byId.isPresent()){
            return byId.get();
        }
        return null;
    }

    @Override
    public CmsPageResult edit(String id,CmsPage cmsPage) {
        CmsPage one = this.findById(id);
        if (one !=null) {
            one.setTemplateId(cmsPage.getTemplateId()); //更新所属站点
            one.setSiteId(cmsPage.getSiteId()); //更新页面别名
            one.setPageAliase(cmsPage.getPageAliase()); //更新页面名称
            one.setPageName(cmsPage.getPageName()); //更新访问路径
            one.setPageWebPath(cmsPage.getPageWebPath()); //更新物理路径
            one.setPagePhysicalPath(cmsPage.getPagePhysicalPath()); //执行更新
            one.setDataUrl(cmsPage.getDataUrl());
        }
        CmsPage save = cmsPageRepository.save(one);
        if (save !=null){
            return new CmsPageResult(CommonCode.SUCCESS,save);
        }
        return new CmsPageResult(CommonCode.FAIL,null);
    }

    @Override
    public ResponseResult delete(String id) {
        CmsPage byId = this.findById(id);
        if (byId !=null){
            cmsPageRepository.deleteById(id);
            return new ResponseResult(CommonCode.SUCCESS);
        }

        return new ResponseResult(CommonCode.FAIL);
    }


    /**
     * 静态化程序获取页面的DataUrl
     * 静态化程序远程请求DataUrl获取数据模型
     * 静态化程序获取页面的模板信息
     * 执行页面静态化
     * @param pageId
     * @return
     */
    @Override
    public String getPageHtml(String pageId) {
        Map model = this.getModel(pageId);
        if (model == null) {
            //获取页面模型数据为空
            ExceptionCast.throwException(CmsCode.CMS_GENERATEHTML_DATAISNULL); }
        //获取页面模板
        String templateContent = getInfo(pageId);
        if(StringUtils.isEmpty(templateContent)){
            //页面模板为空
            ExceptionCast.throwException(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL); }
        //执行静态化
        String html = generateHtml(templateContent, model);
        if (StringUtils.isEmpty(html)) {
            ExceptionCast.throwException(CmsCode.CMS_GENERATEHTML_HTMLISNULL);
        }
        return html;
    }

    //执行页面静态化
    public String generateHtml(String templateContent,Map model){
        try {
            Configuration configuration =new Configuration(Configuration.getVersion());
            StringTemplateLoader  stringTemplateLoader = new StringTemplateLoader();
            stringTemplateLoader.putTemplate("template",templateContent);
            configuration.setTemplateLoader(stringTemplateLoader);
            Template template = configuration.getTemplate("template");
            String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
            return html;
        } catch (Exception e) {
            e.printStackTrace();
        }

       return null;
    }

    //静态化程序获取页面的模板信息
    private String getInfo(String pageId){
        CmsPage page = this.findById(pageId);
        if (page ==null){
            ExceptionCast.throwException(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        String templateId = page.getTemplateId();
        if (StringUtils.isEmpty(templateId)){
            ExceptionCast.throwException(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL);
        }
        Optional<CmsTemplate> byId = cmsTemplateRepository.findById(templateId);
        if (byId.isPresent()) {
            CmsTemplate cmsTemplate = byId.get();
            String templateFileId = cmsTemplate.getTemplateFileId();
            GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(templateFileId)));
            GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
            GridFsResource gridFsResource = new GridFsResource(gridFSFile,gridFSDownloadStream);
            try {
                String content = IOUtils.toString(gridFsResource.getInputStream(), "UTF-8");
                return content;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    //静态化程序远程请求DataUrl获取数据模型
    private Map getModel(String pageId){
        CmsPage page = this.findById(pageId);
        if (page ==null){
            ExceptionCast.throwException(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        String dataUrl = page.getDataUrl();
        if (StringUtils.isEmpty(dataUrl)){
            ExceptionCast.throwException(CmsCode.CMS_GENERATEHTML_DATAURLISNULL);
        }

        /*
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        HttpServletResponse response = attributes.getResponse();
        String token = request.getHeader("uid");
        String name ="user_token:"+token;
        String value = stringRedisTemplate.opsForValue().get(name);
        AuthToken authToken = JSON.parseObject(value, AuthToken.class);
        String access_token = authToken.getAccess_token();
        MultiValueMap<String,String> headers = new LinkedMultiValueMap<>();
        byte[] encode = Base64Utils.encode(access_token.getBytes());
        String head = "Bearer "+access_token;
        headers.add("Authorization",head);
        HttpEntity<MultiValueMap<String,String>> httpEntity = new HttpEntity<MultiValueMap<String, String>>(null,headers);
        ResponseEntity<Map> mapResponseEntity = restTemplate.exchange(dataUrl, HttpMethod.POST, httpEntity, Map.class);
        */

        ResponseEntity<Map> forEntity = restTemplate.getForEntity(dataUrl, Map.class);
        Map body = forEntity.getBody();

        return body;
    }


    public ResponseResult postPage(String pageId){
        String pageHtml = this.getPageHtml(pageId);
        CmsPage cmsPage = this.saveHtml(pageId,pageHtml);
        this.sendPostPage(pageId);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    private CmsPage saveHtml(String pageId,String content)  {
        CmsPage cmsPage = this.findById(pageId);
        String pageHtmlFileId = cmsPage.getHtmlFileId();
        ObjectId objectId = null;
        try {
            //将htmlContent内容转成输入流
            InputStream inputStream = IOUtils.toInputStream(content, "utf-8");
            //将html文件内容保存到GridFS
            objectId = gridFsTemplate.store(inputStream, cmsPage.getPageName());
        } catch (IOException e) {
            e.printStackTrace();
        }
        cmsPage.setHtmlFileId(objectId.toHexString());
        cmsPageRepository.save(cmsPage);
        return cmsPage;
    }

    private void sendPostPage(String pageId){
        CmsPage cmsPage = this.findById(pageId);
        String siteId = cmsPage.getSiteId();
        Map<String,String> map = new HashMap<>();
        map.put("pageId",pageId);
        String s = JSON.toJSONString(map);
        rabbitTemplate.convertAndSend(RabbitmqConfig.EX_ROUTING_CMS_POSTPAGE,siteId,s);
    }


    @Override
    public CmsPageResult save(CmsPage cmsPage) {
        CmsPage byPageNameAndSiteIdAndPageWebPath = cmsPageRepository.findByPageNameAndSiteIdAndPageWebPath(cmsPage.getPageName(), cmsPage.getSiteId(), cmsPage.getPageWebPath());
        if (byPageNameAndSiteIdAndPageWebPath !=null){
           return this.edit(byPageNameAndSiteIdAndPageWebPath.getPageId(),cmsPage);
        }
        return this.add(cmsPage);
    }

    @Override
    public CmsPostPageResult postPageQuick(CmsPage cmsPage) {
        CmsPageResult save = this.save(cmsPage);
        if (!save.isSuccess()){
            return new CmsPostPageResult(CommonCode.FAIL,null);
        }
        CmsPage cmsPage1 = save.getCmsPage();
        String pageId = cmsPage.getPageId();
        ResponseResult responseResult = this.postPage(pageId);
        if(!responseResult.isSuccess()){
            return new CmsPostPageResult(CommonCode.FAIL,null);
        }
        String siteId = cmsPage1.getSiteId();
        CmsSite cmsSite = this.findCmsSiteById(siteId);
        //站点域名
            String siteDomain = cmsSite.getSiteDomain();
            //站点web路径
            String siteWebPath = cmsSite.getSiteWebPath();
            //页面web路径
            String pageWebPath = cmsPage1.getPageWebPath();
            //页面名称
            String pageName = cmsPage1.getPageName();
            //页面的web访问地址
            String pageUrl = siteDomain+siteWebPath+pageWebPath+pageName;
            return new CmsPostPageResult(CommonCode.SUCCESS,pageUrl);
    }

    //根据id查询站点信息
    public CmsSite findCmsSiteById(String siteId) {
        Optional<CmsSite> optional = cmsSiteRepository.findById(siteId);
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }




}
