package com.xuecheng.manage_course.service;

import com.xuecheng.framework.domain.course.CoursePic;
import com.xuecheng.framework.domain.course.Teachplan;
import com.xuecheng.framework.domain.course.TeachplanMedia;
import com.xuecheng.framework.domain.course.ext.CategoryNode;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.course.ext.CourseView;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.domain.course.response.CoursePublishResult;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;
import io.swagger.annotations.ApiOperation;

public interface CourseService {
    public TeachplanNode findTeachplanList(String courseId);

    public ResponseResult addTeachplan(Teachplan teachplan);

    public QueryResponseResult<CourseInfo> findCourseList(String companyId,int page, int size, CourseListRequest courseListReques);

    public ResponseResult saveCoursePic(String courseId,String pic);

    public CoursePic findCoursepic(String courseId);

    public ResponseResult deleteCoursePic(String courseId);

    public CourseView courseview(String id);

    //课程预览
    public CoursePublishResult preview(String id);

    //课程发布
    public CoursePublishResult publish(String id);

    public ResponseResult savemedia(TeachplanMedia teachplanMedia);

}
