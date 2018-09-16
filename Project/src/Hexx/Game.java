package Hexx;

import org.jpl7.Query;
import org.jpl7.Term;
import org.jpl7.fli.*;

import java.io.InputStream;
import java.lang.Object;

public class Game {
	
	private final int ALPHA_MIN;  ///final variables for the algorithm
	private final int ALPHA_MAX;
	private final int BETA_MIN;
	private final int BETA_MAX;
	private final int BOARD_SIZE;
	private int DEPTH ;
	private String [][] gameBoard;
	private String boardString;
	private StringBuilder boardStringBuilder;
	
	
	public Game(int boardSize)
	{
		ALPHA_MIN=Integer.MAX_VALUE;  //the biggest "infinity"
		ALPHA_MAX=Integer.MIN_VALUE;   //"the smallest "-infinity"
		BETA_MIN=Integer.MAX_VALUE;
		BETA_MAX=Integer.MIN_VALUE;
		BOARD_SIZE=6; ////
		DEPTH = 0;  //as default, but after level is chosen, it is 
		gameBoard=new String [BOARD_SIZE][BOARD_SIZE];
		initBoard(); //set the board

	}
	
	public void setDepth(int depth)
	{
		this.DEPTH=depth;   //depth is determined by the evell
	}
	
	//at the beginning, the board is empty
	public void initBoard()
	{
		for(int i = 0; i < gameBoard.length; i++)
		{
			for(int j = 0; j < gameBoard.length; j++)
			{
				gameBoard[i][j] = "empty";
			}
		}
	}

	

	
	//in order to send swi-prolog a list of lists, we need it to be written as prolog syntax requires
	public String ToProlog() 
    {
		if (gameBoard == null)
        {
            return "[]";
        }

        StringBuilder boardStringBuilder = new StringBuilder("[");
        
        for (int i = 0; i < 6; i++)
        {
        	boardStringBuilder.append( "[");
            
            for (int j = 0; j <6; j++)
            {
            	boardStringBuilder.append(gameBoard[i][j] + ",");
            }
         
            boardStringBuilder = new StringBuilder(boardStringBuilder.substring(0, boardStringBuilder.length() - 1));
            boardStringBuilder.append("],");
        }
       
        boardStringBuilder = new StringBuilder(boardStringBuilder.substring(0, boardStringBuilder.length() - 1));
       
        boardStringBuilder.append("]");
   ///tostring to turn the stringbuilder to string
        String boardString = boardStringBuilder.toString();
        return boardString;
    	
    }
	
	//the list that we receive from swi-prolog needs to be proccessed so it can be saved as an array in Java.
	
	public void fromProlog(Term listTerm)
	{
		
		//gameBoard = new String[6][6];    
		Term [][] tempBoard= new Term[6][6];
		
    	for(int j=0;j<6;j++)
    	{
    		Term templistTerm=listTerm;

    			for(int k=0;k<j;k++)
    			{
    				templistTerm = templistTerm.arg(2);
    			}
	
    			templistTerm=templistTerm.arg(1);
    		
    			for (int i = 0; i < 6; i++)
    			{
		
    				if(templistTerm.isAtom()!=true)
    				{
    					tempBoard[j][i]=templistTerm.arg(1);
    					templistTerm = templistTerm.arg(2);
    					
    				}
 
    		  }
    	}
    	//at the end we needd to update the game board
    	for(int j=0;j<6;j++)
    	{
    		for(int i=0; i<6;i++)
    		{
    				this.gameBoard[j][i]=String.valueOf(tempBoard[j][i]);	
    		}
    	}
		
    			

	}
	
	//get and set cell to update the board.
	public String getCell(int x, int y)  
    {

            return this.gameBoard[x][y];
 
    }
	
