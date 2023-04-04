package com.ctrip.framework.apollo.portal.component;

import com.ctrip.framework.apollo.common.dto.ItemChangeSets;
import com.ctrip.framework.apollo.common.dto.ItemDTO;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * 配置项变更比较器
 * @author ZhouYi
 * @date 2023/4/4 12:26
 * @note note
 */
@Component
public class ItemChangeDiffer {
	
	// ===== ===== ===== ===== [常量-通用] ===== ===== ===== ===== //
	
	
	
	// ===== ===== ===== ===== [静态变量] ===== ===== ===== ===== //
	
	
	
	// ===== ===== ===== ===== [成员变量] ===== ===== ===== ===== //
	
	
	
	// ===== ===== ===== ===== [操作方法-通用] ===== ===== ===== ===== //
	
	/**
	 * 当用户执行撤销操作时，需要将当前发布的配置项(老数据)与当前状态下的配置项(新数据)做对比，得出撤销操作对应的新增、更新、删除信息记录
	 * @param namespaceId 配置项所属的Namespace的ID
	 * @param format 配置的文本格式类型，详情参考{@link ConfigFileFormat}
	 * @param releaseItemKeyValue 最近一次发布配置(老数据)的所有KeyValue
	 * @param currBaseItems 当前状态配置(新数据)的所有配置项
	 * @param currDeletedItems 当前状态配置(新数据)的所有被删除的配置项
	 * @return {@code ItemChangeSets} 整理出的新增、更新、删除信息记录
	 * @author ZhouYi
	 * @date 2023/4/4 12:28
	 * @note note
	 */
	public ItemChangeSets diffItemChangeSetsForInvoking(
			long namespaceId, String format, Map<String, String> releaseItemKeyValue
			, List<ItemDTO> currBaseItems, List<ItemDTO> currDeletedItems
	) {
		// STEP 将当前状态下的所有配置项(尚未提交的新新数据)转换为配置键为Key的Map，方便后续对比
		Collector<ItemDTO, ?, Map<String, ItemDTO>> toMapCollector = Collectors.toMap(ItemDTO::getKey, Function.identity(), (o1, o2) -> o1);
		Map<String, ItemDTO> currBaseKeyItemMap = Optional.ofNullable(currBaseItems)
				.orElseGet(ArrayList::new)
				.stream()
				.collect(toMapCollector)
		;
		Map<String, ItemDTO> currDeletedKeyItemMap = Optional.ofNullable(currDeletedItems)
				.orElseGet(ArrayList::new)
				.stream()
				.collect(toMapCollector)
		;
		
		// STEP 获取新配置值的最大行号
		int maxLineNum = Optional.ofNullable(currBaseItems)
				.orElseGet(ArrayList::new)
				.stream()
				.mapToInt(ItemDTO::getLineNum)
				.max()
				.orElse(1)
		;
		
		// STEP 判断是否需要更新行号
		boolean isLineNumUpdatingNeeded = ! (ConfigFileFormat.Properties.getValue().equals(format));
		
		// STEP 初始化变更集合对象
		ItemChangeSets changeSets = new ItemChangeSets();
		
		// STEP 遍历当前发布的配置值KeyValue(老数据)，与当前状态下所有配置项(尚未提交的新新数据)做对比
		int seq = 0;
		for (Map.Entry<String, String> releaseKeyValueEntry : releaseItemKeyValue.entrySet()) {
			// SUBSTEP 行号自增
			seq ++;
			
			// SUBSTEP 获取老数据项的Key和Value
			String key = releaseKeyValueEntry.getKey();
			String value = releaseKeyValueEntry.getValue();
			
			// SUBSTEP 按照Key获取新数据的配置项
			ItemDTO currItem = currBaseKeyItemMap.get(key);
			
			// BRANCH 如果新数据不存在该配置项，则新增
			if (currItem == null) {
				// PART 从被删除配置的Map中获取原始配置
				// NOTE 若被删除配置的Map中没有原始配置，则新增一个空配置项进去，保证同一个配置最多只新增一次
				ItemDTO deletedItemDto = currDeletedKeyItemMap.computeIfAbsent(key, k -> new ItemDTO());
				
				// PART 获取行号
				int lineNum = isLineNumUpdatingNeeded ? seq : deletedItemDto.getLineNum(); // NOTE 若需要更新行号，则使用自增序号作为行号，否则使用原始配置项的行号
				lineNum = (lineNum > 0) ? lineNum : maxLineNum ++; // NOTE 若原始配置项的行号丢失，则将其放到最后一行
				
				// PART 添加一条新增配置项记录
				changeSets.addCreateItem(buildNormalItem(0L, namespaceId, key, value, deletedItemDto.getComment(), lineNum));
				
			// BRANCH 如果新数据存在该配置项，且配置值不同或者行号不同，则更新
			} else if (! (currItem.getValue().equals(value)) || (isLineNumUpdatingNeeded && (seq != currItem.getLineNum()))) {
				// PART 获取行号
				int lineNum = isLineNumUpdatingNeeded ? seq : currItem.getLineNum(); // NOTE 若需要更新行号，则使用自增序号作为行号，否则使用原始配置项的行号
				
				// PART 添加一条更新配置项记录
				changeSets.addUpdateItem(buildNormalItem(currItem.getId(), namespaceId, key, value, currItem.getComment(), lineNum));
				
			}
			
			// SUBSTEP 该配置项已经处理完毕，从新数据的配置项Map中移除
			currBaseKeyItemMap.remove(key);
			
		}
		
		// STEP 处理新数据的所有剩余配置项，将其全部添加到待删除列表中(仅在需要更新行号或者Key不为空时)
		currBaseKeyItemMap.forEach((key, value) -> {
			if (isLineNumUpdatingNeeded || StringUtils.isNotBlank(key)) {
				changeSets.addDeleteItem(currBaseKeyItemMap.get(key));
				
			}
			
		});
		
		// STEP 返回变更集合对象
		return changeSets;
		
	}
	
	// ===== ===== ===== ===== [实例工具方法-通用] ===== ===== ===== ===== //
	
	/**
	 * 构筑一个普通类型的配置项对象
	 * @param id 配置项ID
	 * @param namespaceId 配置项所属的Namespace的ID
	 * @param key 配置项的Key
	 * @param value 配置项的Value
	 * @param comment 配置项的注释
	 * @param lineNum 配置项的行号
	 * @return {@code ItemDTO} 构筑结果
	 * @author ZhouYi
	 * @date 2023/4/4 12:56
	 * @note note
	 */
	private ItemDTO buildNormalItem(Long id, Long namespaceId, String key, String value, String comment, int lineNum) {
		ItemDTO item = new ItemDTO(key, value, comment, lineNum);
		item.setId(id);
		item.setNamespaceId(namespaceId);
		return item;
		
	}
	
	// ===== ===== ===== ===== [静态工具方法-通用] ===== ===== ===== ===== //
	
	
	
	
	
}
