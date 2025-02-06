package com.dev.vocalab.compileRecord;

import com.dev.vocalab.files.FileHandler;
import com.dev.vocalab.files.FilesDTO;
import com.dev.vocalab.files.FilesEntity;
import com.dev.vocalab.files.FilesRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
// 생성자를 통한 의존성 투입
@RequiredArgsConstructor
public class CompileService {

    @PersistenceContext
    private EntityManager entityManager;

    private final CompileRecordRepository compileRecordRepository;
    private final FilesRepository filesRepository;
    private final FileHandler fileHandler;

    // [ compilePro ]
    @Transactional
    public List<Map<String, Object>> compileProService(CompileRecordEntity com, FilesEntity files,
                                                       List<MultipartFile> multipartFiles, String userId) {
        System.out.println("compileProService");
        System.out.println("com: " + com.getCompileId());
        try {
            List<FilesDTO> filesDto = new ArrayList<>();
            FilesDTO fileDto;
            // 이미 기록 되어 있다면 compileRecord, Files insert 생략
            if(com.getCompileId() == 0){
            // < 1. CompileRecord INSERT > - 단어 추출 기록= -------------
                com = compileRecordRepository.save(com);
                System.out.println("insert result com : " + com);

                // < 2 원본 파일이 있다면 처리 후 저장, 없다면 넘어가기 > - 원본 파일 저장 ------------
                    for (MultipartFile multipartFile : multipartFiles) {
                        System.out.println("for - file : " + multipartFile.getOriginalFilename());
                        System.out.println("for문");
                        // 1. spesifyFilePath 메서드 호출(메개변수 :  FilesDTO) => 파일 디렉토리 정하기
                        fileDto = new FilesDTO(com.getUserId(),Integer.toString(com.getCompileId())
                                ,multipartFile,"","","","","");
                        fileDto = FileHandler.spesifyFilePath(fileDto);

                        String saveDir = fileDto.getSaveDir();
                        String originalFileName = fileDto.getOriginalFileName();
                        System.out.println("originalFileName : " + originalFileName);
                        System.out.println("saveDirFinal : " + saveDir);

                        if (!originalFileName.isEmpty()) {
                            // 3-1. 디렉토리 생성 메서드(FileHandler)호출 디렉토리 생성됨
                            FileHandler.createDirectory(saveDir);
                            System.out.println("디렉토리 생성 메서드");

                            // 파일 권한 실행
                            File originalFile = new File(originalFileName);
                            // 읽기 권한 설정
                            boolean readable = originalFile.setReadable(true);
                            System.out.println("Readable: " + readable);

                            // 쓰기 권한 설정
                            boolean writable = originalFile.setWritable(true);
                            System.out.println("Writable: " + writable);

                            // 실행 권한 설정
                            boolean executable = originalFile.setExecutable(true);
                            System.out.println("Executable: " + executable);

                            // 3-2. 서버에 파일 저장
                            multipartFile.transferTo(new File(saveDir, originalFileName));

                            // 3-3. DB에 저장할 filePath데이터 생성(uuid와 경로를 더해 filePath에 저장)
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
                            System.out.println("dbFilePath : " + dbFilePath);

                            // 4. < Files INSERT 1 > - 원본 파일 DB 저장 -------------------------------
                            System.out.println("FilesInsert");

                            // files에 data넣기
                            files.setUserId(com.getUserId());
                            files.setCategory(FilesEntity.Category.COMPILE);
                            files.setFilePath(File.separator + dbFilePath);
                            files.setFileType(FilesEntity.FileType.valueOf(fileDto.getFileType()));
                            files.setUploadedAt(com.getCreatedAt());
                            // files TableId = compileId
                            files.setTableId(com.getCompileId());
                            System.out.println("원본 파일 저장 완료 files : " + files);
                            // insert

                            files = filesRepository.saveAndFlush(files);

                            // 커밋에 반영되지 않는 문제 해결
                            entityManager.flush();

                            System.out.println("DB 저장 결과 - files: " + files);

                            System.out.println("filesDto : " + filesDto);

                            // python에 전달하기 위한 filesDto list
                            filesDto.add(fileDto);
                            System.out.println("filesDto : " + filesDto);
                        }// if
                    } // for
                System.out.println("for문 나옴");

            } else {
               // compileId가 존재하면 해당기록 불러오기 (기록 저장 할 필요없고, 파일도 저장 할 필요 없음)
                System.out.println("mypage에서 기록으로 단어 추출하기");

                fileDto = new FilesDTO();
                fileDto.setUserId(userId);
                System.out.println("기록-fileDto : " + fileDto);
                com = compileRecordRepository.findByUserIdAndCompileId(userId, com.getCompileId());
                System.out.println("기록-com : " + com);
                fileDto.setCompileId(Integer.toString(com.getCompileId()));

                List<FilesEntity> filesList = filesRepository.findByTableIdAndCategoryAndUserId(com.getCompileId(), FilesEntity.Category.COMPILE, userId);
                System.out.println("기록-filesList : " + filesList);
                for (FilesEntity file :filesList){
                    System.out.println("기록으로 for문");
                    String Dir = file.getFilePath();
                    String saveDir = "";
                    if(file.getFilePath().contains("img")) {
                        saveDir = Dir.split("img/")[0] + "img/";
                        System.out.println("img 폴더 saveDir : " + saveDir);
                    } else {
                        saveDir = Dir.split("doc/")[0] + "doc/";
                        System.out.println("doc 폴더 saveDir : " + saveDir);
                    }

                    System.out.println("기록-saveDir : " + Dir);
                    String originalFileName = Dir.split("_")[1];
                    System.out.println("기록에서 단어추출 중 - files데이터 uuid뺌 : " + originalFileName);
                    String fileExtension = originalFileName.split("\\.")[1];
                    fileDto = new FilesDTO(saveDir, originalFileName, fileExtension, "");
                    filesDto.add(fileDto);
                }
                System.out.println("기록-ilesDto : " + filesDto);
            } // if(complieId)
//             < 3. python 처리 >
            System.out.println("comSouce : " + com.getSource());
            List<Map<String, Object>> compileWordsJson = new ArrayList<>();
            compileWordsJson = FileHandler.AICompileWords(com.getSource(), filesDto);
            System.out.println("compileWordsJson : " + compileWordsJson);

//            compileWordsJson데이터를 api로 뿌리기

//             < 4. Files INSERT 2 > - json 데이터를 csv 파일 저장
            // 4-1. 파일 이름과 서버 저장 경로 설정, 경로 생성
            fileDto = new FilesDTO(com.getUserId(),Integer.toString(com.getCompileId()),
                    "compileResult_" + com.getCompileId() + ".csv");
            System.out.println("compileResult_fileDto : " + fileDto);
            fileDto = FileHandler.spesifyFilePath(fileDto);

            String saveDir = fileDto.getSaveDir();
            String saveCsv = saveDir + File.separator + fileDto.getOriginalFileName();
            System.out.println("compileResult_saveDirFinal : " + saveDir);
            FileHandler.createDirectory(saveDir); //폴더생성
//
            List<Map<String, Object>> wordList = FileHandler.saveResultCSV(compileWordsJson, saveCsv); // csv파일로 변환 후 저장
//
//            String[] dir = saveDir.split("static/");
//            String dbPath = "";
//            if (dir.length > 1) {
//                // "static/" 이후의 문자열을 가져오기
//                dbPath = dir[1];
//                System.out.println("추출된 문자열: " + dbPath);
//            } else {
//                System.out.println("경로에 'static/'이 포함되어 있지 않습니다.");
//            }
//
//            String dbFilePath = FileHandler.renameFile(dbPath, fileDto.getOriginalFileName()); // 서버 파일 저장
//            int tableId = com.getCompileId();
//
//            files.setUserId(com.getUserId());
//            files.setCategory(FilesEntity.Category.COMPILERESULT);
//            files.setFilePath(dbFilePath);
//            files.setFileType(FilesEntity.FileType.FILE);
//            files.setUploadedAt(com.getCreatedAt());
//            files.setTableId(tableId);
//            files = filesRepository.save(files);
//            System.out.println("csv 저장 완료 files : " + files);

            return wordList; // python에서 받아온 json데이터
        } catch (Exception e) {
            throw new RuntimeException(e);
        } // try catch
    } //processCompileRecord



