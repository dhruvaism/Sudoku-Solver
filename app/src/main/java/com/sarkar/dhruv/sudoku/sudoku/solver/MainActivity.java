package com.sarkar.dhruv.sudoku.sudoku.solver;


import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sarkar.dhruv.sudoku.sudoku.solver.model.GridItemModel;

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {

    NumberPicker numberPicker;
    RelativeLayout solveLayout,cSLayout;
    TextView solveSudoku;
    RecyclerView gridView;
    GridAdapter gridAdapter;
    ArrayList<GridItemModel> gridItems;
    TextView timertext;
    int size = 4;
    int[][] grid;
    final Handler handler = new Handler();
    Timer timer = new Timer();
    long starttime = 0;
    MediaPlayer safe,notSafe;
    Thread solver;
    boolean stop;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        solveLayout = findViewById(R.id.solve_layout);
        cSLayout = findViewById(R.id.default_layout);
        setCreateSudoku();


    }

    public void setCreateSudoku()
    {
        TextView createSudoku = findViewById(R.id.create_sudoku);
        createSudoku.setText("Create Sudoku");
        numberPicker = findViewById(R.id.number_picker);
        final String[] values= {String.valueOf(4),String.valueOf(9), String.valueOf(16)};
        numberPicker.setMinValue(0);
        numberPicker.setMaxValue(values.length-1);
        numberPicker.setDisplayedValues(values);
        numberPicker.setWrapSelectorWheel(true);
        numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal){
                size = Integer.parseInt(values[newVal]);
            }
        });
        createSudoku.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cSLayout.setVisibility(View.GONE);
                solveLayout.setVisibility(View.VISIBLE);
                setSolveSudoku();
            }
        });
    }

    public void setSolveSudoku()
    {
        grid = new int[size][size];
        for(int i=0;i<size;i++)
            for(int j=0;j<size;j++)
                grid[i][j]=0;

        int nn = 5;

        int k = 0;
        while (k<nn){
            Random random = new Random();
            int r = random.nextInt(size);
            int c = random.nextInt(size);
            int num = 1+random.nextInt(size);
            RowCol rowCol = new RowCol();
            rowCol.setCol(c);
            rowCol.setRow(r);
            if(isSafe(rowCol,num)){
                grid[r][c] = num;
            }
            k++;
        }

        gridItems = new ArrayList<>();
        for(int i=0;i<size;i++)
        {
            for(int j=0;j<size;j++)
            {
                gridItems.add(new GridItemModel(i,j,grid[i][j],0));
            }
        }
        solveSudoku = findViewById(R.id.solve_sudoku);
        solveSudoku.setText("Click to Solve");
        solveSudoku.setTextColor(getResources().getColor(R.color.colorPrimary));
        solveSudoku.setEnabled(true);
        solveSudoku.setClickable(true);
        gridView = findViewById(R.id.grid);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this,size);
        gridView.setLayoutManager(gridLayoutManager);
        setAdapter();

        final LinearLayout timerLayout = findViewById(R.id.timer_layout);
        timertext = findViewById(R.id.timer);

        solveSudoku.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timerLayout.setVisibility(View.VISIBLE);
                solveSudoku.setEnabled(false);
                solveSudoku.setClickable(false);
                solveSudoku.setText("Solving...");
                setSolver();
                safe = MediaPlayer.create(getApplicationContext(),R.raw.correct);
            }
        });

    }

    public void setAdapter()
    {
        gridAdapter = new GridAdapter();
        gridView.setAdapter(gridAdapter);
    }



    public void setSolver()
    {

        final Handler h = new Handler(new Handler.Callback() {

            @Override
            public boolean handleMessage(Message msg) {
                long millis = System.currentTimeMillis() - starttime;
                int seconds = (int) (millis / 1000);
                int minutes = seconds / 60;
                seconds     = seconds % 60;

                timertext.setText(String.format("%d:%02d"+ " min", minutes, seconds));
                return false;
            }
        });

        class firstTask extends TimerTask {

            @Override
            public void run() {
                h.sendEmptyMessage(0);
            }
        };


        solver = new Thread(new Runnable() {
            @Override
            public void run() {

                if(!stop){
                    starttime = System.currentTimeMillis();
                    timer = new Timer();
                    timer.schedule(new firstTask(), 0,1000);
                    boolean isSolved;
                    if(solveSudoku())
                    {
                        isSolved = true;
                    }else {
                        isSolved = false;

                    }

                    timer.cancel();
                    timer.purge();

                    final boolean finalIsSolved = isSolved;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if(finalIsSolved){
                                solveSudoku.setText("Solved");
                                solveSudoku.setTextColor(getResources().getColor(R.color.safe));
                                solveSudoku.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.trans_safe)));
                            }else {
                                solveSudoku.setText("Can't be Solved");
                                solveSudoku.setTextColor(getResources().getColor(R.color.not_safe));
                                solveSudoku.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.trans_not_safe)));

                            }
                        }
                    });
                }


            }
        });
        solver.start();
        stop = false;



    }


    public boolean findUnassignedPosition(RowCol rc){
        for(int r=0;r<size;r++)
        {
            for(int c=0;c<size;c++)
            {
                rc.setRow(r);
                rc.setCol(c);
                if(grid[r][c]==0)
                    return true;
            }
        }

        return false;
    }

    public boolean usedInCol(RowCol rc,int num){
        for(int i=0;i<size;i++)
        {
            if(grid[rc.row][i]==num)
                return true;
        }
        return false;

    }
    public boolean usedInRow(RowCol rc,int num){
        for(int i=0;i<size;i++)
        {
            if(grid[i][rc.col]==num)
                return true;
        }
        return false;

    }

    public boolean usedInBlock(RowCol rc,int num)
    {
        int new_n = (int) Math.sqrt(size);
        int start_r = rc.row - rc.row%new_n;
        int start_c = rc.col - rc.col%new_n;
        for(int i=0;i<new_n;i++)
        {
            for(int j=0;j<new_n;j++)
            {
                if(grid[i+start_r][j+start_c]==num)
                    return true;
            }

        }
        return false;
    }

    public boolean isSafe(RowCol rc,int num)
    {
        if(usedInCol(rc,num) || usedInRow(rc,num) || usedInBlock(rc,num))
            return false;
        return true;
    }



    public boolean solveSudoku(){

        if(size==4){
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }else if(size == 9) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }else {
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        RowCol rc = new RowCol();
        if(!findUnassignedPosition(rc))
            return true;

        for(int i=1;i<=size;i++)
        {
            if(isSafe(rc,i))
            {
                grid[rc.row][rc.col] = i;
                gridItems.set(rc.row*size+rc.col,new GridItemModel(rc.row,rc.col, i,2));
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        setAdapter();
                        safe.start();
                    }
                });


                if(solveSudoku())
                    return true;
                grid[rc.row][rc.col]=0;
                gridItems.set(rc.row*size+rc.col,new GridItemModel(rc.row,rc.col, 0,1));
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        setAdapter();
                    }
                });

            }
        }
        return false;
    }



    public static class RowCol{
        public int row,col;
        public void setRow(int r){
            row = r;
        }
        public void setCol(int c){
            col = c;
        }
    }

    public class GridAdapter extends RecyclerView.Adapter<GridAdapter.GridAdapterVH>
    {

        public class GridAdapterVH extends RecyclerView.ViewHolder{

            public TextView noItem;
            public TextView left,right,top,bottom;
            public GridAdapterVH(@NonNull View itemView) {
                super(itemView);
                noItem = itemView.findViewById(R.id.item_no);
                left = itemView.findViewById(R.id.left);
                right = itemView.findViewById(R.id.right);
                top = itemView.findViewById(R.id.top);
                bottom = itemView.findViewById(R.id.bottom);
            }
        }

        @NonNull
        @Override
        public GridAdapterVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_item,parent,false);
            return new GridAdapterVH(view);
        }

        @Override
        public void onBindViewHolder(@NonNull GridAdapterVH holder, int position) {
            holder.noItem.setText(String.valueOf(gridItems.get(position).getValue()));

            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            float s = displayMetrics.widthPixels;
            ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
            ViewGroup.LayoutParams lparams = holder.left.getLayoutParams();
            ViewGroup.LayoutParams rparams = holder.right.getLayoutParams();
            ViewGroup.LayoutParams tparams = holder.top.getLayoutParams();
            ViewGroup.LayoutParams bparams = holder.bottom.getLayoutParams();
            int r = 0;
            if(size == 4){
                r = 5;
            }else if(size == 9){
                r = 10;
            }else if(size == 16){
                r = 17;
            }

            if(r!=0) {
                params.width = (int) s / r;
                params.height = (int) s / r;
            }

            int sqSize = (int)Math.sqrt(size);
            if(gridItems.get(position).getRow()==0){
                tparams.height = 5;
            }
            if(gridItems.get(position).getRow()==size-1 || gridItems.get(position).getRow()%sqSize==sqSize-1){
                bparams.height = 5;
            }
            if(gridItems.get(position).getCol()==0){
                lparams.width = 5;
            }
            if(gridItems.get(position).getCol()==size-1 || gridItems.get(position).getCol()%sqSize==sqSize-1){
                rparams.width = 5;
            }



            int color;
            if(gridItems.get(position).getStatus()==2){
                color = getResources().getColor(R.color.safe);
                holder.itemView.setBackgroundColor(getResources().getColor(R.color.trans_safe));
            }else if(gridItems.get(position).getStatus()==1){
                color = getResources().getColor(R.color.not_safe);
                holder.itemView.setBackgroundColor(getResources().getColor(R.color.trans_not_safe));
            }else {
                color = Color.DKGRAY;
            }

            holder.left.setBackgroundColor(color);
            holder.right.setBackgroundColor(color);
            holder.top.setBackgroundColor(color);
            holder.bottom.setBackgroundColor(color);





        }

        @Override
        public int getItemCount() {
            return gridItems==null?0:gridItems.size();
        }
    }


    public void stop(){
        stop = true;
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();



    }
}
