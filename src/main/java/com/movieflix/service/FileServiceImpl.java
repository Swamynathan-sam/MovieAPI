package com.movieflix.service;

import com.movieflix.exceptions.FileExistsException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileServiceImpl implements FileService{
    @Override
    public String uploadFile(String path, MultipartFile file) throws IOException {

        //get name of the file
        String fileName = file.getOriginalFilename();

        //to  get the file path
        String filePath = path + File.separator + fileName;

        //create file object
        File f= new File(path);
        if(!f.exists()){
            f.mkdir();
        }
        if (Files.exists(Paths.get(filePath))) {
            throw new FileExistsException("File already exists! Please enter another file name!");
        }
        Files.copy(file.getInputStream(), Paths.get(filePath));
        return fileName;

        // Copy the file or upload file to the path
//        Files.copy(file.getInputStream(), Paths.get(filePath), StandardCopyOption.REPLACE_EXISTING);


    }

    @Override
    public InputStream getResourceFile(String path, String fileName) throws FileNotFoundException {

        String filePath = path + File.separator + fileName;
        return new FileInputStream(filePath);
    }
}
