package xyz.javanew.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Desc 
 *       该类采用缓冲读的方法主要用于改进RandomAccessFile在按行读时的不足(每读写一个字符，就是一次硬盘操作，占用资源太多),效率提升50
 *       -100倍
 * @function1)：主要提供readLineByBuffer()和readLineByBuffer(boolean)方法
 * @function2)：同时提供getReadLineByBufferPosition()获取位置
 * @function3)：readAllContentByBuffer()读取较小文件的全部内容
 * @function4)：setBuffer(byte[] buffer)设置缓冲池大小，默认为10 *1024，根据需要设置，不宜过大过小
 * @instruction1 readLineByBuffer(),默认true,同readLine()，false用于保持与源文件一致性
 *               ，用于复制写入，与readLine()可能不同
 * @instruction2 getReadLineByBufferPosition()仅计算readLineByBuffer()读取增加的长度
 * @author wewenge.yan
 * @Date 2016年8月16日
 * @ClassName RandomAccessFileExtension
 */
public class BufferedRandomAccessFile extends RandomAccessFile {

	private List<String> bufferLines;
	private byte[] lastBytes;
	private byte[] buffer;
	private long position;

	/**
	 * 仅获取readLineByBuffer()所读的字节长度
	 * 
	 * @return
	 */
	public long getReadLineByBufferPosition() {
		return position;
	}

	public void setBuffer(byte[] buffer) {
		this.buffer = buffer;
	}

	public BufferedRandomAccessFile(String name, String mode)
			throws FileNotFoundException {
		super(name, mode);
		this.setBuffer(new byte[1024 * 10]);
		bufferLines = new ArrayList<String>();
	}

	public BufferedRandomAccessFile(File file, String mode)
			throws FileNotFoundException {
		super(file, mode);
		this.setBuffer(new byte[1024 * 10]);
		bufferLines = new ArrayList<String>();
	}

	/**
	 * {@link cn.himma.util.file.RandomAccessFileExtension#readLineByBuffer(boolean)
	 * ;boolean=false}
	 * 
	 * @return
	 */
	public final String readLineByBuffer() {
		return this.readLineByBuffer(true);
	}

	/**
	 * @true:仅取\n为一行,保留\r，用于保留文件一致性,如复制写入(默认)
	 * @false:取\r\n或\n为一行
	 * @param keepR
	 * @return
	 */
	public final String readLineByBuffer(boolean keepR) {
		String result = null;
		int length = 0;
		while (bufferLines.size() <= 0) {
			try {
				length = this.read(buffer);
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (length == -1) {
				break;
			}
			byte[] temp = Arrays.copyOfRange(buffer, 0, length);
			temp = addTwoArrays(lastBytes, temp);
			lastBytes = null;
			int lastIndex = 0;
			for (int i = 0; i < temp.length; i++) {
				// \r\n或\n均为换行符（写入文件有微小差别）
				if (temp[i] == 10) {
					byte[] tempByte = Arrays.copyOfRange(temp, lastIndex, i);
					bufferLines.add(new String(tempByte));
					lastIndex = i + 1;
				} else if (i == temp.length - 1) {
					lastBytes = Arrays
							.copyOfRange(temp, lastIndex, temp.length);
				}
			}
		}
		if (bufferLines.size() > 0) {
			result = bufferLines.get(0);
			bufferLines.remove(0);
		} else if (lastBytes != null) {
			result = new String(lastBytes);
			this.position += lastBytes.length;
			lastBytes = null;
		}
		this.position += result != null ? (result + "\n").getBytes().length : 0;
		if (keepR && result != null && result.endsWith("\r")) {
			result = result.substring(0, result.length() - 1);
		}
		return result;
	}

	public final List<String> readAllContentByBuffer() {
		List<String> allLines = new ArrayList<String>();
		try {
			int length = 0;
			while ((length = this.read(buffer)) != -1) {
				byte[] temp = Arrays.copyOfRange(buffer, 0, length);
				temp = addTwoArrays(lastBytes, temp);
				int lastIndex = 0;
				for (int i = 0; i < temp.length - 1; i++) {
					// \r\n或\n均为换行符（写入文件有微小差别）
					if (temp[i] == 10) {
						byte[] tempByte = Arrays
								.copyOfRange(temp, lastIndex, i);
						bufferLines.add(new String(tempByte));
						lastIndex = i + 1;
					} else if (i == temp.length - 1) {
						lastBytes = Arrays.copyOfRange(temp, lastIndex,
								temp.length);
					}
				}
				this.position = getFilePointer();
			}
			// 针对结尾不换行的文件
			if (lastBytes != null) {
				allLines.add(new String(lastBytes));
				lastBytes = null;
			}
			if (this.position != this.length()) {
				System.out.println(position + "," + length());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return allLines;
	}

	private byte[] addTwoArrays(byte[] former, byte[] latter) {
		if (former == null) {
			return latter;
		} else if (latter == null) {
			return former;
		}
		byte[] array3 = new byte[former.length + latter.length];
		System.arraycopy(former, 0, array3, 0, former.length);
		System.arraycopy(latter, 0, array3, former.length, latter.length);
		return array3;
	}
}