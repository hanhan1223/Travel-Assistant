package org.example.travel.service.impl;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.example.travel.model.dto.env.EnvContextDTO;
import org.example.travel.service.EnvContextService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * 环境上下文服务实现（百度地图版）
 */
@Service
@Slf4j
public class EnvContextServiceImpl implements EnvContextService {

    @Value("${baidu.map.ak:}")
    private String baiduAk;

    // 不适合户外的天气
    private static final Set<String> BAD_WEATHER = Set.of(
            "雨", "雪", "暴雨", "大雨", "中雨", "雷阵雨", "暴雪", "大雪", "雾", "霾"
    );

    @Override
    public EnvContextDTO getEnvContext(BigDecimal lat, BigDecimal lng) {
        EnvContextDTO.EnvContextDTOBuilder builder = EnvContextDTO.builder()
                .lat(lat)
                .lng(lng);

        try {
            // 1. 逆地理编码获取地址信息（百度API）
            String location = lat + "," + lng;
            String geoUrl = String.format(
                    "https://api.map.baidu.com/reverse_geocoding/v3/?ak=%s&output=json&coordtype=wgs84ll&location=%s",
                    baiduAk, location
            );
            String geoResult = HttpUtil.get(geoUrl);
            JSONObject geoJson = JSONUtil.parseObj(geoResult);
            
            if (geoJson.getInt("status") == 0) {
                JSONObject result = geoJson.getJSONObject("result");
                JSONObject addressComponent = result.getJSONObject("addressComponent");
                
                String city = addressComponent.getStr("city");
                String district = addressComponent.getStr("district");
                String address = result.getStr("formatted_address");
                String adcode = addressComponent.getStr("adcode");
                
                builder.city(city)
                       .district(district)
                       .address(address);

                // 2. 获取天气信息（使用百度天气API或其他天气服务）
                String weather = getWeatherByAdcode(adcode);
                if (weather != null) {
                    builder.weather(weather)
                           .outdoorSuitable(!isWeatherBad(weather));
                }
            }
        } catch (Exception e) {
            log.error("获取环境上下文失败", e);
            builder.weather("未知")
                   .outdoorSuitable(true);
        }

        EnvContextDTO context = builder.build();
        context.setDescription(buildDescription(context));
        return context;
    }

