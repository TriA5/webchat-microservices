package user_service.example.user_service.service.util;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Path;
import java.util.Base64;


public class Base64ToMultipartFileConverter {
    public static MultipartFile convert(String base64String) {
        try {
            String base64Content = base64String;

            // Loại bỏ tiền tố Data URI nếu có
            if (base64String.startsWith("data:")) {
                base64Content = base64String.split(",")[1];
            }

            // Loại bỏ khoảng trắng
            String cleanedBase64 = base64Content.replaceAll("\\s", "");

            byte[] decodedBytes = Base64.getDecoder().decode(cleanedBase64);

            // Tạo đối tượng MultipartFile từ mảng byte
            MultipartFile multipartFile = new MultipartFile() {
                @Override
                public String getName() {
                    return "filename.jpg"; // Tên file gốc
                }

                @Override
                public String getOriginalFilename() {
                    return "filename.jpg"; // Tên file gốc
                }

                @Override
                public String getContentType() {
                    return "image/jpeg"; // Định dạng file
                }

                @Override
                public boolean isEmpty() {
                    return decodedBytes.length == 0;
                }

                @Override
                public long getSize() {
                    return decodedBytes.length;
                }

                @Override
                public byte[] getBytes() throws IOException {
                    return decodedBytes;
                }

                @Override
                public InputStream getInputStream() throws IOException {
                    return new ByteArrayInputStream(decodedBytes);
                }

                @Override
                public Resource getResource() {
                    return MultipartFile.super.getResource();
                }

                @Override
                public void transferTo(File dest) throws IOException, IllegalStateException {
                    try (OutputStream outputStream = new FileOutputStream(dest)) {
                        outputStream.write(decodedBytes);
                    }
                }


                @Override
                public void transferTo(Path dest) throws IOException, IllegalStateException {
                    MultipartFile.super.transferTo(dest);
                }
            };

            return multipartFile;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Hàm kiểm tra chuỗi base64
    public static boolean isBase64(String str) {
        try {
            // Loại bỏ tiền tố Data URI nếu có
            if (str.startsWith("data:")) {
                str = str.split(",")[1];
            }

            // Loại bỏ khoảng trắng
            String cleanedBase64 = str.replaceAll("\\s", "");

            byte[] decodedBytes = Base64.getDecoder().decode(cleanedBase64);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
