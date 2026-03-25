package com.mdm.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;

@Controller
public class InstallController {

    private static final String APK_PATH = "static/apk/mdm-device-manager.apk";

    @GetMapping("/install")
    public String installPage(HttpServletRequest request, Model model) throws IOException {
        String baseUrl = request.getScheme() + "://" + request.getServerName()
                + ":" + request.getServerPort();

        Resource apk = new ClassPathResource(APK_PATH);
        long sizeBytes = apk.contentLength();
        String fileSize = String.format("%.1f MB", sizeBytes / (1024.0 * 1024.0));

        model.addAttribute("appVersion", "1.0");
        model.addAttribute("fileSize", fileSize);
        model.addAttribute("downloadUrl", baseUrl + "/install/download");
        model.addAttribute("installUrl", baseUrl + "/install");

        return "install";
    }

    @GetMapping("/install/download")
    public ResponseEntity<Resource> downloadApk() {
        Resource apk = new ClassPathResource(APK_PATH);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.android.package-archive"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"mdm-device-manager.apk\"")
                .body(apk);
    }
}
