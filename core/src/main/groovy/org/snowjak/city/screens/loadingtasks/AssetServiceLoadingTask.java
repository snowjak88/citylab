/**
 * 
 */
package org.snowjak.city.screens.loadingtasks;

import org.snowjak.city.service.GameAssetService;
import org.snowjak.city.service.I18NService;

import com.github.czyzby.autumn.annotation.Component;
import com.github.czyzby.autumn.annotation.Inject;

/**
 * {@link LoadingTask} that monitors the loading-progress of the
 * {@link GameAssetService}.
 * 
 * @author snowjak88
 *
 */
@Component
public class AssetServiceLoadingTask extends LoadingTask {
	
	@Inject
	private GameAssetService assetService;
	
	@Inject
	private I18NService i18nService;
	
	@Override
	public boolean isComplete() {
		
		return assetService.isFinished();
	}
	
	@Override
	public float getProgress() {
		
		assetService.update();
		return assetService.getProgress();
	}
	
	@Override
	public String getDescription() {
		
		return i18nService.get("loading-description");
	}
}
