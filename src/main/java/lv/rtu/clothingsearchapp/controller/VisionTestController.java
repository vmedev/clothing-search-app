package lv.rtu.clothingsearchapp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lv.rtu.clothingsearchapp.service.AiVisionService;
import lv.rtu.clothingsearchapp.service.DetectedClothing;

@RestController
public class VisionTestController {

    private final AiVisionService aiVisionService;

    public VisionTestController(AiVisionService aiVisionService) {
        this.aiVisionService = aiVisionService;
    }

    @PostMapping("/test/vision")
    public ResponseEntity<DetectedClothing> test(@RequestParam("image") MultipartFile image) {
        return aiVisionService.analyze(image)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }
}