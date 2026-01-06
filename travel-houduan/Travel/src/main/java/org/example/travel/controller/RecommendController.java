package org.example.travel.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.example.travel.common.BaseResponse;
import org.example.travel.common.Result;
import org.example.travel.exception.ErrorCode;
import org.example.travel.exception.ThrowUtils;
import org.example.travel.model.dto.env.EnvContextDTO;
import org.example.travel.model.dto.recommend.RecommendRequest;
import org.example.travel.model.dto.recommend.RecommendResponse;
import org.example.travel.model.entity.EnvSnapshot;
import org.example.travel.model.entity.RecommendationRecord;
import org.example.travel.model.entity.User;
import org.example.travel.service.EnvContextService;
import org.example.travel.service.EnvSnapshotService;
import org.example.travel.service.PythonAlgorithmService;
import org.example.travel.service.RecommendationRecordService;
import org.example.travel.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController  
@RequestMapping("/recommend")
public class RecommendController {

    @Resource
    private PythonAlgorithmService pythonAlgorithmService;

    @Resource
    private EnvContextService envContextService;

    @Resource
    private EnvSnapshotService envSnapshotService;

    @Resource
    private RecommendationRecordService recommendationRecordService;

    @Resource
    private UserService userService;

    @PostMapping("/get")
    public BaseResponse<RecommendResponse> getRecommendations(
            @RequestBody RecommendRequestVO request,
            HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(request.getLat() == null || request.getLng() == null,
                ErrorCode.PARAMS_ERROR, "位置信息不能为空");

        User loginUser = userService.getLoginUser(httpRequest);

        EnvContextDTO envContext = envContextService.getEnvContext(request.getLat(), request.getLng());

        EnvSnapshot snapshot = new EnvSnapshot();
        snapshot.setLat(request.getLat());
        snapshot.setLng(request.getLng());
        snapshot.setWeather(envContext.getWeather());
        snapshot.setCreatedAt(new Date());
        envSnapshotService.save(snapshot);

        RecommendRequest recommendRequest = RecommendRequest.builder()
                .userId(loginUser.getId())
                .lat(request.getLat())
                .lng(request.getLng())
                .interestTags(request.getInterestTags())
                .weather(envContext.getWeather())
                .outdoorSuitable(envContext.getOutdoorSuitable())
                .limit(request.getLimit() != null ? request.getLimit() : 10)
                .build();

        RecommendResponse response = pythonAlgorithmService.getRecommendations(recommendRequest);

        saveRecommendationRecord(loginUser.getId(), snapshot.getId(), response);

        return Result.success(response);
    }

    private void saveRecommendationRecord(Long userId, Long envId, RecommendResponse response) {
        try {
            RecommendationRecord record = new RecommendationRecord();
            record.setUserId(userId);
            record.setEnvId(envId);
            record.setStrategy("python_algorithm");
            
            List<Long> resultIds = response.hasData()
                    ? response.getData().stream()
                        .map(RecommendResponse.RecommendItem::getId)
                        .collect(Collectors.toList())
                    : List.of();
            record.setResultIds(resultIds);
            record.setCreatedAt(new Date());
            
            recommendationRecordService.save(record);
        } catch (Exception e) {
            // ignore
        }
    }

    @lombok.Data
    public static class RecommendRequestVO {
        private BigDecimal lat;
        private BigDecimal lng;
        private List<String> interestTags;
        private Integer limit;
    }
}