    // [ 단어 추출 기록 중 원본 파일만 삭제됨 ] : 미완
    @Transactional
    @Scheduled(cron = "0 0 12 * * *") // 오후 12시에 메서드 동작
    public void removeOriginalFiles() {
//        try {
            System.out.println("removeCompileRecords");
            // 파일 서버 디렉토리 찾아서 삭제
            // 디렉토리 경로 찾아오기
//            List<CompileFilesEntity> filelist = filesRepository.fineBYUploadedAtAndCategory(LocalDateTime.now().minusDays(7), "COMPILE");

//            for(CompileFilesEntity file : filelist) {
//
//                // db의 파일경로
//                String dbFilePath = file.getFilePath();
//                System.out.println("dbFilePath : " + dbFilePath);
//                // comfileId기준으로 삭제
//                String comfileId = Integer.toString(file.getTableId());
//
//                int idx = dbFilePath.indexOf(comfileId);
//
//                // 삭제할 진짜 경로
//                String dir = dbFilePath.split(idx + 1);
//
//                File deleteDir = new File(dir);
//
//                // 삭제할 디렉토리 안 원본파일 삭제
//                File[] deleteDirList = deleteDir.listFiles();
//                for (File deleteDirFile : deleteDirList) {
//                    deleteDirFile.delete();
//                }
//                // 디렉토리 삭제
//                deleteDir.delete();


                //생성일기준 7일 지난 원본 파일 삭제
                // [ Files Delete ]
//                filesRepository.deleteByFileId(file.getFileId());
//            }
//
//
//        } catch (RuntimeException e) {
//            throw new RuntimeException(e);
//        }
    }



} //CompileService