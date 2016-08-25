package MCEntityAnimator.distribution;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;

import MCEntityAnimator.MCEA_Main;
import MCEntityAnimator.animation.AnimationData;
import MCEntityAnimator.animation.AnimationSequence;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public class DataHandler
{

	public static final String userPath = MCEA_Main.animationPath + "/user";
	public static final String sharedPath = MCEA_Main.animationPath + "/shared";
	
	
	//private static List<FileInfo> fileList = new ArrayList<FileInfo>();

	public static void downloadFileList()
	{
		try 
		{
			//System.out.println(ServerAccess.executeCommand("/home/shared/getFileData.sh dabigjoe"));
			ServerAccess.getFile("animation/user", "animation");
			ServerAccess.getFile("animation/shared", "/home/shared/animation");
			MCEA_Main.dataHandler.loadNBTData();
		} 
		catch (IOException e) {e.printStackTrace();}
		catch (JSchException e) {e.printStackTrace();}
	}

	public void saveNBTData()
	{	
		List<String> entityNames = getEntities();
		//GUI
		//writeNBTToFile(AnimationData.getGUISetupTag(entityNames), getGUIFile());
		//Entity data
		for(String entityName : entityNames)
		{
			//Parenting and part names
			writeNBTToFile(AnimationData.getEntityDataTag(entityName), getEntityFile(entityName, "data"));
			//Sequences

			List<String> changeSequences = AnimationData.getChangedSequences(entityName);
			for(AnimationSequence s : AnimationData.getSequences(entityName))
			{
				if(changeSequences.contains(s.getName()))
				{
					writeNBTToFile(s.getSaveData(), getAnimationFile(entityName, s.getName()));
				}
			}
		}
	}

	public void loadNBTData()
	{	
		List<String> entityNames = getEntities();
		//GUI
		//		File guiDataFile = getGUIFile();
		//		if(guiDataFile.exists())
		//			AnimationData.loadGUISetup(getNBTFromFile(guiDataFile));


		//Entity data
		for(String entityName : entityNames)
		{
			//Parenting and part names)
			File entityDataFile = getEntityFile(entityName, "data");
			if(entityDataFile.exists())
				AnimationData.loadEntityData(entityName, getNBTFromFile(entityDataFile));

			//Sequences
			for(File animationFile : getAnimationFiles(entityName))
			{
				AnimationSequence sequence = new AnimationSequence(entityName, getNBTFromFile(animationFile));
				AnimationData.addSequence(entityName, sequence);
			}
		}
	}

	/**
	 * Write an NBTTagCompound to a file.
	 */
	private static void writeNBTToFile(NBTTagCompound nbt, File file)
	{
		try 
		{
			if(!file.exists())
				file.createNewFile();
			CompressedStreamTools.writeCompressed(nbt, new FileOutputStream(file));
		} 
		catch (FileNotFoundException e) {e.printStackTrace();}
		catch (IOException e) {e.printStackTrace();}
	}

	/**
	 * Read an NBTTagCompound from a file.
	 */
	private static NBTTagCompound getNBTFromFile(File file)
	{
		try 
		{
			return CompressedStreamTools.readCompressed(new FileInputStream(file));
		} 
		catch (FileNotFoundException e) {throw new RuntimeException(e);}
		catch (IOException e) {throw new RuntimeException(e);}
	}

	/**
	 * Get the list of entities available for animation. Searches the /data/shared folder.
	 */
	public static List<String> getEntities()
	{
		List<String> entities = new ArrayList<String>();
		File dataFolder = new File(sharedPath);
		for(File file : dataFolder.listFiles())
		{
			if(file.isDirectory())
				entities.add(file.getName());
		}
		return entities;
	}

	private static File getGUIFile()
	{
		return new File(MCEA_Main.animationPath + "/data/GuiData.data");
	}

	/**
	 * Get an entity file from the shared folder
	 * @param entityName - Name of entity.
	 * @param ext - File extension (data, pxy, png or obj). No dot!
	 * @return Entity file.
	 */
	public static File getEntityFile(String entityName, String ext)
	{
		return new File(String.format("%s/%s/%s.%s", sharedPath, entityName, entityName, ext));
	}
	
	public static ResourceLocation getEntityResourceLocation(String entityName)
	{
		 return new ResourceLocation(String.format("animation:shared/%s/%s.png", entityName, entityName));
	}

	/**
	 * Get all the animations files for an entity.
	 * @param entityName - Name of entity.
	 * @return List of all animation files.
	 */
	private static List<File> getAnimationFiles(String entityName)
	{
		List<File> animationFiles = new ArrayList<File>();
		File animationFolder = new File(String.format("%s/%s", userPath, entityName));
		animationFolder.mkdir();
		for(File f : animationFolder.listFiles())
			animationFiles.add(f);
		return animationFiles;
	}

	/**
	 * Get the file for a single animation.
	 * @param entityName - Name of entity.
	 * @param animationName - Name of animation.
	 * @return Animation file.
	 */
	private static File getAnimationFile(String entityName, String animationName)
	{
		return new File(String.format("%s/%s/%s.mcea", userPath, entityName, animationName));
	}

}