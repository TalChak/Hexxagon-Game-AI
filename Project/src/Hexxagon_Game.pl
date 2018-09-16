
% alpha beta algorithm, search depth is accordinig to the level that was
% chosen by the player
alphabeta( Pos, Alpha, Beta, GoodPos, Val, Depth,Turn)  :-
  Depth > 0,
  turn_to_player(Turn,Player),
  moves(Pos, PosList, Player), !,
  boundedbest( PosList, Alpha, Beta, GoodPos, Val, Depth,  Turn);
  staticval( Pos, Val ).        % Static value of Pos

boundedbest( [Pos | PosList], Alpha, Beta, GoodPos, GoodVal, Depth,  Turn)  :-
  NewDepth is (Depth - 1),
  switch_turn(Turn,NewTurn),
  alphabeta( Pos, Alpha, Beta, _, Val,NewDepth,NewTurn),
  goodenough( PosList, Alpha, Beta, Pos, Val, GoodPos, GoodVal,  Depth , Turn).

goodenough( [], _, _, Pos, Val, Pos, Val,_,_)  :-  !.    % No other optional cells
goodenough( _, Alpha, Beta, Pos, Val, Pos, Val,_,Turn)  :-
  min_to_move(Turn), Val > Beta, !                   % Maximizer attained upper bound
  ;
  max_to_move(Turn), Val < Alpha, !.                 % Minimizer attained lower bound

goodenough( PosList, Alpha, Beta, Pos, Val, GoodPos, GoodVal, Depth,  Turn)  :-
  newbounds( Alpha, Beta, Pos, Val, NewAlpha, NewBeta, Turn),    % Refine bounds
  boundedbest( PosList, NewAlpha, NewBeta, Pos1, Val1,  Depth, Turn),
  betterof( Pos, Val, Pos1, Val1, GoodPos, GoodVal, Turn).

newbounds( Alpha, Beta, _, Val, Val, Beta,Turn)  :-
  min_to_move(Turn), Val > Alpha, !.                 % Maximizer increased lower bound
newbounds( Alpha, Beta, _, Val, Alpha, Val,Turn)  :-
   max_to_move(Turn), Val < Beta, !.                 % Minimizer decreased upper bound

newbounds( Alpha, Beta, _, _, Alpha, Beta,_).          % Otherwise bounds unchanged

betterof( Pos, Val, _, Val1, Pos, Val, Turn)  :-        % Pos better than Pos1
  min_to_move(Turn), Val > Val1, !
  ;
  max_to_move(Turn), Val < Val1, !.

betterof( _, _, Pos1, Val1, Pos1, Val1,_).             % Otherwise Pos1 better

% Returns the current players' color (max is blue, min
% is red).
turn_to_player(max,blue).
turn_to_player(min,red).

% determine what is the current move.
min_to_move(min).
max_to_move(max).

% change turns
switch_turn(min,max).




% huristic function- value is the number of red cells minus the number
% of blue cells.
staticval(Pos,Val) :- count_instances(Pos,red,NumOfReds),
                           count_instances(Pos,blue,NumOfBlues),
                           Val is (NumOfReds - NumOfBlues).


% Check if the cell (CellX,CellY) is a safe cell for Player to move.
safe_cell(Board,CellX,CellY,Player) :- safe_cell(Board,CellX,CellY,Player,0,0).

%this row is not optional.
safe_cell([_|OtherLists],CellX,CellY,Player,IndexX,IndexY) :-
          IndexX < CellX,
          NewIndexX is (IndexX + 1),
          safe_cell(OtherLists,CellX,CellY,Player,NewIndexX,IndexY).

% an optional row, keep moving toward to cell (CellX,CellY).
safe_cell([[_|TailFirst] | TailOthers],CellX,CellY,Player,IndexX,IndexY) :-
         IndexX =:= CellX,
         IndexY < CellY,
         NewIndexY is (IndexY + 1),
         safe_cell([TailFirst | TailOthers],CellX,CellY,Player,IndexX,NewIndexY).


%FirstItem is the cell (CellX,CellY) - Check if the cell is safe.
safe_cell([[FirstItem|_] | _],CellX,CellY,Player,IndexX,IndexY) :-
         IndexX =:= CellX,
         IndexY =:= CellY,
         (FirstItem = Player ; FirstItem = invalid).

