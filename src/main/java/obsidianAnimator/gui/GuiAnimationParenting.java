package obsidianAnimator.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import org.lwjgl.input.Mouse;

import net.minecraft.client.Minecraft;
import obsidianAPI.render.part.Part;
import obsidianAPI.render.part.PartObj;
import obsidianAnimator.Util;
import obsidianAnimator.gui.frames.HomeFrame;

public class GuiAnimationParenting extends GuiEntityRenderer 
{

	private ParentingFrame parentingFrame;
	private RelationFrame relationFrame;

	public GuiAnimationParenting(String entityName)
	{
		super(entityName);
		parentingFrame = new ParentingFrame();
		relationFrame = new RelationFrame();
	}

	@Override
	public void onGuiClosed()
	{
		super.onGuiClosed();
		parentingFrame.dispose();
		relationFrame.dispose();
	}

	@Override
	public void handleMouseInput()
	{
		scaleModifier += Mouse.getEventDWheel()/40;
		super.handleMouseInput();
	}

	private void attemptParent()
	{
		PartObj parent = getParent();
		PartObj child = getChild();
		if(parent.getName().equals(child.getName()))
			JOptionPane.showMessageDialog(parentingFrame, "Cannot parent a part to itself.", "Parenting issue", JOptionPane.ERROR_MESSAGE);
		else if(entityModel.parenting.hasParent(child))
		{
			Object[] options = {"OK", "Remove bend"};
			int n = JOptionPane.showOptionDialog(parentingFrame, child.getDisplayName() + " already has a parent.", "Parenting issue",
						JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,null,options,options[0]);
			if(n == 1)
			{
				entityModel.parenting.unParent(child);
				relationFrame.updateLabels();
			}
		}
		else
		{
			int n = JOptionPane.showConfirmDialog(parentingFrame, "Parent " + child.getDisplayName() + " to " + parent.getDisplayName() + "?", "Parenting", 
					JOptionPane.YES_NO_CANCEL_OPTION);
			if(n == 0)
				parent(parent, child);
		}
	}

	private void parent(PartObj parent, PartObj child) 
	{
		try
		{
			entityModel.setParent(child, parent);
			relationFrame.updateLabels();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			JOptionPane.showMessageDialog(parentingFrame, "Issue creating relation with bend. Parenting aborted.", "Parenting issue", JOptionPane.ERROR_MESSAGE);
		}
	}

	public boolean doesGuiPauseGame()
	{
		return false;
	}

	private PartObj getParent()
	{
		return Util.getPartObjFromName((String) parentingFrame.parentDropDown.getSelectedItem(), entityModel.parts);
	}

	private PartObj getChild()
	{
		return Util.getPartObjFromName((String) parentingFrame.childDropDown.getSelectedItem(), entityModel.parts);
	}

	private class ParentingFrame extends JFrame
	{
		JComboBox<Part> parentDropDown;
		JComboBox<Part> childDropDown;

		private ParentingFrame()
		{
			super("Parenting");

			JPanel mainPanel = new JPanel();
			mainPanel.setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();

			parentDropDown = new JComboBox<Part>();
			childDropDown = new JComboBox<Part>();
			for(Part p : parts)
			{
				if(p instanceof PartObj)
				{
					parentDropDown.addItem(p);
					childDropDown.addItem(p);
				}
			}
			parentDropDown.setRenderer(new PartComboBoxRenderer(true));
			childDropDown.setRenderer(new PartComboBoxRenderer(false));

			parentDropDown.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					selectedPart = (Part) ((JComboBox<String>) e.getSource()).getSelectedItem();
				}
			});

			childDropDown.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					hoveredPart = (Part) ((JComboBox<String>) e.getSource()).getSelectedItem();
				}
			});

			JButton relationButton = new JButton("Add relation");
			relationButton.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					attemptParent();
				}
			});

			JButton clearButton = new JButton("Clear");
			clearButton.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					entityModel.parenting.clear();
					relationFrame.updateLabels();
				}
			});

			JButton doneButton = new JButton("Done");
			doneButton.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					Minecraft.getMinecraft().displayGuiScreen(new GuiBlack());
					new HomeFrame().display();
				}
			});

			c.gridx = 0;
			c.gridy = 0;
			c.weightx = 1;
			c.weighty = 1;
			c.fill = GridBagConstraints.BOTH;
			mainPanel.add(new JLabel("Parent: "), c);
			c.gridx = 1;
			mainPanel.add(parentDropDown, c);
			c.gridx = 2;
			mainPanel.add(new JLabel("Child: "), c);
			c.gridx = 3;
			mainPanel.add(childDropDown, c);

			c.gridx = 0;
			c.gridy = 1;
			c.weightx = 2;
			c.gridwidth = 2;
			mainPanel.add(relationButton, c);
			c.weightx = 1;
			c.gridwidth = 1;
			c.gridx = 2;
			mainPanel.add(clearButton, c);
			c.gridx = 3;
			mainPanel.add(doneButton, c);

			setContentPane(mainPanel);
			pack();
			setAlwaysOnTop(true);
			setLocation(50, 50);
			setResizable(false);
			setVisible(true);
		}

	}

	private class RelationFrame extends JFrame
	{
		
		private JPanel mainPanel;
		private GridBagConstraints c;
		
		private RelationFrame()
		{
			super("Parenting");
			
			setMinimumSize(new Dimension(270,50));
			
			mainPanel = new JPanel();
			mainPanel.setLayout(new GridBagLayout());
			c = new GridBagConstraints();
			
			c.insets = new Insets(5,10,5,10);
			c.weightx = 1;
			c.weighty = 1;
			c.fill = GridBagConstraints.BOTH;
			updateLabels();
			
			setContentPane(mainPanel);
			pack();
			setAlwaysOnTop(true);
			setLocation(50, 150);
			setResizable(false);
			setVisible(true);
		}
		
		private void updateLabels()
		{
			mainPanel.removeAll();
			
			c.gridx = 0;
			c.gridy = 0;
			mainPanel.add(new JLabel("Parents"),c);
			c.gridx = 1;
			mainPanel.add(new JLabel("Children"),c);
			
			int h = 1;
			for(PartObj parent : entityModel.parenting.getAllParents())
			{
				c.gridx = 0;
				c.gridy = h;
				mainPanel.add(new JLabel(parent.getDisplayName()),c);
				c.gridx = 1;
				String s = "";
				for(PartObj child : entityModel.parenting.getChildren(parent))
				{
					s = s + child.getDisplayName() + ",";
				}
				if(s.length() > 1)
					s = s.substring(0, s.length() - 1);
				
				mainPanel.add(new JLabel(s),c);
				h++;
			}
			revalidate();
			pack();
		}
	}

	private class PartComboBoxRenderer extends BasicComboBoxRenderer
	{
		private boolean parentPart;

		private PartComboBoxRenderer(boolean parentPart)
		{
			this.parentPart = parentPart;
		}

		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) 
		{
			if(isSelected) 
			{
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
				if (index > -1) 
				{
					if(parentPart)
						selectedPart = parts.get(index);
					else
						hoveredPart = parts.get(index);
				}
			} 
			else 
			{
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}
			setFont(list.getFont());
			setText((value == null) ? "" : ((Part)value).getName());
			return this;
		}
	}

}