	 public void setCell(int x, int y, String value)
     {
           this.gameBoard[x][y] = value;
     }
	 
	 
	 //the following methods are sent to swi prolog as a query, and than return the answer. 
	 //
	 public void makeMove(int XFrom, int YFrom, int XTo, int YTo, String player, int distance)
	 {
		 ///turn the array to prolog list of lists
		 String currPos=ToProlog();
		//consult hexxNew by swi-prolog
		 String t1 = "consult('src/Hexxagon_Game.pl')";
		Query q1= new Query(t1);
		System.out.println(t1+" "+(q1.hasSolution() ? "succeeded" : "failed"));
		//create a new query
		String t2="makeMove("+currPos+",NewPos,cell("+XFrom+","+YFrom+",_),cell("+XTo+","+YTo+",_),"+player+","+distance+")";
		System.out.println(t2);
		Query q2=new Query(t2);
		Term listTerm = q2.oneSolution().get("NewPos");
		System.out.println(t2+" "+(q1.hasSolution() ? "succeeded" : "failed"));
		//turn the prolog term to array
		fromProlog(listTerm);
	 }
	 
	 public boolean isValidMove(int XFrom, int YFrom, int XTo, int YTo, String player,  int distance)
	 {
			//consult hexxNew by swi-prolog
		 
		 String t1 = "consult('src/Hexxagon_Game.pl')";
			Query q1= new Query(t1);
			System.out.println(t1+" "+(q1.hasSolution() ? "succeeded" : "failed"));
			//create a new query	
		 String t2= "validMove(cell("+XFrom+","+YFrom+","+gameBoard[XFrom][YFrom]+"),cell("+XTo+","+YTo+","+gameBoard[XTo][YTo]+"),Player,"+distance+"). ";
		 System.out.println(t2);
		 Query q2=new Query(t2);
		boolean answer= q2.hasSolution();
		//check if prolog returned true or false
		if(answer)
		{

			 System.out.println("true");
			return true;
		}
		else
		{
			System.out.println("false");
			return false;
		}
	 }

	 public boolean checkForWin(String player)
	 {
		 String pos=ToProlog();
		 String t1 = "consult('src/Hexxagon_Game.pl')";
			Query q1= new Query(t1);
			System.out.println(t1+" "+(q1.hasSolution() ? "succeeded" : "failed"));
		 	
		 String t2= "winningPos("+pos+",WinningPlayer)";
		 System.out.println(t2);
		 Query q2=new Query(t2);
		boolean answer= q2.hasSolution();
		
		if(answer)
		{
			Term term = q2.oneSolution().get("WinningPlayer");  //get the winner
			player=term.toString();   ///convert the prolog term to a string
			return true;
		}
		else
		{
			return false;
		}
 
	 }
	 public void computerMove(int level) 
	 {
		 String type = "first";
		 String t1 = "consult('src/Hexxagon_Game.pl')";
			Query q1= new Query(t1);
			System.out.println(t1+" "+(q1.hasSolution() ? "succeeded" : "failed"));
			
		 String t2 = "alphabeta(Pos,"+
	               ALPHA_MIN+","+BETA_MAX+",GoodPos,Val,"+type+","+level+",max) ";
		 //
		 System.out.println(t2);
		 Query q2=new Query(t2);
		 Term listTerm = q2.oneSolution().get("GoodPos");
		 fromProlog(listTerm);
		
	 }
	 
	 public boolean HasMoves(String player)
	 {
		 String t1 = "consult('src/Hexxagon_Game.pl')";
			Query q1= new Query(t1);
			System.out.println(t1+" "+(q1.hasSolution() ? "succeeded" : "failed"));
			
		 String t2 = "move(Pos,NewPos,"+player+")"; 
		 System.out.println(t2);
		 Query q2= new Query(t2);
		 boolean answer= q2.hasSolution();
			
			if(answer)
			{
				return true;
			}
			else
			{
				return false;
			}
	 }
	     		 
	 public int numOfCells(String player)
     {
		 String pos =  ToProlog();
		
		 String t1 = "consult('src/Hexxagon_Game.pl')";
			Query q1= new Query(t1);
			System.out.println(t1+" "+(q1.hasSolution() ? "succeeded" : "failed"));
			
		 String t2 = "count_instances("+pos+","+player+",Val)"; 
		 System.out.println(t2);
		 Query q2= new Query(t2);
		 Term term = q2.oneSolution().get("Val");
		 return term.intValue();
     }
	 

}
