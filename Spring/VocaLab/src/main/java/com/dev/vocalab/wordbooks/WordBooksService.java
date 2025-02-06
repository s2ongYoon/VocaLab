package com.dev.vocalab.wordbooks;

import com.dev.vocalab.files.FileHandler;
import com.dev.vocalab.files.FilesEntity;
import com.dev.vocalab.files.FilesRepository;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
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
    private final WordBooksRepository wordBooksRepository;
    // Inject the static file path from application.properties
    @Value("${spring.web.resources.static-locations}")
    private String staticLocations;

    public List<String[]> readWordBook(Integer wordBookId) throws Exception {

        Optional<FilesEntity> fileEntityOpt = filesRepository.findByTableIdAndCategory(wordBookId, FilesEntity.Category.WORD).stream().findFirst();
        System.out.println("fileEntityOpt: " + fileEntityOpt);

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

    public void deleteWord(Integer wordBookId, List<String> deleteWordList) throws Exception {
        Optional<FilesEntity> fileEntityOpt = filesRepository.findByTableIdAndCategory(wordBookId, FilesEntity.Category.WORD).stream().findFirst();
        System.out.println("fileEntityOpt: " + fileEntityOpt);

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

        // Read the CSV file and filter out words to delete
        List<String[]> remainingWords = new ArrayList<>();
        try (CSVReader csvReader = new CSVReader(new FileReader(absoluteFilePath))) {
            String[] line;
            while ((line = csvReader.readNext()) != null) {
                // Skip the line if the word (first column) is in the delete list
                if (line.length > 0 && !deleteWordList.contains(line[0])) {
                    remainingWords.add(line);
                }
            }
        } catch (Exception e) {
            throw new Exception("Failed to read the CSV file: " + absoluteFilePath, e);
        }

        // Write the filtered content back to the file
        try (CSVWriter csvWriter = new CSVWriter(new FileWriter(absoluteFilePath))) {
            csvWriter.writeAll(remainingWords);
        } catch (IOException e) {
            throw new Exception("Failed to write to the CSV file: " + absoluteFilePath, e);
        }
    }

    public void changeStatusOfBookmark(Integer wordBookId) {
        WordBooksEntity wordBooks = wordBooksRepository.findByWordBookId(wordBookId);
        System.out.println("Before set: " + wordBooks.getBookmark());
        wordBooks.setBookmark(!wordBooks.getBookmark());
        System.out.println("After set: " + wordBooks.getBookmark());
        wordBooksRepository.saveAndFlush(wordBooks);
    }

    public WordBooksEntity loadWordBookStatus(Integer wordBookId) {
        return wordBooksRepository.findByWordBookId(wordBookId);
    }

    public void updateWordBookTitle(Integer wordBookId, String newTitle) {
        WordBooksEntity wordBook = wordBooksRepository.findByWordBookId(wordBookId);
        if (wordBook == null) {
            throw new IllegalArgumentException("단어장을 찾을 수 없습니다.");
        }
        // 제목이 비어있는지 확인
        if (newTitle == null || newTitle.trim().isEmpty()) {
            throw new IllegalArgumentException("제목은 비워둘 수 없습니다.");
        }
        // 제목 길이 제한 (예: 100자)
        if (newTitle.length() > 100) {
            throw new IllegalArgumentException("제목이 너무 깁니다. (최대 100자)");
        }
        wordBook.setWordBookTitle(newTitle.trim());
        wordBooksRepository.saveAndFlush(wordBook);
    }

    // [ 사용자 단어장 목록 조회 ]
    public List<WordBooksEntity> getWordBooks(String userId) {
        System.out.println("getWordBooks");
        return wordBooksRepository.findByUserId(userId);
    } //getWordBooks

    // [ 사용자 단어장/단어장의 csv file 삭제 ]
    @Transactional
    public String deleteWordbooks(Integer wordBookId, String userId) {
        System.out.println("deleteWordbooks");

        if (wordBooksRepository.existsById(wordBookId)) {
            wordBooksRepository.deleteByWordBookId(wordBookId);
            List<FilesEntity> filesEntityList = filesRepository.findByTableIdAndCategoryAndUserId(wordBookId, FilesEntity.Category.WORD, userId);
            System.out.println("filesEntityList: " + filesEntityList);
            System.out.println("filesEntityList size: " + filesEntityList.size());
            if (filesEntityList.size() > 0) {
                filesRepository.deleteByTableIdAndCategoryAndUserId(wordBookId, FilesEntity.Category.WORD, userId);
                return "true";
            }
            return "true";
        }
        return "false";
    } //deleteWordbooks

    @Transactional
    public void addWords(String userId, int wordBookId, List<String> words, List<String> meanings) {
        System.out.println("wordBookService - addWords");

        // id로 이름 찾기
        WordBooksEntity wordBook = wordBooksRepository.findByWordBookId(wordBookId);
        String wordBookTitle = wordBook.getWordBookTitle();
        System.out.println(userId + ", " + wordBookId);
        FilesEntity csvfiles = filesRepository.findByUserIdAndTableIdAndCategory(userId, wordBookId, FilesEntity.Category.WORD);
        System.out.println("wordBookTitle: " + wordBookTitle);
        System.out.println("csvfiles: " + csvfiles);

        String saveDir = null;
        String originalFileName = null;

        String uploadDir = System.getProperty("user.dir") + "/src/main/resources/static/uploads/wordBooks/";
        saveDir = uploadDir + File.separator + userId;
        originalFileName = wordBookTitle + ".csv";

        String filePath = saveDir + File.separator + originalFileName;

        // csv 파일이 있으면 덮어쓰기 없으면 새로 저장
        // 단어장에 추가한 단어가 없음 (새로추가)
        if (csvfiles == null) {
            System.out.println("csvfiles is null - 추가한 단어가 없음 새로 추가함");
            if (words == null || meanings == null || words.size() != meanings.size()) {
                throw new IllegalArgumentException("단어와 뜻의 개수가 맞지 않거나 데이터가 비어있습니다.");
            }

            // filePath
            FileHandler.createDirectory(saveDir); // 디렉토리 생성

            // FileWriter를 사용해 CSV 파일 생성
            try (FileWriter writer = new FileWriter(saveDir + File.separator + originalFileName)) {
                for (int i = 0; i < words.size(); i++) {
                    String word = "\""+ words.get(i) + "\"";
                    String mean = meanings.get(i);
                    String[] means = mean.split("\\.");
                    if (means.length > 1) {
                        // "static/" 이후의 문자열을 가져오기
                        mean = "\""+ means[1].trim() + "\"";
                        System.out.println("뜻에서 품사 삭제" + mean);
                    } else {
                        System.out.println("품사가 포함되지 않았읍니다.");
                    }
                    System.out.println("새로운단어장에 추가하는 경우");
                    System.out.println("word" + word);
                    System.out.println("mean" + mean);

                    writer.append(word) // 단어 추가
                            .append(",")         // 쉼표로 구분
                            .append(mean) // 뜻 추가
                            .append("\n");       // 행 종료
                }
                System.out.println("CSV 파일이 생성되었습니다");
            } catch (IOException e) {
                throw new RuntimeException(e);
            } // try catch

            System.out.println("csv DB저장");
            // DB FILES저장
            String[] dir = saveDir.split("static/");
            String dbPath = "";
            if (dir.length > 1) {
                // "static/" 이후의 문자열을 가져오기
                dbPath = dir[1];
                System.out.println("추출된 문자열: " + dbPath);
            } else {
                System.out.println("경로에 'static/'이 포함되어 있지 않습니다.");
            }

            String dbFilePath = FileHandler.renameFile(dbPath, originalFileName);
            csvfiles = new FilesEntity();

            csvfiles.setUserId(userId);
            csvfiles.setCategory(FilesEntity.Category.WORD);
            csvfiles.setFilePath(dbFilePath);
            csvfiles.setFileType(FilesEntity.FileType.FILE);
            csvfiles.setTableId(wordBookId);
            System.out.println("files 테이블에 저장할 csv파일" + csvfiles);

            filesRepository.save(csvfiles);

        } else {
            // 단어장에 추가한 단어가 존재 ( 덮어쓰기 ) - 단 한단어장에 200 단어까지 가능
            if (words == null || meanings == null || words.size() != meanings.size()) {
                throw new IllegalArgumentException("단어와 뜻의 개수가 맞지 않거나 데이터가 비어있습니다.");
            }

            // 파일 경로 생성
            File file = new File(filePath);

            // 기존 데이터를 저장할 리스트
            List<String[]> wordList = new ArrayList<>();

            // 파일이 존재하면 데이터 읽기
            if (file.exists()) {
                try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
                    String[] line;
                    while ((line = reader.readNext()) != null) {
                        wordList.add(line);
                    }
                } catch (CsvValidationException e) {
                    throw new RuntimeException("CSV 파일 읽기 중 오류 발생: " + e.getMessage(), e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            } //if

            // 중복 데이터 방지
            for (int i = 0; i < words.size(); i++) {
                String word = words.get(i);
                String mean = meanings.get(i);
                System.out.println("word" + word);
                System.out.println("따옴표 추가전 : " + mean);
                String[] means = mean.split("\\.");
                String newMean;
                if (means.length > 1) {
                    // "static/" 이후의 문자열을 가져오기
                    newMean = means[1].trim();
                    System.out.println("뜻에서 품사 삭제" + newMean);
                } else {
                    newMean = "";
                    System.out.println("품사가 포함되지 않았읍니다.");
                }

                boolean isDuplicate = wordList.stream()
                        .anyMatch(row -> row[0].equals(word) && row[1].equals(newMean));
                if (!isDuplicate) {
                    wordList.add(new String[]{word, newMean});
                }
            } //for

            // 최대 200 단어 제한
            if (wordList.size() > 200) {
                throw new IllegalArgumentException("단어장은 최대 200개의 단어만 허용됩니다.");
            }

            // 데이터 저장
            try (CSVWriter writer = new CSVWriter(new FileWriter(filePath))) {
                for (String[] line : wordList) {
                    writer.writeNext(line);
                }
                System.out.println("CSV 파일이 업데이트되었습니다.");
            } catch (IOException e) {
                throw new RuntimeException("CSV 파일 쓰기 중 오류 발생", e);
            }

        } // if


    } // addWords
}
