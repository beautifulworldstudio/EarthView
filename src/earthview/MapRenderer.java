package earthview;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;

import javax.imageio.ImageIO;

public class MapRenderer //implements Runnable
 {
  private static final String imgfolder= "img";
  private static final String daysideimage="worldmap.jpg";
  private static final String nightsideimage = "nightmap.jpg";

  private int wholewidth; //
  private int wholeheight; //
  private int viewwidth;
  private int viewheight;
  private int Xbase; //
  private int Xoffset; //
  private int Yequator; //

  private double timevalue; //
  private double elon;//
  private double e;//
  private double asc ;//
  private double dec;//
  private double phai0 ;//
  private double dist; //
  private double parallax ;//
  private double k ;
  private double twilightk;
  private double time; //

  private BufferedImage screen;
  private BufferedImage worldmap;//
  private BufferedImage nightmap;//
  private Calendar calendar ;
  private Graphics2D g2d;

  private int counter;//
  private int interval;
  private boolean update;

  private Thread drawthread;
  private Thread th;

  public MapRenderer()
   {
   }


  public BufferedImage getImage()
   {
    return screen;
   }

  public void initialize()
   {
    try
     {
	  String path =  new File(".").getAbsoluteFile().getParent() + File.separator + imgfolder + File.separator;
      worldmap = ImageIO.read(new File(path + daysideimage));
      nightmap = ImageIO.read(new File(path + nightsideimage));
     }
    catch(IOException e){ System.out.println(e.toString());}

    Xbase = 0;
    Xoffset = 240;
    wholewidth = worldmap.getWidth();
    wholeheight = worldmap.getHeight();
    Yequator = wholeheight / 2;
    viewwidth = 480;
    viewheight = 480;

    interval = 5; //interval minutes to next caliculation timing
    screen = new BufferedImage(wholewidth, wholeheight, BufferedImage.TYPE_INT_ARGB);
    calendar = Calendar.getInstance();//

    calendar.setTimeInMillis(System.currentTimeMillis());
   }


  //@Override
  public void run()
   {
     {
	  synchronized(this)
	   {
	    while(!update)
	     {
 	      try
           {
      	    wait();
           }
        catch(InterruptedException e){ /*Log.d("WorldClock","Intrerruted "); */}
	    }
	  }

	  updateTime();
	  updateScreen();

	  update = false;
	 }
   }


  public void updateTime()
   {
    int y = calendar.get(Calendar.YEAR);
    int m = calendar.get(Calendar.MONTH);
    int d = calendar.get(Calendar.DAY_OF_MONTH);
    int h = calendar.get(Calendar.HOUR_OF_DAY);
    int min = calendar.get(Calendar.MINUTE);
    calculateSunPosition((double)y, (double)m, (double)d, (double)h, (double)min);
   }

  public void addDAY()
   {

    calendar.add(Calendar.HOUR, 1);
   }

  //retrieve the position of the sun
  public void calculateSunPosition(double year, double month, double day, double hour, double minute)
   {
    time = minute / 60.0 + hour;
    timevalue = StarPosition.getTime(year, month, day, time);
	elon = StarPosition.getSunEclipticLongitude(timevalue);//
	e = StarPosition.getInclination(timevalue);//
	asc = StarPosition.getRightAscension(elon, e);//
	dec = StarPosition.getDeclination(elon, e);//
	phai0 =  StarPosition.getSidereal(timevalue, time / 24.0, 0);//
	dist = StarPosition.getSunDistance(timevalue);
	parallax = StarPosition.getParallax(dist);//
	k = StarPosition.getSunriseAltitude(StarPosition.getSunDiameter(dist), 0.0, StarPosition.refraction, parallax);
	twilightk = StarPosition.getTwilightAltitude(0.0, parallax);
   }



  //
  public void updateScreen()
   {
	double hinode_keido, hinoiri_keido, asayake, higure;
	int hinoiriX=0, hinodeX=0;
    int asayakeX=0, higureX=0;

    //screen.eraseColor(0xff000000);
    g2d= screen.createGraphics();
    g2d.drawImage(worldmap, null, 0,0);

    for(int i = 0;i < wholeheight; i++)
     {
      //
      double latitude = getLatitudeFromY(Yequator - i);
      //
      double jikaku = StarPosition.getTimeAngle(k, dec, latitude);
      double jikaku_twi =  StarPosition.getTimeAngle(twilightk, dec, latitude);

      if(!Double.isNaN(jikaku))//
       {
        hinode_keido = StarPosition.reviseAngle(-jikaku + asc - phai0);
        hinoiri_keido = StarPosition.reviseAngle(jikaku + asc - phai0);
        hinodeX =(int)getXfromLongitude(hinode_keido);
        hinoiriX = (int)getXfromLongitude(hinoiri_keido);//

       // drawDayLightSide(hinodeX, hinoiriX, i);//

        if (!Double.isNaN(jikaku_twi))//
         {
          asayake = StarPosition.reviseAngle(-jikaku_twi + asc - phai0);
          higure = StarPosition.reviseAngle(jikaku_twi + asc - phai0);
          asayakeX = (int)getXfromLongitude(asayake);
          higureX = (int)getXfromLongitude(higure);

          drawNightSide(higureX, asayakeX, i);

          if (asayakeX < hinodeX )
           {
          	drawTwilight(latitude, asayakeX, hinodeX, i);
           }
          else
           {
      		drawTwilight(latitude, asayakeX, wholewidth -1, i);
      		drawTwilight(latitude, 0, hinodeX, i);
           }
          //
          if (hinoiriX < higureX )
           {
       		drawTwilight(latitude, hinoiriX, higureX, i);
           }
          else
           {
    		drawTwilight(latitude, hinoiriX, wholewidth -1, i);
     		drawTwilight(latitude, 0, higureX, i);
           }
         }
        else
         {
          if(hinodeX <= hinoiriX)
           {
          	drawTwilight(latitude, hinoiriX, wholewidth - 1, i);
        	drawTwilight(latitude, 0, hinodeX, i);
           }
          else
           {
        	drawTwilight(latitude, hinoiriX, hinodeX, i);
           }
         }

       }
      else //時角がNaN
       {
        if (!Double.isNaN(jikaku_twi))//
         {
          asayake = StarPosition.reviseAngle(-jikaku_twi + asc - phai0);
          higure = StarPosition.reviseAngle(jikaku_twi + asc - phai0);
          asayakeX = (int)getXfromLongitude(asayake);
          higureX = (int)getXfromLongitude(higure);

          if (asayakeX < higureX)
           {
            drawTwilight(latitude, asayakeX, higureX, i);
            drawNightSide(higureX, wholewidth -1 , i);
            drawNightSide(0, asayakeX , i);
           }
          else
           {
        	drawTwilight(latitude, asayakeX, wholewidth - 1, i);
          	drawTwilight(latitude, 0, higureX, i);
            drawNightSide(higureX, asayakeX , i);
           }
         }
        else //
         {
          double altitude = StarPosition.getSunAltitude(asc, dec, latitude, StarPosition.getSidereal(timevalue, time / 24.0, 0.0));
          drawTwilight(latitude, 0, wholewidth - 1,  i);
         }
       }
     }
   }

  //
  private void drawNightSide(int higure, int asayake, int y)
   {
    g2d.setColor(new Color(0x1d, 0x47, 0xbc, 0x50));
    if (higure <= asayake)
	 {
      for(int i = higure; i < asayake; i++)
       {
    	screen.setRGB(i, y, nightmap.getRGB(i, y));
       }
     }
    else
     {
      for(int i = higure; i < wholewidth; i++)
       {
        screen.setRGB(i, y, nightmap.getRGB(i, y));
       }
      for(int i = 0; i < asayake; i++)
       {
        screen.setRGB(i, y, nightmap.getRGB(i, y));
       }
     }
   }


  //
  private void drawDayLightSide(int hinode, int hinoiri, int y)
   {
    if (hinode <= hinoiri)
     {
      for(int i = hinode; i <= hinoiri; i++)
       {
    	//screen.setPixel(i, y, worldmap.getPixel(i, y));
    	screen.setRGB(i, y, worldmap.getRGB(i, y));
       }
     }
    else
     {
      for(int i = hinode; i < wholewidth; i++)
       {
        //screen.setPixel(i, y, worldmap.getPixel(i, y));
        screen.setRGB(i, y, worldmap.getRGB(i, y));
       }
      for(int i = 0; i <= hinoiri; i++)
       {
        //screen.setPixel(i, y, worldmap.getPixel(i, y));
        screen.setRGB(i, y, worldmap.getRGB(i, y));
       }
     }
   }


  //
  private void drawTwilight(double latitude, int startx, int endx, int y)
   {
    int addition = startx <= endx ? 1 : -1;
    double longitude = 0.0;
    g2d.setColor(new Color(0x1d, 0x47, 0xbc, 0x50));
    if(startx < 0 || startx >= wholewidth || endx < 0 || endx >= wholewidth) return;

    for(int i = startx; i != endx; i += addition)
     {
      longitude = (double)i / (double)wholewidth * 360.0;

      double phai = StarPosition.getSidereal(timevalue, time / 24.0, longitude);//?P????
      double altitude = StarPosition.getSunAltitude(asc, dec, latitude, phai);//???x

      if(!Double.isNaN(altitude))
       {
        double ratio = (8.0 + Math.floor(altitude)) / 8.0;

        if(ratio < 0.0) screen.setRGB(i, y, nightmap.getRGB(i, y));
        else if(ratio > 1.0) screen.setRGB(i, y, worldmap.getRGB(i, y));
        else screen.setRGB(i, y, composeColors(nightmap.getRGB(i, y), worldmap.getRGB(i, y),ratio));
       }
     }
   }

  //
  private void drawTwilight(int startx, int endx, int y, double h)
   {
    double ratio =  (8.0 + Math.floor(h)) / 8.0;
    int addition = startx <= endx ? 1 : -1;

    for(int i = startx; i < endx; i++)
     {
      if(ratio < 0.0) screen.setRGB(i, y, nightmap.getRGB(i, y));
      else if(ratio > 1.0) screen.setRGB(i, y, worldmap.getRGB(i, y));
      else screen.setRGB(i, y, composeColors(nightmap.getRGB(i, y), worldmap.getRGB(i, y),ratio));
     }
   }

  //
  public void setOffset(int offset)
   {
	Xoffset = offset;
    //mapsource.left = offset;
    //mapsource.right = offset + 480;
   }

  //
  private int composeColors(int color1, int color2, double ratio)
   {
    int b1 = (color1 & 0xff) , b2 = (color2 & 0xff);
    int newBlue = b1 - (int)((double)(b1 - b2) * ratio);
    if (newBlue < 0) newBlue = 0;
    else if (newBlue > 255) newBlue = 0xff;

    int g1 = (color1 &0xff00) >> 8, g2 = (color2 & 0xff00) >> 8;
    int newGreen = g1 - (int)((double)(g1 - g2) * ratio);
    if (newGreen < 0) newGreen = 0;
    else if(newGreen > 255) newGreen = 0xff;

    int r1 = (color1 &0xff0000) >> 16, r2 = (color2 & 0xff0000) >> 16;
    int newRed = r1 - (int)((double)(r1 - r2) * ratio);
    if (newRed < 0) newRed = 0;
    else if(newRed > 255) newRed = 0xff;

    return (newRed << 16) + (newGreen << 8) + newBlue  + 0xff000000;
   }

  //
  private double getXfromLongitude(double longitude)
   {
    double result = longitude;

    if (result <= -360.0) { result += Math.ceil(result / 360.0) * 360.0; }
    else if(result >= 360.0) { result -= Math.floor(result / 360.0) *360.0; }

    result = result / 360.0 * wholewidth + Xbase; //

    if( result > wholewidth) result -= wholewidth;
    else if (result < 0) result += wholewidth;

    return result;
   }

  //
  private double getLatitudeFromY(int y)
   {
    return (double)y / (double)Yequator * 90.0;
   }
 }

