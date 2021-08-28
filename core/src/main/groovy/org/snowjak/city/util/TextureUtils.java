/**
 * 
 */
package org.snowjak.city.util;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData.TextureDataType;

/**
 * @author snowjak88
 *
 */
public class TextureUtils {
	
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
	public static Texture mask(Texture input, Texture mask, MaskChannel maskChannel, Color resultBackground,
			boolean inverseAlpha) {
		
		if (input == null)
			throw new NullPointerException("Cannot mask Texture -- [input] is null!");
		if (mask == null)
			throw new NullPointerException("Cannot mask Texture -- [mask] is null!");
		if (maskChannel == null)
			throw new NullPointerException("Cannot mask Texture -- selected [maskChannel] is null!");
		
		if (input.getTextureData().getType() != TextureDataType.Pixmap)
			throw new IllegalArgumentException("Cannot mask Texture -- [input] is not backed by a Pixmap!");
		
		if (mask.getTextureData().getType() != TextureDataType.Pixmap)
			throw new IllegalArgumentException("Cannot mask Texture -- [mask] is not backed by a Pixmap!");
		
		final int width = Util.min(input.getWidth(), mask.getWidth());
		final int height = Util.min(input.getHeight(), mask.getHeight());
		
		final Pixmap result = new Pixmap(width, height, Format.RGBA8888);
		
		if (!input.getTextureData().isPrepared())
			input.getTextureData().prepare();
		final Pixmap inputPixmap = input.getTextureData().consumePixmap();
		
		if (!mask.getTextureData().isPrepared())
			mask.getTextureData().prepare();
		final Pixmap maskPixmap = mask.getTextureData().consumePixmap();
		
		for (int x = 0; x < width; x++)
			for (int y = 0; y < height; y++) {
				
				final Color c = convertToColor(inputPixmap.getPixel(x, y), inputPixmap.getFormat());
				final float maskValue = maskChannel
						.getFromColor(convertToColor(maskPixmap.getPixel(x, y), maskPixmap.getFormat()));
				final float invMaskValue = 1f - maskValue;
				
				final float inputAlpha = (inverseAlpha) ? invMaskValue : maskValue;
				final float backgroundAlpha = (inverseAlpha) ? maskValue : invMaskValue;
				
				c.mul(1, 1, 1, inputAlpha);
				final Color r = resultBackground.cpy().mul(1, 1, 1, backgroundAlpha);
				
				r.add(c);
				
				result.setColor(r);
				result.drawPixel(x, y);
			}
		
		return new Texture(result);
	}
	
	public enum MaskChannel {
		
		RED(c -> c.r),
		GREEN(c -> c.g),
		BLUE(c -> c.b),
		ALPHA(c -> c.a);
		
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
