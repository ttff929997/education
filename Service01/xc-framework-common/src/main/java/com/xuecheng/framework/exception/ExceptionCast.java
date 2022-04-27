package com.xuecheng.framework.exception;

import com.xuecheng.framework.model.response.ResultCode;

public class ExceptionCast {
    public static void throwException(ResultCode resultCode){
        throw new CustomException(resultCode);
    }
}
