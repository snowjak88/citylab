/**
 * 
 */
package org.snowjak.city.tools.ui

import java.util.function.Consumer

import org.snowjak.city.service.GameAssetService
import org.snowjak.city.service.GameService
import org.snowjak.city.service.I18NService
import org.snowjak.city.service.SkinService
import org.snowjak.city.tools.Tool
import org.snowjak.city.tools.ToolButton
import org.snowjak.city.tools.ToolGroup
import org.snowjak.city.util.TextureUtils
import org.snowjak.city.util.TextureUtils.MaskChannel

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.Tooltip
import com.badlogic.gdx.scenes.scene2d.ui.Window
import com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent
import com.badlogic.gdx.utils.Align

/**
 * Handles rendering buttons for {@link ButtonActivationMethod button-activated} {@link Tool}s.
 * @author snowjak88
 *
 */
class Toolbar extends Window {
	
	public static final String BUTTON_MASK_FILENAME = "ui/buttons/button_mask.png"
	public static final String BUTTON_MASK_SUNKEN_FILENAME = "ui/buttons/button_mask_sunken.png"
	public static final String BUTTON_HIGHLIGHT_FILENAME = "ui/buttons/button_mask_highlight.png"
	
	private static final Color TRANSPARENT_COLOR = new Color(1, 1, 1, 0)
	private static final int BUTTON_SIZE = 64
	private static final float BUTTON_FOLD_DURATION = 0.2f
	
	private static final ToolGroup UNCATEGORIZED_GROUP = new ToolGroup("")
	static {
		UNCATEGORIZED_GROUP.title = "(...)"
	}
	
	private final Map<String,Tool> tools = [:]
	private final Map<String,ToolGroup> groups = [:]
	private final Map<String,ToolButton> buttonDefs = [:]
	private final Map<String,Set<ToolButton>> groupButtonDefs = [:]
	
	private final Set<String> groupExpanded = []
	private final Map<String,Label> groupLabels = [:]
	private final Map<String,Button> groupExpandButtons = [:]
	private final Map<String,Set<Button>> groupToolButtons = [:]
	private final Map<String,Button> buttons = [:]
	private final Map<String,Label> buttonToolTipLabels = [:]
	
	private final Table buttonTable;
	private final ScrollPane scrollPane;
	
	private final Button expandCollapseAllButton
	
	private final SkinService skinService
	private final I18NService i18nService
	private final GameService gameService
	private final GameAssetService assetService
	
	private final Texture buttonMask, buttonSunkenMask, buttonHighlight
	
	private String activeToolName
	
	private boolean rebuildNeeded = false
	
	public Toolbar(I18NService i18nService, SkinService skinService, GameService gameService, GameAssetService assetService, Runnable scrollFocusCanceller) {
		
		super(i18nService.get("tool-button-list-title"), skinService.current)
		
		this.skinService = skinService		this.i18nService = i18nService
		this.gameService = gameService
		this.assetService = assetService
		
		assetService.load BUTTON_MASK_FILENAME, Texture
		assetService.load BUTTON_MASK_SUNKEN_FILENAME, Texture
		assetService.load BUTTON_HIGHLIGHT_FILENAME, Texture
		
		assetService.finishLoading BUTTON_MASK_FILENAME
		assetService.finishLoading BUTTON_MASK_SUNKEN_FILENAME
		assetService.finishLoading BUTTON_HIGHLIGHT_FILENAME
		
		buttonMask = assetService.get(BUTTON_MASK_FILENAME, Texture)
		buttonSunkenMask = assetService.get(BUTTON_MASK_SUNKEN_FILENAME, Texture)
		buttonHighlight = assetService.get(BUTTON_HIGHLIGHT_FILENAME, Texture)
		
		movable = true
		modal = false
		resizable = false
		
		buttonTable = new Table(skinService.current)
		buttonTable.align = Align.topLeft
		
		scrollPane = new ScrollPane(buttonTable, skin)
		scrollPane.scrollbarsVisible = false
		scrollPane.overscrollDistance = 0
		
		expandCollapseAllButton = new Button(skin, "minus")
		expandCollapseAllButton.addListener( [
			changed: { ChangeEvent e, Actor a ->
				final b = a as Button
				if(!b.checked)
					return
				b.checked = false
				
				final expandAll = ( groupExpanded.isEmpty() )
				groups.keySet().each { groupID -> setToolGroupExpanded(groupID, expandAll) }
			}
		] as ChangeListener)
		
		this.addListener([
			exit: {event, x, y, pointer, toActor ->
				scrollFocusCanceller.run()
			}
		] as InputListener)
		
		add scrollPane
	}
	
