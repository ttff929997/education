package com.xuecheng.manage_media.service.Impl;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.datatype.jsr310.DecimalUtils;
import com.netflix.discovery.converters.Auto;
import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.response.CheckChunkResult;
import com.xuecheng.framework.domain.media.response.MediaCode;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_media.config.RabbitMQConfig;
import com.xuecheng.manage_media.controller.MediaUploadController;
import com.xuecheng.manage_media.dao.MediaFileRepository;
import com.xuecheng.manage_media.service.MediaUploadService;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

@Service
public class MediaUploadServiceImpl implements MediaUploadService {
    private final static Logger LOGGER = LoggerFactory.getLogger(MediaUploadController.class);

    @Autowired
    MediaFileRepository mediaFileRepository;
    @Autowired
    RabbitTemplate rabbitTemplate;


    @Value("${xcServiceManageMedia.mq.routingkeyMediaVideo}")
    String routingkey_media_video;
    //上传文件根目录
    @Value("${xcServiceManageMedia.uploadLocation}")
    String uploadPath;


    /*** 根据文件md5得到文件路径
     * 规则：
     * * 一级目录：md5的第一个字符
     * * 二级目录：md5的第二个字符
     * * 三级目录：md5
     * * 文件名：md5+文件扩展名
     * * @param fileMd5 文件md5值
     * * @param fileExt 文件扩展名
     * * @return 文件路径 */
    //得到文件所属目录路径
    private String getFileFolderPath(String fileMd5) {
        return uploadPath + fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/";
    }

    //得到文件的路径
    private String getFilePath(String fileMd5, String fileExt) {
        return uploadPath + fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/"
                + fileMd5 + "/" + fileMd5 + "." + fileExt;
    }

    //得到块文件所属目录路径
    private String getChunkFileFolderPath(String fileMd5) {
        return uploadPath + fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/chunk/";
    }


    @Override
    public ResponseResult register(String fileMd5, String fileName, Long fileSize, String mimetype, String fileExt) {
        String fileFolderPath = this.getFileFolderPath(fileMd5);
        String filePath = this.getFilePath(fileMd5, fileExt);
        File file = new File(filePath);
        boolean exists = file.exists();
        Optional<MediaFile> byId = mediaFileRepository.findById(fileMd5);
        if (exists && byId.isPresent()) {
            //文件存在
            ExceptionCast.throwException(MediaCode.UPLOAD_FILE_REGISTER_EXIST);
        }
        File fileFolder = new File(fileFolderPath);
        if (!fileFolder.exists()) {
            fileFolder.mkdirs();
        }
        return new ResponseResult(CommonCode.SUCCESS);
    }


    @Override
    public CheckChunkResult checkchunk(String fileMd5, Integer chunk, Integer chunkSize) {
        String chunkFileFolderPath = this.getChunkFileFolderPath(fileMd5);
        File file = new File(chunkFileFolderPath + chunk);
        if (file.exists()) {
            //块文件存在
            return new CheckChunkResult(CommonCode.SUCCESS, true);
        } else {
            //块文件不存在
            return new CheckChunkResult(CommonCode.SUCCESS, false);
        }
    }

