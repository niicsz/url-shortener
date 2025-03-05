package com.example.urlshortener.service;

import com.example.urlshortener.model.Url;
import com.example.urlshortener.repository.UrlRepository;
import org.springframework.stereotype.Service;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class UrlService {

    private final UrlRepository urlRepository;

    private static final Pattern URL_PATTERN = Pattern.compile(
        "^(https?://)?([\\da-z\\.-]+)\\.([a-z\\.]{2,6})[/\\w \\.-]*/?$"
    );

    public UrlService(UrlRepository urlRepository) {
        this.urlRepository = urlRepository;
    }

    public String generateShortUrl(String originalUrl) {
        if (!isValidUrl(originalUrl)) {
            throw new IllegalArgumentException("Invalid URL format");
        }

        originalUrl = ensureProtocol(originalUrl);

        Optional<Url> existingUrl = urlRepository.findByOriginalUrl(originalUrl);
        if (existingUrl.isPresent()) {
            return existingUrl.get().getShortUrl();
        }

        String shortUrl = generateUniqueShortUrl(originalUrl);

        Url url = new Url();
        url.setOriginalUrl(originalUrl);
        url.setShortUrl(shortUrl);

        urlRepository.save(url);
        return shortUrl;
    }

    public String getOriginalUrl(String shortUrl) {
        return urlRepository.findByShortUrl(shortUrl)
                .map(Url::getOriginalUrl)
                .orElse(null);
    }

    private boolean isValidUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }
        
        try {
            new URL(ensureProtocol(url));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String ensureProtocol(String url) {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            return "https://" + url;
        }
        return url;
    }

    private String generateUniqueShortUrl(String originalUrl) {
        String baseShortUrl = encodeUrl(originalUrl);
        
        String shortUrl = baseShortUrl;
        int counter = 1;
        while (urlRepository.findByShortUrl(shortUrl).isPresent()) {
            shortUrl = baseShortUrl + counter++;
            
            if (counter > 100) {
                throw new RuntimeException("Unable to generate unique short URL");
            }
        }
        
        return shortUrl;
    }

    private String encodeUrl(String originalUrl) {
        return Base64.getUrlEncoder()
                .encodeToString(originalUrl.getBytes(StandardCharsets.UTF_8))
                .replaceAll("[^a-zA-Z0-9]", "") // Remove special characters
                .substring(0, Math.min(6, originalUrl.length()));
    }
}
