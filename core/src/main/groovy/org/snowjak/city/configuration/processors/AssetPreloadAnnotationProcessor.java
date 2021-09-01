package org.snowjak.city.configuration.processors;

import org.snowjak.city.configuration.annotations.AssetPreload;
import org.snowjak.city.configuration.annotations.AssetPreloadItem;
import org.snowjak.city.service.GameAssetService;
import org.snowjak.city.service.LoggerService;

import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.context.Context;
import com.github.czyzby.autumn.context.ContextDestroyer;
import com.github.czyzby.autumn.context.ContextInitializer;
import com.github.czyzby.autumn.processor.AbstractAnnotationProcessor;
import com.github.czyzby.kiwi.log.Logger;

public class AssetPreloadAnnotationProcessor extends AbstractAnnotationProcessor<AssetPreload> {
	
	private static final Logger LOG = LoggerService.forClass(AssetPreloadAnnotationProcessor.class);
	
	@Inject
	private GameAssetService assetService;
	
	@Override
	public void processType(Class<?> type, AssetPreload annotation, Object component, Context context,
			ContextInitializer initializer, ContextDestroyer contextDestroyer) {
		
		LOG.info("Pre-loading assets for [{0}] ...", type.getName());
		for (AssetPreloadItem item : annotation.value()) {
			LOG.info("Pre-loading \"{0}\" [{1}] ...", item.value(), item.type());
			assetService.load(item.value(), item.type());
		}
	}
	
	@Override
	public Class<AssetPreload> getSupportedAnnotationType() {
		
		return AssetPreload.class;
	}
	
	@Override
	public boolean isSupportingTypes() {
		
		return true;
	}
}