	@Override
	public void setSkin(Skin skin) {
		
		super.setSkin(skin);
		
		buttonTable?.skin = skin
		groupToolButtons?.values().each {
			it?.each {
				it?.skin = skin
			}
		}
		groupExpandButtons?.values().each {
			it?.skin = skin
		}
		
		rebuildNeeded = true
		checkRebuild()
	}
	
	/**
	 * Add the given Tools to this list.
	 * @param tools
	 */
	public void addTools(Collection<Tool> tools) {
		addTools tools.toArray(new Tool[0])
	}
	
	/**
	 * Add one or more Tools to this list.
	 * <p>
	 * This method will cause the entire button-list to be rebuilt, so try to batch your Tool-additions.
	 * </p>
	 * @param tools
	 */
	public void addTools(Tool...tools) {
		
		//
		// Add all buttons and groups and things.
		//
		tools.each { tool ->
			//
			// If we'd already registered a tool under this same ID,
			// we'd better make sure we remove the old tool.
			if(this.tools.containsKey(tool.id))
				removeTools this.tools[tool.id]
			
			this.tools << [ "$tool.id" : tool ]
			tool.groups.each { id, group -> addToolGroup group }
			tool.buttons.each { id, button -> addToolButton button }
		}
		
		//
		// Rebuild the table
		//
		checkRebuild()
	}
	
	public void removeAllTools() {
		removeTools tools.values()
	}
	
	public void removeTools(Collection<Tool> tools) {
		removeTools tools.toArray(new Tool[0])
	}
	
	public void removeTools(Tool... tools) {
		tools.each { tool ->
			//tool.groups.each { id, group -> removeToolGroup group }
			tool.buttons.each { id, button -> removeToolButton button }
		}
		
		checkRebuild()
	}
	
	/**
	 * @return {@code true} if this list is the actor with the scroll-focus
	 */
	public boolean isScrollFocus() {
		scrollPane?.hasScrollFocus()
	}
	
	private void checkRebuild() {
		
		if(!rebuildNeeded)
			return
		
		if(buttonTable) {
			buttonTable.clear()
			
			buttonTable.row().left()
			buttonTable.add(new Label(i18nService.get("tool-button-list-all"), skin, "title")).fillX().prefWidth(120)
			buttonTable.add expandCollapseAllButton
			
			groups?.each { groupID, groupDef ->
				
				if(groupButtonDefs[groupID] == null || groupButtonDefs[groupID].isEmpty())
					return
				
				final groupLabel = groupLabels[groupID]
				final groupExpandButton = groupExpandButtons[groupID]
				buttonTable.row().left()
				buttonTable.add(groupLabel).fillX().prefWidth(120)
				buttonTable.add(groupExpandButton).center()
				
				groupButtonDefs[groupID]?.each { buttonDef ->
					
					final button = buttons[buttonDef.id]
					buttonTable.row().center()
					buttonTable.add(button)
					
				}
			}
			
		}
		
		this.pack()
		
		rebuildNeeded = false
	}
	
