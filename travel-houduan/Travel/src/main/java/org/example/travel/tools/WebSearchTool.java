package org.example.travel.tools;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 网页搜索工具（爬虫实现）
 * 爬取百度搜索结果
 * 
 * 跨平台支持：
 * 
 * 只需要服务器能访问互联网即可
 */
@Slf4j
@Component
public class WebSearchTool {

    private static final String BAIDU_SEARCH_URL = "https://www.baidu.com/s";
    private static final String USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
    
    // 请求限流：避免频繁请求被封 IP
    private volatile long lastRequestTime = 0;
    private static final long MIN_REQUEST_INTERVAL = 2000; // 2秒
    
    // 统计信息
    private volatile int requestCount = 0;
    private volatile int successCount = 0;
    private volatile int failCount = 0;
    
    @Tool(description = """
            搜索非遗相关的实时信息和知识。
            使用百度搜索引擎爬取搜索结果。
            
            适用场景：
            1. 查询非遗项目的最新动态和新闻
            2. 搜索非遗传承人的信息
            3. 查找非遗相关的活动和展览
            4. 获取非遗项目的详细介绍
            5. 搜索非遗文化的历史背景
            
            注意：
            - 跨平台支持（Windows/Linux/macOS）
            - 需要服务器能访问互联网
            - 建议搜索词包含"非遗"、"传统文化"等关键词
            - 返回前5条搜索结果
            - 搜索失败时自动降级到备用信息
            """)
    public String searchWeb(
            @JsonProperty(required = true)
            @JsonPropertyDescription("搜索关键词，建议包含'非遗'、'传统文化'等相关词汇")
            String query
    ) {
        requestCount++;
        log.info("执行网页搜索 [#{}/成功:{}/失败:{}]: query={}", 
            requestCount, successCount, failCount, query);
        
        try {
            // 请求限流
            waitForRateLimit();
            
            // 爬取搜索结果
            String searchUrl = BAIDU_SEARCH_URL + "?wd=" + URLEncoder.encode(query, StandardCharsets.UTF_8);
            log.debug("搜索URL: {}", searchUrl);
            
            Document doc = Jsoup.connect(searchUrl)
                    .userAgent(USER_AGENT)
                    .timeout(8000)
                    .referrer("https://www.baidu.com")
                    .ignoreHttpErrors(true)
                    .get();
            
            List<SearchResult> results = parseSearchResults(doc);
            
            if (results.isEmpty()) {
                log.warn("未找到搜索结果，可能被反爬虫拦截");
                failCount++;
                return "网络搜索未找到结果，返回预设信息：\n\n" + getFallbackInfo(query);
            }
            
            successCount++;
            log.info("搜索成功，找到 {} 条结果", results.size());
            return formatResults(results, query);
            
        } catch (java.net.UnknownHostException e) {
            log.error("网络连接失败: {}", e.getMessage());
            failCount++;
            return "⚠️ 网络搜索失败：无法连接到搜索引擎\n" +
                   "可能原因：服务器无法访问互联网或DNS解析失败\n\n" +
                   "返回预设信息：\n\n" + getFallbackInfo(query);
            
        } catch (java.net.SocketTimeoutException e) {
            log.error("网络请求超时: {}", e.getMessage());
            failCount++;
            return "⚠️ 网络搜索超时，请稍后重试\n\n" +
                   "返回预设信息：\n\n" + getFallbackInfo(query);
            
        } catch (Exception e) {
            log.error("网页搜索失败: query={}", query, e);
            failCount++;
            return "⚠️ 网络搜索遇到问题\n\n" +
                   "返回预设信息：\n\n" + getFallbackInfo(query);
        }
    }
    
