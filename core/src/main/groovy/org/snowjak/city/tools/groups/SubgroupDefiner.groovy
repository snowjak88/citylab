/**
 * 
 */
package org.snowjak.city.tools.groups

import org.snowjak.city.util.PrioritizationFailedException
import org.snowjak.city.util.RelativePriorityList


/**
 * This trait bestows the ability to manage a prioritized list of sub-groups.
 * 
 * @author snowjak88
 *
 */
trait SubgroupDefiner<T extends ToolGroup> {
	
	public Map<String,T> groups
	public final RelativePriorityList<String, ? extends ToolGroup> prioritizedSubgroups = new RelativePriorityList()
	
	public T group(String id) {
		if(!groups.containsKey(id))
			throw new IllegalArgumentException("Cannot reference tool-group '$id' before it is defined.")
		
		groups[id]
	}
	
	public T group(Closure groupSpec) throws PrioritizationFailedException {
		final group = newDelegate()
		groupSpec = groupSpec.rehydrate(group, this, this)
		groupSpec.resolveStrategy = Closure.DELEGATE_FIRST
		groupSpec()
		afterDelegation(group)
		
		if(group.id == null || group.id.isBlank())
			throw new IllegalArgumentException("Cannot define a new tool-group without an 'id'.")
		
		if(groups[group.id] != null)
			prioritizedSubgroups.remove group
		
		try {
			prioritizedSubgroups.add group
		} catch(PrioritizationFailedException e) {
			throw new PrioritizationFailedException("Cannot prioritize sub-group '$id'/'$group.id' -- incompatible priorities.")
		}
		
		groups[group.id] = group
		
		group
	}
	
	/**
	 * Produce a new instance of this group-type, configured to be used as the delegate to a group-spec/Closure.
	 * @return
	 */
	public abstract T newDelegate()
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
