import java.io.File;

public class XmlTagFixer {
    // 删除临时生成的文件
    public static void deleteTempFile() {
        File tempFile = new File("tempFile.xml");
        if (tempFile.exists()) {
            tempFile.delete();  // 删除临时文件
        }
    }
}
