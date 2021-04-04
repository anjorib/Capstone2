import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.File;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;

import javax.imageio.ImageIO;


public class Crypting {
	/**
	 * Securely generates a new Key
	 * @param width The width of the largest encryptable Image (width of key is two times as wide)
	 * @param height The height of the largest encryptable Image (height of key is two times as tall)
	 * @return The key as a BufferedImage
	 */
	public static BufferedImage generateKey(int width, int height) {
		BufferedImage key = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D keyGraphics = key.createGraphics();
		keyGraphics.setColor(new Color(255, 255, 255, 0)); // fully transparent white
		keyGraphics.fillRect(0, 0, width, height);
		keyGraphics.dispose();
		
		return generateKey(key);
	}
	
	/**
	 * Securely generates a new Key from the given image
	 * @param steganoThis an image to transform into a key by turning its black pixels into 3/4 pixel blocks and
	 * white pixels into 2/4 pixel blocks (which itself are randomly determined). Non-white pixels are treated
	 * as if they were black. Fully white image for truly random key.
	 * @return The key as a BufferedImage
	 */
	public static BufferedImage generateKey(BufferedImage steganoThis) {
		int width = steganoThis.getWidth() * 2;
		int height = steganoThis.getHeight() * 2;
			
		// generate empty key image
		BufferedImage key = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D keyGraphics = key.createGraphics();
		
		// fill it with a fully transparent "white" (should allready be this way with TYPE_INT_ARGB)
		keyGraphics.setColor(new Color(255, 255, 255, 0));
		keyGraphics.fillRect(0, 0, width, height);
		
		// fill it with the random key structure
		keyGraphics.setColor(new Color(0, 0, 0, 255));
		
		// get securerandom. on linux, this uses NativePRNG (e.g. /dev/urandom), on
		// windows, it uses SHA1PRNG
		SecureRandom secureRandom = new SecureRandom();
		
		// each 2x2-pixel-pack has 2 randomly set pixels
		for (int y = 0; y < height; y += 2) {
			for (int x = 0; x < width; x += 2) {
				// get original image pos to be used if a stegano image file was given
				int origX = x/2;
				int origY = y/2;
				
				// determine if we want to generate key for a black or white pixel. if no image was given, the
				// result is the as as for white
				int iRgb = steganoThis.getRGB(origX, origY);
				boolean whitePixel = (iRgb == Color.WHITE.getRGB());
				whitePixel = whitePixel ? true : (iRgb>>>24 == 0); // transparency
				
				// rand pixel coords in 4x4 frame:
				// 0 | 1
				// -----
				// 2 | 3
				if (whitePixel) {
					// determine the two pixels
					int px1 = secureRandom.nextInt(4);
					int px2 = secureRandom.nextInt(4);
					while (px1 == px2) px2 = secureRandom.nextInt(4);
					
					// determine the coordinates of them
					int px1x = (px1 < 2) ? px1 : px1 - 2;
					int px1y = (px1 < 2) ? 0 : 1;
					int px2x = (px2 < 2) ? px2 : px2 - 2;
					int px2y = (px2 < 2) ? 0 : 1;
					
					// write them
					keyGraphics.fillRect(x + px1x, y + px1y, 1, 1);
					keyGraphics.fillRect(x + px2x, y + px2y, 1, 1);
				} else {
					// determine the pixel to stay white
					int px = secureRandom.nextInt(4);
					
					// determine cols, rows to be colored
					int rowBlack = px < 2 ? 1 : 0;
					int colBlack = (px % 2) == 0 ? 1 : 0; // 0 % 2 = 0
					
					// write others
					keyGraphics.fillRect(x + colBlack, y, 1, 2);
					keyGraphics.fillRect(x, y + rowBlack, 2, 1);
				}
			}
		}
		keyGraphics.dispose();
		
		return key;
	}
	
