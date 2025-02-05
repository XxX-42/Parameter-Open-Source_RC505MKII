package XmlParser;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import javax.xml.parsers.*;
import java.io.*;



import java.io.*;
import org.xml.sax.InputSource;

public class XmlParser {

    // 通用的 XML 解析方法，提取数字并转换为 ANSI 字符
    public String numberToAnsi(String numberStr) {
        try {
            int number = Integer.parseInt(numberStr);
            return String.valueOf((char) number);  // 将数字转换为字符
        } catch (NumberFormatException e) {
            return "";
        }
    }

    // 读取文件内容为字符串
    public String readFileContent(File file) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }
}
