package com.dev.vocalab.wordbooks;

import com.dev.vocalab.files.FileHandler;
import com.dev.vocalab.files.FilesEntity;
import com.dev.vocalab.files.FilesRepository;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class WordBookService {

    private final WordBookRepository wordBookRepository;
    private final FilesRepository filesRepository;

    // [ 사용자 단어장 목록 조회 ]
    public List<WordBooksEntity> getWordBooks(String userId) {
        System.out.println("getWordBooks");
        return wordBookRepository.findByUserId(userId);
    } //getWordBooks

    // [ 사용자 단어장/단어장의 csv file 삭제 ]
    @Transactional
    public String deleteWordbooks(int wordBookId, String userId) {
        System.out.println("deleteWordbooks");

            if(wordBookRepository.existsById(wordBookId)) {
                wordBookRepository.deleteByWordBookId(wordBookId);
                List<FilesEntity> filesEntityList = filesRepository.findByTableIdAndCategoryAndUserId(wordBookId, "WORD", userId);
                System.out.println("filesEntityList: " + filesEntityList);
                System.out.println("filesEntityList size: " + filesEntityList.size());
                if (filesEntityList.size() > 0) {
                    filesRepository.deleteByTableIdAndCategoryAndUserId(wordBookId,"WORD", userId);
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
        WordBooksEntity wordBook =  wordBookRepository.findByWordBookId(wordBookId);
        String wordBookTitle = wordBook.getWordBookTitle();
        System.out.println(userId + ", " + wordBookId);
        FilesEntity csvfiles = filesRepository.findByUserIdAndTableIdAndCategory(userId, wordBookId, "WORD");
        System.out.println("wordBookTitle: " + wordBookTitle);
        System.out.println("csvfiles: " + csvfiles);

        String saveDir = null;
        String originalFileName = null;
        try {
            String uploadDir = ResourceUtils.getFile("classpath:static/uploads/wordBooks/").toPath().toString();
            saveDir = uploadDir + File.separator + userId;
            originalFileName = wordBookTitle + ".csv";
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
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
                    writer.append(words.get(i)) // 단어 추가
                            .append(",")         // 쉼표로 구분
                            .append(meanings.get(i)) // 뜻 추가
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
            csvfiles.setCategory("WORD");
            csvfiles.setFilePath(dbFilePath);
            csvfiles.setFileType("FILE");
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
                }  catch (IOException e) {
                    throw new RuntimeException(e);
                }

            } //if

            // 중복 데이터 방지
            for (int i = 0; i < words.size(); i++) {
                String newWord = words.get(i);
                String newMeaning = meanings.get(i);
                boolean isDuplicate = wordList.stream()
                        .anyMatch(row -> row[0].equals(newWord) && row[1].equals(newMeaning));
                if (!isDuplicate) {
                    wordList.add(new String[]{newWord, newMeaning});
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

} // service class