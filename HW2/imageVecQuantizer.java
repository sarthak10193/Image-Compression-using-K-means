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
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class imageVecQuantizer {

	JFrame frame;
	JLabel lbIm1;
	JLabel lbIm2;

	private double [][] _data; // Array of all records in dataset
	private int [] _label;  // generated cluster labels
	private int [] _withLabel; // if original labels exist, load them to _withLabel
	                              // by comparing _label and _withLabel, we can compute accuracy. 
	                              // However, the accuracy function is not defined yet.
	private double [][] _centroids; // centroids: the center of clusters    
	private int _nrows, _ndims; // the number of rows and dimensions    dimension  = 2
	private int _numClusters; // the number of clusters;
	private int _height; 
	private int _width; 


	 // compute Euclidean distance between two vectors v1 and v2
	private double euclidDist(double [] v1, double [] v2){
		double sum=0;
		for (int i=0; i<_ndims; i++)
		{
		  double d = v1[i]-v2[i];
		  sum += d*d;
		}
		return Math.sqrt(sum);
	}


	// find the closest centroid for the record v 
	private int closestPoint(double [] v)
	{
		double min_distance = euclidDist(v, _centroids[0]);
		int label =0;
		for (int i=1; i<_numClusters; i++){
		  double t = euclidDist(v, _centroids[i]);
		  if (min_distance>t){
		    min_distance = t;
		    label = i;
		  }
		}
		return label;
	}

	private double [][] updateCentroids()
	{
		//System.out.println("updateCentroids"); 
    // initialize centroids and set to 0
	    double [][] newc = new double [_numClusters][]; //new centroids 
	    int [] counts = new int[_numClusters]; // sizes of the clusters

	    // intialize
	    for (int i=0; i<_numClusters; i++){
	      counts[i] =0;
	      newc[i] = new double [_ndims];
	      for (int j=0; j<_ndims; j++)
	        newc[i][j] =0;
	    }


	    for (int i=0; i<_nrows; i++){
	      int cn = _label[i]; // the cluster membership id for record i
	      for (int j=0; j<_ndims; j++){
	        newc[cn][j] += _data[i][j]; // update that centroid by adding the member data record
	      }
	      counts[cn]++;
	    }

	    // finally get the average
	    for (int i=0; i< _numClusters; i++){
	      for (int j=0; j<_ndims; j++){
	        newc[i][j]/= counts[i];
	      }
	    } 

	    return newc;
  	}

	private boolean hasConverged(double [][] c1, double [][] c2, double threshold)
	{
    
	    double maxv = 0;
	    for (int i=0; i< _numClusters; i++){
	        double d= euclidDist(c1[i], c2[i]);
	        if (maxv<d)
	            maxv = d;
	    } 

	    if (maxv <threshold)
	      return true;
	    else
	      return false;
    
  	}

    public void KMeans(String fileName, String[] args, int ndims, int channels) 
  	{
  		
  	  	try{
  			
  			_height = 288;
  			_width = 352; 	
  			// ******************* Reading the data points **********************//

  	  		FileInputStream fstream = new FileInputStream(fileName);
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

			String strLine;


			while ((strLine = br.readLine()) != null)   
				_nrows++; 			// rows of data , each data value has 2 dimension [.raw]  or 6 dim [.rgb] 
			
			System.out.println("no of rows of data ie no of data points : "+ _nrows + "    each of dimension :" + ndims); 


		   _ndims = ndims; 
	       _data = new double[_nrows][];
	       for (int i=0; i<_nrows; i++)
	          _data[i] = new double[_ndims];  


  	  		fstream = new FileInputStream(fileName); 
			br = new BufferedReader(new InputStreamReader(fstream));

	      	int nrow = 0 ; 
			while ((strLine = br.readLine()) != null)   
			{
			    	
				String[] array1 = strLine.split(",");


				double [] dv = new double[_ndims];	
				for (int k = 0 ; k<_ndims; k++)
				{
					dv[k] = Double.parseDouble(array1[k]);
		        	
				}
				
		        
		        _data[nrow] = dv;   
		        nrow++;  
	    	}

	    	
	    	br.close();


	    	// ************************Centroids********************//
	    	_numClusters = Integer.parseInt(args[1]);
            
    	    _centroids = new double[_numClusters][];   

        	ArrayList duplicate_id = new ArrayList();
	        for (int i=0; i<_numClusters; i++)
	        {
	          	int c;
	          	do{
	            	c = (int) (Math.random()*_nrows);
	          	}while(duplicate_id.contains(c)); // avoid duplicates
	          	duplicate_id.add(c);

          		// copy the value from _data[c]
	            _centroids[i] = new double[_ndims];    // meansing each of the 16 positions in the _centroids array is a an array 
	            for (int j=0; j<_ndims; j++)
	            _centroids[i][j] = _data[c][j];
        	}
        	
        	
			//***************** Running K means ******************//
			
        	// select random cluster centroids to begin with
			double [][] c1 = _centroids;
			double threshold = 0.001;
			int round=0;
			int MAX_ITER = 60 ; 

			while (true)  // running 10 iternations of k means
			{
				// update _centroids 
				_centroids = c1;

				//assign the point the label of its closest centroid
				_label = new int[_nrows];
				for (int i=0; i<_nrows; i++)
				  _label[i] = closestPoint(_data[i]);

				// updating the centroids until either we reach MAX_ITER or converge
		        c1 = updateCentroids();
		        round ++;
				if ((MAX_ITER >0 && round >=MAX_ITER) || hasConverged(_centroids, c1, threshold))
          			break;
			
			}
			
      		
			byte[] bytes = new byte[_height*_width*channels];    // the final bytes array will be of size 101376 for a .raw image and 304128 for a .rgb 3 channel image
			
			int index = 0 ;
			BufferedImage img_quant; 
			
			if(channels==1)
			{
				img_quant = new BufferedImage (_width,_height,BufferedImage.TYPE_BYTE_GRAY);  
				for (int i = 0 ; i<_nrows; i++)
        		{
        		
	        		if(i%_width==0 && i!=0){
	        			index = index+_width; 
	        				
	        			}

	        		bytes[index] = (byte)((int)_centroids[_label[i]][0] & 0xFF);
	        		bytes[index+_width] = (byte)((int)_centroids[_label[i]][1] & 0xFF); 
	        		index++;	

        		}	
			}
        	else
        	{
        		img_quant = new BufferedImage (_width,_height,BufferedImage.TYPE_INT_RGB);
        		
        		int row_num = 0 ; 
        		for (int i = 0 ; i<_height; i+=2){
        			for (int j = 0 ; j<_width; j++)
        			{
        				int red = (int)_centroids[_label[row_num]][0] & 0xFF; // r for odd rows
	        			int green = (int)_centroids[_label[row_num]][1] & 0xFF; // g for odd rows
                     	int blue = (int)_centroids[_label[row_num]][2] & 0xFF; // b for odd rows
                     	Color temp = new Color(red, green, blue);
                     	img_quant.setRGB(j, i, temp.getRGB()); 
	        			row_num++; 

        			}
        		}	
        		row_num = 0 ; 
        		for (int i = 1 ; i<_height; i+=2){
        			for (int j = 0 ; j<_width; j++)
        			{
        				int red = (int)_centroids[_label[row_num]][3] & 0xFF; // r for odd rows
	        			int green = (int)_centroids[_label[row_num]][4] & 0xFF; // g for odd rows
                     	int blue = (int)_centroids[_label[row_num]][5] & 0xFF; // b for odd rows
                     	Color temp = new Color(red, green, blue);
                     	img_quant.setRGB(j, i, temp.getRGB()); 
	        			row_num++; 

        			}
        		}


        			
        	}
 

        	//********************* creating the image **********************//
        	int ind  = 0 ; 
        	if(channels==1)
        	{
				Raster raster = img_quant.getData();
				
				for(int y = 0; y < _height; y++)
				{    

					for(int x = 0; x < _width; x++)
					{

						byte a = 0;
						byte r = bytes[ind];

						int pix = 0xff000000 | ((r & 0xff) << 16) | ((r & 0xff) << 8) | (r & 0xff);
						
						img_quant.setRGB(x,y,pix);   
						ind++;
					}
				}
			}

			

        	JFrame qframe = new JFrame(); 
			GridBagLayout gLayout = new GridBagLayout();
			qframe.getContentPane().setLayout(gLayout);
			String result = String.format("quantizeImage height: %d, width: %d", _height, _width);
			JLabel lbText1 = new JLabel(result);
			lbText1.setHorizontalAlignment(SwingConstants.CENTER);	
			
			GridBagConstraints c = new GridBagConstraints();
			c.fill = GridBagConstraints.HORIZONTAL;
			c.anchor = GridBagConstraints.CENTER;
			c.weightx = 0.5;
			c.gridx = 0;
			c.gridy = 0;
			qframe.getContentPane().add(lbText1, c);
			lbIm1 = new JLabel();	
			qframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


			lbIm1.setIcon(new ImageIcon(img_quant)); 
			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridx = 0;
			c.gridy = 1;
			qframe.getContentPane().add(lbIm1, c);
			
			qframe.pack();
			qframe.setVisible(true);	
			lbIm1.repaint(); 




  	  	}catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
		} 
		
  	}

  	public void quantizeImage_rgb(String[] args)
  	{

  		try {
			
			int height = 288;
			int width = 352; 

			File file = new File(args[0]);
			InputStream is = new FileInputStream(file);  //A FileInputStream obtains input bytes from a file in a file system , obtaining the bytes from the rgb file

			long len = file.length();
			byte[] bytes = new byte[(int)len];   
			System.out.println("The image is of this many bytes:"+ bytes.length); 

			int offset = 0;
			int numRead = 0;
			while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {       
				offset += numRead;
				
			}	

			BufferedImage img = new BufferedImage (width,height,BufferedImage.TYPE_INT_RGB);	
			
			int ind = 0 ;
			int[] oddlines = new int[144*352*3]; 
			int[] evenlines = new int[144*352*3]; 
			int index_odd = 0; 
			int index_even = 0 ;  

			for(int y = 0; y < height; y++)
			{    

				for(int x = 0; x < width; x++)
				{

					byte a = 0;
					byte r = bytes[ind];
					byte g = bytes[ind+height*width];
					byte b = bytes[ind+height*width*2]; 
					
					int r_int = r&0xFF; 
					int g_int = g&0xFF; 
					int b_int = b&0xFF; 
					
					int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);

					if(y%2==0){
						evenlines[index_even++] = r_int;
						evenlines[index_even++] = g_int;
						evenlines[index_even++] = b_int;
					}
					else{
						oddlines[index_odd++] = r_int;
						oddlines[index_odd++] = g_int;
						oddlines[index_odd++] = b_int;
					}	
					
					img.setRGB(x,y,pix);   
					ind++;
				}
			}

			BufferedWriter writer = new BufferedWriter(new FileWriter("output_rgb.txt"));
            
			for (int i = 0 ; i< 144*352 *3; i+=3){
					writer.write(oddlines[i] +","+oddlines[i+1]+ ","+ oddlines[i+2]+ "," + evenlines[i] + "," + evenlines[i+1] + "," + evenlines[i+2] +   "\n");
			}
			
			writer.close();	

			frame = new JFrame(); 
			GridBagLayout gLayout = new GridBagLayout();
			frame.getContentPane().setLayout(gLayout);
			String result = String.format("original height: %d, width: %d", height, width);
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


			lbIm1.setIcon(new ImageIcon(img)); 
			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridx = 0;
			c.gridy = 1;
			frame.getContentPane().add(lbIm1, c);
			
			frame.pack();
			frame.setVisible(true);	
			lbIm1.repaint(); 
		

		 	
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
  	}

	public void quantizeImage_raw(String[] args) {
		
		
		try {
			
			_height = 288;
			_width = 352; 

			File file = new File(args[0]);
			InputStream is = new FileInputStream(file);  //A FileInputStream obtains input bytes from a file in a file system , obtaining the bytes from the rgb file

			long len = file.length();
			byte[] bytes = new byte[(int)len];   // for a raw image of size 352 X 288 total pixels = 101376.  since its a raw black and white image ***** 1 pixel = 8 bits  = 1 byte *****  The same rgb color image will be 101376*3 bytes 304128
			//System.out.println("Total bytes length : "+ bytes.length);
			
			/*
			public int read(byte[] b,int off, int len) 
			 return the number of bytes read into the buffer or -1 if there is no more data because the end of the stream has been reached				
			*/	
			int offset = 0;
			int numRead = 0;
			while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {       
				offset += numRead;
				
			}	

			int[] oddlines = new int[144*352]; 
			int[] evenlines = new int[144*352]; 
			int index_odd = 0; 
			int index_even = 0 ; 

			BufferedImage img = new BufferedImage (_width,_height,BufferedImage.TYPE_BYTE_GRAY);	
			Raster raster = img.getData();
			int ind = 0 ; 
			for(int y = 0; y < _height; y++)
			{    

				for(int x = 0; x < _width; x++)
				{

					byte a = 0;
					byte r = bytes[ind];
				
					if(y%2==0)
						evenlines[index_even++] = r&0xFF; 
					else
						oddlines[index_odd++] = r&0xFF; 

					int pix = 0xff000000 | ((r & 0xff) << 16) | ((r & 0xff) << 8) | (r & 0xff);
					//System.out.println(r&0xFF);    // baically converting to unsigned int  earlier range -127 to +127 , new range 0 255

					img.setRGB(x,y,pix);   
					ind++;
				}
			}
			
			BufferedWriter writer = new BufferedWriter(new FileWriter("output_raw.txt"));
            
			for (int i = 0 ; i< 144*352 ; i++){
					writer.write(oddlines[i] + "," + evenlines[i] + "\n");
			}
			writer.close();

			// ************** creating a frame for image display **************** //



			frame = new JFrame(); 
			GridBagLayout gLayout = new GridBagLayout();
			frame.getContentPane().setLayout(gLayout);
			String result = String.format("original height: %d, width: %d", _height, _width);
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


			lbIm1.setIcon(new ImageIcon(img)); 
			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridx = 0;
			c.gridy = 1;
			frame.getContentPane().add(lbIm1, c);
			
			frame.pack();
			frame.setVisible(true);	
			lbIm1.repaint(); 


			
				

		 	
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
		 			
	}

	public static void main(String[] args)  {
		imageVecQuantizer ren = new imageVecQuantizer();
		

		String line = args[0]; 
      	String pattern = "(.)(rgb)";

      	// Create a Pattern object
      	Pattern r = Pattern.compile(pattern);

      // Now create matcher object.
      	Matcher m = r.matcher(line);
      	if(m.find()){
      		System.out.println("quantizing .rgb image\n For a .rgb image 2 vertically Adject pixel colors are considered as a Vector ie we have a vector of the form [C1, C2] where each Ci is of the from r.g.b \n"); 
			ren.quantizeImage_rgb(args); 
			ren.KMeans("output_rgb.txt", args, 6, 3);  // 3-D points data path , no of clusters, and dimensions
		}
      	else{

      		System.out.println("quantizing .raw image\n"); 
			ren.quantizeImage_raw(args);
			ren.KMeans("output_raw.txt", args, 2, 1);  // 2-D points data path , no of cluseters and dimension
			
      	}
		System.out.println("\n\nDONE"); 
		
	}
   

}