    @Override
    public String getWeather(String city) {
        try {
            // 先通过地理编码获取城市的行政区划代码
            String encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8);
            String geoUrl = String.format(
                    "https://api.map.baidu.com/geocoding/v3/?address=%s&output=json&ak=%s",
                    encodedCity, baiduAk
            );
            String geoResult = HttpUtil.get(geoUrl);
            JSONObject geoJson = JSONUtil.parseObj(geoResult);
            
            if (geoJson.getInt("status") == 0) {
                JSONObject location = geoJson.getJSONObject("result").getJSONObject("location");
                double lat = location.getDouble("lat");
                double lng = location.getDouble("lng");
                
                // 使用经纬度查询天气
                String weatherUrl = String.format(
                        "https://api.map.baidu.com/weather/v1/?data_type=now&ak=%s&location=%.6f,%.6f&coordtype=bd09ll",
                        baiduAk, lng, lat
                );
                String weatherResult = HttpUtil.get(weatherUrl);
                JSONObject weatherJson = JSONUtil.parseObj(weatherResult);
                
                if (weatherJson.getInt("status") == 0) {
                    JSONObject resultObj = weatherJson.getJSONObject("result");
                    JSONObject now = resultObj.getJSONObject("now");
                    return String.format("%s，温度%s℃，%s",
                            now.getStr("text"),
                            now.getStr("temp"),
                            now.getStr("wind_dir") + now.getStr("wind_class")
                    );
                }
            }
        } catch (Exception e) {
            log.error("获取天气失败", e);
        }
        return "天气信息获取失败";
    }

    /**
     * 根据adcode获取天气
     */
    private String getWeatherByAdcode(String adcode) {
        try {
            String url = String.format(
                    "https://api.map.baidu.com/weather/v1/?district_id=%s&data_type=now&ak=%s",
                    adcode, baiduAk
            );
            String result = HttpUtil.get(url);
            JSONObject json = JSONUtil.parseObj(result);
            
            if (json.getInt("status") == 0) {
                JSONObject now = json.getJSONObject("result").getJSONObject("now");
                return now.getStr("text");
            }
        } catch (Exception e) {
            log.error("获取天气失败", e);
        }
        return null;
    }

    @Override
    public String reverseGeocode(BigDecimal lat, BigDecimal lng) {
        try {
            String location = lat + "," + lng;
            String url = String.format(
                    "https://api.map.baidu.com/reverse_geocoding/v3/?ak=%s&output=json&coordtype=wgs84ll&location=%s",
                    baiduAk, location
            );
            String result = HttpUtil.get(url);
            JSONObject json = JSONUtil.parseObj(result);
            
            if (json.getInt("status") == 0) {
                return json.getJSONObject("result").getStr("formatted_address");
            }
        } catch (Exception e) {
            log.error("逆地理编码失败", e);
        }
        return "位置信息获取失败";
    }

    /**
     * 地点搜索（POI搜索）
     */
    public JSONObject searchPlace(String query, String region) {
        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String encodedRegion = URLEncoder.encode(region, StandardCharsets.UTF_8);
            String url = String.format(
                    "https://api.map.baidu.com/place/v2/search?query=%s&region=%s&output=json&ak=%s",
                    encodedQuery, encodedRegion, baiduAk
            );
            String result = HttpUtil.get(url);
            return JSONUtil.parseObj(result);
        } catch (Exception e) {
            log.error("地点搜索失败", e);
            return null;
        }
    }

    /**
     * 路线规划
     * @param originLat 起点纬度
     * @param originLng 起点经度
     * @param destLat 终点纬度
     * @param destLng 终点经度
     * @param mode 出行方式：driving(驾车)/walking(步行)/transit(公交)
     */
    public String planRoute(BigDecimal originLat, BigDecimal originLng, 
                           BigDecimal destLat, BigDecimal destLng, String mode) {
        try {
            String origin = originLat + "," + originLng;
            String destination = destLat + "," + destLng;
            
            String apiPath;
            switch (mode) {
                case "walking":
                    apiPath = "direction/v2/walking";
                    break;
                case "transit":
                    apiPath = "direction/v2/transit";
                    break;
                default:
                    apiPath = "direction/v2/driving";
            }
            
            String url = String.format(
                    "https://api.map.baidu.com/%s?origin=%s&destination=%s&ak=%s&coord_type=wgs84",
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
                    
                    return String.format("距离：%s，预计用时：%s", distanceStr, durationStr);
                }
            }
        } catch (Exception e) {
            log.error("路线规划失败", e);
        }
        return "路线规划失败";
    }

    /**
     * 获取地点详情（包含图片）
     */
    public JSONObject getPlaceDetail(String uid) {
        try {
            String url = String.format(
                    "https://api.map.baidu.com/place/v2/detail?uid=%s&output=json&scope=2&ak=%s",
                    uid, baiduAk
            );
            String result = HttpUtil.get(url);
            return JSONUtil.parseObj(result);
        } catch (Exception e) {
            log.error("获取地点详情失败", e);
            return null;
        }
    }

    /**
     * 判断天气是否不适合户外
     */
    private boolean isWeatherBad(String weather) {
        if (weather == null) return false;
        return BAD_WEATHER.stream().anyMatch(weather::contains);
    }

    /**
     * 构建环境描述
     */
    private String buildDescription(EnvContextDTO context) {
        StringBuilder sb = new StringBuilder();
        sb.append("用户当前位于");
        if (context.getCity() != null) {
            sb.append(context.getCity());
        }
        if (context.getDistrict() != null) {
            sb.append(context.getDistrict());
        }
        if (context.getWeather() != null) {
            sb.append("，天气").append(context.getWeather());
        }
        if (context.getTemperature() != null) {
            sb.append("，温度").append(context.getTemperature()).append("℃");
        }
        if (Boolean.FALSE.equals(context.getOutdoorSuitable())) {
            sb.append("，不太适合户外活动，建议推荐室内项目");
        } else {
            sb.append("，适合户外活动");
        }
        return sb.toString();
    }
}
