package com.dev.vocalab.wordbooks;

import com.dev.vocalab.files.FilesEntity;
import com.dev.vocalab.files.FilesRepository;
import com.opencsv.CSVReader;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WordBooksService {


    private final FilesRepository filesRepository;
    // Inject the static file path from application.properties
    @Value("${spring.web.resources.static-locations}")
    private String staticLocations;

    public List<String[]> readWordBook(Integer wordBookId) throws Exception {

        Optional<FilesEntity> fileEntityOpt = filesRepository.findByTableIdAndCategory(wordBookId, FilesEntity.Category.WORD)
                .stream()
                .findFirst();
        System.out.println("fileEntityOpt: " +fileEntityOpt);

        if (fileEntityOpt.isEmpty()) {
            throw new Exception("No file found for the given wordBookId.");
        }

        // Extract the filePath from the retrieved FilesEntity
        String relativeFilePath = fileEntityOpt.get().getFilePath();
        System.out.println("Relative file path: " + relativeFilePath);

        // Ensure the file path is absolute
        String baseStaticPath = staticLocations.split(",")[1].replace("file:", "");
        String absoluteFilePath = Paths.get(baseStaticPath, relativeFilePath).toString();
        System.out.println("Absolute file path: " + absoluteFilePath);

        // Check if file exists
        if (!Files.exists(Paths.get(absoluteFilePath))) {
            throw new Exception("The file does not exist: " + absoluteFilePath);
        }

        // Read the CSV file
        List<String[]> wordList = new ArrayList<>();
        try (CSVReader csvReader = new CSVReader(new FileReader(absoluteFilePath))) {
            String[] line;
            while ((line = csvReader.readNext()) != null) {
                wordList.add(line);
            }
        } catch (Exception e) {
            throw new Exception("Failed to read the CSV file: " + absoluteFilePath, e);
        }
        return wordList;
    }
}