	/**
	 * Loads a key or encrypted file in Image and checks it (roughly).
	 * It is assumed that the file is a png
	 * @param keyFile
	 * @return The key file as an image or null if it isn't a key file.  Any white pixels the image might have had are converted to transparent ones.
	 */
	public static BufferedImage loadAndCheckEncrFile(File keyFile) {
		if (keyFile == null) return null;
		BufferedImage imgKey = null;
		try {
			imgKey = ImageIO.read(keyFile);
		} catch (Exception e) {
			return null;
		}
		
		// check if width + height are divisable by 2
		if (imgKey.getWidth() % 2 != 0) return null;
		if (imgKey.getHeight() % 2 != 0) return null;
		
		// convert image to ARGB colorspace (if it isn't allready)
		if (imgKey.getType() != BufferedImage.TYPE_INT_ARGB) {
			BufferedImage raw_image = imgKey;
			imgKey = new BufferedImage(raw_image.getWidth(), raw_image.getHeight(), BufferedImage.TYPE_INT_ARGB);
			new ColorConvertOp(null).filter(raw_image, imgKey);
		}
		
		// check if image contains only black + transparent or white pixels
		// also count those
		long lAmountOfTotalPixels = 0;
		long lAmountOfWhitePixels = 0;
		
		for(int i = 0; i < imgKey.getHeight(); i++) {
			for(int j = 0; j < imgKey.getWidth(); j++) {
				int iRgb = imgKey.getRGB(j, i);
				
				// white to transparent
				if(iRgb == Color.WHITE.getRGB()) {
					imgKey.setRGB(j, i, 0x00FFFFFF);
					iRgb = imgKey.getRGB(j, i);
				}
				
				// only count transparent pixels as white, everything else as black
				if(iRgb>>>24 == 0) {
					++lAmountOfTotalPixels;
					++lAmountOfWhitePixels;
				} else {
					++lAmountOfTotalPixels;
				}
				
			}
		}
		
		
		//if (lAmountOfTotalPixels / lAmountOfBlackPixels != 2) return null;
		// since stegano, if all pixels of the stegano file were black, at least a quart would have to be white
		// if all pixels of the stegano file where white, at least half of it would have to be white
		// so, total/black must have to fall between 2 and 4, inclusive
		double whites = (double)lAmountOfTotalPixels / lAmountOfWhitePixels;
		if (whites < 2 || whites > 4) return null;
		
		return imgKey;
	}
	
	/**
	 * Loads the image to be encrypted. If the image is smaller than the maximum possible size and resize is true, it is resized.
	 * Also ensures the image is a valid source file (only black and white (or transparent) pixels). 
	 * It is assumed that the file is a png.
	 * @param sourceFile The image to be encrypted
	 * @param width The width of the key to be used / 2
	 * @param height The height of the key to be used / 2
	 * @param resize true if image should be resized
	 * @return The (resized) image if it was OK or null. Any white pixels the image might have had are converted to transparent ones.
	 */
	public static BufferedImage loadAndCheckSource(File sourceFile, int width, int height, boolean resize) {
		if (sourceFile == null) return null;
		BufferedImage imgSrc = null;
		try {
			imgSrc = ImageIO.read(sourceFile);
		} catch (Exception e) {
			return null;
		}
		
		if (resize && (imgSrc.getWidth() > width || imgSrc.getHeight() > height)) return null;
		
		// convert image to ARGB colorspace (if it isn't allready)
		if (imgSrc.getType() != BufferedImage.TYPE_INT_ARGB) {
			BufferedImage raw_image = imgSrc;
			imgSrc = new BufferedImage(raw_image.getWidth(), raw_image.getHeight(), BufferedImage.TYPE_INT_ARGB);
			new ColorConvertOp(null).filter(raw_image, imgSrc);
		}
		
		// check if image contains only black + transparent or white pixels
		// colored pixels get converted to either black or transparent
		for(int i = 0; i < imgSrc.getHeight(); i++) {
			for(int j = 0; j < imgSrc.getWidth(); j++) {
				int iRgb = imgSrc.getRGB(j, i);
				
				// white to transparent
				if(iRgb == Color.WHITE.getRGB()) {
					imgSrc.setRGB(j, i, 0x00FFFFFF);
					iRgb = imgSrc.getRGB(j, i);
				}
				
				// check if pixel is either fully transparent or black
				if(!(iRgb>>>24 == 0 || iRgb == Color.BLACK.getRGB())) {
					int r = (iRgb & 0x00FF0000)>>16;
					int g = (iRgb & 0x0000FF00)>>8;
					int b = iRgb & 0x000000FF;
					// brightness by euclidian distance)
					double brightness = (0.2126 * r) + (0.7152 * g) + (0.0722 * b);
					if (brightness > (255/2)) {
						// transparent
						imgSrc.setRGB(j, i, 0x00FFFFFF);
					} else {
						// black
						imgSrc.setRGB(j, i, Color.BLACK.getRGB());
					}
				}
				
			}
		}
		
		// resize image
		if (!resize || (imgSrc.getWidth() == width && imgSrc.getHeight() == height)) return imgSrc;
		BufferedImage imgSrcRes =  new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = imgSrcRes.createGraphics();
		int x = (width - imgSrc.getWidth()) / 2;
		int y = (height - imgSrc.getHeight()) / 2;
		g.drawImage(imgSrc, x, y, imgSrc.getWidth() + x, imgSrc.getHeight() + y, 0, 0, imgSrc.getWidth(), imgSrc.getHeight(), null);
		g.dispose();
		
		return imgSrcRes;
	}
	
