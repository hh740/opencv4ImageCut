package com.changhong.csc.bill.opencv;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.opencv.core.RotatedRect;
import org.opencv.imgproc.Imgproc;

/**
 * OpenCV测试代码，用于测试OpenCV环境是否配置好<br>
 * 测试步骤：<br>
 * 		1、修改OpenCVLib对opencv本地文件的加载，Linux/Windows二选一，注释掉其中一个<br>
 * 		2、使用eclipse导出     Runnable JAR file     类型<br>
 * 
 * @author dehai.wang@changhong.com
 *
 */
public class Main {
	
	public static void main(String[] args) {
		m1();
	}


	private static void m1() {
		try {
			//String path = "http://10.4.68.7:9999/group1/M00/00/52/CgREB1ggHXGAegoJAAGtkX17jZE290.png";
			String path = "D:/image/test.jpg";
			OpenCVLib lib = new OpenCVLib();
			System.out.println("OenCVLib初始化。。。");
			ArrayList<RotatedRect> info = lib.getInfo(path);
			System.out.println(info);
			//display the rectangle
			ArrayList<BufferedImage> imageList = lib.cut(path, info);
			saveImage(imageList);
			System.out.println(imageList);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.err.println("成功。。。");
	}
	
	
	public static void saveImage(List<BufferedImage> images){
		try{
			int i = 1; 
			for(BufferedImage image : images){
				String fileName = String.format("%d.jpg", i++);
				File file = new File("D:/image/result/",fileName);
				if(!file.exists()){
					file.createNewFile();
				}
				ImageIO.write(image, "jpg", file);
			}	
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
}

