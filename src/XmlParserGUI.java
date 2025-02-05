import XmlParser.*;
import XmlParser.XmlParserFX.FxParam;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

public class XmlParserGUI {
    private JFrame frame;
    private JLabel fileNameLabel;
    private JTextArea dataTextArea;
    // private JTextArea errorTextArea; // 移除错误信息文本区域
    private String lastDirectory;
    private String fixedXmlContent;  // 存储修复后的 XML 内容

    // 输入和输出银行的下拉框
    private JComboBox<String> inputBankComboBox1, inputBankComboBox2;
    // 输入FX A-D的下拉框
    private JComboBox<String> inputFxAComboBox, inputFxBComboBox, inputFxCComboBox, inputFxDComboBox;
    // 输出FX A-D的下拉框
    private JComboBox<String> outputFxAComboBox, outputFxBComboBox, outputFxCComboBox, outputFxDComboBox;

    // FX选项列表，包括新增的四个效果
    private static final String[] fxNames = {
            "select","LPF", "BPF", "HPF", "PHASER", "FLANGER", "SYNTH", "LOFI", "RADIO",
            "RING_MODULATOR", "G2B", "SUSTAINER", "AUTO_RIFF", "SLOW_GEAR",
            "TRANSPOSE", "PITCH_BEND", "ROBOT", "ELECTRIC", "HARMONIST_MANUAL",
            "HARMONIST_AUTO", "VOCODER", "OSC_VOCODER", "OSC_BOT", "PREAMP",
            "DIST", "DYNAMICS", "EQ", "ISOLATOR", "OCTAVE", "AUTO_PAN", "MANUAL_PAN",
            "STEREO_ENHANCE", "TREMOLO", "VIBRATO", "PATTERN_SLICER", "STEP_SLICER",
            "DELAY", "PANNING_DELAY", "REVERSE_DELAY", "MOD_DELAY", "TAPE_ECHO",
            "TAPE_ECHO_V505V2", "GRANULAR_DELAY", "WARP", "TWIST", "ROLL",
            "ROLL_V505V2", "FREEZE", "CHORUS", "REVERB", "GATE_REVERB", "REVERSE_REVERB",
            "BEAT_SCATTER", "BEAT_REPEAT", "BEAT_SHIFT", "VINYL_FLICK"
    };


    // 自定义背景面板类
    class BackgroundPanel extends JPanel {
        private BufferedImage backgroundImage;

        public BackgroundPanel(String imagePath) {
            try {
                // 使用 ClassLoader 从资源路径加载图片
                backgroundImage = ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(imagePath)));
                if (backgroundImage != null) {
                    backgroundImage = getGaussianBlurFilter(backgroundImage, 15).filter(backgroundImage, null);
                } else {
                    throw new IOException("Background image not found: " + imagePath);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            setLayout(new BorderLayout());
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImage != null) {
                // 缩放背景图片以适应面板大小
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            }
        }