	/**
	 * Encrypts an image. It is assumed that the source image is the maximum possible size (width and height half of that of the key).
	 * Validity of source and key image are not checked, see loadAndCheckKey and loadAndCheckSource for that.
	 * @param imgKey The key to be used for the encryption
	 * @param imgSrc The image to be encrypted
	 * @return The encrypted image or null if an error occured
	 */
	public static BufferedImage encryptImage(BufferedImage imgKey, BufferedImage imgSrc) {
		if (imgKey == null || imgSrc == null) return null;
		// check for key/source file match
		if (imgSrc.getWidth() != imgKey.getWidth() / 2 || imgSrc.getHeight() != imgKey.getHeight() / 2) return null;
		
		// resize the source to the size of the key
		BufferedImage imgSrcRes =  new BufferedImage(imgKey.getWidth(), imgKey.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = imgSrcRes.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		g.drawImage(imgSrc, 0, 0, imgKey.getWidth(), imgKey.getHeight(), 0, 0, imgSrc.getWidth(), imgSrc.getHeight(), null);
		g.dispose();
		
		BufferedImage imgEncr =  new BufferedImage(imgKey.getWidth(), imgKey.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D encrGraphics = imgEncr.createGraphics();
		
		// fill it with a fully transparent "white" (should allready be this way with TYPE_INT_ARGB)
		encrGraphics.setColor(new Color(255, 255, 255, 0));
		encrGraphics.fillRect(0, 0, imgEncr.getWidth(), imgEncr.getHeight());
		
		// encrypt
		encrGraphics.setColor(new Color(0, 0, 0, 255));
		
		// each 2x2-pixel-pack has 2 pixels to set
		for (int y = 0; y < imgEncr.getHeight(); y += 2) {
			for (int x = 0; x < imgEncr.getWidth(); x += 2) {
				// because 1 black pixel of the original image is now a square of 4 black pixels,
				// only the first pixel has to be checked
				if (imgSrcRes.getRGB(x, y) == Color.BLACK.getRGB()) {
					// write the two pixels to complete the block together with the key
					if (imgKey.getRGB(x, y)>>>24 == 0) encrGraphics.fillRect(x, y, 1, 1);
					if (imgKey.getRGB(x + 1, y)>>>24 == 0) encrGraphics.fillRect(x + 1, y, 1, 1);
					if (imgKey.getRGB(x, y + 1)>>>24 == 0) encrGraphics.fillRect(x, y + 1, 1, 1);
					if (imgKey.getRGB(x + 1, y + 1)>>>24 == 0) encrGraphics.fillRect(x + 1, y + 1, 1, 1);
				} else {
					// write the two pixels at the same position in the key
					if (imgKey.getRGB(x, y) == Color.BLACK.getRGB()) encrGraphics.fillRect(x, y, 1, 1);
					if (imgKey.getRGB(x + 1, y) == Color.BLACK.getRGB()) encrGraphics.fillRect(x + 1, y, 1, 1);
					if (imgKey.getRGB(x, y + 1) == Color.BLACK.getRGB()) encrGraphics.fillRect(x, y + 1, 1, 1);
					if (imgKey.getRGB(x + 1, y + 1) == Color.BLACK.getRGB()) encrGraphics.fillRect(x + 1, y + 1, 1, 1);
				}
			}
		}
		encrGraphics.dispose();
		
		return imgEncr;
	}
	
	/**
	 * Generates an overlay of the key and the encrypted file, therefore producing an unclean, but
	 * Human readable decryption
	 * @param imgKey The key file used to encrypt the image
	 * @param imgEnc The encrypted image
	 * @return The overlay or null if the images are of different size
	 */
	public static BufferedImage overlayImages(BufferedImage imgKey, BufferedImage imgEnc) {
		if (imgKey == null || imgEnc == null || imgKey.getWidth() != imgEnc.getWidth() || imgKey.getHeight() != imgEnc.getHeight()) return null;
		
		// copy key to image
		BufferedImage imgOverlay =  new BufferedImage(imgKey.getWidth(), imgKey.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = imgOverlay.createGraphics();
		g.drawImage(imgKey, 0, 0, imgKey.getWidth(), imgKey.getHeight(), 0, 0, imgKey.getWidth(), imgKey.getHeight(), null);
		
		// impose the encrypted image on it
		g.drawImage(imgEnc, 0, 0, imgEnc.getWidth(), imgEnc.getHeight(), 0, 0, imgEnc.getWidth(), imgEnc.getHeight(), null);
		
		g.dispose();
		
		return imgOverlay;
	}
	
	/**
	 * Decrypts an encrypted image
	 * @param imgKey The key file used to encrypt the image
	 * @param imgEnc The encrypted image
	 * @return The decrypted picture
	 */
	public static BufferedImage decryptImage(BufferedImage imgKey, BufferedImage imgEnc) {
		return decryptImage(overlayImages(imgKey, imgEnc));
	}
	
	/**
	 * Decrypts an encrypted image (cleans up the provided overlay)
	 * @param imgOverlay An overlay generated by overlayImages()
	 * @return The decrypted picture
	 */
	public static BufferedImage decryptImage(BufferedImage imgOverlay) {
		if (imgOverlay == null || imgOverlay.getHeight() % 2 != 0 || imgOverlay.getWidth() % 2 != 0) return null;
		
		BufferedImage imgClean = new BufferedImage(imgOverlay.getWidth() / 2, imgOverlay.getHeight() / 2, BufferedImage.TYPE_INT_ARGB);
		Graphics2D cleanGraphics = imgClean.createGraphics();
		
		// fill it with a fully transparent "white" (should allready be this way with TYPE_INT_ARGB)
		cleanGraphics.setColor(new Color(255, 255, 255, 0));
		cleanGraphics.fillRect(0, 0, imgClean.getWidth(), imgClean.getHeight());
		
		// fill it with the cleaned up picture
		cleanGraphics.setColor(new Color(0, 0, 0, 255));
		
		// go through the picture and write all fully colored 2x2 blocks to the result picture
		for (int yOver = 0, yCln = 0; yOver < imgOverlay.getHeight(); yOver += 2, ++yCln) {
			for (int xOver = 0, xCln = 0; xOver < imgOverlay.getWidth(); xOver += 2, ++xCln) {
				int rgbFirstPixel = imgOverlay.getRGB(xOver, yOver);
				if (rgbFirstPixel >>>24 != 0 &&
						imgOverlay.getRGB(xOver + 1, yOver) >>>24 != 0 &&
						imgOverlay.getRGB(xOver, yOver + 1) >>>24 != 0 &&
						imgOverlay.getRGB(xOver + 1, yOver + 1) >>>24 != 0) {
					cleanGraphics.setColor(new Color(rgbFirstPixel, true));
					cleanGraphics.fillRect(xCln, yCln, 1, 1);
				}
			}
		}
		cleanGraphics.dispose();
		return imgClean;
	}
	

	
	/**
	 * Hides an image in two other ones. If the images given are not valid, or they're not all the same size, null is
	 * returned
	 * @param imgFirst The first image to be used to hide the imgToHide (assumed to be black & transparent)
	 * @param imgSecond The second image to be used to hide the imgToHide (assumed to be black & transparent)
	 * @param imgToHide The image supposed to be hidden in the other two (assumed to be black & transparent)
	 * @return An array where index 0 is the transformed imgFirst and index 1 the transformed imgSecond, or null if error
	 * @see http://datagenetics.com/blog/november32013/index.html
	 */
	public static BufferedImage[] hideImage(BufferedImage imgFirst, BufferedImage imgSecond, BufferedImage imgToHide) {
		if (imgFirst == null || imgSecond == null || imgToHide == null) return null;
		int width = imgFirst.getWidth();
		int height = imgFirst.getHeight();
		if (imgSecond.getWidth() != width || imgToHide.getWidth() != width
				|| imgSecond.getHeight() != height || imgToHide.getHeight() != height) return null;
		
		// generate a key out of the first image
		BufferedImage keyFirstImg = Crypting.generateKey(imgFirst);
		
		// build second image canvas
		BufferedImage keySecondImg =  new BufferedImage(width*2, height*2, BufferedImage.TYPE_INT_ARGB);
		Graphics2D gSecImg = keySecondImg.createGraphics();
		
		// fill it with a fully transparent "white" (should allready be this way with TYPE_INT_ARGB)
		gSecImg.setColor(new Color(255, 255, 255, 0));
		gSecImg.fillRect(0, 0, width*2, height*2);
		
		gSecImg.setColor(new Color(0, 0, 0, 255)); // black
		
		for (int y = 0; y < height; ++y) {
			for (int x = 0; x < width; ++x) {
				// determine the top left pixel of the 4x4 pixel block of the new second imag key
				int newX = x * 2;
				int newY = y * 2;

				int iRgb = imgToHide.getRGB(x, y);
				boolean targetShouldBeBlack = (iRgb>>>24 != 0); // transparency
				iRgb = imgSecond.getRGB(x, y);
				boolean secondImageIsBlack = (iRgb>>>24 != 0);
				
				// determine how many black pixels we want to set
				// for example, result could look like this, white, then black:
				// 0 | 1     1 | 1
				// 1 | 0     1 | 0
				int blackPixelsToSet = secondImageIsBlack ? 3 : 2;
				
				// if targetShouldBeBlack is true, 4/4 pixels of this block should, when combined with first image, be black
				// if targetShouldBeBlack is false, 3/4 pixels of this block should, when combined with first image, be black
				// if secondImageIsBlack is true, 3/4 pixels of this block should be black
				// if secondImageIsBlack is false, 2/4 pixels of this block should be black
				// refer to the truth table at http://datagenetics.com/blog/november32013/c2.png from
				// http://datagenetics.com/blog/november32013/index.html where B1 is first image, B2 the one we're making
				// and the one at the top the one to be hidden
				
				// new pixels
				Boolean[][] newPixels = new Boolean[2][2]; // initialized to [[null, null],[null, null]]
				// set the 1-2 white pixels of the original to black (skip one if target should be white, resulting in 0-1 px)
				boolean skipFirst = !targetShouldBeBlack;
				for (int minix = 0; minix < 2; ++minix) {
					for (int miniy = 0; miniy < 2; ++miniy) {
						// determine if the pixel at this pos in the first image is white
						int firstRgb = keyFirstImg.getRGB(newX + minix, newY + miniy);
						boolean firstIsWhite = (firstRgb != Color.BLACK.getRGB()); // made before, can only be black or transparent
						
						if (firstIsWhite) {
							if (skipFirst) {
								skipFirst = false;
								newPixels[minix][miniy] = Boolean.FALSE; // make white as well
								continue;
							}
							newPixels[minix][miniy] = Boolean.TRUE;
							-- blackPixelsToSet;
						}
					}
				}
				
				// randomly set blackPixelsToSet amount to black of the fields that weren't set (to black or white) before
				// for that, make a list of unused pixels and shuffle
				ArrayList<Integer> shuffleList = new ArrayList<>();
				for (int minix = 0; minix < 2; ++minix) {
					for (int miniy = 0; miniy < 2; ++miniy) {
						if (newPixels[minix][miniy] == null) shuffleList.add(minix*2 + miniy);
					}
				}
				// get securerandom. on linux, this uses NativePRNG (e.g. /dev/urandom), on windows, it uses SHA1PRNG
				SecureRandom secureRandom = new SecureRandom();
				Collections.shuffle(shuffleList, secureRandom);
				
				for (int i = blackPixelsToSet - 1; i >= 0; --i) {
					// shuffleList has at least as many contents as blackPixelsToSet
					int idx = shuffleList.get(i);
					int idxx = idx / 2;
					int idxy = idx % 2;
					newPixels[idxx][idxy] = Boolean.TRUE;
				}
				
//				// set all the rest if any to white
//				for (int minix = 0; minix < 2; ++minix) {
//					for (int miniy = 0; miniy < 2; ++miniy) {
//						if (newPixels[minix][miniy] == null) newPixels[minix][miniy] = Boolean.FALSE;
//					}
//				}
				
				// now write all new black pixels down
				for (int minix = 0; minix < 2; ++minix) {
					for (int miniy = 0; miniy < 2; ++miniy) {
						if (newPixels[minix][miniy] != null && newPixels[minix][miniy].booleanValue()) {
							gSecImg.fillRect(newX + minix, newY + miniy, 1, 1);
						}
					}
				}
			}
		}
		gSecImg.dispose();
		
		return new BufferedImage[]{keyFirstImg, keySecondImg};
	}
}
