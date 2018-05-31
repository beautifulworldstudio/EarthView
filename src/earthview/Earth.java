package earthview;

import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;


public class Earth extends JPanel implements MouseListener, MouseMotionListener
 {
  private JFrame appframe;
  private BufferedImage screen;
  MapRenderer mapper;

  public Earth()
   {
    appframe = new JFrame();
    appframe.setSize(800, 600);
    appframe.setContentPane(this);

    appframe.addWindowListener(new WindowListener()
     {
      public void windowOpened(WindowEvent we){}
      public void windowClosed(WindowEvent we){}
      public void windowActivated(WindowEvent we){}
      public void windowDeactivated(WindowEvent we){}
      public void windowIconified(WindowEvent we){}
      public void windowDeiconified(WindowEvent we){}
      public void windowClosing(WindowEvent we)
       {
        System.exit(0);
       }
     });

    addMouseListener(this);
   // screen = new BufferedImage(600, 400, BufferedImage.TYPE_INT_ARGB);
    appframe.setVisible(true);

    mapper = new MapRenderer();
    mapper.initialize();

    mapper.updateTime();
    mapper.updateScreen();
    screen = mapper.getImage();
   }


  public void paint(Graphics g)
   {
/*
    for (int i = 0 ; i < screen.getWidth(); i++)
     {
      for (int j = 0; j < screen.getHeight(); j++)
       {
        screen.setRGB(i, j, 0xffffffff);
       }
     }
*/
    if (screen != null) g.drawImage(screen, 0, 0, null);
   }


  //MouseEvents
  public void mousePressed(MouseEvent e)
   {
    if (e.getButton() == MouseEvent.BUTTON1)
     {
      int x = e.getX();
      int y = e.getY();
      mapper.addDAY();
      mapper.updateTime();
      mapper.updateScreen();
     }
   }

  public void mouseClicked(MouseEvent e){}
  public void mouseReleased(MouseEvent e){}
  public void mouseEntered(MouseEvent e){}
  public void mouseExited(MouseEvent e){}
  public void mouseDragged(MouseEvent e){}
  public void mouseMoved(MouseEvent e){}

  //entry point
  public static void main(String[] args)
   {

    new Earth();
   }
 }

