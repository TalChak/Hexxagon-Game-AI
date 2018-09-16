package Hexx;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.Timer;
import javax.swing.text.*;

/*
 *
 * @author tchaklai
 */


public class Frame extends JFrame {
 ///frames and panels
    private JPanel panel;
    private JPanel textPanel;
    private  JFrame textFrame;
    private Game hexx;  //new game
    private String [][] gameMat;    //status of cells
    private final int XOFFSET;   //for the hexagon calculation
    private final int YOFFSET;   //for the hexagon calculation
    private int fromX; // origin cell
    private int fromY; // 
    private int pwidth;  //panel sizes
    private int pheight;
    private final int EDGE_WIDTH;  //the hexagon edge
    private final int BOARD_SIZE;   //board size is constant
    private final int INVALID_COORDINATE;  //-1
    private int level;  
    private Board board;  //new board
    
    
	 public Frame() 
	 {
		//frames  and panel settings
	     this.INVALID_COORDINATE=-1;
	     this.fromX = INVALID_COORDINATE;  //at the beginig, the from point is (-1,-1).
 	     this.fromY = INVALID_COORDINATE;
	     this.EDGE_WIDTH=50;   //every edge of each hexagon
	     this.XOFFSET=5;      
	     this.YOFFSET=5;
	     this.BOARD_SIZE=6;   //constant
	     this.setDefaultCloseOperation(EXIT_ON_CLOSE); 
	     this.panel=new JPanel();
	     this.textFrame=new JFrame();
	     this.textFrame.setSize(100,100);
	     this.textFrame.setLocation(200, 200);
	     this.calculatePanelDims();	//we need to calculate the size of he panel according to the size of the hexagons
	     this.panel.setSize(pwidth, pheight); 
	     this.setSize(pwidth, pheight);
	     this.setLocation(450, 100);
	     this.setResizable(false);
	  	 this.panel.setBackground(Color.white);
	     this.textPanel=new JPanel();
         this.textPanel.setSize(pwidth,100);
         this.textPanel.setLocation(0,0);
	     this.hexx=new Game(BOARD_SIZE);  //new game
	     this.board=new Board(BOARD_SIZE, BOARD_SIZE, XOFFSET, YOFFSET, EDGE_WIDTH);  //new board   
	     this.initGameMat();     // set the game matrix
         this.add(panel);  

         this.panel.addMouseListener(new CustomMouseListener());   //add mouse listener for the user
	     chooselevel();

     }
	 
	 public void chooselevel()   //open a dialog box for the user to chose the level
	 {
		//choose level
		 Object[] options = {"Level1",
		                     "Level2",
		                     "Level3"};
		 int n = JOptionPane.showOptionDialog(textFrame,
		     "Choose a Level ",
		     "Hexxagon Game",
		     JOptionPane.YES_NO_CANCEL_OPTION,
		     JOptionPane.QUESTION_MESSAGE,
		     null,
		     options,
		     options[0]);
		 
		 level=n;
		 hexx.setDepth(n);   //search depth of Alpha Beta algorithm is based  on the chosen level
	 }

	 
	 public void initGameMat()   //first board position
	 {
		 gameMat=new String[][] {{"red","empty","empty","empty","empty","blue"},
	    		{"empty","invalid","empty","empty","empty","empty"},
	    		{"empty","empty","empty","empty","invalid","empty"},
	    		{"empty","invalid","empty","empty","empty","empty"},
	    		{"empty","empty","empty","empty","invalid","empty"},
	    		{"blue","empty","empty","empty","empty","red"}};
	    	
	    		
	    		
	    for(int i=0;i<BOARD_SIZE;i++)
	    {
	    	for(int j=0;j<BOARD_SIZE;j++)
	    	{
	    		hexx.setCell(i, j, gameMat[i][j]);    //update the Game classes' board
	    	}
	    	
	    }
	 }

	     
	 public void calculatePanelDims()   //we need to calculate the size of he panel according to the size of the hexagons
	 {
	        int h = (int)(Math.sin(degreesToRadians(30)) * EDGE_WIDTH);
	        int r = (int)(Math.cos(degreesToRadians(30)) * EDGE_WIDTH);
		 pwidth = (int)h + 2 * XOFFSET + (int)((BOARD_SIZE / 2.0) * (2 *EDGE_WIDTH + BOARD_SIZE)) + (int)((BOARD_SIZE / 2.0) * EDGE_WIDTH);
		 pheight = 2 * XOFFSET + (int)(2 * BOARD_SIZE * r) + (int)r;
	 
	 }
	 public void paint(Graphics g)  //every move the board needs to be repaint
	    {							//it is done according to the game moves
		 	this.board.boardColorUpdate(hexx);
		 	this.board.draw(g);
	    }

