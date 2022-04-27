package com.xuecheng.api.course;

import com.xuecheng.framework.domain.course.CourseBase;
import com.xuecheng.framework.domain.course.response.AddCourseResult;
import com.xuecheng.framework.domain.course.response.CoursePublishResult;
import com.xuecheng.framework.model.response.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;



@Api(value = "课程管理", description = "课程管理", tags = {"课程管理"})
public interface CourseBaseControllerApi {

        @ApiOperation("添加课程基础信息")
        public AddCourseResult addCourseBase(CourseBase courseBase);

        @ApiOperation("查看课程基础信息")
        public CourseBase findCourseBase(String id);

        @ApiOperation("修改课程基础信息")
        public ResponseResult updateCourseBase(String id,CourseBase courseBase);


}
