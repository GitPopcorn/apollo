package com.ctrip.framework.apollo.portal.component.txtresolver;

import com.ctrip.framework.apollo.common.dto.ItemDTO;
import org.apache.commons.lang.StringUtils;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 配置项DTO封装，在{@link ItemDTO}的基础上，增加了一些额外的信息
 * @author ZhouYi
 * @date 2023/4/3 18:29
 * @note note
 */
public class ItemDTOWrapper {
	
	// ===== ===== ===== ===== [常量-未分类] ===== ===== ===== ===== //
	
	/** 注释行的格式 */
	public static final Pattern COMMENT_LINE_PATTERN = Pattern.compile("^\\s*[#!].*$");
	
	// ===== ===== ===== ===== [成员变量] ===== ===== ===== ===== //
	
	/** 配置项 */
	private ItemDTO itemDTO;
	
	/** 配置项对应的原始行文本 */
	private String line;
	
	/** 配置项类型，详情参考{@link LineTypes} */
	private int lineType;
	
	// ===== ===== ===== ===== [构造方法] ===== ===== ===== ===== //
	
	public ItemDTOWrapper() {
	}
	
	public ItemDTOWrapper(ItemDTO itemDTO) {
		this.itemDTO = itemDTO;
		this.lineType = determineItemType(itemDTO);
	}
	
	public ItemDTOWrapper(ItemDTO itemDTO, String line) {
		this.itemDTO = itemDTO;
		this.line = line;
		this.lineType = determineItemType(itemDTO);
	}
	
	public ItemDTOWrapper(ItemDTO itemDTO, String line, int lineType) {
		this.itemDTO = itemDTO;
		this.line = line;
		this.lineType = lineType;
	}
	
	// ===== ===== ===== ===== [操作方法-Getters & Setters] ===== ===== ===== ===== //
	
	public ItemDTO getItemDTO() {
		return itemDTO;
	}
	
	public ItemDTOWrapper setItemDTO(ItemDTO itemDTO) {
		this.itemDTO = itemDTO;
		return this;
	}
	
	public String getLine() {
		return line;
	}
	
	public ItemDTOWrapper setLine(String line) {
		this.line = line;
		return this;
	}
	
	public int getLineType() {
		return lineType;
	}
	
	public ItemDTOWrapper setLineType(int lineType) {
		this.lineType = lineType;
		return this;
	}
	
	// ===== ===== ===== ===== [静态工具方法-文本行判断] ===== ===== ===== ===== //
	
	/** 判断是否为注释配置项 */
	public static boolean isCommentItem(ItemDTO item) {
		return (item != null) && StringUtils.isBlank(item.getKey()) && isCommentLine(item.getComment());
		
	}
	
	/** 判断是否为注释行 */
	public static boolean isCommentLine(String line) {
		// return (line != null) && (line.startsWith("#") || line.startsWith("!"));
		return (line != null) && COMMENT_LINE_PATTERN.matcher(line).matches();
		
	}
	
	/** 判断是否为空白配置项 */
	public static boolean isBlankItem(ItemDTO item) {
		return (item != null) && StringUtils.isBlank(item.getKey()) && StringUtils.isBlank(item.getComment());
		
	}
	
	/** 判断是否为空白行 */
	public static boolean isBlankLine(String line) {
		return StringUtils.isBlank(line);
		
	}
	
	/** 判断配置项类型 */
	public static int determineItemType(ItemDTO item) {
		if (item == null) { return LineTypes.UNKNOWN; } 
		if (isBlankItem(item)) { return LineTypes.BLANK; } 
		if (isCommentItem(item)) { return LineTypes.COMMENT; } 
		return LineTypes.NORMAL;
		
	}
	
	/** 判断行类型 */
	public static int determineLineType(String line) {
		if (line == null) { return LineTypes.UNKNOWN; } 
		if (isBlankLine(line)) { return LineTypes.BLANK; } 
		if (isCommentLine(line)) { return LineTypes.COMMENT; } 
		return LineTypes.NORMAL;
		
	}
	