        // 创建高斯模糊滤镜
        private ConvolveOp getGaussianBlurFilter(BufferedImage image, int radius) {
            int size = radius * 2 + 1;
            float[] data = new float[size * size];
            float sigma = radius / 3.0f;
            float twoSigmaSquare = 2.0f * sigma * sigma;
            float sigmaRoot = (float) Math.sqrt(twoSigmaSquare * Math.PI);
            float total = 0.0f;

            for (int y = -radius; y <= radius; y++) {
                for (int x = -radius; x <= radius; x++) {
                    float distance = x * x + y * y;
                    int index = (y + radius) * size + (x + radius);
                    data[index] = (float) Math.exp(-distance / twoSigmaSquare) / sigmaRoot;
                    total += data[index];
                }
            }

            // 归一化核
            for (int i = 0; i < data.length; i++) {
                data[i] /= total;
            }

            Kernel kernel = new Kernel(size, size, data);
            return new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
        }
    }

    public XmlParserGUI() {

        // 设置全局样式
        UIManager.put("Label.foreground", Color.WHITE);
        UIManager.put("Label.font", new Font("Arial", Font.BOLD, 14));
        UIManager.put("ComboBox.background", Color.WHITE);
        UIManager.put("ComboBox.foreground", Color.BLACK);
        UIManager.put("ComboBox.font", new Font("Arial", Font.PLAIN, 14));

        lastDirectory = FilePathMemory.loadLastDirectory();  // 加载上次的文件夹路径

        // 创建带背景图片的面板
        BackgroundPanel backgroundPanel = new BackgroundPanel("img.png"); // 确保RC.jpg在src目录下
        frame = new JFrame("RC505MKII PARSER");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1400, 800);
        frame.setContentPane(backgroundPanel); // 设置内容面板为背景面板
        frame.setLayout(new BorderLayout());

        // 顶部面板：打开文件按钮和文件名标签
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout());
        topPanel.setOpaque(false); // 使面板透明以显示背景
        backgroundPanel.add(topPanel, BorderLayout.NORTH);

        JButton openButton = new JButton("Open File");
        openButton.addActionListener(this::openFile);
        topPanel.add(openButton);

        fileNameLabel = new JLabel("No file selected");
        fileNameLabel.setForeground(Color.WHITE); // 根据背景调整文字颜色
        topPanel.add(fileNameLabel);

        // 中间区域：显示解析后的数据
        dataTextArea = new JTextArea(20, 60);
        dataTextArea.setEditable(false);
        dataTextArea.setFont(new Font("Monospaced", Font.BOLD, 15)); // 设置等宽粗体字体
        dataTextArea.setForeground(Color.BLACK); // 设置文字颜色为黑色
        dataTextArea.setBackground(new Color(230, 230, 230)); // 设置灰色背景底纹
        JScrollPane scrollPane = new JScrollPane(dataTextArea);
        scrollPane.setOpaque(true);
        scrollPane.getViewport().setOpaque(true);
        scrollPane.setBackground(new Color(230, 230, 230)); // 确保滚动面板背景与文本区域一致
        scrollPane.setBorder(BorderFactory.createEmptyBorder()); // 移除滚动边框
        backgroundPanel.add(scrollPane, BorderLayout.CENTER);

        // 底部区域：固定文字和导出按钮
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());
        bottomPanel.setOpaque(false); // 使面板透明

        // 添加“导出为 Markdown”按钮
        JButton exportButton = new JButton("Export to Markdown");
        exportButton.addActionListener(this::exportToMarkdown);
        JPanel exportPanel = new JPanel();
        exportPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        exportPanel.setOpaque(false); // 使面板透明
        exportPanel.add(exportButton);
        bottomPanel.add(exportPanel, BorderLayout.NORTH);

        // 固定文字部分
        JLabel footerLabel = new JLabel("<html>Designed and implemented by XXX | find me in <a href='https://github.com/XxX-42/Parameter-Open-Source_RC505MKII'>https://github.com/XxX-42/Parameter-Open-Source_RC505MKII</a></html>");
        footerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        footerLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        footerLabel.setForeground(Color.BLACK); // 根据背景调整文字颜色
        footerLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI("https://github.com/XxX-42/Parameter-Open-Source_RC505MKII"));
                } catch (IOException | URISyntaxException ex) {
                    ex.printStackTrace();
                }
            }
        });
        bottomPanel.add(footerLabel, BorderLayout.SOUTH);

        backgroundPanel.add(bottomPanel, BorderLayout.SOUTH);

        // 左侧面板：输入和输出银行及FX选择
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));  // 设置垂直布局
        leftPanel.setOpaque(false); // 使面板透明
        backgroundPanel.add(leftPanel, BorderLayout.WEST);

        // 输入部分
        leftPanel.add(new JLabel("INPUT bank:"));
        leftPanel.add(inputBankComboBox1 = new JComboBox<>(new String[]{"A", "B", "C", "D"}));  // 只选择银行字母

        // 输入FX A-D 的下拉框
        leftPanel.add(new JLabel("FX A:"));
        leftPanel.add(inputFxAComboBox = new JComboBox<>(fxNames));

        leftPanel.add(new JLabel("FX B:"));
        leftPanel.add(inputFxBComboBox = new JComboBox<>(fxNames));

        leftPanel.add(new JLabel("FX C:"));
        leftPanel.add(inputFxCComboBox = new JComboBox<>(fxNames));

        leftPanel.add(new JLabel("FX D:"));
        leftPanel.add(inputFxDComboBox = new JComboBox<>(fxNames));

        // 输出部分
        leftPanel.add(new JLabel("OUTPUT bank:"));
        leftPanel.add(inputBankComboBox2 = new JComboBox<>(new String[]{"A", "B", "C", "D"}));  // 只选择银行字母

        // 输出FX A-D 的下拉框
        leftPanel.add(new JLabel("FX A:"));
        leftPanel.add(outputFxAComboBox = new JComboBox<>(fxNames));

        leftPanel.add(new JLabel("FX B:"));
        leftPanel.add(outputFxBComboBox = new JComboBox<>(fxNames));

        leftPanel.add(new JLabel("FX C:"));
        leftPanel.add(outputFxCComboBox = new JComboBox<>(fxNames));

        leftPanel.add(new JLabel("FX D:"));
        leftPanel.add(outputFxDComboBox = new JComboBox<>(fxNames));

        // Output按钮
        JButton outputButton = new JButton("Output Selected FX");
        outputButton.addActionListener(this::outputSelectedFX);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 10)));  // 添加空白间隔
        leftPanel.add(outputButton);

        frame.setVisible(true);
    }

    /**
     * 输出选中的FX数据到dataTextArea，不清空原有内容
     */
    private void outputSelectedFX(ActionEvent event) {
        // 检查是否已选择文件
        if (fixedXmlContent == null || fixedXmlContent.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "请先选择并打开一个XML文件。", "未选择文件", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 创建XmlParserFX实例
        XmlParserFX parserFX = new XmlParserFX();

        // 创建StringBuilder来构建输出内容
        StringBuilder outputBuilder = new StringBuilder();

        // 处理输入银行的FX A-D
        String inputBankLetter = (String) inputBankComboBox1.getSelectedItem();
        outputBuilder.append("Selected INPUT bank: ").append(inputBankLetter).append("\n");

        // FX A
        String inputFxAName = ((String) inputFxAComboBox.getSelectedItem());
        FxParam inputFxAParam = parserFX.parseFX("ifx", inputBankLetter, "A", inputFxAName, fixedXmlContent);
        outputBuilder.append("FX A:\n").append(inputFxAParam != null ? inputFxAParam.toString() : "N/A").append("\n\n");

        // FX B
        String inputFxBName = ((String) inputFxBComboBox.getSelectedItem());
        FxParam inputFxBParam = parserFX.parseFX("ifx", inputBankLetter, "B", inputFxBName, fixedXmlContent);
        outputBuilder.append("FX B:\n").append(inputFxBParam != null ? inputFxBParam.toString() : "N/A").append("\n\n");

        // FX C
        String inputFxCName = ((String) inputFxCComboBox.getSelectedItem());
        FxParam inputFxCParam = parserFX.parseFX("ifx", inputBankLetter, "C", inputFxCName, fixedXmlContent);
        outputBuilder.append("FX C:\n").append(inputFxCParam != null ? inputFxCParam.toString() : "N/A").append("\n\n");

        // FX D
        String inputFxDName = ((String) inputFxDComboBox.getSelectedItem());
        FxParam inputFxDParam = parserFX.parseFX("ifx", inputBankLetter, "D", inputFxDName, fixedXmlContent);
        outputBuilder.append("FX D:\n").append(inputFxDParam != null ? inputFxDParam.toString() : "N/A").append("\n\n");

        // 处理输出银行的FX A-D
        String outputBankLetter = (String) inputBankComboBox2.getSelectedItem();
        outputBuilder.append("Selected OUTPUT bank: ").append(outputBankLetter).append("\n");

        // FX A
        String outputFxAName = ((String) outputFxAComboBox.getSelectedItem());
        FxParam outputFxAParam = parserFX.parseFX("tfx", outputBankLetter, "A", outputFxAName, fixedXmlContent);
        outputBuilder.append("FX A:\n").append(outputFxAParam != null ? outputFxAParam.toString() : "N/A").append("\n\n");

        // FX B
        String outputFxBName = ((String) outputFxBComboBox.getSelectedItem());
        FxParam outputFxBParam = parserFX.parseFX("tfx", outputBankLetter, "B", outputFxBName, fixedXmlContent);
        outputBuilder.append("FX B:\n").append(outputFxBParam != null ? outputFxBParam.toString() : "N/A").append("\n\n");

        // FX C
        String outputFxCName = ((String) outputFxCComboBox.getSelectedItem());
        FxParam outputFxCParam = parserFX.parseFX("tfx", outputBankLetter, "C", outputFxCName, fixedXmlContent);
        outputBuilder.append("FX C:\n").append(outputFxCParam != null ? outputFxCParam.toString() : "N/A").append("\n\n");

        // FX D
        String outputFxDName = ((String) outputFxDComboBox.getSelectedItem());
        FxParam outputFxDParam = parserFX.parseFX("tfx", outputBankLetter, "D", outputFxDName, fixedXmlContent);
        outputBuilder.append("FX D:\n").append(outputFxDParam != null ? outputFxDParam.toString() : "N/A").append("\n\n");

        // 显示在dataTextArea，使用 append() 来避免清空文本
        dataTextArea.append(outputBuilder.toString());
    }

    /**
     * 新增方法：将dataTextArea的内容导出为Markdown文件
     */
    private void exportToMarkdown(ActionEvent event) {
        String content = dataTextArea.getText();
        if (content.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "当前输出框中没有内容可导出。", "无内容", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser(lastDirectory);
        fileChooser.setDialogTitle("Save as Markdown");
        fileChooser.setSelectedFile(new File("output.md"));
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Markdown Files", "md"));

        int userSelection = fileChooser.showSaveDialog(frame);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            // 确保文件以 .md 结尾
            if (!fileToSave.getName().toLowerCase().endsWith(".md")) {
                fileToSave = new File(fileToSave.getParentFile(), fileToSave.getName() + ".md");
            }
            try (java.io.FileWriter writer = new java.io.FileWriter(fileToSave)) {
                writer.write(content);
                JOptionPane.showMessageDialog(frame, "内容已成功导出到 " + fileToSave.getAbsolutePath(), "导出成功", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(frame, "导出失败: " + e.getMessage(), "导出错误", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    /**
     * 打开文件并解析，使用 append() 而不是 setText()
     */
    private void openFile(ActionEvent event) {
        JFileChooser fileChooser = new JFileChooser(lastDirectory);
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("RC0 Files", "RC0"));
        int returnValue = fileChooser.showOpenDialog(frame);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            lastDirectory = selectedFile.getParent();
            FilePathMemory.saveLastDirectory(lastDirectory);
            fileNameLabel.setText(selectedFile.getName());

            try {
                // 读取文件内容并修复标签
                XmlParser xmlParser = new XmlParser();
                String content = xmlParser.readFileContent(selectedFile);
                fixedXmlContent = XmlTagFixerUtil.fixTags(content);  // 缓存修复后的 XML 内容

                // 使用 XmlParserName 解析文件
                XmlParserName nameParser = new XmlParserName();
                String nameResult = nameParser.parseXmlFile(fixedXmlContent);  // 使用修复后的 XML 内容

                // 使用 XmlParserMasterFX 解析文件并获取 COMP 和 REVERB 信息
                XmlParserMasterFX masterFxParser = new XmlParserMasterFX();
                String masterFxResult = masterFxParser.parseMasterFxXmlFile(fixedXmlContent);  // 使用修复后的 XML 内容

                // 使用 XmlParserInput 解析 <INPUT> 标签并获取相应的内容
                XmlParserInput inputParser = new XmlParserInput();
                String inputResult = inputParser.parseXmlFile(fixedXmlContent);  // 使用修复后的 XML 内容

                // 使用 XmlParserTrack 解析多个 <track1>, <track2>, ..., <track5> 标签并获取相应的内容
                XmlParserTrack trackParser = new XmlParserTrack();
                String trackResult = trackParser.parseXmlFile(fixedXmlContent);

                // 使用 XmlParserInputEQ 解析多个 <EQ> 标签并获取相应的内容
                XmlParserInputEQ EQParser = new XmlParserInputEQ();
                String EQResult = EQParser.parseXmlFile(fixedXmlContent);

                // 将所有结果拼接显示，使用 append 确保不会清空文本区域
                StringBuilder combinedResult = new StringBuilder();
                combinedResult.append(nameResult).append("\n")
                        .append(masterFxResult).append("\n")
                        .append(inputResult).append("\n")
                        .append(trackResult).append("\n")
                        .append(EQResult).append("\n");

                dataTextArea.append(combinedResult.toString());
                // errorTextArea.setText(""); // 移除错误文本区域的清空操作
            } catch (Exception e) {
                // 如果需要显示错误信息，可以重新添加 errorTextArea 并正确显示
                JOptionPane.showMessageDialog(frame, "Error parsing XML: " + e.getMessage(), "解析错误", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(XmlParserGUI::new);
    }
}
