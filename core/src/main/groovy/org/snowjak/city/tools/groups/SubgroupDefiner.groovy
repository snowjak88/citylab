/**
 * 
 */
package org.snowjak.city.tools.groups

import com.badlogic.gdx.files.FileHandle


/**
 * This trait bestows the ability to manage a prioritized list of sub-groups.
 * 
 * @author snowjak88
 *
 */
trait SubgroupDefiner<T extends ToolGroup> {
	
	public T thisGroup
	public Map<String,T> groups
	
	public T group(String id) {
		if(!groups.containsKey(id))
			throw new IllegalArgumentException("Cannot reference tool-group '$id' before it is defined.")
		
		thisGroup = groups[id]
		thisGroup
	}
	
	public T group(String id, Closure groupSpec) {
		final group = newDelegate(id, groups, this.baseDirectory, this)
		groupSpec = groupSpec.rehydrate(group, this, this)
		groupSpec.resolveStrategy = Closure.DELEGATE_FIRST
		groupSpec()
		afterDelegation(group)
		
		groups[id] = group
		
		thisGroup = group
		thisGroup
	}
	
	/**
	 * Produce a new instance of this group-type, configured to be used as the delegate to a group-spec/Closure.
	 * @return
	 */
	public abstract T newDelegate(String id, Map<String,T> groups, FileHandle baseDirectory, T parent)
	/**
	 * Perform any required processing to a group-instance after it has been used as the delegate to a group-spec/Closure.
	 * <p>
	 * The default implementation does nothing.
	 * </p>
	 * @param delegate
	 */
	public void afterDelegation(T delegate) {
	}
}
