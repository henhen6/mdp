package top.mddata;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Java 8 后台压测工具
 * 压测：CPU 满核 + GPU 渲染/图像处理
 */
public class HardwareStressTest {

    // CPU 压测线程数 = 核心数
    private static final int CPU_THREADS = 3 * Runtime.getRuntime().availableProcessors();

    // 窗口大小（越大越吃显卡）
    private static final int WINDOW_SIZE = 1200;

    public static void main(String[] args) {
        // Mac 系统适配：开启渲染加速
        System.setProperty("sun.java2d.opengl", "true");
        System.setProperty("apple.awt.graphics.UseQuartz", "true");

        System.out.println("======================================");
        System.out.println("  CPU + GPU 压测（带显示窗口）");
        System.out.println("  CPU 线程数: " + CPU_THREADS);
        System.out.println("  窗口大小: " + WINDOW_SIZE + "x" + WINDOW_SIZE);
        System.out.println("  关闭窗口即可停止");
        System.out.println("======================================");

        // 1. 启动 CPU 压测
        ExecutorService cpuPool = Executors.newFixedThreadPool(CPU_THREADS);
        for (int i = 0; i < CPU_THREADS; i++) {
            cpuPool.submit(HardwareStressTest::cpuStressTask);
        }

        // 2. 启动 GUI 窗口 + GPU 实时渲染显示
        SwingUtilities.invokeLater(HardwareStressTest::createAndShowGui);
    }

    /**
     * CPU 满负载压测
     */
    private static void cpuStressTask() {
        double a = 12345.6789;
        double b = 98765.4321;
        while (!Thread.currentThread().isInterrupted()) {
            a = Math.sin(a) * Math.cos(b) + Math.tan(a) * Math.log(b + 1);
            b = Math.pow(a, 1.0001) * Math.sqrt(Math.abs(a + b));
        }
    }

    /**
     * 创建窗口并显示 GPU 渲染画面
     */
    private static void createAndShowGui() {
        JFrame frame = new JFrame("GPU 实时渲染压测窗口（Mac 可见）");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(WINDOW_SIZE, WINDOW_SIZE);
        frame.setLocationRelativeTo(null); // 居中

        // 渲染面板
        GpuRenderPanel panel = new GpuRenderPanel();
        frame.add(panel);

        frame.setVisible(true);

        // 启动 GPU 渲染循环（高频刷新 = 显卡高负载）
        new Thread(() -> {
            while (true) {
                panel.repaint(); // 不断重绘 → 显卡疯狂工作
                try {
                    Thread.sleep(8); // 约 120 FPS 超高刷
                } catch (InterruptedException e) {
                    break;
                }
            }
        }, "GPU-Render-Thread").start();
    }

    /**
     * GPU 渲染面板（实时画图形，Mac 可见）
     */
    static class GpuRenderPanel extends JPanel {
        private final BufferedImage image;
        private final Graphics2D g2d;

        public GpuRenderPanel() {
            setDoubleBuffered(true); // 开启双缓冲，显卡更忙
            image = new BufferedImage(WINDOW_SIZE, WINDOW_SIZE, BufferedImage.TYPE_INT_ARGB);
            g2d = image.createGraphics();
            // 开启抗锯齿 → 显卡负载更高
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            // ---------- GPU 高强度渲染开始 ----------
            // 随机背景色
            g2d.setColor(new Color((int) (Math.random() * 0x1000000)));
            g2d.fillRect(0, 0, WINDOW_SIZE, WINDOW_SIZE);

            // 画大量随机图形（显卡核心压力）
            for (int i = 0; i < 120; i++) {
                g2d.setColor(new Color((int) (Math.random() * 0x1000000)));
                int x = (int) (Math.random() * WINDOW_SIZE);
                int y = (int) (Math.random() * WINDOW_SIZE);
                int w = (int) (Math.random() * 400);
                int h = (int) (Math.random() * 400);
                g2d.drawOval(x, y, w, h);
                g2d.drawRect(x, y, w, h);
                g2d.drawLine(x, y, x + w, y + h);
            }

            // 读取像素 → 强制显存读写
            image.getRGB(0, 0, WINDOW_SIZE, WINDOW_SIZE, null, 0, WINDOW_SIZE);

            // 绘制到窗口
            g.drawImage(image, 0, 0, null);
        }
    }
}