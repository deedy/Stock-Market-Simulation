/* FIXES:
 * 1. Fix the moving of the gray lines
 * 2. Make variables like timepass static
 * 3. Keep developing the stock generation function
 * 4. Work on deleting the past stock data
 * 5. Check limit correction
 * 6. Labelling the axes
 * 7. Pausing simulation, and using mouseMove to track stocks
 * 8. Written visual display of stock prices.
 */
import java.awt.*; 
import java.applet.*; 
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.util.*;
import java.awt.image.BufferedImage;
import java.text.*;
public class StockDoubleBufferAnimated extends Applet implements ActionListener, Runnable {
    Thread animation;
    Graphics offscreen;
    Image image;
    
    static final int REFRESH_RATE=100;
    Vector< Vector<Double> > arr=new Vector< Vector<Double> >();
    private static int FRAME_WIDTH=1000;
    private static int FRAME_HEIGHT=600;
    private static int X_LENGTH=800;
    private static int Y_LENGTH=400;
    private static int X_SEP=100;
    private static int Y_SEP=100;
    private double X_MULTIPLIER=1;
    private double Y_MULTIPLIER=1;
    private int VISUAL_LIMIT=1000;
    private boolean neg=false;
    private Graphics2D ig ;
    private Image image2;
    private double MAX=25;
    private int time=0;
    private Color colorDef []= {Color.red, Color.blue, Color.yellow, Color.green, Color.magenta, Color.pink, Color.cyan, Color.orange, Color.black};
    /** Real Paint method */
    public void paintGraph (Graphics g) {
        BufferedImage image=new BufferedImage (FRAME_WIDTH,FRAME_HEIGHT, BufferedImage.TYPE_INT_RGB);
        ig=image.createGraphics();
        ig.setRenderingHint (RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        
        try {
            CheckLimits();
        }catch (Exception e) {}
        drawAxes (ig,Color.white,Color.black);
        plotGraphs(ig);
        

       g.drawImage (image,0,0, this);
       image2=image;
    }
    
    
    /**Applet Initializer Method */
    public void init () {
        setBackground (Color.black);
        image= createImage (FRAME_WIDTH, FRAME_HEIGHT);
        offscreen=image.getGraphics ();
        setSize (FRAME_WIDTH, FRAME_HEIGHT);
        for (int i=0;i<10;i++) {
            Vector<Double> v=new Vector<Double>();
            for (int j=0;j<100;j++) {
                v.add(nextStockGen(v));
            }
            arr.add(v);
        }
         paintGraph(offscreen);   
    }
    
    /** Button Listener Method */
    public void actionPerformed(ActionEvent evt)  
    { 
    }
    
    /** Button Listener Method */
    public boolean mouseMove (Event e, int x, int y)  
    { 
        offscreen.drawImage (image2,0,0,this);
        if (x>X_SEP && x<X_SEP+X_LENGTH && y>FRAME_HEIGHT-Y_SEP-Y_LENGTH && y<FRAME_HEIGHT-Y_SEP) {
            
            offscreen.setColor (Color.red);
            //Derive index from x
            for (int i=0;i<arr.size();i++) {
                int index=(int) ((x-X_SEP)/(double)(X_LENGTH) * (arr.get(i)).size());
                try {
                    offscreen.setColor (colorDef[i]);
                } catch (Exception ex) {
                    offscreen.setColor (Color.black);
                }
                offscreen.drawOval ((int)(X_SEP+index*X_MULTIPLIER)-3,(int)(FRAME_HEIGHT-Y_SEP-(neg?Y_LENGTH/2:0)-arr.get(i).get(index)*Y_MULTIPLIER)-3,6,6);

            }
        }
        return true;
    }
    
    /** Applet Start Method */
    public void start () {
        animation = new Thread (this);
        if (animation!=null) {
            animation.start ();
        }
    }
    
    /** Double-Buffered Overridden Update Method */
    public void update (Graphics g) {
        paint (g);
    }
    
    /** Double-Buddered Paint Method */
    public void paint (Graphics g) {

        //ig.setRenderingHint (RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        
        try {
            CheckLimits();
        }catch (Exception e) {}
        drawAxes (ig,Color.white,Color.black);
        Timepass(5);
        if (isFinish()) {
            System.out.println (time);
            while (true) {
                try {
                Thread.sleep(10);
                }catch (Exception e) {}
            }                
        }
        plotGraphs(ig);
        g.drawImage (image2,0,0,this);
        //image2=image;
    }
    
    public boolean isFinish() {
        boolean tag=true;
        for (int i=0;i<arr.size();i++) {
            if (arr.get(i).get(arr.get(i).size()-1)!=0) {
                tag=false;
                continue;
            }
            else if (arr.get(i).get(arr.get(i).size()-1)==0 && arr.get(i).get(arr.get(i).size()-5)!=0)
                System.out.println (i+" is OUT.");
        }
        return tag;
    }
    
    public void Timepass (int a) {
        time+=1;
        for (int i=0;i<arr.size();i++) {
            for (int j=0;j<a;j++) {
                arr.get(i).add(nextStockGen(arr.get(i)));
                
            }
        }
    }
    
    /**Main Run Method */
    public void run () {
        while (true) {
            repaint ();
            try {
                Thread.sleep (REFRESH_RATE);
            }catch (Exception e) {};
        }
    }
    
    /** Apple Stop Method */
    public void stop () {
        if (animation!=null) {
            animation.stop();
            animation=null;
        }
    }
    
    public void CheckLimits () {
        int who=0;
        int xlim=arr.get(0).size();
        double ylim=Math.abs(arr.get(0).get(0));
        for (int i=0;i<arr.size();i++) {
            for (int j=0;j<arr.get(i).size();j++) {
                if (Math.abs(arr.get(i).get(j))>ylim) {
                    ylim=Math.abs(arr.get(i).get(j));
                    who=i;
                }
                if (arr.get(i).get(j)<0)
                    neg=true;
            }
        }
        if (ylim>MAX) {
           System.out.println (ylim+"\t"+time+"\t"+who);
           time=0;
        }
        MAX=ylim;
       
        Y_MULTIPLIER=neg?Y_LENGTH/(2*Math.ceil(ylim)):Y_LENGTH/Math.ceil(ylim);
        
    }
    
    private void plotIndiGraph (Graphics g, Vector<Double>t)  {
        X_MULTIPLIER=(double)X_LENGTH/t.size();
        int i;
        if (t.size()<VISUAL_LIMIT) {
        for (i=0;i<t.size()-1;i++) {
            g.drawLine ((int)(X_SEP+i*X_MULTIPLIER),(int)(FRAME_HEIGHT-Y_SEP-(neg?Y_LENGTH/2:0)-t.get(i)*Y_MULTIPLIER),
                        (int)(X_SEP+(i+1)*X_MULTIPLIER),(int)(FRAME_HEIGHT-Y_SEP-(neg?Y_LENGTH/2:0)-t.get(i+1)*Y_MULTIPLIER));
            
        }
        }
        else {
            X_MULTIPLIER=(double)X_LENGTH/VISUAL_LIMIT;
            for (i=t.size()-VISUAL_LIMIT;i<t.size()-1;i++) {
            g.drawLine ((int)(X_SEP+(i-t.size()+VISUAL_LIMIT)*X_MULTIPLIER),(int)(FRAME_HEIGHT-Y_SEP-(neg?Y_LENGTH/2:0)-t.get(i)*Y_MULTIPLIER),
                        (int)(X_SEP+(i-t.size()+VISUAL_LIMIT+1)*X_MULTIPLIER),(int)(FRAME_HEIGHT-Y_SEP-(neg?Y_LENGTH/2:0)-t.get(i+1)*Y_MULTIPLIER));
            
        }
        }
        
    }
    public void plotGraphs (Graphics g) {
         for (int i=0;i<arr.size();i++) {
            try {
                g.setColor (colorDef[i]);
            } catch (Exception e) {
                g.setColor (Color.black);
            }
            plotIndiGraph (g,arr.get(i));
        }
    }
        
    
    public void drawAxes (Graphics g, Color back, Color front) {
        
        Color c=g.getColor();
        g.setColor (back);
        g.fillRect (0,0,FRAME_WIDTH,FRAME_HEIGHT);
        g.setColor (new Color (225,225,225));
        if (neg) {
             g.drawLine (X_SEP,(int)(FRAME_HEIGHT-Y_SEP-Y_LENGTH/2),X_SEP+X_LENGTH,(int)(FRAME_HEIGHT-Y_SEP-Y_LENGTH/2));
        }
        for (int i=1;i<8;i++) {
            if (i%2==0)
                g.setColor (new Color (190,190,220));
            else
                g.setColor (new Color (208,208,240));
            g.drawLine (X_SEP,(int)(FRAME_HEIGHT-Y_SEP-(i*Y_LENGTH/8)),X_SEP+X_LENGTH,(int)(FRAME_HEIGHT-Y_SEP-(i*Y_LENGTH/8)));
        }
        
        for (int i=1;i<10;i++) {
            if (i%4==0)
                g.setColor (new Color (216,216,216));
            else
                g.setColor (new Color (245,245,245));
            g.drawLine ((int)(X_SEP+(i*X_LENGTH/10)),FRAME_HEIGHT-Y_SEP,(int)(X_SEP+(i*X_LENGTH/10)),FRAME_HEIGHT-Y_SEP-Y_LENGTH);
        }
        g.setColor (front);
        g.drawLine (X_SEP,FRAME_HEIGHT-Y_SEP,X_SEP,FRAME_HEIGHT-Y_SEP-Y_LENGTH);
        g.drawLine (X_SEP,FRAME_HEIGHT-Y_SEP,X_SEP+X_LENGTH,FRAME_HEIGHT-Y_SEP);
        g.drawLine (X_SEP,FRAME_HEIGHT-Y_SEP-Y_LENGTH,X_SEP+X_LENGTH,FRAME_HEIGHT-Y_SEP-Y_LENGTH);
        g.drawLine (X_SEP+X_LENGTH,FRAME_HEIGHT-Y_SEP,X_SEP+X_LENGTH,FRAME_HEIGHT-Y_SEP-Y_LENGTH);
        if (neg) {
            g.setColor (Color.black);
             g.drawLine (X_SEP,(int)(FRAME_HEIGHT-Y_SEP-Y_LENGTH/2),X_SEP+X_LENGTH,(int)(FRAME_HEIGHT-Y_SEP-Y_LENGTH/2));
        }
        g.setColor (c);
    }
    
    public static double[] randomAvgGen () {
        double a[]=new double [500];
        double sum=0;
        for (int i=0;i<a.length;i++) {
            sum+=Math.pow(Math.random()*Math.random(),Math.random()*3);
            a[i]=sum/(i+1);
        }
        return a;
        
    }
    
    //Additive with Threshold
    public static Double nextStockGen2 (Vector<Double> a) {
        double threshold=Math.random();
        if (a.size()==0)
            return Math.random();
        else if (a.size()==1)
            return a.get(0)+Math.random();
        else {
            double next;
            if (a.get(a.size()-1)-a.get(a.size()-2)>0) {
                if (Math.random()<threshold) {
                    next=Math.random();
                }
                else
                    next=-Math.random();
            }
            else {
                if (Math.random()>threshold) {
                    next=-Math.random();
                }
                else
                    next=Math.random();
            }
            return (a.get(a.size()-1)+next);

        }
    }
    
    //Multiplicative with Volatility
    public static Double nextStockGen1 (Vector<Double> a) {
        if (a.size()==0)
            return 25.0;
        else {
            if (a.get(a.size()-1)<1)
                return 0.0;
            double volatility=10*Math.random();
            double next;
            if (Math.random()>0.5) {
                next=volatility*Math.random();
            }
            else {
                next=-volatility*Math.random();
            }
            return a.get(a.size()-1)*(100+next)/100;
        }
            
        }
    
    //Multiplicative Simple with upward market bias
    public static Double nextStockGen (Vector<Double> a) {
        if (a.size()==0)
            return 25.0;
        else {
            if (a.get(a.size()-1)<1)
                return 0.0;
            double b=(Math.random())/2 + 0.5;
            return a.get(a.size()-1)*(b*10+(Math.random()-0.5))/(b*10);
        }
    }
                 
    
}
    