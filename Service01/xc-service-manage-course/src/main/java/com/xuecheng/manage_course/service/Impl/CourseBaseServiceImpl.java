package com.xuecheng.manage_course.service.Impl;

import com.xuecheng.framework.domain.course.CourseBase;
import com.xuecheng.framework.domain.course.response.AddCourseResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_course.dao.CourseBaseRepository;
import com.xuecheng.manage_course.service.CourseBaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class CourseBaseServiceImpl implements CourseBaseService {
    @Autowired
    CourseBaseRepository courseBaseRepository;

    @Override
    @Transactional
    public AddCourseResult addCourseBase(CourseBase courseBase) {
        courseBase.setStatus("202001");
        courseBaseRepository.save(courseBase);
        return new AddCourseResult(CommonCode.SUCCESS,courseBase.getId());
    }

    @Override
    public CourseBase findCourseBase(String id) {
        Optional<CourseBase> optional = courseBaseRepository.findById(id);
        if (optional.isPresent()) {
            CourseBase courseBase = optional.get();
            return courseBase;
        }
        return null;
    }

    @Override
    @Transactional
    public ResponseResult updateCourseBase(String id, CourseBase courseBase) {
        CourseBase one = this.findById(id);
        if (one == null){
            ExceptionCast.throwException(CommonCode.INVALID_PARAM);
        }
        //修改课程信息
        one.setName(courseBase.getName());
        one.setMt(courseBase.getMt());
        one.setSt(courseBase.getSt());
        one.setGrade(courseBase.getGrade());
        one.setStudymodel(courseBase.getStudymodel());
        one.setUsers(courseBase.getUsers());
        one.setDescription(courseBase.getDescription());
        CourseBase save = courseBaseRepository.save(one);
        return new ResponseResult(CommonCode.SUCCESS);

    }

    public CourseBase findById(String id){
        Optional<CourseBase> optionalCourseBase = courseBaseRepository.findById(id);
        if (!optionalCourseBase.isPresent()){
            ExceptionCast.throwException(CommonCode.INVALID_PARAM);
        }
        CourseBase courseBase = optionalCourseBase.get();
        return courseBase;
    }
}
