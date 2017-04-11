/**
 * 
 */
package xyz.javanew.util;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.imageio.ImageIO;

import org.springframework.util.CollectionUtils;

/**
 * @Desc
 * @author wewenge.yan
 * @Date 2017年4月14日
 * @ClassName ColorImageAnalyser
 */
public class ColorImageAnalyser {
	private int colorDifference = 48;// 色差（是指颜色差距），默认
	private String formatName = "jpg";
	private int minOccupyArea = 25;// 假面积限制（假面积，最大占用面积）
	private int minAreaPercent = 20;// 真面积百分比限制（真面积，像素面积）
	private int maxAreaPercent = 80;// 真面积百分比限制（真面积，像素面积）
	private int width = 48;// 宽度
	private int height = 48;// 长度
	private int borderPercent = 10;// 边框%比，用于计算背景颜色
	private int intensityPercent = 16;// 密集度%比
	private int intensitySize = 2;// 密集度计算范围
	private boolean filterAbsolute = true;// 完全孤立过滤

	public static void main(String[] args) {
		ColorImageAnalyser analyser = new ColorImageAnalyser();
		String sourcePath = "/D:/项目读写文件/image/source(9).png";
		String destPath = "/D:/项目读写文件/image/dest.jpg";
		analyser.parseImage(new File(sourcePath), destPath);
	}

	public void parseImage(File source, String destPath) {
		Map<Color, List<Point>> colorPointsMap = this.extractColorPointsMap(source);
		colorPointsMap = this.filterIsolatePoints(colorPointsMap);
		colorPointsMap = this.filterByArea(colorPointsMap);
		colorPointsMap = this.filterBackground(colorPointsMap);
		colorPointsMap = this.filterByIntensity(colorPointsMap);
		this.generateNewImages(colorPointsMap, destPath);
	}

	/**
	 * @param colorPointsMap
	 */
	private Map<Color, List<Point>> filterBackground(Map<Color, List<Point>> colorPointsMap) {
		int maxBackCount = 0;
		Color backColor = null;
		for (Entry<Color, List<Point>> entry : colorPointsMap.entrySet()) {
			List<Point> points = entry.getValue();
			int backCount = 0;
			for (Point point : points) {
				int x = (int) point.getX();
				int y = (int) point.getY();
				if ((x < width * borderPercent / 100 || width - x < width * borderPercent / 100)
						&& (y < height * borderPercent / 100 || height - y < height * borderPercent / 100)) {
					backCount++;
				}
			}
			if (maxBackCount == 0 || backCount > maxBackCount) {
				maxBackCount = backCount;
				backColor = entry.getKey();
			}
		}
		colorPointsMap.remove(backColor);
		return colorPointsMap;
	}

	/**
	 * @param colorPointsMap
	 */
	private Map<Color, List<Point>> filterByIntensity(Map<Color, List<Point>> colorPointsMap) {
		int minIntensity = 0;
		//
		// if ((x < width * borderPercent / 100 || width - x < width * borderPercent / 100)
		// && (y < height * borderPercent / 100 || height - y < height * borderPercent / 100)) {
		// Integer count = backCountMap.get(color);
		// if (count == null) {
		// count = 0;
		// }
		// backCountMap.put(color, ++count);
		// }
		Entry<Color, List<Point>> infoEntry = null;
		for (Entry<Color, List<Point>> entry : colorPointsMap.entrySet()) {
			List<Point> points = entry.getValue();
			int intensity = 0;
			for (Point point : points) {
				intensity += Math.sqrt(point.getX() - width / 2) + Math.sqrt(point.getY() - height / 2);
			}
			intensity /= points.size();
			if (minIntensity == 0 || intensity < minIntensity) {
				minIntensity = intensity;
				infoEntry = entry;
			}
		}
		colorPointsMap.clear();
		colorPointsMap.put(infoEntry.getKey(), infoEntry.getValue());
		return colorPointsMap;
	}

