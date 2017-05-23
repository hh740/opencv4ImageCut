package com.changhong.csc.bill.opencv;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.imgcodecs.Imgcodecs;

public class OpenCVLib {
	
	public static final String LINUX = "/myfolder/opencv_tools/opencv-3.0.0/build/share/OpenCV/java/libopencv_java300.so";
	public static final String WINDOWS = "D:/opencv/build/java/x64/opencv_java300.dll";
	public static final String OS_NAME = "os.name";
	
	private CutImageForLib cutLib = new CutImageForLib();
	private Mat image;
	
	static {
		String loadContent = LINUX;
		String osName = System.getProperty(OS_NAME);
		if(null != osName && osName.trim().length() > 0) {
			if(osName.toLowerCase().startsWith("windows")) {
				loadContent = WINDOWS;
			}
		}
		System.load(loadContent);
		System.err.println("此操作系统为：" + osName);
		System.err.println("加载本地文件：" + loadContent);
	}
	
	/**
	 * 获取票据边框信息
	 * @param path 扫描件url或者本地绝对路径
	 * @return 返回票据边框信息
	 * 
	 * @see com.changhong.csc.bill.RectModel
	 */
	public ArrayList<RectModel> getInfo(String path) {
		loadImage(path);
		ArrayList<RotatedRect> rectList = cutAndDeskewImage();
		if(rectList == null) {
			return null;
		}
		ArrayList<RectModel> resultList = new ArrayList<RectModel>(rectList.size()); //封装矩形的信息
		for(RotatedRect rect : rectList) {
			RectModel model = new RectModel();
			model.setWidth(rect.size.width);
			model.setHeight(rect.size.height);
			model.setRotation(rect.angle);
			Point[] pts = new Point[4];
			rect.points(pts);
			model.setX(pts[1].x);
			model.setY(pts[1].y);
			resultList.add(model);
		}
		return resultList;
	}
	
	/**
	 * 从指定的图片上切割矩形区域
	 * @param path 源图片url或者本地绝对路径
	 * @param rectList 要切割的矩形区域列表
	 * @return 返回根据传入的矩形区域列表信息切割好的BufferImage列表
	 * 
	 * @see com.changhong.csc.bill.RectModel
	 */
	public ArrayList<BufferedImage> cut(String path, ArrayList<RectModel> rectList) {
		if(rectList == null || rectList.size() == 0) {
			return null;
		}
		loadImage(path);
		if(image == null) {
			return null;
		}
//		if(rectList.size() == 1){
//			ArrayList<BufferedImage> images = new ArrayList<BufferedImage>();
//			images.add((BufferedImage) ImageUtil.toBufferedImage(image));
//			return images;
//		}
		return cutLib.cutImages(image.clone(), rectList);
	}
	
	public boolean rotate(ArrayList<RotateImageModel> imageList) {
		boolean isSuccessful = false;
		try {
			isSuccessful = cutLib.rotateImages(imageList);
		} catch(Exception e) {
		    e.printStackTrace();
		}
		return isSuccessful;
	}
	
	private ArrayList<RotatedRect> cutAndDeskewImage() {
		if(image == null) {
			return null;
		}
		return cutLib.edgeDetect(image.clone());
	}
	
	private void loadImage(String inputStr) {
		if(!inputStr.equals("") && !inputStr.equals(null)) {
			if(inputStr.startsWith("http") || inputStr.startsWith("https")) {
				image = loadRemoteImage(inputStr);
			} else {
				image = Imgcodecs.imread(inputStr);
			}
		}
	}
	
	private Mat loadRemoteImage(String spec) {
	    Mat m = null;
	    InputStream inStream = null;
		try {
			// new一个URL对象
			URL url = new URL(spec);
			// 打开链接
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			// 设置请求方式为"GET"
			conn.setRequestMethod("GET");
			// 超时响应时间为5秒
			conn.setConnectTimeout(5 * 1000);
			// 通过输入流获取图片数据
			inStream = conn.getInputStream();
			//转换inputStream为matrix，并且解码图片
			m = ImageUtil.toMat(inStream);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		    if(inStream != null) {
		        try {
		            inStream.close();
		        } catch(Exception e) {
		            e.printStackTrace();
		        }
		    }
		}
		return m;
	}
	
//	private byte[] readInputStream(InputStream inStream) throws Exception {
//		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
//		// 创建一个Buffer字符串
//		byte[] buffer = new byte[1024];
//		// 每次读取的字符串长度，如果为-1，代表全部读取完毕
//		int len = 0;
//		// 把输入流从里的数据读取到buffer中
//		while ((len = inStream.read(buffer)) != -1) {
//			// 把buffer中的数据写入输出流中，中间参数代表从哪个位置开始读，len代表读取的长度
//			outStream.write(buffer, 0, len);
//		}
//		// 关闭输入流
//		inStream.close();
//		// 把outStream里的数据写入内存
//		return outStream.toByteArray();
//	}
	
//	private Point getTopLeftPoint(RotatedRect rect) {
//		double[] vals = {rect.center.x, rect.center.y, rect.size.width, rect.size.height, 0d};
//		RotatedRect newRect = new RotatedRect(vals);
//		Point[] newPt = new Point[4];
//		newRect.points(newPt);
//		double newTopLeftX = newPt[0].x, newTopLeftY = newPt[0].y;
//		for(int i = 1, len = newPt.length; i < len; i++) {
//			newTopLeftX = newTopLeftX < newPt[i].x ? newTopLeftX : newPt[i].x;
//			newTopLeftY = newTopLeftY < newPt[i].y ? newTopLeftY : newPt[i].y;
//		}
//		
//		double tx, ty, rx, ry, radius, rotateRadians;
//		tx = newTopLeftX - newRect.center.x;
//		ty = newTopLeftY - newRect.center.y;
//		radius = Math.sqrt(tx * tx + ty * ty);
//		rotateRadians = -Math.PI + Math.atan2(Math.abs(ty) , Math.abs(tx)) + rect.angle * Math.PI / 180d; 
//		rx = rect.center.x + radius * Math.cos(rotateRadians);
//		ry = rect.center.y + radius * Math.sin(rotateRadians);
//		return new Point(rx, ry);
//	}
	
}
