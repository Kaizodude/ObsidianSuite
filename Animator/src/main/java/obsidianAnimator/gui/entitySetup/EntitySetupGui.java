package obsidianAnimator.gui.entitySetup;

import org.lwjgl.input.Keyboard;

import obsidianAPI.render.part.PartObj;
import obsidianAnimator.gui.entityRenderer.GuiEntityRenderer;

public class EntitySetupGui extends GuiEntityRenderer
{

	private EntitySetupController controller;

	public EntitySetupGui(String entityName, EntitySetupController controller) 
	{
		super(entityName);		
		this.controller = controller;
	}

	@Override
	protected void keyTyped(char par1, int par2)
	{
		if(par2 == Keyboard.KEY_ESCAPE)
			controller.close();
		else
			super.keyTyped(par1, par2);
	}

	@Override
	protected void mouseClicked(int x, int y, int i) 
	{
		super.mouseClicked(x, y, i);
		if(i == 1 && hoveredPart != null)
			controller.attemptParent((PartObj)selectedPart, (PartObj)hoveredPart);
	}
}
