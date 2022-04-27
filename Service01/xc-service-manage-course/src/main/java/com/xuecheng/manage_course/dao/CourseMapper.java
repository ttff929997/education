package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.course.CourseBase;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by Administrator.
 */
@Mapper
@Repository
public interface CourseMapper {
   CourseBase findCourseBaseById(String id);

   List<CourseInfo> findCourseListPage(CourseListRequest courseListRequest);
}
