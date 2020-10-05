package OSproject;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;

import java.util.Calendar;
import java.lang.Float;

import java.awt.Font;
import java.awt.geom.Rectangle2D;

public class RWsProblem {

	public static int data_size = 10;	
	public static int red_size = -1;
	public static int blue_size = -1;			
	public static int room_seed = 0;
	public static int room_size = 10;
	public static int ball_speed = 1;
	
	
	public static int rfinishCount = 0;
	public static int wfinishCount = 0;
	public static int data1[] = new int[data_size];
	public static int data2[] = new int[data_size];
	public static int rwaitCount = 0;
	public static int wwaitCount = 0;
	public static int readerCount = 0;
	public static boolean canRead = true;
	public static boolean canWrite = true;
	public static boolean startCopy = false;
	public static int rGenerateCount = 0;
	public static int wGenerateCount = 0;
		
	public static Object canReadMutex = new Object();
	public static Object canWriteMutex = new Object();	
	public static Object rcMutex = new Object();
	public static Object copyMutex = new Object();
	public static Object moveMutex = new Object();
	public static Object rwaitMutex = new Object();
	public static Object wwaitMutex = new Object();	
	public static Object rFinishMutex = new Object();
	public static Object wFinishMutex = new Object();
	public static Object generateMutex = new Object();
	
	
	public static Random ran = new Random(10);
	public static int[] pos = {50,100,150,200,250,300,350,400,450,500};
	
	public JFrame frame;
    public static void main(String[] argv) {
    	for (int i = 0; i < data_size; i ++) {
    		data1[i] = ran.nextInt(100);
    		data2[i] = data1[i];
    	}
    	RWsProblem p = new RWsProblem();
        SwingUtilities.invokeLater( () -> new RWsProblem().startup(p) );
    }
   
    JTextField playSpeed;
    JTextField roomSeed;
    JTextField roomSize;
    JTextField readerLimit;
    JTextField writerLimit;
    static JButton playbtn;
    
    public void startup(RWsProblem p) { 	
        frame = new JFrame("Read & Write Problem");			// create a window
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);		// default close window method
        Harbor h = new Harbor();
        frame.setContentPane(h);                         
        frame.setResizable(false);									// set window size fixed
        frame.setSize(1105, 645);									// set window size
        frame.setVisible(true);										// set window visible
        
        playSpeed = new JTextField();						// input for playSpeed
        playSpeed.setFont(new Font("TimesRoman", Font.PLAIN, 25));
        playSpeed.setBounds(950, 25,125, 30);
        frame.add(playSpeed);
        
        roomSize = new JTextField();						// input for roomSize
        roomSize.setFont(new Font("TimesRoman", Font.PLAIN, 25));
        roomSize.setBounds(950, 55,125, 30);
        frame.add(roomSize);
        
        roomSeed = new JTextField();						// input for roomSeed
        roomSeed.setFont(new Font("TimesRoman", Font.PLAIN, 25));
        roomSeed.setBounds(950, 85,125, 30);
        frame.add(roomSeed);
        
        readerLimit = new JTextField();						// input for readerLimit
        readerLimit.setFont(new Font("TimesRoman", Font.PLAIN, 25));
        readerLimit.setBounds(950, 115,125, 30);
        frame.add(readerLimit);
        
        writerLimit = new JTextField();						// input for writerLimit
        writerLimit.setFont(new Font("TimesRoman", Font.PLAIN, 25));
        writerLimit.setBounds(950, 145,125, 30);
        frame.add(writerLimit);
        
