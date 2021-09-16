/**
 * 
 */
package org.snowjak.city.util;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Filter;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * @author snowjak88
 *
 */
public class PixmapUtils {
	
	private static final ToFloatFunction<Color> toGrayscale = (c) -> (0.21f * c.r + 0.72f * c.g + 0.07f * c.b);
	
	public static Pixmap fromTexture(Texture texture) {
		
		if (!texture.getTextureData().isPrepared())
			texture.getTextureData().prepare();
		return texture.getTextureData().consumePixmap();
	}
	
	public static Pixmap fromTextureRegion(TextureRegion region) {
		
		if (!region.getTexture().getTextureData().isPrepared())
			region.getTexture().getTextureData().prepare();
		final Pixmap inputPixmap = region.getTexture().getTextureData().consumePixmap();
		final Pixmap result = new Pixmap(region.getRegionWidth(), region.getRegionHeight(), inputPixmap.getFormat());
		
		result.drawPixmap(inputPixmap, 0, 0, region.getRegionX(), region.getRegionY(), region.getRegionWidth(),
				region.getRegionHeight());
		
		return result;
	}
	
	/**
	 * Convert an {@code input} {@link Texture} to a grayscale equivalent. The
	 * RGB->Grayscale conversion does not affect the alpha-channel, which is
	 * preserved in the result Texture.
	 * 
	 * @param input
	 * @return
	 */
	public static Pixmap grayscale(Pixmap input) {
		
		final Pixmap result = new Pixmap(input.getWidth(), input.getHeight(), Format.RGBA8888);
		
		for (int x = 0; x < input.getWidth(); x++)
			for (int y = 0; y < input.getHeight(); y++) {
				final Color color = convertToColor(input.getPixel(x, y), input.getFormat());
				final float gray = toGrayscale.apply(color);
				result.drawPixel(x, y, Color.rgba8888(gray, gray, gray, color.a));
			}
		
		return result;
	}
	
	/**
	 * Given a non-square {@code input} {@link Texture}, produce a square Texture
	 * where the given {@code input} is centered. The output Texture's side-lengths
	 * will be equal to the greater side-length of the input.
	 * 
	 * @param input
	 * @param resultBackground
	 * @return
	 */
	public static Pixmap center(Pixmap input, Color resultBackground) {
		
		final int dimension = Util.max(input.getWidth(), input.getHeight());
		final Pixmap result = new Pixmap(dimension, dimension, Format.RGBA8888);
		
		result.setColor(resultBackground);
		result.fill();
		
		final int offsetX = (dimension / 2) - (input.getWidth() / 2);
		final int offsetY = (dimension / 2) - (input.getHeight() / 2);
		
		result.drawPixmap(input, offsetX, offsetY);
		
		return result;
	}
	
	/**
	 * Resize the given {@code input} {@link Texture} to the specified size.
	 * 
	 * @param input
	 * @param newWidth
	 * @param newHeight
	 * @return
	 */
	public static Pixmap resize(Pixmap input, int newWidth, int newHeight) {
		
		final Pixmap result = new Pixmap(newWidth, newHeight, Format.RGBA8888);
		result.setFilter(Filter.BiLinear);
		
		result.drawPixmap(input, 0, 0, input.getWidth(), input.getHeight(), 0, 0, newWidth, newHeight);
		
		return result;
	}
	
	/**
	 * Produce a new Texture by drawing {@code top} onto {@code bottom}, starting
	 * from the given location ({@code topOffsetX}, {@code topOffsetY}) on
	 * {@code bottom}. The resulting image is the same size as {@code bottom}.
	 * 
	 * @param bottom
	 * @param top
	 * @param topOffsetX
	 * @param topOffsetY
	 * @return
	 */
	public static Pixmap layer(Pixmap bottom, Pixmap top, int topOffsetX, int topOffsetY) {
		
		final Pixmap result = new Pixmap(bottom.getWidth(), bottom.getHeight(), Format.RGBA8888);
		
		result.drawPixmap(bottom, 0, 0);
		
		final int topDrawWidth = Util.clamp(top.getWidth(), 0, bottom.getWidth() - topOffsetX);
		final int topDrawHeight = Util.clamp(top.getHeight(), 0, bottom.getHeight() - topOffsetY);
		
		result.drawPixmap(top, topOffsetX, topOffsetY, 0, 0, topDrawWidth, topDrawHeight);
		
		return result;
	}
	
