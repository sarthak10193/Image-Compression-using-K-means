/*
Number of bits per frame = (640 * 480) pixels * 24 bits/pixel = 7372800 bits/frame 
Bit rate = 25 frames/sec * 7372800 bits/frame = 184,320,000 bits/sec 

Number of bytes per frame = 7372800 bits/frame / 8 bits/byte = 921,600 bytes/frame 
# of frames = 10 minutes * 60 sec/min * 25 frames/sec = 15000 frames 
Number of bytes total = 921600 bytes/frame * 15000 frames = 13,824,000,000 bytes = 13.824 GB

input : prison_176_144   w = 176 h = 144 fps = 10    w*h = 25344  25344*2 = 50688

Number of bits per frame = 176*144*24 = 608256 bits/frame
BIT rate = 10* 608256  = 6082560 bits/sec

Number of bits per frame = 176*144*24 = 608256 bits/frame = 76032 bytes/frame
BIT rate = 10* 608256  = 6082560 bits/sec


*/


//package hw1;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;
import java.lang.*; 
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.AlphaComposite;
 
import java.awt.Color;
import java.io.File;
import javax.imageio.*;
import java.util.ArrayList; 


public class imageReaderSampler  {

	JFrame frame;
	JLabel lbIm1;
	JLabel lbIm2;
	BufferedImage img;
	
	int new_height ;
	int new_width;   
	int width ;  
	int height;
	int outputFrame2D[][]; 
	 

