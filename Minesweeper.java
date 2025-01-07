import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;

//utils class
class Utils {
  Utils(){    
  }

  //creates an array list of cells, essentially the grid with cells.
  ArrayList<ArrayList<Cell>> createCells(int r, int c) {
    ArrayList<Cell> cols = new ArrayList<Cell>();
    ArrayList<ArrayList<Cell>> result = new ArrayList<ArrayList<Cell>>();
    for (int i = 0; i < c; i = i + 1) {
      for (int j = 0; j < r; j = j + 1) {
        cols.add(new Cell(false, false, false));
      }
      result.add(cols);
      cols = new ArrayList<Cell>();
    }
    return result;
  }
}


//represents a cell
class Cell {
  boolean mine; //tells whether a cell has a mine or not.
  boolean flag; //tells whether a cell is flagged or not.
  boolean revealed; //tells whether the cell has been revealed or not.
  ArrayList<Cell> neighbors; //represents the neighbors of the cell, the 8 cells surrounding it

  Cell(boolean mine, boolean flag, boolean revealed) {
    this.mine = mine; 
    this.flag = flag; 
    this.revealed = revealed; 
    this.neighbors = new ArrayList<Cell>(); 

  }

  //finds the value for a cell based on the neighbors which are mines.
  //this means, the number on the cell increases if there are more neighbors that are mines.
  int findVal(ArrayList<Cell> l) {
    int result = 0;
    for (int i = 0; i < l.size(); i = i + 1) {
      if (l.get(i).mine) {
        result = result + 1;
      }
    }
    return result;
  }

  //Effect:
  //reveal cells neigbors when val = 0
  public void revNeighbors() {
    for (Cell c : this.neighbors) {
      if (!c.mine) {
        if (((c.findVal(c.neighbors)) == 0) && !c.revealed) {
          c.revealed = true;
          c.revNeighbors();
        }
        if (!c.revealed) {
          c.revealed = true;   
        }
      }
    }
  }

  //renders the cells
  public WorldImage drawCell() {
    //cell is a mine
    if (this.mine && this.revealed) {
      return new OverlayOffsetAlign(AlignModeX.CENTER, AlignModeY.MIDDLE, 
          new CircleImage(8, OutlineMode.SOLID, Color.BLACK)
          , 1, 1, new RectangleImage(30, 30, OutlineMode.OUTLINE, Color.black));
    }

    //cell has a value (it was revealed and has mines nearby)
    else if (this.revealed) {
      if ((this.findVal(this.neighbors) == 0)) {       
        return new RectangleImage(30, 30, OutlineMode.OUTLINE, Color.DARK_GRAY);
      }
      else {
        return new OverlayOffsetAlign(AlignModeX.CENTER, AlignModeY.MIDDLE, 

            (new TextImage(this.findVal(this.neighbors) + "", 15, FontStyle.BOLD, Color.RED))
            , 1, 1, new RectangleImage(30, 30, OutlineMode.OUTLINE, Color.black));
      }
    }
    //cell has a flag
    else if (this.flag) {     
      return new OverlayOffsetAlign(AlignModeX.CENTER, AlignModeY.MIDDLE, 
          new EquilateralTriangleImage(15, OutlineMode.SOLID, Color.RED)
          , 1, 1, new OverlayOffsetAlign(AlignModeX.CENTER, AlignModeY.MIDDLE, 
              new RectangleImage(30, 30, OutlineMode.OUTLINE, Color.black)
              , 1, 1, new RectangleImage(30, 30, OutlineMode.SOLID, Color.gray)));
    }
    //cell hasn't been clicked
    else {
      return new OverlayOffsetAlign(AlignModeX.CENTER, AlignModeY.MIDDLE, 
          new RectangleImage(30, 30, OutlineMode.OUTLINE, Color.black)
          , 1, 1, new RectangleImage(30, 30, OutlineMode.SOLID, Color.gray));
    }
  }




}

