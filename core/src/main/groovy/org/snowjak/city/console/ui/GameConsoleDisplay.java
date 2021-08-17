/**
 * 
 */
package org.snowjak.city.console.ui;

import java.util.LinkedList;

import org.snowjak.city.service.SkinService;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;

/**
 * @author snowjak88
 *
 */
public class GameConsoleDisplay extends Group {
	
	/**
	 * Console will display at most N lines of history.
	 */
	public static final int MAX_CONSOLE_ENTRIES = 4096;
	/**
	 * Console will permit at most N characters of input.
	 */
	public static final int CONSOLE_INPUT_BUFFER_LENGTH = 4096;
	
	private final Skin consoleSkin;
	private final VerticalGroup root;
	private final TextArea inputTextArea;
	private final ScrollPane scrollPane;
	private final Table consoleEntriesTable;
	private final LinkedList<Label> consoleEntries = new LinkedList<>();
	
	public GameConsoleDisplay(SkinService skinService) {
		
		super();
		
		consoleSkin = skinService.getSkin("default");
		
		consoleEntriesTable = new Table(consoleSkin);
		scrollPane = new ScrollPane(consoleEntriesTable);
		
		inputTextArea = new TextArea("", consoleSkin);
		inputTextArea.setMaxLength(4096);
		inputTextArea.setPrefRows(1.5f);
		
		root = new VerticalGroup();
		root.addActor(scrollPane);
		root.addActor(inputTextArea);
		
		root.setFillParent(true);
		inputTextArea.setFillParent(true);
		scrollPane.setFillParent(true);
		consoleEntriesTable.setFillParent(true);
	}
	
	public void writeConsoleEntry(String text) {
		
	}
}
