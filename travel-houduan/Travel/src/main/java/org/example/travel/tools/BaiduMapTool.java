package org.example.travel.tools;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 百度地图工具
 * 提供地点搜索、路线规划、地点图片等功能
 */
@Component
@Slf4j
public class  BaiduMapTool {

    @Value("${baidu.map.ak:}")
    private String baiduAk;

    @Tool(description = "搜索地点信息，可获取地点的位置、评分、图片等详细信息")
    public String searchPlace(
            @ToolParam(description = "搜索关键词，如：广州博物馆、陈家祠") String query,
            @ToolParam(description = "搜索区域，如：广州、北京") String region
    ) {
        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String encodedRegion = URLEncoder.encode(region, StandardCharsets.UTF_8);
            String url = String.format(
                    "https://api.map.baidu.com/place/v2/search?query=%s&region=%s&output=json&scope=2&ak=%s",
                    encodedQuery, encodedRegion, baiduAk
            );
            String result = HttpUtil.get(url);
            JSONObject json = JSONUtil.parseObj(result);
            
            if (json.getInt("status") == 0) {
                JSONArray results = json.getJSONArray("results");
                if (results != null && !results.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    int count = Math.min(results.size(), 3); // 最多返回3个结果
                    
                    for (int i = 0; i < count; i++) {
                        JSONObject place = results.getJSONObject(i);
                        sb.append(String.format("%d. %s\n", i + 1, place.getStr("name")));
                        sb.append(String.format("   地址：%s\n", place.getStr("address")));
                        
                        JSONObject location = place.getJSONObject("location");
                        if (location != null) {
                            sb.append(String.format("   坐标：%.6f, %.6f\n", 
                                    location.getBigDecimal("lat"), location.getBigDecimal("lng")));
                        }
                        
                        // 获取详情信息
                        JSONObject detailInfo = place.getJSONObject("detail_info");
                        if (detailInfo != null) {
                            if (detailInfo.containsKey("overall_rating")) {
                                sb.append(String.format("   评分：%s\n", detailInfo.getStr("overall_rating")));
                            }
                            if (detailInfo.containsKey("image")) {
                                sb.append(String.format("   图片：%s\n", detailInfo.getStr("image")));
                            }
                        }
                        sb.append("\n");
                    }
                    return sb.toString();
                }
            }
        } catch (Exception e) {
            log.error("地点搜索失败", e);
        }
        return "未找到相关地点信息";
    }

    @Tool(description = "规划从起点到终点的路线，支持驾车、步行、公交三种方式")
    public String planRoute(
            @ToolParam(description = "起点纬度") BigDecimal originLat,
            @ToolParam(description = "起点经度") BigDecimal originLng,
            @ToolParam(description = "终点纬度") BigDecimal destLat,
            @ToolParam(description = "终点经度") BigDecimal destLng,
            @ToolParam(description = "出行方式：driving(驾车)/walking(步行)/transit(公交)") String mode
    ) {
        try {
            String origin = originLat + "," + originLng;
            String destination = destLat + "," + destLng;
            
            String apiPath;
            switch (mode) {
                case "walking":
                    apiPath = "directionlite/v1/walking";
                    break;
                case "transit":
                    apiPath = "directionlite/v1/transit";
                    break;
                default:
                    apiPath = "directionlite/v1/driving";
                    mode = "driving";
            }
            
            String url = String.format(
                    "https://api.map.baidu.com/%s?origin=%s&destination=%s&ak=%s",
                    apiPath, origin, destination, baiduAk
            );
            String result = HttpUtil.get(url);
            JSONObject json = JSONUtil.parseObj(result);
            
            if (json.getInt("status") == 0) {
                JSONObject resultObj = json.getJSONObject("result");
                JSONArray routes = resultObj.getJSONArray("routes");
                if (routes != null && !routes.isEmpty()) {
                    JSONObject route = routes.getJSONObject(0);
                    int distance = route.getInt("distance");
                    int duration = route.getInt("duration");
                    
                    String distanceStr = distance >= 1000 ? 
                            String.format("%.1f公里", distance / 1000.0) : distance + "米";
                    String durationStr = duration >= 3600 ?
                            String.format("%d小时%d分钟", duration / 3600, (duration % 3600) / 60) :
                            String.format("%d分钟", duration / 60);
                    
                    String modeStr = switch (mode) {
                        case "walking" -> "步行";
                        case "transit" -> "公交";
                        default -> "驾车";
                    };
                    
                    return String.format("%s路线：距离%s，预计用时%s", modeStr, distanceStr, durationStr);
                }
            }
        } catch (Exception e) {
            log.error("路线规划失败", e);
        }
        return "路线规划失败，请检查起终点坐标是否正确";
    }

    @Tool(description = "根据地点名称规划路线，自动搜索地点坐标后进行路线规划")
    public String planRouteByName(
            @ToolParam(description = "起点纬度") BigDecimal originLat,
            @ToolParam(description = "起点经度") BigDecimal originLng,
            @ToolParam(description = "目的地名称，如：广州博物馆") String destination,
            @ToolParam(description = "目的地所在城市") String city,
            @ToolParam(description = "出行方式：driving(驾车)/walking(步行)/transit(公交)") String mode
    ) {
        try {
            // 先搜索目的地坐标
            String encodedDest = URLEncoder.encode(destination, StandardCharsets.UTF_8);
            String encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8);
            String searchUrl = String.format(
                    "https://api.map.baidu.com/place/v2/search?query=%s&region=%s&output=json&ak=%s",
                    encodedDest, encodedCity, baiduAk
            );
            String searchResult = HttpUtil.get(searchUrl);
            JSONObject searchJson = JSONUtil.parseObj(searchResult);
            
            if (searchJson.getInt("status") == 0) {
                JSONArray results = searchJson.getJSONArray("results");
                if (results != null && !results.isEmpty()) {
                    JSONObject place = results.getJSONObject(0);
                    JSONObject location = place.getJSONObject("location");
                    BigDecimal destLat = location.getBigDecimal("lat");
                    BigDecimal destLng = location.getBigDecimal("lng");
                    String placeName = place.getStr("name");
                    String address = place.getStr("address");
                    
                    // 规划路线
                    String routeInfo = planRoute(originLat, originLng, destLat, destLng, mode);
                    
                    StringBuilder sb = new StringBuilder();
                    sb.append(String.format("目的地：%s\n", placeName));
                    sb.append(String.format("地址：%s\n", address));
                    sb.append(String.format("坐标：%.6f, %.6f\n", destLat, destLng));
                    sb.append(routeInfo);
                    
                    // 尝试获取图片
                    JSONObject detailInfo = place.getJSONObject("detail_info");
                    if (detailInfo != null && detailInfo.containsKey("image")) {
                        sb.append(String.format("\n图片：%s", detailInfo.getStr("image")));
                    }
                    
                    return sb.toString();
                }
            }
        } catch (Exception e) {
            log.error("路线规划失败", e);
        }
        return "未找到目的地'" + destination + "'，请确认地点名称是否正确";
    }

    @Tool(description = "获取地点的图片，返回图片URL。如果没有实景图片，会返回地图截图")
    public String getPlaceImage(
            @ToolParam(description = "地点名称，如：广州博物馆、陈家祠") String placeName,
            @ToolParam(description = "所在城市") String city
    ) {
        try {
            String encodedPlace = URLEncoder.encode(placeName, StandardCharsets.UTF_8);
            String encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8);
            String url = String.format(
                    "https://api.map.baidu.com/place/v2/search?query=%s&region=%s&output=json&scope=2&ak=%s",
                    encodedPlace, encodedCity, baiduAk
            );
            String result = HttpUtil.get(url);
            JSONObject json = JSONUtil.parseObj(result);
            
            if (json.getInt("status") == 0) {
                JSONArray results = json.getJSONArray("results");
                if (results != null && !results.isEmpty()) {
                    JSONObject place = results.getJSONObject(0);
                    JSONObject detailInfo = place.getJSONObject("detail_info");
                    JSONObject location = place.getJSONObject("location");
                    
                    StringBuilder sb = new StringBuilder();
                    sb.append(String.format("地点：%s\n", place.getStr("name")));
                    sb.append(String.format("地址：%s\n", place.getStr("address")));
                    
                    // 优先返回实景图片
                    if (detailInfo != null && detailInfo.containsKey("image")) {
                        sb.append(String.format("实景图片：%s\n", detailInfo.getStr("image")));
                    }
                    
                    // 生成静态地图图片作为补充
                    if (location != null) {
                        BigDecimal lat = location.getBigDecimal("lat");
                        BigDecimal lng = location.getBigDecimal("lng");
                        String staticMapUrl = String.format(
                                "https://api.map.baidu.com/staticimage/v2?ak=%s&center=%s,%s&zoom=16&width=400&height=300&markers=%s,%s",
                                baiduAk, lng, lat, lng, lat
                        );
                        sb.append(String.format("地图图片：%s\n", staticMapUrl));
                        sb.append(String.format("坐标：%.6f, %.6f", lat, lng));
                    }
                    
                    return sb.toString();
                }
            }
        } catch (Exception e) {
            log.error("获取地点图片失败", e);
        }
        return "未找到'" + placeName + "'的信息";
    }
}
