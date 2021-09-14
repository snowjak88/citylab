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
	
	public final LinkedHashSet<Failure> failures = new LinkedHashSet<>(), reportedFailures = new LinkedHashSet<>()
	
	public ModuleExceptionRegistry(GameAssetService assetService) {
		this.assetService = assetService
	}
	
	public void reportFailure(Module module, FailureDomain domain, Throwable exception) {
		failures << new Failure(
				moduleID: module.id,
				moduleFile: assetService.getFileByID(module.id, Module).name(),
				moduleFullFile: assetService.getFileByID(module.id, Module).path(),
				domain: domain,
				exceptionType: exception.class.simpleName,
				exceptionMessage: exception.message,
				stacktrace: Throwables.getStackTraceAsString(exception))
	}
	
	public void reportFailure(String moduleID, String moduleFilename, String moduleFullFilename, FailureDomain domain, Throwable exception) {
		failures << new Failure(
				moduleID: moduleID,
				moduleFile: moduleFilename,
				moduleFullFile: moduleFullFilename,
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
		final f = failures.first()
		if(f) {
			failures.remove f
			reportedFailures << f
		}
		f
	}
	
	@Immutable
	public static class Failure {
		String moduleID, moduleFile, moduleFullFile
		FailureDomain domain
		String exceptionType, exceptionMessage, stacktrace
		@Override
		public int hashCode() {
			
			return Objects.hash(domain, exceptionMessage, exceptionType, moduleFile, moduleFullFile, moduleID);
		}
		@Override
		public boolean equals(Object obj) {
			
			if (this === obj)
				return true
			if (obj === null)
				return false
			if (getClass() != obj.getClass())
				return false
			Failure other = (Failure) obj
			domain == other.domain && Objects.equals(exceptionMessage, other.exceptionMessage) &&
					Objects.equals(exceptionType, other.exceptionType) && Objects.equals(moduleFile, other.moduleFile) &&
					Objects.equals(moduleFullFile, other.moduleFullFile) && Objects.equals(moduleID, other.moduleID)
		}
	}
	
	public enum FailureDomain {
		LOAD('module-failure-load'),
		CELL_RENDERER('module-failure-cellrenderer'),
		CUSTOM_RENDERER('module-failure-customrenderer'),
		ENTITY_SYSTEM('module-failure-entitysystem'),
		TOOL_ACTIVATE('module-failure-tool-activate'),
		TOOL_UPDATE('module-failure-tool-update'),
		TOOL_DEACTIVATE('module-failure-tool-deactivate'),
		VISUAL_PARAMETER('module-failure-visualparameter'),
		OTHER('module-failure-other');
		
		final String bundleKey
		private FailureDomain(String bundleKey) {
			this.bundleKey = bundleKey
		}
	}
}
