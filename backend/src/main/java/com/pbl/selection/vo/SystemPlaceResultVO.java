package com.pbl.selection.vo;

import com.pbl.selection.vo.DiversionCandidateVO;
import lombok.Data;

import java.util.List;

/**
 * 系统统筹结果
 */
@Data
public class SystemPlaceResultVO {

    /** 第一优先级（实训室内）安置人数 */
    private int internalPlaced;
    /** 第二优先级（全校）安置人数 */
    private int crossPlaced;
    /** 未安置学生 */
    private List<DiversionCandidateVO> unplaced;
}