	public void pixelConvolution(int [][] pixel2Darray, int x, int y, double [][] filter, int filter_Size, int xaxis, int yaxis){  
	   
	    int r = 0, g = 0, b = 0;
	    for(int i=0;i<filter_Size;i++){
	      for(int j=0;j<filter_Size;j++){
	      	r += (filter[i][j] * (new Color(pixel2Darray[x+i][y+j])).getRed());
			g += (filter[i][j] * (new Color(pixel2Darray[x+i][y+j])).getGreen());
			b += (filter[i][j] * (new Color(pixel2Darray[x+i][y+j])).getBlue());
		
	      }
	    }
	    r = r / 9;
        g = g / 9;
        b = b / 9;
	    int newpix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff); 
	    outputFrame2D[yaxis][xaxis] = newpix;  
	    
    }

	public BufferedImage apply33Filter(int [][] pixel2Darray, int stride){
		
		int filter_Size = 3; 	
		double[][] filter = {  {1,1,1},
							   {1,1,1}, 
							   {1,1,1}
						    } ;

		// we'll move colm wise  o = floor[(i-k)/s] + 1			

		int output_width = (int)Math.floor((pixel2Darray[1].length-filter_Size)/stride) + 1;   //320    // 958  pixel2Darray[0].length
		int output_height = (int)Math.floor((pixel2Darray.length-filter_Size)/stride) + 1;   //269    //538
 
		outputFrame2D = new int [output_height][output_width];  
	    for(int i=0;i<output_height;i++){
	      for(int j=0;j<output_width;j++){
			outputFrame2D[i][j]=0;    // initalizing the new array of 2D pixel , to be filled using convolution
	      }
	    }
	    int xaxis = 0 ; 
		int yaxis = 0 ;
		for(int i=0;i<pixel2Darray.length;i+=stride){   // 540 
      		for(int j=0;j<pixel2Darray[1].length;j+=stride){   //960
      			//System.out.println(" i :" + i+ "   j: "+ j  + " xaxis " + xaxis);
      			pixelConvolution(pixel2Darray,i,j,filter, filter_Size, xaxis, yaxis);
      			xaxis++ ; 

	      }
	      xaxis = 0 ; 
	      yaxis++; 
	    }
    	
    	// convert the output 2d back to bufferimage
    	BufferedImage filteredImage = new BufferedImage(output_width, output_height, BufferedImage.TYPE_INT_RGB);
    	for(int i = 0; i < output_height; i++){    
			for(int j = 0; j < output_width; j++){
				filteredImage.setRGB(j,i,outputFrame2D[i][j]);   // so we got the pixel value for postion x, y and according to the loop we do this for all the pixel in our HXW image
			}
		} 

		return filteredImage; 
	}


	/*
		Given the original Frame eg 960*540 This method converts the BufferedImage to a 2D matrix where each i,j represent a pixel value
	*/
	private static int[][]  convertTo2DUsingGetRGB(BufferedImage originalFrame) {
      int width =  originalFrame.getWidth();
      int height = originalFrame.getHeight();
      int[][] originalFrame2D = new int[height][width];

      for (int row = 0; row < height; row++) {
         for (int col = 0; col < width; col++) {
         	originalFrame2D[row][col] = originalFrame.getRGB(col, row);   
         }
      }

      return originalFrame2D;   // this returns the   "int pixel[][] " 
   }

	public BufferedImage resizeImage(BufferedImage originalFrame, int anti_aliasing, String conversion_type_string){

        if(anti_aliasing == 1 && conversion_type_string.equals("HD2SD")){
        	//downscaling
        	//System.out.println("Anti aliasing is ON .... " + conversion_type_string); 
			int[][] originalFrame2D = convertTo2DUsingGetRGB(originalFrame);  // getting the 2D matrix for the frame to apply filter/conv if required
			originalFrame = apply33Filter(originalFrame2D, 3);  
		}
		if(anti_aliasing == 1 && conversion_type_string.equals("SD2HD")){
        	//System.out.println("Anti aliasing is ON .... " + conversion_type_string); 
			BufferedImage resizedFrameTemp =  new BufferedImage(new_width, new_height, BufferedImage.TYPE_INT_RGB) ;     
			Graphics2D g = resizedFrameTemp.createGraphics(); 
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
			g.drawImage(originalFrame, 0 , 0 , new_width, new_height, null);    	
			int[][] originalFrame2D = convertTo2DUsingGetRGB(resizedFrameTemp);  // getting the 2D matrix for the frame to apply filter/conv if required
			originalFrame = apply33Filter(originalFrame2D,3);  
		}

		BufferedImage resizedFrame =  new BufferedImage(new_width, new_height, BufferedImage.TYPE_INT_RGB) ; 
		Graphics2D g = resizedFrame.createGraphics(); 
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		g.drawImage(originalFrame, 0 , 0 , new_width, new_height, null);	 
		g.dispose();

		return resizedFrame; 
	
	}
	
	public void showIms(String[] args) {
		
		String conversion_type_string = args[1];
		int anti_aliasing = Integer.parseInt(args[2]);
		
		if(conversion_type_string.equals("HD2SD")){ 
			new_width = 176; 
			new_height = 144;
			width = 960;
			height = 540;  

		}
		else{
			new_width = 960; 
			new_height = 540;
			width = 176;
			height = 144;

		} 		

		String outputFilePath = "converted_to_"+new_width+"X" + new_height + "Anti-aliasing - " + anti_aliasing+".rgb";  

		Thread t = new Thread(); 
		t.start();
		
		try {

			File file = new File(args[0]);
			InputStream is = new FileInputStream(file);  //A FileInputStream obtains input bytes from a file in a file system , obtaining the bytes from the rgb file

			long len = file.length();
			System.out.println("len : " + len);  
			byte[] bytes = new byte[(int)len];   // bytes.length = 304128 , defining an array of bytes of length 304128


			System.out.println("Total bytes length : "+ bytes.length);
			System.out.println("Total frames : "+ bytes.length/(height*width*3)); 

			frame = new JFrame();
			GridBagLayout gLayout = new GridBagLayout();
			frame.getContentPane().setLayout(gLayout);
			String result = String.format("Video height: %d, width: %d  , anti_aliasing %d ", new_height, new_width, anti_aliasing);
			JLabel lbText1 = new JLabel(result);
			lbText1.setHorizontalAlignment(SwingConstants.CENTER);	
			
			GridBagConstraints c = new GridBagConstraints();
			c.fill = GridBagConstraints.HORIZONTAL;
			c.anchor = GridBagConstraints.CENTER;
			c.weightx = 0.5;
			c.gridx = 0;
			c.gridy = 0;
			frame.getContentPane().add(lbText1, c);
			lbIm1 = new JLabel();	
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		
			//public int read(byte[] b,int off, int len) 
			//return the number of bytes read into the buffer or -1 if there is no more data because the end of the stream has been reached				
		
			int offset = 0;
			int numRead = 0;
			while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {       
				offset += numRead;
				
			}
			System.out.println("offset:"+offset+ " numRead :"+ numRead); 


			int ind = 0; // keeps the painted pixel count
	
			long start = System.currentTimeMillis();
			for(int i=0; i<(bytes.length)/(height*width*3);i++){

				img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
				
				for(int y = 0; y < height; y++){    

					for(int x = 0; x < width; x++){

						byte a = 0;
						byte r = bytes[ind];
						byte g = bytes[ind+height*width];
						byte b = bytes[ind+height*width*2]; 

						int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);

						img.setRGB(x,y,pix);   // so we got the pixel value for postion x, y and according to the loop we do this for all the pixel in our HXW image
						ind++;
					}
				}
			 	
			
			 	BufferedImage resizedImage = resizeImage(img, anti_aliasing, conversion_type_string); 
				/*		
	 			if(i==60 || i == 28){
					String imageFileName = "converted Frame" + Integer.toString(i) + "_" + Integer.toString(height) +"X" + Integer.toString(width) + "_ Anti-aliasing -" + Integer.toString(anti_aliasing) + ".png";   
					ImageIO.write(resizedImage, "png", new File(imageFileName));
				}
				*/

			 	
	
			 	byte[] rgbbytes = new byte[new_width*new_height*3]; 

			 	int index = 0 ; 
			 	for(int y = 0 ; y<new_height; y++)
			 	{
			 		for (int x = 0 ; x<new_width ; x++)
			 		{
			 			int color = resizedImage.getRGB(x, y);

						// Components will be in the range of 0..255:
						int red = (color & 0xff0000) >> 16;
						int green = (color & 0xff00) >> 8;
						int blue = color & 0xff;
						rgbbytes[index] = (byte)red;
						rgbbytes[index+(new_height*new_width)] = (byte)green; 
						rgbbytes[index+(new_height*new_width*2)] = (byte)blue;
						index ++; 
				    }
				}

				FileOutputStream stream = new FileOutputStream(outputFilePath, true);
				try {
				    stream.write(rgbbytes);
				} finally {
				    stream.close();
				}
			

				lbIm1.setIcon(new ImageIcon(resizedImage)); 
				c.fill = GridBagConstraints.HORIZONTAL;
				c.gridx = 0;
				c.gridy = 1;
				frame.getContentPane().add(lbIm1, c);
				
				frame.pack(); 
				frame.setVisible(true);
				lbIm1.repaint(); 
				
				ind = ind + height*width*2 ; 
					

				Thread.sleep(20); 
			}

			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e){
			e.printStackTrace(); 
		}
		 
		 
		 			
	}

	public static void main(String[] args) throws InterruptedException {
		imageReaderSampler ren = new imageReaderSampler();
		ren.showIms(args);
	}
   

}