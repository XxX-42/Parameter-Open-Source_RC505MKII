import java.io.*;
import java.util.*;

public class FilePathMemory {
    private static final String FILE_PATH_KEY = "lastDirectory";
    private static final String PROPERTIES_FILE = "config.properties";

    // 加载上次使用的文件夹路径
    public static String loadLastDirectory() {
        try (FileInputStream fis = new FileInputStream(PROPERTIES_FILE)) {
            Properties properties = new Properties();
            properties.load(fis);
            return properties.getProperty(FILE_PATH_KEY, "");
        } catch (IOException e) {
            return "";  // 默认返回空路径
        }
    }

    // 保存当前文件夹路径
    public static void saveLastDirectory(String path) {
        try (FileOutputStream fos = new FileOutputStream(PROPERTIES_FILE)) {
            Properties properties = new Properties();
            properties.setProperty(FILE_PATH_KEY, path);
            properties.store(fos, null);  // 保存路径
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
