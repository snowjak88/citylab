package org.snowjak.city.tools.groups

import com.badlogic.gdx.files.FileHandle

/**
 * This trait bestows the ability to define groups for:
 * <ul>
 * <li>Buttons (using {@link ButtonToolGroup})</li>
 * <li>Menu-items (using {@link MenuToolGroup})</li>
 * </ul>
 * @author snowjak88
 *
 */
trait GroupsDefiner {
	
	public final Map<String,ButtonToolGroup> buttonGroups = new LinkedHashMap<>()
	public final Map<String,MenuToolGroup> menuGroups = new LinkedHashMap<>()
	
	public MenuToolGroup menuGroup(String id) {
		if(!menuGroups.containsKey(id))
			throw new IllegalArgumentException("Cannot reference menu tool-group '$id' before it is defined!")
		menuGroups[id]
	}
	
	public MenuToolGroup menuGroup(String id, @DelegatesTo(value=MenuToolGroup, strategy=Closure.DELEGATE_FIRST) Closure groupSpec) {
		final group = new MenuToolGroup(id, menuGroups, baseDirectory)
		groupSpec = groupSpec.rehydrate(group, this, this)
		groupSpec.resolveStrategy = Closure.DELEGATE_FIRST
		groupSpec()
		
		menuGroups[group.id] = group
		group
	}
	
	public ButtonToolGroup buttonGroup(String id) {
		if(!buttonGroups.containsKey(id))
			throw new IllegalArgumentException("Cannot reference button tool-group '$id' before it is defined!")
		buttonGroups[id]
	}
	
	public ButtonToolGroup buttonGroup(String id, @DelegatesTo(value=ButtonToolGroup, strategy=Closure.DELEGATE_FIRST) Closure groupSpec) {
		final group = new ButtonToolGroup(id, buttonGroups, baseDirectory)
		groupSpec = groupSpec.rehydrate(group, this, this)
		groupSpec.resolveStrategy = Closure.DELEGATE_FIRST
		groupSpec()
		
		buttonGroups[id] = group
		group
	}
	
	public abstract FileHandle getBaseDirectory();
}
