package com.xuecheng.manage_course.service;

import com.xuecheng.framework.domain.course.CourseBase;
import com.xuecheng.framework.domain.course.response.AddCourseResult;
import com.xuecheng.framework.model.response.ResponseResult;

public interface CourseBaseService {

    public AddCourseResult addCourseBase(CourseBase courseBase);

    public CourseBase findCourseBase(String id);

    public ResponseResult updateCourseBase(String id, CourseBase courseBase);
}