	/**
	 * Add a new tool-group to the list. If a tool-group with a matching ID already exists,
	 * this merely updates the tool-group's attributes.
	 * <p>
	 * A tool-group is composed of several rows in the host Table:
	 * <ul>
	 * <li>1 row with the group name and an expand/collapse button in separate cells</li>
	 * <li>1 row for every button in the group</li>
	 * </ul>
	 * The expand/collapse button is wired so that, when it is clicked, all its associated
	 * button-rows are made visible/invisible.
	 * </p>
	 * @param group
	 */
	private void addToolGroup(ToolGroup group) {
		
		final groupID = group.id
		
		if(groups.containsKey(groupID)) {
			//
			// Only update group-label
			groupLabels[groupID].text = group.title
			
			rebuildNeeded = true
			return
		}
		
		groups[groupID] = group
		
		
		final groupLabel = new Label(group.title, skinService.current)
		final groupExpandButton = new Button(skinService.current, "minus")
		groupExpandButton.addListener( [ changed: { ChangeEvent e, Actor a ->
				final b = a as Button
				if(b.checked) {
					setToolGroupExpanded groupID, !(groupExpanded.contains(groupID))
					b.checked = false
				}
			} ] as ChangeListener)
		
		groupLabels[groupID] = groupLabel
		groupExpandButtons[groupID] = groupExpandButton
		groupExpanded << groupID
		
		rebuildNeeded = true
	}
	
	private void removeToolGroup(ToolGroup group) {
		
		final groupID = group.id
		
		groupLabels.remove(groupID)?.remove()
		groupExpandButtons.remove(groupID)?.remove()
		
		groupExpanded.remove groupID
		
		groups.remove groupID
		groupButtonDefs.remove groupID
		groupToolButtons.remove(groupID)?.each { it.remove() }
		rebuildNeeded = true
	}
	
	/**
	 * Adds the given tool-button to the list.
	 * <p>
	 * This does <strong>not</strong> handle adding the button's referenced
	 * tool-group to the list. Rather, this button references a group by ID;
	 * at draw-time
	 * </p>
	 * @param button
	 */
	private void addToolButton(ToolButton buttonDef) {
		
		//
		// Does the button's referenced group exist?
		final groupID
		if(groups.containsKey(buttonDef.group))
			groupID = buttonDef.group
		else {
			if(!groups.containsKey(UNCATEGORIZED_GROUP.id))
				addToolGroup UNCATEGORIZED_GROUP
			groupID = UNCATEGORIZED_GROUP.id
		}
		
		if(buttonDefs.containsKey(buttonDef.id))
			removeToolButton buttonDef
		
		final button = createToolButton(buttonDef)
		button.addListener( [ changed: { ChangeEvent e, Actor a ->
				final b = a as Button
				if(b.disabled) {
					e.cancel()
					return
				}
				if(b.checked)
					buttonDef.tool.activate()
				else
					buttonDef.tool.deactivate()
			} ] as ChangeListener )
		
		buttonDef.tool.enabledListeners << ( { Tool t -> setToolButtonEnabled(buttonDef.id, t.enabled) } as Consumer<Tool> )
		buttonDef.tool.activateListeners << ( { Tool t -> setToolButtonActive(buttonDef.id, true) } as Consumer<Tool> )
		buttonDef.tool.deactivateListeners << ( { Tool t -> setToolButtonActive(buttonDef.id, false) } as Consumer<Tool> )
		
		buttonDefs[buttonDef.id] = buttonDef
		groupButtonDefs.computeIfAbsent(groupID, { _ -> new LinkedHashSet<>() }) << buttonDef
		groupToolButtons.computeIfAbsent(groupID, { _ -> new LinkedHashSet<>() }) << button
		buttons[buttonDef.id] = button
		
		rebuildNeeded = true
	}
	
	private void removeToolButton(ToolButton buttonDef) {
		
		if(!buttons.containsKey(buttonDef.id))
			return
		
		final button = buttons.remove(buttonDef.id)
		buttonDefs.remove buttonDef.id
		groupButtonDefs[buttonDef.group]?.remove buttonDef
		groupToolButtons[buttonDef.group]?.remove(button)
		button.remove()
		
		rebuildNeeded = true
	}
	
	private void setToolButtonEnabled(String buttonID, boolean enabled) {
		
		buttons[buttonID]?.disabled = !enabled
	}
	
