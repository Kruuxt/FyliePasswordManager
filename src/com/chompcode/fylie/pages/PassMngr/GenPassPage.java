package com.chompcode.fylie.pages.PassMngr;

import com.chompcode.fylie.apps.passmngr.PasswordManager;
import com.chompcode.fylie.tools.CopyClipboardButton;
import com.chompcode.fylie.tools.Page;
import com.chompcode.fylie.tools.PageManager;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class GenPassPage extends Page {

    public GenPassPage(GridPane root, PageManager pageManager)
    {
        super(root, pageManager);
    }

    @Override
    public void start(){
        // Text at the bottom of the page to display relevant information. (ie missing password, missing website, password stored)
        Text userMessage = new Text ();

        // == Init TextFields/Labels == //
        // TextField to hold web address.
        Label websiteLabel = new Label ("Website URL: ");
        TextField websiteEntry = new TextField ();
        // TextField to hold username.
        Label usernameLabel = new Label ("Username: ");
        TextField usernameEntry = new TextField ();
        // TextField to hold password length. (optional)
        Label lengthLabel = new Label ("Length: ");
        TextField lengthEntry = new TextField ();
        lengthEntry.setPromptText ("Default: 8");
        // Uneditable TextField for the password generator to output to.
        Label passwordLabel = new Label ("Output: ");
        TextField passwordOutput = new TextField ();
        passwordOutput.setEditable (false);

        // == Init Buttons == //
        // Button to generate new random password.
        Button generateButton = new Button ("Generate");
        // Button to return to startup page.
        Button back = new Button ("Back");
        back.setOnAction (e -> getPageManager().open (new StartPassPage(root, getPageManager())));

        // == Init Checkboxes == //
        // Password requires at least one lower case letter.
        CheckBox hasLow = new CheckBox ("Requires Lowercase ");
        hasLow.setSelected (true);
        // Password requires at least one upper case letter.
        CheckBox hasUp = new CheckBox ("Requires Uppercase ");
        // Password requires at least one number.
        CheckBox hasNum = new CheckBox ("Requires Numbers ");
        // Password requires at least one special character.
        CheckBox spChars = new CheckBox ("Requires Special Characters ");

        // HBox to store checkboxes for aesthetic reasons.
        HBox horizontalPane = new HBox ();
        horizontalPane.getChildren ().addAll (hasLow, hasUp, hasNum, spChars);

        // Button that saves the password to the SQL DB.
        Button savePass = new Button ("Store password");
        savePass.setOnAction (e -> PasswordManager.validatePasswordInput (websiteEntry, passwordOutput, usernameEntry, userMessage));

        // Button that copies generated password to clipboard.
        CopyClipboardButton copyClipboardButton = new CopyClipboardButton(passwordOutput, 25, 25);

        // On press event handler for the "generate new password" button. Sends required info to generateNewPassword().
        generateButton.setOnAction (e -> generateNewPassword (lengthEntry, passwordOutput, hasLow.isSelected (), hasUp.isSelected (), hasNum.isSelected (), spChars.isSelected ()));

        // Display all buttons / text-fields / etc.
        root.add (websiteLabel, 0, 0);
        root.add (websiteEntry, 1, 0);

        root.add (usernameLabel, 0, 1);
        root.add (usernameEntry, 1, 1);

        root.add (lengthLabel, 0, 2);
        root.add (lengthEntry, 1, 2);

        root.add (horizontalPane, 0, 3, 4, 1);

        root.add (generateButton, 0, 4, 2, 1);

        root.add (passwordLabel, 0, 5);
        root.add (passwordOutput, 1, 5);
        root.add (copyClipboardButton, 2, 5);

        root.add (savePass, 0, 6);

        root.add (back, 1, 7);
        root.add (userMessage, 0, 8, 2, 1);
    }
    /**
     * Generate a new password using the given {@link TextField}'s and {@link Character} sequence.
     * Set the text of output field equal to this new password.
     *
     * @param passLength of password.
     * @param output     object to display password.
     *                   TODO: requires some tweaking but generally works.
     */
    public static void generateNewPassword (TextField passLength, TextField output, boolean low, boolean up, boolean num, boolean spChars)
    {
        ArrayList<Integer> filters = new ArrayList<> ();
        if (low)
            filters.add (0);
        if (up)
            filters.add (1);
        if (num)
            filters.add (2);
        if (spChars)
            filters.add (3);

        Random rand = new Random ();
        ArrayList<Character> passwordArray = new ArrayList<> ();
        int length = 0;

        try
        {
            if (passLength.getText ().isEmpty ())
                length = 8;
            else
                length = Integer.parseInt (passLength.getText ());
        } catch (Exception e)
        {
            e.printStackTrace ();
            passLength.clear ();
        }

        for (int f : filters)
        {
            int randChar = rand.nextInt (getCharType (f).length);
            passwordArray.add (getCharType (f)[randChar]);
        }

        for (int i = 0; i < length - filters.size (); i++)
        {
            if (filters.size () == 0)
                break;
            int type = filters.get (rand.nextInt (filters.size ()));
            int randChar = rand.nextInt (getCharType (type).length);
            passwordArray.add (getCharType (type)[randChar]);
        }

        Collections.shuffle (passwordArray);

        StringBuilder password = new StringBuilder ();
        for (Character s : passwordArray)
        {
            password.append (s);
        }
        output.setText (password.toString ());
    }
    /**
     * Simple method that holds a switch to return an array of chars based on input int.
     *
     * @param type is essentially an id for the different character arrays.
     * @return an array of characters.
     */
    public static char[] getCharType (int type)
    {
        switch (type)
        {
            case 0:
                return PasswordManager.lLetters;
            case 1:
                return PasswordManager.uLetters;
            case 2:
                return PasswordManager.nums;
            case 3:
                return PasswordManager.sChars;
            default:
                return PasswordManager.lLetters;
        }
    }



}
