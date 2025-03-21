package com.github.fineke.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class FolderMD5Calculator {

    /**
     * 计算文件夹的 MD5 值
     *
     * @param folder 文件夹路径
     * @return 文件夹的 MD5 值
     */
    public static String calculateFolderMD5(File folder) {
        if (!folder.isDirectory()) {
            throw new IllegalArgumentException("提供的路径不是一个文件夹: " + folder.getAbsolutePath());
        }


        try {
            // 获取文件夹下所有文件的 MD5 值
            List<String> fileMD5List = new ArrayList<>();
            collectFileMD5s(folder, fileMD5List);

            // 将所有文件的 MD5 值合并，计算整个文件夹的 MD5
            MessageDigest digest = MessageDigest.getInstance("MD5");
            for (String md5 : fileMD5List) {
                digest.update(md5.getBytes());
            }
            byte[] folderMD5Bytes = digest.digest();

            // 将字节数组转换为十六进制字符串
            return bytesToHex(folderMD5Bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 算法不可用", e);
        }
    }

    /**
     * 递归收集文件夹下所有文件的 MD5 值
     *
     * @param folder      文件夹
     * @param fileMD5List 存储文件 MD5 值的列表
     */
    private static void collectFileMD5s(File folder, List<String> fileMD5List) {
        File[] files = folder.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                // 如果是文件夹，递归处理
                if (file.getName().equals("target")) {
                    continue;
                }
                collectFileMD5s(file, fileMD5List);
            } else {
                // 如果是文件，计算 MD5 并添加到列表
                String fileMD5 = calculateFileMD5(file);
                fileMD5List.add(fileMD5);
            }
        }
    }

    /**
     * 计算单个文件的 MD5 值
     *
     * @param file 文件
     * @return 文件的 MD5 值
     */
    private static String calculateFileMD5(File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
            byte[] md5Bytes = digest.digest();
            return bytesToHex(md5Bytes);
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new RuntimeException("计算文件 MD5 失败: " + file.getAbsolutePath(), e);
        }
    }

    /**
     * 将字节数组转换为十六进制字符串
     *
     * @param bytes 字节数组
     * @return 十六进制字符串
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public static void main(String[] args) {
        // 示例：计算文件夹的 MD5
        File folder = new File("path/to/your/folder");
        String folderMD5 = calculateFolderMD5(folder);
        System.out.println("文件夹的 MD5 值: " + folderMD5);
    }
}