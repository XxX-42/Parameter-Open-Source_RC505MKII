package XmlParser;

public class XmlTagFixerUtil {

    // 修复标签的操作
    public static String fixTags(String content) {
        // 删除 <count>4E</count> 标签
        content = content.replaceAll("<count>.*?</count>", "");  // 删除所有 <count> 标签及其内容

        // 替换非法的数字标签
        content = content.replaceAll("<([0-9]+)>", "<tag$1>"); // 将 <0> 改为 <tag0> 等
        content = content.replaceAll("</([0-9]+)>", "</tag$1>"); // 将 </0> 改为 </tag0> 等
        content = content.replaceAll("<#>", "<hash>");  // 将 <#> 改为 <hash>
        content = content.replaceAll("</#>", "</hash>"); // 将 </#> 改为 </hash>

        return content;
    }
}