        Font font = new Font("TimesRoman",Font.PLAIN,25);
        playbtn = new JButton("Start");							// play buttom       
        playbtn.setBounds(960, 200, 100, 50);
        frame.add(playbtn,"South");
        

        
         playbtn.addActionListener(new StartHandler(p,h,playbtn));
         playbtn.setFont(font);
                
    }
 
    public static class Harbor extends JComponent {
        public List<BallWithOwnThread> balls = new ArrayList<>();
        public DrawRoom r1 = new DrawRoom(this);
        public DrawData d1 = new DrawData(this);
        public DrawDoor dr1 = new DrawDoor(this);
        
        public Harbor() {
            //balls.add(new BallWithOwnThread(Color.RED, 175, 590, 12, 101, this ,balls,p) );
        }
        
 
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);                                    // handle opaqueness
            Graphics2D g2 = (Graphics2D)g;
            balls.forEach( ball -> ball.paint(g2) );
            r1.paint(g2);
            d1.paint(g2);
            dr1.paint(g2);
        }
    } // end static class Harbor
 
    public static class DrawDoor implements Runnable {
    	private JComponent parent;
    	int offset = 0;
    	public DrawDoor(JComponent parent) {
    		this.parent = parent;
    	}
    	
    	public void paint(Graphics2D g) {
  
    		if(!canWrite) {
    			try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }if(offset < 100) {
                	offset ++;
                }
    		}
    		else{
    			offset = 0;
    		}
  		      Rectangle2D topEnter = new Rectangle2D.Double(25+offset, 150, 100 ,5);
		      g.setPaint(Color.BLACK);
		      g.draw(topEnter);
    	}
    	
    	public void run() {
    	}
    	
    }

    public static class DrawData implements Runnable {
    	private JComponent parent;
    	public static int y = 0;
    	public static int sizeOffset = -5;
    	public DrawData(JComponent parent) {
    		this.parent = parent;
    	}
    	public void paint(Graphics2D g) {
	    		if(startCopy) {
	    			try {
	                    Thread.sleep(10);
	                } catch (InterruptedException e) {
	                    e.printStackTrace();
	                    return;
	                }
	    			if(y < 155) {
	        		  y += 1 *ball_speed;
	    			}else {
	    				 synchronized(moveMutex) {
	     						moveMutex.notifyAll(); 
	            	   }  
	    			}
	    		  g.setPaint(Color.BLUE);
	  	   		  g.drawString(""+(data1[0] == -1 ? "" : data1[0]),35,235+y);
	  	   		  g.drawString(""+(data1[1] == -1 ? "" : data1[1]),85,235+y);
	  	   		  g.drawString(""+(data1[2] == -1 ? "" : data1[2]),135,235+y);
	  	   		  g.drawString(""+(data1[3] == -1 ? "" : data1[3]),185,235+y);
	  	   		  g.drawString(""+(data1[4] == -1 ? "" : data1[4]),235,235+y);
	  	   		  g.drawString(""+(data1[5] == -1 ? "" : data1[5]),285,235+y);
	  	   		  g.drawString(""+(data1[6] == -1 ? "" : data1[6]),335,235+y);
	  	   		  g.drawString(""+(data1[7] == -1 ? "" : data1[7]),385,235+y);
	  	   		  g.drawString(""+(data1[8] == -1 ? "" : data1[8]),435,235+y);
	  	   		  g.drawString(""+(data1[9] == -1 ? "" : data1[9]),485,235+y);
	  	   		  
	  	   		    		  
	  	   		  
	  		   	  Rectangle2D array1_Element1 = new Rectangle2D.Double(25, 200+y, 50 ,50+sizeOffset);
	  	  		  g.setPaint(Color.BLACK);
	  	  		  g.draw(array1_Element1);	 
	  	  		  
	  	  		  Rectangle2D array1_Element2 = new Rectangle2D.Double(75, 200+y, 50 ,50+sizeOffset);
	  	  		  g.setPaint(Color.BLACK);
	  	  		  g.draw(array1_Element2);
	  	  		  
	  	  		  Rectangle2D array1_Element3 = new Rectangle2D.Double(125, 200+y, 50 ,50+sizeOffset);
	  	  		  g.setPaint(Color.BLACK);
	  	  		  g.draw(array1_Element3);
	  	  		  
	  	  		  
	  	  		  
	  	  		  Rectangle2D array1_Element4 = new Rectangle2D.Double(175, 200+y, 50 ,50+sizeOffset);
	  	  		  g.setPaint(Color.BLACK);
	  	  		  g.draw(array1_Element4);
	  	  		  ;
	  	  		  
	  	  		  Rectangle2D array1_Element5 = new Rectangle2D.Double(225, 200+y, 50 ,50+sizeOffset);
	  	  		  g.setPaint(Color.BLACK);
	  	  		  g.draw(array1_Element5);
	  	  		  
	  	  		  
	  	  		  
	  	  		  Rectangle2D array1_Element6 = new Rectangle2D.Double(275, 200+y, 50 ,50+sizeOffset);
	  	  		  g.setPaint(Color.BLACK);
	  	  		  g.draw(array1_Element6);
	  	  		  
	  	  		  
	  	  		  
	  	  		  Rectangle2D array1_Element7 = new Rectangle2D.Double(325, 200+y, 50 ,50+sizeOffset);
	  	  		  g.setPaint(Color.BLACK);
	  	  		  g.draw(array1_Element7);
	  	  		  
	  	  		  
	  	  		  
	  	  		  Rectangle2D array1_Element8 = new Rectangle2D.Double(375, 200+y, 50 ,50+sizeOffset);
	  	  		  g.setPaint(Color.BLACK);
	  	  		  g.draw(array1_Element8);
	  	  		  
	  	  		  
	  	  		  Rectangle2D array1_Element9 = new Rectangle2D.Double(425, 200+y, 50 ,50+sizeOffset);
	  	  		  g.setPaint(Color.BLACK);
	  	  		  g.draw(array1_Element9);
	  	  		  
	  	  		  
	  	  		  
	  	  		  Rectangle2D array1_Element10 = new Rectangle2D.Double(475, 200+y, 50 ,50+sizeOffset);
	  	  		  g.setPaint(Color.BLACK);
	  	  		  g.draw(array1_Element10);
	  	   		    
	  	  		  parent.repaint();	    			
	    		}else {
	    			 y = 0;
	    			 parent.repaint();	   
	    		}  		
    	}
    	public void run() {
    	}
    }
   
    public static class DrawRoom implements Runnable {
    	private JComponent parent;
    	public DrawRoom(JComponent parent) {
    		this.parent = parent;
    	}
  	
    	public void paint(Graphics2D g) {
    		  g.setPaint(Color.BLUE);
    		  g.setFont(new Font("TimesRoman", Font.PLAIN, 25)); 
	   		  g.drawString("Waiting Writers:"+rwaitCount, 575, 530);
	   		  g.drawString("Generated Writers:"+wGenerateCount, 575, 590);
    		
   	   		  g.setPaint(Color.RED);
	   		  g.drawString("Waiting Readers: "+wwaitCount, 575, 500);
	   		  g.drawString("Generated Readers: "+rGenerateCount, 575, 560);
	   		  
	   		  g.setPaint(Color.BLACK);
	   		  g.drawString("                       Control Settings", 575, 20);
	   		  g.drawString("                       Control Settings", 576, 21);
	   		  g.drawString("> Play Speed(1x ~ 5x):", 575, 50);
	   		  g.drawString("> Room Size(1~10): ", 575, 80);
	   		  g.drawString("> Room Data Seed: ", 575, 110);
	   		  g.drawString("> Reader Limit(>=1):", 575, 140);
	   		  g.drawString("> Writer Limit(>=1):", 575, 170);
	   		  g.drawString("             Current Settings Information", 575, 290);
	   		  g.drawString("             Current Settings Information", 576, 291);
	   		  g.drawString("Play Speed for this Cycle: "+ball_speed, 575, 320);
	   		  g.drawString("Room Size for this Cycle: "+room_size, 575, 350);
	   		  g.drawString("Room Seed for this Cycle: "+room_seed, 575, 380);
	   		  g.drawString("Target Number of Reader:  "+ (red_size == -1 ? "inf" : red_size), 575, 410);
	   		  g.drawString("Target Number of Writer:  "+ (blue_size == -1 ? "inf" : blue_size), 575, 440);
	   		  g.drawString("                Current State Information", 575, 470);
	   		  g.drawString("                Current State Information", 576, 471);
	   		  
	   		  // divide line
	   		  g.drawLine(549, 0, 549, 605);
	   		  g.drawLine(551, 0, 551, 605);
	   		  
	   		  // Control Settings line
	   		  g.drawLine(551, 10, 725, 10);
	   		  g.drawLine(925, 10, 1100, 10);
	   		  
	   		  // Current Settings Information line
	   		  g.drawLine(551, 280, 660, 280);
	   		  g.drawLine(990, 280, 1100, 280);
	   		  
	   		  // Current State Information line
	   		  g.drawLine(551, 460, 675, 460);
	   		  g.drawLine(975, 460, 1100, 460);
	   		  
    		  //�ｿｽ�ｿｽ�ｿｽ�ｿｽ�ｿｽ�ｿｽ�ｿｽ(reader)�ｿｽ[�ｿｽ�ｿｽ
    		  Rectangle2D topRectM = new Rectangle2D.Double(25, 250,500,5);
    	      g.setPaint(Color.BLACK);
    	      g.fill(topRectM);
    		  g.draw(topRectM);
    		  
    		  g.setFont(new Font("TimesRoman", Font.PLAIN, 25));
              g.setColor(Color.BLUE);

    		  
    		  Rectangle2D topRectR = new Rectangle2D.Double(25, 150 ,5,100);
    	      g.setPaint(Color.BLACK);
    	      g.fill(topRectR);
    		  g.draw(topRectR);

    		  
    		  Rectangle2D topRectL = new Rectangle2D.Double(520, 150,5,100);
    	      g.setPaint(Color.BLACK);
    	      g.fill(topRectL);
    		  g.draw(topRectL);
    		  
    		 
    		  Rectangle2D topRectML = new Rectangle2D.Double(25, 150, 100 ,5);
    	      g.setPaint(Color.BLACK);
    	      g.fill(topRectML);
    		  g.draw(topRectML);
    		  
    		  
    		  Rectangle2D topRectMM = new Rectangle2D.Double(225, 150, 100 ,5);
    	      g.setPaint(Color.BLACK);
    	      g.fill(topRectMM);
    		  g.draw(topRectMM);
    		  
    		  
    		  Rectangle2D topRectMR = new Rectangle2D.Double(425, 150, 100 ,5);
    	      g.setPaint(Color.BLACK);
    	      g.fill(topRectMR);
    		  g.draw(topRectMR);
    		 
    		  
    		  //if(!canWrite) {
    		   // Rectangle2D topEnter = new Rectangle2D.Double(125, 150, 100 ,5);
    		  //  g.setPaint(Color.BLACK);
    		   // g.draw(topEnter);
    		 // }
    		  
    		  
    		  //�ｿｽ�ｿｽ�ｿｽ�ｿｽ�ｿｽ�ｿｽ�ｿｽ(reader)�ｿｽ[�ｿｽﾔ趣ｿｽ�ｿｽ�ｿｽ
    		  g.setPaint(Color.BLUE);  		  
  	   		  g.drawString(""+(data1[0] == -1 ? "" : data1[0]),35,235);
  	   		  g.drawString(""+(data1[1] == -1 ? "" : data1[1]),85,235);
  	   		  g.drawString(""+(data1[2] == -1 ? "" : data1[2]),135,235);
  	   		  g.drawString(""+(data1[3] == -1 ? "" : data1[3]),185,235);
  	   		  g.drawString(""+(data1[4] == -1 ? "" : data1[4]),235,235);
  	   		  g.drawString(""+(data1[5] == -1 ? "" : data1[5]),285,235);
  	   		  g.drawString(""+(data1[6] == -1 ? "" : data1[6]),335,235);
  	   		  g.drawString(""+(data1[7] == -1 ? "" : data1[7]),385,235);
  	   		  g.drawString(""+(data1[8] == -1 ? "" : data1[8]),435,235);
  	   		  g.drawString(""+(data1[9] == -1 ? "" : data1[9]),485,235);
    		  
    		  
    		  //�ｿｽ�ｿｽ�ｿｽ�ｿｽ�ｿｽ�ｿｽ�ｿｽ(reader)�ｿｽ[�ｿｽﾔ趣ｿｽ�ｿｽ�ｿｽ�ｿｽI�ｿｽi�ｿｽq
    		  Rectangle2D array1_Element1 = new Rectangle2D.Double(25, 200, 50 ,50);
    		  g.setPaint(Color.BLACK);
    		  g.draw(array1_Element1);
    		  
    		
    		  Rectangle2D array1_Element2 = new Rectangle2D.Double(75, 200, 50 ,50);
    		  g.setPaint(Color.BLACK);
    		  g.draw(array1_Element2);
    		  
    		  Rectangle2D array1_Element3 = new Rectangle2D.Double(125, 200, 50 ,50);
    		  g.setPaint(Color.BLACK);
    		  g.draw(array1_Element3);
    		  
    		  
    		  
    		  Rectangle2D array1_Element4 = new Rectangle2D.Double(175, 200, 50 ,50);
    		  g.setPaint(Color.BLACK);
    		  g.draw(array1_Element4);
    		  ;
    		  
    		  Rectangle2D array1_Element5 = new Rectangle2D.Double(225, 200, 50 ,50);
    		  g.setPaint(Color.BLACK);
    		  g.draw(array1_Element5);
    		  
    		  
    		  
    		  Rectangle2D array1_Element6 = new Rectangle2D.Double(275, 200, 50 ,50);
    		  g.setPaint(Color.BLACK);
    		  g.draw(array1_Element6);
    		  
    		  
    		  
    		  Rectangle2D array1_Element7 = new Rectangle2D.Double(325, 200, 50 ,50);
    		  g.setPaint(Color.BLACK);
    		  g.draw(array1_Element7);
    		  
    		  
    		  
    		  Rectangle2D array1_Element8 = new Rectangle2D.Double(375, 200, 50 ,50);
    		  g.setPaint(Color.BLACK);
    		  g.draw(array1_Element8);
    		  
    		  
    		  Rectangle2D array1_Element9 = new Rectangle2D.Double(425, 200, 50 ,50);
    		  g.setPaint(Color.BLACK);
    		  g.draw(array1_Element9);
    		  
    		  
    		  
    		  Rectangle2D array1_Element10 = new Rectangle2D.Double(475, 200, 50 ,50);
    		  g.setPaint(Color.BLACK);
    		  g.draw(array1_Element10);
    		  


    		  
    		  
    		  
    		  
    		  //�ｿｽ�ｿｽ�ｿｽ�ｿｽ�ｿｽ�ｿｽ�ｿｽ�ｿｽ(writer)�ｿｽ[�ｿｽ�ｿｽ
    		  Rectangle2D bottomRectM = new Rectangle2D.Double(25, 350,500,5);
    	      g.setPaint(Color.BLACK);
    	      g.fill(bottomRectM);
    		  g.draw(bottomRectM);
    		  
    		  Rectangle2D bottomRectR = new Rectangle2D.Double(25, 350 ,5,100);
    	      g.setPaint(Color.BLACK);
    	      g.fill(bottomRectR);
    		  g.draw(bottomRectR);
    		  
    		  Rectangle2D bottomRectL = new Rectangle2D.Double(520, 350,5,100);
    	      g.setPaint(Color.BLACK);
    	      g.fill(bottomRectL);
    		  g.draw(bottomRectL);
    		  
    		  
    		  Rectangle2D bottomRectML = new Rectangle2D.Double(25, 450, 100 ,5);
    	      g.setPaint(Color.BLACK);
    	      g.fill(bottomRectML);
    		  g.draw(bottomRectML);
    		  
    		  Rectangle2D bottomRectMM = new Rectangle2D.Double(225, 450, 100 ,5);
    	      g.setPaint(Color.BLACK);
    	      g.fill(bottomRectMM);
    		  g.draw(bottomRectMM);
    		  
    		  Rectangle2D bottomRectMR = new Rectangle2D.Double(425, 450, 100 ,5);
    	      g.setPaint(Color.BLACK);
    	      g.fill(bottomRectMR);
    		  g.draw(bottomRectMR);
    		  
    		  
    		  if(!canRead) {
    		    Rectangle2D bottomEnter = new Rectangle2D.Double(125, 450, 100 ,5);
    		    g.setPaint(Color.BLACK);
    		    g.draw(bottomEnter);
    		  }
    		  
    		  //�ｿｽ�ｿｽ�ｿｽ�ｿｽ�ｿｽ�ｿｽ�ｿｽ�ｿｽ(writer)�ｿｽ[�ｿｽﾔ趣ｿｽ�ｿｽ�ｿｽ
    		  g.setPaint(Color.RED); 		  
  	   		  g.drawString(""+(data2[0] == -1 ? "" : data2[0]),35,390);
  	   		  g.drawString(""+(data2[1] == -1 ? "" : data2[1]),85,390);
  	   		  g.drawString(""+(data2[2] == -1 ? "" : data2[2]),135,390);
  	   		  g.drawString(""+(data2[3] == -1 ? "" : data2[3]),185,390);
  	   		  g.drawString(""+(data2[4] == -1 ? "" : data2[4]),235,390);
  	   		  g.drawString(""+(data2[5] == -1 ? "" : data2[5]),285,390);
  	   		  g.drawString(""+(data2[6] == -1 ? "" : data2[6]),335,390);
  	   		  g.drawString(""+(data2[7] == -1 ? "" : data2[7]),385,390);
  	   		  g.drawString(""+(data2[8] == -1 ? "" : data2[8]),435,390);
  	   		  g.drawString(""+(data2[9] == -1 ? "" : data2[9]),485,390);
    		  
    		  
    		  

 
    		  Rectangle2D array2_Element1 = new Rectangle2D.Double(25, 350, 50 ,50);
    		  g.setPaint(Color.BLACK);
    		  g.draw(array2_Element1);
    		  Rectangle2D array2_Element2 = new Rectangle2D.Double(75, 350, 50 ,50);
    		  g.setPaint(Color.BLACK);
    		  g.draw(array2_Element2);
    		  Rectangle2D array2_Element3 = new Rectangle2D.Double(125, 350, 50 ,50);
    		  g.setPaint(Color.BLACK);
    		  g.draw(array2_Element3);
    		  Rectangle2D array2_Element4 = new Rectangle2D.Double(175, 350, 50 ,50);
    		  g.setPaint(Color.BLACK);
    		  g.draw(array2_Element4);
    		  Rectangle2D array2_Element5 = new Rectangle2D.Double(225, 350, 50 ,50);
    		  g.setPaint(Color.BLACK);
    		  g.draw(array2_Element5);
    		  Rectangle2D array2_Element6 = new Rectangle2D.Double(275, 350, 50 ,50);
    		  g.setPaint(Color.BLACK);
    		  g.draw(array2_Element6);
    		  Rectangle2D array2_Element7 = new Rectangle2D.Double(325, 350, 50 ,50);
    		  g.setPaint(Color.BLACK);
    		  g.draw(array2_Element7);
    		  Rectangle2D array2_Element8 = new Rectangle2D.Double(375, 350, 50 ,50);
    		  g.setPaint(Color.BLACK);
    		  g.draw(array2_Element8);
    		  Rectangle2D array2_Element9 = new Rectangle2D.Double(425, 350, 50 ,50);
    		  g.setPaint(Color.BLACK);
    		  g.draw(array2_Element9);
    		  Rectangle2D array2_Element10 = new Rectangle2D.Double(475, 350, 50 ,50);
    		  g.setPaint(Color.BLACK);
    		  g.draw(array2_Element10);
    		  
    		  parent.repaint();	  
    	}
    	@Override
    	public void run() {
    		while(true) {
    			parent.repaint();	
    		}
    	}
    }

    public static class BallWithOwnThread implements Runnable {
        private Color color;
        private int x, y, radius, milliseconds;
        private boolean blink, visible=true;
        private JComponent parent;
        int mode = 0;
        
        int t = 0;
        String s = "";
        
        public BallWithOwnThread(Color color, int x, int y, int radius, int milliseconds, JComponent parent,List<BallWithOwnThread> balls , RWsProblem p) {       	
        	this.color = color;
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.milliseconds = milliseconds;
            this.parent = parent;

            Timer timer = new Timer();
	            timer.schedule(p.new DateTasks(14, 101,parent,balls,p), (long)(-1/(1.0/1000.0)*Math.log(Math.random())));
	            new Thread(this).start();
        }
 
        public void paint(Graphics2D g) {
            // Draw the ball. Don't change state.
            if (visible) {
                g.setColor(color);
                g.fillOval(x-radius, y-radius, 2*radius, 2*radius);
                g.setFont(new Font("TimesRoman", Font.PLAIN, 18));
                g.setColor(Color.WHITE);
                g.drawString(s,x-radius + 4.5f,y-radius + 21);
            }
        }
 
        @Override
        public void run() {

                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }
 
                int xOld = x, yOld = y;
              
                
                if(color == Color.BLUE) {
                	 int value = ran.nextInt(100);
                	 s = ""+ value;
                	// y = 130 �ｿｽ�ｿｽO
                	// y = 175 �ｿｽ�ｿｽ�ｿｽ
                	// x = 50 100 150 200 250 300 350 400 450 500 (index)
                	// x = 375 exit  position
                	// x = 125 enter position
                	
                	
                	while(true) {
                		try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            return;
                        }
                		if(y <= 130) {
                			y += 1*ball_speed;
                       	    parent.repaint(x-radius, y-radius, 2*radius, 2*radius);         
                            parent.repaint(xOld-radius, yOld-radius, 2*radius, 2*radius);
                		}else {
                			break;
                		}
                	}
                	
                	
              	      synchronized(rwaitMutex) {	
              	    	 rwaitCount++;
              	      }
        	
            		while(true) {
           			 synchronized(canWriteMutex) {	
           				 if(canWrite == true) {
           					 canWrite = false;
           					 System.out.println("Enter write: [" + "]");
           					 break;
           				 }else {
           					 try { 
           						    canWriteMutex.wait(); 
           				         } 
           				         catch(InterruptedException e) { 
           				             e.printStackTrace(); 
           				         }
           				 }
           			 }	 		 
           		  }
                
           		
            	      synchronized(rwaitMutex) {	
            	    	 rwaitCount--;
            	      }
            		
            		
            		
            		while(true) {
                		try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            return;
                        }
                		if(y <= 175) {
                			y += 1*ball_speed;
                       	    parent.repaint(x-radius, y-radius, 2*radius, 2*radius);         
                            parent.repaint(xOld-radius, yOld-radius, 2*radius, 2*radius);
                		}else {
                			break;
                		}
                	}
            		
            		
            	   int index = ran.nextInt(data_size);
            	  
            		
                	
            	   
            	   if(index <= 2) {
                   	while(true) {
                   		try {
                               Thread.sleep(10);
                           } catch (InterruptedException e) {
                               e.printStackTrace();
                               return;
                           }
                   		if(x > pos[index]) {
                   			 x -= 1*ball_speed;
                          	    parent.repaint(x-radius, y-radius, 2*radius, 2*radius);         
                               parent.repaint(xOld-radius, yOld-radius, 2*radius, 2*radius);
                   		}else {
                   			break;
                   		}
                   	}
               	}else {
               		while(true) {
                   		try {
                               Thread.sleep(10);
                           } catch (InterruptedException e) {
                               e.printStackTrace();
                               return;
                           }
                   		if(x < pos[index]) {
                   			 x += 1*ball_speed;
                          	   parent.repaint(x-radius, y-radius, 2*radius, 2*radius);         
                               parent.repaint(xOld-radius, yOld-radius, 2*radius, 2*radius);
                   		}else {
                   			break;
                   		}
                   	}
               	}
            	   
            	   try {
                       Thread.sleep(1000);
                       data1[index] = value;
                       s = "";
                   } catch (InterruptedException e) {
                       e.printStackTrace();
                       return;
                   }
            	   
            	   
            	   if(index <= 6) {
                   	while(true) {
                   		try {
                               Thread.sleep(10);
                           } catch (InterruptedException e) {
                               e.printStackTrace();
                               return;
                           }
                   		if(x < 375) {
                   			 x += 1*ball_speed;
                          	    parent.repaint(x-radius, y-radius, 2*radius, 2*radius);         
                               parent.repaint(xOld-radius, yOld-radius, 2*radius, 2*radius);
                   		}else {
                   			break;
                   		}
                   	}
               	}else {
               		while(true) {
                   		try {
                               Thread.sleep(10);
                           } catch (InterruptedException e) {
                               e.printStackTrace();
                               return;
                           }
                   		if(x > 375) {
                   			 x -= 1*ball_speed;
                          	   parent.repaint(x-radius, y-radius, 2*radius, 2*radius);         
                               parent.repaint(xOld-radius, yOld-radius, 2*radius, 2*radius);
                   		}else {
                   			break;
                   		}
                   	}
               	}
            	   

            	   
            	   
            	   synchronized(canReadMutex) {	
           			   canRead = false;
           		   }
            	       
            	   
            	   while(true) {
             			synchronized(rcMutex) {
             				if(readerCount == 0) {           					
             					break;
             				}else {
             					 try { 
             						 rcMutex.wait(); 
             				     } 
             				     catch(InterruptedException e) { 
             				         e.printStackTrace(); 
             				     }
             				}
             			}
             		}
            	        
            	   
            	   synchronized(copyMutex) {
            	      startCopy = true;
            	   }
         	   
            	   synchronized(moveMutex) {
     					 try { 
     						moveMutex.wait(); 
       				     } 
       				     catch(InterruptedException e) { 
       				         e.printStackTrace(); 
       				     }
            	   }  
            	   
            	   	   
            	   try {
                       Thread.sleep(1000);
                   } catch (InterruptedException e) {
                       e.printStackTrace();
                       return;
                   }
            	   
            	   for(int i=0; i<10;i++) {
  						data2[i] = data1[i];
  					}
            	  
            	   
            	   synchronized(copyMutex) {
            	      startCopy = false;
            	   }
            	   
            	   
            	   while(true) {
                  		try {
                              Thread.sleep(10);
                          } catch (InterruptedException e) {
                              e.printStackTrace();
                              return;
                          }
                  		if(y > 130) {
                  			 y -= 1*ball_speed;
                         	    parent.repaint(x-radius, y-radius, 2*radius, 2*radius);         
                              parent.repaint(xOld-radius, yOld-radius, 2*radius, 2*radius);
                  		}else {
                  			break;
                  		}
                  	 }
            	   
            	   
            	   
            	   synchronized(canReadMutex) {	
           			canRead = true;
           			canReadMutex.notifyAll();
           		   }
           		
           		   synchronized(canWriteMutex) {	
           			canWrite = true;
           			canWriteMutex.notifyAll();
           			 System.out.println("Levae write: [" + "]");
           		   }
            	  
           		synchronized(rFinishMutex) {
            		synchronized(wFinishMutex) {
            			   wfinishCount++;
		             	 if(rfinishCount == red_size && wfinishCount == blue_size) {
		            		 playbtn.setEnabled(true);
		            	 }
            		}
           		}
           		  
           		  while(true) {
                		try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            return;
                        }
                		if(y > -20) {
                			 y -= 1*ball_speed;
                       	    parent.repaint(x-radius, y-radius, 2*radius, 2*radius);         
                            parent.repaint(xOld-radius, yOld-radius, 2*radius, 2*radius);
                		}else {
                             	break;                           
                		}
                	 }
           		   
           		   
  
                }
                
                
                
                
                if(color == Color.RED) {
                	
                	// y = 425 �ｿｽ�ｿｽO
                	// y = 480 �ｿｽ�ｿｽ�ｿｽ
                	// x = 50 100 150 200 250 300 350 400 450 500 (index)
                	// x = 375 exit  position
                	// x = 125 enter position 	
                	
                	
                	while(true) {
                		try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            return;
                        }
                		if(y >= 480) {
                			 y -= 1*ball_speed;
                       	    parent.repaint(x-radius, y-radius, 2*radius, 2*radius);         
                            parent.repaint(xOld-radius, yOld-radius, 2*radius, 2*radius);
                		}else {
                			break;
                		}
                	}
                	
                	 synchronized(wwaitMutex) {	
              	    	 wwaitCount++;
              	      }
                	
                	
                	while(true) {
	           			 synchronized(canReadMutex) {	
	           				 if(canRead == true) {
	           					 break;
	           				 }else {
	           					 try { 
	           						    canReadMutex.wait(); 
	           				         } 
	           				         catch(InterruptedException e) { 
	           				             e.printStackTrace(); 
	           				         }
	           				  }
	           			 }	 		 
           		   }
                	
                	
               	 synchronized(wwaitMutex) {	
          	    	 wwaitCount--;
          	      }
                	
               	   
                	
                	while(true) {
                		try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            return;
                        }
                		if(y >= 425) {
                			 y -= 1*ball_speed;
                       	    parent.repaint(x-radius, y-radius, 2*radius, 2*radius);         
                            parent.repaint(xOld-radius, yOld-radius, 2*radius, 2*radius);
                		}else {
                			break;
                		}
                	}
                	
                	
                	synchronized(rcMutex) {	
            			readerCount++;
            			System.out.println("Enter read: [" + "] Now Reader : " + readerCount );
            		}	 
                	
                	
                	int index = ran.nextInt(data_size);

                	if(index <= 2) {
                    	while(true) {
                    		try {
                                Thread.sleep(10);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                                return;
                            }
                    		if(x > pos[index]) {
                    			 x -= 1*ball_speed;
                           	    parent.repaint(x-radius, y-radius, 2*radius, 2*radius);         
                                parent.repaint(xOld-radius, yOld-radius, 2*radius, 2*radius);
                    		}else {
                    			break;
                    		}
                    	}
                	}else {
                		while(true) {
                    		try {
                                Thread.sleep(10);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                                return;
                            }
                    		if(x < pos[index]) {
                    			 x += 1*ball_speed;
                           	    parent.repaint(x-radius, y-radius, 2*radius, 2*radius);         
                                parent.repaint(xOld-radius, yOld-radius, 2*radius, 2*radius);
                    		}else {
                    			break;
                    		}
                    	}
                	}
                	
                	
                	
                	try {
                        Thread.sleep(1000);
                        s = ""+data2[index];
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return;
                    }
                	
                	
                	if(index <= 6) {
                    	while(true) {
                    		try {
                                Thread.sleep(10);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                                return;
                            }
                    		if(x < 375) {
                    			 x += 1*ball_speed;
                           	    parent.repaint(x-radius, y-radius, 2*radius, 2*radius);         
                                parent.repaint(xOld-radius, yOld-radius, 2*radius, 2*radius);
                    		}else {
                    			break;
                    		}
                    	}
                	}else {
                		while(true) {
                    		try {
                                Thread.sleep(10);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                                return;
                            }
                    		if(x > 375) {
                    			 x -= 1*ball_speed;
                           	    parent.repaint(x-radius, y-radius, 2*radius, 2*radius);         
                                parent.repaint(xOld-radius, yOld-radius, 2*radius, 2*radius);
                    		}else {
                    			break;
                    		}
                    	}
                	}
                	
                	
                	while(true) {
                		try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            return;
                        }
                		if(y < 480) {
                			 y += 1*ball_speed;
                       	    parent.repaint(x-radius, y-radius, 2*radius, 2*radius);         
                            parent.repaint(xOld-radius, yOld-radius, 2*radius, 2*radius);
                		}else {
                			break;
                		}
                	}

                	

                	synchronized(rcMutex) {	
            			readerCount--;
            			if(readerCount == 0) {
            				rcMutex.notifyAll();
            			}
            			System.out.println("Leave read: [" + "] Now Reader : " + readerCount );
            		}
                	
                	synchronized(rFinishMutex) {
                		synchronized(wFinishMutex) {
                			 rfinishCount++;                	
	                	 if(rfinishCount == red_size && wfinishCount == blue_size) {
	                		 playbtn.setEnabled(true);
	                	 }
                		}
                	}
                	
                	while(true) {
                		try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            return;
                        }
                			y += 1*ball_speed;
                       	    parent.repaint(x-radius, y-radius, 2*radius, 2*radius);         
                            parent.repaint(xOld-radius, yOld-radius, 2*radius, 2*radius);
                            if(y > 620) {
                            	break;
                            }
                	}
                	
                	
                }
                
                
                
            }
        
        
    }
       
        
        
    public class DateTasks extends TimerTask {
        private int x, y, radius, milliseconds;
        private boolean blink, visible=true;
        private JComponent parent;
        private List<BallWithOwnThread> balls;
        private RWsProblem p;
		 public DateTasks(int radius, int milliseconds, JComponent parent,List<BallWithOwnThread> balls , RWsProblem p) {
			 super();
		     this.radius = radius;
		     this.milliseconds = milliseconds;
		     this.parent = parent;
		     this.balls = balls;
		     this.p = p;
		 }
		 @ Override
		 public void run() {
			 double c = Math.random();
			 synchronized(generateMutex){
			  if(c >= 0.5 && (rGenerateCount < red_size || red_size < 0)) {
				 balls.add( new BallWithOwnThread(Color.RED,175, 590, radius, milliseconds, parent,balls,p) );
				 rGenerateCount++;
		   	  }else if(wGenerateCount < blue_size || blue_size < 0){
		   		 balls.add(new BallWithOwnThread(Color.BLUE,175, 10, radius,  milliseconds, parent,balls,p) );	  
		   		 wGenerateCount++;
		   	 }else if((rGenerateCount < red_size || red_size < 0)) {
		   		 balls.add( new BallWithOwnThread(Color.RED,175, 590, radius, milliseconds, parent,balls,p) );
		   		 rGenerateCount++;
		   	 }
			 }
		 }
  }
    
    class StartHandler implements ActionListener {
    	RWsProblem p;
    	Harbor h;
    	JButton startB;
    	public StartHandler(RWsProblem p,Harbor h,JButton startB  ) {
    		this.p = p;
    		this.h= h;
    		this.startB = startB;
    	}
        public void actionPerformed(ActionEvent e) { 
        	rGenerateCount = 0;
        	wGenerateCount = 0;
        	wfinishCount = 0;
        	rfinishCount = 0;
        	// "playSpeed" input////////////////////////////
        	int speed = 0;
        	if(playSpeed.getText().equals("")) {
        		speed = 1;
        	}else {       		
        		speed = Integer.parseInt(playSpeed.getText());
        	}
        	if(speed < 1) {
        		speed = 1;
        	}else if(speed > 5f) {
        		speed = 5;
        	}
        	ball_speed = speed;
        	//////////////////////////////////////////
        	
        	
        	
        	// "Room size" input////////////////////////////
        	if(!roomSize.getText().equals("")) {
	        	int size = 0;
	        	size = Integer.parseInt(roomSize.getText());
	        	if(size>10) {
	        		size= 10;
	        	}
	        	if(size < 1) {
	        		size = 1;
	        	}
	        	
	        	for (int i = size; i < 10; i ++) {
	        		data1[i] = -1;
	        		data2[i] = -1;
	        	}
	        	data_size = size;
	        	room_size = size;
        	}
        	//////////////////////////////////////////
        	
        	// "Seed" input////////////////////////////
        	int seed = 0;
        	if(roomSeed.getText().equals("")) {
        	    seed = (int)Calendar.getInstance().getTimeInMillis();
        	}else {
        		seed = Integer.parseInt(roomSeed.getText());
        	}
        	Random ran2 = new Random(seed);
        	room_seed = seed;
        	for (int i = 0; i < data_size; i ++) {
        		data1[i] = ran2.nextInt(100);
        		data2[i] = data1[i];
        	}
        	//////////////////////////////////////////
        	
       	
        	// "reader limit" input////////////////////////////
        	if(!readerLimit.getText().equals("")) {
	        	int size = 0;
	        	size = Integer.parseInt(readerLimit.getText());
	        	if(size<1) {
	        		size= 1;
	        	}
	        	red_size = size;
        	}
        	//////////////////////////////////////////
        	

        	
        	// "ter limit" input////////////////////////////
        	if(!writerLimit.getText().equals("")) {
	        	int size = 0;
	        	size = Integer.parseInt(writerLimit.getText());
	        	if(size<1) {
	        		size= 1;
	        	}
	        	blue_size = size;
        	}
        	//////////////////////////////////////////
        	
        	
        	
        	h.balls.add(new BallWithOwnThread(Color.RED, 175, 590, 14, 101, h ,h.balls,p) );
        	synchronized(generateMutex){
        	    rGenerateCount++;
        	}
        	startB.setEnabled(false);
        }
    }
    
        
 
}