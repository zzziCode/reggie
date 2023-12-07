package com.zzzi.reggie.controller;

import com.zzzi.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

/**
 * @author zzzi
 * @date 2023/12/3 9:49
 * 通用的表现层代码
 * 主要用来进行文件的上传和下载
 * <p>
 * 上传是将图片保存到配置文件中指定的目录并且返回一个文件的名称
 * 下载时根据文件的名称和配置文件中的路径找到文件，并且通过流的方式将其传递给前端
 *
 * 就是文件流的使用
 */
@RestController
@RequestMapping("/common")
@Slf4j
public class CommonController {

    @Value("${reggie.path}")
    private String basePath;

    @PostMapping("/upload")
    //文件上传的功能，将选择的图片保存到指定位置，并且将指定位置的路径返回给前端
    //前端拿到路径之后，根据路径展示图片即可
    public R<String> upload(MultipartFile file) {
        log.info("接收到的文件：{}", file.toString());

        String originalFilename = file.getOriginalFilename();
        //拿到文件的后缀名
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        //生成一个随机的文件名称，并且加上后缀
        String fileName = UUID.randomUUID().toString() + suffix;


        File dir = new File(basePath);
        //如果配置的文件夹不存在，那么就创建
        if (!dir.exists()) {
            dir.mkdirs();
        }
        try {
            file.transferTo(new File(basePath + fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        //将当前文件名保存到数据库中
        //保存绝对路径
        return R.success(fileName);
    }

    //实现文件下载的功能，根据提供的路径将图片展示给浏览器
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response) {
        try (
                //根据路径读取到图片
                //接收到的就是图片的绝对路径
                FileInputStream fis = new FileInputStream(basePath + name);
                //将读取到的图片传递给前端
                ServletOutputStream outputStream = response.getOutputStream();
        ) {

            byte[] bytes = new byte[1024];
            int len = 0;

            //设置给前端相应的文件的类型
            response.setContentType("image/jpeg");
            //只要当前文件没有读完，就一直读
            while ((len = fis.read(bytes)) != -1) {
                //读多长些多长
                outputStream.write(bytes, 0, len);
                outputStream.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
