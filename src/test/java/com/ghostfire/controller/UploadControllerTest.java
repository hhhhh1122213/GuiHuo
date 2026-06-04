package com.ghostfire.controller;

import com.ghostfire.common.Result;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UploadControllerTest {

    @TempDir
    Path uploadPath;

    @Test
    void uploadRejectsFileWithFakeImageExtension() throws Exception {
        UploadController controller = newController();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "payload.png",
                "image/png",
                "<script>alert(1)</script>".getBytes());

        Result<?> result = controller.avatar(file);

        assertEquals(500, result.getCode());
        assertEquals("文件内容不是有效图片", result.getMsg());
    }

    @Test
    void uploadAcceptsValidPngImage() throws Exception {
        UploadController controller = newController();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.PNG",
                "image/png",
                pngBytes());

        Result<?> result = controller.avatar(file);

        assertEquals(200, result.getCode());
        @SuppressWarnings("unchecked")
        Map<String, String> data = (Map<String, String>) result.getData();
        String url = data.get("url");
        assertTrue(url.startsWith("/uploads/profile-pic/"));
        assertTrue(url.endsWith(".png"));
        assertEquals(1, Files.list(uploadPath.resolve("profile-pic")).count());
    }

    private UploadController newController() {
        UploadController controller = new UploadController();
        ReflectionTestUtils.setField(controller, "uploadPath", uploadPath.toString());
        return controller;
    }

    private byte[] pngBytes() throws Exception {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "png", out);
        return out.toByteArray();
    }
}
