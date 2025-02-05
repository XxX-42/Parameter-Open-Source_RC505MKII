package XmlParser;

import org.w3c.dom.*;
import org.xml.sax.InputSource;

import javax.xml.parsers.*;
import java.io.*;

public class XmlParserInput extends XmlParser {

    // 解析 XML 内容并提取 <INPUT> 标签中的内容
    public String parseXmlFile(String content) throws Exception {
        StringBuilder result = new StringBuilder();

        // 解析 XML 内容
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(content));
        Document document = builder.parse(is);

        // 获取 <INPUT> 标签
        NodeList inputNodeList = document.getElementsByTagName("INPUT");

        // 如果有 <INPUT> 标签，处理其中的子标签 <A>, <B>, ..., <M>
        if (inputNodeList.getLength() > 0) {
            Element inputElement = (Element) inputNodeList.item(0);
            NodeList childNodes = inputElement.getChildNodes();

            // 变量用于存储各个标签的值
            String gValue = "", hValue = "", jValue = "", kValue = "", lValue = "", mValue = "";

            for (int i = 0; i < childNodes.getLength(); i++) {
                Node node = childNodes.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    String nodeName = node.getNodeName();
                    String nodeValue = node.getTextContent();

                    // 根据标签名提取相应的值
                    switch (nodeName) {
                        case "G":
                            gValue = nodeValue;
                            break;
                        case "H":
                            hValue = nodeValue;
                            break;
                        case "J":
                            jValue = nodeValue;
                            break;
                        case "K":
                            kValue = nodeValue;
                            break;
                        case "L":
                            lValue = nodeValue;
                            break;
                        case "M":
                            mValue = nodeValue;
                            break;
                        default:
                            break;
                    }
                }
            }

            // 构建最终的结果，包含每个标签的对应内容
            result.append("MIC 1 COMP: ").append(gValue).append("\n");
            result.append("MIC 1 NS: ").append(jValue).append("\n");
            result.append("MIC 2 COMP: ").append(hValue).append("\n");
            result.append("MIC 2 NS: ").append(kValue).append("\n");
            result.append("INST 1 NS: ").append(lValue).append("\n");
            result.append("INST 2 NS: ").append(mValue).append("\n");
        }

        return result.toString();
    }
}
