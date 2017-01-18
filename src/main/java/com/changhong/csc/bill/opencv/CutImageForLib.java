package com.changhong.csc.bill.opencv;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class CutImageForLib {
	public static final double DEFAULT_CANNY_THRESHOLD1 = 14d;
	public static final double DEFAULT_CANNY_THRESHOLD2 = 40d;
	
	private double cnyThreshold1 = DEFAULT_CANNY_THRESHOLD1;
	private double cnyThreshold2 = DEFAULT_CANNY_THRESHOLD2;
	private static String ip = null;
	private static int charMinSize;
	private static int charMaxSize;
	
	private static final int DPI_LESS_300 = 0;
	private static final int DPI_300 = 1;
	private static final int DPI_600 = 2;
	
	private static int dpiType = DPI_300;//0.<300dpi;1.300dpi;2.600dpi
	
	static {
		try {
			ip = InetAddress.getLocalHost().getHostAddress();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unused")
	public ArrayList<RotatedRect> edgeDetect(Mat m) {
		Mat imageGray = new Mat();
		Mat imageCny = new Mat();
		Mat hierarchy = new Mat();
		
		Mat cloneM = m.clone();
		
		//去噪
		Imgproc.bilateralFilter(cloneM, imageGray, 5, 50, 50);	//双边滤波  第三个参数过大导致计算慢，一般图片噪声不大可适当调低
		Imgproc.medianBlur(imageGray, imageGray, 5);			//中值滤波平滑
		
//		//强化边缘
//		Mat imageSharpness = new Mat(m.rows(), m.cols(), m.type());
//		Imgproc.GaussianBlur(imageGray, imageSharpness, new Size(0, 0), 10d);
//		Core.addWeighted(imageGray, 1.25d, imageSharpness, -0.25d, 0d, imageSharpness);
//		imageGray.release();
//		imageGray = imageSharpness;
		
		Imgproc.cvtColor(imageGray, imageGray, Imgproc.COLOR_BGR2GRAY);            //转化为灰度图
		Imgproc.Canny(imageGray, imageCny, cnyThreshold1, cnyThreshold2, 3, true);//canny 算子
		Imgproc.threshold(imageCny, imageCny, 120, 255, Imgproc.THRESH_BINARY);  //Threshold Binary：即二值化，将大于阈值的灰度值设为最大灰度值，小于阈值的值设为0。
		
		Mat kernel = new Mat(3, 3, CvType.CV_8U, new Scalar(255));
		Imgproc.dilate(imageCny, imageCny, kernel); 							 //膨胀图像，模糊化
		
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		List<MatOfPoint> resultContours = new ArrayList<MatOfPoint>();
		
//		Imgproc.findContours(imageCny, contours, hierarchy,
//				Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);					//获得图像轮廓
//		
		Imgproc.findContours(imageCny, contours, hierarchy,
				Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_NONE);					//获得图像轮廓
		//draw the origin contours
		Imgproc.drawContours(cloneM, contours, -1, new Scalar(255,255,255));
		BufferedImage image = (BufferedImage) ImageUtil.toBufferedImage(cloneM);	
		// save the image
		String path = "d:/image/temp/";
		String name = "contours.jpg";
		File file2 = new File(path,name);
		try{
			if(!file2.exists()){
				file2.createNewFile();
			}
			ImageIO.write(image, "jpg", file2);
		}catch(Exception e){
			e.printStackTrace();
		}

		List<MatOfPoint2f> approx = new ArrayList<MatOfPoint2f>();

		for (int i = 0; i < contours.size(); i++) {

			MatOfPoint contour = contours.get(i);
			MatOfPoint2f myPt = new MatOfPoint2f();
			contour.convertTo(myPt, CvType.CV_32FC2); 							//MatOfPoint转换数据类型 32F 32bit float
			approx.add(myPt);

			MatOfPoint2f myPt2 = new MatOfPoint2f();
			contour.convertTo(myPt2, CvType.CV_32FC2);

			Imgproc.approxPolyDP(myPt2, myPt,
					Imgproc.arcLength(myPt2, true) * 0.02, true);  				//对图像轮廓点进行多边形拟合 arclength：计算轮廓或曲线的长度
			
			MatOfPoint points = new MatOfPoint(myPt.toArray());
			MatOfPoint points2 = new MatOfPoint(myPt2.toArray());
			
			Rect r = Imgproc.boundingRect(points);								//获取外部矩形边界
			double area = Imgproc.contourArea(points);							//计算轮廓矩面积。
			if(area < m.size().height * m.size().width*0.05)		//火车票（目前最小票据）大概占据A4纸的0.08左右，所以将阈值设为0.05，过滤点大部分无关轮廓
				continue;
//			if (area < 100 || r.width < m.size().width * 0.1 || r.height < m.size().height * 0.1)
//				continue;
			if (r.width > m.size().width * 0.8 && r.height > m.size().height * 0.8)
				continue;
			
			resultContours.add(contour);

			System.out.println("myPt: " + myPt.size()
					+ " Imgproc.contourArea(myPt): "
					+ Imgproc.contourArea(myPt));

		}
		
		//draw the resultContours
		Mat cloneM1 = m.clone();
		Imgproc.drawContours(cloneM1, resultContours, -1, new Scalar(255,255,255));
		BufferedImage image1 = (BufferedImage) ImageUtil.toBufferedImage(cloneM1);	
		// save the image
		name = "resultcontours.jpg";
		File file1 = new File(path,name);
		try{
			if(!file1.exists()){
				file1.createNewFile();
			}
			ImageIO.write(image1, "jpg", file1);
		}catch(Exception e){
			e.printStackTrace();
		}
		MatOfPoint cont1, cont2;
		for (int i = 0; i < resultContours.size(); i++) {
			cont1 = resultContours.get(i);
			Rect r1 = Imgproc.boundingRect(cont1);
			for (int j = 1; j < resultContours.size(); j++) {
				cont2 = resultContours.get(j);
				Rect r2 = Imgproc.boundingRect(cont2);
				if(r1.contains(new Point(r2.x + r2.width / 2, r2.y + r2.height / 2)) && r1.area() > r2.area()) {
					resultContours.remove(j);
					j--;
				}
				
				if(r2.contains(new Point(r1.x + r1.width / 2, r1.y + r1.height / 2)) && r2.area() > r1.area()) {
					resultContours.remove(i);
					i--;
					break;
				}
			}
		}
		//draw the finalContours
				Mat cloneM2 = m.clone();
				Imgproc.drawContours(cloneM2, resultContours, -1, new Scalar(255,255,255));
				BufferedImage image3 = (BufferedImage) ImageUtil.toBufferedImage(cloneM2);	
				// save the image
				name = "finalcontours.jpg";
				File file3 = new File(path,name);
				try{
					if(!file3.exists()){
						file3.createNewFile();
					}
					ImageIO.write(image3, "jpg", file3);
				}catch(Exception e){
					e.printStackTrace();
				}
		ArrayList<RotatedRect> rectList = new ArrayList<RotatedRect>();
		if(resultContours.size() != 0){
			for (int i = 0; i < resultContours.size(); i++) {
				cont1 = resultContours.get(i);
				MatOfPoint2f myPt = new MatOfPoint2f();
				cont1.convertTo(myPt, CvType.CV_32FC2);
				//如果是单张票据或者边缘不明显的票据
				//返回整张票据
				RotatedRect minAreaRect = Imgproc.minAreaRect(myPt);  				//最小包围矩形
				rectList.add(minAreaRect);
				myPt.release();
				cont1.release();
			}
		}else{
			MatOfPoint2f pts = new MatOfPoint2f();
			pts.fromArray(new Point(0,m.size().height),new Point(0,0),new Point(m.size().width,0), new Point(m.size().height,m.size().width));
			RotatedRect minAreaRect = Imgproc.minAreaRect(pts); 
			RotatedRect result = new RotatedRect(new Point(m.size().width/2, m.size().height/2),new Size(m.size().width,m.size().height),0);
			rectList.add(result);
			pts.release();
		}
		imageGray.release();
		imageCny.release();
		cloneM.release();
		return rectList;
	}
	
	public ArrayList<BufferedImage> cutImages(Mat m, ArrayList<RotatedRect> rectList) {
		//删除以前导出的图片文件
//		deleteOldOutputImages();
		
		if(m.rows() > 5000 || m.cols() > 5000) {//600dpi
			dpiType = DPI_600;
			charMinSize = 30;
			charMaxSize = 180;
		} else if(m.rows() > 2500 || m.cols() > 2500) {//300dpi
			dpiType = DPI_300;
			charMinSize = 15;
			charMaxSize = 90;
		} else {//<300dpi
			dpiType = DPI_LESS_300;
			charMinSize = 10;
			charMaxSize = 70;
		}
		
		ArrayList<BufferedImage> imageList = new ArrayList<BufferedImage>(0);
		for(int i = 0, len = rectList.size(); i < len; i++) {
			RotatedRect rectModel = rectList.get(i);
			double w = rectModel.size.width;
			double h = rectModel.size.height;
			Point pts[] = new Point[4];
			rectModel.points(pts);
			Mat result = new Mat();
		    MatOfPoint2f src = new MatOfPoint2f();
		    src.fromArray(pts[0],pts[1],pts[2]);
		    MatOfPoint2f tgt=new MatOfPoint2f();
		    tgt.fromArray(new Point(0,h),new Point(0,0),new Point(w,0));
		    // get the AffineTransform Matrix
		    Mat rotImage = Imgproc.getAffineTransform(src, tgt);
		    Imgproc.warpAffine(m, result, rotImage, new Size(w , h), 
		    		Imgproc.INTER_LINEAR + Imgproc.CV_WARP_FILL_OUTLIERS, 0, new Scalar(255, 255, 255));

			BufferedImage image = (BufferedImage) ImageUtil.toBufferedImage(result);	
			// save the image
//			name = "temp2.jpg";
//			File file2 = new File(path,name);
//			try{
//				if(!file2.exists()){
//					file2.createNewFile();
//				}
//				ImageIO.write(image, "jpg", file2);
//			}catch(Exception e){
//				e.printStackTrace();
//			}
			imageList.add(image);
			result.release();
		}

		return imageList;
	}
	
	public boolean rotateImages(ArrayList<RotateImageModel> imageList) {
		if(imageList.size() == 0) {
			return false;
		}
		for(int i = 0, len = imageList.size(); i < len; i++) {
			RotateImageModel imageModel = imageList.get(i);
			double angle = imageModel.getRotation();
			if(angle % 360d == 0d){
				continue;
			}
			String imagePath = PathConst.DEFAULT_DEMO_FOLDER_PATH + imageModel.getName();
			Mat image = Imgcodecs.imread(imagePath);
			double w = image.cols();
			double h = image.rows();
			
			//transform
			double size = w > h ? w : h;
			Mat tm = new Mat(2, 3, CvType.CV_32FC1);
			float tx = (float) (size / 2d - w / 2d);
			float ty = (float) (size / 2d - h / 2d);
			float[] value = { 1f, 0f, tx, 0f, 1f, ty };
			tm.put(0, 0, value);
			Mat result0 = new Mat((int)size, (int)size, image.type());
			Imgproc.warpAffine(image, result0, tm,  new Size(size, size), Imgproc.INTER_LINEAR
					+ Imgproc.CV_WARP_FILL_OUTLIERS, 0, new Scalar(255, 255, 255));
			
			// rotating image
			double centerX = size / 2d;
			double centerY = size / 2d;
		    Point center = new Point(centerX, centerY);
		    //1.0 means 100 % scale
		    Mat rotImage = Imgproc.getRotationMatrix2D(center, angle, 1.0);
		    double[] d = new double[6];
		    rotImage.get(0, 0, d);
		    Mat result1 = new Mat((int)size, (int)size, image.type());
		    Imgproc.warpAffine(result0, result1, rotImage, new Size(size, size), 
		    		Imgproc.INTER_LINEAR + Imgproc.CV_WARP_FILL_OUTLIERS, 0, new Scalar(255, 255, 255));
		    
			// move image to top left point of the inner rect
		    boolean isReplaceWH = Math.round(Math.abs(angle) / 90d) % 2 == 1;
		    if(isReplaceWH) {
		    	double temp = w;
		    	w = h;
		    	h = temp;
		    }
			tm = new Mat(2, 3, CvType.CV_32FC1);
			tx = (float) -(result1.cols() / 2f - w / 2f);
			ty = (float) -(result1.rows() / 2f - h / 2f);
			float[] v = { 1f, 0f, tx, 0f, 1f, ty };
			tm.put(0, 0, v);
			Mat result2 = new Mat((int) h, (int) w, image.type());
			Imgproc.warpAffine(result1, result2, tm, new Size(w, h),
					Imgproc.INTER_LINEAR + Imgproc.CV_WARP_FILL_OUTLIERS, 0,
					new Scalar(255, 255, 255));
		    Imgcodecs.imwrite(imagePath, result2);
		    
		    tm.release();
		    image.release();
		    result0.release();
		    result1.release();
		    result2.release();
		}
		 return true;
	}
	
	private Mat rotateImage(Mat image, double angle) {
		if(angle % 360d == 0d){
			return image;
		}
		double w = image.cols();
		double h = image.rows();
		
		//transform
		double size = w > h ? w : h;
		Mat tm = new Mat(2, 3, CvType.CV_32FC1);
		float tx = (float) (size / 2d - w / 2d);
		float ty = (float) (size / 2d - h / 2d);
		float[] value = { 1f, 0f, tx, 0f, 1f, ty };
		tm.put(0, 0, value);
		Mat result0 = new Mat((int)size, (int)size, image.type());
		Imgproc.warpAffine(image, result0, tm,  new Size(size, size), Imgproc.INTER_LINEAR
				+ Imgproc.CV_WARP_FILL_OUTLIERS, 0, new Scalar(255, 255, 255));
		
		// rotating image
		double centerX = size / 2d;
		double centerY = size / 2d;
	    Point center = new Point(centerX, centerY);
	    //1.0 means 100 % scale
	    Mat rotImage = Imgproc.getRotationMatrix2D(center, angle, 1.0);
	    double[] d = new double[6];
	    rotImage.get(0, 0, d);
	    Mat result1 = new Mat((int)size, (int)size, image.type());
	    Imgproc.warpAffine(result0, result1, rotImage, new Size(size, size), 
	    		Imgproc.INTER_LINEAR + Imgproc.CV_WARP_FILL_OUTLIERS, 0, new Scalar(255, 255, 255));
	    
		// move image to top left point of the inner rect
	    boolean isReplaceWH = Math.round(Math.abs(angle) / 90d) % 2 == 1;
	    if(isReplaceWH) {
	    	double temp = w;
	    	w = h;
	    	h = temp;
	    }
		tm = new Mat(2, 3, CvType.CV_32FC1);
		tx = (float) -(result1.cols() / 2f - w / 2f);
		ty = (float) -(result1.rows() / 2f - h / 2f);
		float[] v = { 1f, 0f, tx, 0f, 1f, ty };
		tm.put(0, 0, v);
		Mat result2 = new Mat((int) h, (int) w, image.type());
		Imgproc.warpAffine(result1, result2, tm, new Size(w, h),
				Imgproc.INTER_LINEAR + Imgproc.CV_WARP_FILL_OUTLIERS, 0,
				new Scalar(255, 255, 255));
	    
	    tm.release();
	    image.release();
	    result0.release();
	    result1.release();
	    
	    return result2;
	}
	
	private Mat[] rotateImageVer(Mat m, String fileName) {
		// 去噪
		Mat imageGray = new Mat();
		Mat hierarchy = new Mat();

		Imgproc.bilateralFilter(m.clone(), imageGray, 8, 50, 50);
		Imgproc.medianBlur(imageGray, imageGray, dpiType == DPI_600 ? 5 : 3);
		Imgproc.cvtColor(imageGray, imageGray, Imgproc.COLOR_BGR2GRAY);
		Imgproc.adaptiveThreshold(imageGray, imageGray, 255,
				Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY_INV,
				dpiType == DPI_600 ? 17 : 11, 5);
		Mat cloneM = imageGray.clone();

		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		List<MatOfPoint> resultContours = new ArrayList<MatOfPoint>();

		Imgproc.findContours(imageGray, contours, hierarchy, Imgproc.RETR_LIST,
				Imgproc.CHAIN_APPROX_NONE);

		List<MatOfPoint2f> approx = new ArrayList<MatOfPoint2f>();

		for (int i = 0; i < contours.size(); i++) {
			MatOfPoint contour = contours.get(i);
			MatOfPoint2f myPt = new MatOfPoint2f();
			contour.convertTo(myPt, CvType.CV_32FC2);
			approx.add(myPt);

			MatOfPoint2f myPt2 = new MatOfPoint2f();
			contour.convertTo(myPt2, CvType.CV_32FC2);
			Imgproc.approxPolyDP(myPt2, myPt, Imgproc.arcLength(myPt2, true) * 0.02, true);
			MatOfPoint points = new MatOfPoint(myPt.toArray());
			Rect r = Imgproc.boundingRect(points);
			if (r.width < charMinSize || r.height < charMinSize || r.width > charMaxSize || r.height > charMaxSize)
				continue;
			if (r.width > m.size().width * 0.8 && r.height > m.size().height * 0.8)
				continue;

			resultContours.add(contour);
		}

		MatOfPoint cont1, cont2;
		for (int i = 0; i < resultContours.size(); i++) {
			cont1 = resultContours.get(i);
			Rect r1 = Imgproc.boundingRect(cont1);
			for (int j = 1; j < resultContours.size(); j++) {
				cont2 = resultContours.get(j);
				Rect r2 = Imgproc.boundingRect(cont2);
				if (r1.contains(new Point(r2.x + r2.width / 2, r2.y + r2.height
						/ 2))
						&& r1.area() > r2.area()) {
					resultContours.remove(j);
					j--;
				}

				if (r2.contains(new Point(r1.x + r1.width / 2, r1.y + r1.height
						/ 2))
						&& r2.area() > r1.area()) {
					resultContours.remove(i);
					i--;
					break;
				}
			}
		}

		int wMoreThanHNum = 0;
		for (int i = 0; i < resultContours.size(); i++) {
			cont1 = resultContours.get(i);
			MatOfPoint2f myPt = new MatOfPoint2f();
			cont1.convertTo(myPt, CvType.CV_32FC2);
			Rect boundingRect = Imgproc.boundingRect(cont1);
			Point[] minAreaRectP = new Point[4];
			RotatedRect minAreaRect = Imgproc.minAreaRect(myPt);
			minAreaRect.points(minAreaRectP);

			Rect resultBoundingRect = Imgproc.boundingRect(new MatOfPoint(
					boundingRect.tl(), boundingRect.br(), minAreaRectP[0],
					minAreaRectP[1], minAreaRectP[2], minAreaRectP[3]));
			Rect cutRect = new Rect(
					new Point(clamp(resultBoundingRect.tl().x, 0, m.cols()),
							clamp(resultBoundingRect.tl().y, 0, m.rows())),
					new Point(clamp(resultBoundingRect.br().x, 0, m.cols()),
							clamp(resultBoundingRect.br().y, 0, m.rows())));

			Mat cutImage = cloneM.submat(cutRect);
			if (cutImage.cols() > cutImage.rows())
				wMoreThanHNum++;
			cutImage.release();
		}
		double verPercent = (double) ((double) wMoreThanHNum / (double) resultContours
				.size());
		System.out.println("width longer than hight percent : " + verPercent);

		imageGray.release();
		hierarchy.release();

		Mat[] matList = new Mat[2];
		matList[0] = m;
		matList[1] = cloneM;
		//hor image, rotate 90 degree
		if(verPercent > 0.5) {
			matList[0] = rotateImage(m, 90d);
			matList[1] = rotateImage(cloneM, 90d);
		} 
		return matList;
	}
	
	private double clamp(double val, double sideVal1, double sideVal2) {
		double min = Math.min(sideVal1, sideVal2);
		double max = Math.max(sideVal1, sideVal2);
		return val > max ? max : (val < min ? min : val);
	}
	
	//删除以前导出的图片文件
	private void deleteOldOutputImages() {
		String path = PathConst.DEFAULT_DEMO_FOLDER_PATH;
		File file = new File(path);
		if (!file.exists()) {
			System.out.println(path + "不存在。");
			return;
		}
		if (!file.isDirectory()) {
			System.out.println(path + "不是文件夹。");
			return;
		}

		File[] list = file.listFiles();
		if (list == null || list.length == 0) {
			System.out.println(path + "下没有内容。");
			return;
		}
		int i, len;
		String fileName;
		File childFile;
		len = list.length;
		for (i = 0; i < len; i++) {
			childFile = list[i];
			if (!childFile.isDirectory()) {
				fileName = childFile.getName();
				if (fileName != null && fileName.startsWith("note_")) {
					childFile.delete();
				}
			}
		}
	}

	public double getCnyThreshold1() {
		return cnyThreshold1;
	}

	public void setCnyThreshold1(double cnyThreshold1) {
		this.cnyThreshold1 = cnyThreshold1;
	}

	public double getCnyThreshold2() {
		return cnyThreshold2;
	}

	public void setCnyThreshold2(double cnyThreshold2) {
		this.cnyThreshold2 = cnyThreshold2;
	}
}