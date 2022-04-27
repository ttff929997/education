package com.xuecheng.manage_course.controller;

import com.xuecheng.api.course.CourseMarketControllerApi;
import com.xuecheng.framework.domain.course.CourseMarket;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_course.service.CourseMarketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/course")
public class CourseMarketController implements CourseMarketControllerApi {

    @Autowired
    CourseMarketService courseMarketService;

    @Override
    @GetMapping("/coursemarket/get/{courseId}")
    public CourseMarket getCourseMarketById(@PathVariable("courseId") String courseId) {
        return courseMarketService.getCourseMarketById(courseId);
    }

    @Override
    @PostMapping("/coursemarket/update/{id}")
    public ResponseResult updateCourseMarket(@PathVariable("id") String id, @RequestBody CourseMarket courseMarket) {
        CourseMarket courseMarket1 = courseMarketService.updateCourseMarket(id, courseMarket);
        if (courseMarket1 != null) {
            return new ResponseResult(CommonCode.SUCCESS);
        }else {
            return new ResponseResult(CommonCode.FAIL);
        }
    }
}
