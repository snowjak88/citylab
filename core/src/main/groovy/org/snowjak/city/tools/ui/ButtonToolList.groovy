/**
 * 
 */
package org.snowjak.city.tools.ui

import org.snowjak.city.service.GameAssetService
import org.snowjak.city.service.SkinService
import org.snowjak.city.tools.Tool
import org.snowjak.city.tools.activation.ButtonActivationMethod
import org.snowjak.city.tools.groups.ButtonToolGroup
import org.snowjak.city.util.TextureUtils
import org.snowjak.city.util.TextureUtils.MaskChannel

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Tree
import com.badlogic.gdx.scenes.scene2d.ui.Window
import com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle

/**
 * Handles rendering buttons for {@link ButtonActivationMethod button-activated} {@link Tool}s.
 * @author snowjak88
 *
 */
class ButtonToolList extends Window {
	
	private static final Color TRANSPARENT_COLOR = new Color(1, 1, 1, 0)
	private static final int BUTTON_SIZE = 64
	
	private final Map<String,Tool> tools = new LinkedHashMap<>()
	private final Map<String,ButtonToolGroup> groupsByID = new LinkedHashMap<>()
	private final Map<String,ButtonActivationMethod> buttonsByID = new LinkedHashMap<>()
	private final ModelNode root = new ModelNode()
	private final Map<String,ModelNode> modelByID = new LinkedHashMap<>()
	
	private final Tree<ButtonTreeNode,ButtonActivationMethod> buttonTree
	private final ScrollPane scrollPane;
	
	private final SkinService skinService
	private final GameAssetService assetService
	
	private final Texture buttonMask, buttonHighlight
	
	public ButtonToolList(SkinService skinService, GameAssetService assetService) {
		
		super("", skinService.getCurrent())
		
		this.skinService = skinService
		this.assetService = assetService
		
		assetService.load "ui/buttons/button_mask.png", Texture
		assetService.load "ui/buttons/button_mask_highlight.png", Texture
		assetService.finishLoading "ui/buttons/button_mask.png"
		assetService.finishLoading "ui/buttons/button_mask_highlight.png"
		
		buttonMask = assetService.get("ui/buttons/button_mask.png", Texture)
		buttonHighlight = assetService.get("ui/buttons/button_mask_highlight.png", Texture)
		
		pad 0
		movable = false
		modal = false
		resizable = false
		
		buttonTree = new Tree<>(skin)
		buttonTree.indentSpacing = BUTTON_SIZE
		
		scrollPane = new ScrollPane(buttonTree, skin)
		scrollPane.scrollbarsVisible = false
		scrollPane.overscrollDistance = 0
		
		add scrollPane
	}
	
	public void add(Tool tool) {
		// TODO
		tool.activationMethods.each {
			if(it instanceof ButtonActivationMethod)
				mergeWithModel it as ButtonActivationMethod
		}
	}
	
	private void mergeWithModel(ButtonActivationMethod button) {
		final ButtonToolGroup buttonGroup = button.org_snowjak_city_tools_groups_SubgroupDefiner__thisGroup
		def groupNode = mergeWithModel(buttonGroup)
	}
	
	private ModelNode mergeWithModel(ButtonToolGroup group) {
		
	}
	
	private void newGroupButton(ButtonToolGroup group) {
		
		def style = new ButtonStyle()
		style.up = processButtonTexture( assetService.get(group.buttonUp.path(), Texture.class) )
		style.down = processButtonTexture( assetService.get(group.buttonDown.path(), Texture.class) )
		style.disabled = processButtonTexture( assetService.get(group.buttonUp.path(), Texture.class), false, true )
		
		new Button(style)
	}
	
	private void newToolButton(ButtonActivationMethod button) {
		
		def style = new ButtonStyle()
		style.up = processButtonTexture( assetService.get(button.buttonUp.path(), Texture.class) )
		style.down = processButtonTexture( assetService.get(button.buttonDown.path(), Texture.class) )
		style.checked = processButtonTexture( assetService.get(button.buttonUp.path(), Texture.class), true )
		style.disabled = processButtonTexture( assetService.get(button.buttonUp.path(), Texture.class), false, true )
		
		new Button(style)
	}
	
	/**
	 * Process the given button-texture to ensure:
	 * <ul>
	 * <li>It is resized to the expected size and centered (if necessary)</li>
	 * <li>It is masked properly</li>
	 * <li>A highlight is added if required</li>
	 * </ul>
	 */
	private Texture processButtonTexture(Texture buttonTexture, boolean includeHighlight = false, boolean grayscale = false) {
		
		if(buttonTexture.height != buttonTexture.height)
			buttonTexture = TextureUtils.center(buttonTexture, TRANSPARENT_COLOR)
		final maskedTexture = TextureUtils.mask(buttonTexture, buttonMask, MaskChannel.ALPHA, TRANSPARENT_COLOR, false)
		
		final Texture highlit
		if(includeHighlight)
			highlit = TextureUtils.layer(maskedTexture, buttonHighlight, 0, 0)
		else
			highlit = maskedTexture
		
		final Texture grayed
		if(grayscale)
			grayed = TextureUtils.grayscale(highlit)
		else
			grayed = highlit
		
		TextureUtils.resize(grayed, BUTTON_SIZE, BUTTON_SIZE)
	}
	
	private static class ModelNode {
		String id
		ModelNode parent
		final Set<ModelNode> childGroups = []
		final Set<String> buttons = []
	}
	
	private static class ButtonTreeNode extends Tree.Node<ButtonTreeNode, ModelNode, Button> {
	}
}
