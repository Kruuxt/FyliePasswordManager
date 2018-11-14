package com.chompcode.fylie.pages.PassMngr;

import com.chompcode.fylie.tools.Page;
import com.chompcode.fylie.tools.PageManager;
import com.chompcode.fylie.tools.PageManager;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;

public class StartPassPage extends Page
{
    public StartPassPage (GridPane root, PageManager pageManager)
    {
        super(root, pageManager);
    }

    @Override
    public void start()
    {
        // == Init Buttons == //
        // Button that opens the password generation page.
        Button generatePass = new Button("Generate New Password");
        generatePass.setOnAction(e -> getPageManager ().open (new GenPassPage(root, getPageManager ())));
        // Button that opens the custom password page.
        Button addPass = new Button("Add Custom Password");
        addPass.setOnAction(e -> getPageManager ().open(new CustomPassPage(root, getPageManager ())));
        // Button that opens the password viewer.
        Button viewPass = new Button("View Passwords");
        viewPass.setOnAction(e -> getPageManager ().open(new ViewPassPage(root, getPageManager ())));

        // Display buttons.
        root.add(generatePass, 0, 0);
        root.add(addPass, 0, 1);
        root.add(viewPass, 0, 2);
    }


}
