package com.xuecheng.manage_course.service.Impl;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.domain.course.*;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.course.ext.CourseView;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.domain.course.response.CourseCode;
import com.xuecheng.framework.domain.course.response.CoursePublishResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_course.client.CmsPageClient;
import com.xuecheng.manage_course.dao.*;
import com.xuecheng.manage_course.service.CourseService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class CourseServiceImpl implements CourseService {
    @Autowired
    private TeachplanMapper teachplanMapper;
    @Autowired
    private CourseMapper courseMapper;
    @Autowired
    private TeachplanRepository teachplanRepository;
    @Autowired
    private CourseBaseRepository courseBaseRepository;
    @Autowired
    private CoursePicRepository coursePicRepository;
    @Autowired
    private CourseMarketRepository courseMarketRepository;
    @Autowired
    private CmsPageClient cmsPageClient;
    @Autowired
    private CoursePubRepository coursePubRepository;
    @Autowired
    private TeachplanMediaRepository teachplanMediaRepository;
    @Autowired
    private TeachplanMediaPubRepository teachplanMediaPubRepository;


    /*private String publish_siteId="5b30cba5f58b4411fc6cb1e5";

    private String publish_dataUrlPre="http://localhost:31200/course/courseview/";

    private String publish_page_physicalpath = "/course/detail/";

    private String publish_page_webpath="/course/detail/";

    private String publish_templateId="5b345a6b94db44269cb2bfec";

    private String previewUrl="http://www.xcproject.com/cms/preview/";*/

    @Value("${coursePublish.dataUrlPre}")
    private String publish_dataUrlPre;
    @Value("${coursePublish.pagePhysicalPath}")
    private String publish_page_physicalpath;
    @Value("${coursePublish.pageWebPath}")
    private String publish_page_webpath;
    @Value("${coursePublish.siteId}")
    private String publish_siteId;
    @Value("${coursePublish.templateId}")
    private String publish_templateId;
    @Value("${coursePublish.previewUrl}")
    private String previewUrl;

    @Override
    public TeachplanNode findTeachplanList(String courseId) {

            return teachplanMapper.selectList(courseId);
        }

    @Override
    @Transactional
    public ResponseResult addTeachplan(Teachplan teachplan) {
        //校验课程id和课程计划名称
        if(teachplan == null
                || StringUtils.isEmpty(teachplan.getCourseid())
                || StringUtils.isEmpty(teachplan.getPname())) {
            ExceptionCast.throwException(CommonCode.INVALID_PARAM);
        }
        String courseid = teachplan.getCourseid();
        String parentid = teachplan.getParentid();
        if (StringUtils.isEmpty(parentid)){
            parentid = this.getTeachplanRoot(courseid);
        }
        Teachplan teachplanNew = new Teachplan();
        //将teachplan的属性拷贝到teachplanNew中
        BeanUtils.copyProperties(teachplan,teachplanNew);
        teachplanNew.setCourseid(courseid);
        teachplanNew.setParentid(parentid);
        Optional<Teachplan> parent = teachplanRepository.findById(parentid);
        if (!parent.isPresent()) {
            ExceptionCast.throwException(CommonCode.INVALID_PARAM);
        }
        Teachplan teachplan1 = parent.get();
        String grade = teachplan1.getGrade();
        if(grade.equals("1")){
            teachplanNew.setGrade("2");
        }else{
            teachplanNew.setGrade("3");
        }
        teachplanNew.setStatus("0");
        teachplanRepository.save(teachplanNew);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    @Override
                    public QueryResponseResult<CourseInfo> findCourseList(String companyId,int page, int size, CourseListRequest courseListRequest) {
        if(courseListRequest == null){
            courseListRequest = new CourseListRequest();
        }
    if (page<=0){
            page =1;
        }
        //page -=1;
        if (size<=0){
            size=10;
        }
        PageHelper.startPage(page,size);
        PageInfo<CourseInfo> courseListPage = new PageInfo<>(courseMapper.findCourseListPage(courseListRequest));
        List<CourseInfo> courseInfos = courseListPage.getList();
        long totalElements = courseListPage.getTotal();
        QueryResult<CourseInfo> queryResult = new QueryResult<CourseInfo>();
        queryResult.setList(courseInfos);
        queryResult.setTotal(totalElements);
        return new QueryResponseResult<CourseInfo>(CommonCode.SUCCESS,queryResult);
    }

    @Override
    @Transactional
    public ResponseResult saveCoursePic(String courseId, String pic) {
        //查询课程图片
        Optional<CoursePic> picOptional = coursePicRepository.findById(courseId);
        CoursePic coursePic = null;
        if (picOptional.isPresent()) {
            coursePic = picOptional.get();
        }
        //没有课程图片则新建对象
        if (coursePic == null) {
            coursePic = new CoursePic();
        }
        coursePic.setCourseid(courseId);
        coursePic.setPic(pic);
        coursePicRepository.save(coursePic);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    @Override
    public CoursePic findCoursepic(String courseId) {
        Optional<CoursePic> byId = coursePicRepository.findById(courseId);
        if (byId.isPresent()) {
            return byId.get();
        }
        return null;
    }

    @Override
    @Transactional
    public ResponseResult deleteCoursePic(String courseId) {
        //执行删除，返回1表示删除成功，返回0表示删除失败
        long result = coursePicRepository.deleteByCourseid(courseId);
        if (result > 0) {
            return new ResponseResult(CommonCode.SUCCESS);
        }
        return new ResponseResult(CommonCode.FAIL);
    }

    @Override
    public CourseView courseview(String id) {
        CourseView courseView = new CourseView();
        //查询课程基本信息
        Optional<CourseBase> optional = courseBaseRepository.findById(id);
        if(optional.isPresent()){
            CourseBase courseBase = optional.get();
            courseView.setCourseBase(courseBase);
        }
        //查询课程营销信息
        Optional<CourseMarket> courseMarketOptional = courseMarketRepository.findById(id);
        if(courseMarketOptional.isPresent()){
            CourseMarket courseMarket = courseMarketOptional.get();
            courseView.setCourseMarket(courseMarket);
        }
        //查询课程图片信息
        Optional<CoursePic> picOptional = coursePicRepository.findById(id);
        if(picOptional.isPresent()){
            CoursePic coursePic = picOptional.get();
            courseView.setCoursePic(picOptional.get());
        }
        //查询课程计划信息
        TeachplanNode teachplanNode = teachplanMapper.selectList(id);
        courseView.setTeachplanNode(teachplanNode);
        return courseView;
    }


    //根据id查询课程基本信息
    public CourseBase findCourseBaseById(String courseId){
        Optional<CourseBase> byId = courseBaseRepository.findById(courseId);
        if (byId.isPresent()) {
            CourseBase courseBase = byId.get();
            return courseBase;
        }
        return null;
    }



    public  String getTeachplanRoot(String courseId){
        Optional<CourseBase> optional = courseBaseRepository.findById(courseId);
        if(!optional.isPresent()){
            return null;
        }
        CourseBase courseBase = optional.get();
        List<Teachplan> byCourseidAndParentid = teachplanRepository.findByCourseidAndParentid(courseId, "0");
        if (byCourseidAndParentid==null || byCourseidAndParentid.size()<=0){
            Teachplan teachplan = new Teachplan();
            teachplan.setCourseid(courseId);
            teachplan.setParentid("0");
            teachplan.setGrade("1");//一级结点
            teachplan.setStatus("0");
            teachplan.setPname(courseBase.getName());
            Teachplan save = teachplanRepository.save(teachplan);
            return save.getId();
        }
        return byCourseidAndParentid.get(0).getId();
    }

    //课程预览
    public CoursePublishResult preview(String id) {
        //查询课程
        CourseBase courseBaseById = this.findCourseBaseById(id);
        //请求cms添加页面
        //准备cmsPage信息
        CmsPage cmsPage = new CmsPage();
        cmsPage.setSiteId(publish_siteId);//站点id
        cmsPage.setDataUrl(publish_dataUrlPre+id);//数据模型url
        cmsPage.setPageName(id+".html");//页面名称
        cmsPage.setPageAliase(courseBaseById.getName());//页面别名，就是课程名称
        cmsPage.setPagePhysicalPath(publish_page_physicalpath);//页面物理路径
        cmsPage.setPageWebPath(publish_page_webpath);//页面webpath
        cmsPage.setTemplateId(publish_templateId);//页面模板id

        //远程调用cms
        CmsPageResult cmsPageResult = cmsPageClient.save(cmsPage);
        if(!cmsPageResult.isSuccess()){
            return new CoursePublishResult(CommonCode.FAIL,null);
        }

        CmsPage cmsPage1 = cmsPageResult.getCmsPage();
        String pageId = cmsPage1.getPageId();
        //拼装页面预览的url
        String url = previewUrl+pageId;
        //返回CoursePublishResult对象（当中包含了页面预览的url）
        return new CoursePublishResult(CommonCode.SUCCESS,url);
    }

    @Override
    public CoursePublishResult publish(String id) {
        //查询课程
        CourseBase courseBaseById = this.findCourseBaseById(id);

        //请求cms添加页面
        //准备cmsPage信息
        CmsPage cmsPage = new CmsPage();
        cmsPage.setSiteId(publish_siteId);//站点id
        cmsPage.setDataUrl(publish_dataUrlPre+id);//数据模型url
        cmsPage.setPageName(id+".html");//页面名称
        cmsPage.setPageAliase(courseBaseById.getName());//页面别名，就是课程名称
        cmsPage.setPagePhysicalPath(publish_page_physicalpath);//页面物理路径
        cmsPage.setPageWebPath(publish_page_webpath);//页面webpath
        cmsPage.setTemplateId(publish_templateId);//页面模板id
        CmsPostPageResult cmsPostPageResult = cmsPageClient.postPageQuick(cmsPage);
        if (cmsPostPageResult == null) {
            return new CoursePublishResult(CommonCode.FAIL,null);
        }
        CourseBase courseBase = this.saveCoursePubState(id);
        if (courseBase == null) {
            return new CoursePublishResult(CommonCode.FAIL,null);
        }
        String pageUrl = cmsPostPageResult.getPageUrl();
        CoursePub coursePub =this.createCoursePub(id);

        this.saveCoursePub(id,coursePub);

        this.saveTeachplanMediaPub(id);
        return new CoursePublishResult(CommonCode.SUCCESS,pageUrl);
    }


    //向teachplanMediaPub中保存课程媒资信息
    private void saveTeachplanMediaPub(String courseId){
        //先删除teachplanMediaPub中的数据
        teachplanMediaPubRepository.deleteByCourseId(courseId);
        //从teachplanMedia中查询
        List<TeachplanMedia> teachplanMediaList = teachplanMediaRepository.findByCourseId(courseId);
        List<TeachplanMediaPub> teachplanMediaPubs = new ArrayList<>();
        //将teachplanMediaList数据放到teachplanMediaPubs中
        for(TeachplanMedia teachplanMedia:teachplanMediaList){
            TeachplanMediaPub teachplanMediaPub = new TeachplanMediaPub();
            BeanUtils.copyProperties(teachplanMedia,teachplanMediaPub);
            //添加时间戳
            teachplanMediaPub.setTimestamp(new Date());
            teachplanMediaPubs.add(teachplanMediaPub);
        }

        //将teachplanMediaList插入到teachplanMediaPub
        teachplanMediaPubRepository.saveAll(teachplanMediaPubs);
    }

    @Override
    public ResponseResult savemedia(TeachplanMedia teachplanMedia) {
        if(teachplanMedia == null || org.apache.commons.lang3.StringUtils.isEmpty(teachplanMedia.getTeachplanId())){
            ExceptionCast.throwException(CommonCode.INVALID_PARAM);
        }
        //校验课程计划是否是3级
        //课程计划
        String teachplanId = teachplanMedia.getTeachplanId();
        //查询到课程计划
        Optional<Teachplan> optional = teachplanRepository.findById(teachplanId);
        if(!optional.isPresent()){
            ExceptionCast.throwException(CommonCode.INVALID_PARAM);
        }
        //查询到教学计划
        Teachplan teachplan = optional.get();
        //取出等级
        String grade = teachplan.getGrade();
        if(org.apache.commons.lang3.StringUtils.isEmpty(grade) || !grade.equals("3")){
            //只允许选择第三级的课程计划关联视频
            ExceptionCast.throwException(CourseCode.COURSE_MEDIA_TEACHPLAN_GRADEERROR);
        }
        //查询teachplanMedia
        Optional<TeachplanMedia> mediaOptional = teachplanMediaRepository.findById(teachplanId);
        TeachplanMedia one = null;
        if(mediaOptional.isPresent()){
            one = mediaOptional.get();
        }else{
            one = new TeachplanMedia();
        }

        //将one保存到数据库
        one.setCourseId(teachplan.getCourseid());//课程id
        one.setMediaId(teachplanMedia.getMediaId());//媒资文件的id
        one.setMediaFileOriginalName(teachplanMedia.getMediaFileOriginalName());//媒资文件的原始名称
        one.setMediaUrl(teachplanMedia.getMediaUrl());//媒资文件的url
        one.setTeachplanId(teachplanId);
        teachplanMediaRepository.save(one);

        return new ResponseResult(CommonCode.SUCCESS);
    }


    private CoursePub saveCoursePub(String id,CoursePub coursePub){
        CoursePub coursePubNew = null;
        Optional<CoursePub> coursePubOptional = coursePubRepository.findById(id);
        if (coursePubOptional.isPresent()) {
            coursePubNew = coursePubOptional.get();
        }else {
            coursePubNew = new CoursePub();
        }
        BeanUtils.copyProperties(coursePub,coursePubNew);
        coursePubNew.setId(id);
        coursePub.setTimestamp(new Date());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
        String date = simpleDateFormat.format(new Date());
        coursePubNew.setPubTime(date);
        coursePubRepository.save(coursePubNew);
        return coursePubNew;
    }

    private CoursePub createCoursePub(String id){
        CoursePub coursePub= new CoursePub();
        //根据课程id查询course_base
        Optional<CourseBase> baseOptional = courseBaseRepository.findById(id);
        if(baseOptional.isPresent()){
            CourseBase courseBase = baseOptional.get();
            //将courseBase属性拷贝到CoursePub中
            BeanUtils.copyProperties(courseBase,coursePub);
        }

        //查询课程图片
        Optional<CoursePic> picOptional = coursePicRepository.findById(id);
        if(picOptional.isPresent()){
            CoursePic coursePic = picOptional.get();
            BeanUtils.copyProperties(coursePic, coursePub);
        }

        //课程营销信息
        Optional<CourseMarket> marketOptional = courseMarketRepository.findById(id);
        if(marketOptional.isPresent()){
            CourseMarket courseMarket = marketOptional.get();
            BeanUtils.copyProperties(courseMarket, coursePub);
        }
        TeachplanNode teachplanNode = teachplanMapper.selectList(id);
        String jsonString = JSON.toJSONString(teachplanNode);
        coursePub.setTeachplan(jsonString);
        return coursePub;
    }

    //更新课程状态为已发布 202002
    private CourseBase  saveCoursePubState(String courseId){
        CourseBase courseBaseById = this.findCourseBaseById(courseId);
        courseBaseById.setStatus("202002");
        CourseBase save = courseBaseRepository.save(courseBaseById);
        return save;
    }

}
