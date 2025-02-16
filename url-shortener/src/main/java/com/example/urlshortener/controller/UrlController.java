package com.example.urlshortener.controller;

import com.example.urlshortener.service.UrlService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/url")
public class UrlController {

    private final UrlService urlService;

    public UrlController(UrlService urlService) {
        this.urlService = urlService;
    }

    @PostMapping("/shorten")
    public ResponseEntity<String> shortenUrl(@RequestParam String url) {
        String shortUrl = urlService.generateShortUrl(url);
        return ResponseEntity.ok("https://encurtaurl.up.railway.app/api/url/" + shortUrl);
    }

    @GetMapping("/{shortUrl}")
    public ResponseEntity<Void> redirect(@PathVariable String shortUrl) {
        String originalUrl = urlService.getOriginalUrl(shortUrl);
        if (originalUrl != null) {
            return ResponseEntity.status(302).header("Location", originalUrl).build();
        }
        return ResponseEntity.notFound().build();
    }
}
