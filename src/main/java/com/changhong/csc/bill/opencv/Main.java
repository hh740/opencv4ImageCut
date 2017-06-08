package com.changhong.csc.bill.opencv;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;


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
//			String path = "http://10.4.68.7:9999/group1/M00/05/84/CgREB1k0y9eAWVDSAA9Pt7rZyxA664.jpg";
//			String path = "D:\\image\\test.jpg";
			String path = "C:\\Users\\hasee\\Desktop\\error-pic\\autopre\\NHHD2.jpg";
//			String path = "C:/Users/hasee/Desktop/xml格式定义/可用测试票据/ZZSPTFP2.jpg";
			OpenCVLib lib = new OpenCVLib();
			System.out.println("OenCVLib初始化。。。");
			ArrayList<RectModel> info = lib.getInfo(path);
			System.out.println(info);
//			ArrayList<RectModel> info = new ArrayList<RectModel>();
//			RectModel model = new RectModel(0,0,2818,1668,0);
//			info.add(model);
			//display the rectangle
			ArrayList<BufferedImage> imageList = lib.cut(path, info);
			saveImage(imageList);
			System.out.println(imageList);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("成功。。。");
	}
	
//	@Test
//	public void dpiTest(){
//		OpenCVLib lib = new OpenCVLib();
//		Mat temp = null;
//		List<BufferedImage> images = new ArrayList<BufferedImage>();
//		try{
////			FileInputStream fis = new FileInputStream(new File);
//			
////			temp = ImageUtil.toMat(fis);
//			temp = Imgcodecs.imread("D:\\image\\XHQD1.jpg");
//			BufferedImage out = (BufferedImage)ImageUtil.toBufferedImage(temp);
//			images.add(out);
////			BufferedImage pic1 = ImageIO.read(fis);
////			images.add(pic1);
//			System.out.println("the test is done");
//			saveImage(images);			
//		}catch(Exception e){
//			e.printStackTrace();
//		}
//		
//		
//		
//	}
	public static void saveImage(List<BufferedImage> images){
		try{
			int i = 1; 
			for(BufferedImage image : images){
				String fileName = String.format("%d.jpg", i++);
				File output = new File("D:/image/result/",fileName);
				if(!output.exists()){
					output.createNewFile();
				}
//				ImageIO.write(image, "jpg", file);
				final String formatName = "jpg";
			    for (Iterator<ImageWriter> iw = ImageIO.getImageWritersByFormatName(formatName); iw.hasNext();) {
			        ImageWriter writer = iw.next();
			        ImageWriteParam writeParam = writer.getDefaultWriteParam();
			        writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
			        writeParam.setCompressionQuality(1.0f);
			        ImageTypeSpecifier typeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB);
			        IIOMetadata metadata = writer.getDefaultImageMetadata(typeSpecifier, writeParam);
			        if (metadata.isReadOnly() || !metadata.isStandardMetadataFormatSupported()) {
			            continue;
			        }
//			        setDPI(metadata, 300, 300);
			        final ImageOutputStream stream = ImageIO.createImageOutputStream(output);
			        try {
			            writer.setOutput(stream);
			            writer.write(metadata, new IIOImage(image, null, metadata), writeParam);
//			            writer.write(null, new IIOImage(image, null, null), writeParam);
			        } finally {
			            stream.close();
			            writer.dispose();
			        }
			        break;
			    }
			}	
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	private static void setDPI(IIOMetadata metadata, int wDpi, int hDpi) throws IIOInvalidTreeException {
		
	    String metadataFormat = "javax_imageio_jpeg_image_1.0";
	    IIOMetadataNode root = new IIOMetadataNode(metadataFormat);
	    IIOMetadataNode jpegVariety = new IIOMetadataNode("JPEGvariety");
	    IIOMetadataNode markerSequence = new IIOMetadataNode("markerSequence");

	    IIOMetadataNode app0JFIF = new IIOMetadataNode("app0JFIF");
	    app0JFIF.setAttribute("majorVersion", "1");
	    app0JFIF.setAttribute("minorVersion", "2");
	    app0JFIF.setAttribute("thumbWidth", "0");
	    app0JFIF.setAttribute("thumbHeight", "0");
	    app0JFIF.setAttribute("resUnits", "01");
	    app0JFIF.setAttribute("Xdensity", String.valueOf(wDpi));
	    app0JFIF.setAttribute("Ydensity", String.valueOf(hDpi));

	    root.appendChild(jpegVariety);
	    root.appendChild(markerSequence);
	    jpegVariety.appendChild(app0JFIF);

	    metadata.mergeTree(metadataFormat, root);
	}
}

