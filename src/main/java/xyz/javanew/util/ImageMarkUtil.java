/**
 * 
 */
package xyz.javanew.util;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.imageio.ImageIO;

/**
 * @Desc
 * @author wewenge.yan
 * @Date 2017年5月4日
 * @ClassName ImageMarkUtil
 */
public class ImageMarkUtil {
	public static File mark(File sourceImgFile, String destImgPath, String text, Font font, Color color, int x, int y) {
		try {
			if (!checkImageFile(sourceImgFile)) {
				return null;
			}
			BufferedImage img = ImageIO.read(sourceImgFile);
			// 加水印
			BufferedImage bufImg = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
			mark(bufImg, img, text, font, color, x, y);
			// 输出图片
			File file = new File(destImgPath);
			FileOutputStream outImgStream = new FileOutputStream(file);
			ImageIO.write(bufImg, destImgPath.substring(destImgPath.lastIndexOf(".") + 1), outImgStream);
			outImgStream.flush();
			outImgStream.close();
			return file;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * @param sourceImgFile
	 * @return
	 */
	private static boolean checkImageFile(File sourceImgFile) {
		return sourceImgFile != null && sourceImgFile.exists() && sourceImgFile.isFile() && sourceImgFile.canRead();
	}

	public static File mark(File sourceImgFile, String destImgPath, File markImgFile, int width, int height, int x, int y) {
		// 读取原图片信息
		try {
			if (!checkImageFile(sourceImgFile) || !checkImageFile(markImgFile)) {
				return null;
			}
			BufferedImage img = ImageIO.read(sourceImgFile);
			BufferedImage mark = ImageIO.read(markImgFile);
			BufferedImage bufImg = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
			// mark(bufImg, img, mark, width, height, x, y);
			mark(bufImg, img, mark, mark.getWidth(), mark.getHeight(), x, y);
			File destImgFile = new File(destImgPath);
			FileOutputStream outImgStream = new FileOutputStream(destImgFile);
			ImageIO.write(bufImg, "jpg", outImgStream);
			outImgStream.flush();
			outImgStream.close();
			return destImgFile;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	// 给图片增加文字水印
	public static void mark(BufferedImage bufImg, BufferedImage img, String text, Font font, Color color, int x, int y) {
		Graphics2D g = bufImg.createGraphics();
		g.drawImage(img, 0, 0, bufImg.getWidth(), bufImg.getHeight(), null);
		g.setColor(color);
		g.setFont(font);
		g.drawString(text, x, y);
		g.dispose();
	}

	// 加图片水印
	public static void mark(BufferedImage bufImg, BufferedImage img, BufferedImage markImg, int width, int height, int x, int y) {
		Graphics2D g = bufImg.createGraphics();
		g.drawImage(img, 0, 0, bufImg.getWidth(), bufImg.getHeight(), null);
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.5f));
		g.drawImage(markImg, x, y, width, height, null);
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
		g.dispose();
	}

	// int width=225,int height=70
	public static Map<Point, Color> getMarkMap(File sourceImgFile, String markImgPath, int align, int x, int y, int width, int height) {
		if (!checkImageFile(sourceImgFile)) {
			return null;
		}
		try {
			BufferedImage sourceImg = ImageIO.read(sourceImgFile);
			int imgWidth = sourceImg.getWidth();
			int imgHeight = sourceImg.getHeight();
			if (align == 1) {

			} else {
				x = imgWidth - x - width;
				y = imgHeight - y - height;
			}
			int minX = 0;
			int minY = 0;
			int maxX = 0;
			int maxY = 0;
			Map<Point, Color> map = new HashMap<Point, Color>();
			BufferedImage destImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
					int rgb = sourceImg.getRGB(x + i, y + j);
					Color color = new Color(rgb);
					// color = getStandardColor(color, k);
					if (isWhite(color)) {
						map.put(new Point(i, j), color);
						if (minX == 0 || minX > x + i) {
							minX = x + i;
						}
						if (minY == 0 || minY > y + j) {
							minY = y + j;
						}
						if (maxX == 0 || maxX < x + i) {
							maxX = x + i;
						}
						if (maxY == 0 || maxY < y + j) {
							maxY = y + j;
						}
						destImg.setRGB(i, j, Color.WHITE.getRGB());
					} else {
						destImg.setRGB(i, j, Color.BLACK.getRGB());
					}
					// destImg.setRGB(i, j, color.getRGB());
				}
			}
			// mark(bufImg, img, mark, width, height, x, y);
			File markImgFile = new File(markImgPath);
			FileOutputStream outImgStream = new FileOutputStream(markImgFile);
			ImageIO.write(destImg, "jpg", outImgStream);
			outImgStream.flush();
			outImgStream.close();
			System.out.println("(" + imgWidth + "," + imgHeight + ")对应中心点(" + (minX + maxX) / 2 + "," + (minY + maxY) / 2 + "),长宽(" + (maxX - minX)
					+ "," + (maxY - minY) + ")空白" + "(" + (imgWidth - maxX) + "," + (imgHeight - maxY) + ")");
			return map;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static File removeMark(File sourceImgFile, String destImgPath, Map<Point, Color> markMap, int align, int x, int y, int width, int height) {
		if (!checkImageFile(sourceImgFile)) {
			return null;
		}
		try {
			BufferedImage sourceImg = ImageIO.read(sourceImgFile);
			int imgWidth = sourceImg.getWidth();
			int imgHeight = sourceImg.getHeight();
			if (align == 1) {

			} else {
				x = imgWidth - x - width;
				y = imgHeight - y - height;
			}
			int minColorDifference = calculateColorDifference(new Color(0, 0, 0), new Color(16, 16, 16));
			BufferedImage destImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			int errorCount = 0;
			for (Entry<Point, Color> entry : markMap.entrySet()) {
				Point key = entry.getKey();
				Color value = entry.getValue();
				int rgb = sourceImg.getRGB(x + (int) key.getX(), y + (int) key.getY());
				Color color = new Color(rgb);
				int colorDifference = calculateColorDifference(value, color);
				System.out.println("(" + ((int) key.getX()) + "," + ((int) key.getY()) + ")" + value + "和" + "(" + ((int) key.getX() + x) + ","
						+ ((int) key.getY() + y) + ")" + color + "色差：" + colorDifference);
				if (colorDifference > minColorDifference) {
					errorCount++;
				}
			}
			System.out.println(errorCount + "/" + markMap.size());
			File destImgFile = new File(destImgPath);
			FileOutputStream outImgStream = new FileOutputStream(destImgPath);
			ImageIO.write(destImg, "jpg", outImgStream);
			outImgStream.flush();
			outImgStream.close();
			return destImgFile;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private static Color getStandardColor(Color color, int colorDifference) {
		if (color == null) {
			return null;
		}
		// int alpha = Math.round((float) color.getAlpha() / colorDifference);
		int red = Math.round((float) color.getRed() / colorDifference);
		int green = Math.round((float) color.getGreen() / colorDifference);
		int blue = Math.round((float) color.getBlue() / colorDifference);
		return new Color(red * colorDifference, green * colorDifference, blue * colorDifference);
	}

	private static boolean checkColor(Color color, int offset, int white) {
		int red = color.getRed();
		int green = color.getGreen();
		int blue = color.getBlue();
		if (white == 0) {
			return ((255 - red) <= offset) && ((255 - green) <= offset) && ((255 - blue) <= offset);
		} else {
			return ((red <= offset) && (green <= offset) && (blue <= offset));
		}
	}

	private static boolean isWhite(Color color) {
		int red = color.getRed();
		int green = color.getGreen();
		int blue = color.getBlue();
		int colorDiff = 32;
		int minWhite = 128;
		return red > minWhite && green > minWhite && blue > minWhite && Math.abs(red - green) <= colorDiff && Math.abs(red - blue) <= colorDiff
				&& Math.abs(blue - green) <= colorDiff;
	}

	private static void getMarkPosition(File sourceImgFile) {
		if (!checkImageFile(sourceImgFile)) {
			return;
		}
		try {
			BufferedImage sourceImg = ImageIO.read(sourceImgFile);
			int imgWidth = sourceImg.getWidth();
			int imgHeight = sourceImg.getHeight();
			// int imgWidth = 640;
			// int imgHeight = (int) Math.round(640.0 / sourceImg.getWidth() * sourceImg.getHeight());
			// BufferedImage newImg = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_RGB);
			// newImg.getGraphics().drawImage(sourceImg.getScaledInstance(imgWidth, imgHeight,
			// java.awt.Image.SCALE_SMOOTH), 0, 0, null);
			// sourceImg = newImg;
			int markWidth = 200;
			int markHeight = 70;
			int startX = imgWidth - markWidth;
			int startY = imgHeight - markHeight;
			int minX = 0;
			int minY = 0;
			for (int i = 0; i < markWidth; i++) {
				for (int j = 0; j < markHeight; j++) {
					int rgb = sourceImg.getRGB(startX + i, startY + j);
					Color color = new Color(rgb);
					if (isWhite(color)) {
						if (minX == 0 || minX > startX + i) {
							minX = startX + i;
						}
						if (minY == 0 || minY > startY + j) {
							minY = startY + j;
						}
					}
				}
			}
			markWidth = imgWidth - minX;
			markHeight = imgHeight - minY;
			System.out.println(imgWidth + "=" + minX + "+" + markWidth + ";" + imgHeight + "=" + minY + "+" + markHeight);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static int calculateColorDifference(Color color1, Color color2) {
		if (color1 == null || color2 == null) {
			throw new IllegalArgumentException("color1,color2不能为空！");
		}
		int result = 0;
		result += Math.pow(color1.getAlpha() - color2.getAlpha(), 2);
		result += Math.pow(color1.getRed() - color2.getRed(), 2);
		result += Math.pow(color1.getGreen() - color2.getGreen(), 2);
		result += Math.pow(color1.getBlue() - color2.getBlue(), 2);
		return result;
	}

	// (640,853)对应中心点(526,814),长宽(171,43)
	public static void main(String[] args) {
		File sourceImgFile = new File("/D:/项目读写文件/image/img_to_mark.jpg");
		File markImgFile = new File("/D:/项目读写文件/image/dest.jpg");
		String text = "test mark";
		String destImgPath1 = "/D:/项目读写文件/image/img_marked_with_text.jpg";
		String destImgPath2 = "/D:/项目读写文件/image/img_marked_with_img.jpg";
		Font font = new Font("宋体", Font.PLAIN, 12);
		Color color = new Color(0, 0, 0, 128);
		int height = 60;
		int width = 22;
		int x = 8;
		int y = 8;
		// mark(sourceImgFile, destImgPath1, text, font, color, x, y + font.getSize());
		// mark(sourceImgFile, destImgPath2, markImgFile, width, height, x, y);
		sourceImgFile = new File("/D:/项目读写文件/image/white_mark2.jpg");
		String markImgPath = "/D:/项目读写文件/image/extract_mark.jpg";
		int align = 3;
		File white_mark = new File("/D:/项目读写文件/image/white_mark.jpg");
		File white_mark1 = new File("/D:/项目读写文件/image/white_mark1.jpg");
		File white_mark2 = new File("/D:/项目读写文件/image/white_mark2.jpg");
		File white_mark3 = new File("/D:/项目读写文件/image/white_mark3.jpg");
		File white_mark4 = new File("/D:/项目读写文件/image/white_mark4.jpg");
		// getMarkPosition(white_mark);
		// getMarkPosition(white_mark1);
		// getMarkPosition(white_mark2);
		String destImgPath = "/D:/项目读写文件/image/img_mark-removed.jpg";
		// Map<Point, Color> markMap = getMarkMap(white_mark2, markImgPath, align, 0, 0, 225, 70);
		// removeMark(white_mark3, destImgPath, markMap, align, 0, 0, 225, 70);
		// markMap = getMarkMap(white_mark, markImgPath, align, 0, 0, 170, 60);
		// markMap = getMarkMap(white_mark3, markImgPath, align, 0, 0, 225, 70);
		removeMark(white_mark3, destImgPath, markImgPath);
	}

	/**
	 * @param white_mark3
	 * @param destImgPath
	 * @param markImgPath
	 */
	private static void removeMark(File sourceImgFile, String destImgPath, String markImgPath) {
		String fingerprint = ImageAnalyseUtil.generateFingerprint(markImgPath);
		System.out.println(fingerprint);

	}

}