    //上传分块
    public ResponseResult uploadchunk(MultipartFile file, Integer chunk, String fileMd5) {
        //检查分块目录，如果不存在则要自动创建
        //得到分块目录
        String chunkFileFolderPath = this.getChunkFileFolderPath(fileMd5);
        //得到分块文件路径
        String chunkFilePath = chunkFileFolderPath + chunk;

        File chunkFileFolder = new File(chunkFileFolderPath);
        //如果不存在则要自动创建
        if(!chunkFileFolder.exists()){
            chunkFileFolder.mkdirs();
        }
        //得到上传文件的输入流
        InputStream inputStream = null;
        FileOutputStream outputStream  =null;
        try {
            inputStream = file.getInputStream();
            outputStream = new FileOutputStream(new File(chunkFilePath));
            IOUtils.copy(inputStream,outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new ResponseResult(CommonCode.SUCCESS);

    }

    @Override
    public ResponseResult mergechunks(String fileMd5, String fileName, Long fileSize, String mimetype, String fileExt) {
        String chunkFileFolderPath = this.getChunkFileFolderPath(fileMd5);
        File chunkFileFolder = new File(chunkFileFolderPath);

        File[] files = chunkFileFolder.listFiles();
        List<File> fileList = Arrays.asList(files);

        //创建一个合并文件
        String filePath = this.getFilePath(fileMd5, fileExt);
        File mergeFile = new File(filePath);
        //执行合并
        mergeFile = this.mergeFile(fileList, mergeFile);
        if (mergeFile ==null) {
            //合并文件失败
            ExceptionCast.throwException(MediaCode.MERGE_FILE_FAIL);
        }
        //2、校验文件的md5值是否和前端传入的md5一到
        boolean checkFileMd5 = this.checkFileMd5(mergeFile, fileMd5);
        if (!checkFileMd5) {
            //校验文件失败
            ExceptionCast.throwException(MediaCode.MERGE_FILE_CHECKFAIL);
        }
        //3、将文件的信息写入mongodb
        MediaFile mediaFile = new MediaFile();
        mediaFile.setFileId(fileMd5);
        mediaFile.setFileOriginalName(fileName);
        mediaFile.setFileName(fileMd5 + "." + fileExt);
        //文件路径保存相对路径
        String filePath1 = fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/";
        mediaFile.setFilePath(filePath1);
        mediaFile.setFileSize(fileSize);
        mediaFile.setUploadTime(new Date());
        mediaFile.setMimeType(mimetype);
        mediaFile.setFileType(fileExt);
        //状态为上传成功
        mediaFile.setFileStatus("301002");
        mediaFileRepository.save(mediaFile);
        String fileId = mediaFile.getFileId();
        this.sendProcessVideoMsg(fileId);
        return new ResponseResult(CommonCode.SUCCESS);
    }


    //向MQ发送视频处理消息
    public ResponseResult sendProcessVideoMsg(String mediaId){
        Optional<MediaFile> byId = mediaFileRepository.findById(mediaId);
        if (!byId.isPresent()) {
            return new ResponseResult(CommonCode.FAIL);
        }
        Map<String,String> map = new HashMap<>();
        map.put("mediaId",mediaId);
        String s = JSON.toJSONString(map);
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EX_MEDIA_PROCESSTASK, routingkey_media_video, s);
            LOGGER.info("send media process task msg:{}",s);
        }catch (Exception e){
            LOGGER.info("send media process task error,msg is:{},error:{}",s,e.getMessage());
            return new ResponseResult(CommonCode.FAIL);
        }
        return new ResponseResult(CommonCode.SUCCESS);
    }

    //校验文件
    private boolean checkFileMd5(File mergeFile,String md5){
        try {
            FileInputStream inputStream = new FileInputStream(mergeFile);
            //得到文件的md5
            String md5Hex = DigestUtils.md5Hex(inputStream);

            //和传入的md5比较
            if(md5.equalsIgnoreCase(md5Hex)){
                return true;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }



    //合并文件
    private File mergeFile(List<File> chunkFileList, File mergeFile) {
        try {
            //如果合并文件已存在则删除，否则创建新文件
            if (mergeFile.exists()) {
                mergeFile.delete();
            } else {
                //创建一个新文件
                mergeFile.createNewFile();
            }

            //对块文件进行排序
            Collections.sort(chunkFileList, new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    if(Integer.parseInt(o1.getName())>Integer.parseInt(o2.getName())){
                        return 1;
                    }
                    return -1;

                }
            });
            //创建一个写对象
            RandomAccessFile raf_write = new RandomAccessFile(mergeFile,"rw");
            byte[] b = new byte[1024];
            for(File chunkFile:chunkFileList){
                RandomAccessFile raf_read = new RandomAccessFile(chunkFile,"r");
                int len = -1;
                while ((len = raf_read.read(b))!=-1){
                    raf_write.write(b,0,len);
                }
                raf_read.close();
            }
            raf_write.close();
            return mergeFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