	/**
	 * Masks an {@code input} {@link Texture} using the given {@link MaskChannel
	 * maskChannel} of the given {@code mask} Texture.
	 * <p>
	 * The produced Texture is <strong>cropped</strong> to the smaller of the two
	 * input Texture sizes.
	 * </p>
	 * <p>
	 * Adapted from <a href="https://stackoverflow.com/a/34262865">a Stackoverflow
	 * entry</a> by Tekkerue.
	 * </p>
	 * 
	 * @param input
	 * @param mask
	 * @param maskChannel
	 * @param resultBackground
	 * @param inverseAlpha
	 * @return
	 * @throws NullPointerException
	 *             if {@code input}, {@code mask}, or {@code maskChannel} are
	 *             {@code null}
	 * @throws IllegalArgumentException
	 *             if either of the given Textures are not backed by a Pixmap
	 */
	public static Pixmap mask(Pixmap input, Pixmap mask, MaskChannel maskChannel, Color resultBackground,
			boolean inverseAlpha) {
		
		if (input == null)
			throw new NullPointerException("Cannot mask Pixmap -- [input] is null!");
		if (mask == null)
			throw new NullPointerException("Cannot mask Pixmap -- [mask] is null!");
		if (maskChannel == null)
			throw new NullPointerException("Cannot mask Pixmap -- selected [maskChannel] is null!");
		
		final int width = Util.min(input.getWidth(), mask.getWidth());
		final int height = Util.min(input.getHeight(), mask.getHeight());
		
		final Pixmap result = new Pixmap(width, height, Format.RGBA8888);
		
		for (int x = 0; x < width; x++)
			for (int y = 0; y < height; y++) {
				
				final Color c = convertToColor(input.getPixel(x, y), input.getFormat());
				final float maskValue = maskChannel.getFromColor(convertToColor(mask.getPixel(x, y), mask.getFormat()));
				final float invMaskValue = 1f - maskValue;
				
				final float inputAlpha = (inverseAlpha) ? invMaskValue : maskValue;
				final float backgroundAlpha = (inverseAlpha) ? maskValue : invMaskValue;
				
				c.mul(1, 1, 1, inputAlpha);
				final Color r = resultBackground.cpy().mul(1, 1, 1, backgroundAlpha);
				
				// r.add(c);
				result.setColor(r);
				result.drawPixel(x, y);
				
				result.setColor(c);
				result.drawPixel(x, y);
			}
		
		return result;
	}
	
	public enum MaskChannel {
		
		RED(c -> c.r), GREEN(c -> c.g), BLUE(c -> c.b), ALPHA(c -> c.a);
		
		private final ToFloatFunction<Color> channel;
		
		private MaskChannel(ToFloatFunction<Color> channel) {
			
			this.channel = channel;
		}
		
		public float getFromColor(Color color) {
			
			return channel.apply(color);
		}
	}
	
	/**
	 * Convert a packed-int of the given {@link Format} to a {@link Color}.
	 * 
	 * @param packed
	 * @param format
	 * @return
	 * @throws IllegalArgumentException
	 *             if {@code format} is not a packed-int Format
	 */
	public static Color convertToColor(int packed, Format format) {
		
		final Color result = new Color();
		switch (format) {
		case RGBA8888:
			Color.rgba8888ToColor(result, packed);
			break;
		case RGBA4444:
			Color.rgba4444ToColor(result, packed);
			break;
		case RGB888:
			Color.rgb888ToColor(result, packed);
			break;
		case RGB565:
			Color.rgb565ToColor(result, packed);
			break;
		default:
			throw new IllegalArgumentException("Cannot convert an int to Color of Format=[" + format + "].");
		}
		
		return result;
	}
}
