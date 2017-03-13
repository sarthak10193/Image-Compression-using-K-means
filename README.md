# Multimedia-Design-Systems-CSCI-576
Multimedia Design Systems: Class Assigments and Project on topics like imageFiltering, image compression using kmeans [vector qunatization] 

running imageReader.java
java imageReader C:/myDir/input_vid.rgb  176 144 10    // width, height fps
java imageReader C:/myDir/input_vid.rgb  960 540 10    // width, height fps


Running imageReaderSampler.java   // image filtering using a 3*3 filter
For task 1
YourProgram.exe C:/myDir/input_vid.rgb  C:/myDir/output_vid.rgb  SD2HD 0      // SD2HD anti-alising off
YourProgram.exe C:/myDir/input_vid.rgb  C:/myDir/output_vid.rgb  SD2HD 1      // SD2HD anti-aliasing on
For task 2
YourProgram.exe C:/myDir/input_vid.rgb  C:/myDir/output_vid.rgb  HD2SD 0
YourProgram.exe C:/myDir/input_vid.rgb  C:/myDir/output_vid.rgb  HD2SD 1



# HW2 vector quantization of .raw and .rgb images using k-Means
Here .raw considers a vector of the form [pixel1 , pixel2] where pixel1 and pixel 2 are two vertically neighboring pixels.

Here .rgb considers a vector of the form [Color1, Color2] where each color is (r,g,b). Again Color 1 and color 2 are colors of two vertically adjacent pixels.

How to Run ??
> javac imageQuantizer.java
> java imageQuantizer image2.rgb N
or 
> java imageQuantizer image2.raw N

where N is the no of clusters in K means. 