% return all possible moves for a given board position and a specific
% player
moves(Pos, PosList, Player) :-
                       retractall(scanned(_,_,_,_)),
                       findall(NewPos,move(Pos,NewPos,Player),PosList),PosList \== []. % Goal fails if no moves found

% Get an optional cell that the player can move to and paint the two
% cells, distance is 1 in that case
get_optional_cell(XFrom,YFrom,XTo,YTo) :-
              adjacentCell(XFrom,YFrom,XTo,YTo),
              not(scanned(XFrom,YFrom,XTo,YTo)),
              assert(scanned(XFrom,YFrom,XTo,YTo)).
% get an optional cell that the player can move to, without painting
% the origin cell. in that case, distance is 2
get_optional_cell(XFrom,YFrom,XTo,YTo) :-
              adjacentCell(XFrom,YFrom,MidX,MidY),
              adjacentCell(MidX,MidY,XTo,YTo),
              not(adjacentCell(XFrom,YFrom,XTo,YTo)),
              not(scanned(XFrom,YFrom,XTo,YTo)),
              assert(scanned(XFrom,YFrom,XTo,YTo)).

% find a valid game move from original position for the player, and
% returns the new board position
move(Pos,NewPos,Player) :-
	boardcell(cell(XFrom,YFrom,Player),Pos),get_optional_cell(XFrom,YFrom,TargetX,TargetY),check_cell(Pos,TargetX,TargetY,empty),
	validMove(cell(XFrom,YFrom,Player),cell(TargetX,TargetY,empty),Player,Distance),
	makeMove(Pos,NewPos,cell(XFrom,YFrom,Player),cell(TargetX,TargetY,empty),Player,Distance).

% return a cell as a term cell(X,Y) from board
boardcell(Cell,Board) :-
	boardRow(RowNum,0,Row,Board),
	boardCol(ColNum,0,Value,Row),
	Cell = cell(RowNum,ColNum,Value).

%returns a row and its X coordinate from Board
boardRow(CurrRowNum,CurrRowNum,CurrRow,[CurrRow|_]).
boardRow(RowNum,CurrRowNum,NextRow,[_|OtherRows]) :-
    NextRowNum is CurrRowNum+1,boardRow(RowNum,NextRowNum,NextRow,OtherRows).

%return the Y coordinate of a valid column in Row
boardCol(CurrColNum,CurrColNum,Value,[Value|_]). % Column list not empty, return current column
boardCol(ColNum,CurrColNum,Value,[_|OtherCols]) :-
	NextColNum is CurrColNum+1,boardCol(ColNum,NextColNum,Value,OtherCols).

% valid move is a move to a cell with the same color of the player, and
% the distance from the target cell is either 1 or 2
validMove(cell(_,_,SourceValue),cell(_,_,TargetValue),Player,Distance) :-
	(SourceValue == red ; SourceValue == blue),Player = SourceValue, TargetValue == empty,Distance==1,! ;Distance==2.


%all possible six neighbors
adjacentCell(Cell1X,Cell1Y,Cell2X,Cell2Y) :-
	((Cell2Y is Cell1Y,(Cell2X is Cell1X+1 ; Cell2X is Cell1X-1)); % Case 1
	(Cell2X is Cell1X,(Cell2Y is Cell1Y+1 ; Cell2Y is Cell1Y-1)); % Case 2
	((Cell2X is Cell1X-1,(Cell2Y is Cell1Y+1 ; Cell2Y is Cell1Y-1));
	(Cell2X is Cell1X+1,(Cell2Y is Cell1Y+1 ; Cell2Y is Cell1Y-1)))). % Case 3

% returns the other players' color
otherPlayer(blue,red).
otherPlayer(red,blue).

% we can go to makemove after we know the move is valid.
% if the distance is 1, than change the cell to the players' color,
% else, move and keep the cell empty.
% return the new board position
makeMove(Pos,NewPos,cell(XFrom,YFrom,_),cell(TargetX,TargetY,_),Player,Distance) :-
	setCell(Pos,TargetX,TargetY,Player,CopiedPos),(Distance is 2,!, setCell(CopiedPos,XFrom,YFrom,empty,MovedPos);MovedPos = CopiedPos),
	otherPlayer(Player,OtherPlayer),
	findall(cell(CellX,CellY),(adjacentCell(TargetX,TargetY,CellX,CellY),check_cell(MovedPos,CellX,CellY,Value),Value == OtherPlayer),Capturecells),
	setCells(MovedPos,Capturecells,Player,NewPos).

