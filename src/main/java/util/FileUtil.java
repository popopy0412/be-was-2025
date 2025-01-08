package util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class FileUtil {
    private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);

    private FileUtil() {}

    public static byte[] fileToByteArray(String path) throws IOException {
        File file = new File(path);
        String isFileExist = file.exists() ? "exists" : "not exists";
        String isDirectory = file.isDirectory() ? "isDirectory" : "not isDirectory";

        logger.debug(file.getPath());
        logger.debug(isFileExist);
        logger.debug(isDirectory);
        if (file.exists()) {
            return Files.readAllBytes(file.toPath());
        } else {
            return null;
        }
    }
}
