public class main {
    public static void main(String[] args) {
        // 启动 GUI
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // 创建并显示图形界面
                new XmlParserGUI();
            }
        });
    }
}
