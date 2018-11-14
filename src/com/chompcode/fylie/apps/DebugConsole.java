package com.chompcode.fylie.apps;

import com.chompcode.fylie.apps.passmngr.PasswordManager;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.Arrays;

public class DebugConsole extends App
{
    int height = 160, width = 500;
    TextField consIn;
    TextArea consOut;
    public int attempts;

    public DebugConsole ()
    {
        super("Debug Console", false);
        this.appID = 3;
        initiate ();
        super.height = height;
        super.width = width;
        super.runningApps.add(this);
    }

    void initiate ()
    {
        consIn = new TextField();

        consIn.setText (">> ");

        consOut = new TextArea();

        consOut.setEditable(false);
        consOut.setText("Starting console ...");


        consIn.setOnAction (e -> executeCommand());
        consIn.setOnKeyTyped (e -> resetConsIn());

        consIn.setStyle ("-fx-font-family: 'DejaVu Sans Mono', monospace");
        consOut.setStyle ("-fx-font-family: 'DejaVu Sans Mono', monospace");
    }

    @Override
    public void start (Stage primaryStage)
    {
        this.primaryStage = primaryStage;
        initialiseStage ();

        this.isOpen = true;

        primaryStage.setOnCloseRequest (e -> close());

        gridPane.add (consOut,0,0);
        gridPane.add (consIn,0,1);
        primaryStage.show();

        double width = primaryStage.getWidth ();
        consIn.setStyle ("-fx-min-width: " + width + "px;");
        consOut.setStyle ("-fx-min-width: " + width + "px;");

        primaryStage.widthProperty().addListener((obs, oldVal, newVal) -> resizeTextBoxes());

    }

    void resizeTextBoxes ()
    {
        double width = primaryStage.getWidth ();
        consIn.setStyle ("-fx-min-width: " + width + "px;");
        consOut.setStyle ("-fx-min-width: " + width + "px;");
    }

    void executeCommand ()
    {
        String input = consIn.getText (3, consIn.getLength ());
        outputToConsole (input);
        consIn.setText (">> ");

        String[] command = input.split (" ");
        String cmd = command[0].toLowerCase();
        String[] args = Arrays.copyOfRange (command, 1, command.length);


        if (cmd.equals("showrunningapps")){
            outputToConsole(App.runningApps.toString());
        }
        if (cmd.equals ("encrypt"))
        {
            StringBuilder text = new StringBuilder ();
            for (String arg : args)
            {
                text.append (arg);
            }
            try
            {
                outputToConsole (PasswordManager.encrypt (text.toString (), PasswordManager.key));
            }
            catch (Exception e)
            {
                outputToConsole (e.toString ());
                e.printStackTrace ();
            }
        }

        if (cmd.equals ("decrypt"))
        {
            StringBuilder text = new StringBuilder ();
            for (String arg : args)
            {
                text.append (arg);
            }
            try
            {
                outputToConsole (PasswordManager.decrypt (text.toString (), PasswordManager.key));
            }
            catch (Exception e)
            {
                outputToConsole (e.toString ());
                e.printStackTrace ();
            }
        }
        if (cmd.equals ("read"))
        {
            for (String s : readFile ("", args[0]))
            {
                outputToConsole (s);
            }
        }

        if (cmd.equals ("write"))
        {
            String[] lines = {String.join (" ", Arrays.copyOfRange (args, 1, args.length))};
            writeFile ("", args[0], lines, true);
        }
        consIn.positionCaret (3);
    }

    void resetConsIn ()
    {
        if (consIn.getLength () < 3)
        {
            consIn.setText (">> ");
            consIn.positionCaret (3);
        }
        else if  (!consIn.getText (0, 3).equals (">> "))
        {
            consIn.setText (">> ");
            consIn.positionCaret (3);
        }
    }

    public void outputToConsole (String s)
    {
        getConsole().consOut.appendText ("\n" + "> " + s);
        getConsole().consOut.setScrollTop (Double.MAX_VALUE);
    }

}
