/**
 * 
 */
package org.snowjak.city.tools.activation

import org.snowjak.city.tools.groups.SubgroupDefiner
import org.snowjak.city.tools.groups.ToolGroup
import org.snowjak.city.util.RelativePriority
import org.snowjak.city.util.RelativelyPrioritized

/**
 * @author snowjak88
 *
 */
abstract class GroupedActivationMethod<T extends ToolGroup> implements SubgroupDefiner<T>, ActivationMethod, RelativelyPrioritized<GroupedActivationMethod, String> {
	
	final RelativePriority<String> relativePriority = new RelativePriority()
	
	public GroupedActivationMethod(Map<String,T> groups) {
		this.org_snowjak_city_tools_groups_SubgroupDefiner__groups = groups
	}
	
	@Override
	public String getRelativePriorityKey() {
		
		id
	}
	
	public RelativePriority<String> after(String...ids) {
		relativePriority.after(ids)
	}
	
	public RelativePriority<String> before(String...ids) {
		relativePriority.before(ids)
	}
}
