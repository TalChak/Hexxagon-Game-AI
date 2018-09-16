package Hexx;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.awt.*;
import java.lang.Object;
import javax.swing.*;
import java.awt.event.*;
import java.awt.geom.*; 



/**
 *
 * @author tchaklai
 */
public class Board {
    
    private Color [][] colorMat;  // color matrix
    private int BOARD_WIDTH; //  board dimensions
    private int BOARD_HEIGHT; // 
    private int EDGE_WIDTH; // The width of each edge of the hexagon.
    private int XOffset;  //helps us calculate the exagon
    private int YOffset;
    private int INVALID_COORDINATE; //-1
    
    //create the board
    public Board(int width, int height, int nxoffset, int nyoffset, int nedgewidth) 
    {
         BOARD_WIDTH=width;   
         BOARD_HEIGHT=height;  
         XOffset=5;
         YOffset=5;
         EDGE_WIDTH= nedgewidth;
         colorMat=new Color[width][height];
         
    }
    
 
    //update color matrix
   public void colorUpdate(Color color, Point point)
   {
       colorMat[point.x][point.y]=color;
   }
   
   
   //every time a player moves, we need to update the colors of the cells
   public void boardColorUpdate(Game hexx)
   {
	   
	   for (int i = 0; i < BOARD_WIDTH; i++)
       {
           for (int j = 0; j < BOARD_HEIGHT; ++j)
           {
        	   
               if (hexx.getCell(i, j) == "red")
               {
                   colorMat[i][j] = Color.RED;
                   System.out.println(colorMat[i][j]);
               }
               if (hexx.getCell(i, j) == "blue")
               {
                   colorMat[i][ j] = Color.BLUE;
               }
               if (hexx.getCell(i, j) == "empty")
               {
                   colorMat[i][ j] = Color.LIGHT_GRAY;
               }
               if (hexx.getCell(i, j) == "invalid")
               {
                   colorMat[i][ j] = Color.BLACK;
               }
           }
       }
	   
   }
   
   // we need this method to calculate the hexagon points
   public double degreesToRadians(double degrees)
   {
        double radians=degrees*Math.PI/180;
        return radians;
   }
   
   ///check if the chosen point is inside the chosen polygon
    public boolean isInsideCell(Point[] polygon,int N,Point p)  // inside polygon
    {
        int counter=0;
        int i;
        double xinters;
        Point p1;
        Point p2;

        p1 = polygon[0];
        for (i = 1; i <= N; i++)
        {
            p2 = polygon[i % N];
            if (p.y > Math.min(p1.y, p2.y))
            {
                if (p.y <= Math.max(p1.y, p2.y))
                {
                    if (p.x <= Math.max(p1.x, p2.x))
                    {
                        if (p1.y != p2.y)
                        {
                            xinters = (p.y - p1.y) * (p2.x - p1.x) / (p2.y - p1.y) + p1.x;
                            if (p1.x == p2.x || p.x <= xinters)
                                counter++;
                        }
                    }
                }
            }
           p1=p2;
        }

        if (counter % 2 == 0)
            return false;
        else
            return true;
    }
    
    
    //this method translate the GUI board to an array, and tells us which cell in the array was chosen
    public Point getPositionByCoordinates(Point coordinates)
    {
    	Point[] polygonPoints = new Point[6];
    	
       int h=( int)(Math.sin(degreesToRadians(30)) * EDGE_WIDTH);
       int r=( int)(Math.cos(degreesToRadians(30)) * EDGE_WIDTH);
       int x=XOffset+h;
       int y=XOffset;

        for (int i = 0; i < BOARD_WIDTH; i++)
        {
            // Scan one line.
            for (int j = 0; j < BOARD_HEIGHT; j++)
            {
                // Calculate the hexagon vertices.
                polygonPoints[0] = new Point(x, y);
                polygonPoints[1] = new Point(x + EDGE_WIDTH, y);
                polygonPoints[2] = new Point(x + EDGE_WIDTH + h, y + r);
                polygonPoints[3] = new Point(x + EDGE_WIDTH, y + r + r);
                polygonPoints[4] = new Point(x, y + r + r);
                polygonPoints[5] = new Point(x - h, y + r);

                // Check if the coordinate is in this hexagon. 
                if (isInsideCell(polygonPoints, 6, coordinates) == true)
                    return new Point(i, j);

                // Calculate the first vertex of the next hexagon.
                if (j % 2 == 0)
                {
                    x += EDGE_WIDTH + h;
                    y += r;
                }
                else
                {
                    x += EDGE_WIDTH + h;
                    y -= r;
                }
            }

            // Calculate the first vertex of the first hexagon in the next line.
            x = XOffset + h;
            y = YOffset + (i + 1) * (2 * r);
        }

        // The coordinate is not in the board.
       return new Point(INVALID_COORDINATE, INVALID_COORDINATE);
     
    }
    
    
    //draw the board 
    public void draw(Graphics g)
    {
    	Graphics2D g2 = (Graphics2D)g;
		 g2.setStroke(new BasicStroke(5));
	       
	        

	        // Calculate the 'h' value and the 'r' values.
	        int h = (int)(Math.sin(degreesToRadians(30)) * EDGE_WIDTH);
	        int r = (int)(Math.cos(degreesToRadians(30)) * EDGE_WIDTH);

	        // Calculate the first vertex of the first hexagon.
	        int x = XOffset + h;
	        int y = YOffset;
	        Polygon poly;
	        // Draw the board.
	        
	        for (int i = 0; i < BOARD_WIDTH; i++)
	        {
	            // Draw the first line.
	            for (int j = 0; j < BOARD_HEIGHT; j++)
	            {
	            	
	                //Calculate the hexagon vertices.
	             poly=new Polygon();
	             poly.addPoint(x, y);
	             poly.addPoint(x + EDGE_WIDTH, y);
	             poly.addPoint(x + EDGE_WIDTH + h, y + r);
	             poly.addPoint(x + EDGE_WIDTH, y + r + r);
	             poly.addPoint(x, y + r + r);
	             poly.addPoint(x - h, y + r);
	                
	                
	            	
	                // Draw the hexagon.
	               
	                g2.setColor(this.colorMat[i][j]);
	                g2.fillPolygon(poly);
	                g2.setColor(Color.pink);
	                
	                g2.drawPolygon(poly);

	                // Calculate the first vertex of the next hexagon.
	                if (j % 2 == 0)
	                {
	                    x += EDGE_WIDTH + h;
	                    y += r;
	                }
	                else
	                {
	                    x += EDGE_WIDTH + h;
	                    y -= r;
	                }
	            }

	            // Calculate the first vertex of the first hexagon in the next line.
	            x = XOffset + h;
	            y = YOffset + (i + 1) * (2 * r);
	        }
    }
    
 
}