% change the value of all cells in cells on board Board to Value and
% return NewBoard
setCells(Board,[cell(CellX,CellY)|Othercells],Value,NewBoard) :-
	setCells(Board,Othercells,Value,TempBoard),setCell(TempBoard,CellX,CellY,Value,NewBoard).
setCells(Board,[],_,Board).

% change the specific cell to Value and return
%the new board position
%
setCell(Board,CellX,CellY,Value,NewBoard) :-
	setcellRow(Board,CellX,0,CellY,Value,NewBoard).

%ued by setcell() to iterate row by row to create the new board
setcellRow([CurrRow|OtherRows],CellX,CurrX,CellY,Value,[NewRow|OtherNewRows]) :-
	NextX is CurrX+1,setcellRow(OtherRows,CellX,NextX,CellY,Value,OtherNewRows),setcellCol(CurrRow,CellX,CurrX,CellY,0,Value,NewRow).
setcellRow([],_,_,_,_,[]).

% used by setcellRow() to iterate column by column to create a new row.
setcellCol([CurrCol|OtherCols],CellX,CurrX,CellY,CurrY,Value,[NewCol|OtherNewCols]) :-
	NextY is CurrY+1,setcellCol(OtherCols,CellX,CurrX,CellY,NextY,Value,OtherNewCols),
	(CurrX =:= CellX, CurrY =:= CellY, !, NewCol = Value ;
	NewCol = CurrCol).

setcellCol([],_,_,_,_,_,[]).

% check if the cell (CellX,CellY) holds the item 'Item'.
%
check_cell(Board,CellX,CellY,Item) :- check_cell(Board,CellX,CellY,Item,0,0).

check_cell([_|OtherLists],CellX,CellY,Item,IndexX,IndexY) :-
          IndexX < CellX,
          NewIndexX is (IndexX + 1),
          check_cell(OtherLists,CellX,CellY,Item,NewIndexX,IndexY).

check_cell([[_|TailFirst] | TailOthers],CellX,CellY,Item,IndexX,IndexY) :-
         IndexX =:= CellX,
         IndexY < CellY,
         NewIndexY is (IndexY + 1),
         check_cell([TailFirst | TailOthers],CellX,CellY,Item,IndexX,NewIndexY).

% Item is the cell (CellX,CellY).
check_cell([[Item|_] | _],CellX,CellY,Item,IndexX,IndexY) :-
         IndexX =:= CellX,
         IndexY =:= CellY.

%game is over when the board is full of red and blue cells
% postion is a winning position for player WinningPlayer if he has more
% points (he filled more cells)%
%checkpos check if there is no empty cell
%
%
winningPos(Pos,WinningPlayer):-
    check_pos(Pos), winningPlayer(Pos,WinningPlayer).
check_pos(Pos):- \+member2(empty,Pos).
member2(X, [X|_]).                 %X is first element
member2(X, [L|_]) :- member(X, L). %X is member of first element
member2(X, [_|T]) :- member(X, T). %X is member of tail
winningPlayer(Pos,WinningPlayer):- retractall(scanned(_,_,_,_)),count_instances(Pos,red,CounterReds),count_instances(Pos,blue,CounterBlues),
	(CounterReds>CounterBlues,!,WinningPlayer=red;CounterReds<CounterBlues,!,WinningPlayer=blue;WinningPlayer=tie).

%Count the number of instances of an item in a given board (Pos).
count_instances(Pos,Item,Val) :- count_instances(Pos,Item,Val,0).

%he current cell contains Item.
count_instances([[Item|TailFirst] | TailOthers],Item,Val,Counter) :- ! ,
                 NewCounter is (Counter+ 1),
                 count_instances([TailFirst | TailOthers],Item,Val,NewCounter).

% current cell does't contain Item.
count_instances([[_|TailFirst] | TailOthers],Item,Val,Counter) :- ! ,
                 count_instances([TailFirst | TailOthers],Item,Val,Counter).

% end of the row, continue to the next row.
count_instances([[]|TailOthers],Item,Val,Counter) :- ! ,
                 count_instances(TailOthers,Item,Val,Counter).

% the whole board has been scanned.
count_instances([],_,Counter,Counter).