	/**
	 * @param colorPointsMap
	 */
	private Map<Color, List<Point>> filterIsolatePoints(Map<Color, List<Point>> colorPointsMap) {
		Map<Color, List<Point>> result = new HashMap<Color, List<Point>>();
		for (Entry<Color, List<Point>> entry : colorPointsMap.entrySet()) {
			List<Point> points = entry.getValue();
			int isolateCount = 0;
			for (int i = 0; i < points.size(); i++) {
				Point point = points.get(i);
				if (this.isIsolate(point, i, points) || filterAbsolute && this.isAbsoluteIsolate(point, points)) {
					points.remove(i);
					i--;
					isolateCount++;
				}
			}
			if (!CollectionUtils.isEmpty(points)) {
				result.put(entry.getKey(), points);
			}
			System.out.println("通过" + intensityPercent + "%密集度过滤像素数：" + isolateCount);
		}
		return result;
	}

	public void generateNewImages(Map<Color, List<Point>> colorPointsMap, String destPath) {
		if (CollectionUtils.isEmpty(colorPointsMap)) {
			return;
		}
		int i = 0;
		for (Entry<Color, List<Point>> entry : colorPointsMap.entrySet()) {
			Color color = entry.getKey();

			List<Point> points = entry.getValue();
			if (CollectionUtils.isEmpty(points)) {
				continue;
			}

			BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_BGR);
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					if (points.contains(new Point(x, y))) {
						image.setRGB(x, y, color.getRGB());
					} else {
						image.setRGB(x, y, 0xffffff);
					}
				}
			}
			try {
				String realDestPath = destPath;
				if (!destPath.contains(".")) {
					realDestPath = destPath + "." + formatName;
				}
				realDestPath = realDestPath.replace(".", i == 0 ? "." : i + ".");
				ImageIO.write(image, formatName, new File(realDestPath));
			} catch (IOException e) {
				e.printStackTrace();
			}
			i++;
		}
	}

	/**
	 * @param points
	 * @param index
	 * @param color
	 * @return
	 */
	private Map<Color, List<Point>> filterByArea(Map<Color, List<Point>> colorPointsMap) {
		Map<Color, List<Point>> result = new HashMap<Color, List<Point>>();
		int index = 0;
		for (Entry<Color, List<Point>> entry : colorPointsMap.entrySet()) {
			Color color = entry.getKey();
			List<Point> points = entry.getValue();
			int left = 0, right = 0, top = 0, bottom = 0;
			for (Point point : points) {
				if (left == 0 || point.getX() < left) {
					left = (int) point.getX();
				}
				if (point.getX() > right) {
					right = (int) point.getX();
				}
				if (top == 0 || point.getY() < top) {
					top = (int) point.getY();
				}
				if (point.getY() > bottom) {
					bottom = (int) point.getY();
				}
			}

			// 真假面积过滤
			int occupyArea = (right - left + 1) * (bottom - top + 1);
			int areaPercent = points.size() * 100 / occupyArea;
			if (occupyArea >= minOccupyArea && areaPercent >= minAreaPercent && areaPercent <= maxAreaPercent) {
				result.put(color, points);
			}
			System.out.println("第" + (index + 1) + "前景颜色为:" + color + "占用面积为：" + occupyArea + "，像素面积百分比：" + areaPercent);
			index++;
		}
		return result;
	}

	/**
	 * 按照颜色分割成点集
	 * 
	 * @param source
	 * @return
	 */
	public Map<Color, List<Point>> extractColorPointsMap(File source) {
		Map<Color, List<Point>> result = new HashMap<Color, List<Point>>();
		BufferedImage bufferedImage = null;
		try {
			bufferedImage = ImageIO.read(source);
		} catch (IOException e) {
			e.printStackTrace();
			return result;
		}
		height = bufferedImage.getHeight();
		width = bufferedImage.getWidth();

		// Map<Color, Integer> backCountMap = new HashMap<Color, Integer>();

		// int filterCount = 0;
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				Point point = new Point(x, y);
				int argb = bufferedImage.getRGB(x, y);
				Color color = this.getStandardColor(new Color(argb));
				if (isColorEquals(color, new Color(0xffffff))) {
					continue;
				}
				List<Point> value = result.get(color);
				if (value == null) {
					value = new ArrayList<Point>();
				}
				value.add(point);
				result.put(color, value);
			}
		}
		return result;
	}

	/**
	 * @param bufferedImage
	 * @param x
	 * @param y
	 * @param color
	 * @return
	 */
	private boolean isAbsoluteIsolate(Point point, List<Point> points) {
		double x = point.getX(), y = point.getY();
		int aroundCount = 0;
		for (int i = 0; i < points.size(); i++) {
			Point point2 = points.get(i);
			double x2 = point2.getX();
			double y2 = point2.getY();
			if (Math.abs(x2 - x) <= 1 && Math.abs(y2 - y) <= 1) {
				aroundCount++;
			}
		}
		return aroundCount < 2;
	}

	/**
	 * @param bufferedImage
	 * @param x
	 * @param y
	 * @param color
	 * @return
	 */
	private boolean isIsolate(Point point, int index, List<Point> points) {
		int x = (int) point.getX(), y = (int) point.getY();
		int left = 0, right = 0, top = 0, bottom = 0;
		for (int i = x - intensitySize;; i++) {
			if (i >= 0) {
				left = i;
				break;
			}
		}
		for (int i = x + intensitySize;; i--) {
			if (i <= width) {
				right = i;
				break;
			}
		}
		for (int i = y - intensitySize;; i++) {
			if (i >= 0) {
				top = i;
				break;
			}
		}
		for (int i = y + intensitySize;; i--) {
			if (i <= height) {
				bottom = i;
				break;
			}
		}
		int aroundCount = 1;
		for (int i = 0; i < points.size(); i++) {
			Point point2 = points.get(i);
			if (point2.getX() >= left && point2.getX() <= right && point2.getY() >= top && point2.getY() <= bottom) {
				aroundCount++;
			}
		}
		return aroundCount * 100 < (right - left + 1) * (bottom - top + 1) * intensityPercent;
	}

	/**
	 * @param bufferedImage
	 * @param x
	 * @param y
	 * @param color
	 * @return
	 */
	private boolean isIsolate(BufferedImage bufferedImage, Point point, Color color) {
		int x = (int) point.getX(), y = (int) point.getY();
		int count = 0;
		int left = 0, top = 0;
		for (int i = x - intensitySize;; i++) {
			if (i >= 0) {
				left = i;
				break;
			}
		}
		for (int i = y - intensitySize;; i++) {
			if (i >= 0) {
				top = i;
				break;
			}
		}
		for (int i = left; i >= 0 && i >= x - intensitySize && i <= width - 1 && i <= x + intensitySize; i++) {
			for (int j = top; j >= 0 && j >= y - intensitySize && j <= height - 1 && j <= y + intensitySize; j++) {
				int argb = bufferedImage.getRGB(i, j);
				Color aroundColor = this.getStandardColor(new Color(argb));
				if (isColorEquals(color, aroundColor)) {
					count++;
				}
			}
		}
		return 4 * count < intensityPercent;
	}

	private Color getStandardColor(Color color) {
		if (color == null) {
			return null;
		}
		int alpha = Math.round(color.getAlpha() / colorDifference);
		int red = Math.round(color.getRed() / colorDifference);
		int green = Math.round(color.getGreen() / colorDifference);
		int blue = Math.round(color.getBlue() / colorDifference);
		return new Color(red * colorDifference, green * colorDifference, blue * colorDifference, alpha * colorDifference);
	}

	/**
	 * 判断颜色是否相同
	 * 
	 * @param color1
	 * @param color2
	 * @return
	 */
	private boolean isColorEquals(Color color1, Color color2) {
		if (color1 == null || color2 == null) {
			return false;
		}
		if (Math.abs(color1.getRed() - color2.getRed()) <= colorDifference && Math.abs(color1.getGreen() - color2.getGreen()) <= colorDifference
				&& Math.abs(color1.getBlue() - color2.getBlue()) <= colorDifference) {
			return true;
		}
		return false;
	}

	/**
	 * 计算颜色差距
	 * 
	 * @param color1
	 * @param color2
	 * @return
	 */
	private int calculateColorDifference(Color color1, Color color2) {
		if (color1 == null || color2 == null) {
			throw new IllegalArgumentException("color1,color2不能为空！");
		}
		int result = 0;
		result += Math.sqrt(color1.getAlpha() - color2.getAlpha());
		result += Math.sqrt(color1.getRed() - color2.getRed());
		result += Math.sqrt(color1.getGreen() - color2.getGreen());
		result += Math.sqrt(color1.getBlue() - color2.getBlue());
		return result;
	}
}
