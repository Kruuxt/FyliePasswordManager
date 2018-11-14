package com.chompcode.fylie.apps.passmngr;

import com.chompcode.fylie.apps.App;
import com.chompcode.fylie.pages.PassMngr.*;
import com.chompcode.fylie.tools.*;

import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class PasswordManager extends App
{

    GridPane root;
    public static SecretKeySpec key;

    //Storing different categories of characters into their own arrays to call down the line.
    public static final char[] lLetters = "abcdefghiklmnopqrstuvwxyz".toCharArray ();
    public static final char[] uLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray ();
    public static final char[] nums = "1234567890".toCharArray ();
    public static final char[] sChars = " !\"#$%&'()*+,-./:;<=>?@[]\\^_`{}|~".toCharArray ();
    
    public PasswordManager ()
    {
        super ("Password Manager", true);
        isOpen = true;
        this.appID = 4;
    }

    @Override
    public void start (Stage primaryStage)
    {
        this.primaryStage = primaryStage;
        // Grab encryption/decryption key at start-up.
        //String k = getKey ();

        initialiseStage ();

        root = new GridPane ();
        pages = new PageManager(root);

        pages.open(new PassWallPage(root, pages));
        gridPane.add (root, 0, 0);
        primaryStage.show ();
    }

    /**
     * Overriding the {@link #createMenu()} function to limit the items being displayed in the menubar to just the app selector.
     * @return the {@link MenuBar} that holds all the {@link Menu} objects we plan on using.
     */
    @Override
    public MenuBar createMenu ()
    {
        ArrayList<Menu> menus = new ArrayList<> ();

        // Create Menu.
        menuBar = new MenuBar ();

        // apps menu (access other apps).

        menuBar.getMenus ().addAll (menus);

        return menuBar;
    }

    /**
     * Adds a user, pass, and web address into the SQL.
     * @param password
     * @param web
     */
    public static void addPassword (String password, String web, String user)
    {
        try
        {
            String encrypted = encrypt (password, key);
            // Insert into password table the supplied strings.
            String sql = "INSERT INTO passwords (website, pass, username) VALUES (?,?,?)";
            PreparedStatement statement = App.conn.prepareStatement (sql);
            statement.setString (1, web);
            statement.setString (2, encrypted);
            statement.setString (3, user);
            statement.executeUpdate ();
        }
        catch (SQLException | UnsupportedEncodingException | GeneralSecurityException e)
        {
            System.out.println (e.getMessage ());
        }
    }

    /**
     * Remove a password from the SQL DB.  Displays pop-up for confirmation.
     * @param id
     * @param siteName
     */
    public static void deletePassword (int id, String siteName)
    {
        Alert confirmBox = new Alert (Alert.AlertType.CONFIRMATION);
        confirmBox.setTitle ("Confirm deletion");
        confirmBox.setHeaderText ("Attempting to delete password for the website: " + siteName);
        confirmBox.setContentText ("Continue with deletion?");
        Optional<ButtonType> result = confirmBox.showAndWait ();
        if (result.isPresent ())
        {
            if (result.get () == ButtonType.OK)
            {
                try
                {
                    String sqlDeletePassword = "DELETE FROM passwords" +
                            " WHERE id = ?";

                    PreparedStatement statement = App.conn.prepareStatement (sqlDeletePassword);
                    statement.setInt (1, id);
                    statement.executeUpdate ();
                }
                catch (SQLException e)
                {
                    e.printStackTrace ();
                }
            }
        }
    }

    /**
     * Sets up encryption keys 'stuff' for later.
     * TODO: rewrite this comment.
     */
    public static boolean setupEncryption (String k)
    {
        try
        {
            key = createSecretKey (k.toCharArray (), "salt".getBytes (), 1, 128);

            // Validate key on startup, using dummy database field.
            String sql = "SELECT pass FROM passwords WHERE id = ?";
            PreparedStatement statement = App.conn.prepareStatement (sql);
            // Store 'dummy' value in ID = -1, since values go from 0 -> INF will not be overwritten.
            statement.setInt (1, -1);
            ResultSet rs = statement.executeQuery ();
            if (rs.next ())
            {
                String test = decrypt (rs.getString ("pass"), key);
                if (!test.equals ("123456789"))
                {
                    throw new BadPaddingException();
                }
            }
            // Insert dummy data if it is not found.
            else
            {
                sql = "INSERT INTO passwords (id, website, username, pass) VALUES (?, ?, ?, ?)";
                statement = App.conn.prepareStatement (sql);
                statement.setInt (1, -1);
                statement.setString (2, "");
                statement.setString (3, "");
                statement.setString (4, encrypt ("123456789", key));
                statement.executeUpdate ();
            }
            return true;
        }
        // Read as: wrong key exception.
        catch (BadPaddingException e)
        {
            System.out.println ("Invalid key provided.");
            return false;
        }
        catch (Exception e)
        {
            getConsole ().outputToConsole (e.toString ());
            e.printStackTrace ();
            return false;
        }
    }

    /**
     * Generates a keyGenerator, and sets the encryption algorithm to use.
     *
     * @param password       to use for encryption.
     * @param salt           to modify password, acts as additional section of password.
     * @param iterationCount to prevent brute force.
     * @param keyLength      length of each string produced.
     * @return {@link SecretKeySpec} that can produce new encrypted strings.
     * @throws NoSuchAlgorithmException if invalid algorithm is supplied.
     * @throws InvalidKeySpecException  if some part of the encryption process fails.
     */
    static SecretKeySpec createSecretKey (char[] password, byte[] salt, int iterationCount, int keyLength) throws NoSuchAlgorithmException, InvalidKeySpecException
    {
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance ("PBKDF2WithHmacSHA512");
        PBEKeySpec keySpec = new PBEKeySpec (password, salt, iterationCount, keyLength);
        SecretKey keyTmp = keyFactory.generateSecret (keySpec);
        return new SecretKeySpec (keyTmp.getEncoded (), "AES");
    }

    /**
     * Uses #key to encrypt a string.
     * @param property string to encrypt.
     * @param key      to encrypt using.
     * @return {@link String}, encrypted text.
     * @throws GeneralSecurityException     TODO: figure out what this means.
     * @throws UnsupportedEncodingException encoding is invalid (weird characters used).
     */
    public static String encrypt (String property, SecretKeySpec key) throws GeneralSecurityException, UnsupportedEncodingException
    {
        Cipher pbeCipher = Cipher.getInstance ("AES/CBC/PKCS5Padding");
        pbeCipher.init (Cipher.ENCRYPT_MODE, key);

        AlgorithmParameters parameters = pbeCipher.getParameters ();
        IvParameterSpec ivParameterSpec = parameters.getParameterSpec (IvParameterSpec.class);
        byte[] cryptoText = pbeCipher.doFinal (property.getBytes (StandardCharsets.UTF_8));
        byte[] iv = ivParameterSpec.getIV ();
        return base64Encode (iv) + ":" + base64Encode (cryptoText);
    }

    /**
     * Convert String to Base64, (AES needs strings in sections of 2**n bytes.
     *
     * @param bytes input array of bytes.
     * @return Base64 converted array of bytes.
     */
    static String base64Encode (byte[] bytes)
    {
        return Base64.getEncoder ().encodeToString (bytes);
    }

    /**
     * Uses #key to decrypt a string.
     *
     * @param string encrypted string.
     * @param key    to decrypt using.
     * @return original text.
     * @throws GeneralSecurityException TODO: figure out what this means.
     * @throws IOException              TODO: figure out why {@link IOException}
     */
    public static String decrypt (String string, SecretKeySpec key) throws GeneralSecurityException, IOException
    {
        String iv = string.split (":")[0];
        String property = string.split (":")[1];
        Cipher pbeCipher = Cipher.getInstance ("AES/CBC/PKCS5Padding");
        pbeCipher.init (Cipher.DECRYPT_MODE, key, new IvParameterSpec (base64Decode (iv)));
        return new String (pbeCipher.doFinal (base64Decode (property)), "UTF-8");
    }

    /**
     * Convert from Base64 String to regular string.
     *
     * @param property
     * @return
     */
    static byte[] base64Decode (String property)
    {
        return Base64.getDecoder ().decode (property);
    }

    /**
     * Check the SQL database for duplicate passwords.
     * @param webEntry website entry field.
     * @param passEntry password entry field.
     * @param usernameEntry username entry field.
     * @param output
     */
    public static void validatePasswordInput (TextField webEntry, TextField passEntry, TextField usernameEntry, Text output)
    {
        try
        {
            // Find all records with same details as the ones entered.
            String sqlCheckDuplicates = "SELECT website, username FROM passwords WHERE website = ? AND username = ?";
            PreparedStatement statement = App.conn.prepareStatement (sqlCheckDuplicates);
            statement.setString (1, webEntry.getText ());
            statement.setString (2, usernameEntry.getText ());

            ResultSet rs = statement.executeQuery ();

            // If there are not any results from the search.
            if (!rs.next())
            {
                // Check fields aren't empty.
                if (!passEntry.getText ().isEmpty () && !webEntry.getText ().isEmpty () && ! usernameEntry.getText ().isEmpty ())
                {
                    addPassword (passEntry.getText (), webEntry.getText (), usernameEntry.getText ());
                    output.setText ("Password Stored.");
                }
                else
                {
                    if (passEntry.getText ().isEmpty ())
                        output.setText ("Password field is empty.");
                    else if (webEntry.getText ().isEmpty ())
                        output.setText ("Website field is empty.");
                    else if (usernameEntry.getText ().isEmpty ())
                        output.setText ("Username field is empty.");

                }
            }
            else
                output.setText ("Password already stored.");

        }
        catch (SQLException e)
        {
            System.out.println (e.getMessage ());
        }
    }

    String getKey (int attemptNo)
    {
        // Simple recursion prevention, if they enter something completely invalid 3 times close the app.
/*
        if (attemptNo > 3)
        {
            Alert closingBox = new Alert (Alert.AlertType.WARNING);
            closingBox.setTitle ("Too Many Attempts");
            closingBox.setHeaderText ("Too many invalid key entry attempts, closing.");
            closingBox.showAndWait ();
            closeDB ();
            Platform.exit();
            return null;
        }
*/
        // Get user to input a key.

        TextInputDialog dialog = new TextInputDialog ();
        dialog.setTitle ("Key Required to View this PasswordPages.Page");
        dialog.setHeaderText ("Please enter your decryption key:\n(" + (attemptNo) + ") attempts.");
        dialog.setContentText ("Key:");
        Optional<String> result = dialog.showAndWait ();

        return result.get();
/*
        // If key is entered, return it.
        if (result.isPresent ())
        {
            if (!result.get ().isEmpty () && !result.get ().equals (""))
            {
                return result.get ();
            }
        }
        else {
            close();
        }
        return null;*/
    }

    // Over-loader so function can be called without explicit (1) parameter.
    String getKey ()
    {
        return getKey (1);
    }
}
