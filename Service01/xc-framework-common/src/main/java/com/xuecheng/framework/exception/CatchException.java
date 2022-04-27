package com.xuecheng.framework.exception;

import com.google.common.collect.ImmutableMap;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.model.response.ResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class CatchException {
    private static final Logger LOGGER = LoggerFactory.getLogger(CatchException.class);

    protected static  ImmutableMap<Class<? extends Throwable>,ResultCode> EXCEPTIONS;
    protected static  ImmutableMap.Builder<Class<? extends Throwable>,ResultCode> builder = ImmutableMap.builder();

    @ExceptionHandler(CustomException.class)
    @ResponseBody
    public ResponseResult catchExe(CustomException customException){
        LOGGER.error("catchException:{}",customException.getMessage());
        ResultCode resultCode = customException.getResultCode();
        return new ResponseResult(resultCode);
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseResult catchExcep(Exception exception){
        LOGGER.error("catchException:{}",exception.getMessage());
        if (EXCEPTIONS ==null){
            EXCEPTIONS=builder.build();
        }
        ResultCode resultCode = EXCEPTIONS.get(exception.getClass());
        if (resultCode!=null){
            return new ResponseResult(resultCode);
        }else {
            return new ResponseResult(CommonCode.SERVER_ERROR);
        }

    }

    static {
        builder.put(HttpMessageNotReadableException.class, CommonCode.INVALID_PARAM);
    }

}