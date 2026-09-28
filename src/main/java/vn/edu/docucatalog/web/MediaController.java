package vn.edu.docucatalog.web;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.docucatalog.service.AvatarStorageService;

@RestController
@RequestMapping("/media/avatars")
@RequiredArgsConstructor
public class MediaController {
    private final AvatarStorageService avatarStorageService;

    @GetMapping(value = "/{fileName:.+}", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<Resource> avatar(@PathVariable String fileName) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noCache())
                .contentType(MediaType.IMAGE_JPEG)
                .body(avatarStorageService.load(fileName));
    }
}
