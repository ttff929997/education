package com.xuecheng.manage_media;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;

public class MediaTest {

    @Test
    public void testChunk() throws IOException {
        File sourceFile = new File("F:\\education_project\\video\\lucene.mp4");
        String chunkPath = "F:\\education_project\\video\\chunk\\";
        File chunkFolder = new File(chunkPath);
        if (!chunkFolder.exists()){
            chunkFolder.mkdirs();
        }
        long chunckSize  = 1*1024*1024;
        long chunkNum = (long) Math.ceil(sourceFile.length() * 1.0 / chunckSize);
        if (chunkNum<=0){
            chunkNum = 1;
        }
        byte[] b = new byte[1024];
        RandomAccessFile raf_read = new RandomAccessFile(sourceFile,"r");
        for (int i =0;i<chunkNum;i++){
            File file = new File(chunkPath+i);
            boolean newFile = file.createNewFile();
            if (newFile){
                RandomAccessFile raf_write = new RandomAccessFile(file,"rw");
                int len = -1;
                while((len = raf_read.read(b))!=-1){
                    raf_write.write(b,0,len);
                    if (file.length()>chunckSize){
                        break;
                    }
                }
                raf_write.close();
            }
        }
        raf_read.close();
    }

    @Test
    public void testMergeFile() throws IOException {
        //块文件目录
        String chunkFileFolderPath = "F:\\education_project\\video\\chunk\\";
        //块文件目录对象
        File chunkFileFolder = new File(chunkFileFolderPath);
        //块文件列表
        File[] files = chunkFileFolder.listFiles();
        //将块文件排序，按名称升序
        List<File> fileList = Arrays.asList(files);
        Collections.sort(fileList, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                if(Integer.parseInt(o1.getName())>Integer.parseInt(o2.getName())){
                    return 1;
                }
                return -1;

            }
        });

        //合并文件
        File mergeFile = new File("F:\\education_project\\video\\lucene_merge.avi");
        //创建新文件
        boolean newFile = mergeFile.createNewFile();

        //创建写对象
        RandomAccessFile raf_write = new RandomAccessFile(mergeFile,"rw");

        byte[] b = new byte[1024];
        for(File chunkFile:fileList){
            //创建一个读块文件的对象
            RandomAccessFile raf_read = new RandomAccessFile(chunkFile,"r");
            int len = -1;
            while((len = raf_read.read(b))!=-1){
                raf_write.write(b,0,len);
            }
            raf_read.close();
        }
        raf_write.close();
    }



}
