package com.chompcode.fylie.apps;
import com.chompcode.fylie.apps.passmngr.PasswordManager;

import com.chompcode.fylie.tools.PageManager;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.*;
import java.util.ArrayList;
import java.sql.*;

/**
 * Fylie's app manager. Contains all the default variables for our apps, as well as some basic mechanics/tools/methods.
 */
public abstract class App extends Application
{

    //Window variables
    public Stage primaryStage;
    public GridPane gridPane;
    public PageManager pages;

    //Holds the title of an app
    String appTitle;

    //Flag to determine if the menu bar should be displayed
    boolean hasMenu;

    //Holds a list of all running apps
    public static ArrayList<App> runningApps = new ArrayList<> ();

    static String url = "jdbc:sqlite:files/database.db";
    public static Connection conn;

    //Flag to determine if there are any instances of a certain app open
    public boolean isOpen;

    //Default height and width of an app
    int height = 600;
    int width = 600;

    //A unique integer ID for each app
    public int appID;

    public MenuBar menuBar;


    public App (String appTitle, boolean hasMenu)
    {
        this.appTitle = appTitle;
        this.hasMenu = hasMenu;
    }

    Stage getStage ()
    {
        return this.primaryStage;
    }

    /**
     * initialiseStage sets up the window and some basic mechanics.
     * Creates a root object to attach {@link Node} to, ({@link GridPane} if no {@link MenuBar}, {@link  VBox} otherwise).
     * Also creates a {@link Scene} to hold the root, and sets the scene.
     */
    public void initialiseStage ()
    {
        if(!(this instanceof DebugConsole))
            runningApps.add(this);
        primaryStage.setTitle (appTitle);
        primaryStage.setOnCloseRequest (e -> close());

        gridPane = new GridPane ();

        Node root = gridPane;

        if (hasMenu)
            root = addFileMenu ();

        Scene scene = new Scene ((Parent) root, width, height);
        primaryStage.setScene (scene);
    }

    /**
     * Holds the logic for, and creates the Menu object of the "File" tab in the menu bar.
     * Called later in the {@link #createMenu()} method.
     * @return File Menu object
     */
    Menu createFileMenu()
    {
        Menu fileMenu = new Menu("File");

        return fileMenu;
    }

    /**
     * Holds the logic for, and creates the Menu object of the "Edit" tab in the menu bar.
     * Called later in the {@link #createMenu()} method.
     * @return Edit Menu object
     */
    Menu createEditMenu()
    {
        Menu editMenu = new Menu("Edit");

        return editMenu;
    }

    /**
     * Holds the logic for, and creates the Menu object of the "apps" tab in the menu bar.
     * Called later in the {@link #createMenu()} method.
     * @return File Menu object
     */

    /**
     * Stitches together the MenuBar with all the different "create___Menu()" functions.
     * @return Menu Bar
     */
    public MenuBar createMenu ()
    {
        ArrayList<Menu> menus = new ArrayList<>();

        // Create Menu
        menuBar = new MenuBar ();
        // File menu stuff
        menus.add(this.createFileMenu());
        //Edit menu stuff
        menus.add(this.createEditMenu());
        // apps menu (access other apps)

        menuBar.getMenus ().addAll (menus);

        return menuBar;
    }

    /**
     * Adds the file menu to the top using a {@link VBox} in order to keep the ordering of the {@link GridPane} tidy.
     */
    VBox addFileMenu ()
    {
        VBox vBox = new VBox ();

        MenuBar menuBar = createMenu ();
        vBox.getChildren ().add (menuBar);

        vBox.getChildren ().add (gridPane);

        return vBox;
    }

    /**
     * Passes an app to the {@link #tryOpening(App)} function.  Opens a window without closing the previous one.
     * A slightly redundant method put into place for future-proofing reasons.
     * @param newApp com.chompcode.fylie.apps.App to open
     */
    public void openApp (App newApp) {
        if(newApp instanceof DebugConsole){
            if(getConsole().isOpen)
                getConsole().primaryStage.toFront();
            else {
                tryOpening(newApp);
                getConsole().isOpen = true;
            }
        }
        else
            tryOpening(newApp);
    }

    /**
     * Passes an app to the {@link #tryOpening(App)} function.  Opens a window while closing the previous one.
     * @param newApp com.chompcode.fylie.apps.App to open
     */
    public void switchApp (App newApp)
    {
        tryOpening(newApp);
        this.close();
    }

    /**
     * A simple method to try and open the app, catches any error.  Just here so we don't need multiple
     * try catch statements.
     * @param newApp com.chompcode.fylie.apps.App attempting to open.
     */
    public void tryOpening (App newApp)
    {
        try
        {
            newApp.start (new Stage ());
        }
        catch (Exception e)
        {
            getConsole().outputToConsole ("Error opening app in com.chompcode.fylie.apps.App.class line 145: " + e + ".");
            e.printStackTrace ();
        }
    }

