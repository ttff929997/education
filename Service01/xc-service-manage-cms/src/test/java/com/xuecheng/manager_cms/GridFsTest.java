package com.xuecheng.manager_cms;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @author Administrator
 * @version 1.0
 * @create 2018-09-12 18:11
 **/
@SpringBootTest
@RunWith(SpringRunner.class)
public class GridFsTest {

    @Autowired
    GridFsTemplate gridFsTemplate;

    @Autowired
    GridFSBucket gridFSBucket;

    //存文件
    @Test

    public void testGridFs() throws FileNotFoundException {
        //要存储的文件
        File file = new File("d:/course.ftl"); //定义输入流
         FileInputStream inputStram = new FileInputStream(file); //向GridFS存储文件
         ObjectId objectId = gridFsTemplate.store(inputStram, "Bootstrap开发框架1", ""); //得到文件ID
         String fileId = objectId.toString();
         System.out.println(file);
    }

    public void getFile() throws IOException {
        GridFSFile id = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is("6250410595ab663ccc7efcf9")));
        GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(id.getObjectId());
        GridFsResource gridFsResource =new GridFsResource(id,gridFSDownloadStream);
        String s = IOUtils.toString(gridFsResource.getInputStream(), "UTF-8");
        System.out.println(s);
    }

}
