/*
 * Decompiled with CFR 0_114.
 */
package migracao.swingreverse;

import javax.swing.*;
import java.io.IOException;
import java.io.OutputStream;

public class CustomOutputStream
extends OutputStream {
    private JTextArea textArea;

    public CustomOutputStream(JTextArea textArea) {
        this.textArea = textArea;
    }

    @Override
    public void write(int b) throws IOException {
        this.textArea.append(String.valueOf((char)b));
        this.textArea.setCaretPosition(this.textArea.getDocument().getLength());
    }
}

