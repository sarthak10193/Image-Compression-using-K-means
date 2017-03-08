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
import java.util.Timer;  
import java.util.TimerTask;

public class imageReader extends TimerTask  {

	JFrame frame;
	JLabel lbIm1;
	JLabel lbIm2;
	BufferedImage img;
    

	public void run(){

	}

	public void showIms(String[] args) {
		int width = Integer.parseInt(args[1]);
		int height = Integer.parseInt(args[2]);
		int fps = Integer.parseInt(args[3]); 

		Thread t = new Thread(); 
		t.start();
		
		frame = new JFrame(); 
		GridBagLayout gLayout = new GridBagLayout();
		frame.getContentPane().setLayout(gLayout);
		String result = String.format("Video height: %d, width: %d", height, width);
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
		
		try {
			long startTime ; 
			long URDTimeMillis; 
			long waitTime; 
			long totalTime = 0 ; 
			long targetTime = 1000/fps; 

			int frameCount = 0 ; 
			int maxFrameCount = 100; 
			double averageFPS; 

			while(true)
			{			
				File file = new File(args[0]);
				InputStream is = new FileInputStream(file);  //A FileInputStream obtains input bytes from a file in a file system , obtaining the bytes from the rgb file

				long len = file.length();
				 
				byte[] bytes = new byte[(int)len];   // bytes.length = 304128 , defining an array of bytes of length 304128

				System.out.println("Total bytes length : "+ bytes.length);
				
				/*
				public int read(byte[] b,int off, int len) 
				 return the number of bytes read into the buffer or -1 if there is no more data because the end of the stream has been reached				
				*/	
				int offset = 0;
				int numRead = 0;
				while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {       
					offset += numRead;
					
				}
				 
				int ind = 0; // keeps the painted pixel count
				
				
				for(int i=0; i<(bytes.length)/(height*width*3);i++)
				{
					startTime = System.nanoTime(); 
					img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
					
					for(int y = 0; y < height; y++)
					{    

						for(int x = 0; x < width; x++)
						{

							byte a = 0;
							byte r = bytes[ind];
							byte g = bytes[ind+height*width];
							byte b = bytes[ind+height*width*2]; 
							System.out.println(r + " " +  g + " " +  b); 
							int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
							
							img.setRGB(x,y,pix);   
							ind++;
						}
					}
					
					lbIm1.setIcon(new ImageIcon(img)); 
					c.fill = GridBagConstraints.HORIZONTAL;
					c.gridx = 0;
					c.gridy = 1;
					frame.getContentPane().add(lbIm1, c);
					
					frame.pack();
					frame.setVisible(true);	
					lbIm1.repaint(); 
						
					/*
					System.out.println("frame"+(i+1)); 
					System.out.println("R : "+ind);  // index value obv turns out to be height * width 
					System.out.println("G : " + (ind+ height*width)); 
					System.out.println("B :" + (ind + height*width*2));     //101376 + 202751  or actual 25344 + 50688	
					*/
					ind = ind + height*width*2 ; 
					//System.out.println("new start index : "+ ind);  // index value obv turns out to be height * width			

					
					URDTimeMillis = (System.nanoTime()- startTime)/1000000; 
					waitTime = targetTime - URDTimeMillis; 
					try{
						if(waitTime>0)
							Thread.sleep(waitTime);
					}catch (InterruptedException e) {

						e.printStackTrace(); 
					}

					totalTime += System.nanoTime()- startTime; 
					
					 
				}
			}

		 	
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
		 			
	}

	public static void main(String[] args) throws InterruptedException {
		imageReader ren = new imageReader();
		ren.showIms(args);
	}
   

}