import com.chompcode.fylie.apps.App;
import com.chompcode.fylie.apps.DebugConsole;
import com.chompcode.fylie.apps.passmngr.PasswordManager;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Main class.  Mostly handles launch options and some basic functions.
 */
public class Main extends Application
{
    public static void main (String[] args)
    {
        launch(args);
    }

    /**
     * Overrides {@link javafx.application.Application#start(Stage)} to provide some startup commands.
     */
    @Override
    public void start (Stage primaryStage)
    {
        //Starts the console.
        new DebugConsole();

        String[] p = getParameters ().getRaw ().toArray (new String[0]);

        // Connect to SQL database.
        App.connectToDB ();
        App.setupDB ();

        //Opens a hub.
        PasswordManager pw = new PasswordManager();
        pw.start(new Stage());

    }
}
