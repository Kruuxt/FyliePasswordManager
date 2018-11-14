package com.chompcode.fylie.tools;

import javafx.scene.layout.GridPane;

public abstract class Page
{
    public GridPane root;
    PageManager pageManager;

    public Page (GridPane root, PageManager pageManager)
    {
        this.root = root;
        this.pageManager = pageManager;
    }

    public abstract void  start ();

    public PageManager getPageManager ()
    {
        return this.pageManager;
    }
}
