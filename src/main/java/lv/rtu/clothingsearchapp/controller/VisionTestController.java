package lv.rtu.clothingsearchapp.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lv.rtu.clothingsearchapp.model.Product;
import lv.rtu.clothingsearchapp.service.AiVisionService;
import lv.rtu.clothingsearchapp.service.DetectedClothing;
import lv.rtu.clothingsearchapp.service.SimilarProductService;

@RestController
public class VisionTestController {

    private final AiVisionService aiVisionService;
    private final SimilarProductService similarProductService;

    public VisionTestController(AiVisionService aiVisionService,
                                SimilarProductService similarProductService) {
        this.aiVisionService = aiVisionService;
        this.similarProductService = similarProductService;
    }

    @PostMapping("/test/vision")
    public ResponseEntity<Result> test(@RequestParam("image") MultipartFile image) {
        return aiVisionService.analyze(image)
                .map(d -> ResponseEntity.ok(new Result(d, similarProductService.findSimilar(d))))
                .orElse(ResponseEntity.noContent().build());
    }

    public record Result(DetectedClothing detected, List<Product> products) {
    }
}
