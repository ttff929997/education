package com.xuecheng.manage_course.service;

import com.xuecheng.framework.domain.course.CourseMarket;
import com.xuecheng.framework.model.response.ResponseResult;
import io.swagger.annotations.ApiOperation;

public interface CourseMarketService {

    public CourseMarket getCourseMarketById(String courseId);

    public CourseMarket updateCourseMarket(String id, CourseMarket courseMarket);
}
