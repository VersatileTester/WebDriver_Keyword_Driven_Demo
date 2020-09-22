package com.versatiletester.util.file;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@Component
public class FileUtils {
    private static final Logger log = Logger.getLogger(FileUtils.class);
    public static final String OUTPUT_FILE_DIRECTORY = "./target/reports/";
    public static final String CSV_SEPARATOR = ",";

    public synchronized void writeToFile(String fileName, String content){
        try {
            Files.write(Paths.get(OUTPUT_FILE_DIRECTORY + fileName),
                    content.getBytes(),
                    Files.exists(Paths.get(OUTPUT_FILE_DIRECTORY + fileName)) ? StandardOpenOption.APPEND : StandardOpenOption.CREATE);
            //Files.write(Paths.get(OUTPUT_FILE_DIRECTORY + fileName), content.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException ex) { log.error("Attempt to write to file failed, cause was: " + ex.getCause()); }
    }

    public synchronized void writeTestDataToCSV(String fileName, String certificate, String applicationID, String caseID){
        if(!Files.exists(Paths.get(OUTPUT_FILE_DIRECTORY + fileName + ".csv"))){
            writeToFile(fileName,"Certificate,ApplicationID,CaseID\n");
        }
        StringBuilder csvRow = new StringBuilder();
        csvRow.append(certificate);
        csvRow.append(CSV_SEPARATOR);
        csvRow.append(applicationID);
        csvRow.append(CSV_SEPARATOR);
        csvRow.append(caseID);
        csvRow.append(CSV_SEPARATOR);
        csvRow.append("\n");

        writeToFile(fileName,csvRow.toString());
    }

    public static String convertPathToUniversal(String path){
        return path.replaceAll("\\|\\/", File.separator);
    }
}