    /**
     * 请求限流
     */
    private void waitForRateLimit() {
        long now = System.currentTimeMillis();
        long timeSinceLastRequest = now - lastRequestTime;
        
        if (timeSinceLastRequest < MIN_REQUEST_INTERVAL) {
            long waitTime = MIN_REQUEST_INTERVAL - timeSinceLastRequest;
            log.debug("请求限流，等待 {}ms", waitTime);
            try {
                Thread.sleep(waitTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        lastRequestTime = System.currentTimeMillis();
    }
    
    /**
     * 解析百度搜索结果
     */
    private List<SearchResult> parseSearchResults(Document doc) {
        List<SearchResult> results = new ArrayList<>();
        
        Elements resultElements = doc.select("#content_left > div.result");
        if (resultElements.isEmpty()) {
            resultElements = doc.select(".result");
        }
        
        log.debug("找到 {} 个搜索结果元素", resultElements.size());
        
        int count = 0;
        for (Element element : resultElements) {
            if (count >= 5) break;
            
            try {
                SearchResult result = new SearchResult();
                
                Element titleElement = element.selectFirst("h3");
                if (titleElement != null) {
                    result.title = titleElement.text();
                }
                
                Element abstractElement = element.selectFirst(".c-abstract");
                if (abstractElement == null) {
                    abstractElement = element.selectFirst(".c-span-last");
                }
                if (abstractElement != null) {
                    result.snippet = abstractElement.text();
                }
                
                Element linkElement = element.selectFirst("a");
                if (linkElement != null) {
                    result.url = linkElement.attr("href");
                }
                
                if (result.title != null && !result.title.isEmpty()) {
                    results.add(result);
                    count++;
                }
                
            } catch (Exception e) {
                log.warn("解析单个搜索结果失败", e);
            }
        }
        
        return results;
    }
    
    /**
     * 格式化搜索结果
     */
    private String formatResults(List<SearchResult> results, String query) {
        StringBuilder sb = new StringBuilder();
        sb.append("🔍 搜索 \"").append(query).append("\" 的结果：\n\n");
        
        for (int i = 0; i < results.size(); i++) {
            SearchResult result = results.get(i);
            sb.append(i + 1).append(". ");
            
            if (result.title != null) {
                sb.append(result.title).append("\n");
            }
            
            if (result.snippet != null && !result.snippet.isEmpty()) {
                sb.append("   ").append(result.snippet).append("\n");
            }
            
            if (result.url != null && !result.url.isEmpty()) {
                sb.append("   来源: ").append(result.url).append("\n");
            }
            
            sb.append("\n");
        }
        
        sb.append("💡 提示：以上信息来自网络搜索，仅供参考。");
        
        return sb.toString();
    }
    
    /**
     * 获取备用信息
     */
    private String getFallbackInfo(String query) {
        String lowerQuery = query.toLowerCase();
        
        if (lowerQuery.contains("粤剧") || lowerQuery.contains("戏曲")) {
            return """
                📚 粤剧相关信息：
                
                粤剧是广东省的传统戏曲剧种，被誉为"南国红豆"。
                
                主要特点：
                • 起源于明末清初，至今已有300多年历史
                • 唱腔优美，融合了广东音乐和民间曲调
                • 表演形式丰富，包括唱、做、念、打
                • 服饰华丽，脸谱精美
                • 2009年被联合国教科文组织列入人类非物质文化遗产代表作名录
                
                著名剧目：《帝女花》、《紫钗记》、《牡丹亭惊梦》
                代表人物：薛觉先、马师曾、红线女等粤剧大师
                """;
        }
        
        if (lowerQuery.contains("广绣") || lowerQuery.contains("刺绣")) {
            return """
                📚 广绣相关信息：
                
                广绣是广东地区的传统刺绣工艺，与苏绣、湘绣、蜀绣并称为中国四大名绣。
                
                主要特点：
                • 色彩鲜艳明快，构图饱满
                • 针法多样，包括平绣、垫绣、贴绣等
                • 题材丰富，以花鸟、龙凤、人物为主
                • 善用金银线，富丽堂皇
                
                历史渊源：
                • 起源于唐代，兴盛于明清
                • 清代成为贡品，享誉海内外
                • 2006年列入国家级非物质文化遗产名录
                """;
        }
        
        return """
            📚 非遗文化相关信息：
            
            中国非物质文化遗产是中华民族优秀传统文化的重要组成部分。
            
            主要类别：
            • 传统口头文学以及作为其载体的语言
            • 传统美术、书法、音乐、舞蹈、戏剧、曲艺和杂技
            • 传统技艺、医药和历法
            • 传统礼仪、节庆等民俗
            • 传统体育和游艺
            
            保护措施：
            • 建立国家、省、市、县四级非遗名录体系
            • 认定代表性传承人
            • 设立传承基地和展示馆
            • 开展非遗进校园、进社区活动
            """;
    }
    
    /**
     * 搜索结果内部类
     */
    private static class SearchResult {
        String title;
        String snippet;
        String url;
    }
}
