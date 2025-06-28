package com.skymmer.utils;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.TextAlignment;
import com.skymmer.pojo.Travel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class FileExport {

    @Value("${app.export.outputDir}")
    private String outputDir;

    // 声明中文字体变量
    private PdfFont simHeiFont;

    public void exportToPDF(boolean isRetainLocation, boolean isRetainTime,
                            boolean isRetainParams, Travel travel, String exportPath) throws IOException {
        Path outputPath;
        if(exportPath != null){
            outputPath = Paths.get(exportPath);
            if (!Files.exists(outputPath)) {
                Files.createDirectories(outputPath);
            }
        }else {
            // 确保输出目录存在
            outputPath = Paths.get(outputDir);
            if (!Files.exists(outputPath)) {
                Files.createDirectories(outputPath);
            }
        }

        // 生成唯一文件名
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String fileName = "export_" + sdf.format(new Date()) + ".png";
        String outputFile = outputPath.resolve(fileName).toString();

        // 加载中文字体（只在第一次使用时加载）
        if (simHeiFont == null) {
            loadChineseFont();
        }

        // 使用try-with-resources自动关闭所有资源
        try (PdfWriter writer = new PdfWriter(outputFile);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {

            // 设置页面边距
            document.setMargins(36, 36, 36, 36);

            // 设置文档默认字体（中文）
            if (simHeiFont != null) {
                document.setFont(simHeiFont);
            }

            // 添加图片（如果有）
            if (travel.getImagePath() != null && !travel.getImagePath().isEmpty()) {
                Image image = new Image(
                        ImageDataFactory.create(travel.getImagePath()));

                // 自动调整图片大小以适应页面宽度
                float availableWidth = pdf.getDefaultPageSize().getWidth() -
                        document.getLeftMargin() -
                        document.getRightMargin();
                image.setMaxWidth(availableWidth);
                image.setTextAlignment(TextAlignment.CENTER);

                document.add(image);
                document.add(new Paragraph("")); // 添加空行
            }

            // 添加位置信息
            if (isRetainLocation) {
                document.add(createSectionTitle("位置信息"));
                document.add(createInfoLine("纬度:", String.valueOf(travel.getLatitude())));
                document.add(createInfoLine("经度:", String.valueOf(travel.getLongitude())));
                document.add(createInfoLine("位置:", travel.getLocation()));
                document.add(new Paragraph(""));
            }

            // 添加时间信息
            if (isRetainTime) {
                document.add(createSectionTitle("拍摄时间"));
                document.add(createInfoLine("拍摄时间:", String.valueOf(travel.getTakenTime())));
                document.add(new Paragraph(""));
            }

            // 添加参数信息
            if (isRetainParams) {
                document.add(createSectionTitle("拍摄参数"));
                document.add(createInfoLine("制造商:", travel.getMake()));
                document.add(createInfoLine("设备类型:", travel.getModel()));
                document.add(createInfoLine("图片类型:", travel.getType()));
                document.add(createInfoLine("图片大小:", travel.getWidth() + "×" + travel.getHeight()));
                document.add(createInfoLine("光圈值:", travel.getFnumber()));
                document.add(createInfoLine("曝光时间:", travel.getExposureTime()));
                document.add(createInfoLine("感光度:", travel.getIso()));
                document.add(new Paragraph(""));
            }

            // 添加内容描述
            if (travel.getContent() != null && !travel.getContent().isEmpty()) {
                document.add(createSectionTitle("游记"));
                // 使用中文段落创建方法
                document.add(createChineseParagraph(travel.getContent()));
            }

            System.out.println("PDF导出成功，文件路径：" + outputFile);
        } catch (IOException e) {
            System.err.println("PDF导出失败: " + e.getMessage());
            throw e; // 重新抛出异常以便上层处理
        }
    }

    // 字体加载方法
    private void loadChineseFont() throws IOException {
        try {
            ClassPathResource fontResource = new ClassPathResource("fonts/SimHei.ttf");
            if (fontResource.exists()) {
                // 获取字体文件的绝对路径
                String fontPath = fontResource.getFile().getAbsolutePath();

                // 使用文件路径创建字体（7.2.5兼容方式）
                simHeiFont = PdfFontFactory.createFont(
                        fontPath,
                        PdfEncodings.IDENTITY_H,
                        PdfFontFactory.EmbeddingStrategy.FORCE_EMBEDDED
                );
            } else {
                System.err.println("警告：未找到字体文件 fonts/SimHei.ttf");
            }
        } catch (IOException e) {
            System.err.println("加载中文字体失败: " + e.getMessage());
            throw e;
        }
    }

    // 创建章节标题（使用中文）
    private Paragraph createSectionTitle(String title) {
        Text text = new Text(title);
        text.setBold();
        text.setFontSize(14);
        return new Paragraph(text);
    }

    // 创建信息行（使用中文）
    private Paragraph createInfoLine(String label, String value) {
        Text labelText = new Text(label);
        labelText.setBold();

        Text valueText = new Text(value == null ? "未知" : value);

        return new Paragraph().add(labelText).add(valueText);
    }

    // 创建中文段落（处理换行）
    private Paragraph createChineseParagraph(String content) {
        // 按换行符分割内容
        String[] lines = content.split("\n");
        Paragraph paragraph = new Paragraph();

        for (String line : lines) {
            paragraph.add(line);
            // 添加换行符（相当于回车）
            paragraph.add(new Text("\n"));
        }
        return paragraph;
    }


    public String exportToPNG(boolean isRetainLocation, boolean isRetainTime,
                              boolean isRetainParams, Travel travel, String exportPath) throws IOException {
        Path outputPath;
        if(exportPath != null){
            outputPath = Paths.get(exportPath);
            if (!Files.exists(outputPath)) {
                Files.createDirectories(outputPath);
            }
        }else {
            // 确保输出目录存在
            outputPath = Paths.get(outputDir);
            if (!Files.exists(outputPath)) {
                Files.createDirectories(outputPath);
            }
        }

        // 生成唯一文件名
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String fileName = "export_" + sdf.format(new Date()) + ".png";
        String outputFile = outputPath.resolve(fileName).toString();

        try {
            // 1. 创建基础图像
            BufferedImage image = createBaseImage(travel);

            // 2. 获取图形上下文
            Graphics2D g2d = image.createGraphics();

            // 3. 设置渲染质量
            setRenderingQuality(g2d);

            // 4. 绘制背景
            drawBackground(g2d, image.getWidth(), image.getHeight());

            // 5. 绘制内容
            drawContent(g2d, image.getWidth(), image.getHeight(),
                    isRetainLocation, isRetainTime, isRetainParams, travel);

            // 6. 释放资源
            g2d.dispose();

            // 7. 保存图像
            ImageIO.write(image, "PNG", new File(outputFile));

            System.out.println("PNG导出成功，文件路径：" + outputFile);
            return outputFile;
        } catch (IOException e) {
            System.err.println("PNG导出失败: " + e.getMessage());
            throw e;
        }
    }

    /**
     * 创建基础图像（增加高度以容纳更多文本）
     */
    private BufferedImage createBaseImage(Travel travel) throws IOException {
        int baseWidth = 1200; // 基础宽度
        int baseHeight = 1800; // 基础高度

        // 如果Travel对象中有图片，则使用该图片作为基础
        if (travel.getImagePath() != null && !travel.getImagePath().isEmpty()) {
            File imgFile = new File(travel.getImagePath());
            if (imgFile.exists()) {
                BufferedImage original = ImageIO.read(imgFile);

                // 创建新的图像，高度增加50%用于添加文本
                baseWidth = original.getWidth();
                baseHeight = (int) (original.getHeight() * 1.5);
            }
        }

        return new BufferedImage(baseWidth, baseHeight, BufferedImage.TYPE_INT_RGB);
    }

    /**
     * 设置图形渲染质量
     */
    private void setRenderingQuality(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    }

    /**
     * 绘制背景
     */
    private void drawBackground(Graphics2D g2d, int width, int height) {
        // 绘制渐变背景
        GradientPaint gradient = new GradientPaint(
                0, 0, new Color(245, 250, 255),
                width, height, new Color(225, 235, 255)
        );
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, width, height);

        // 添加水印效果
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.05f));
        g2d.setColor(Color.GRAY);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 180)); // 增大水印字体
        FontMetrics fm = g2d.getFontMetrics();
        String watermark = "SKYMMER";
        int x = (width - fm.stringWidth(watermark)) / 2;
        int y = (height + fm.getHeight()) / 2;
        g2d.drawString(watermark, x, y);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    }

    /**
     * 绘制内容（优化字体大小）
     */
    private void drawContent(Graphics2D g2d, int width, int height,
                             boolean isRetainLocation, boolean isRetainTime,
                             boolean isRetainParams, Travel travel) throws IOException {
        int yOffset = 40; // 增加顶部间距

        // 1. 绘制标题（增大字体）
        g2d.setColor(new Color(40, 40, 90));
        g2d.setFont(new Font("SansSerif", Font.BOLD, 100)); //
        String title = "旅行照片信息卡";
        FontMetrics titleFm = g2d.getFontMetrics();
        int titleX = (width - titleFm.stringWidth(title)) / 2;
        g2d.drawString(title, titleX, yOffset + titleFm.getAscent());
        yOffset += titleFm.getHeight() + 30; // 增加间距

        // 2. 绘制分隔线（加粗）
        g2d.setColor(new Color(70, 130, 180));
        g2d.setStroke(new BasicStroke(3)); // 加粗线条
        g2d.drawLine(50, yOffset, width - 50, yOffset);
        yOffset += 40; // 增加间距

        int imageX = 50;//图片起始绘制位置
        // 3. 绘制图片
        if (travel.getImagePath() != null && !travel.getImagePath().isEmpty()) {
            File imgFile = new File(travel.getImagePath());
            if (imgFile.exists()) {
                BufferedImage originalImage = ImageIO.read(imgFile);

                // 计算缩放比例，最大宽度为图像宽度的70%（缩小图片）
                int maxWidth = (int) (width * 0.7);
                double scaleFactor = Math.min(1.0, (double) maxWidth / originalImage.getWidth());

                int scaledWidth = (int) (originalImage.getWidth() * scaleFactor);
                int scaledHeight = (int) (originalImage.getHeight() * scaleFactor);

                // 计算图片位置（居中）
                imageX = (width - scaledWidth) / 2;

                // 绘制图片边框
                g2d.setColor(Color.WHITE);
                g2d.fillRect(imageX - 8, yOffset - 8, scaledWidth + 16, scaledHeight + 16);
                g2d.setColor(new Color(70, 130, 180));
                g2d.setStroke(new BasicStroke(3)); // 加粗边框
                g2d.drawRect(imageX - 8, yOffset - 8, scaledWidth + 16, scaledHeight + 16);

                // 绘制图片
                g2d.drawImage(originalImage, imageX, yOffset, scaledWidth, scaledHeight, null);

                yOffset += scaledHeight + 50; // 增加图片下方间距
            }
        }

        // 4. 绘制信息部分（增大字体）
        int infoStartY = yOffset;
        int infoWidth = width - 100;
        int infoX = imageX;

        // 设置信息文本字体（增大字体）
        Font infoFont = new Font("SansSerif", Font.PLAIN, 28);
        g2d.setFont(infoFont);
        FontMetrics infoFm = g2d.getFontMetrics();
        int lineHeight = infoFm.getHeight() + 10; // 增加行高

        // 位置信息
        if (isRetainLocation) {
            drawInfoSection(g2d, "位置信息", infoX, yOffset, infoWidth, infoFm);
            yOffset += infoFm.getHeight() + 15; // 增加间距

            yOffset = drawInfoLine(g2d, "纬度:", String.valueOf(travel.getLatitude()), infoX, yOffset, infoWidth, lineHeight);
            yOffset = drawInfoLine(g2d, "经度:", String.valueOf(travel.getLongitude()), infoX, yOffset, infoWidth, lineHeight);
            yOffset = drawInfoLine(g2d, "位置:", travel.getLocation(), infoX, yOffset, infoWidth, lineHeight);

            yOffset += 25; // 增加间距
        }

        // 时间信息
        if (isRetainTime) {
            drawInfoSection(g2d, "拍摄时间", infoX, yOffset, infoWidth, infoFm);
            yOffset += infoFm.getHeight() + 15; // 增加间距

            yOffset = drawInfoLine(g2d, "拍摄时间:", String.valueOf(travel.getTakenTime()), infoX, yOffset, infoWidth, lineHeight);

            yOffset += 25; // 增加间距
        }

        // 参数信息
        if (isRetainParams) {
            drawInfoSection(g2d, "拍摄参数", infoX, yOffset, infoWidth, infoFm);
            yOffset += infoFm.getHeight() + 15; // 增加间距

            yOffset = drawInfoLine(g2d, "制造商:", travel.getMake(), infoX, yOffset, infoWidth, lineHeight);
            yOffset = drawInfoLine(g2d, "设备类型:", travel.getModel(), infoX, yOffset, infoWidth, lineHeight);
            yOffset = drawInfoLine(g2d, "图片类型:", travel.getType(), infoX, yOffset, infoWidth, lineHeight);
            yOffset = drawInfoLine(g2d, "图片大小:", travel.getWidth() + "×" + travel.getHeight(), infoX, yOffset, infoWidth, lineHeight);
            yOffset = drawInfoLine(g2d, "光圈值:", travel.getFnumber(), infoX, yOffset, infoWidth, lineHeight);
            yOffset = drawInfoLine(g2d, "曝光时间:", travel.getExposureTime(), infoX, yOffset, infoWidth, lineHeight);
            yOffset = drawInfoLine(g2d, "感光度:", travel.getIso(), infoX, yOffset, infoWidth, lineHeight);

            yOffset += 25; // 增加间距
        }

        // 5. 绘制内容描述（增大字体）
        if (travel.getContent() != null && !travel.getContent().isEmpty()) {
            drawInfoSection(g2d, "内容描述", infoX, yOffset, infoWidth, infoFm);
            yOffset += infoFm.getHeight() + 15; // 增加间距

            // 设置内容字体（增大字体）
            Font contentFont = new Font("Serif", Font.PLAIN, 50); // 从16增加到26
            g2d.setFont(contentFont);
            FontMetrics contentFm = g2d.getFontMetrics();

            // 绘制多行文本
            String content = travel.getContent();
            int contentLineHeight = contentFm.getHeight() + 8; // 增加行高
            int maxWidth = width - 100;
            int maxLines = (height - yOffset - 50) / contentLineHeight; // 保留底部空间

            int lineCount = 0;
            int currentY = yOffset + contentFm.getAscent();
            int start = 0;
            int end;

            while (start < content.length() && lineCount < maxLines) {
                // 找到适合的换行位置
                end = start + 1;
                while (end <= content.length()) {
                    if (contentFm.stringWidth(content.substring(start, end)) > maxWidth) {
                        // 回退到上一个空格
                        int lastSpace = content.substring(start, Math.min(end, content.length())).lastIndexOf(' ');
                        if (lastSpace > 0) {
                            end = start + lastSpace;
                        }
                        break;
                    }
                    end++;
                }

                if (end > content.length()) {
                    end = content.length();
                }

                String line = content.substring(start, end).trim();
                if (!line.isEmpty()) {
                    g2d.setColor(new Color(30, 30, 30));
                    g2d.drawString(line, infoX, currentY);
                    currentY += contentLineHeight;
                    lineCount++;
                }

                start = end;

                // 跳过空格
                while (start < content.length() && Character.isWhitespace(content.charAt(start))) {
                    start++;
                }
            }
        }

        // 6. 绘制底部版权信息（增大字体）
        g2d.setColor(new Color(80, 80, 80));
        g2d.setFont(new Font("SansSerif", Font.BOLD, 100)); // 从12增加到18
        String footer = "© " + new SimpleDateFormat("yyyy").format(new Date()) + " Skymmer - 旅行信息导出";
        int footerX = width - g2d.getFontMetrics().stringWidth(footer) - 40;
        int footerY = height - 30;
        g2d.drawString(footer, footerX, footerY);
    }

    /**
     * 绘制信息部分标题（增大字体）
     */
    private void drawInfoSection(Graphics2D g2d, String title, int x, int y, int width, FontMetrics fm) {
        g2d.setColor(new Color(60, 120, 200)); // 更深的蓝色
        Font boldFont = fm.getFont().deriveFont(Font.BOLD, 32); // 从18增加到32
        g2d.setFont(boldFont);
        FontMetrics boldFm = g2d.getFontMetrics();
        g2d.drawString(title, x, y + boldFm.getAscent());

        // 恢复字体
        g2d.setFont(fm.getFont());
    }

    /**
     * 绘制信息行（增大字体和间距）
     * @return 新的y坐标
     */
    private int drawInfoLine(Graphics2D g2d, String label, String value, int x, int y, int width, int lineHeight) {
        if (value == null) {
            value = "未知";
        }

        // 绘制标签
        g2d.setColor(new Color(50, 50, 50));
        g2d.setFont(g2d.getFont().deriveFont(Font.BOLD)); // 标签加粗
        g2d.drawString(label, x, y + g2d.getFontMetrics().getAscent());

        // 绘制值
        g2d.setColor(new Color(30, 30, 30));
        g2d.setFont(g2d.getFont().deriveFont(Font.PLAIN)); // 值正常字体
        g2d.drawString(value, x + 180, y + g2d.getFontMetrics().getAscent()); // 增加标签和值的间距

        return y + lineHeight; // 使用更大的行高
    }
}