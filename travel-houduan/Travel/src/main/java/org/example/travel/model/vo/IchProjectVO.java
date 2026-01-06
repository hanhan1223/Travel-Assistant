package org.example.travel.model.vo;

import lombok.Data;
import org.example.travel.model.entity.IchMedia;
import org.example.travel.model.entity.IchProject;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 非遗项目VO（包含媒体列表）
 */
@Data
public class IchProjectVO {
    /**
     * 非遗项目ID
     */
    private Long id;

    /**
     * 非遗名称
     */
    private String name;

    /**
     * 非遗类别
     */
    private String category;

    /**
     * 非遗简介
     */
    private String description;

    /**
     * 所属城市
     */
    private String city;

    /**
     * 纬度
     */
    private BigDecimal lat;

    /**
     * 经度
     */
    private BigDecimal lng;

    /**
     * 是否室内
     */
    private Integer isIndoor;

    /**
     * 开放状态
     */
    private String openStatus;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 媒体列表（图片/视频）
     */
    private List<IchMedia> mediaList;

    /**
     * 从实体转换
     */
    public static IchProjectVO fromEntity(IchProject project, List<IchMedia> mediaList) {
        if (project == null) {
            return null;
        }
        IchProjectVO vo = new IchProjectVO();
        BeanUtils.copyProperties(project, vo);
        vo.setMediaList(mediaList);
        return vo;
    }
}
