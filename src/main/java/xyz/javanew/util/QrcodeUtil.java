package xyz.javanew.util;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Hashtable;

import javax.imageio.ImageIO;

import lombok.Data;

import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.Result;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

public class QrcodeUtil {
	private static final Logger logger = Logger.getLogger(QrcodeUtil.class);
	private static final String DEFAULT_CHARSET = "utf-8";

	@Data
	public static class QrcodeInfo {
		private String qrcodeContent;
		private String qrcodePath;
		private String qrcodeName;
		private boolean needCompress = true;
		private String logoPath;
		private String qrcodeFormat = "jpg";
		private int qrcodeWidth = 128;// 二维码宽度
		private int qrcodeHeight = 128;// 二维码高度
		private int marginLeft = 48;// 间隔宽度
		private int marginTop = 48;// 间隔宽度
		private int logoWidth = 32;// LOGO宽度
		private int logoHeight = 32;// LOGO高度
		private int cornerWidth = 6;// 四个角
		private int cornerHeight = 6;// 四个角
		private String background = "FFFFFF";// 背景色
		private String foreground = "000000";// 前景色

		public QrcodeInfo() {
			super();
		}
	}

	private static BufferedImage createImage(QrcodeInfo qrcodeInfo) throws Exception {
		Hashtable<EncodeHintType, Object> hints = new Hashtable<EncodeHintType, Object>();
		ErrorCorrectionLevel calculateQrcodeECL = calculateQrcodeECL(qrcodeInfo.qrcodeContent);
		hints.put(EncodeHintType.ERROR_CORRECTION, calculateQrcodeECL);
		hints.put(EncodeHintType.CHARACTER_SET, DEFAULT_CHARSET);
		hints.put(EncodeHintType.MARGIN, 0);
		BitMatrix bitMatrix = new MultiFormatWriter().encode(qrcodeInfo.qrcodeContent, BarcodeFormat.QR_CODE, qrcodeInfo.qrcodeWidth,
				qrcodeInfo.qrcodeHeight, hints);
		// bitMatrix = removeWhiteSpace(bitMatrix);
		int width = bitMatrix.getWidth();
		int height = bitMatrix.getHeight();
		/*
		 * int width = qrcodeInfo.qrcodeWidth; int height = qrcodeInfo.qrcodeHeight;
		 */
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int foreground = Integer.parseInt(qrcodeInfo.foreground, 16);
				int background = Integer.parseInt(qrcodeInfo.background, 16);
				image.setRGB(x, y, bitMatrix.get(x, y) ? foreground : background);
			}
		}
		// 插入图片
		if (StringUtils.hasText(qrcodeInfo.logoPath)) {
			insertLogoImage(image, qrcodeInfo);
		}
		return image;
	}

	/**
	 * 插入LOGO
	 * 
	 * @param source 二维码图片
	 * @param imgPath LOGO图片地址
	 * @throws Exception
	 */
	private static void insertLogoImage(BufferedImage source, QrcodeInfo qrcodeInfo) throws Exception {
		File file = new File(qrcodeInfo.logoPath);
		if (!file.exists()) {
			logger.error("" + qrcodeInfo.logoPath + "   该文件不存在！");
			return;
		}
		Image src = ImageIO.read(new File(qrcodeInfo.logoPath));
		int width = src.getWidth(null);
		int height = src.getHeight(null);
		if (qrcodeInfo.needCompress) { // 压缩LOGO
			if (width > qrcodeInfo.logoWidth) {
				width = qrcodeInfo.logoWidth;
			}
			if (height > qrcodeInfo.logoHeight) {
				height = qrcodeInfo.logoHeight;
			}
			Image image = src.getScaledInstance(width, height, Image.SCALE_SMOOTH);
			BufferedImage tag = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			Graphics g = tag.getGraphics();
			g.drawImage(image, 0, 0, null); // 绘制缩小后的图
			g.dispose();
			src = image;
		}
		// 插入LOGO
		Graphics2D graph = source.createGraphics();
		int x = qrcodeInfo.marginLeft;
		int y = qrcodeInfo.marginTop;
		graph.drawImage(src, x, y, width, height, null);
		int arcw = qrcodeInfo.cornerWidth;
		int arch = qrcodeInfo.cornerHeight;
		Shape shape = new RoundRectangle2D.Float(x, y, width, width, arcw, arch);
		graph.setStroke(new BasicStroke(3f));
		graph.draw(shape);
		graph.dispose();
	}

	/**
	 * 生成二维码(内嵌LOGO)
	 * 
	 * @param qrcodeInfo 内容
	 * @return String 二维码全路径
	 */
	public static String generateQrcode(QrcodeInfo qrcodeInfo) {
		try {
			String fullPath = qrcodeInfo.qrcodePath + "/" + qrcodeInfo.qrcodeName + "." + qrcodeInfo.qrcodeFormat;
			File file = new File(fullPath);
			// 当文件夹不存在时，mkdirs会自动创建多层目录，区别于mkdir．(mkdir如果父目录不存在则会抛出异常)
			if (!file.exists()) {
				file.mkdirs();
			}
			BufferedImage image = createImage(qrcodeInfo);
			ImageIO.write(image, qrcodeInfo.qrcodeFormat, new File(fullPath));
			return fullPath;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 生成二维码(内嵌LOGO)
	 * 
	 * @param content 内容
	 * @param imgPath LOGO地址
	 * @param output 输出流
	 * @param needCompress 是否压缩LOGO
	 */
	public static boolean generateQrcodeStream(QrcodeInfo qrcodeInfo, OutputStream output) {
		try {
			BufferedImage image = createImage(qrcodeInfo);
			ImageIO.write(image, qrcodeInfo.qrcodeFormat, output);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 解析二维码
	 * 
	 * @param file 二维码图片
	 * @return
	 * @throws Exception
	 */
	public static String parseQrcode(File file) {
		try {
			BufferedImage image = ImageIO.read(file);
			if (image == null) {
				return null;
			}
			BufferedImageLuminanceSource source = new BufferedImageLuminanceSource(image);
			BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
			Hashtable<DecodeHintType, Object> hints = new Hashtable<DecodeHintType, Object>();
			hints.put(DecodeHintType.CHARACTER_SET, DEFAULT_CHARSET);
			Result result = new MultiFormatReader().decode(bitmap, hints);
			String resultStr = result.getText();
			return resultStr;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}

	/**
	 * @param length
	 * @return
	 */
	private static ErrorCorrectionLevel calculateQrcodeECL(String source) {
		int length = source.getBytes(Charset.defaultCharset()).length;
		// L(7%)-154、M(15%)-122、Q(25%)-86、H(30%)-64
		ErrorCorrectionLevel result = ErrorCorrectionLevel.L;
		if (length <= 64) {
			result = ErrorCorrectionLevel.H;
		} else if (length <= 86) {
			result = ErrorCorrectionLevel.Q;
		} else if (length <= 122) {
			result = ErrorCorrectionLevel.M;
		}
		logger.info("内容【" + source + "】字节长度为：" + length + ",自动容错等级为：" + result);
		return result;
	}

	private static BitMatrix removeWhiteSpace(BitMatrix matrix) {
		int[] rec = matrix.getEnclosingRectangle();
		int resWidth = rec[2] + 1;
		int resHeight = rec[3] + 1;

		BitMatrix resMatrix = new BitMatrix(resWidth, resHeight);
		resMatrix.clear();
		for (int i = 0; i < resWidth; i++) {
			for (int j = 0; j < resHeight; j++) {
				if (matrix.get(i + rec[0], j + rec[1])) resMatrix.set(i, j);
			}
		}
		return resMatrix;
	}

	/**
	 * 解析二维码
	 * 
	 * @param path 二维码图片地址
	 * @return
	 * @throws Exception
	 */
	public static String parseQrcode(String path) {
		return parseQrcode(new File(path));
	}

	public static void main(String[] args) {
		String qrcodeContent = "1234567890" + "1234567890" + "1234567890" + "1234567890" + "1234567890" + "1234567890" + "1234567890" + "1234567890"
				+ "1234567890" + "1234567890" + "1234567890" + "1234567890" + "1234567890" + "1234567890" + "1234567890" + "1234567890"
				+ "1234567890" + "1234567890" + "1234567890" + "1234567890" + "1234567890" + "1234567890" + "1234567890" + "1234567890"
				+ "1234567890" + "1234567890" + "1234567890" + "1234567890" + "1234567890" + "1234567890" + "1234567890" + "1234567890"
				+ "1234567890" + "1234567890" + "1234567890" + "1234567890" + "1234567890" + "1234567890" + "1234567890" + "1234567890"
				+ "1234567890" + "1234567890" + "1234567890" + "1234567890" + "1234567890" + "1234567890" + "1234567890" + "1234567890"
				+ "1234567890" + "1234567890" + "1234567890" + "1234567890" + "1234567890" + "1234567890" + "1234567890" + "1234567890"
				+ "1234567890";
		String qrcodeContent2 = "该工具出自：https://github.com/jeromeetienne/jquery-qrcode（Jquery不支持图片，这里用样式控制覆盖在上面，如果图片太大，会扫不出来）";
		String qrcodeContent3 = "1234567890" + "1234567890";
		String qrcodePath = "/D:/项目读写文件/Qrcode";
		String logoPath = "/D:/项目读写文件/Qrcode/头像.png";
		QrcodeInfo qrcodeInfo = new QrcodeInfo();
		qrcodeInfo.setQrcodeName("temp");
		qrcodeInfo.setQrcodeContent(qrcodeContent3);
		qrcodeInfo.setQrcodePath(qrcodePath);
		qrcodeInfo.setNeedCompress(true);
		// qrcodeInfo.setLogoPath(logoPath);
		qrcodeInfo.setQrcodeWidth(128);// 16-0,32-1,48-2,64-1,80-0,96-1,112-1,128-0
		qrcodeInfo.setQrcodeHeight(128);
		// qrcodeInfo.setLogoWidth(32);
		// qrcodeInfo.setLogoHeight(32);
		// qrcodeInfo.setMarginLeft(128);
		// qrcodeInfo.setMarginTop(128);
		qrcodeInfo.setBackground("FFFFFF");
		qrcodeInfo.setForeground("FF00FF");
		String generateQrcode = generateQrcode(qrcodeInfo);
		System.out.println(generateQrcode);
		// System.out.println(parseQrcode(new File(generateQrcode)));
		// String parsePath = "D:\\项目读写文件\\Qrcode\\微信.jpg";
		// File file = new File(parsePath);
		// String parseQrcode = parseQrcode(file);
		// System.out.println(parseQrcode);
		// String parsePath = "D:\\项目读写文件\\Qrcode\\weixin.png";
		// File file = new File(parsePath);
		// String parseQrcode = parseQrcode(file);
		// System.out.println(parseQrcode);

	}
}