	   public static double degreesToRadians(double degrees)  // we need it to create the boards' dimensions 
       {
           return degrees * Math.PI / 180;
       }
	   
	   
	   public class CustomMouseListener extends JPanel implements MouseListener
	   {
		   public void mouseClicked(MouseEvent e)
		   {
			
			   Point currPoint = board.getPositionByCoordinates(e.getPoint());
			   System.out.print("player clicked on cell: "+currPoint.toString());
			   
	          //if  the user didn't press on an hexagon, nothing happens
			   
	           if (currPoint.x != INVALID_COORDINATE && currPoint.y != INVALID_COORDINATE)
	           {
	                   if ((fromX == INVALID_COORDINATE)) ///game starts
	                   {
	                       if (hexx.getCell(currPoint.x, currPoint.y) == "red")  //the user needs to press
	                       {												    //on the cell from which he wants to move, 
	                           fromX = currPoint.x;								//so we'll know the source cell
	                           fromY = currPoint.y;
	                       }
	                   }
	                   else
	                   {
	                       String player = "red";  
	                        int  dist=0;  //set as default value;

	                       // Source block clicked again, remove highlight
	                       if ((currPoint.x == fromX) && (currPoint.y == fromY))  
	                       {
	                           fromX = INVALID_COORDINATE;   //if the user has pressed the same cell again, we start over again
	                           fromY = INVALID_COORDINATE;
	                       }
	            
	                       else
	                       {
	                    	  //set the distance between the original cell and the chosen cell
	                    	  dist=Math.max(Math.abs(currPoint.x - fromX),Math.abs(currPoint.y - fromY));	                    	   
	                    	   ///check if the move is valid
	                    	   if (hexx.isValidMove(fromX, fromY, currPoint.x, currPoint.y, player, dist)==true)
	                    	   {
	                           // if it is, tell prolog to make this move
	                    		   hexx.makeMove(fromX, fromY, currPoint.x, currPoint.y, player, dist);
	                           		board.boardColorUpdate(hexx);  //update board colors
	                           		this.repaint(); 				//paint the updated board 
	                           		fromX = INVALID_COORDINATE;  //from the beginning
	                           		fromY = INVALID_COORDINATE;
	                           		
	                           		
	                           // Check if there is a winner.
	                           		if (hexx.checkForWin(player))
	                           		{
	                           		  JOptionPane.showMessageDialog(this,
	                                		    player+ " Player Wins",
	                                		    "Hexxagon Game",
	                                		    JOptionPane.PLAIN_MESSAGE);
	                           		}
	                           		else
	                           		{
	                               // make computer move only if it has an option.
	                           			if (hexx.HasMoves("blue"))
	                           			{
		                                   boolean isGameOver = false;
		                                   
		                                   int numberOfBlues = hexx.numOfCells("blue");
		                                   int numberOfReds = hexx.numOfCells("red");
		                                   do
		                                   {
		                                	   JOptionPane.showMessageDialog(this,
			                                		    "Computer is making a move",
			                                		    "Hexxagon Game",
			                                		    JOptionPane.PLAIN_MESSAGE);
		
		                                       
		                                       hexx.computerMove(level);
		                                       board.boardColorUpdate(hexx);
		                                       this.repaint();
		                                       
		                                       // Check if there is a winner and who won the game
		                                       if (hexx.checkForWin(player))
		                                       {
		                                    	   String string1;
		                                    	   
		                                    	   if (numberOfReds > numberOfBlues)
		                                           {
		                                               string1 = "red";
		                                           }
		                                           else if (numberOfBlues > numberOfReds)
		                                           {
		                                        	   string1 = "blue";
		                                           }
		                                           else
		                                           {
		                                        	   string1 = "tie";
		                                           }
		                                  		  JOptionPane.showMessageDialog(this,   
		  	                                		    string1 + " Player Wins",
		  	                                		    "Hexxagon Game",
		  	                                		    JOptionPane.PLAIN_MESSAGE);
		                                    	   
		                                       }

		                                         	
		                                       
		              
		                                   } while (!hexx.HasMoves("red") && !isGameOver); 
	
	                                   JOptionPane.showMessageDialog(this,
	                                		    "Your Turn",
	                                		    "Hexxagon Game",
	                                		    JOptionPane.PLAIN_MESSAGE);
	                              }
	                            }
	                         } 
	                    	 else
	                       {
	                    		 JOptionPane.showMessageDialog(this,
	                    				    "Invalid Move",
	                    				    "Hexxagon Game",
	                    				    JOptionPane.ERROR_MESSAGE);
	                       }
	                       
	                   }
	                   // Force a refresh of the graphical game board
	                   repaint();
	               }
	           }  	 	     
		   }
		   //override
		     public void mousePressed(MouseEvent e) {
		      }
		      public void mouseReleased(MouseEvent e) {
		      }
		      public void mouseEntered(MouseEvent e) {
		      }
		      public void mouseExited(MouseEvent e) {
		      
		      }
		      
	   }
	 
	   
  
}