//represents the game state
class Game extends World {
  int rows; //the number of rows on the grid
  int cols; //the number of columns on the grid
  int mines; //the number of mines on the game
  int sec;
  int minutes;
  boolean started;
  ArrayList<ArrayList<Cell>> cells; //represents the grid with cells
  Random r;

  //actual
  Game(int rows, int cols, int mines) {
    this.started = false;
    this.rows = rows;
    this.cols = cols;
    this.mines = mines;
    this.cells = new Utils().createCells(rows, cols);
    this.sec = 0;
    this.minutes = 0; 
    this.r = new Random();
    this.addNeighbors();
    this.placeMines();
    if (this.sec == 60) {
      this.minutes += 1;
      this.sec = 0;
    }
  }

  //tester
  Game(int rows, int cols, int mines, Random r) {
    this.started = false;
    this.rows = rows;
    this.cols = cols;
    this.mines = mines;
    this.sec = 0;
    this.minutes = 0; 
    this.cells = new Utils().createCells(rows, cols);
    this.r = r;
  }

  //creates the scene
  public WorldScene makeScene() {
    WorldScene win = new WorldScene(this.cols * 30, this.rows * 30);
    WorldScene loss = new WorldScene(this.cols * 30, this.rows * 30);
    win.placeImageXY(new TextImage("You Win!", 50, FontStyle.BOLD, Color.GREEN), 
        this.cols * 15, this.rows * 15);
    loss.placeImageXY(new TextImage("You Lose!", 50, FontStyle.BOLD, Color.RED), 
        this.cols * 15, this.rows * 15);
    int revealed = 0;
    int flags = 0;
    WorldScene w = new WorldScene(this.cols * 30, (this.rows + 1) * 30);
    if (this.started) {
      for (ArrayList<Cell> column : cells) {
        int x = this.cells.indexOf(column) * 30 + (30 / 2);
        for (Cell cell : column) {
          int y = column.indexOf(cell) * 30 + (30 / 2);
          w.placeImageXY(cell.drawCell(), x, y);
          if (cell.flag) {
            flags += 1;
          }
          //loss
          if (cell.mine && cell.revealed) {
            this.started = false;
            this.rows = 16;
            this.cols = 30;
            this.cells = new Utils().createCells(this.rows, this.cols);
            return loss;
          }
          if (cell.revealed) {
            revealed += 1;
          }
        }
      }
      w.placeImageXY(new BesideImage(new TextImage((this.mines - flags) + "", 20, 
          FontStyle.BOLD, Color.black), 
          new RectangleImage(8, 20, OutlineMode.SOLID, Color.white), 
          new EquilateralTriangleImage(20, OutlineMode.SOLID, Color.RED)), 
          (this.cols - 1) * 19, (this.rows + 1) * 29);
      w.placeImageXY(new TextImage(this.minutes + ":" + this.sec, 20, 
          FontStyle.BOLD, Color.black), this.cols * 11, (this.rows + 1) * 29);
    }
    else {
      w = new WorldScene(this.cols * 30, (this.rows + 1) * 30);
      w.placeImageXY(new BesideImage(
          new TextImage("Easy", 40, FontStyle.BOLD, Color.black), 
          new RectangleImage(100, 0, OutlineMode.SOLID, Color.white), 
          new TextImage("Medium", 40, FontStyle.BOLD, Color.black),
          new RectangleImage(50, 1, OutlineMode.SOLID, Color.white), 
          new TextImage("Hard", 40, FontStyle.BOLD, Color.black)), this.cols * 13, this.rows * 15);
    }
    //win
    if ((this.rows * this.cols) - revealed <= this.mines) {
      this.started = false;
      this.rows = 16;
      this.cols = 30;
      this.cells = new Utils().createCells(this.rows, this.cols);
      return win;
    }
    return w;
  }
  //

  //onTick for the clock
  @Override
  public void onTick() {
    if (this.started) {
      this.sec = this.sec + 1;
    }
    if (this.sec == 60) {
      this.minutes += 1;
      this.sec = 0;
    }
  }