    /**
     * Statement to replace default close function.  Removes the closed app from {@link #runningApps}, checks if
     * it is closing the last instance of an app, and then closes the app.
     */
    void close()
    {
        getConsole().outputToConsole("Closing " + this.appTitle);
        //Removes the app from the runningApps array as long as its not the console.
        if (!(this instanceof DebugConsole))
            runningApps.remove(this);
        else
            getConsole().isOpen = false;
        //If the last instance of a particular app is closed, isOpen changes to false.
        int appCount = 0;

        for (App app : runningApps)
            if (app == this)
                appCount++;
        if (appCount == 0)
            isOpen=false;

        //Finally closes the app
        this.primaryStage.close();

        if(runningApps.size () == 1){
            App.closeDB ();
        }
    }

    /**
     * Checks the {@link #runningApps} array for an instance of {@link DebugConsole}, which there should only be one of.
     * @return The console
     */
    public static DebugConsole getConsole ()
    {

        for (App a : runningApps)
        {
            if (a instanceof DebugConsole) return (DebugConsole) a;
        }

        return null;
    }

    /**
     * Reads file and returns array of each line.
     * @param path to file, including '/'.
     * @param fileName of desired file + .extension.
     * @param make whether or not to make a new file if the supplied one is not found.
     * @return String[] containing each line in the file.
     */
    String[] readFile (String path, String fileName, boolean make)
    {
        ArrayList<String> fileIn = new ArrayList<> ();
        try
        {
            File file = new File (path + fileName);
            if (make)
            {
                if (file.createNewFile ())
                {
                    getConsole ().outputToConsole ("File '" + path + fileName +"' created.");

                } else
                {
                    getConsole ().outputToConsole ("Found '" + path + fileName +"'.");
                }
            }

            BufferedReader reader = new BufferedReader (new FileReader (file));
            String line;
            while ((line = reader.readLine()) != null)
            {
                fileIn.add (line);
            }

            getConsole().outputToConsole ("'" + path + fileName + "' read successfully.");
            return fileIn.toArray (new String[0]);
        }
        catch (Exception e)
        {
            e.printStackTrace ();
            getConsole ().outputToConsole (e.toString ());

        }
        return null;

    }

    /**
     * For overloading purposes, see return function, by default do not make a new file.
     */
    String[] readFile (String path, String fileName)
    {
        return readFile (path, fileName, false);
    }

    /**
     * Writes text to a file.
     * @param path of file, including '/'.
     * @param fileName of desired file + .extension.
     * @param text to write to file, in a String array of lines format.
     * @param overWrite whether or not to overwrite the file if one is found.
     */
    void writeFile (String path, String fileName, String[] text, boolean overWrite)
    {
        try
        {
            File file = new File (path + fileName);
            if (file.createNewFile ())
            {
                getConsole ().outputToConsole ("File '" + path + fileName + "' created.");

            }
            else
            {
                getConsole ().outputToConsole ("Found existing '" + path + fileName + "'.");
                //If no overwriting, halt execution when file is found.
                if (!overWrite)
                    return;
            }
            // StringBuilder used to turn array of strings into single string.
            StringBuilder lines = new StringBuilder ();
            for (String aText : text)
            {
                lines.append (aText).append("\n");
            }
            BufferedWriter writer = new BufferedWriter(new FileWriter (file));
            writer.write(lines.toString ());

            writer.close();

            getConsole ().outputToConsole ("Wrote text to file successfully.");
        }
        catch (Exception e)
        {
            getConsole ().outputToConsole (e.toString ());
            e.printStackTrace ();
        }
    }


    /**
     * Connect to, or create, SQL database specified, and store connection as static variable for access anywhere.
     */
    public static void connectToDB ()
    {
        try
        {
            conn = DriverManager.getConnection (url, "admin", "admin");

            System.out.println("Connection to database established.");
        }
        catch (SQLException e)
        {
            System.out.println (e.getMessage ());
        }
    }

    public static void setupDB ()
    {
        String passwordSQL = "CREATE TABLE IF NOT EXISTS `passwords` (\n" +
                "id integer PRIMARY KEY,\n" +
                "website text NOT NULL,\n" +
                "pass text NOT NULL,\n" +
                "username TEXT NOT NULL\n" +
                ");";
        String eventsSQL = "CREATE TABLE IF NOT EXISTS events (\n" +
                "id integer PRIMARY KEY, \n" +
                "startDate date NOT NULL, \n" +
                "endDate date NOT NULL \n," +
                "text text NOT NULL" +
                ");";

        try
        {
            Statement statement = conn.createStatement ();
            statement.execute (passwordSQL);
            statement.execute (eventsSQL);
        }
        catch (SQLException e)
        {
            System.out.println (e.getMessage ());
        }
    }

    static void closeDB()
    {
        try
        {
            conn.close ();
            System.out.println ("Connection to database closed.");
        }
        catch (SQLException e)
        {
            e.printStackTrace ();
        }
    }
}