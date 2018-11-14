package com.chompcode.fylie.pages.PassMngr;

import com.chompcode.fylie.tools.PageManager;
import com.chompcode.fylie.apps.passmngr.PasswordManager;
import com.chompcode.fylie.tools.Page;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;

public class CustomPassPage extends Page {

    public CustomPassPage (GridPane root, PageManager pageManager)
    {
        super(root, pageManager);
    }

    @Override
    public void start()
    {
        //Text at the bottom of the page to display relevant information. (i.e. missing password, missing website, password stored).
        Text userMessage = new Text ();

        // == Init TextFields/Labels == //
        // Field for inserting a web address.
        Label websiteLabel = new Label ("Website URL: ");
        TextField websiteEntry = new TextField ();
        // Field for inserting a web address.
        Label usernameLabel = new Label ("Username: ");
        TextField usernameEntry = new TextField ();
        // Field for inserting a password.
        Label passLabel = new Label ("Password: ");
        TextField password = new TextField ();

        // Button to save the password to SQL DB.
        Button savePass = new Button ("Store password");
        savePass.setOnAction (e -> PasswordManager.validatePasswordInput (websiteEntry, password, usernameEntry, userMessage));

        // Back button to return to startup page.
        Button back = new Button ("Back");
        back.setOnAction (e -> getPageManager ().open(new StartPassPage(root, getPageManager())));

        // Displaying all buttons/text-fields/etc.
        root.add (websiteLabel, 0, 0);
        root.add (websiteEntry, 1, 0);

        root.add(usernameLabel,0,1);
        root.add(usernameEntry,1,1);


        root.add (passLabel, 0, 2);
        root.add (password, 1, 2);

        root.add (savePass, 0, 3);
        root.add (back, 1, 3);

        root.add (userMessage, 0, 4, 2, 1);

    }


}
