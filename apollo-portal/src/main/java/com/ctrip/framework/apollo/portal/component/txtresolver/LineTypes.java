package com.ctrip.framework.apollo.portal.component.txtresolver;

/**
 * 行类型
 * @author ZhouYi
 * @date 2023/4/3 18:29
 * @note note
 */
public interface LineTypes {
	
	/** 注释行 */
	int COMMENT = 1;
	
	/** 空行 */
	int BLANK = 2;
	
	/** 普通行 */
	int NORMAL = 3;
	
	/** 无法识别的行 */
	int UNKNOWN = 4;
	
}