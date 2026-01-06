package org.example.travel.controller;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.travel.common.BaseResponse;
import org.example.travel.common.Result;
import org.example.travel.model.entity.*;
import org.example.travel.service.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 管理后台统计接口
 * 完全基于用户会话数据统计
 */
@RestController
@RequestMapping("/admin/statistics")
public class AdminStatisticsController {

    @Resource
    private UserService userService;

    @Resource
    private ChatConversationService conversationService;

    @Resource
    private ChatMessageService messageService;

    @Resource
    private IchProjectService projectService;

    /**
     * 获取概览数据（顶部卡片）
     * - 总用户数：user表总数
     * - 今日活跃(DAU)：今日有会话活动的独立用户数
     * - 累计对话数：chat_conversation表总数
     * - 累计消息数：chat_message表总数
     */
    @GetMapping("/overview")
    public BaseResponse<OverviewData> getOverview() {
        // 总用户数
        long totalUsers = userService.count();
        
        // 今日活跃用户数 - 今天有会话更新的独立用户数
        Date today = DateUtil.beginOfDay(new Date());
        QueryWrapper<ChatConversation> dauWrapper = new QueryWrapper<>();
        dauWrapper.select("DISTINCT user_id")
                .and(w -> w.ge("created_at", today).or().ge("updated_at", today));
        long dau = conversationService.count(dauWrapper);
        
        // 累计对话数
        long totalConversations = conversationService.count();
        
        // 累计消息数
        long totalMessages = messageService.count();
        
        OverviewData data = OverviewData.builder()
                .totalUsers(totalUsers)
                .dau(dau)
                .totalConversations(totalConversations)
                .totalMessages(totalMessages)
                .build();
        
        return Result.success(data);
    }

    /**
     * 获取近七日流量趋势
     * - PV：每日用户消息数（role=user的消息）
     * - UV：每日有会话活动的独立用户数
     */
    @GetMapping("/traffic-trend")
    public BaseResponse<List<TrafficData>> getTrafficTrend() {
        List<TrafficData> result = new ArrayList<>();
        
        for (int i = 6; i >= 0; i--) {
            Date date = DateUtil.offsetDay(new Date(), -i);
            Date startOfDay = DateUtil.beginOfDay(date);
            Date endOfDay = DateUtil.endOfDay(date);
            
            // PV: 当日用户发送的消息数
            long pv = messageService.lambdaQuery()
                    .eq(ChatMessage::getRole, "user")
                    .ge(ChatMessage::getCreatedAt, startOfDay)
                    .le(ChatMessage::getCreatedAt, endOfDay)
                    .count();
            
            // UV: 当日有会话活动的独立用户数
            QueryWrapper<ChatConversation> uvWrapper = new QueryWrapper<>();
            uvWrapper.select("DISTINCT user_id")
                    .and(w -> w
                            .between("created_at", startOfDay, endOfDay)
                            .or()
                            .between("updated_at", startOfDay, endOfDay));
            long uv = conversationService.count(uvWrapper);
            
            result.add(TrafficData.builder()
                    .date(DateUtil.format(date, "MM-dd"))
                    .dayOfWeek(getDayOfWeek(date))
                    .pv(pv)
                    .uv(uv)
                    .build());
        }
        
        return Result.success(result);
    }

    /**
     * 获取热门非遗项目 Top5
     * 通过分析用户消息内容中提到的项目名称来统计
     */
    @GetMapping("/hot-projects")
    public BaseResponse<List<HotProjectData>> getHotProjects() {
        // 获取所有非遗项目
        List<IchProject> allProjects = projectService.list();
        if (allProjects.isEmpty()) {
            return Result.success(List.of());
        }
        
        // 获取最近30天的用户消息
        Date thirtyDaysAgo = DateUtil.offsetDay(new Date(), -30);
        List<ChatMessage> userMessages = messageService.lambdaQuery()
                .eq(ChatMessage::getRole, "user")
                .ge(ChatMessage::getCreatedAt, thirtyDaysAgo)
                .isNotNull(ChatMessage::getContent)
                .list();
        
        // 统计每个项目被提及的次数
        Map<Long, Long> mentionCount = new HashMap<>();
        for (IchProject project : allProjects) {
            mentionCount.put(project.getId(), 0L);
        }
        
        for (ChatMessage msg : userMessages) {
            String content = msg.getContent();
            if (content == null || content.isEmpty()) continue;
            
            for (IchProject project : allProjects) {
                // 检查消息中是否包含项目名称
                if (content.contains(project.getName())) {
                    mentionCount.merge(project.getId(), 1L, Long::sum);
                }
                // 也检查类别关键词
                if (project.getCategory() != null && content.contains(project.getCategory())) {
                    mentionCount.merge(project.getId(), 1L, Long::sum);
                }
            }
        }
        
        // 按提及次数排序，取Top5
        List<HotProjectData> result = allProjects.stream()
                .map(p -> HotProjectData.builder()
                        .projectId(p.getId())
                        .name(p.getName())
                        .category(p.getCategory())
                        .visitCount(mentionCount.getOrDefault(p.getId(), 0L))
                        .build())
                .sorted((a, b) -> Long.compare(b.getVisitCount(), a.getVisitCount()))
                .limit(5)
                .collect(Collectors.toList());
        
        return Result.success(result);
    }