	// ===== ===== ===== ===== [静态工具方法-{@link ItemDTOWrapper}操作] ===== ===== ===== ===== //
	
	/** 判断{@link ItemDTOWrapper}以及内部的{@link ItemDTO}是否为null */
	public static boolean isItemDTONull(ItemDTOWrapper wrapper) {
		return (wrapper == null) || (wrapper.getItemDTO() == null);
		
	}
	
	/** 判断{@link ItemDTOWrapper}以及内部的{@link ItemDTO}是否不为null */
	public static boolean isItemDTONotNull(ItemDTOWrapper wrapper) {
		return (wrapper != null) && (wrapper.getItemDTO() != null);
		
	}
	
	// ===== ===== ===== ===== [静态工具方法-{@link ItemDTO}操作] ===== ===== ===== ===== //
	
	/** 将{@link ItemDTO}对象转化为文本行 */
	public static String toLine(ItemDTO item) {
		if (item == null) { return null; }
		if (isBlankItem(item)) { return ""; }
		if (isCommentItem(item)) { return item.getComment(); }
		return item.getKey() + " = " + item.getValue();
		
	}
	
	/** 浅拷贝克隆{@link ItemDTO}对象 */
	public static ItemDTO clone(ItemDTO src) {
		if (src == null) { return null; }
		ItemDTO dest = new ItemDTO();
		dest.setId(src.getId());
		dest.setComment(src.getComment());
		dest.setKey(src.getKey());
		dest.setNamespaceId(src.getNamespaceId());
		dest.setValue(src.getValue());
		dest.setLineNum(src.getLineNum());
		dest.setDataChangeCreatedBy(src.getDataChangeCreatedBy());
		dest.setDataChangeLastModifiedBy(src.getDataChangeLastModifiedBy());
		dest.setDataChangeCreatedTime(src.getDataChangeCreatedTime());
		dest.setDataChangeLastModifiedTime(src.getDataChangeLastModifiedTime());
		return dest;
		
	}
	
	/** 浅拷贝克隆{@link ItemDTO}对象中的所有不为null的属性 */
	public static void copyNonNullProperties(ItemDTO src, ItemDTO dest) {
		if ((src == null) || (dest == null)) { return; }
		dest.setId(src.getId());
		if (src.getComment() != null) { dest.setComment(src.getComment()); }
		if (src.getKey() != null) { dest.setKey(src.getKey()); }
		dest.setNamespaceId(src.getNamespaceId());
		if (src.getValue() != null) { dest.setValue(src.getValue()); }
		dest.setLineNum(src.getLineNum());
		if (src.getDataChangeCreatedBy() != null) { dest.setDataChangeCreatedBy(src.getDataChangeCreatedBy()); }
		if (src.getDataChangeLastModifiedBy() != null) { dest.setDataChangeLastModifiedBy(src.getDataChangeLastModifiedBy()); }
		if (src.getDataChangeCreatedTime() != null) { dest.setDataChangeCreatedTime(src.getDataChangeCreatedTime()); }
		if (src.getDataChangeLastModifiedTime() != null) { dest.setDataChangeLastModifiedTime(src.getDataChangeLastModifiedTime()); }
		
	}
	
	/** 判断两个{@link ItemDTO}的属性是否相等 */
	public static boolean isPropertiesEqual(ItemDTO item1, ItemDTO item2, boolean ignoreComment) {
		if (item1 == null) { return item2 == null; }
		if (item2 == null) { return false; }
		return Objects.equals(item1.getKey(), item2.getKey()) 
				&& Objects.equals(item1.getValue(), item2.getValue())
				&& Objects.equals(item1.getLineNum(), item2.getLineNum())
				&& (ignoreComment || Objects.equals(item1.getComment(), item2.getComment()))
		;
		
	}
	
}
