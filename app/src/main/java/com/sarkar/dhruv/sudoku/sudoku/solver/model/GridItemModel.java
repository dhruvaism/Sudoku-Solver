package com.sarkar.dhruv.sudoku.sudoku.solver.model;

public class GridItemModel {

    int row,col,value,status;

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public int getValue() {
        return value;
    }

    public int getStatus() {
        return status;
    }

    public GridItemModel(int row, int col, int value, int status) {
        this.row = row;
        this.col = col;
        this.value = value;
        this.status = status;
    }
}
