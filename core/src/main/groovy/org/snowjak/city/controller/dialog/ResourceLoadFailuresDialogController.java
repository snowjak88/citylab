/**
 * 
 */
package org.snowjak.city.controller.dialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.snowjak.city.map.generator.MapGenerator;
import org.snowjak.city.map.tiles.TileSet;
import org.snowjak.city.module.Module;
import org.snowjak.city.service.GameAssetService;
import org.snowjak.city.service.GameAssetService.LoadFailureBean;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.mvc.component.ui.InterfaceService;
import com.github.czyzby.autumn.mvc.stereotype.ViewDialog;
import com.github.czyzby.lml.annotation.LmlAction;
import com.github.czyzby.lml.parser.action.ActionContainer;
import com.github.czyzby.lml.util.LmlUtilities;

/**
 * @author snowjak88
 *
 */
@ViewDialog(id = "resourceLoadFailures", value = "ui/templates/dialogs/resourceLoadFailures.lml")
public class ResourceLoadFailuresDialogController implements ActionContainer {
	
	@Inject
	private GameAssetService assetService;
	
	@Inject
	private InterfaceService interfaceService;
	
	private Map<AssetType, List<Integer>> loadFailures = null;
	private ArrayList<LoadFailureBean> loadFailuresByID = null;
	
	@LmlAction
	public List<AssetType> getAssetTypes() {
		
		checkLoadFailures();
		
		return new LinkedList<>(loadFailures.keySet());
	}
	
	@LmlAction
	public List<Integer> getLoadFailureIDs(final Actor actor) {
		
		checkLoadFailures();
		
		final AssetType assetType = AssetType.valueOf(LmlUtilities.getActorId(actor));
		
		return loadFailures.get(assetType);
	}
	
	@LmlAction
	public String getLoadFailureFile(final Actor actor) {
		
		checkLoadFailures();
		
		final int loadFailureIndex = Integer.parseInt(LmlUtilities.getActorId(actor));
		return loadFailuresByID.get(loadFailureIndex).getFile().path();
	}
	
	@LmlAction
	public String getLoadFailureMessage(final Actor actor) {
		
		checkLoadFailures();
		
		final int loadFailureIndex = Integer.parseInt(LmlUtilities.getActorId(actor));
		return loadFailuresByID.get(loadFailureIndex).getException().getMessage();
	}
	
	private void checkLoadFailures() {
		
		if (loadFailures == null)
			synchronized (this) {
				if (loadFailures == null) {
					
					loadFailures = new HashMap<>();
					loadFailuresByID = new ArrayList<>();
					int index = 0;
					
					for (LoadFailureBean lf : assetService.getLoadFailures()) {
						
						loadFailuresByID.add(lf);
						loadFailures.computeIfAbsent(AssetType.getFor(lf.getAssetType()), (a) -> new LinkedList<>())
								.add(index);
						
						index++;
					}
				}
			}
	}
	
	public enum AssetType {
		
		TILESET(TileSet.class, 1),
		MAPGENERATOR(MapGenerator.class, 2),
		MODULE(Module.class, 3),
		OTHER(Object.class, 4);
		
		private final Class<?> assetType;
		private final int order;
		
		private AssetType(Class<?> assetType, int order) {
			
			this.assetType = assetType;
			this.order = order;
		}
		
		public boolean isType(Class<?> assetType) {
			
			return this.assetType.isAssignableFrom(assetType);
		}
		
		public int getOrder() {
			
			return order;
		}
		
		public static AssetType getFor(Class<?> assetType) {
			
			for (AssetType t : AssetType.values())
				if (t.isType(assetType))
					return t;
			return null;
		}
	}
	
}
