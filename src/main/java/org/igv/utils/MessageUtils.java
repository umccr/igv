/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2007-2015 Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.igv.utils;

import java.util.Optional;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.broad.igv.Globals;
import org.broad.igv.ui.IGV;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;

/**
 * Provides thread-safe, Swing-safe, utilities for interacting with Alerts.  
 * 
 * @author jrobinso
 * @author eby Port to JavaFX
 */
// May need to account for owning Application
public class MessageUtils {

    private static Logger log = Logger.getLogger(MessageUtils.class);

    // Somewhat silly class, needed to pass values between threads
    static class ValueHolder {
        boolean boolValue = false;
        String stringValue = null;
    }

    /**
     * Log the exception and show {@code message} to the user
     *
     * @param e
     * @param message
     */
    public static void showErrorMessage(String message, Exception e) {
        log.error(message, e);
        showMessage(Level.ERROR, message);
    }

    public static void showMessage(String message) {
        showMessage(Level.INFO, message);
    }

    // Does it still need to be synchronized?
    public static synchronized void showMessage(Level level, String message) {

        log.log(level, message);
        boolean showDialog = !(Globals.isHeadless() || Globals.isSuppressMessages() || Globals.isTesting() || Globals.isBatch());
        if (showDialog) {
            Platform.runLater(() -> {
                // Always use HTML for message displays, but first remove any embedded <html> tags.
                String dlgMessage = "<html>" + message.replaceAll("<html>", "");

                // Using a very basic dialog type for now; can elaborate later as we determine needs.
                Alert dialog = new Alert(AlertType.INFORMATION, dlgMessage);
                dialog.showAndWait();
            });
        }
    }

    // Not yet converted to JavaFX equivalent
    public static void setStatusBarMessage(final String message) {
        log.debug("Status bar: " + message);
        if (IGV.hasInstance()) {
            IGV.getInstance().setStatusBarMessage(message);
        }
    }

    /**
     * Show a yes/no confirmation dialog.
     *
     * @param component
     * @param message
     * @return
     */
    // Does it still need to be synchronized?
    public static synchronized boolean confirm(final String message) {
        if(Globals.isHeadless() || Globals.isBatch()) {
            return true;
        }

        final ValueHolder returnValue = new ValueHolder();
        Platform.runLater(() -> {
            // Using a very basic dialog type for now; can elaborate later as we determine needs.
            Alert dialog = new Alert(AlertType.CONFIRMATION, message);
            dialog.showAndWait().filter(response -> response == ButtonType.OK).ifPresent(response -> returnValue.boolValue = true);
        });
        return (Boolean) returnValue.boolValue;
    }

    public static String showInputDialog(String message, String defaultValue) {

        //Pad message with spaces so it's as wide as the defaultValue
        if(defaultValue != null && message.length() < defaultValue.length()){
            message = String.format("%-" + defaultValue.length() + "s", message);
        }
        final String actMsg = message;

        // Using a very basic dialog type for now; can elaborate later as we determine needs.
        TextInputDialog dialog = new TextInputDialog(defaultValue);
        dialog.setContentText(actMsg);
        Optional<String> result = dialog.showAndWait();
        return (result.isPresent()) ? result.get() : defaultValue;
    }

    public static String showInputDialog(final String message) {
        return showInputDialog(message, null);
    }
}
