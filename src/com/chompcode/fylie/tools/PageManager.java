package com.chompcode.fylie.tools;

import javafx.scene.layout.GridPane;

import java.util.ArrayList;

public class PageManager
{

    public GridPane root;
    Page currentPage;

    public PageManager () {}

    public PageManager(GridPane root){
        this.root = root;
    }

    public void open(Page page){
        clear();
        this.setCurrentPage (page);
        page.start();
    }

    public void clear()
    {
        root.getChildren().clear();
        this.currentPage = null;
    }

    public Page getCurrentPage ()
    {
        return this.currentPage;
    }

    public void setCurrentPage (Page page)
    {
        this.currentPage = page;
    }
}
