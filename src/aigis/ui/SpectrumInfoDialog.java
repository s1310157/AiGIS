package aigis.ui;

import javax.swing.*;
import java.awt.*;

public class SpectrumInfoDialog extends JDialog {

    public SpectrumInfoDialog(JFrame owner, String title, String description) {
        super(owner, title, true);

        JTextArea area = new JTextArea(description);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(400, 250));

        add(scroll);
        pack();
    }
}