  //on mouse click method handler
  //Effect:
  @Override
  public void onMouseClicked(Posn pos, String buttonName) {
    //right click: adds flags or removes flags
    if (buttonName.equals("RightButton") && this.started) {
      Cell cellrc = this.cells.get(Math.floorDiv(pos.x, 30)).get(Math.floorDiv(pos.y, 30));
      if (!cellrc.revealed) {
        if (cellrc.flag) {
          cellrc.flag = false;
        }
        else {
          cellrc.flag = true;
        }
      }
    }
    //left click: reveals tiles
    if (buttonName.equals("LeftButton") && this.started) {
      Cell celllc = this.cells.get(Math.floorDiv(pos.x, 30)).get(Math.floorDiv(pos.y, 30));
      if (!celllc.revealed && !celllc.flag) {
        if (celllc.findVal(celllc.neighbors) == 0) {
          celllc.revealed = true;
          celllc.revNeighbors();
        }
        if (celllc.findVal(celllc.neighbors) > 0) {
          celllc.revealed = true;
        }
      }   
    }
    if (buttonName.equals("LeftButton") && !this.started
        && pos.y >= 200 && pos.y <= 280) {
      //medium game
      if (pos.x >= 350 && pos.x <= 510) {
        this.rows = 16;
        this.cols = 18;
        this.mines = 40;
        this.sec = 0;
        this.minutes = 0;
        this.started = true;
        this.cells = new Utils().createCells(rows, cols);
        this.addNeighbors();
        this.testPlaceMines(this.r);
      }
      //easy game
      if (pos.x >= 230 && pos.x <= 320) {
        this.rows = 8;
        this.cols = 10;
        this.mines = 10;
        this.sec = 0;
        this.minutes = 0;
        this.started = true;
        this.cells = new Utils().createCells(rows, cols);
        this.addNeighbors();
        this.testPlaceMines(this.r);
      }
      //hard game
      if (pos.x >= 540 && pos.x <= 640) {
        this.rows = 16;
        this.cols = 30;
        this.mines = 70;
        this.sec = 0;
        this.minutes = 0;
        this.started = true;
        this.cells = new Utils().createCells(rows, cols);
        this.addNeighbors();
        this.testPlaceMines(this.r);
      }
    }
  } 



  //Effect:
  //randomly selects cells from the gamestate and mutates their boolean mine value to true
  void placeMines() {
    Random r = new Random();
    int bound = this.cols * this.rows;
    int val = r.nextInt(bound);
    ArrayList<Integer> values = new ArrayList<Integer>();
    while (values.size() < this.mines) {
      if (!(values.contains(val))) {
        values.add(val);
        this.cells.get((int) Math.ceil(val / this.rows)).get(
            val % this.rows).mine = true; }
      val = r.nextInt(bound);
    }
  }

  //Effect:
  //randomly selects cells from the gamestate and 
  //mutates their boolean mine value to true, for testing.
  void testPlaceMines(Random r) {
    int bound = this.cols * this.rows;
    int val = r.nextInt(bound);
    ArrayList<Integer> values = new ArrayList<Integer>();
    while (values.size() < this.mines) {
      if (!(values.contains(val))) {
        values.add(val);
        this.cells.get((int) Math.ceil(val / this.rows)).get(
            val % this.rows).mine = true; }
      val = r.nextInt(bound);
    }
  }

  //Effect: reveals cells randomly across the grid, 
  //mutates a cell to be have the boolean revealed to true
  //for testing
  void reveal(Random r) {
    int bound = this.cols * this.rows;
    int val = r.nextInt(bound);
    ArrayList<Integer> values = new ArrayList<Integer>();
    while (values.size() < (bound / 2)) {
      if (!(values.contains(val))) {
        values.add(val);
        this.cells.get((int) Math.ceil(val / this.rows)).get(
            val % this.rows).revealed = true; }
      val = r.nextInt(bound);
    }
  }

