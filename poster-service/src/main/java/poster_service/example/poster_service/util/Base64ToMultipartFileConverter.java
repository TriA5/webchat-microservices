package poster_service.example.poster_service.util;

import org.springframework.web.multipart.MultipartFile;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

public class Base64ToMultipartFileConverter {

    public static MultipartFile convert(String base64Data) {
        return new Base64MultipartFile(base64Data);
    }

    private static class Base64MultipartFile implements MultipartFile {
        private final byte[] fileContent;
        private final String contentType;
        private final String originalFilename;

        public Base64MultipartFile(String base64Data) {
            // Parse: "data:image/png;base64,iVBORw0KGgo..." or "data:video/mp4;base64,..."
            String[] parts = base64Data.split(",");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid base64 data format");
            }

            String metadata = parts[0]; // "data:image/png;base64" or "data:video/mp4;base64"
            String base64Content = parts[1];

            // Extract content type
            if (metadata.contains(":") && metadata.contains(";")) {
                this.contentType = metadata.substring(metadata.indexOf(":") + 1, metadata.indexOf(";"));
            } else {
                this.contentType = "application/octet-stream";
            }

            // Determine file extension
            String extension = ".bin";
            if (contentType.startsWith("image/")) {
                extension = "." + contentType.substring(6); // "image/png" -> ".png"
            } else if (contentType.startsWith("video/")) {
                extension = "." + contentType.substring(6); // "video/mp4" -> ".mp4"
            }

            this.originalFilename = "upload" + extension;
            this.fileContent = Base64.getDecoder().decode(base64Content);
        }

        @Override
        public String getName() {
            return "file";
        }

        @Override
        public String getOriginalFilename() {
            return originalFilename;
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public boolean isEmpty() {
            return fileContent == null || fileContent.length == 0;
        }

        @Override
        public long getSize() {
            return fileContent.length;
        }

        @Override
        public byte[] getBytes() throws IOException {
            return fileContent;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(fileContent);
        }

        @Override
        public void transferTo(File dest) throws IOException, IllegalStateException {
            try (FileOutputStream fos = new FileOutputStream(dest)) {
                fos.write(fileContent);
            }
        }
    }
}
