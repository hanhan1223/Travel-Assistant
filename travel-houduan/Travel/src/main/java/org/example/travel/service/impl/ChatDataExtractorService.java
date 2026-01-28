package org.example.travel.service.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.travel.model.dto.chat.LocationData;
import org.example.travel.model.dto.chat.ProductData;
import org.example.travel.model.entity.IchMedia;
import org.example.travel.model.entity.IchProject;
import org.example.travel.model.entity.Merchant;
import org.example.travel.service.IchMediaService;
import org.example.travel.service.IchProjectService;
import org.example.travel.service.MerchantService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 聊天数据提取服务
 * 从工具调用结果中提取结构化数据
 */
@Service
@Slf4j
public class ChatDataExtractorService {

    @Resource
    private IchProjectService ichProjectService;

    @Resource
    private MerchantService merchantService;

    @Resource
    private IchMediaService ichMediaService;

    /**
     * 从工具调用结果中提取地点数据
     */
    public List<LocationData> extractLocationsFromToolResult(String toolName, String toolResult) {
        List<LocationData> locations = new ArrayList<>();
        
        try {
            if (toolResult == null) {
                return locations;
            }
            
            // 先去除可能的外层引号
            String cleanResult = unwrapJsonString(toolResult);
            
            if (cleanResult.startsWith("未找到") || cleanResult.startsWith("暂无") 
                    || cleanResult.startsWith("推荐服务暂时不可用")) {
                return locations;
            }

            // 判断是项目还是商户
            if (toolName.contains("Project") || toolName.contains("project")) {
                locations.addAll(extractProjectLocations(cleanResult));
            } else if (toolName.contains("Merchant") || toolName.contains("merchant")) {
                locations.addAll(extractMerchantLocations(cleanResult));
            } else if (toolName.contains("Recommend") || toolName.contains("recommend")) {
                // 推荐结果可能包含两者
                locations.addAll(extractFromRecommendResult(cleanResult));
            } else if (toolName.equals("searchPlace")) {
                // 百度地图地点搜索结果
                locations.addAll(extractFromBaiduSearchResult(cleanResult));
            } else if (toolName.equals("planRouteByName")) {
                // 路线规划结果（包含目的地信息）
                locations.addAll(extractFromRouteResult(cleanResult));
            } else if (toolName.equals("getPlaceImage")) {
                // 地点图片结果
                locations.addAll(extractFromPlaceImageResult(cleanResult));
            }
        } catch (Exception e) {
            log.warn("提取地点数据失败: {}", e.getMessage());
        }
        
        return locations;
    }

