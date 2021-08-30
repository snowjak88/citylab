/**
 * 
 */
package org.snowjak.city.tools.activation

import org.snowjak.city.tools.groups.GroupDefiner
import org.snowjak.city.tools.groups.ToolGroup
import org.snowjak.city.util.RelativePriority
import org.snowjak.city.util.RelativelyPrioritized

/**
 * @author snowjak88
 *
 */
abstract class GroupedActivationMethod<T extends ToolGroup> extends GroupDefiner<T> implements ActivationMethod, RelativelyPrioritized<GroupedActivationMethod, String> {

	T grouping

	final RelativePriority<String> relativePriority = new RelativePriority()

	public GroupedActivationMethod(Map<String,T> groups) {
		super(groups)
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
