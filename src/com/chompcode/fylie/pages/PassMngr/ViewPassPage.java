package com.chompcode.fylie.pages.PassMngr;

import com.chompcode.fylie.apps.App;
import com.chompcode.fylie.apps.passmngr.PasswordManager;
import com.chompcode.fylie.tools.CopyClipboardButton;
import com.chompcode.fylie.tools.PageManager;
import com.chompcode.fylie.tools.Page;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ViewPassPage extends Page {

    public ViewPassPage (GridPane root, PageManager pageManager)
    {
        super(root, pageManager);
    }

    @Override
    public void start(){
        // Button that returns the user back to the startup page.
        Button back = new Button ("Back");
        back.setOnAction (e -> getPageManager ().open(new StartPassPage(root, getPageManager ())));

        try
        {
            // Select all info from password table.
            String sqlGetAllPasswords = "SELECT id, username, website, pass FROM passwords WHERE id > ?";
            PreparedStatement statement = App.conn.prepareStatement (sqlGetAllPasswords);
            statement.setInt (1, -1);
            ResultSet rs = statement.executeQuery ();

            // Row offset counter.
            int row = 0;
            while (rs.next ())
            {
                // Get values from the search.
                int id = rs.getInt ("id");
                String web = rs.getString ("website");
                String encryptedPass = rs.getString ("pass");
                String pass = PasswordManager.decrypt (encryptedPass, PasswordManager.key);
                String username = rs.getString ("username");

                // Create new UI elements to hold the database view.
                Label webLabel = new Label (web);
                Label userLabel = new Label (username);
                TextField passField = new TextField ();
                passField.setText (pass);
                passField.setEditable (false);

                CopyClipboardButton ccb = new CopyClipboardButton(passField);

                // Deletion button.
                Button delete = new Button ("X");
                delete.setOnAction (e -> {
                    PasswordManager.deletePassword (id, web);
                    getPageManager ().open (new ViewPassPage(root, getPageManager ()));
                });

                root.add (webLabel, 0, row);
                root.add (userLabel, 1, row);
                root.add (passField, 2, row);
                root.add (ccb, 3, row);
                root.add (delete, 4, row);

                row++;
            }

            root.add (back, 0, row);

        }
       catch (SQLException | GeneralSecurityException | IOException e)
        {
            e.printStackTrace ();
        }

    }
}
