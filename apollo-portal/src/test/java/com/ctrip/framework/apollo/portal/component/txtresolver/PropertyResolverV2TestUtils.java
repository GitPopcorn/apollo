package com.ctrip.framework.apollo.portal.component.txtresolver;

import com.ctrip.framework.apollo.common.dto.ItemChangeSets;
import com.ctrip.framework.apollo.common.dto.ItemDTO;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * {@link PropertyResolverV2Test}的基础工具类
 * @author ZhouYi
 * @date 2023/4/4 14:28
 * @note note
 */
public class PropertyResolverV2TestUtils {
	
	// ===== ===== ===== ===== [常量-未分类] ===== ===== ===== ===== //
	
	/** {@link ItemDTO}列表类型 */
	public static final Type STRING_MAP_TYPE = new TypeToken<Map<String, String>>() {}.getType();
	
	/** {@link ItemDTO}列表类型 */
	public static final Type ITEM_LIST_TYPE = new TypeToken<List<ItemDTO>>() {}.getType();
	
	/** GSON对象 */
	public static final Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
	
	// ===== ===== ===== ===== [静态工具方法-未分类] ===== ===== ===== ===== //
	
	/** 安全地沉睡指定时长 */
	public static void safelySleep(long millis) {
		try {
			Thread.sleep(millis);
			
		} catch (InterruptedException e) {
			e.printStackTrace();
			
		}
		
	}
	
	/** 安全地写出文本到指定文件 */
	@SuppressWarnings("UnstableApiUsage")
	public static void safelyWriteTo(String output, String pathText, Charset charset) {
		try {
			File file = new File(pathText);
			Files.createParentDirs(file);
			Files.write(output, file, charset);
			
		} catch (IOException e) {
			throw new RuntimeException(e);
			
		}
		
	}
	
	/** 模拟数据库持久化操作 */
	public static List<ItemDTO> mockPersist(List<ItemDTO> items, ItemChangeSets changeSets) {
		// STEP 获取删除和更新的配置列表，转化为以Key为键，以ItemDTO为值的Map
		Map<Long, ItemDTO> deletedKeyItemMap = changeSets.getDeleteItems().stream().collect(Collectors.toMap(ItemDTO::getId, Function.identity()));
		Map<Long, ItemDTO> updatedKeyItemMap = changeSets.getUpdateItems().stream().collect(Collectors.toMap(ItemDTO::getId, Function.identity()));
		
		// STEP 获取最大的配置项ID
		Long maxId = items.stream().map(ItemDTO::getId).max(Long::compareTo).orElse(1L);
		
		// STEP 循环处理原有的配置项列表，删除已删除的配置项，更新已更新的配置项，保留未变更的配置项
		// STEP 为新增的配置项设置ID，并添加到新的配置项列表中 
		List<ItemDTO> newItems = new ArrayList<>(items);
		for (ItemDTO item : changeSets.getCreateItems()) {
			item.setId(++ maxId);
			newItems.add(item);
			
		}
		
		// STEP 执行更新操作
		for (ItemDTO item : items) {
			if (updatedKeyItemMap.containsKey(item.getId())) {
				ItemDTOWrapper.copyNonNullProperties(updatedKeyItemMap.get(item.getId()), item);
				
			}
			
		}
		
		// STEP 执行删除操作
		newItems = newItems.stream().filter(item -> ! deletedKeyItemMap.containsKey(item.getId())).collect(Collectors.toList());
		
		// STEP 对处理后的配置项列表进行排序
		newItems.sort(Comparator.comparing(ItemDTO::getLineNum));
		
		// STEP 返回处理后的配置项列表
		return newItems;
		
	}
	
}
