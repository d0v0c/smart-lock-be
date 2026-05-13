package ie.tcd.smartlock.controller;

import ie.tcd.smartlock.config.SmartLockProperties;
import ie.tcd.smartlock.utils.BusinessException;
import ie.tcd.smartlock.utils.Result;
import ie.tcd.smartlock.utils.StatusCode;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * @author xylingying
 * @date 2025-03-26 0:42
 * @description: 返回更新所需的文件
 */
@RestController
@RequestMapping("/api/file")
public class FileController {
    private final String fileDir;

    public FileController(SmartLockProperties properties) {
        this.fileDir = properties.file().dir();
    }

    @Operation(summary = "上传固件文件", description = "返回文件名")
    @PostMapping
    public Result<String> uploadFile(@RequestParam("file") MultipartFile file) {
        // 将上传的文件复制到file.dir，如果文件已存在则替换
        try {
            String fileName = StringUtils.cleanPath(file.getOriginalFilename());
            Path path = Paths.get(fileDir).resolve(fileName).normalize();
            Files.createDirectories(path.getParent());
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            return Result.success("File uploaded successfully: " + fileName);
        } catch (IOException e) {
            throw new BusinessException(StatusCode.SYSTEM_ERROR, "Could not upload the file");
        }
    }

    @Operation(summary = "下载固件文件", description = "返回ESP32设备固件bin文件")
    @GetMapping("/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
        try {
            // 解析并规范化文件路径
            Path filePath = Paths.get(fileDir).resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                throw new FileNotFoundException("File not found " + fileName);
            }
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
