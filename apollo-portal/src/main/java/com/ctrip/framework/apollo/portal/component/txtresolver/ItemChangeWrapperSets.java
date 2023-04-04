package com.ctrip.framework.apollo.portal.component.txtresolver;

import com.ctrip.framework.apollo.common.dto.BaseDTO;
import com.ctrip.framework.apollo.common.dto.ItemChangeSets;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 配置项变更{@link ItemDTOWrapper}集合封装
 * @author ZhouYi
 * @date 2023/4/3 18:30
 * @note note
 */
public class ItemChangeWrapperSets extends BaseDTO {
	
	/** 新增项列表 */
	private List<ItemDTOWrapper> createItems = new ArrayList<>();
	
	/** 新增项列表 */
	private List<ItemDTOWrapper> updateItems = new ArrayList<>();
	
	/** 新增项列表 */
	private List<ItemDTOWrapper> deleteItems = new ArrayList<>();
	
	// ===== ===== ===== ===== [操作方法-添加配置项] ===== ===== ===== ===== //
	
	public void addCreateItem(ItemDTOWrapper item) {
		createItems.add(item);
	}
	
	public void addUpdateItem(ItemDTOWrapper item) {
		updateItems.add(item);
	}
	
	public void addDeleteItem(ItemDTOWrapper item) {
		deleteItems.add(item);
	}
	
	public boolean isEmpty() {
		return createItems.isEmpty() && updateItems.isEmpty() && deleteItems.isEmpty();
	}
	
	// ===== ===== ===== ===== [操作方法-Getters & Setters] ===== ===== ===== ===== //
	
	public List<ItemDTOWrapper> getCreateItems() {
		return createItems;
	}
	
	public List<ItemDTOWrapper> getUpdateItems() {
		return updateItems;
	}
	
	public List<ItemDTOWrapper> getDeleteItems() {
		return deleteItems;
	}
	
	public void setCreateItems(List<ItemDTOWrapper> createItems) {
		this.createItems = createItems;
	}
	
	public void setUpdateItems(List<ItemDTOWrapper> updateItems) {
		this.updateItems = updateItems;
	}
	
	public void setDeleteItems(List<ItemDTOWrapper> deleteItems) {
		this.deleteItems = deleteItems;
	}
	
	// ===== ===== ===== ===== [操作方法-类转化] ===== ===== ===== ===== //
	
	/** 将当前对象转化为{@link ItemChangeSets} */
	public ItemChangeSets toItemChangeSets() {
		ItemChangeSets dest = new ItemChangeSets();
		dest.setCreateItems(this.getCreateItems().stream().map(ItemDTOWrapper::getItemDTO).collect(Collectors.toList()));
		dest.setUpdateItems(this.getUpdateItems().stream().map(ItemDTOWrapper::getItemDTO).collect(Collectors.toList()));
		dest.setDeleteItems(this.getDeleteItems().stream().map(ItemDTOWrapper::getItemDTO).collect(Collectors.toList()));
		dest.setDataChangeCreatedBy(this.getDataChangeCreatedBy());
		dest.setDataChangeLastModifiedBy(this.getDataChangeLastModifiedBy());
		dest.setDataChangeCreatedTime(this.getDataChangeCreatedTime());
		dest.setDataChangeLastModifiedTime(this.getDataChangeLastModifiedTime());
		return dest;
		
	}
	
}