  //Effect: flags cells randomly across the grid, 
  //mutates a cell to be have the boolean flag to true
  //for testing
  void flags(Random r) {
    int bound = this.cols * this.rows;
    int val = r.nextInt(bound);
    ArrayList<Integer> values = new ArrayList<Integer>();
    while (values.size() < (bound / 4)) {
      if (!(values.contains(val))) {
        values.add(val);
        this.cells.get((int) Math.ceil(val / this.rows)).get(
            val % this.rows).flag = true; }
      val = r.nextInt(bound);
    }
  }

  //Effect:
  //mutates the neighbors on each cell on the grid according to its index
  void addNeighbors() {
    for (int i = 0; i < this.cols; i = i + 1) {
      for (int j = 0; j < this.rows; j = j + 1) {
        // left hand side
        if ((i - 1) >= 0) {
          this.cells.get(i).get(j).neighbors.add(
              this.cells.get(i - 1).get(j));
          if ((j - 1) >= 0) {
            this.cells.get(i).get(j).neighbors.add(
                this.cells.get(i - 1).get(j - 1));
          }
          if (j + 1 < this.rows) {
            this.cells.get(i).get(j).neighbors.add(
                this.cells.get(i - 1).get(j + 1));
          }
        }
        // right hand side
        if ((i + 1) < this.cols) {
          this.cells.get(i).get(j).neighbors.add(
              this.cells.get(i + 1).get(j));
          if ((j - 1) >= 0) {
            this.cells.get(i).get(j).neighbors.add(
                this.cells.get(i + 1).get(j - 1));
          }
          if (j + 1 < this.rows) {
            this.cells.get(i).get(j).neighbors.add(
                this.cells.get(i + 1).get(j + 1));
          }
        }
        //up and down
        if ((j - 1) >= 0) {
          this.cells.get(i).get(j).neighbors.add(
              this.cells.get(i).get(j - 1));
        }
        if (j + 1 < this.rows) {
          this.cells.get(i).get(j).neighbors.add(
              this.cells.get(i).get(j + 1));
        }
      }
    }
  }
}



//example class
class ExamplesGame {
  Game testGame;
  Game smallTestGame;
  WorldScene w1;
  WorldScene w2;
  WorldScene w3;
  WorldScene w4;

  //initialize the data
  void initData() {
    this.testGame = new Game(16, 30, 70);
    this.smallTestGame = new Game(3, 3, 2, new Random(4));
    this.smallTestGame.started = true;

  }

  //GAME DEMO!:
  //Just run the code and select the difficulty
  //Hard is the 30x16 grid
  void testBigBang(Tester t) {
    this.initData();
    this.testGame.bigBang(910, 510, 1);
  }

  //tests for findVal
  void testFindVal(Tester t) {
    Cell c1 = new Cell(true, false, false);
    Cell c2 = new Cell(false, false, false);
    Cell c3 = new Cell(false, false, false);
    Cell c4 = new Cell(true, false, false);
    Cell c5 = new Cell(true, false, false); 
    ArrayList<Cell> neighbors = new ArrayList<Cell>();
    neighbors.add(c1);
    neighbors.add(c2);
    neighbors.add(c3);
    neighbors.add(c4);
    neighbors.add(c5);
    t.checkExpect(c1.findVal(neighbors), 3);
    neighbors.clear();
    t.checkExpect(c1.findVal(neighbors), 0);
  }

