package tk.zhangmz;

import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.text.PlainDocument;
import javax.swing.text.StringContent;
import javax.swing.text.StyledDocument;

public class CellularAutomata extends JFrame {
    private static Integer b = 2;
    private static List<Integer> s = Arrays.asList(2, 3);
    private static CellScreen screen;
    private static Boolean running = false;

    public CellularAutomata() throws HeadlessException {
        super("Cellular Automata");

        // 主面板
        JPanel panel = new JPanel();

        // 控制面板
        JPanel ctlPanel = new JPanel();
        ctlPanel.setPreferredSize(new Dimension(200, 600));
        ctlPanel.setBackground(Color.WHITE);

        // 输入框
        Label bLabel = new Label("B:");
        Label sLabel = new Label("S:");
        JTextField bTextField = new JTextField(String.valueOf(b));
        JTextField sTextField = new JTextField(StringUtils.join(s.stream().map(String::valueOf).collect(Collectors.toList()), ","));
        Dimension textFieldSize = new Dimension(150, 25);
        bTextField.setPreferredSize(textFieldSize);
        sTextField.setPreferredSize(textFieldSize);
        ctlPanel.add(bLabel);
        ctlPanel.add(bTextField);
        ctlPanel.add(sLabel);
        ctlPanel.add(sTextField);

        // 文本域
        JTextArea textForm = new JTextArea();
        Dimension textSize = new Dimension(150, 300);
        textForm.setBackground(Color.LIGHT_GRAY);
        JScrollPane scrollPane = new JScrollPane(textForm);
        scrollPane.setPreferredSize(textSize);
        ctlPanel.add(scrollPane);

        // 控制按钮
        JButton startButton = new JButton("开始");
        JButton stopButton = new JButton("停止");
        JButton initButton = new JButton("初始化");
        JButton nextButton = new JButton("单步");
        JButton importButton = new JButton("导入");
        JButton exportButton = new JButton("导出");
        Dimension buttonSize = new Dimension(90, 30);
        startButton.setPreferredSize(buttonSize);
        stopButton.setPreferredSize(buttonSize);
        initButton.setPreferredSize(buttonSize);
        nextButton.setPreferredSize(buttonSize);
        importButton.setPreferredSize(buttonSize);
        exportButton.setPreferredSize(buttonSize);
        stopButton.setEnabled(false);

        startButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                running = true;
                b = Integer.parseInt(bTextField.getText().trim());
                s = Arrays.stream(sTextField.getText().split(",")).map(String::trim).map(Integer::valueOf).collect(Collectors.toList());
                Thread thread = new Thread(new Automata(() -> {
                    running = false;
                    startButton.setEnabled(true);
                    stopButton.setEnabled(false);
                    nextButton.setEnabled(true);
                }));
                thread.start();
                startButton.setEnabled(false);
                stopButton.setEnabled(true);
                nextButton.setEnabled(false);
            }
        });
        stopButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                running = false;
                startButton.setEnabled(true);
                stopButton.setEnabled(false);
                nextButton.setEnabled(true);
            }
        });
        initButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                init();
            }
        });
        nextButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new Automata().nextState();
            }
        });
        importButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                setPixelsFromString(textForm.getText());
            }
        });
        exportButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                textForm.setText(getStringFromPixels());
            }
        });

        ctlPanel.add(startButton);
        ctlPanel.add(stopButton);
        ctlPanel.add(nextButton);
        ctlPanel.add(initButton);
        ctlPanel.add(importButton);
        ctlPanel.add(exportButton);
        panel.add(ctlPanel);

        // 展示屏
        CellScreen screen = new CellScreen(500);
        CellularAutomata.screen = screen;
        screen.setPreferredSize(new Dimension(900, 900));
        screen.setBackground(Color.LIGHT_GRAY);

        panel.add(screen);

        this.add(panel);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.pack();

        toScreenCenter();
        this.setVisible(true);

        init();
    }

    public void init() {
        String initValue = "########################*\n" +
                "######################*#*\n" +
                "############**######**############**\n" +
                "###########*###*####**############**\n" +
                "**########*#####*###**\n" +
                "**########*###*#**####*#*\n" +
                "##########*#####*#######*\n" +
                "###########*###*\n" +
                "############**";
        setPixelsFromString(initValue);
    }

    private void setPixelsFromString(String strValue) {
        if (strValue == null) {
            return;
        }
        strValue = strValue.replaceAll(System.lineSeparator(), "\n");
        String[] split = strValue.split("\n");
        int height = split.length;
        int width = Arrays.stream(split).map(String::length).max(Comparator.comparingInt(t -> t)).orElse(0);
        if (height > screen.getScreenSize() || width > screen.getScreenSize()) {
            JOptionPane.showMessageDialog(this, "导入内容过大");
            return;
        }
        boolean[][] temp = new boolean[height][width];
        for (int i = 0; i < split.length; i++) {
            String line = split[i];
            for (int j = 0; j < line.toCharArray().length; j++) {
                temp[i][j] = '*' == line.toCharArray()[j];
            }
        }

        int x = (screen.getScreenSize() - width) / 2;
        int y = (screen.getScreenSize() - height) / 2;
        System.out.printf("x: %s, y: %s", x, y);

        boolean[][] pixels = new boolean[screen.getScreenSize()][screen.getScreenSize()];
        for (int i = 0; i < temp.length; i++) {
            System.arraycopy(temp[i], 0, pixels[i + y], x, temp[i].length);
        }

        screen.setPixels(pixels);
        screen.repaint();
    }

    private String getStringFromPixels() {
        boolean[][] pixels = screen.getPixels();
        int x1 = pixels.length - 1;
        int y1 = pixels.length - 1;
        int x2 = 0;
        int y2 = 0;
        for (int y = 0; y < pixels.length; y++) {
            for (int x = 0; x < pixels[y].length; x++) {
                if (pixels[y][x]) {
                    if (x1 > x) x1 = x;
                    if (y1 > y) y1 = y;
                    if (x2 < x) x2 = x;
                    if (y2 < y) y2 = y;
                }
            }
        }
        StringBuilder buffer = new StringBuilder();
        for (int y = y1; y <= y2; y++) {
            for (int x = x1; x <= x2; x++) {
                buffer.append(pixels[y][x] ? '*' : '#');
            }
            buffer.append("\n");
        }

        return buffer.toString();
    }

    private static class Automata implements Runnable {
        private final Callback stopFunc;

        public Automata(Callback stopFunc) {
            this.stopFunc = stopFunc;
        }

        public Automata() {
            this.stopFunc = () -> {
            };
        }

        @Override
        public void run() {
            while (running) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                nextState();
            }
        }

        public void nextState() {
            calculate();
            screen.repaint();
        }

        // 计算下一代
        private void calculate() {
            boolean[][] pixels = screen.getPixels();
            boolean[][] newGen = new boolean[pixels.length][pixels.length];
            for (int i = 0; i < pixels.length; i++) {
                for (int j = 0; j < pixels[i].length; j++) {
                    boolean cell = pixels[i][j];
                    int jl = j - 1;
                    int jr = j + 1;
                    int it = i - 1;
                    int id = i + 1;
                    int neighborNum = hasCell(it, jl, pixels) +
                            hasCell(it, j, pixels) +
                            hasCell(it, jr, pixels) +
                            hasCell(i, jl, pixels) +
                            hasCell(i, jr, pixels) +
                            hasCell(id, jl, pixels) +
                            hasCell(id, j, pixels) +
                            hasCell(id, jr, pixels);
                    if (cell) {
                        if (neighborNum < b) {
                            newGen[i][j] = false;
                        } else if (s.contains(neighborNum)) {
                            newGen[i][j] = true;
                        }
                    } else {
                        if (neighborNum == s.get(s.size() - 1)) {
                            newGen[i][j] = true;
                        }
                    }
                }
            }
            // 如果结果没有变化, 停止模拟
            if (Arrays.deepEquals(newGen, pixels)) {
                stopFunc.callback();
            }
            screen.setPixels(newGen);
        }

        private int hasCell(int i, int j, boolean[][] pixels) {
            if (i < 0 || i >= pixels.length || j < 0 || j >= pixels.length) {
                return 0;
            } else {
                return pixels[i][j] ? 1 : 0;
            }
        }
    }

    public void toScreenCenter() {
        // 居中展示窗口
        Toolkit defaultToolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = defaultToolkit.getScreenSize();
        int w = this.getWidth();
        int h = this.getHeight();
        int x = (int) ((screenSize.getWidth() - w) / 2);
        int y = (int) ((screenSize.getHeight() - h) / 2);
        x = Math.max(x, 0);
        y = Math.max(y, 0);
        this.setLocation(x, y);
    }

    public static void main(String[] args) {
        new CellularAutomata();
    }
}
