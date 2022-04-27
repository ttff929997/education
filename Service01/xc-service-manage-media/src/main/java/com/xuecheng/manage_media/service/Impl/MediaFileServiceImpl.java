package com.xuecheng.manage_media.service.Impl;


import com.xuecheng.api.media.MediaFileControllerApi;
import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.request.QueryMediaFileRequest;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.manage_media.dao.MediaFileRepository;
import com.xuecheng.manage_media.service.MediaFileService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class MediaFileServiceImpl implements MediaFileService {

    private static Logger logger = LoggerFactory.getLogger(MediaFileService.class);

    @Autowired
    private MediaFileRepository mediaFileRepository;


    @Override
    public QueryResponseResult findList(int page, int size, QueryMediaFileRequest queryMediaFileRequest) {
        if (page<=0){
            page = 1;
        }
        page = page-1;
        if (size<=0){
            size = 10;
        }
        MediaFile mediaFile = new MediaFile();
        if (queryMediaFileRequest==null){
            queryMediaFileRequest = new QueryMediaFileRequest();
        }
        ExampleMatcher exampleMatcher = ExampleMatcher.matching()
                                       .withMatcher("tag",ExampleMatcher.GenericPropertyMatchers.contains()).
                        withMatcher("fileOriginalName", ExampleMatcher.GenericPropertyMatchers.contains())//文件原始名称模糊匹配
        .withMatcher("processStatus", ExampleMatcher.GenericPropertyMatchers.exact());// 处理状态精确匹配（默认）
        //查询条件对象
        if(StringUtils.isNotEmpty(queryMediaFileRequest.getTag())){
            mediaFile.setTag(queryMediaFileRequest.getTag());
        }
        if (StringUtils.isNotEmpty(queryMediaFileRequest.getFileOriginalName())) {
            mediaFile.setFileOriginalName(queryMediaFileRequest.getFileOriginalName());
        }
        if (StringUtils.isNotEmpty(queryMediaFileRequest.getProcessStatus())) {
            mediaFile.setProcessStatus(queryMediaFileRequest.getProcessStatus());
        }
        Example<MediaFile> example  = Example.of(mediaFile,exampleMatcher);
        Pageable pageable = PageRequest.of(page,size);
        Page<MediaFile> all = mediaFileRepository.findAll(example, pageable);
        List<MediaFile> content = all.getContent();
        QueryResult<MediaFile> mediaFileQueryResult = new QueryResult<MediaFile>();
        mediaFileQueryResult.setList(content);
        mediaFileQueryResult.setTotal(all.getTotalElements());
        return new QueryResponseResult(CommonCode.SUCCESS,mediaFileQueryResult);
    }
}