	private void setToolButtonActive(String buttonID, boolean active) {
		buttons[buttonID]?.checked = active
	}
	
	/**
	 * Set the given group's expansion:
	 * <ul>
	 * <li>Change the group's "expand" button's style (to "plus" or "minus")</li>
	 * <li>Add an {@link Action} to each of this group's buttons, to cause them to change size and fade in/out</li>
	 * </ul>
	 * @param groupID
	 * @param visible
	 */
	private void setToolGroupExpanded(String groupID, boolean expand) {
		
		if(expand) {
			groupExpandButtons[groupID]?.style = skinService.current.get("minus", ButtonStyle)
			groupToolButtons[groupID]?.each { b ->
				b.addAction Actions.sequence(
						Actions.visible(true),
						Actions.fadeIn(BUTTON_FOLD_DURATION)
						)
			}
			groupExpanded << groupID
		}
		else {
			groupExpandButtons[groupID]?.style = skinService.current.get("plus", ButtonStyle)
			groupToolButtons[groupID]?.each { b ->
				b.addAction Actions.sequence(
						Actions.fadeOut(BUTTON_FOLD_DURATION),
						Actions.visible(false)
						)
			}
			groupExpanded.remove groupID
		}
		
		checkExpandAllButton()
	}
	
	private void checkExpandAllButton() {
		expandCollapseAllButton.style = skin.get(( groupExpanded.isEmpty() ) ? "plus" : "minus", ButtonStyle )
	}
	
	/**
	 * Create a new {@link Button} configured to match the given {@link ToolButton} definition.
	 * @param button
	 * @return
	 */
	private Button createToolButton(ToolButton buttonDef) {
		
		final style = new ButtonStyle()
		style.up = processButtonTexture( assetService.get(buttonDef.buttonUp.path(), Texture.class) )
		style.down = processButtonTexture( assetService.get(buttonDef.buttonDown.path(), Texture.class), true )
		style.checked = processButtonTexture( assetService.get(buttonDef.buttonUp.path(), Texture.class), false, true )
		style.disabled = processButtonTexture( assetService.get(buttonDef.buttonUp.path(), Texture.class), false, false, true )
		
		final buttonToolTipLabel = new Label(buttonDef.tool.title, skin)
		buttonToolTipLabels[buttonDef.id] = buttonToolTipLabel
		
		final b = new Button(style)
		b.addListener(new Tooltip(buttonToolTipLabel))
		
		b
	}
	
	private void updateToolButton(Button button, ToolButton buttonDef) {
		final style = button.style
		style.up = processButtonTexture( assetService.get(buttonDef.buttonUp.path(), Texture.class) )
		style.down = processButtonTexture( assetService.get(buttonDef.buttonDown.path(), Texture.class), true )
		style.checked = processButtonTexture( assetService.get(buttonDef.buttonUp.path(), Texture.class), false, true )
		style.disabled = processButtonTexture( assetService.get(buttonDef.buttonUp.path(), Texture.class), false, false, true )
		
		buttonToolTipLabels[buttonDef.id]?.text = buttonDef.tool.title
	}
	
	/**
	 * Process the given button-texture to ensure:
	 * <ul>
	 * <li>It is resized to the expected size and centered (if necessary)</li>
	 * <li>It is masked properly</li>
	 * <li>A highlight is added if required</li>
	 * </ul>
	 */
	private TextureRegionDrawable processButtonTexture(Texture buttonTexture, boolean isSunken = false, boolean includeHighlight = false, boolean grayscale = false) {
		
		if(buttonTexture.height != buttonTexture.height)
			buttonTexture = TextureUtils.center(buttonTexture, TRANSPARENT_COLOR)
		final maskedTexture = TextureUtils.mask(buttonTexture, (isSunken) ? buttonSunkenMask : buttonMask, MaskChannel.ALPHA, TRANSPARENT_COLOR, false)
		
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
		
		new TextureRegionDrawable( TextureUtils.resize(grayed, BUTTON_SIZE, BUTTON_SIZE) )
	}
}
