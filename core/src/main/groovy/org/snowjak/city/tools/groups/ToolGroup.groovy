/**
 * 
 */
package org.snowjak.city.tools.groups

import org.snowjak.city.resources.AssetDependent
import org.snowjak.city.tools.activation.GroupedActivationMethod
import org.snowjak.city.util.RelativePriority
import org.snowjak.city.util.RelativelyPrioritized

import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.scenes.scene2d.Actor

/**
 * @author snowjak88
 *
 */
abstract class ToolGroup<T extends ToolGroup> implements SubgroupDefiner<T>, RelativelyPrioritized<T, String>, AssetDependent {
	final String id = ""
	final GroupedActivationMethod context
	
	final T parent
	
	final List<GroupedActivationMethod> activationMethods = []
	final FileHandle baseDirectory
	
	final RelativePriority<String> relativePriority = new RelativePriority()
	
	public ToolGroup(String id, GroupedActivationMethod context = null, Map<String,T> groups, FileHandle baseDirectory, T parent) {
		
		this.org_snowjak_city_tools_groups_SubgroupDefiner__groups = groups
		
		this.id = id
		this.context = context
		this.parent = parent
		this.baseDirectory = baseDirectory
	}
	
	@Override
	public void afterDelegation(T delegate) {
		
		if(context)
			context.grouping = delegate
	}
	
	public abstract Actor getRepresentation()
	
	public RelativePriority<String> after(String... ids) {
		relativePriority.after(ids)
	}
	
	public RelativePriority<String> before(String... ids) {
		relativePriority.before(ids)
	}
	
	@Override
	public String getRelativePriorityKey() {
		id
	}
}
