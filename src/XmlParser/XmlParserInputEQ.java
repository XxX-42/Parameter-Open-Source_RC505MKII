package XmlParser;

import org.w3c.dom.*;
import org.xml.sax.InputSource;

import javax.xml.parsers.*;
import java.io.*;

public class XmlParserInputEQ extends XmlParser {

    // 定义标签的子标签名
    private static final String[] LABELS = {
            "SW", "LOGAIN", "HIGAIN", "LM-FREQ", "LM-Q", "LM-GAIN",
            "HM-FREQ", "HM-Q", "HM-GAIN", "LEVEL" , "LOCUT", "HICUT"
    };

    // 解析多个 <EQ_MIC1> 和 <EQ_MIC2> 标签并提取其子标签的内容
    public String parseXmlFile(String content) throws Exception {
        StringBuilder result = new StringBuilder();

        // 解析修复后的 XML 内容
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(content));
        Document document = builder.parse(is);

        // 获取所有 <EQ_MIC1>、<EQ_MIC2> 和 <EQ_MAINOUTR> 标签
        String[] tags = {"EQ_MIC1", "EQ_MIC2", "EQ_MAINOUTR"};
        for (String tag : tags) {
            NodeList eqNodeList = document.getElementsByTagName(tag);

            // 如果有该标签，处理其中的子标签
            if (eqNodeList.getLength() > 0) {
                Element eqElement = (Element) eqNodeList.item(0);
                NodeList childNodes = eqElement.getChildNodes();

                // 输出当前标签的信息
                result.append(tag).append(":\n");

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

                        // 对于每个子标签，根据其值进行映射
                        nodeValue = getMappedValue(labelName, nodeValue);

                        // 格式化标签名和值（右对齐，宽度 10）
                        tagNames.append(String.format("%-10s", labelName));  // 标签名右对齐，宽度 10
                        tagValues.append(String.format("%-10s", nodeValue));  // 标签值右对齐，宽度 10
                        labelIndex++;
                    }
                }

                // 添加标签名和标签值到结果中
                result.append(tagNames.toString().trim()).append("\n");
                result.append(tagValues.toString().trim()).append("\n\n");
            }
        }

        // 如果没有解析到任何标签，输出提示信息
        if (result.length() == 0) {
            result.append("No tags found in the XML content.\n");
        }

        return result.toString(); // 返回解析结果
    }

    // 映射值的方法，按照您的表格逻辑进行转换
    private String getMappedValue(String label, String value) {
        // 根据标签名和对应的值，进行映射
        switch (label) {
            case "SW":
                if (value.equals("0")) return "OFF";
                else if (value.equals("1")) return "ON";
                break;

            case "LOGAIN":
            case "HIGAIN":
            case "LM-GAIN":
            case "HM-GAIN":
            case "LEVEL":
                // 对于 LEVEL 标签，进行数值判断并映射
                try {
                    int intValue = Integer.parseInt(value);

                    // 判断值是否大于 20，20以上输出数值减去 20
                    if (intValue > 20) {
                        return String.valueOf(intValue - 20);  // 20以上，返回原值减去 20
                    }
                    // 判断值是否小于 20，20以下输出 "0下"
                    else if (intValue < 20) {
                        return "-" + String.valueOf(20 - intValue);  // 20以下
                    }
                    // 判断值是否等于 20，输出 "0"
                    else {
                        return "0";  // 20为0
                    }
                } catch (NumberFormatException e) {
                    // 如果解析失败，返回原始值
                    return value;
                }

            case "LOCUT":
                // 对 LOCUT 进行相应的映射
                return "UNKNOWN";

            case "HICUT":
                // 对 HICUT 进行相应的映射
                return "UNKNOWN";

            case "LM-FREQ":
                // 对 LM-FREQ 进行相应的映射
                return getFrequencyMapping(value);

            case "HM-FREQ":
                // 对 HM-FREQ 进行相应的映射
                return getFrequencyMapping(value);

            case "HM-Q":
            case "LM-Q":
                // 对于 HM-Q 和 LM-Q 标签，根据提供的映射表进行转换
                try {
                    int intValue = Integer.parseInt(value);

                    switch (intValue) {
                        case 0:
                            return "0.5";
                        case 1:
                            return "1";
                        case 2:
                            return "2";
                        case 3:
                            return "4";
                        case 4:
                            return "8";
                        case 5:
                            return "16";
                        default:
                            return value;  // 对于其他不在映射范围内的值，返回原始值
                    }
                } catch (NumberFormatException e) {
                    // 如果解析失败，返回原始值
                    return value;
                }

            default:
                return value; // 如果没有匹配的标签，返回原始值
        }
        return value; // 默认返回值
    }

    private String getFrequencyMapping(String value) {
        // 根据传入的频率值（数字）返回对应的频率标签
        switch (value) {
            case "0":
                return "20.0Hz";
            case "1":
                return "25.0Hz";
            case "2":
                return "31.5Hz";
            case "3":
                return "40.0Hz";
            case "4":
                return "50.0Hz";
            case "5":
                return "63.0Hz";
            case "6":
                return "80.0Hz";
            case "7":
                return "100Hz";
            case "8":
                return "125Hz";
            case "9":
                return "160Hz";
            case "10":
                return "200Hz";
            case "11":
                return "250Hz";
            case "12":
                return "315Hz";
            case "13":
                return "400Hz";
            case "14":
                return "500Hz";
            case "15":
                return "630Hz";
            case "16":
                return "800Hz";
            case "17":
                return "1.00KHz";
            case "18":
                return "1.25KHz";
            case "19":
                return "1.6KHz";
            case "20":
                return "2.00KHz";
            case "21":
                return "2.5KHz";
            case "22":
                return "3.15KHz";
            case "23":
                return "4.00KHz";
            case "24":
                return "5.00KHz";
            case "25":
                return "6.3KHz";
            case "26":
                return "8.00KHz";
            case "27":
                return "10.00KHz";
            default:
                return value; // 如果没有匹配的频率值，返回原始值
        }
    }
}