  //tests for DrawCell
  void testDrawCell(Tester t) {
    Cell c1 = new Cell(true, false, true);
    Cell c2 = new Cell(false, false, true);
    Cell c5 = new Cell(false, false, true);
    Cell c3 = new Cell(false, true, false);
    Cell c4 = new Cell(false, false, false);
    ArrayList<Cell> neighbors = new ArrayList<Cell>();
    neighbors.add(c1);
    neighbors.add(c2);
    neighbors.add(c3);
    neighbors.add(c4);
    neighbors.add(c5);
    c5.neighbors = neighbors;
    t.checkExpect(c1.drawCell(), new OverlayOffsetAlign(AlignModeX.CENTER, AlignModeY.MIDDLE, 
        new CircleImage(8, OutlineMode.SOLID, Color.BLACK)
        , 1, 1, new RectangleImage(30, 30, OutlineMode.OUTLINE, Color.black)));
    t.checkExpect(c2.drawCell(), new RectangleImage(30, 30, OutlineMode.OUTLINE, Color.DARK_GRAY));
    t.checkExpect(c3.drawCell(), new OverlayOffsetAlign(AlignModeX.CENTER, AlignModeY.MIDDLE, 
        new EquilateralTriangleImage(15, OutlineMode.SOLID, Color.RED)
        , 1, 1, new OverlayOffsetAlign(AlignModeX.CENTER, AlignModeY.MIDDLE, 
            new RectangleImage(30, 30, OutlineMode.OUTLINE, Color.black)
            , 1, 1, new RectangleImage(30, 30, OutlineMode.SOLID, Color.gray))));
    t.checkExpect(c4.drawCell(), new OverlayOffsetAlign(AlignModeX.CENTER, AlignModeY.MIDDLE, 
        new RectangleImage(30, 30, OutlineMode.OUTLINE, Color.black)
        , 1, 1, new RectangleImage(30, 30, OutlineMode.SOLID, Color.gray)));
    t.checkExpect(c5.drawCell(), new OverlayOffsetAlign(AlignModeX.CENTER, AlignModeY.MIDDLE, 
        (new TextImage(1 + "", 15, FontStyle.BOLD, Color.RED))
        , 1, 1, new RectangleImage(30, 30, OutlineMode.OUTLINE, Color.black)));
  }

  //tests for PlaceMines
  void testPlaceMines(Tester t) {
    initData();
    this.smallTestGame.testPlaceMines(this.smallTestGame.r);
    //places a mine in the correct places
    t.checkExpect(this.smallTestGame.cells.get(2).get(1).mine, true);
    t.checkExpect(this.smallTestGame.cells.get(2).get(2).mine, true);
    //dosnt place extra mines
    t.checkExpect(this.smallTestGame.cells.get(0).get(0).mine, false);
    t.checkExpect(this.smallTestGame.cells.get(1).get(1).mine, false);
    t.checkExpect(this.smallTestGame.cells.get(0).get(1).mine, false);
    t.checkExpect(this.smallTestGame.cells.get(1).get(0).mine, false);
    t.checkExpect(this.smallTestGame.cells.get(0).get(2).mine, false);
    t.checkExpect(this.smallTestGame.cells.get(2).get(0).mine, false);
    t.checkExpect(this.smallTestGame.cells.get(1).get(2).mine, false);

  }

  //tests for create cells
  void testCreateCells(Tester t) {
    Cell empty = new Cell(false, false, false);
    t.checkExpect(new Utils().createCells(0, 0), new ArrayList<ArrayList<Cell>>());
    ArrayList<Cell> cols1 = new ArrayList<Cell>(Arrays.asList(empty, empty));
    ArrayList<Cell> cols2 = new ArrayList<Cell>(Arrays.asList(empty, empty));
    ArrayList<Cell> cols3 = new ArrayList<Cell>(Arrays.asList(empty, empty));
    t.checkExpect(new Utils().createCells(2, 3), 
        new ArrayList<ArrayList<Cell>>(Arrays.asList(cols1, cols2, cols3)));         
  }

