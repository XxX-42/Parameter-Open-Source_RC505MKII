package XmlParser;

import org.w3c.dom.*;
import org.xml.sax.InputSource;

import javax.xml.parsers.*;
import java.io.*;

public class XmlParserOutputEQ extends XmlParser {

    // 定义标签名的替换
    private static final String[] LABELS = {
            "REVERSE", "1SHOT", "PAN", "PLAYLEVEL", "STARTMODE", "STOPMODE", "DUBMODE",
            "FX", "PLAYMODE", "MEASURE", "LPSYN SW", "LPSYN MODE", "TSYN SW", "TSYN CMODE", "SPEED", "BOUNCEIN",
            "UNKNOWN1", "UNKNOWN2", "UNKNOWN3", "UNKNOWN4", "UNKNOWN5", "UNKNOWN6", "UNKNOWN7", "UNKNOWN8", "UNKNOWN9", "UNKNOWN10"//这一行没有提供解析
    };


    // 解析多个 <TRACK1>, <TRACK2>, <TRACK3>, ..., <TRACK5> 标签并提取其子标签的内容
    public String parseXmlFile(String content) throws Exception {
        StringBuilder result = new StringBuilder();

        // 解析修复后的 XML 内容
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(content));
        Document document = builder.parse(is);

        // 获取所有 <TRACK1>, <TRACK2>, <TRACK3>, ..., <TRACK5> 标签
        for (int i = 1; i <= 5; i++) {
            String trackTag = "TRACK" + i; // 如 "TRACK1", "TRACK2", ...
            NodeList trackNodeList = document.getElementsByTagName(trackTag);

            // 如果有该标签，处理其中的子标签
            if (trackNodeList.getLength() > 0) {
                Element trackElement = (Element) trackNodeList.item(0);
                NodeList childNodes = trackElement.getChildNodes();

                // 输出当前 track 标签的信息
                result.append(trackTag).append(":\n");

                // 用于存储标签名和标签值
                StringBuilder tagNames = new StringBuilder();
                StringBuilder tagValues = new StringBuilder();

                // 遍历子节点
                int labelIndex = 0;
                for (int j = 0; j < childNodes.getLength(); j++) {
                    Node node = childNodes.item(j);
                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        String nodeValue = node.getTextContent();
                        String labelName = LABELS[labelIndex];

                        // 对于特定标签，根据其值进行映射
                        nodeValue = getMappedValue(labelName, nodeValue);

                        // 格式化标签名和值（右对齐，宽度 10）
                        tagNames.append(String.format("%-12s", labelName));  // 标签名右对齐，宽度 10
                        tagValues.append(String.format("%-12s", nodeValue));  // 标签值右对齐，宽度 10
                        labelIndex++;
                    }
                }

                // 添加标签名和标签值到结果中
                result.append(tagNames.toString().trim()).append("\n");
                result.append(tagValues.toString().trim()).append("\n\n");
            }
        }

        // 如果没有解析到任何 <TRACK1>, <TRACK2>, ..., <TRACK5> 标签，输出提示信息
        if (result.length() == 0) {
            result.append("No track tags found in the XML content.\n");
        }

        return result.toString(); // 返回所有 <TRACK> 标签的解析结果
    }

    // 检查标签名是否在需要转换为 "ON" 或 "OFF" 的标签列表中

    // 映射值的方法，按照您的表格逻辑进行转换
    private String getMappedValue(String label, String value) {
        // 根据标签名和对应的值，进行映射
        switch (label) {
            case "CENTER":
                if (value.equals("0")) return "1";
                else if (value.equals("1")) return "2";
                break;

            case "STARTMODE":
                if (value.equals("0")) return "IMMEDIATE";
                else if (value.equals("1")) return "FADE";
                break;

            case "STOPMODE":
                if (value.equals("0")) return "IMMEDIATE";
                else if (value.equals("1")) return "FADE";
                else if (value.equals("3")) return "LOOP";
                break;

            case "DUBMODE":
                if (value.equals("0")) return "OVERDUB";
                else if (value.equals("1")) return "REPLACE1";
                else if (value.equals("2")) return "REPLACE2";
                break;

            case "PLAYMODE":
                if (value.equals("0")) return "MULTI";
                else if (value.equals("1")) return "SINGLE";
                break;

            case "MEASURE":
                if (value.equals("0")) return "AUTO";
                else if (value.equals("1")) return "FREE";
                else if (value.equals("3")) return "1/16note";
                else if (value.equals("4")) return "1/8note";
                else if (value.equals("5")) return "1/4note";
                else if (value.equals("6")) return "1/2note";
                else if (value.equals("7")) return "One bar";
                else if (value.equals("8")) return "Two bars";
                break;

            case "LOOPSYNC":
                if (value.equals("0")) return "OFF";
                else if (value.equals("1")) return "ON";
                break;

            case "MODE":
                if (value.equals("0")) return "PITCH";
                else if (value.equals("1")) return "XFADE";
                break;

            case "SPEED":
                if (value.equals("0")) return "HALF";
                else if (value.equals("1")) return "NORMAL";
                else if (value.equals("3")) return "DOUBLE";
                break;

            // 处理需要转换为 "ON" 或 "OFF" 的标签
            case "1SHOT":
            case "FX":
            case "LPSYN SW":
            case "TSYN SW":
            case "BOUNCEIN":
                // 如果值是 "0"，转换为 "OFF"；如果值是 "1"，转换为 "ON"
                if (value.equals("0")) return "OFF";
                else if (value.equals("1")) return "ON";
                break;

            case "MIC1":
                if (value.equals("126")) return "OFF";
                else if (value.equals("127")) return "ON";
                break;
            case "REVERSE":
                // 如果值是 "0"，转换为 "OFF"；如果值是 "1"，转换为 "ON"
                if (value.equals("0")) return "OFF";
                else if (value.equals("1")) return "ON";
                break;

            case "LPSYN MODE":
                if (value.equals("0")) return "IMMEDIATE";
                else if (value.equals("1")) return "MEASURE";
                else if (value.equals("1")) return "LOOP LENGTH";
                break;

            default:
                return value; // 如果没有匹配的标签，返回原始值
        }
        return value; // 返回值的默认映射
    }

}
