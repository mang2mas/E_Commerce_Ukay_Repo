package com.ukayukay.backend.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Resolve to an absolute path: relative "file:" locations break Spring's
        // resource-containment check (it needs an absolute URI), which made every
        // request under /images/uploaded/** fail with a 500, regardless of whether
        // the file existed — so uploaded product photos never actually rendered.
        Path uploadDir = Paths.get("uploads", "product-images").toAbsolutePath().normalize();
        if (!Files.exists(uploadDir)) {
            try {
                Files.createDirectories(uploadDir);
            } catch (Exception ignored) {
                // Directory is also created lazily on first upload.
            }
        }

        registry.addResourceHandler("/images/uploaded/**")
                .addResourceLocations(uploadDir.toUri().toString())
                .setCacheControl(CacheControl.noCache());
    }
}
