package com.chompcode.fylie.pages.PassMngr;

import static com.chompcode.fylie.apps.App.getConsole;
import com.chompcode.fylie.tools.Page;

import com.chompcode.fylie.apps.App;
import com.chompcode.fylie.apps.passmngr.PasswordManager;
import com.chompcode.fylie.tools.Page;
import com.chompcode.fylie.tools.PageManager;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.concurrent.ExecutionException;


public class PassWallPage extends Page
{

    public static final int MAXATTEMPTS = 3;

    private Text footer;
    private PasswordField pass;

    // New Lock Imagery
    private Canvas canvas;
    private GraphicsContext gc;
    ImageView lockBody;
    ImageView lockTumbler;
    ImageView lockShackle;

    public boolean lockedOut;

    public PassWallPage(GridPane root, PageManager pageManager)
    {
        super(root, pageManager);
    }

    @Override
    public void start()
    {
        if (getConsole().attempts >= MAXATTEMPTS){
            lockoutScreen();
            return;
        }

        canvas = new Canvas (250, 250);
        gc = canvas.getGraphicsContext2D ();
        lockBody = new ImageView (new Image ("file:files/padlock_body.png", 75, 75, true, true));
        lockTumbler = new ImageView (new Image ("file:files/padlock_tumbler.png", 25, 25, true, true));
        lockShackle = new ImageView (new Image ("file:files/padlock_shackle.png", 75 ,75 , true, true));

        Text header = new Text("Please enter your password below.");
        footer = new Text();

        Label passLabel = new Label("Password: ");
        pass = new PasswordField ();
        pass.setOnKeyPressed (e -> onKey (e.getCode ()));

        Button enter = new Button("Continue");

        enter.setOnAction(e -> loginAttempt());

        gc.drawImage (lockShackle.getImage (), 0, 0);
        gc.drawImage (lockBody.getImage (), 0, lockShackle.getImage ().getHeight ()-10);
        gc.drawImage (lockTumbler.getImage (), (lockBody.getImage ().getWidth () / 2) -
                (lockTumbler.getImage ().getWidth () / 2), (lockShackle.getImage ().getHeight ()-10) + (lockBody.getImage ().getHeight () / 2) - (lockTumbler.getImage ().getHeight () / 2));


        root.add(header,0,0);
        root.add(passLabel,0,1);
        root.add(pass,1,1);
        root.add(enter,0,2);
        root.add(footer,0,3);

        BorderPane b = new BorderPane ();
        b.setCenter (canvas);
        root.add (b, 0, 5, 3, 1);
        System.out.println (canvas.getWidth ());

    }

    void onKey (KeyCode k)
    {
        if (k.equals (KeyCode.ENTER))
        {
            loginAttempt ();
        }
    }

    void lockoutScreen()
    {
        lockedOut = true;
        getPageManager ().clear();
        Text locked = new Text("This session is locked out.");
        root.add(locked,0,0);

        /*for (App app : App.runningApps)
        {
            if (app instanceof PasswordManager)
            {
                if (((PasswordManager) app).pages.getCurrentPage () instanceof PassWallPage)
                {
                    ((PassWallPage) app.pages.getCurrentPage ()).lockoutScreen ();
                }
            }
        }*/
    }

    void loginAttempt ()
    {

        boolean loggedIn = PasswordManager.setupEncryption(pass.getText());

        for(App app : App.runningApps)
        {
            if (app instanceof PasswordManager)
            {
                if (loggedIn)
                {

                    app.pages.open(new StartPassPage(app.pages.root, app.pages));
                    getConsole().attempts = 0;
                }
                else
                {
                    jiggleLock ();
                    ((PassWallPage) app.pages.getCurrentPage()).pass.clear();

                    if (!((PassWallPage) app.pages.getCurrentPage ()).lockedOut)
                        ((PassWallPage) app.pages.getCurrentPage()).footer.setText("Invalid password. Attempts left: " + (MAXATTEMPTS - getConsole().attempts) + ".");

                    if (getConsole().attempts >= MAXATTEMPTS)
                        ((PassWallPage) app.pages.getCurrentPage()).lockoutScreen();
                }

            }
        }
        if(!loggedIn)
            getConsole().attempts++;
    }

    /**
     * Do a lock jiggling animation.
     */
    void jiggleLock()
    {
        AnimationTimer timer = new AnimationTimer ()
        {
            int t = 0;
            float a = 0;
            int stage = 0;

            @Override
            public void handle (long l)
            {
                if (t <= 6)
                {
                    gc.clearRect (0, 0, 250, 250);

                    gc.drawImage (lockShackle.getImage (), 0, 0);
                    gc.drawImage (lockBody.getImage (), 0, lockShackle.getImage ().getHeight ()-10);

                    gc.save ();

                    gc.translate ((lockBody.getImage ().getWidth () / 2) , (lockShackle.getImage ().getHeight ()-10) + (lockBody.getImage ().getHeight () / 2) - (lockTumbler.getImage ().getHeight () / 2));
                    gc.rotate (a);
                    gc.translate (-((lockBody.getImage ().getWidth () / 2)), -((lockShackle.getImage ().getHeight ()-10) + (lockBody.getImage ().getHeight () / 2) - (lockTumbler.getImage ().getHeight () / 2)));

                    gc.drawImage (lockTumbler.getImage (), (lockBody.getImage ().getWidth () / 2) -
                            (lockTumbler.getImage ().getWidth () / 2), (lockShackle.getImage ().getHeight ()-10) + (lockBody.getImage ().getHeight () / 2) - (lockTumbler.getImage ().getHeight () / 2));

                    gc.restore ();

                    t++;
                    if (stage == 0 || stage == 2)
                    {
                        a += 0.75;
                    }
                    if (stage == 1)
                    {
                        a -= 0.75;
                    }

                }
                else if (stage == 0)
                {
                    stage = 1;
                    t = -3;
                }
                else if (stage == 1)
                {
                    stage = 2;
                    t = 0;
                }

                else
                {
                    this.stop ();
                }


            }
        };

        timer.start ();
    }
}
