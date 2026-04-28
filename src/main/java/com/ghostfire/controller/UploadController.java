package com.ghostfire.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.ghostfire.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    @Value("${app.upload-path}")
    private String uploadPath;

    @PostMapping("/image")
    public Result<?> image(@RequestParam("file") MultipartFile file) throws IOException {
        String originalName = file.getOriginalFilename();
        String ext = FileUtil.extName(originalName);
        String newName = IdUtil.fastSimpleUUID() + "." + ext;
        String dateDir = cn.hutool.core.date.DateUtil.today();
        File dir = new File(uploadPath + "/" + dateDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File dest = new File(dir, newName);
        file.transferTo(dest);
        Map<String, String> result = new HashMap<>();
        result.put("url", "/uploads/" + dateDir + "/" + newName);
        return Result.ok(result);
    }
}
