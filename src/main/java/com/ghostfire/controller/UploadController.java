package com.ghostfire.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.hutool.core.util.IdUtil;
import com.ghostfire.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.MemoryCacheImageInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/upload")
@SaCheckLogin
public class UploadController {

    @Value("${app.upload-path}")
    private String uploadPath;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("png", "jpg", "jpeg", "gif");
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    private static final int MAX_IMAGE_DIMENSION = 10000;
    private static final long MAX_IMAGE_PIXELS = 40_000_000L;

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
        String ext = getExtension(originalName);
        if (ext.isBlank() || !ALLOWED_EXTENSIONS.contains(ext)) {
            return Result.fail("仅支持 png/jpg/jpeg/gif 格式");
        }
        String imageError = validateImage(file, ext);
        if (imageError != null) {
            return Result.fail(imageError);
        }

        Path baseDir = Paths.get(uploadPath).toAbsolutePath().normalize();
        Path dir = baseDir.resolve(subDir).normalize();
        if (!dir.startsWith(baseDir)) {
            return Result.fail("上传目录不合法");
        }
        Files.createDirectories(dir);

        String newName = IdUtil.fastSimpleUUID() + "." + ext;
        Path dest = dir.resolve(newName).normalize();
        if (!dest.startsWith(dir)) {
            return Result.fail("文件名不合法");
        }
        Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);

        Map<String, String> result = new HashMap<>();
        result.put("url", "/uploads/" + subDir + "/" + newName);
        return Result.ok(result);
    }

    private String getExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }

    private String validateImage(MultipartFile file, String ext) throws IOException {
        try (MemoryCacheImageInputStream imageInput = new MemoryCacheImageInputStream(file.getInputStream())) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(imageInput);
            if (!readers.hasNext()) {
                return "文件内容不是有效图片";
            }

            ImageReader reader = readers.next();
            try {
                reader.setInput(imageInput, true, true);
                String format = reader.getFormatName().toLowerCase(Locale.ROOT);
                if (!matchesExtension(format, ext)) {
                    return "图片格式与扩展名不一致";
                }
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                if (width <= 0 || height <= 0
                        || width > MAX_IMAGE_DIMENSION
                        || height > MAX_IMAGE_DIMENSION
                        || (long) width * height > MAX_IMAGE_PIXELS) {
                    return "图片尺寸过大";
                }
                return null;
            } finally {
                reader.dispose();
            }
        }
    }

    private boolean matchesExtension(String format, String ext) {
        return switch (ext) {
            case "jpg", "jpeg" -> "jpeg".equals(format) || "jpg".equals(format);
            default -> ext.equals(format);
        };
    }
}
