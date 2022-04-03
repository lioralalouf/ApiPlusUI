package utils;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class FileUtils {

    public static String getFileHash(File file) throws NoSuchAlgorithmException, IOException {
        byte[] fileContent = Files.readAllBytes(file.toPath());
        return (new HexBinaryAdapter()).marshal(MessageDigest.getInstance("MD5").digest(fileContent));
    }

    public static String getRemoteFileHash(String url) {
        String filename = UUID.randomUUID().toString();

        try (BufferedInputStream in = new BufferedInputStream(new URL(url).openStream());
             FileOutputStream fileOutputStream = new FileOutputStream(filename)) {
            byte dataBuffer[] = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }

            return getFileHash(new File(filename));
        } catch (IOException e) {
            // handle exception
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return null;
    }
}
