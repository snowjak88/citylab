package org.snowjak.city.configuration.processors;

import org.snowjak.city.configuration.annotations.Asset;
import org.snowjak.city.service.GameAssetService;
import org.snowjak.city.service.LoggerService;

import com.badlogic.gdx.utils.reflect.Field;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.context.Context;
import com.github.czyzby.autumn.context.ContextDestroyer;
import com.github.czyzby.autumn.context.ContextInitializer;
import com.github.czyzby.autumn.processor.AbstractAnnotationProcessor;
import com.github.czyzby.kiwi.log.Logger;
import com.github.czyzby.kiwi.util.gdx.reflection.Reflection;

public class AssetAnnotationProcessor extends AbstractAnnotationProcessor<Asset> {
	
	private static final Logger LOG = LoggerService.forClass(AssetAnnotationProcessor.class);
	
	@Inject
	private GameAssetService assetService;
	
	@Override
	public void processField(Field field, Asset annotation, Object component, Context context,
			ContextInitializer initializer, ContextDestroyer contextDestroyer) {
		
		final String filename = annotation.value();
		final Class<?> assetType = field.getType();
		assetService.load(filename, assetType);
		assetService.addOnLoadAction(() -> {
			try {
				Reflection.setFieldValue(field, component, assetService.get(filename, assetType));
			} catch (ReflectionException e) {
				LOG.error(e, "Cannot inject required asset [{0}] ({1}) into [{2}].[{3}]", filename,
						assetType.getSimpleName(), component.toString(), field.getName());
			}
		});
	}
	
	@Override
	public boolean isSupportingFields() {
		
		return true;
	}
	
	@Override
	public Class<Asset> getSupportedAnnotationType() {
		
		return Asset.class;
	}
	
}
