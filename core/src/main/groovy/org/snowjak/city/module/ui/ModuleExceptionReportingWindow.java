package org.snowjak.city.module.ui;

import java.util.LinkedHashSet;
import java.util.Set;

import org.snowjak.city.module.ModuleExceptionRegistry;
import org.snowjak.city.module.ModuleExceptionRegistry.Failure;
import org.snowjak.city.service.I18NService;
import org.snowjak.city.service.SkinService;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextTooltip;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

/**
 * Acts as a pop-up for a single {@link ModuleExceptionRegistry.Failure}.
 * 
 * @author snowjak88
 *
 */
public class ModuleExceptionReportingWindow extends Window {
	
	private final I18NService i18nService;
	private final SkinService skinService;
	
	private Failure failure = null;
	
	private Set<Runnable> onDismissActions = new LinkedHashSet<>();
	
	private final Label moduleId, moduleFilename, failureDomain, failureMessage;
	private final TextTooltip moduleFullFilename;
	private final TextButton copyStacktraceButton, dismissButton;
	
	public ModuleExceptionReportingWindow(I18NService i18nService, SkinService skinService) {
		
		super(i18nService.get("module-failure-window-title"), skinService.getCurrent());
		this.i18nService = i18nService;
		this.skinService = skinService;
		
		setModal(true);
		setMovable(false);
		setVisible(false);
		
		final Skin skin = skinService.getCurrent();
		
		final Label textLabel = new Label(i18nService.get("module-failure-window-text"), skin);
		
		final Label moduleIdLabel = new Label(i18nService.get("module-failure-window-moduleid"), skin);
		moduleId = new Label("???", skin, "mono");
		
		final Label moduleFilenameLabel = new Label(i18nService.get("module-failure-window-modulefile"), skin);
		moduleFilename = new Label("", skin, "mono");
		moduleFullFilename = new TextTooltip("???", skin);
		moduleFilename.addListener(moduleFullFilename);
		
		failureDomain = new Label("???", skin);
		failureMessage = new Label("???", skin, "mono");
		failureMessage.setWrap(true);
		
		copyStacktraceButton = new TextButton(i18nService.get("module-failure-window-copystack"), skin);
		copyStacktraceButton.addListener(new ChangeListener() {
			
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				
				final TextButton b = (TextButton) actor;
				
				if (!b.isChecked())
					return;
				if (failure == null || failure.getStacktrace() == null)
					return;
				copyStackTraceToClipboard();
				copyStacktraceButton.setText(i18nService.get("module-failure-window-copiedstack"));
				b.setChecked(false);
			}
		});
		
		dismissButton = new TextButton(i18nService.get("module-failure-window-dismiss"), skin);
		final Window thisWindow = this;
		dismissButton.addListener(new ChangeListener() {
			
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				
				final TextButton b = (TextButton) actor;
				if (!b.isChecked())
					return;
				onDismissActions.forEach(Runnable::run);
				thisWindow.setVisible(false);
				b.setChecked(false);
			}
		});
		
		defaults().padLeft(25).padRight(25);
		
		row().colspan(2);
		add(textLabel).padTop(25).padBottom(25);
		
		row().left().top();
		add(moduleIdLabel).padLeft(25);
		add(moduleId).padRight(25);
		
		row().left().top();
		add(moduleFilenameLabel).padLeft(25);
		add(moduleFilename).padRight(25);
		
		row().left().top();
		add(failureDomain).padLeft(25);
		add(failureMessage).fill().padRight(25);
		
		row().padTop(50).padBottom(25);
		add(copyStacktraceButton).right();
		add(dismissButton).left();
		
		pack();
	}
	
	public void addDismissAction(Runnable action) {
		
		onDismissActions.add(action);
	}
	
	public void setFailure(Failure failure) {
		
		resetTexts();
		
		this.failure = failure;
		
		moduleId.setText(failure.getModuleID());
		moduleFilename.setText(failure.getModuleFile());
		moduleFullFilename.getActor().setText(failure.getModuleFullFile());
		if (failure.getDomain().getBundleKey() != null)
			failureDomain.setText(i18nService.get(failure.getDomain().getBundleKey()));
		
		failureMessage.setText(failure.getExceptionMessage());
		
		pack();
	}
	
	private void resetTexts() {
		
		moduleId.setText("???");
		moduleFilename.setText("???");
		moduleFullFilename.getActor().setText("???");
		failureDomain.setText("???");
		failureMessage.setText("???");
		copyStacktraceButton.setText(i18nService.get("module-failure-window-copystack"));
	}
	
	private void copyStackTraceToClipboard() {
		
		if (failure == null || failure.getStacktrace() == null)
			return;
		
		Gdx.app.getClipboard().setContents(failure.getStacktrace());
	}
}