  //tests for addNeighbors
  void testAddNeighbors(Tester t) {
    this.initData();
    this.smallTestGame.testPlaceMines(this.smallTestGame.r);
    this.smallTestGame.addNeighbors();
    ArrayList<Cell> neighborsTopLeft = new ArrayList<Cell>(Arrays.asList(
        this.smallTestGame.cells.get(1).get(0),
        this.smallTestGame.cells.get(1).get(1),
        this.smallTestGame.cells.get(0).get(1)));

    ArrayList<Cell> neighborsBottomRight = new ArrayList<Cell>(Arrays.asList(
        this.smallTestGame.cells.get(1).get(2),
        this.smallTestGame.cells.get(1).get(1),
        this.smallTestGame.cells.get(2).get(1)));

    ArrayList<Cell> neighborsCenter = new ArrayList<Cell>(Arrays.asList(
        this.smallTestGame.cells.get(0).get(1),
        this.smallTestGame.cells.get(0).get(0),
        this.smallTestGame.cells.get(0).get(2),
        this.smallTestGame.cells.get(2).get(1),
        this.smallTestGame.cells.get(2).get(0),
        this.smallTestGame.cells.get(2).get(2),
        this.smallTestGame.cells.get(1).get(0),
        this.smallTestGame.cells.get(1).get(2)));

    ArrayList<Cell> neighborsLeft = new ArrayList<Cell>(Arrays.asList(
        this.smallTestGame.cells.get(1).get(1),
        this.smallTestGame.cells.get(1).get(0),
        this.smallTestGame.cells.get(1).get(2),
        this.smallTestGame.cells.get(0).get(0),
        this.smallTestGame.cells.get(0).get(2)      
        ));

    ArrayList<Cell> neighborsRight = new ArrayList<Cell>(Arrays.asList(
        this.smallTestGame.cells.get(1).get(1),
        this.smallTestGame.cells.get(1).get(0),
        this.smallTestGame.cells.get(1).get(2),
        this.smallTestGame.cells.get(2).get(0),
        this.smallTestGame.cells.get(2).get(2)      
        ));

    t.checkExpect(this.smallTestGame.cells.get(0).get(0).neighbors, neighborsTopLeft);
    t.checkExpect(this.smallTestGame.cells.get(2).get(2).neighbors, neighborsBottomRight);
    t.checkExpect(this.smallTestGame.cells.get(1).get(1).neighbors, neighborsCenter);
    t.checkExpect(this.smallTestGame.cells.get(0).get(1).neighbors, neighborsLeft);
    t.checkExpect(this.smallTestGame.cells.get(2).get(1).neighbors, neighborsRight);
  }

  //tests for on click
  void testOnMouseClicked(Tester t) {
    this.initData();
    this.smallTestGame.testPlaceMines(this.smallTestGame.r);
    this.smallTestGame.addNeighbors();
    this.smallTestGame.onMouseClicked(new Posn(15, 15), "RightButton");
    //checks if the top left corner cell was clicked on and altered
    t.checkExpect(this.smallTestGame.cells.get(0).get(0).flag, true);
    //reveals the cell
    this.smallTestGame.onMouseClicked(new Posn(50, 15), "LeftButton");
    t.checkExpect(this.smallTestGame.cells.get(1).get(0).revealed, true);
    //doesnt reveal the neighbors if the cell has a value > 0
    t.checkExpect(this.smallTestGame.cells.get(0).get(0).revealed, false);
    //checks if it reveals a cell
    this.smallTestGame.onMouseClicked(new Posn(15, 45), "LeftButton");
    t.checkExpect(this.smallTestGame.cells.get(0).get(1).revealed, true);
    //check if it reveals neighbors
    t.checkExpect(this.smallTestGame.cells.get(0).get(0).revealed, true);
    t.checkExpect(this.smallTestGame.cells.get(1).get(1).revealed, true);
    t.checkExpect(this.smallTestGame.cells.get(2).get(2).revealed, false);
    //turn the game started to false
    this.smallTestGame.started = false;
    //check to see there is not a new game if the board is randomly clicked
    this.smallTestGame.onMouseClicked(new Posn(10, 10), "LeftButton");
    t.checkExpect(this.smallTestGame.started, false);
    //check to see if it starts an easy game
    this.smallTestGame.onMouseClicked(new Posn(240, 240), "LeftButton");
    t.checkExpect(this.smallTestGame.cols, 10);
    t.checkExpect(this.smallTestGame.rows, 8);
    t.checkExpect(this.smallTestGame.mines, 10);
    t.checkExpect(this.smallTestGame.minutes, 0);
    t.checkExpect(this.smallTestGame.sec, 0);
    //end the game again
    this.smallTestGame.started = false;
    //check to see if it starts a Medium game
    this.smallTestGame.onMouseClicked(new Posn(400, 245), "LeftButton");
    t.checkExpect(this.smallTestGame.cols, 18);
    t.checkExpect(this.smallTestGame.rows, 16);
    t.checkExpect(this.smallTestGame.mines, 40);
    t.checkExpect(this.smallTestGame.minutes, 0);
    t.checkExpect(this.smallTestGame.sec, 0);
    t.checkExpect(this.smallTestGame.started, true);
    //end the game again
    this.smallTestGame.started = false;
    //check to see if it starts a Hard game
    this.smallTestGame.onMouseClicked(new Posn(600, 245), "LeftButton");
    t.checkExpect(this.smallTestGame.cols, 30);
    t.checkExpect(this.smallTestGame.rows, 16);
    t.checkExpect(this.smallTestGame.mines, 70);
    t.checkExpect(this.smallTestGame.minutes, 0);
    t.checkExpect(this.smallTestGame.sec, 0);
    t.checkExpect(this.smallTestGame.started, true);
  }

