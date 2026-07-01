package com.ukayukay.backend.config;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.ukayukay.backend.entity.Product;
import com.ukayukay.backend.repository.ProductRepository;

@Component
public class ImagePathUpdater implements CommandLineRunner {

    private final ProductRepository productRepository;

    public ImagePathUpdater(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        File imagesDir = new File("src/main/resources/static/images");
        if (!imagesDir.exists() || !imagesDir.isDirectory()) {
            return;
        }

        String[] files = imagesDir.list((d, n) -> n.toLowerCase().matches(".*\\.(png|jpg|jpeg|gif)$"));
        if (files == null || files.length == 0) return;

        List<String> filenames = Arrays.stream(files).sorted().collect(Collectors.toList());

        for (Product p : productRepository.findAll()) {
            String current = p.getImagePath();

            // Preserve any existing explicit image path (especially uploaded files).
            // This runner should only backfill missing image paths.
            if (current != null && !current.trim().isEmpty()) {
                continue;
            }

            String itemName = p.getItemName() == null ? "" : p.getItemName().toLowerCase();
            String category = p.getCategory() == null ? "" : p.getCategory().toLowerCase();

            String chosen = null;

            // Try to match tokens from the item name to filenames
            for (String fname : filenames) {
                String n = fname.toLowerCase();
                for (String token : itemName.split("\\s+")) {
                    token = token.replaceAll("[^a-z0-9]", "");
                    if (token.length() < 3) continue;
                    if (n.contains(token)) { chosen = fname; break; }
                }
                if (chosen != null) break;
            }

            // If not matched, try category
            if (chosen == null && category.length() > 0) {
                for (String fname : filenames) {
                    if (fname.toLowerCase().contains(category)) { chosen = fname; break; }
                }
            }

            // Fallback: pick by product id modulo available images
            if (chosen == null) {
                int idx = 0;
                if (p.getProductId() != null && filenames.size() > 0) {
                    idx = Math.floorMod(p.getProductId(), filenames.size());
                }
                chosen = filenames.get(idx);
            }

            String newPath = "/images/" + chosen;
            p.setImagePath(newPath);
            productRepository.save(p);
        }
    }
}
