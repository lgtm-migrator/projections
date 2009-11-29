package projections.gui;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;

public class GraphDisplayCanvas extends Canvas 
{

	// Temporary hardcode. This variable will be assigned appropriate
    // meaning in future versions of Projections that support multiple
    // runs.
    int myRun = 0;

   private GraphData data;
   private OrderedGraphDataList graphDataList;
   private Image offscreen;

   public GraphDisplayCanvas()
   {
	  setBackground(MainWindow.runObject[myRun].background);
   }   
   private void addDataForBar(ZItem item, int x)
   {
	  int h = getSize().height;
	  
	  int y = 0;

	  if (data.xmode == GraphData.PROCESSOR) {
	      y = item.curPData[x];
	  } else {
	      y = item.curIData[x];   
	  }
	  if (MainWindow.dataDump != null) {
	      if (GraphWindow.dumpNow) {
		  MainWindow.dataDump.print(" " + y);
	      }
	  }
	  if (item.ymode == GraphData.BOTH) {
	      y = h - (int)(data.wscale * y);
	  } else {
	      y = h - (int)(data.yscale * y);
	  }				   
	  graphDataList.insert(y, item.color);
   }   

   private void drawDisplay(Graphics g)
   {
       if (data == null)
	   return;
       
       int w = getSize().width;
       int h = getSize().height;
       
       g.setColor(MainWindow.runObject[myRun].background);
       g.fillRect(0, 0, w, h);
       g.setColor(MainWindow.runObject[myRun].foreground);
       g.translate(-data.displayPanel.getHSBValue(), 0);
       
       if (MainWindow.dataDump != null) {
	   if (GraphWindow.dumpNow) {
	       if (data.xmode == GraphData.PROCESSOR) {
		   MainWindow.dataDump.println("--- Graph PE ---");
	       } else {
		   MainWindow.dataDump.println("--- Graph INT ---");
	       }
	   }
       }
       
       if (data.graphtype == GraphData.LINE) {
	   for (int a=0; a<data.onGraph.length; a++) {
	       if (data.ymode == GraphData.MSGS) {
		   if (data.onGraph[a].ymode != GraphData.TIME) {
		       drawItemLine(g, data.onGraph[a]);
		   }
	       } else {
		   if (data.onGraph[a].ymode != GraphData.MSGS) {
		       drawItemLine(g, data.onGraph[a]);
		   }
	       }
	   } 
       } else {
	   graphDataList = new OrderedGraphDataList();
	   
	   long x_value = 0;
	   if (data.xmode == GraphData.PROCESSOR) {
	       data.origProcList.reset();
	       x_value = data.origProcList.nextElement();
	   } else {
	       data.interval.list.reset();
	       x_value = data.interval.list.nextElement()*
		   data.interval.size;
	   }
	   for (int x=data.minx; x<=data.maxx; x++) {
	       graphDataList.removeAll();
	       if (MainWindow.dataDump != null) {
		   MainWindow.dataDump.print(x_value);
		   if (data.xmode == GraphData.PROCESSOR) {
		       x_value = data.origProcList.nextElement();
		   } else {
		       x_value = data.interval.list.nextElement()*
			   data.interval.size;
		   }
	       }
	       for (int a=0; a<data.onGraph.length; a++) {   
		   if (data.ymode == GraphData.MSGS) {
		       if (data.onGraph[a].ymode != GraphData.TIME) {
			   addDataForBar(data.onGraph[a], x);
		       }
		   } else {
		       if (data.onGraph[a].ymode != GraphData.MSGS) {
			   addDataForBar(data.onGraph[a], x);
		       }
		   }
	       }
	       if (MainWindow.dataDump != null) {
		   MainWindow.dataDump.println();
	       }
	       drawItemBar(g, x);
	   }
       }
       if (MainWindow.dataDump != null) {
	   if (GraphWindow.dumpNow) {
	       MainWindow.dataDump.flush();
	       GraphWindow.dumpNow = false;
	   }
       }
   }   
    

    private void drawItemBar(Graphics g, int x)
    {
	int x1, y, w, h;
	Color c;
	
	x1 = (int)(data.xscale*x);
	w  = (int)(data.xscale*(x+1)) - x1 - 1;
	if(w<1) w=1;
	
	x1 += data.offset3;
	
	graphDataList.reset();
	while (graphDataList.hasMoreElements()) {
	    y = graphDataList.currentY();
	    c = graphDataList.currentC();          
	    h = getSize().height - y; 
	    if (h > 0) {
		g.setColor(c);
		g.fillRect(x1, y, w, h);
	    }  
	    graphDataList.nextElement();    
	}    
    }   

    private void drawItemLine(Graphics g, ZItem item)
    {
	int  x1, x2, y1, y2;
	g.setColor(item.color);
	x1 = y1 = -1;
	
	int h = getSize().height;
	for (int x=data.minx; x<= data.maxx; x++) {
	   
	    x2 = data.offset3 + (int)(x * data.xscale);
	    
	    if (data.xmode == GraphData.PROCESSOR) {
		y2 = item.curPData[x];
	    } else {
		y2 = item.curIData[x];   
	    }
	    if (MainWindow.dataDump != null) {
		MainWindow.dataDump.print(" " + y2);
	    }
	    
	    if (item.ymode == GraphData.BOTH)  
		y2 = h - (int)(data.wscale * y2);
	    else           
		y2 = h - (int)(data.yscale * y2);
							   
	    if(x1 >= 0 && !(y2==h && y1==y2))
		g.drawLine(x1, y1, x2, y2);
	    x1 = x2;
	    y1 = y2;    
	}    
    }   
    
   public void paint(Graphics g)
   {
	  if(offscreen == null)
		 return;
	  Graphics og = offscreen.getGraphics();
	   
	  drawDisplay(og);
	  
	  int w = getSize().width;
	  int h = getSize().height;
	  g.drawImage(offscreen, 0,0,w,h, 0,0,w,h, null);                  
   }

   public void print(Graphics pg)
   {
       ((Graphics2D)pg).setBackground(Color.white);
       setForeground(Color.black);
       drawDisplay(pg);
       ((Graphics2D)pg).setBackground(Color.black);
       setForeground(Color.white);
   }   
   public void setBounds(int x, int y, int w, int h)
   {     
	  // make offscreen image   
	  if(getSize().width != w || getSize().height != h)
	  {
		 try
		 {
			offscreen = createImage(w, h);
		 }
		 catch(OutOfMemoryError e)
		 {
			System.out.println("NOT ENOUGH MEMORY!");  
		 }
	  }      
	  super.setBounds(x, y, w, h);  
   }   
   public void setData(GraphData data)
   {
	  this.data = data;
   }   
   public void update(Graphics g)
   {
	  paint(g);
   }   
}