  //tests for revNeighbors
  void testRevNeighbors(Tester t) {
    Cell c1 = new Cell(false, false, false);
    Cell c2 = new Cell(false, false, true);
    Cell c3 = new Cell(false, false, false);
    Cell c4 = new Cell(false, false, false);
    Cell c5 = new Cell(true, false, false);
    Cell c6 = new Cell(false, false, false);
    Cell c7 = new Cell(false, false, true);
    c2.neighbors.add(c1);
    c4.neighbors.add(c5);
    c4.neighbors.add(c3);
    c4.neighbors.add(c7);
    c2.neighbors.add(c6);
    c1.neighbors.add(c4);
    c2.revNeighbors();
    //reveals the neighbors of a cell
    t.checkExpect(c1.revealed, true);
    //calls it recursively if there is not a mine near the cell
    t.checkExpect(c4.revealed, true);
    t.checkExpect(c6.revealed, true);
    //doesnt call it recursively if there is a nearby mine
    t.checkExpect(c3.revealed, false);
    //wont reveal a mine
    t.checkExpect(c5.revealed, false);
    //wont unreveal something
    t.checkExpect(c7.revealed, true);
  }

  //tests for on tick
  void testOnTick(Tester t) {
    this.initData();
    //0 at start
    t.checkExpect(this.smallTestGame.minutes, 0);
    t.checkExpect(this.smallTestGame.sec, 0);
    this.smallTestGame.onTick();
    //adds a second
    t.checkExpect(this.smallTestGame.minutes, 0);
    t.checkExpect(this.smallTestGame.sec, 1);
    this.smallTestGame.sec = 59;
    this.smallTestGame.onTick();
    //goes 1 minute 0 seconds
    t.checkExpect(this.smallTestGame.minutes, 1);
    t.checkExpect(this.smallTestGame.sec, 0);
    this.smallTestGame.sec = 34;
    this.smallTestGame.minutes = 6;
    //test more numbers
    t.checkExpect(this.smallTestGame.minutes, 6);
    t.checkExpect(this.smallTestGame.sec, 34);     
  }  


