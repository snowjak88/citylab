/**
 * 
 */
package org.snowjak.city.util.validation


import java.util.function.Function
import java.util.function.Predicate

/**
 * @author snowjak88
 *
 */
public class Validator<T> {
	
	private final Set<Validation<T>> validations = new LinkedHashSet<>()
	
	public static <T> Validator.Builder<T> getFor(Class<T> type) {
		
		return new Builder<>(type)
	}
	
	private Validator(Set<Validation<T>> validations) {
		
		this.validations.addAll(validations)
	}
	
	public void validate(T object) throws ValidationException {
		
		final Optional<Validation<T>> firstFailedValidation = validations.stream()
				.filter({v -> !v.isValid(object)})
				.findFirst()
		if (firstFailedValidation.isPresent())
			throw new ValidationException(firstFailedValidation.get().getFailureMessage())
	}
	
	private boolean validateSilently(T object) {
		!(validations.stream()
				.anyMatch({v -> !v.isValid(object)}))
	}
	
	public static class Builder<T> {
		
		private final Set<Validation<T>> validations = new LinkedHashSet<>()
		private final Class<T> type
		private final Builder<T> parent
		
		private Builder(Class<T> type) {
			this(type, null)
		}
		
		private Builder(Class<T> type, Builder<T> parent) {
			this.type = type
			this.parent = parent
		}
		
		/**
		 * @return the fully-built Validator instance
		 */
		public Validator<T> build() {
			new Validator(validations)
		}
		
		/**
		 * The given string must be blank.
		 * 
		 * @param getter
		 * @param failureMessage
		 * @return
		 */
		public Builder<T> isBlank(Function<T, String> getter, String failureMessage = "Value must be blank") {
			
			validations.add(new Validation<T>( { t ->
				final String v = getter.apply(t)
				(v == null || v.isEmpty())
			}, failureMessage))
			this
		}
		
		/**
		 * The given string must not be blank.
		 * 
		 * @param getter
		 * @param failureMessage
		 * @return
		 */
		public Builder<T> notBlank(Function<T, String> getter, String failureMessage = "Value must not be blank") {
			
			validations.add(new Validation<T>({t ->
				final String v = getter.apply(t)
				(v != null && !v.isEmpty())
			}, failureMessage))
			this
		}
		
		/**
		 * The given reference must be null.
		 * @param getter
		 * @param failureMessage
		 * @return
		 */
		public Builder<T> isNull(Function<T,Object> getter, String failureMessage = "Reference must be null") {
			validations.add(new Validation<T>({ t ->
				(getter.apply(t) == null)
			},failureMessage))
			this
		}
		
		/**
		 * The given reference must not be null.
		 * @param getter
		 * @param failureMessage
		 * @return
		 */
		public Builder<T> notNull(Function<T,Object> getter, String failureMessage = "Reference must not be null") {
			validations.add(new Validation<T>({ t ->
				(getter.apply(t) != null)
			},failureMessage))
			this
		}
		
		/**
		 * The given int must be less than {@code ceiling}.
		 * 
		 * @param getter
		 * @param ceiling
		 * @param failureMessage
		 * @return
		 */
		public Builder<T> lessThan(ObjectToIntFunction<T> getter, int ceiling, String failureMessage = "Value must be less than $ceiling") {
			
			validations.add(new Validation<T>({t ->
				final int v = getter.apply(t)
				(v < ceiling)
			}, failureMessage))
			this
		}
		
		/**
		 * The given int must be greater than {@code floor}.
		 * 
		 * @param getter
		 * @param floor
		 * @param failureMessage
		 * @return
		 */
		public Builder<T> greaterThan(ObjectToIntFunction<T> getter, int floor, String failureMessage = "Value must be greater than $floor") {
			
			validations.add(new Validation<T>({ t ->
				final int v = getter.apply(t)
				(v > floor)
			}, failureMessage))
			this
		}
		
		/**
		 * The given {@link Collection} must be empty
		 * @param getter
		 * @param failureMessage
		 * @return
		 */
		public Builder<T> isEmpty(Function<T,Collection<?>> getter, String failureMessage = "Collection must be empty") {
			validations.add(new Validation<T>( { t ->
				getter.apply(t).isEmpty()
			}, failureMessage))
			this
		}
		
		/**
		 * The given {@link Collection} must be empty
		 * @param getter
		 * @param failureMessage
		 * @return
		 */
		public Builder<T> notEmpty(Function<T,Collection<?>> getter, String failureMessage = "Collection must not be empty") {
			validations.add(new Validation<T>( { t ->
				!getter.apply(t).isEmpty()
			}, failureMessage))
			this
		}
		
		/**
		 * At least one of the following conditions must be true.
		 * @return
		 */
		public Builder<T> andAny() {
			new Builder(type, this)
		}
		
		/**
		 * Mark the end of the {@link andAny()} conditions.
		 * @param failureMessage
		 * @return
		 */
		public Builder<T> endAny(String failureMessage) {
			if(parent == null)
				throw new IllegalStateException("Cannot finish building an \"any\" Validator-condition -- the Validator-builder we're finishing is not a child of another builder!")
			
			final Set<Validation<T>> anyValidations = this.validations
			parent.validations.add new Validation<>({ v -> anyValidations.stream().anyMatch({ it.isValid(v) })}, failureMessage)
			
			parent
		}
	}
	
	public static class Validation<T> {
		
		private final Predicate<T> test
		private final String failureMessage
		
		private Validation(Predicate<T> test, String failureMessage) {
			
			this.test = test
			this.failureMessage = failureMessage
		}
		
		public boolean isValid(T object) {
			
			return test.test(object)
		}
		
		public String getFailureMessage() {
			
			return failureMessage
		}
	}
	
	@FunctionalInterface
	public interface ObjectToBooleanFunction<T> {
		
		public boolean apply(T object)
	}
	
	@FunctionalInterface
	public interface ObjectToIntFunction<T> {
		
		public int apply(T object)
	}
	
	@FunctionalInterface
	public interface ObjectToLongFunction<T> {
		
		public long apply(T object)
	}
	
	@FunctionalInterface
	public interface ObjectToFloatFunction<T> {
		
		public float apply(T object)
	}
	
	@FunctionalInterface
	public interface ObjectToDoubleFunction<T> {
		
		public double apply(T object)
	}
	
	public static class ValidationException extends RuntimeException {
		
		private static final long serialVersionUID = -5730464386777855669L
		
		public ValidationException() {
			
			super()
		}
		
		public ValidationException(String message, Throwable cause) {
			
			super(message, cause)
		}
		
		public ValidationException(String message) {
			
			super(message)
		}
		
		public ValidationException(Throwable cause) {
			
			super(cause)
		}
	}
}
