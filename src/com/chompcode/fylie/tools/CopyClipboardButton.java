package com.chompcode.fylie.tools;

import javafx.scene.control.Button;
import javafx.scene.control.TextInputControl;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

public class CopyClipboardButton extends Button
{
    TextInputControl input;

    public CopyClipboardButton (TextInputControl text, int w, int h)
    {
        super();
        this.input = text;
        this.setOnAction (e -> this.clicked());

        this.setMinSize (w, h);
        this.setMaxSize (w, h);

        Image clipBoardImage = new Image ("file:files/clipboard.png", this.getMinWidth (), this.getMinHeight (), true, true);
        ImageView clipBoardBImage = new ImageView (clipBoardImage);

        this.setGraphic (clipBoardBImage);
    }

    public CopyClipboardButton (TextInputControl text)
    {
        super();
        this.input = text;
        this.setOnAction (e -> this.clicked());

        this.setMinSize (25, 25);
        this.setMaxSize (25, 25);

        Image clipBoardImage = new Image ("file:files/clipboard.png", this.getMinWidth (), this.getMinHeight (), true, true);
        ImageView clipBoardBImage = new ImageView (clipBoardImage);

        this.setGraphic (clipBoardBImage);
    }

    void clicked ()
    {
        final Clipboard clipboard = Clipboard.getSystemClipboard ();
        final ClipboardContent content = new ClipboardContent ();
        content.putString (this.input.getText ());
        clipboard.setContent (content);
    }

}