  //tests for make scene
  void testMakeScene(Tester t) {
    this.initData();
    //test for the selection of difficulty screen
    this.smallTestGame.started = false;

    this.w1 = new WorldScene(90, 120);
    w1.placeImageXY(new BesideImage(
        new TextImage("Easy", 40, FontStyle.BOLD, Color.black), 
        new RectangleImage(100, 0, OutlineMode.SOLID, Color.white), 
        new TextImage("Medium", 40, FontStyle.BOLD, Color.black),
        new RectangleImage(50, 1, OutlineMode.SOLID, Color.white), 
        new TextImage("Hard", 40, FontStyle.BOLD, Color.black)), 39, 45);
    t.checkExpect(this.smallTestGame.makeScene(), w1);   

    this.smallTestGame.started = true;

    this.smallTestGame.cells.get(0).get(0).mine = true;
    this.smallTestGame.cells.get(0).get(1).revealed = true;
    this.smallTestGame.cells.get(0).get(2).revealed = true;
    this.smallTestGame.cells.get(1).get(0).revealed = true;
    this.smallTestGame.cells.get(1).get(1).revealed = true;
    this.smallTestGame.cells.get(1).get(2).revealed = true;
    this.smallTestGame.cells.get(2).get(0).revealed = true;
    this.smallTestGame.cells.get(2).get(1).revealed = true;
    this.smallTestGame.cells.get(2).get(2).revealed = true;

    //test win
    this.w2 = new WorldScene(90, 90);
    w2.placeImageXY(new TextImage("You Win!", 50, FontStyle.BOLD, Color.GREEN), 45, 45);
    t.checkExpect(this.smallTestGame.makeScene(), w2);  

    //restart the game
    //test loss
    this.initData();
    this.smallTestGame.cells.get(0).get(0).mine = true;
    this.smallTestGame.cells.get(0).get(0).revealed = true;
    this.w3 = new WorldScene(90, 90);
    w3.placeImageXY(new TextImage("You Lose!", 50, FontStyle.BOLD, Color.RED), 45, 45);
    t.checkExpect(this.smallTestGame.makeScene(), w3);

    //restart the data
    this.initData();
    this.w4 = new WorldScene(90, 120);
    //add the mines
    this.smallTestGame.cells.get(0).get(0).mine = true;
    this.smallTestGame.cells.get(0).get(1).mine = true; 
    this.smallTestGame.cells.get(0).get(2).revealed = true;//show number 1
    this.smallTestGame.cells.get(1).get(0).flag = true; //flagged
    this.smallTestGame.cells.get(2).get(1).revealed = true;//revealed with no value
    this.smallTestGame.cells.get(2).get(2).revealed = false;//normal blank cell

    //create what the grid should look like
    //tests the position placements and the application of drawCell()
    w4.placeImageXY(this.smallTestGame.cells.get(0).get(0).drawCell(), 15, 15);
    w4.placeImageXY(this.smallTestGame.cells.get(0).get(1).drawCell(), 15, 45);
    w4.placeImageXY(this.smallTestGame.cells.get(0).get(2).drawCell(), 15, 75);
    w4.placeImageXY(this.smallTestGame.cells.get(1).get(0).drawCell(), 45, 15);
    w4.placeImageXY(this.smallTestGame.cells.get(1).get(1).drawCell(), 45, 45);
    w4.placeImageXY(this.smallTestGame.cells.get(1).get(2).drawCell(), 45, 75);
    w4.placeImageXY(this.smallTestGame.cells.get(2).get(0).drawCell(), 75, 15);
    w4.placeImageXY(this.smallTestGame.cells.get(2).get(1).drawCell(), 75, 45);
    w4.placeImageXY(this.smallTestGame.cells.get(2).get(2).drawCell(), 75, 75);
    w4.placeImageXY(new BesideImage(new TextImage(1 + "", 20, 
        FontStyle.BOLD, Color.black), 
        new RectangleImage(8, 20, OutlineMode.SOLID, Color.white), 
        new EquilateralTriangleImage(20, OutlineMode.SOLID, Color.RED)), 
        (this.smallTestGame.cols - 1) * 19, (this.smallTestGame.rows + 1) * 29);
    w4.placeImageXY(new TextImage(this.smallTestGame.minutes + ":" + this.smallTestGame.sec, 20, 
        FontStyle.BOLD, Color.black), 33, (this.smallTestGame.rows + 1) * 29);

    //test
    t.checkExpect(this.smallTestGame.makeScene(), w4);
  }
}