    /**
     * 获取用户兴趣分布
     * 通过分析用户消息内容中的关键词来统计兴趣类别
     */
    @GetMapping("/interest-distribution")
    public BaseResponse<List<InterestData>> getInterestDistribution() {
        // 定义兴趣类别及其关键词
        Map<String, List<String>> categoryKeywords = new LinkedHashMap<>();
        categoryKeywords.put("传统美术", List.of("广绣", "刺绣", "绘画", "书法", "剪纸", "年画", "雕刻", "陶瓷"));
        categoryKeywords.put("传统技艺", List.of("手工", "工艺", "制作", "编织", "染织", "漆器", "木雕", "玉雕"));
        categoryKeywords.put("民俗", List.of("民俗", "节日", "习俗", "庙会", "祭祀", "婚俗", "风俗"));
        categoryKeywords.put("传统戏剧", List.of("粤剧", "戏曲", "戏剧", "木偶", "皮影", "曲艺", "杂技"));
        categoryKeywords.put("传统美食", List.of("美食", "小吃", "茶", "早茶", "点心", "糕点", "酒"));
        
        // 获取最近30天的用户消息
        Date thirtyDaysAgo = DateUtil.offsetDay(new Date(), -30);
        List<ChatMessage> userMessages = messageService.lambdaQuery()
                .eq(ChatMessage::getRole, "user")
                .ge(ChatMessage::getCreatedAt, thirtyDaysAgo)
                .isNotNull(ChatMessage::getContent)
                .list();
        
        // 统计各类别的提及次数
        Map<String, Long> categoryCount = new LinkedHashMap<>();
        for (String category : categoryKeywords.keySet()) {
            categoryCount.put(category, 0L);
        }
        
        for (ChatMessage msg : userMessages) {
            String content = msg.getContent();
            if (content == null || content.isEmpty()) continue;
            
            for (Map.Entry<String, List<String>> entry : categoryKeywords.entrySet()) {
                for (String keyword : entry.getValue()) {
                    if (content.contains(keyword)) {
                        categoryCount.merge(entry.getKey(), 1L, Long::sum);
                        break; // 每条消息每个类别只计一次
                    }
                }
            }
        }
        
        // 转换为结果列表
        List<InterestData> result = categoryCount.entrySet().stream()
                .map(e -> InterestData.builder()
                        .category(e.getKey())
                        .count(e.getValue())
                        .build())
                .collect(Collectors.toList());
        
        // 如果所有类别都是0，基于项目类别分布返回
        long total = result.stream().mapToLong(InterestData::getCount).sum();
        if (total == 0) {
            QueryWrapper<IchProject> wrapper = new QueryWrapper<>();
            wrapper.select("category, COUNT(*) as count")
                    .isNotNull("category")
                    .ne("category", "")
                    .groupBy("category");
            List<Map<String, Object>> projectStats = projectService.listMaps(wrapper);
            
            result.clear();
            for (Map<String, Object> stat : projectStats) {
                String category = (String) stat.get("category");
                Long count = ((Number) stat.get("count")).longValue();
                result.add(InterestData.builder().category(category).count(count).build());
            }
        }
        
        return Result.success(result);
    }

    private String getDayOfWeek(Date date) {
        String[] days = {"周日", "周一", "周二", "周三", "周四", "周五", "周六"};
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return days[cal.get(Calendar.DAY_OF_WEEK) - 1];
    }

    // ========== 响应数据结构 ==========

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OverviewData {
        private Long totalUsers;          // 总用户数
        private Long dau;                 // 今日活跃用户
        private Long totalConversations;  // 累计对话数
        private Long totalMessages;       // 累计消息数
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrafficData {
        private String date;      // 日期 MM-dd
        private String dayOfWeek; // 周几
        private Long pv;          // 用户消息数
        private Long uv;          // 活跃用户数
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HotProjectData {
        private Long projectId;
        private String name;
        private String category;
        private Long visitCount;  // 被提及次数
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InterestData {
        private String category;
        private Long count;
    }
}
