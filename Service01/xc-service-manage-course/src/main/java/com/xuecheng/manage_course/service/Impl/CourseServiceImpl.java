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
        //????????????id?????????????????????
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
        //???teachplan??????????????????teachplanNew???
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
        //??????????????????
        Optional<CoursePic> picOptional = coursePicRepository.findById(courseId);
        CoursePic coursePic = null;
        if (picOptional.isPresent()) {
            coursePic = picOptional.get();
        }
        //?????????????????????????????????
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
        //?????????????????????1???????????????????????????0??????????????????
        long result = coursePicRepository.deleteByCourseid(courseId);
        if (result > 0) {
            return new ResponseResult(CommonCode.SUCCESS);
        }
        return new ResponseResult(CommonCode.FAIL);
    }

    @Override
    public CourseView courseview(String id) {
        CourseView courseView = new CourseView();
        //????????????????????????
        Optional<CourseBase> optional = courseBaseRepository.findById(id);
        if(optional.isPresent()){
            CourseBase courseBase = optional.get();
            courseView.setCourseBase(courseBase);
        }
        //????????????????????????
        Optional<CourseMarket> courseMarketOptional = courseMarketRepository.findById(id);
        if(courseMarketOptional.isPresent()){
            CourseMarket courseMarket = courseMarketOptional.get();
            courseView.setCourseMarket(courseMarket);
        }
        //????????????????????????
        Optional<CoursePic> picOptional = coursePicRepository.findById(id);
        if(picOptional.isPresent()){
            CoursePic coursePic = picOptional.get();
            courseView.setCoursePic(picOptional.get());
        }
        //????????????????????????
        TeachplanNode teachplanNode = teachplanMapper.selectList(id);
        courseView.setTeachplanNode(teachplanNode);
        return courseView;
    }


    //??????id????????????????????????
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
            teachplan.setGrade("1");//????????????
            teachplan.setStatus("0");
            teachplan.setPname(courseBase.getName());
            Teachplan save = teachplanRepository.save(teachplan);
            return save.getId();
        }
        return byCourseidAndParentid.get(0).getId();
    }

    //????????????
    public CoursePublishResult preview(String id) {
        //????????????
        CourseBase courseBaseById = this.findCourseBaseById(id);
        //??????cms????????????
        //??????cmsPage??????
        CmsPage cmsPage = new CmsPage();
        cmsPage.setSiteId(publish_siteId);//??????id
        cmsPage.setDataUrl(publish_dataUrlPre+id);//????????????url
        cmsPage.setPageName(id+".html");//????????????
        cmsPage.setPageAliase(courseBaseById.getName());//?????????????????????????????????
        cmsPage.setPagePhysicalPath(publish_page_physicalpath);//??????????????????
        cmsPage.setPageWebPath(publish_page_webpath);//??????webpath
        cmsPage.setTemplateId(publish_templateId);//????????????id

        //????????????cms
        CmsPageResult cmsPageResult = cmsPageClient.save(cmsPage);
        if(!cmsPageResult.isSuccess()){
            return new CoursePublishResult(CommonCode.FAIL,null);
        }

        CmsPage cmsPage1 = cmsPageResult.getCmsPage();
        String pageId = cmsPage1.getPageId();
        //?????????????????????url
        String url = previewUrl+pageId;
        //??????CoursePublishResult???????????????????????????????????????url???
        return new CoursePublishResult(CommonCode.SUCCESS,url);
    }

    @Override
    public CoursePublishResult publish(String id) {
        //????????????
        CourseBase courseBaseById = this.findCourseBaseById(id);

        //??????cms????????????
        //??????cmsPage??????
        CmsPage cmsPage = new CmsPage();
        cmsPage.setSiteId(publish_siteId);//??????id
        cmsPage.setDataUrl(publish_dataUrlPre+id);//????????????url
        cmsPage.setPageName(id+".html");//????????????
        cmsPage.setPageAliase(courseBaseById.getName());//?????????????????????????????????
        cmsPage.setPagePhysicalPath(publish_page_physicalpath);//??????????????????
        cmsPage.setPageWebPath(publish_page_webpath);//??????webpath
        cmsPage.setTemplateId(publish_templateId);//????????????id
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


    //???teachplanMediaPub???????????????????????????
    private void saveTeachplanMediaPub(String courseId){
        //?????????teachplanMediaPub????????????
        teachplanMediaPubRepository.deleteByCourseId(courseId);
        //???teachplanMedia?????????
        List<TeachplanMedia> teachplanMediaList = teachplanMediaRepository.findByCourseId(courseId);
        List<TeachplanMediaPub> teachplanMediaPubs = new ArrayList<>();
        //???teachplanMediaList????????????teachplanMediaPubs???
        for(TeachplanMedia teachplanMedia:teachplanMediaList){
            TeachplanMediaPub teachplanMediaPub = new TeachplanMediaPub();
            BeanUtils.copyProperties(teachplanMedia,teachplanMediaPub);
            //???????????????
            teachplanMediaPub.setTimestamp(new Date());
            teachplanMediaPubs.add(teachplanMediaPub);
        }

        //???teachplanMediaList?????????teachplanMediaPub
        teachplanMediaPubRepository.saveAll(teachplanMediaPubs);
    }

    @Override
    public ResponseResult savemedia(TeachplanMedia teachplanMedia) {
        if(teachplanMedia == null || org.apache.commons.lang3.StringUtils.isEmpty(teachplanMedia.getTeachplanId())){
            ExceptionCast.throwException(CommonCode.INVALID_PARAM);
        }
        //???????????????????????????3???
        //????????????
        String teachplanId = teachplanMedia.getTeachplanId();
        //?????????????????????
        Optional<Teachplan> optional = teachplanRepository.findById(teachplanId);
        if(!optional.isPresent()){
            ExceptionCast.throwException(CommonCode.INVALID_PARAM);
        }
        //?????????????????????
        Teachplan teachplan = optional.get();
        //????????????
        String grade = teachplan.getGrade();
        if(org.apache.commons.lang3.StringUtils.isEmpty(grade) || !grade.equals("3")){
            //???????????????????????????????????????????????????
            ExceptionCast.throwException(CourseCode.COURSE_MEDIA_TEACHPLAN_GRADEERROR);
        }
        //??????teachplanMedia
        Optional<TeachplanMedia> mediaOptional = teachplanMediaRepository.findById(teachplanId);
        TeachplanMedia one = null;
        if(mediaOptional.isPresent()){
            one = mediaOptional.get();
        }else{
            one = new TeachplanMedia();
        }

        //???one??????????????????
        one.setCourseId(teachplan.getCourseid());//??????id
        one.setMediaId(teachplanMedia.getMediaId());//???????????????id
        one.setMediaFileOriginalName(teachplanMedia.getMediaFileOriginalName());//???????????????????????????
        one.setMediaUrl(teachplanMedia.getMediaUrl());//???????????????url
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
        //????????????id??????course_base
        Optional<CourseBase> baseOptional = courseBaseRepository.findById(id);
        if(baseOptional.isPresent()){
            CourseBase courseBase = baseOptional.get();
            //???courseBase???????????????CoursePub???
            BeanUtils.copyProperties(courseBase,coursePub);
        }

        //??????????????????
        Optional<CoursePic> picOptional = coursePicRepository.findById(id);
        if(picOptional.isPresent()){
            CoursePic coursePic = picOptional.get();
            BeanUtils.copyProperties(coursePic, coursePub);
        }

        //??????????????????
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

    //?????????????????????????????? 202002
    private CourseBase  saveCoursePubState(String courseId){
        CourseBase courseBaseById = this.findCourseBaseById(courseId);
        courseBaseById.setStatus("202002");
        CourseBase save = courseBaseRepository.save(courseBaseById);
        return save;
    }

}
