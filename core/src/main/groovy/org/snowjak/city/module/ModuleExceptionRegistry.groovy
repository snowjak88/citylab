package org.snowjak.city.module

import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

import org.snowjak.city.service.GameAssetService

import com.github.czyzby.autumn.annotation.Component
import com.github.czyzby.autumn.annotation.Inject
import com.google.common.base.Throwables

import groovy.transform.EqualsAndHashCode
import groovy.transform.Immutable

/**
 * Registry for holding exceptions thrown off by Modules. Typically, if part of a Module fails, its
 * failure would be recorded here for subsequent reporting.
 * 
 * @author snowjak88
 *
 */
@Component
class ModuleExceptionRegistry {
	
	@Inject
	private GameAssetService assetService
	
	public final BlockingQueue<Failure> failures = new LinkedBlockingQueue<>(), reportedFailures = new LinkedBlockingQueue<>()
	
	public ModuleExceptionRegistry(GameAssetService assetService) {
		this.assetService = assetService
	}
	
	public void reportFailure(Module module, FailureDomain domain, Throwable exception) {
		failures.offer new Failure(
				moduleID: module.id,
				moduleFile: assetService.getFileByID(module.id, Module),
				domain: domain,
				exceptionType: exception.class.simpleName,
				exceptionMessage: exception.message,
				stacktrace: Throwables.getStackTraceAsString(exception))
	}
	
	public void reportFailure(String moduleID, String moduleFilename, FailureDomain domain, Throwable exception) {
		failures.offer new Failure(
				moduleID: moduleID,
				moduleFile: moduleFilename,
				domain: domain,
				exceptionType: exception.class.simpleName,
				exceptionMessage: exception.message,
				stacktrace: Throwables.getStackTraceAsString(exception))
	}
	
	/**
	 * @return {@code true} if the next call to {@link #nextFailure()} will return a non-{@code null} value
	 */
	public boolean hasUnreportedFailure() {
		!failures.isEmpty()
	}
	
	/**
	 * @return the next unreported {@link Failure}, or {@code null} if no un-reported Failures
	 */
	public Failure nextFailure() {
		final f = failures.poll()
		if(f)
			reportedFailures.offer f
		f
	}
	
	@EqualsAndHashCode
	@Immutable
	public static class Failure {
		String moduleID, moduleFile
		FailureDomain domain
		String exceptionType, exceptionMessage, stacktrace
	}
	
	public enum FailureDomain {
		LOAD('module-failure-load'),
		CELL_RENDERER('module-failure-cellrenderer'),
		CUSTOM_RENDERER('module-failure-customrenderer'),
		TOOL_ACTIVATE('module-failure-tool-activate'),
		TOOL_UPDATE('module-failure-tool-update'),
		TOOL_DEACTIVATE('module-failure-tool-deactivate'),
		OTHER('module-failure-other');
		
		final String bundleKey
		private FailureDomain(String bundleKey) {
			this.bundleKey = bundleKey
		}
	}
}
