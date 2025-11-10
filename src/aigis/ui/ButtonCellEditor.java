package aigis.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

/**
 * cell editor and renderer with a button or checkbox.
 */
@SuppressWarnings("serial")
public class ButtonCellEditor extends AbstractCellEditor implements TableCellRenderer, TableCellEditor {

	interface CellEventListener {
		public void actionPerformed(JTable table, int row, int column, boolean check);
	}

	public static final String NORMAL = "[ボタン]";
	public static final String DISABLED = "[ボタンD]";
	public static final String CHECKED = "[ボタンC]";
	private final DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
	private JButton renderButton = null;
	private JCheckBox renderCheck = null;

	private CellEventListener eventListener;

	public ButtonCellEditor(final String title, final CellEventListener eventListener, boolean isCheck) {
		if (isCheck) {
			this.renderCheck = new JCheckBox(title);
		} else {
			this.renderButton = new JButton(title);
			this.renderButton.setDefaultCapable(false);
			this.renderButton.setMargin(new Insets(0, 0, 0, 0));
		}
		this.eventListener = eventListener;
	}

	@Override
	public Object getCellEditorValue() {
		return NORMAL;
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		if (NORMAL.equals(value) || DISABLED.equals(value) || CHECKED.equals(value)) {
			if (renderButton == null) {
				JCheckBox check = new JCheckBox(renderCheck.getText());
				check.setPreferredSize(new Dimension(10, 10));
				check.setSelected(CHECKED.equals(value));
				check.addChangeListener(new ChangeListener() {
					@Override
					public void stateChanged(ChangeEvent e) {
						ButtonCellEditor.this.eventListener.actionPerformed(table, row, column, check.isSelected());
					}
				});
				return check;
			} else {
				JButton button = new JButton(renderButton.getText());
				button.setDefaultCapable(false);
				button.setMargin(new Insets(0, 0, 0, 0));
				button.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						fireEditingStopped();
						ButtonCellEditor.this.eventListener.actionPerformed(table, row, column, false);
					}
				});
				return button;
			}
		} else {
			return null;
		}
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		if (NORMAL.equals(value) || DISABLED.equals(value) || CHECKED.equals(value)) {
			if (renderButton == null) {
				if (isSelected) {
					renderCheck.setBackground(table.getSelectionBackground());
				} else {
					renderCheck.setBackground(table.getBackground());
				}
				renderCheck.setEnabled(!DISABLED.equals(value));
				renderCheck.setSelected(CHECKED.equals(value));
				return renderCheck;
			} else {
				renderButton.setEnabled(!DISABLED.equals(value));
				return renderButton;
			}
		} else {
			return renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		}
	}
}
