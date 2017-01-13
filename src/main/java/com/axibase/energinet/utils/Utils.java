package com.axibase.energinet.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Utils {
    public static String fileAsString(String path) {
        try {
            File file = new File(path);
            return org.apache.commons.io.IOUtils.toString(new FileInputStream(file), "UTF-8");
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static void fileRename(String oldName, String newName) {
        File oldfile = new File(oldName);
        File newfile = new File(newName);
        try {
            org.apache.commons.io.FileUtils.copyFile(oldfile, newfile);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        if (!oldfile.delete()) {
            throw new IllegalStateException(String.format("Can't delete temporary file %s", new Object[]{oldfile
                    .getAbsolutePath()}));
        }
    }


    public static List<String> fileAsStringList(String path) {
        File file = new File(path);
        List<String> resultList = new ArrayList<String>();
        FileInputStream fis = null;
        BufferedReader reader = null;
        try {
            fis = new FileInputStream(file);
            reader = new BufferedReader(new java.io.InputStreamReader(fis, "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                resultList.add(line);
            }
            reader.close();
            fis.close();


            return resultList;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
            }
        }
    }
}