package XmlParser;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import javax.xml.parsers.*;
import java.io.*;

public class XmlParserMasterFX extends XmlParser {

    // 解析修复后的 XML 文件并提取 <MASTER_FX> 标签中的 <A> 和 <B> 值
    public String parseMasterFxXmlFile(String content) throws Exception {
        StringBuilder result = new StringBuilder();

        // 解析修复后的 XML 内容
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(content));
        Document document = builder.parse(is);

        // 获取 <MASTER_FX> 标签
        NodeList masterFxNodeList = document.getElementsByTagName("MASTER_FX");

        // 如果有 <MASTER_FX> 标签，处理其中的子标签 <A>, <B>
        if (masterFxNodeList.getLength() > 0) {
            Element masterFxElement = (Element) masterFxNodeList.item(0);
            NodeList childNodes = masterFxElement.getChildNodes();

            String aValue = "", bValue = "";

            for (int i = 0; i < childNodes.getLength(); i++) {
                Node node = childNodes.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    String nodeName = node.getNodeName();
                    String nodeValue = node.getTextContent();

                    if ("A".equals(nodeName)) {
                        aValue = nodeValue;  // 提取 A 的值
                    } else if ("B".equals(nodeName)) {
                        bValue = nodeValue;  // 提取 B 的值
                    }
                }
            }

            // 如果 A 的值为 0，显示为 OFF
            if ("0".equals(aValue)) {
                aValue = "OFF";
            }

            // 构建最终的结果
            result.append("COMP: ").append(aValue).append("\n");
            result.append("REVERB: ").append(bValue).append("\n");
        }

        return result.toString();
    }
}