    /**
     * 从项目查询结果提取地点数据
     */
    private List<LocationData> extractProjectLocations(String jsonResult) {
        List<LocationData> locations = new ArrayList<>();
        
        try {
            if (jsonResult.startsWith("[")) {
                JSONArray array = JSONUtil.parseArray(jsonResult);
                for (int i = 0; i < array.size(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    locations.add(convertProjectToLocation(obj));
                }
            } else if (jsonResult.startsWith("{")) {
                JSONObject obj = JSONUtil.parseObj(jsonResult);
                locations.add(convertProjectToLocation(obj));
            }
        } catch (Exception e) {
            log.warn("解析项目JSON失败: {}", e.getMessage());
        }
        
        return locations;
    }

    /**
     * 从商户查询结果提取地点数据
     */
    private List<LocationData> extractMerchantLocations(String jsonResult) {
        List<LocationData> locations = new ArrayList<>();
        
        try {
            if (jsonResult.startsWith("[")) {
                JSONArray array = JSONUtil.parseArray(jsonResult);
                for (int i = 0; i < array.size(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    locations.add(convertMerchantToLocation(obj));
                }
            } else if (jsonResult.startsWith("{")) {
                JSONObject obj = JSONUtil.parseObj(jsonResult);
                locations.add(convertMerchantToLocation(obj));
            }
        } catch (Exception e) {
            log.warn("解析商户JSON失败: {}", e.getMessage());
        }
        
        return locations;
    }
    
    /**
     * 去除JSON字符串外层的引号包装
     * 工具返回的结果可能被包装成 "\"[{...}]\"" 格式
     */
    private String unwrapJsonString(String str) {
        if (str == null) return null;
        str = str.trim();
        // 如果以引号开头结尾，去掉外层引号
        while (str.startsWith("\"") && str.endsWith("\"") && str.length() > 2) {
            str = str.substring(1, str.length() - 1);
            // 处理转义的引号和换行符
            str = str.replace("\\\"", "\"").replace("\\n", "\n");
        }
        return str.trim();
    }

    /**
     * 从推荐结果提取地点数据
     */
    private List<LocationData> extractFromRecommendResult(String result) {
        List<LocationData> locations = new ArrayList<>();
        // 推荐结果是文本格式，需要解析项目ID和商户ID
        // 这里简化处理，实际可以通过正则提取
        return locations;
    }

    /**
     * 从百度地图搜索结果提取地点数据
     * 格式示例：
     * 1. 广州博物馆(镇海楼馆区)
     *    地址：广东省广州市越秀区镇海路99号越秀公园内
     *    坐标：23.144000, 113.272125
     *    评分：4.7
     */
    private List<LocationData> extractFromBaiduSearchResult(String result) {
        List<LocationData> locations = new ArrayList<>();
        
        try {
            // 先处理转义的换行符
            result = result.replace("\\n", "\n");
            
            // 按地点分割（以数字+点开头）
            String[] blocks = result.split("(?=\\d+\\. )");
            
            for (String block : blocks) {
                if (block.trim().isEmpty()) continue;
                
                LocationData location = new LocationData();
                location.setType("poi");
                
                // 提取名称（第一行）
                String[] lines = block.split("\n");
                if (lines.length > 0) {
                    String nameLine = lines[0].trim();
                    // 去掉序号
                    nameLine = nameLine.replaceFirst("^\\d+\\.\\s*", "");
                    location.setName(nameLine);
                }
                
                // 提取其他字段
                for (String line : lines) {
                    line = line.trim();
                    if (line.startsWith("地址：")) {
                        location.setAddress(line.substring(3));
                    } else if (line.startsWith("坐标：")) {
                        String coordStr = line.substring(3);
                        String[] coords = coordStr.split(",\\s*");
                        if (coords.length == 2) {
                            location.setLat(new java.math.BigDecimal(coords[0].trim()));
                            location.setLng(new java.math.BigDecimal(coords[1].trim()));
                        }
                    } else if (line.startsWith("评分：")) {
                        try {
                            location.setRating(new java.math.BigDecimal(line.substring(3)));
                        } catch (Exception ignored) {}
                    } else if (line.startsWith("图片：")) {
                        String imageUrl = line.substring(3);
                        if (!imageUrl.isEmpty()) {
                            location.setImages(List.of(imageUrl));
                        }
                    } else if (line.startsWith("实景图片：")) {
                        String imageUrl = line.substring(5);
                        if (!imageUrl.isEmpty()) {
                            // 实景图片优先
                            location.setImages(List.of(imageUrl));
                        }
                    } else if (line.startsWith("地图图片：")) {
                        String imageUrl = line.substring(5);
                        // 如果还没有图片，用地图图片
                        if (location.getImages() == null || location.getImages().isEmpty()) {
                            location.setImages(List.of(imageUrl));
                        }
                    }
                }
                
                // 只添加有名称和坐标的地点
                if (location.getName() != null && location.getLat() != null) {
                    locations.add(location);
                }
            }
        } catch (Exception e) {
            log.warn("解析百度搜索结果失败: {}", e.getMessage());
        }
        
        return locations;
    }

    /**
     * 从路线规划结果提取目的地数据
     * 格式示例：
     * 目的地：广州博物馆(镇海楼馆区)
     * 地址：广东省广州市越秀区镇海路99号越秀公园内
     * 坐标：23.144000, 113.272125
     * 步行路线：距离2.2公里，预计用时30分钟
     */
    private List<LocationData> extractFromRouteResult(String result) {
        List<LocationData> locations = new ArrayList<>();
        
        try {
            // 先处理转义的换行符和多余的引号
            result = result.replace("\\n", "\n").replace("\"", "");
            
            LocationData location = new LocationData();
            location.setType("destination");
            
            String[] lines = result.split("\n");
            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty()) continue;
                
                if (line.startsWith("目的地：")) {
                    location.setName(line.substring(4).trim());
                } else if (line.startsWith("地址：")) {
                    location.setAddress(line.substring(3).trim());
                } else if (line.startsWith("坐标：")) {
                    try {
                        String coordStr = line.substring(3).trim();
                        String[] coords = coordStr.split(",\\s*");
                        if (coords.length == 2) {
                            location.setLat(new java.math.BigDecimal(coords[0].trim()));
                            location.setLng(new java.math.BigDecimal(coords[1].trim()));
                        }
                    } catch (Exception e) {
                        log.debug("坐标解析失败: {}", line);
                    }
                } else if (line.startsWith("图片：")) {
                    String imageUrl = line.substring(3).trim();
                    if (!imageUrl.isEmpty()) {
                        location.setImages(List.of(imageUrl));
                    }
                } else if (line.startsWith("实景图片：")) {
                    String imageUrl = line.substring(5).trim();
                    if (!imageUrl.isEmpty()) {
                        location.setImages(List.of(imageUrl));
                    }
                } else if (line.startsWith("地图图片：")) {
                    String imageUrl = line.substring(5).trim();
                    if (location.getImages() == null || location.getImages().isEmpty()) {
                        location.setImages(List.of(imageUrl));
                    }
                } else if (line.contains("路线：")) {
                    // 提取距离信息作为 distance 字段
                    location.setDistance(line.trim());
                }
            }
            
            if (location.getName() != null && location.getLat() != null) {
                locations.add(location);
            }
        } catch (Exception e) {
            log.warn("解析路线结果失败: {}", e.getMessage());
        }
        
        return locations;
    }

