import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class OpenBMP extends JFrame {
//two functions for autolevel: find the larger percentile
  public int littlePerc(double[] dist, int min)
  {
    for(int i = 0; i < 256; i++)
    {
      if(dist[i] > 0.01)
      {
        min = i-1;
        break;
      }
    }
    return min;
  }
//function: find the smaller percentile
  public int bigPerc(double[] dist, int max)
  {
    for(int i = 255; i > 0; i--)
    {
      if(dist[i] < 0.99)
      {
        max = i + 1;
        break;
      }
    }
    return max;
  }
    private class ExitListener implements ActionListener{
    @Override

    public void actionPerformed(ActionEvent e) {
      System.exit(0);
//        return 0;
    }
  }

  ImageIcon iconDithered;
  ImageIcon iconGrey;
  ImageIcon iconLeveled;

  ImageIcon icon;
  ImageIcon[] icons;

  int count = 1; // to iterate through the ImageIcons
  JLabel original; /*original image*/

  JLabel operated; //second window for the edited images

  int width = 704*2;
  int height = 576;
  int width_button = 300;
  int height_button = 40;
  int padding = 10;
  int width_padded = width + 2*padding;
  int height_padded = height + 2*padding;
  public OpenBMP()
  {

    super("Image Operations");

    setResizable(false);

    JButton openFile = new JButton("Open File");
    openFile.setBounds(padding,height + height_button,width_button,height_button);

    JButton exit = new JButton("Exit");
    exit.setBounds(width - width_button,height + height_button,width_button,height_button);

    JButton next = new JButton("Next");
    next.setBounds(width/2 -width_button/2,height+height_button,width_button,height_button);

    original = new JLabel();
    original.setBounds(padding,padding,width/2,height);

    operated = new JLabel();
    operated.setBounds(width/2, padding, width/2,height);

    add(openFile);
    add(exit);
    add(next);
    add(original);
    add(operated);

    openFile.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        count = 1;
        JFileChooser file = new JFileChooser();
        //highlight only BMP files in finder/explorer window
        FileNameExtensionFilter filter = new FileNameExtensionFilter("BMP Images","bmp");
        file.addChoosableFileFilter(filter);
        file.setFileFilter(filter);
        int response = file.showOpenDialog(null);
        //if a file is chosen to open, JFileChooser to File
        if(response == JFileChooser.APPROVE_OPTION)
        {
          File imageFile = file.getSelectedFile();

          BufferedImage image;
          try
          {
              image = ImageIO.read(imageFile);
          } catch (IOException ex)
          {
              throw new RuntimeException(ex);
          }

          int imgW = image.getWidth(null);
          int imgH = image.getHeight(null);

          BufferedImage greyImg = new BufferedImage(imgW, imgH,
                  BufferedImage.TYPE_BYTE_GRAY);

          Graphics g = greyImg.getGraphics();
          g.drawImage(image, 0, 0, null);
          g.dispose();

          icon = new ImageIcon(image);
          original.setIcon(icon);

          iconGrey = new ImageIcon(greyImg);
          operated.setIcon(iconGrey);

          int[][] toGrey = new int[image.getWidth(null)][image.getHeight(null)];
          int[][] remapped = new int[image.getWidth(null)][image.getHeight(null)];

//          getting two grey images, for grayscale and dithered
          for(int x = 0; x < toGrey.length; x++)
          {
            for(int y = 0; y < toGrey[x].length; y++)
            {
              int grey= greyImg.getRGB(x, y)& 0xFF;
              toGrey[x][y] = grey;
              remapped[x][y] = grey;

            }
          }

//          int[][] ditherMat = {
//                  { 0, 32, 8, 40, 2, 34, 10, 42}, /* 8x8 Bayer ordered dithering */
//                  {48, 16, 56, 24, 50, 18, 58, 26}, /* pattern. Each input pixel */
//                  {12, 44, 4, 36, 14, 46, 6, 38}, /* is scaled to the 0..63 range */
//                  {60, 28, 52, 20, 62, 30, 54, 22}, /* before looking in this table */
//                  { 3, 35, 11, 43, 1, 33, 9, 41}, /* to determine the action. */
//                  {51, 19, 59, 27, 49, 17, 57, 25},
//                  {15, 47, 7, 39, 13, 45, 5, 37},
//                  {63, 31, 55, 23, 61, 29, 53, 21} }; //8x8 test
//http://devlog-martinsh.blogspot.com/2011/03/glsl-8x8-bayer-matrix-dithering.html
//          int[][] ditherMat = {{0,2},{3,1}}; //2x2 test
          int i, j;
          int[][] ditherRes = new int[image.getWidth(null)][image.getHeight(null)];
          int[][] ditherMat = {{0,14,3,13},
                              {11,5,8,6},
                              {12,2,15,1},
                              {7,9,4,10}};
          //4x4 dither matrix courtesy of http://www.jhlabs.com/ip/filters/DitherFilter.html
          int n = ditherMat.length;

          Color white = new Color(255,255,255);
          int whiteRGB = white.getRGB();
          Color black = new Color(0,0,0);
          int blackRGB = black.getRGB();

          BufferedImage imgDithered = new BufferedImage(ditherRes.length,ditherRes[0].length, BufferedImage.TYPE_BYTE_GRAY);



//          for(int x = 0; x < toGrey.length; x++)
//          {
//            for(int y = 0; y < toGrey[x].length; y++)
//            {
//              //autolevel code
//
//              int curPixel = image.getRGB(x,y);
//
//              int red = (curPixel & 0xff0000) >> 16;
//              int green = (curPixel & 0xff00) >> 8;
//              int blue = curPixel & 0xff;
//
//              if(red > maxR)
//                maxR = red;
//              if(red < minR)
//                minR = red;
//
//              if(green > maxG)
//                maxG = green;
//              if(green < minG)
//                minG = green;
//
//              if(blue > maxB)
//                maxB = blue;
//              if(blue < minB)
//                minB = blue;
//
//              //endof autolevel code
//
//              //simply the dithering pseudocode ported to Java
//              i = x % n;
//              j = y % n;
//
//              remapped[x][y] /= n*n+1;
//              remapped[x][y] -= 3; //to make the image a little darker.
//
//              if(remapped[x][y] > ditherMat[i][j])
//              {
//                imgDithered.setRGB(x,y,whiteRGB);
//              }
//              else
//              {
//                imgDithered.setRGB(x,y,blackRGB);
//              }
//            }
//          }

          int[] valsR = new int[256];
          int[] valsG = new int[256];
          int[] valsB = new int[256];

          for(int x = 0; x < toGrey.length; x++)
          {
            for(int y = 0; y < toGrey[x].length; y++)
            {
              //autolevel code

              int curPixel = image.getRGB(x,y);

              int red = (curPixel & 0xff0000) >> 16;
              valsR[red]++;
              int green = (curPixel & 0xff00) >> 8;
              valsG[green]++;
              int blue = curPixel & 0xff;
              valsB[blue]++;

//              if(red > maxR)
//                maxR = red;
//              if(red < minR)
//                minR = red;
//
//              if(green > maxG)
//                maxG = green;
//              if(green < minG)
//                minG = green;
//
//              if(blue > maxB)
//                maxB = blue;
//              if(blue < minB)
//                minB = blue;

              //endof autolevel code

              //simply the dithering pseudocode ported to Java
              i = x % n;
              j = y % n;

              remapped[x][y] /= n*n+1;
              remapped[x][y] -= 3; //to make the image a little darker.

              if(remapped[x][y] > ditherMat[i][j])
              {
                imgDithered.setRGB(x,y,whiteRGB);
              }
              else
              {
                imgDithered.setRGB(x,y,blackRGB);
              }
            }
          }

          double[] cumSumR = new double[256];
          int sumR = 0;

          double[] cumSumG = new double[256];
          int sumG = 0;

          double[] cumSumB = new double[256];
          int sumB = 0;

          for(i = 0; i < 256; i++)
          {
            sumR += valsR[i];
            cumSumR[i] = sumR;

            sumG += valsG[i];
            cumSumG[i] = sumG;

            sumB += valsB[i];
            cumSumB[i] = sumB;
          }

          double sumRMax = cumSumR[255];

          for(i = 0; i < 256; i++)
          {
            cumSumR[i] /= sumRMax;
            cumSumG[i] /= cumSumG[255];
            cumSumB[i] /= cumSumB[255];
          }

          int maxR = 0, maxG = 0, maxB = 0;
          int minR = 0, minG = 0, minB = 0;



          minR = littlePerc(cumSumR,minR);
          minG = littlePerc(cumSumG, minG);
          minB = littlePerc(cumSumB, minB);

          System.out.println("red"+minR);
          System.out.println("green"+minG);
          System.out.println("blue"+minB);

          maxR = bigPerc(cumSumR, maxR);
          maxG = bigPerc(cumSumG, maxG);
          maxB = bigPerc(cumSumB, maxB);

          System.out.println("minr"+minR);
          System.out.println(maxR);

          int denomR = maxR - minR, denomG = maxG - minG, denomB = maxB - minB;


          int curR = 0, curG = 0, curB = 0;

          BufferedImage imgLeveled = new BufferedImage(ditherRes.length,ditherRes[0].length, BufferedImage.TYPE_INT_ARGB);
          //will fail if there is a 0 or 255 value. maybe add a bit of a 'pad' on either side to still try to increase contrast?
          for(int x = 0; x < image.getWidth(); x++)
          {
            for(int y = 0; y < image.getHeight(); y++)
            {
              int curPixel = image.getRGB(x,y);

              int red = (curPixel & 0xff0000) >> 16;
              int green = (curPixel & 0xff00) >> 8;
              int blue = curPixel & 0xff;

              if(red < minR)
                curR = 0;

              else if (red > maxR)
                curR = 255;

              else {
                curR = (int) ((red - minR) * (255.0 / denomR));
              }

              if(green < minG)
                curG = 0;

              else if (green > maxG)
                curG = 255;

              else
                curG = (int)((green - minG) * (255.0/denomG));

              if(blue < minB)
                curB = 0;

              else if (blue > maxB)
                curB = 255;

              else
                curB = (int)((blue - minB) * (255.0/denomB));

              Color curColor = new Color(curR,curG,curB);
              int curColorRGB = curColor.getRGB();

              imgLeveled.setRGB(x,y,curColorRGB);

            }
          }

          iconDithered = new ImageIcon(imgDithered);

          iconLeveled = new ImageIcon(imgLeveled);

          icons = new ImageIcon[]{iconGrey,iconDithered,iconLeveled};
          /*
          for x = 0 to xmax// columns
            for y = 0 to ymax // rows
          i = x mod n
          j = y mod n
          // I(x, y) is the input, O(x, y) is the output,
          //D is the dither matrix. if I(x, y) > D(i, j)
          O(x, y) = 1; else
          O(x, y) = 0;
          */
        }
      }
    });

    next.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e) {

        operated.setIcon(icons[count]);

        if(icons[count] ==  iconDithered)
          original.setIcon(iconGrey);

        else
          original.setIcon(icon);

        count++;

        if(count == icons.length)
        {
          count = 0;
        }
      }
    });

    //exit button
    exit.addActionListener(new ExitListener());

    setLayout(null);
    setLocationRelativeTo(null);
    setSize(width_padded,height_padded + 100); //+100 for some extra space for the button.
    setVisible(true);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    } catch (InstantiationException e) {
      throw new RuntimeException(e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    } catch (UnsupportedLookAndFeelException e) {
      throw new RuntimeException(e);
    }

  }

}
