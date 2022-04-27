package com.xuecheng.manage_media.service;

import com.xuecheng.framework.domain.media.response.CheckChunkResult;
import com.xuecheng.framework.model.response.ResponseResult;
import org.springframework.web.multipart.MultipartFile;

public interface MediaUploadService {
    public ResponseResult register(String fileMd5, String fileName, Long fileSize, String mimetype, String fileExt);

    public CheckChunkResult checkchunk(String fileMd5, Integer chunk, Integer chunkSize);

    public ResponseResult uploadchunk(MultipartFile file, Integer chunk, String fileMd5);

    public ResponseResult mergechunks(String fileMd5, String fileName, Long fileSize, String mimetype, String fileExt);
}
