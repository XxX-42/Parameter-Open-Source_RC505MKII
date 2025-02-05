package XmlParser;

import org.w3c.dom.*;
import org.xml.sax.InputSource;

import javax.xml.parsers.*;
import java.io.*;

public class XmlParserName extends XmlParser {

    // 解析修复后的 XML 文件并提取 <NAME> 标签中的数字，并转换为字符
    public String parseXmlFile(String content) throws Exception {
        StringBuilder result = new StringBuilder();

        // 解析修复后的 XML 内容
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(content));
        Document document = builder.parse(is);

        // 获取 <NAME> 标签
        NodeList nameNodeList = document.getElementsByTagName("NAME");

        // 如果有 <NAME> 标签，处理其中的子标签
        if (nameNodeList.getLength() > 0) {
            Element nameElement = (Element) nameNodeList.item(0);
            NodeList childNodes = nameElement.getChildNodes();

            for (int i = 0; i < childNodes.getLength(); i++) {
                Node node = childNodes.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    String nodeValue = node.getTextContent();

                    // 只有在遇到数字时进行处理
                    if (nodeValue.matches("\\d+")) {
                        String ansiChar = super.numberToAnsi(nodeValue);  // 调用父类的 numberToAnsi 方法
                        result.append(ansiChar);  // 直接将字符附加到结果字符串中
                    }
                }
            }
        }

        return result.toString();  // 返回一个没有标签的字符串
    }
}
