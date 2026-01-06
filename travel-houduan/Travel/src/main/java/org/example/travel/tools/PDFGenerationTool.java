package org.example.travel.tools;

import cn.hutool.http.HttpUtil;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.travel.manager.CosManager;
import org.example.travel.model.entity.GeneratedDocument;
import org.example.travel.service.GeneratedDocumentService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * PDF生成工具
 * 支持生成包含文字和图片的PDF文档，并上传到对象存储
 */
@Component
@Slf4j
public class PDFGenerationTool {

    @Resource
    private CosManager cosManager;

    @Resource
    private GeneratedDocumentService generatedDocumentService;

    // 当前用户ID（由ChatService在调用前设置）
    private static final ThreadLocal<Long> currentUserId = new ThreadLocal<>();
    private static final ThreadLocal<Long> currentProjectId = new ThreadLocal<>();

    public static void setCurrentUserId(Long userId) {
        currentUserId.set(userId);
    }

    public static void setCurrentProjectId(Long projectId) {
        currentProjectId.set(projectId);
    }

    public static void clearContext() {
        currentUserId.remove();
        currentProjectId.remove();
    }

    @Tool(description = """
            生成PDF游览报告文档。当用户要求生成PDF、导出报告、保存游览记录时调用此工具。
            可以包含标题、正文内容和图片。生成后会自动保存并返回下载链接。
            """)
    public String generatePDF(
            @ToolParam(description = "PDF文档标题") String title,
            @ToolParam(description = "PDF正文内容，支持多段落，用\\n分隔") String content,
            @ToolParam(description = "图片URL列表，用逗号分隔，可为空") String imageUrls
    ) {
        File tempFile = null;
        try {
            // 创建临时文件
            tempFile = File.createTempFile("travel_report_", ".pdf");
            
            // 创建PDF
            PdfWriter writer = new PdfWriter(new FileOutputStream(tempFile));
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // 设置中文字体（使用iText 9.x的font-asian包）
            PdfFont font;
            try {
                font = PdfFontFactory.createFont("STSong-Light", "UniGB-UCS2-H");
            } catch (Exception e) {
                // 如果STSong-Light不可用，尝试使用系统字体
                log.warn("STSong-Light字体不可用，尝试使用默认字体", e);
                font = PdfFontFactory.createFont();
            }

            // 添加标题
            Paragraph titlePara = new Paragraph(title)
                    .setFont(font)
                    .setFontSize(24)
                    .setFontColor(ColorConstants.BLACK)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(titlePara);

            // 添加生成时间
            Paragraph timePara = new Paragraph("生成时间：" + new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()))
                    .setFont(font)
                    .setFontSize(10)
                    .setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setMarginBottom(20);
            document.add(timePara);

            // 添加正文内容
            if (content != null && !content.isEmpty()) {
                String[] paragraphs = content.split("\\\\n|\\n");
                for (String para : paragraphs) {
                    if (!para.trim().isEmpty()) {
                        Paragraph p = new Paragraph(para.trim())
                                .setFont(font)
                                .setFontSize(12)
                                .setMarginBottom(10)
                                .setFirstLineIndent(24);
                        document.add(p);
                    }
                }
            }

            // 添加图片
            if (imageUrls != null && !imageUrls.trim().isEmpty()) {
                document.add(new Paragraph("\n"));
                document.add(new Paragraph("相关图片")
                        .setFont(font)
                        .setFontSize(16)
                        .setFontColor(ColorConstants.BLACK)
                        .setMarginTop(20)
                        .setMarginBottom(10));

                String[] urls = imageUrls.split(",");
                for (String url : urls) {
                    url = url.trim();
                    // 校验URL格式：必须以http://或https://开头
                    if (url.isEmpty() || (!url.startsWith("http://") && !url.startsWith("https://"))) {
                        log.warn("跳过无效的图片URL: {}", url);
                        continue;
                    }
                    try {
                        // 下载图片
                        byte[] imageBytes = HttpUtil.downloadBytes(url);
                        if (imageBytes == null || imageBytes.length == 0) {
                            log.warn("图片下载为空: {}", url);
                            continue;
                        }
                        Image image = new Image(ImageDataFactory.create(imageBytes));
                        
                        // 设置图片大小（最大宽度400）
                        float maxWidth = 400;
                        if (image.getImageWidth() > maxWidth) {
                            image.scaleToFit(maxWidth, maxWidth * image.getImageHeight() / image.getImageWidth());
                        }
                        
                        image.setMarginBottom(10);
                        document.add(image);
                    } catch (Exception e) {
                        log.warn("图片加载失败: {}", url, e);
                        // 不在PDF中显示错误信息，静默跳过
                    }
                }
            }

            // 添加页脚
            document.add(new Paragraph("\n\n"));
            document.add(new Paragraph("—— 非遗文化智能伴游助手 ——")
                    .setFont(font)
                    .setFontSize(10)
                    .setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER));

            document.close();

            // 上传到COS
            String cosKey = "pdf/" + UUID.randomUUID() + ".pdf";
            cosManager.putObject(cosKey, tempFile);
            
            // 获取访问URL（需要根据实际COS配置调整）
            String fileUrl = cosManager.getObjectUrl(cosKey);
            if (fileUrl == null || fileUrl.isEmpty()) {
                // 如果CosManager没有getObjectUrl方法，使用默认URL格式
                fileUrl = "https://travel-1356886289.cos.ap-guangzhou.myqcloud.com/" + cosKey;
            }

            // 保存生成记录
            Long userId = currentUserId.get();
            Long projectId = currentProjectId.get();
            if (userId != null) {
                GeneratedDocument doc = new GeneratedDocument();
                doc.setUserId(userId);
                doc.setProjectId(projectId);
                doc.setFileUrl(fileUrl);
                doc.setCreatedAt(new Date());
                generatedDocumentService.save(doc);
            }

            log.info("PDF生成成功: {}", fileUrl);
            return "PDF文档已生成，下载链接：" + fileUrl;

        } catch (Exception e) {
            log.error("PDF生成失败", e);
            return "PDF生成失败：" + e.getMessage();
        } finally {
            // 删除临时文件
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }
}
