package com.ghostfire.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.dev33.satoken.annotation.SaCheckLogin;
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
@SaCheckLogin
public class UploadController {

    @Value("${app.upload-path}")
    private String uploadPath;

    private static final java.util.Set<String> ALLOWED_EXTENSIONS = java.util.Set.of("png", "jpg", "jpeg", "gif");
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    /** 上传头像 */
    @PostMapping("/avatar")
    public Result<?> avatar(@RequestParam("file") MultipartFile file) throws IOException {
        return upload(file, "profile-pic");
    }

    /** 上传帖子图片 */
    @PostMapping("/post")
    public Result<?> post(@RequestParam("file") MultipartFile file) throws IOException {
        return upload(file, "post");
    }

    /** 上传吹牛图片 */
    @PostMapping("/boast")
    public Result<?> boast(@RequestParam("file") MultipartFile file) throws IOException {
        return upload(file, "boast");
    }

    private Result<?> upload(MultipartFile file, String subDir) throws IOException {
        if (file.isEmpty()) {
            return Result.fail("文件不能为空");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            return Result.fail("文件大小不能超过10MB");
        }
        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) {
            return Result.fail("文件名不能为空");
        }
        String ext = FileUtil.extName(originalName);
        if (ext == null || !ALLOWED_EXTENSIONS.contains(ext.toLowerCase())) {
            return Result.fail("仅支持 png/jpg/jpeg/gif 格式");
        }
        String newName = IdUtil.fastSimpleUUID() + "." + ext;
        File dir = new File(uploadPath + "/" + subDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File dest = new File(dir, newName);
        file.transferTo(dest);
        Map<String, String> result = new HashMap<>();
        result.put("url", "/uploads/" + subDir + "/" + newName);
        return Result.ok(result);
    }
}