    /**
     * 从 getPlaceImage 工具结果提取地点数据
     * 格式示例：
     * 地点：广东省博物馆
     * 地址：广州市天河区珠江东路2号
     * 实景图片：https://xxx
     * 地图图片：https://api.map.baidu.com/staticimage/v2?...
     * 坐标：23.120486, 113.332975
     */
    private List<LocationData> extractFromPlaceImageResult(String result) {
        List<LocationData> locations = new ArrayList<>();
        
        try {
            // 先处理转义的换行符和多余的引号
            result = result.replace("\\n", "\n").replace("\"", "");
            
            LocationData location = new LocationData();
            location.setType("poi");
            
            List<String> images = new ArrayList<>();
            
            String[] lines = result.split("\n");
            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty()) continue;
                
                if (line.startsWith("地点：")) {
                    location.setName(line.substring(3).trim());
                } else if (line.startsWith("地址：")) {
                    location.setAddress(line.substring(3).trim());
                } else if (line.startsWith("坐标：")) {
                    try {
                        String coordStr = line.substring(3).trim();
                        String[] coords = coordStr.split(",\\s*");
                        if (coords.length == 2) {
                            location.setLat(new java.math.BigDecimal(coords[0].trim()));
                            location.setLng(new java.math.BigDecimal(coords[1].trim()));
                        }
                    } catch (Exception e) {
                        log.debug("坐标解析失败: {}", line);
                    }
                } else if (line.startsWith("实景图片：")) {
                    // 实景图片放在前面
                    images.add(0, line.substring(5).trim());
                } else if (line.startsWith("地图图片：")) {
                    // 地图图片放在后面
                    images.add(line.substring(5).trim());
                }
            }
            
            if (!images.isEmpty()) {
                location.setImages(images);
            }
            
            if (location.getName() != null) {
                locations.add(location);
            }
        } catch (Exception e) {
            log.warn("解析地点图片结果失败: {}", e.getMessage());
        }
        
        return locations;
    }

    /**
     * 将项目JSON转换为LocationData
     */
    private LocationData convertProjectToLocation(JSONObject obj) {
        Long projectId = obj.getLong("id");
        
        // 获取项目图片
        List<String> images = getProjectImages(projectId);
        
        return LocationData.builder()
                .id(projectId)
                .type("project")
                .name(obj.getStr("name"))
                .address(obj.getStr("city"))
                .lat(obj.getBigDecimal("lat"))
                .lng(obj.getBigDecimal("lng"))
                .category(obj.getStr("category"))
                .description(obj.getStr("description"))
                .images(images)
                .build();
    }

    /**
     * 将商户JSON转换为LocationData
     */
    private LocationData convertMerchantToLocation(JSONObject obj) {
        return LocationData.builder()
                .id(obj.getLong("id"))
                .type("merchant")
                .name(obj.getStr("name"))
                .address(obj.getStr("address"))
                .lat(obj.getBigDecimal("lat"))
                .lng(obj.getBigDecimal("lng"))
                .phone(obj.getStr("phone"))
                .rating(obj.getBigDecimal("rating"))
                .category(obj.getStr("category"))
                .build();
    }

    /**
     * 获取项目图片列表
     */
    private List<String> getProjectImages(Long projectId) {
        if (projectId == null) {
            return List.of();
        }
        
        try {
            List<IchMedia> mediaList = ichMediaService.lambdaQuery()
                    .eq(IchMedia::getProjectId, projectId)
                    .eq(IchMedia::getMediaType, "image")
                    .last("LIMIT 5")
                    .list();
            
            return mediaList.stream()
                    .map(IchMedia::getMediaUrl)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("获取项目图片失败: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * 根据项目ID获取完整的LocationData
     */
    public LocationData getProjectLocationById(Long projectId) {
        IchProject project = ichProjectService.getById(projectId);
        if (project == null) {
            return null;
        }
        
        List<String> images = getProjectImages(projectId);
        
        return LocationData.builder()
                .id(project.getId())
                .type("project")
                .name(project.getName())
                .address(project.getCity())
                .lat(project.getLat())
                .lng(project.getLng())
                .category(project.getCategory())
                .description(project.getDescription())
                .images(images)
                .build();
    }

    /**
     * 根据商户ID获取完整的LocationData
     */
    public LocationData getMerchantLocationById(Long merchantId) {
        Merchant merchant = merchantService.getById(merchantId);
        if (merchant == null) {
            return null;
        }
        
        return LocationData.builder()
                .id(merchant.getId())
                .type("merchant")
                .name(merchant.getName())
                .lat(merchant.getLat())
                .lng(merchant.getLng())
                .rating(merchant.getRating())
                .category(merchant.getCategory())
                .build();
    }
}
